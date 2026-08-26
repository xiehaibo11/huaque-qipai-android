package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;

/** Keeps the original table-clock countdown visible when legacy snapshots omit seconds. */
final class TaizhouCenterClockTimer {
    static final int DEFAULT_TURN_SECONDS = 20;

    private TaizhouCenterClockTimer() {}

    static Integer secondsFor(GameplayTableState state) {
        if (state == null) {
            return null;
        }
        return secondsFor(state.phase(), state.activeSeat(), state.clockRemainingSeconds());
    }

    static Integer secondsFor(GameplayPhase phase, Integer activeSeat, Integer serverSeconds) {
        if (serverSeconds != null) {
            return serverSeconds;
        }
        if (activeSeat == null || phase == null) {
            return null;
        }
        return phase == GameplayPhase.DEALING || phase == GameplayPhase.PLAYING
                ? DEFAULT_TURN_SECONDS
                : null;
    }
}
