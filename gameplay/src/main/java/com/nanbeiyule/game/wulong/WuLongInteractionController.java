package com.nanbeiyule.game.wulong;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Local preselection only; the server revalidates ownership, type, turn and precedence. */
public final class WuLongInteractionController {
    private final Set<Integer> selectedIndexes = new LinkedHashSet<>();

    /**
     * Ports BaseWuLong HandCardArea: a tap selects one recovered power group, while a second tap
     * on any member clears that whole group.  Four or more jokers form one special group.
     */
    public List<Integer> tap(int sourceIndex, List<Integer> serverHand) {
        if (sourceIndex < 0 || sourceIndex >= serverHand.size()) return selectedCards(serverHand);
        if (selectedIndexes.contains(sourceIndex)) {
            clear();
            return List.of();
        }
        int tappedCard = serverHand.get(sourceIndex);
        boolean selectAllJokers = jokerCount(serverHand) >= 4 && isJoker(tappedCard);
        int tappedPower = WuLongCardRules.power(tappedCard);
        clear();
        for (int index = 0; index < serverHand.size(); index++) {
            int card = serverHand.get(index);
            if (selectAllJokers ? isJoker(card) : WuLongCardRules.power(card) == tappedPower) {
                selectedIndexes.add(index);
            }
        }
        return selectedCards(serverHand);
    }

    public List<Integer> selectedCards(List<Integer> hand) {
        return selectedIndexes.stream().filter(index -> index < hand.size()).map(hand::get).toList();
    }

    public boolean isSelected(int index) { return selectedIndexes.contains(index); }

    public void clear() { selectedIndexes.clear(); }

    private static int jokerCount(List<Integer> cards) {
        return (int) cards.stream().filter(WuLongInteractionController::isJoker).count();
    }

    private static boolean isJoker(int card) { return card == 53 || card == 54; }
}
