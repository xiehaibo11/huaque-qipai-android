package com.nanbeiyule.game.wulong;

import java.util.ArrayList;
import java.util.List;

/**
 * Public last-play geometry ported from BaseCardGame CardArea and WuLong OutCardConf.
 *
 * <p>The Lua CardArea is a child of an out-card position node and the whole area has scale .65.
 * Consequently both its 60×75 logical spacing and the 232×294 face are scaled here.  Each
 * placement is a Canvas left/top rectangle, derived from the original ImageView center only once.
 */
final class WuLongOutCardProjection {
    private static final int LINE_CARD_COUNT = 8;

    interface Receiver {
        void draw(int cardId, WuLongTableLayout.CardBounds bounds);
    }

    record CardPlacement(int cardId, int lineIndex, WuLongTableLayout.CardBounds bounds) {
        float centerX() { return bounds.left() + bounds.width() / 2f; }
        float centerY() { return WuLongTableLayout.DESIGN_HEIGHT - bounds.top() - bounds.height() / 2f; }
    }

    private WuLongOutCardProjection() {}

    static List<CardPlacement> project(int serverSeat, int mySeat, List<Integer> cards) {
        if (cards == null || cards.isEmpty()) return List.of();
        int localSeat = WuLongTableLayout.localSeatFor(serverSeat, mySeat);
        int lineCount = (cards.size() + LINE_CARD_COUNT - 1) / LINE_CARD_COUNT;
        float[] parent = WuLongTableLayout.cocosOutAnchor(serverSeat, mySeat);
        List<CardPlacement> placements = new ArrayList<>(cards.size());
        for (int index = 0; index < cards.size(); index++) {
            int lineIndex = index / LINE_CARD_COUNT;
            int indexInLine = index % LINE_CARD_COUNT;
            int cardsThisLine = lineIndex == lineCount - 1
                    ? cards.size() - lineIndex * LINE_CARD_COUNT : LINE_CARD_COUNT;
            float logicalX = -indexInLine * WuLongTableLayout.OUT_CARD_SPACING_X
                    + addDistanceX(localSeat, cardsThisLine);
            float logicalY = lineIndex * WuLongTableLayout.OUT_CARD_SPACING_Y
                    - (lineCount * WuLongTableLayout.OUT_CARD_SPACING_Y / 2f
                    - WuLongTableLayout.OUT_CARD_SPACING_Y / 2f);
            float centerX = parent[0] + logicalX * WuLongTableLayout.OUT_CARD_SCALE;
            float centerY = parent[1] + logicalY * WuLongTableLayout.OUT_CARD_SCALE;
            float top = WuLongTableLayout.cocosToAndroidTop(centerY,
                    WuLongTableLayout.OUT_CARD_HEIGHT, .5f);
            placements.add(new CardPlacement(cards.get(index), lineIndex,
                    new WuLongTableLayout.CardBounds(centerX - WuLongTableLayout.OUT_CARD_WIDTH / 2f,
                            top, WuLongTableLayout.OUT_CARD_WIDTH, WuLongTableLayout.OUT_CARD_HEIGHT)));
        }
        return List.copyOf(placements);
    }

    /** Runtime rendering seam shared by Canvas production code and its behavior-level contract. */
    static void render(int serverSeat, int mySeat, List<Integer> cards, Receiver receiver) {
        for (CardPlacement placement : project(serverSeat, mySeat, cards)) {
            receiver.draw(placement.cardId(), placement.bounds());
        }
    }

    private static float addDistanceX(int localSeat, int cardsThisLine) {
        // WuLong CardLayerConfig.OutCardConf: left=Right, bottom/top=Center, right=Left.
        return switch (localSeat) {
            case 1 -> (cardsThisLine - 1) * WuLongTableLayout.OUT_CARD_SPACING_X;
            case 2, 4 -> (cardsThisLine - 1) * WuLongTableLayout.OUT_CARD_SPACING_X / 2f;
            case 3 -> 0f;
            default -> throw new IllegalArgumentException("Unexpected local seat " + localSeat);
        };
    }
}
