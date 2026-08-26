package com.huaque.ui.wechat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.wechat.WechatSubscriptionCallback;
import com.nanbeiyule.game.wechat.WechatSubscriptionIntent;
import com.nanbeiyule.game.wechat.WechatSubscriptionLauncher;
import com.nanbeiyule.game.wechat.WechatSubscriptionPending;
import com.nanbeiyule.game.wechat.WechatSubscriptionStartResult;
import com.nanbeiyule.game.wechat.WechatSubscriptionStore;
import org.junit.Test;

public final class WechatSubscriptionControllerTest {
    @Test
    public void loginSuccessNeverStartsSdkUntilExplicitStart() {
        FakeBackend backend = new FakeBackend();
        FakeLauncher launcher = new FakeLauncher();
        WechatSubscriptionController controller = new WechatSubscriptionController(
                backend, launcher, new FakeStore(), ignored -> {});

        WechatSubscriptionOfferPolicy policy = new WechatSubscriptionOfferPolicy();
        policy.onWechatAuthenticated();

        assertFalse(launcher.started);
        assertTrue(policy.onHomeLoaded());
        assertFalse(policy.onHomeLoaded());
        assertFalse(launcher.started);

        controller.start("jwt-token");
        backend.createCallback.onComplete(WechatSubscriptionBackend.Result.success(intent()));
        assertTrue(launcher.started);
    }

    @Test
    public void networkFailureKeepsCapturedCallbackForRetryAndAckClearsIt() {
        FakeBackend backend = new FakeBackend();
        FakeStore store = new FakeStore();
        store.pending = captured();
        WechatSubscriptionController controller = new WechatSubscriptionController(
                backend, new FakeLauncher(), store, ignored -> {});

        controller.flush("jwt-token");
        backend.completeCallback.onComplete(
                WechatSubscriptionBackend.Result.failure(
                        WechatSubscriptionBackend.Failure.NETWORK));
        assertEquals("intent-1", store.pending.intent().intentId());

        controller.flush("jwt-token");
        backend.completeCallback.onComplete(
                WechatSubscriptionBackend.Result.success("CONFIRMED"));
        assertNull(store.pending);
    }

    @Test
    public void coldCallbackIsCapturedWithoutStartingWechatAgain() {
        FakeStore store = new FakeStore();
        store.pending = WechatSubscriptionPending.sdkPending(intent());
        FakeLauncher launcher = new FakeLauncher();
        WechatSubscriptionController controller = new WechatSubscriptionController(
                new FakeBackend(), launcher, store, ignored -> {});

        assertEquals(WechatSubscriptionPending.CaptureResult.CAPTURED,
                controller.capture(callback(), 1L));
        assertFalse(launcher.started);
        assertEquals(WechatSubscriptionPending.State.CALLBACK_CAPTURED,
                store.pending.state());
    }

    private static WechatSubscriptionIntent intent() {
        return new WechatSubscriptionIntent(
                "intent-1", "template", 7, "Reserved123", Long.MAX_VALUE);
    }

    private static WechatSubscriptionCallback callback() {
        return new WechatSubscriptionCallback(
                0, "confirm", "template", 7, "Reserved123", "openid", "intent-1");
    }

    private static WechatSubscriptionPending captured() {
        return WechatSubscriptionPending.sdkPending(intent()).capture(callback(), 1L).pending();
    }

    private static final class FakeLauncher implements WechatSubscriptionLauncher {
        boolean started;
        @Override public WechatSubscriptionStartResult startSubscription(
                WechatSubscriptionIntent intent) {
            started = true;
            return WechatSubscriptionStartResult.STARTED;
        }
    }

    private static final class FakeStore implements WechatSubscriptionStore {
        WechatSubscriptionPending pending;
        @Override public boolean saveNew(WechatSubscriptionIntent intent, long now) {
            if (pending != null) return false;
            pending = WechatSubscriptionPending.sdkPending(intent);
            return true;
        }
        @Override public WechatSubscriptionPending.CaptureResult capture(
                WechatSubscriptionCallback callback, long now) {
            if (pending == null) return WechatSubscriptionPending.CaptureResult.MISSING;
            WechatSubscriptionPending.CaptureAttempt attempt = pending.capture(callback, now);
            pending = attempt.pending();
            return attempt.result();
        }
        @Override public WechatSubscriptionPending load() { return pending; }
        @Override public void clearSdkFailure(String intentId) {
            if (pending != null && pending.intent().intentId().equals(intentId)
                    && pending.state() == WechatSubscriptionPending.State.SDK_PENDING) {
                pending = null;
            }
        }
        @Override public void clearAcknowledged(String intentId) {
            if (pending != null && pending.intent().intentId().equals(intentId)) pending = null;
        }
    }

    private static final class FakeBackend implements WechatSubscriptionBackend {
        Callback<WechatSubscriptionIntent> createCallback;
        Callback<String> completeCallback;
        @Override public void createIntent(
                String accessToken, Callback<WechatSubscriptionIntent> callback) {
            createCallback = callback;
        }
        @Override public void complete(String accessToken,
                WechatSubscriptionPending pending, Callback<String> callback) {
            completeCallback = callback;
        }
        @Override public void close() {}
    }
}
