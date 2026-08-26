package com.nanbeiyule.game.wulong;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplaySnapshot;

/** Runtime projection for one recovered 30588 active-seat clock panel. */
final class WuLongClockProjection {
    record Bounds(float left, float top, float width, float height) {}

    record Panel(int localSeat, Bounds bounds, String remainingText, String statusLabel) {}

    private WuLongClockProjection() {}

    static Panel project(GameplaySnapshot snapshot) {
        if (snapshot == null || snapshot.gameId() != 30588L || snapshot.wuLongRound().isEmpty()) {
            return null;
        }
        com.nanbeiyule.game.gameplay.WuLongRound round = snapshot.wuLongRound().get();
        return project(snapshot.phase(), round.activeSeat(), snapshot.mySeat(),
                round.clockRemainingSeconds(), round.turnTimeoutPolicy(), round.turnTimeoutStatus());
    }

    static Panel project(
            GameplayPhase phase,
            Integer activeServerSeat,
            int mySeat,
            Integer remainingSeconds,
            String timeoutPolicy,
            String timeoutStatus) {
        if (phase != GameplayPhase.PLAYING || activeServerSeat == null) return null;
        int localSeat = WuLongTableLayout.localSeatFor(activeServerSeat, mySeat);
        WuLongTableLayout.ClockBounds bounds = WuLongTableLayout.clockBounds(activeServerSeat, mySeat);
        int seconds = Math.max(0, remainingSeconds == null ? 0 : remainingSeconds);
        String label = "";
        if ("NANBEI_SELF_BUILT_NO_AUTOPLAY".equals(timeoutPolicy)
                || "WULONG_TIMEOUT_NO_AUTOPLAY".equals(timeoutStatus)) {
            label = "自建仅记录，不自动出牌"
                    + ("WULONG_TIMEOUT_NO_AUTOPLAY".equals(timeoutStatus) ? "（已超时）" : "");
        }
        return new Panel(localSeat, new Bounds(bounds.left(), bounds.top(), bounds.width(), bounds.height()),
                String.format(java.util.Locale.ROOT, "%02d", seconds), label);
    }
}
