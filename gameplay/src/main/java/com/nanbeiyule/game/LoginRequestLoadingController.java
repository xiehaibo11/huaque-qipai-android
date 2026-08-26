package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;

/**
 * Owns the visible login-loading request without allowing stale callbacks to close a newer one.
 */
final class LoginRequestLoadingController {
    static final long NO_TOKEN = 0L;
    static final long REQUEST_TIMEOUT_MILLIS = 15_000L;

    interface Display {
        void setLoadingVisible(boolean visible);
    }

    interface Cancellable {
        void cancel();
    }

    interface Scheduler {
        Cancellable schedule(Runnable action, long delayMillis);
    }

    private final Scheduler scheduler;

    private Display display;
    private Cancellable watchdog;
    private long generation;
    private long activeToken;
    private boolean destroyed;

    LoginRequestLoadingController(Scheduler scheduler) {
        if (scheduler == null) {
            throw new IllegalArgumentException("Scheduler must not be null");
        }
        this.scheduler = scheduler;
    }

    static LoginRequestLoadingController createForMainThread() {
        Handler handler = new Handler(Looper.getMainLooper());
        return new LoginRequestLoadingController(
                (action, delayMillis) -> {
                    handler.postDelayed(action, delayMillis);
                    return () -> handler.removeCallbacks(action);
                });
    }

    synchronized void bind(Display nextDisplay) {
        if (display == nextDisplay) {
            if (display != null) {
                display.setLoadingVisible(activeToken != NO_TOKEN && !destroyed);
            }
            return;
        }
        if (display != null) {
            display.setLoadingVisible(false);
        }
        display = nextDisplay;
        if (display != null) {
            display.setLoadingVisible(activeToken != NO_TOKEN && !destroyed);
        }
    }

    synchronized void unbind(Display expectedDisplay) {
        if (display != expectedDisplay) {
            return;
        }
        display.setLoadingVisible(false);
        display = null;
    }

    synchronized long begin() {
        if (destroyed) {
            return NO_TOKEN;
        }
        cancelWatchdogLocked();
        activeToken = ++generation;
        scheduleWatchdogLocked(activeToken);
        updateDisplayLocked(true);
        return activeToken;
    }

    synchronized long refresh(long token) {
        if (destroyed) {
            return NO_TOKEN;
        }
        if (isCurrentLocked(token)) {
            cancelWatchdogLocked();
            scheduleWatchdogLocked(token);
            updateDisplayLocked(true);
            return token;
        }
        return begin();
    }

    synchronized boolean finish(long token) {
        if (!isCurrentLocked(token)) {
            return false;
        }
        clearActiveLocked();
        return true;
    }

    synchronized boolean isCurrent(long token) {
        return isCurrentLocked(token);
    }

    synchronized void finishAll() {
        if (destroyed) {
            return;
        }
        generation++;
        clearActiveLocked();
    }

    synchronized void destroy() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        generation++;
        clearActiveLocked();
        display = null;
    }

    private void scheduleWatchdogLocked(long token) {
        watchdog =
                scheduler.schedule(
                        () -> expire(token),
                        REQUEST_TIMEOUT_MILLIS);
    }

    private synchronized void expire(long token) {
        if (isCurrentLocked(token)) {
            clearActiveLocked();
        }
    }

    private boolean isCurrentLocked(long token) {
        return !destroyed
                && token != NO_TOKEN
                && token == activeToken;
    }

    private void clearActiveLocked() {
        cancelWatchdogLocked();
        activeToken = NO_TOKEN;
        updateDisplayLocked(false);
    }

    private void cancelWatchdogLocked() {
        if (watchdog != null) {
            watchdog.cancel();
            watchdog = null;
        }
    }

    private void updateDisplayLocked(boolean visible) {
        if (display != null) {
            display.setLoadingVisible(visible);
        }
    }
}
