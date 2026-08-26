package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplaySnapshot;

/** Lifecycle-owned authoritative 30588 snapshot synchronizer. */
final class WuLongSnapshotSync {
    static final long REFRESH_INTERVAL_MILLIS = 1_000L;
    interface Scheduler {
        Cancellable schedule(long delayMillis, Runnable task);
    }

    interface Cancellable {
        void cancel();
    }

    interface Listener {
        void onSnapshot(GameplaySnapshot snapshot);

        void onLoginRequired();

        void onError(String message);
    }

    interface Requester {
        void load(GameplayTransport.Callback<GameplaySnapshot> callback);

        void cancelPending();
    }

    private final Requester requester;
    private final Scheduler scheduler;
    private final Listener listener;
    private Cancellable scheduledRefresh;
    private long generation;
    private long revision = Long.MIN_VALUE;
    private boolean active;
    private boolean closed = true;
    private boolean requestInFlight;

    WuLongSnapshotSync(Requester requester, Scheduler scheduler, Listener listener) {
        this.requester = requester;
        this.scheduler = scheduler;
        this.listener = listener;
    }

    void start(GameplaySnapshot initialSnapshot) {
        if (initialSnapshot == null || initialSnapshot.gameId() != 30588L) {
            listener.onError("服务器返回的不是乌龙 30588 会话");
            return;
        }
        cancelScheduledRefresh();
        generation++;
        closed = false;
        active = true;
        requestInFlight = false;
        revision = initialSnapshot.revision();
        listener.onSnapshot(initialSnapshot);
        scheduleRefresh();
    }

    void resume() {
        if (closed || active) return;
        active = true;
        requestSnapshot();
    }

    void requestNow() {
        if (!active || closed) return;
        cancelScheduledRefresh();
        requestSnapshot();
    }

    void pause() {
        if (!active) return;
        active = false;
        generation++;
        requestInFlight = false;
        cancelScheduledRefresh();
        requester.cancelPending();
    }

    void close() {
        pause();
        closed = true;
    }

    private void requestSnapshot() {
        if (!active || requestInFlight) return;
        requestInFlight = true;
        long requestGeneration = generation;
        requester.load(new GameplayTransport.Callback<>() {
            @Override public void onSuccess(GameplaySnapshot snapshot) {
                if (!isCurrent(requestGeneration)) return;
                requestInFlight = false;
                if (snapshot == null || snapshot.gameId() != 30588L) {
                    listener.onError("服务器返回的不是乌龙 30588 会话");
                } else if (snapshot.revision() > revision) {
                    revision = snapshot.revision();
                    listener.onSnapshot(snapshot);
                }
                scheduleRefresh();
            }

            @Override public void onUnauthorized() {
                if (!isCurrent(requestGeneration)) return;
                requestInFlight = false;
                listener.onLoginRequired();
            }

            @Override public void onError(String message) {
                if (!isCurrent(requestGeneration)) return;
                requestInFlight = false;
                listener.onError(message);
                scheduleRefresh();
            }
        });
    }

    private boolean isCurrent(long requestGeneration) {
        return active && !closed && generation == requestGeneration;
    }

    private void scheduleRefresh() {
        if (!active || closed) return;
        cancelScheduledRefresh();
        scheduledRefresh = scheduler.schedule(REFRESH_INTERVAL_MILLIS, this::requestSnapshot);
    }

    private void cancelScheduledRefresh() {
        if (scheduledRefresh != null) {
            scheduledRefresh.cancel();
            scheduledRefresh = null;
        }
    }
}
