package com.nanbeiyule.game.wulong;

import com.nanbeiyule.game.gameplay.GameplayPhase;

/**
 * Single runtime authority gate for the native 30588 table.  It deliberately makes no rule
 * decision: cards, turn and phase still come from the authenticated server snapshot.
 */
public final class WuLongTableInputRouter {
    public enum Target { NONE, READY, START, HAND_CARD, PLAY, PASS, NEXT }
    public enum Intent { NONE, READY, START, TOGGLE_HAND, PLAY, PASS, NEXT_ROUND }
    public record Decision(Intent intent) {}

    private WuLongTableInputRouter() {}

    public static Decision route(
            GameplayPhase phase,
            int mySeat,
            Integer activeSeat,
            boolean owner,
            boolean allReady,
            boolean hasSelection,
            boolean hasLastPlay,
            Target target) {
        if (phase == GameplayPhase.WAITING) {
            if (target == Target.READY) return new Decision(Intent.READY);
            if (target == Target.START && owner && allReady) return new Decision(Intent.START);
            return new Decision(Intent.NONE);
        }
        if (phase == GameplayPhase.ROUND_RESULT) {
            return new Decision(target == Target.NEXT && owner ? Intent.NEXT_ROUND : Intent.NONE);
        }
        if (!WuLongTableInteractionDecisions.canSelectHand(phase, mySeat, activeSeat)) {
            return new Decision(Intent.NONE);
        }
        if (target == Target.HAND_CARD) return new Decision(Intent.TOGGLE_HAND);
        if (target == Target.PLAY && hasSelection) return new Decision(Intent.PLAY);
        if (target == Target.PASS && hasLastPlay) return new Decision(Intent.PASS);
        return new Decision(Intent.NONE);
    }
}
