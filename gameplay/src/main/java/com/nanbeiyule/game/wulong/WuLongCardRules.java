package com.nanbeiyule.game.wulong;

import java.util.List;

/** Pure table-selection helper matching recovered client classification; it never authorizes play. */
public final class WuLongCardRules {
    private WuLongCardRules() {}

    public static WuLongCardType classify(List<Integer> cards) {
        if (cards == null || cards.isEmpty() || cards.stream().anyMatch(card -> !valid(card))) {
            return WuLongCardType.INVALID;
        }
        int count = cards.size();
        int power = power(cards.get(0));
        boolean same = cards.stream().allMatch(card -> power(card) == power);
        if (count == 1) return WuLongCardType.SINGLE;
        if (count == 2 && same) return WuLongCardType.PAIR;
        if (count == 3 && same) return WuLongCardType.TRIPLE;
        if (count >= 4 && cards.stream().allMatch(card -> card == 53 || card == 54)) {
            return WuLongCardType.KING_BOMB;
        }
        return count >= 4 && same ? WuLongCardType.ORDINARY_BOMB : WuLongCardType.INVALID;
    }

    public static int power(int cardId) {
        if (!valid(cardId)) return 0;
        if (cardId == 53) return 16;
        if (cardId == 54) return 17;
        int value = cardId % 13;
        if (value == 0) value = 13;
        return value == 1 ? 14 : value == 2 ? 15 : value;
    }

    private static boolean valid(Integer card) {
        return card != null && card >= 1 && card <= 54;
    }
}
