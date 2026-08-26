package com.nanbeiyule.game.wechat;

public interface WechatSubscriptionStore {
    boolean saveNew(WechatSubscriptionIntent intent, long nowMillis);

    WechatSubscriptionPending.CaptureResult capture(
            WechatSubscriptionCallback callback, long nowMillis);

    WechatSubscriptionPending load();

    void clearSdkFailure(String intentId);

    void clearAcknowledged(String intentId);
}
