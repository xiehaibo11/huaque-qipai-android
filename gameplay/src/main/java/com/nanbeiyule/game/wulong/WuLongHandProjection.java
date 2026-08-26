package com.nanbeiyule.game.wulong;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.IntPredicate;

/**
 * The recovered local-only 30588 hand order and positions.
 *
 * <p>This ports {@code BaseWuLong/Logic/CardLogic.lua:getCardIDsBySortType} and
 * {@code Modules/CardLayer/CardArea.lua:getCardsPositionAndZorder}.  The latter intentionally
 * uses the WuLong hand override: a 27-card visual baseline, 90px ordinary spacing and 6px for
 * adjacent equal powers (or adjacent jokers), then centres the resulting run.  The direct
 * {@code HandCardConf} still declares 71 cards for bottom/top and disables BaseCardGame auto
 * spacing; its custom hand branch is what makes the 54/75-card run fit without a guessed second
 * row strategy.
 *
 * <p>Each placement retains its original authenticated server-hand index.  It is display/input
 * data only: the backend remains the sole authority for ownership, type, turn and precedence.
 */
public final class WuLongHandProjection {
    private static final int BASELINE_CARD_COUNT = 27;
    private static final float BASELINE_DISTANCE_X = 63f;
    private static final float ORDINARY_DISTANCE_X = 90f;
    private static final float SAME_POWER_DISTANCE_X = 6f;
    private static final int SMALL_JOKER = 53;
    private static final int BIG_JOKER = 54;

    public record CardPlacement(int sourceIndex, int cardId, WuLongTableLayout.CardBounds bounds) {}

    private record SourceCard(int sourceIndex, int cardId, int power) {}

    private WuLongHandProjection() {}

    /** Produces the one ordered set of own-hand rectangles for both Canvas and touch handling. */
    public static List<CardPlacement> project(List<Integer> serverHand, IntPredicate selectedSourceIndex) {
        Objects.requireNonNull(serverHand, "serverHand");
        Objects.requireNonNull(selectedSourceIndex, "selectedSourceIndex");
        List<SourceCard> ordered = sortLikeRecoveredClient(serverHand);
        if (ordered.isEmpty()) return List.of();

        float[] anchor = WuLongTableLayout.cocosHandAnchor(1, 1); // self maps to local-bottom.
        float[] centers = new float[ordered.size()];
        centers[0] = (BASELINE_CARD_COUNT - 1) * BASELINE_DISTANCE_X / 2f;
        for (int index = 1; index < ordered.size(); index++) {
            centers[index] = centers[index - 1] - (samePowerOrJoker(ordered.get(index - 1), ordered.get(index))
                    ? SAME_POWER_DISTANCE_X : ORDINARY_DISTANCE_X);
        }
        float baselineWidth = (BASELINE_CARD_COUNT - 1) * BASELINE_DISTANCE_X;
        float currentWidth = centers[0] - centers[centers.length - 1];
        float centeringOffset = (baselineWidth - currentWidth) / 2f;

        List<CardPlacement> result = new ArrayList<>(ordered.size());
        for (int index = 0; index < ordered.size(); index++) {
            SourceCard card = ordered.get(index);
            result.add(new CardPlacement(card.sourceIndex(), card.cardId(),
                    WuLongTableLayout.handCardBoundsAtCocosCenter(anchor[0] + centers[index] - centeringOffset,
                            anchor[1], WuLongTableLayout.BOTTOM_HAND_SCALE,
                            selectedSourceIndex.test(card.sourceIndex()))));
        }
        return List.copyOf(result);
    }

    /** Reverse order matches Canvas painter order, so a visible overlapped card is selected. */
    public static int hitTest(List<CardPlacement> placements, float x, float y) {
        for (int index = placements.size() - 1; index >= 0; index--) {
            CardPlacement placement = placements.get(index);
            if (placement.bounds().contains(x, y)) return placement.sourceIndex();
        }
        return -1;
    }

    private static List<SourceCard> sortLikeRecoveredClient(List<Integer> serverHand) {
        List<SourceCard> input = new ArrayList<>(serverHand.size());
        boolean allBacks = !serverHand.isEmpty();
        for (int index = 0; index < serverHand.size(); index++) {
            Integer cardId = Objects.requireNonNull(serverHand.get(index), "server hand card");
            allBacks &= cardId == 0;
            input.add(new SourceCard(index, cardId, WuLongCardRules.power(cardId)));
        }
        if (allBacks) return input;

        Map<Integer, List<SourceCard>> byPower = new HashMap<>();
        for (SourceCard card : input) byPower.computeIfAbsent(card.power(), ignored -> new ArrayList<>()).add(card);
        byPower.values().forEach(cards -> cards.sort(Comparator.comparingInt(SourceCard::cardId)
                // Equal physical IDs are indistinguishable to the server; retain their received order for touch.
                .thenComparingInt(SourceCard::sourceIndex)));

        int jokerCount = jokerCount(input);
        Map<Integer, List<Integer>> powersByCount = new TreeMap<>();
        if (jokerCount < 4) {
            for (int power = 3; power <= 17; power++) addPower(powersByCount, byPower, power);
        } else {
            for (int power = 3; power <= 15; power++) addPower(powersByCount, byPower, power);
            int bigJokerCount = countCard(input, BIG_JOKER);
            int kingBombLine = kingBombLine(jokerCount, bigJokerCount);
            if (bigJokerCount == 0) addPowerAtCount(powersByCount, 16, kingBombLine);
            else if (bigJokerCount == jokerCount) addPowerAtCount(powersByCount, 17, kingBombLine);
            else {
                addPowerAtCount(powersByCount, 16, kingBombLine);
                addPowerAtCount(powersByCount, 17, kingBombLine);
            }
        }

        List<SourceCard> result = new ArrayList<>(input.size());
        for (Map.Entry<Integer, List<Integer>> entry : powersByCount.entrySet()) {
            for (int power : entry.getValue()) result.addAll(byPower.getOrDefault(power, List.of()));
        }
        return result;
    }

    private static void addPower(Map<Integer, List<Integer>> powersByCount,
            Map<Integer, List<SourceCard>> byPower, int power) {
        int count = byPower.getOrDefault(power, List.of()).size();
        if (count > 0) addPowerAtCount(powersByCount, power, count);
    }

    private static void addPowerAtCount(Map<Integer, List<Integer>> powersByCount, int power, int count) {
        powersByCount.computeIfAbsent(count, ignored -> new ArrayList<>()).add(power);
    }

    private static boolean samePowerOrJoker(SourceCard left, SourceCard right) {
        return (isJoker(left.cardId()) && isJoker(right.cardId())) || left.power() == right.power();
    }

    private static boolean isJoker(int cardId) { return cardId == SMALL_JOKER || cardId == BIG_JOKER; }

    private static int jokerCount(List<SourceCard> cards) {
        return (int) cards.stream().filter(card -> isJoker(card.cardId())).count();
    }

    private static int countCard(List<SourceCard> cards, int cardId) {
        return (int) cards.stream().filter(card -> card.cardId() == cardId).count();
    }

    /** Direct CardLogic.lua getKingBombLine mapping (total joker count, then big-joker count). */
    private static int kingBombLine(int total, int big) {
        return switch (total) {
            case 4 -> (big == 0 || big == 4) ? 8 : 7;
            case 5 -> (big == 0 || big == 1 || big == 4 || big == 5) ? 10 : 8;
            case 6 -> big == 3 ? 9 : 12;
            case 7 -> 14;
            case 8 -> 16;
            case 9 -> 18;
            case 10 -> 20;
            case 11 -> 22;
            case 12 -> 24;
            default -> throw new IllegalArgumentException("unsupported recovered king bomb: " + total + "/" + big);
        };
    }
}
