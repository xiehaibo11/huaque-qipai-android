package com.nanbeiyule.game.wulong;

import com.nanbeiyule.game.gameplay.GameplayPhase;

/** Pure, testable authority gates shared by table touch handling and action buttons. */
public final class WuLongTableInteractionDecisions {
    private WuLongTableInteractionDecisions() {}

    public static boolean canSelectHand(GameplayPhase phase, int mySeat, Integer activeSeat) {
        return phase == GameplayPhase.PLAYING && activeSeat != null && activeSeat == mySeat;
    }

    public static boolean canSubmitPlay(
            GameplayPhase phase, int mySeat, Integer activeSeat, boolean hasSelection) {
        return canSelectHand(phase, mySeat, activeSeat) && hasSelection;
    }

    public static boolean canPass(
            GameplayPhase phase, int mySeat, Integer activeSeat, boolean hasLastPlay) {
        return canSelectHand(phase, mySeat, activeSeat) && hasLastPlay;
    }
}
