package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayActionTip;
import com.nanbeiyule.game.gameplay.GameplayTableState;

final class TaizhouMahjongActionEffectTracker {
    static final long ACTION_EFFECT_MILLIS = 900L;

    record Running(int serverSeat, String animationName, long startedAtMillis) {}

    private boolean initialized;
    private GameplayActionTip seenTip;
    private Running running;

    void update(GameplayTableState state, long nowElapsed) {
        if (state == null) {
            initialized = false;
            seenTip = null;
            running = null;
            return;
        }
        GameplayActionTip tip = state.actionTip().orElse(null);
        if (!initialized) {
            initialized = true;
            seenTip = tip;
            return;
        }
        if (tip == null || !tip.isNewerThan(seenTip)) {
            return;
        }
        String animationName = TaizhouMahjongActionEffect.animationName(tip.kind());
        running = animationName == null
                ? null
                : new Running(tip.seatNumber(), animationName, nowElapsed);
        seenTip = tip;
    }

    Running running(long nowElapsed) {
        if (running == null) {
            return null;
        }
        if (nowElapsed - running.startedAtMillis() >= ACTION_EFFECT_MILLIS) {
            running = null;
        }
        return running;
    }

    long nextRepaintDelayMillis(long nowElapsed) {
        return running(nowElapsed) == null ? 0L : 16L;
    }
}
