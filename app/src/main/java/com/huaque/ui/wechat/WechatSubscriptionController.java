package com.huaque.ui.wechat;

import com.nanbeiyule.game.wechat.WechatSubscriptionCallback;
import com.nanbeiyule.game.wechat.WechatSubscriptionLauncher;
import com.nanbeiyule.game.wechat.WechatSubscriptionPending;
import com.nanbeiyule.game.wechat.WechatSubscriptionStartResult;
import com.nanbeiyule.game.wechat.WechatSubscriptionStore;

public final class WechatSubscriptionController implements AutoCloseable {
    public enum Event {
        STARTED,
        NOT_CONFIGURED,
        NOT_INSTALLED,
        UNSUPPORTED,
        ALREADY_PENDING,
        START_FAILED,
        CONFIRMED,
        TERMINAL,
        NETWORK_PENDING,
        UNAUTHORIZED
    }

    public interface Listener {
        void onEvent(Event event);
    }

    private final WechatSubscriptionBackend backend;
    private final WechatSubscriptionLauncher launcher;
    private final WechatSubscriptionStore store;
    private final Listener listener;
    private boolean completing;

    public WechatSubscriptionController(
            WechatSubscriptionBackend backend,
            WechatSubscriptionLauncher launcher,
            WechatSubscriptionStore store,
            Listener listener) {
        this.backend = backend;
        this.launcher = launcher;
        this.store = store;
        this.listener = listener;
    }

    public void start(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            listener.onEvent(Event.UNAUTHORIZED);
            return;
        }
        backend.createIntent(accessToken, result -> {
            if (!result.isSuccess()) {
                listener.onEvent(result.failure()
                        == WechatSubscriptionBackend.Failure.UNAUTHORIZED
                                ? Event.UNAUTHORIZED
                                : Event.START_FAILED);
                return;
            }
            listener.onEvent(eventForStart(launcher.startSubscription(result.value())));
        });
    }

    public WechatSubscriptionPending.CaptureResult capture(
            WechatSubscriptionCallback callback, long nowMillis) {
        return store.capture(callback, nowMillis);
    }

    public void flush(String accessToken) {
        if (completing || accessToken == null || accessToken.isBlank()) {
            return;
        }
        WechatSubscriptionPending pending = store.load();
        if (pending == null
                || pending.state() != WechatSubscriptionPending.State.CALLBACK_CAPTURED) {
            return;
        }
        completing = true;
        backend.complete(accessToken, pending, result -> {
            completing = false;
            if (!result.isSuccess()) {
                listener.onEvent(eventForCompletionFailure(result.failure()));
                return;
            }
            store.clearAcknowledged(pending.intent().intentId());
            listener.onEvent(pending.callback().isConfirmed()
                    ? Event.CONFIRMED
                    : Event.TERMINAL);
        });
    }

    private static Event eventForStart(WechatSubscriptionStartResult result) {
        return switch (result) {
            case STARTED -> Event.STARTED;
            case NOT_CONFIGURED -> Event.NOT_CONFIGURED;
            case NOT_INSTALLED -> Event.NOT_INSTALLED;
            case UNSUPPORTED -> Event.UNSUPPORTED;
            case ALREADY_PENDING -> Event.ALREADY_PENDING;
            case REJECTED -> Event.START_FAILED;
        };
    }

    private static Event eventForCompletionFailure(
            WechatSubscriptionBackend.Failure failure) {
        return switch (failure) {
            case NETWORK -> Event.NETWORK_PENDING;
            case UNAUTHORIZED -> Event.UNAUTHORIZED;
            case NONE, REJECTED -> Event.START_FAILED;
        };
    }

    @Override
    public void close() {
        backend.close();
    }
}
