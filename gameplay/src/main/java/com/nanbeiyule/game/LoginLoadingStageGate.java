package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

/**
 * Delays a transition until one complete recovered login-loading animation cycle has elapsed.
 */
final class LoginLoadingStageGate {
    static final long NO_TOKEN = 0L;
    static final long MINIMUM_STAGE_MILLIS = 3_075L;

    interface Cancellable {
        void cancel();
    }

    interface Scheduler {
        long nowMillis();

        Cancellable schedule(
                Runnable action,
                long delayMillis);
    }

    private final Scheduler scheduler;

    private Cancellable pending;
    private long generation;
    private long activeToken;
    private long startedAtMillis;
    private boolean destroyed;

    LoginLoadingStageGate(Scheduler scheduler) {
        if (scheduler == null) {
            throw new IllegalArgumentException(
                    "Scheduler must not be null");
        }
        this.scheduler = scheduler;
    }

    static LoginLoadingStageGate createForMainThread() {
        Handler handler = new Handler(Looper.getMainLooper());
        return new LoginLoadingStageGate(
                new Scheduler() {
                    @Override
                    public long nowMillis() {
                        return SystemClock.elapsedRealtime();
                    }

                    @Override
                    public Cancellable schedule(
                            Runnable action,
                            long delayMillis) {
                        handler.postDelayed(action, delayMillis);
                        return () -> handler.removeCallbacks(action);
                    }
                });
    }

    synchronized long begin() {
        if (destroyed) {
            return NO_TOKEN;
        }
        cancelPendingLocked();
        activeToken = ++generation;
        startedAtMillis = scheduler.nowMillis();
        return activeToken;
    }

    boolean runAfterMinimum(
            long token,
            Runnable action) {
        if (action == null) {
            throw new IllegalArgumentException(
                    "Action must not be null");
        }
        boolean runImmediately = false;
        synchronized (this) {
            if (!isCurrentLocked(token)) {
                return false;
            }
            cancelPendingLocked();
            long elapsedMillis =
                    Math.max(
                            0L,
                            scheduler.nowMillis()
                                    - startedAtMillis);
            long remainingMillis =
                    Math.max(
                            0L,
                            MINIMUM_STAGE_MILLIS
                                    - elapsedMillis);
            if (remainingMillis == 0L) {
                activeToken = NO_TOKEN;
                runImmediately = true;
            } else {
                pending =
                        scheduler.schedule(
                                () -> complete(token, action),
                                remainingMillis);
            }
        }
        if (runImmediately) {
            action.run();
        }
        return true;
    }

    synchronized boolean cancel(long token) {
        if (!isCurrentLocked(token)) {
            return false;
        }
        cancelPendingLocked();
        activeToken = NO_TOKEN;
        return true;
    }

    synchronized void destroy() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        generation++;
        cancelPendingLocked();
        activeToken = NO_TOKEN;
    }

    private void complete(
            long token,
            Runnable action) {
        synchronized (this) {
            if (!isCurrentLocked(token)) {
                return;
            }
            pending = null;
            activeToken = NO_TOKEN;
        }
        action.run();
    }

    private boolean isCurrentLocked(long token) {
        return !destroyed
                && token != NO_TOKEN
                && token == activeToken;
    }

    private void cancelPendingLocked() {
        if (pending != null) {
            pending.cancel();
            pending = null;
        }
    }
}
