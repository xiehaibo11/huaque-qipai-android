package com.nanbeiyule.game.wechat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public final class WechatLoginManagerSubscriptionTest {
    @Test
    public void persistsBeforeSendingAndCopiesEveryServerField() {
        List<String> events = new ArrayList<>();
        FakeStore store = new FakeStore(events);
        FakeSdk sdk = new FakeSdk(events);
        WechatLoginManager manager = new WechatLoginManager(true, true, sdk, store);

        WechatSubscriptionStartResult result = manager.startSubscription(intent());

        assertEquals(WechatSubscriptionStartResult.STARTED, result);
        assertEquals(List.of("stored", "sent"), events);
        assertEquals(intent(), sdk.subscription);
    }

    @Test
    public void rejectsUnsupportedOrConcurrentRequestsWithoutOpeningWechat() {
        FakeStore store = new FakeStore(new ArrayList<>());
        FakeSdk sdk = new FakeSdk(new ArrayList<>());
        sdk.supportApi = Build.SUBSCRIBE_MESSAGE_SUPPORTED_SDK_INT - 1;
        WechatLoginManager manager = new WechatLoginManager(true, true, sdk, store);

        assertEquals(WechatSubscriptionStartResult.UNSUPPORTED,
                manager.startSubscription(intent()));
        assertFalse(sdk.sent);

        sdk.supportApi = Build.SUBSCRIBE_MESSAGE_SUPPORTED_SDK_INT;
        store.acceptNew = false;
        assertEquals(WechatSubscriptionStartResult.ALREADY_PENDING,
                manager.startSubscription(intent()));
        assertFalse(sdk.sent);
    }

    @Test
    public void failedSdkSendClearsOnlyTheSdkPendingRequest() {
        FakeStore store = new FakeStore(new ArrayList<>());
        FakeSdk sdk = new FakeSdk(new ArrayList<>());
        sdk.sendResult = false;
        WechatLoginManager manager = new WechatLoginManager(true, true, sdk, store);

        assertEquals(WechatSubscriptionStartResult.REJECTED,
                manager.startSubscription(intent()));
        assertEquals("intent-1", store.clearedSdkIntentId);
    }

    private static WechatSubscriptionIntent intent() {
        return new WechatSubscriptionIntent(
                "intent-1", "template", 7, "Reserved123", Long.MAX_VALUE);
    }

    private static final class FakeSdk implements WechatLoginManager.SdkGateway {
        private final List<String> events;
        int supportApi = Build.SUBSCRIBE_MESSAGE_SUPPORTED_SDK_INT;
        boolean sendResult = true;
        boolean sent;
        BaseReq request;
        WechatSubscriptionIntent subscription;

        FakeSdk(List<String> events) { this.events = events; }
        @Override public boolean isInstalled() { return true; }
        @Override public int supportApi() { return supportApi; }
        @Override public boolean send(BaseReq request) {
            events.add("sent");
            sent = true;
            this.request = request;
            return sendResult;
        }
        @Override public boolean sendSubscription(WechatSubscriptionIntent intent) {
            events.add("sent");
            sent = true;
            subscription = intent;
            return sendResult;
        }
        @Override public void detach() {}
    }

    private static final class FakeStore implements WechatSubscriptionStore {
        private final List<String> events;
        boolean acceptNew = true;
        String clearedSdkIntentId;

        FakeStore(List<String> events) { this.events = events; }
        @Override public boolean saveNew(WechatSubscriptionIntent intent, long now) {
            if (!acceptNew) return false;
            events.add("stored");
            return true;
        }
        @Override public WechatSubscriptionPending.CaptureResult capture(
                WechatSubscriptionCallback callback, long now) {
            return WechatSubscriptionPending.CaptureResult.MISSING;
        }
        @Override public WechatSubscriptionPending load() { return null; }
        @Override public void clearSdkFailure(String intentId) {
            clearedSdkIntentId = intentId;
        }
        @Override public void clearAcknowledged(String intentId) {}
    }
}
