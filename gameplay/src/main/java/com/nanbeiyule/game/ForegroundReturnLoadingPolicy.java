package com.nanbeiyule.game;

/** Decides when a foreground return should reuse the original Mahjong loading overlay. */
final class ForegroundReturnLoadingPolicy {
    static final long LONG_BACKGROUND_THRESHOLD_MILLIS = 30_000L;

    private long stoppedAtMillis;

    void onStoppedAt(long elapsedRealtimeMillis) {
        stoppedAtMillis = Math.max(0L, elapsedRealtimeMillis);
    }

    boolean shouldShowLoadingOnForeground(
            long elapsedRealtimeMillis,
            boolean hasRecoverableSession,
            boolean showingGameHome,
            boolean loginRequestInFlight) {
        if (stoppedAtMillis <= 0L
                || !hasRecoverableSession
                || !showingGameHome
                || loginRequestInFlight) {
            return false;
        }
        long backgroundMillis = elapsedRealtimeMillis - stoppedAtMillis;
        stoppedAtMillis = 0L;
        return backgroundMillis >= LONG_BACKGROUND_THRESHOLD_MILLIS;
    }
}
