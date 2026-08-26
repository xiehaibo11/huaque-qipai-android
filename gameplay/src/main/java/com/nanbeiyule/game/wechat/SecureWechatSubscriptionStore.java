package com.nanbeiyule.game.wechat;

import android.content.Context;
import com.nanbeiyule.game.auth.SecureStringStorage;
import org.json.JSONException;
import org.json.JSONObject;

public final class SecureWechatSubscriptionStore implements WechatSubscriptionStore {
    private static final String KEY = "wechat_subscription_pending_v1";
    private final SecureStringStorage storage;

    public SecureWechatSubscriptionStore(Context context) {
        this(new SecureStringStorage(context.getApplicationContext()));
    }

    SecureWechatSubscriptionStore(SecureStringStorage storage) {
        this.storage = storage;
    }

    @Override
    public synchronized boolean saveNew(
            WechatSubscriptionIntent intent, long nowMillis) {
        if (intent == null || intent.isExpired(nowMillis)) {
            return false;
        }
        WechatSubscriptionPending current = load();
        if (current != null && !current.intent().isExpired(nowMillis)) {
            return false;
        }
        write(WechatSubscriptionPending.sdkPending(intent));
        return true;
    }

    @Override
    public synchronized WechatSubscriptionPending.CaptureResult capture(
            WechatSubscriptionCallback callback, long nowMillis) {
        WechatSubscriptionPending current = load();
        if (current == null) {
            return WechatSubscriptionPending.CaptureResult.MISSING;
        }
        WechatSubscriptionPending.CaptureAttempt attempt =
                current.capture(callback, nowMillis);
        if (attempt.result() == WechatSubscriptionPending.CaptureResult.CAPTURED) {
            write(attempt.pending());
        }
        return attempt.result();
    }

    @Override
    public synchronized WechatSubscriptionPending load() {
        String encoded = storage.get(KEY);
        if (encoded == null) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(encoded);
            WechatSubscriptionIntent intent = new WechatSubscriptionIntent(
                    json.getString("intentId"),
                    json.getString("templateId"),
                    json.getInt("scene"),
                    json.getString("reserved"),
                    json.getLong("expiresAtMillis"));
            WechatSubscriptionPending.State state =
                    WechatSubscriptionPending.State.valueOf(json.getString("state"));
            WechatSubscriptionCallback callback = null;
            if (state == WechatSubscriptionPending.State.CALLBACK_CAPTURED) {
                callback = new WechatSubscriptionCallback(
                        json.getInt("errCode"),
                        json.optString("action"),
                        json.getString("templateId"),
                        json.getInt("scene"),
                        json.getString("reserved"),
                        json.optString("openId"),
                        json.optString("transaction"));
            }
            return new WechatSubscriptionPending(intent, state, callback);
        } catch (Exception error) {
            storage.remove(KEY);
            return null;
        }
    }

    @Override
    public synchronized void clearSdkFailure(String intentId) {
        WechatSubscriptionPending current = load();
        if (current != null
                && current.state() == WechatSubscriptionPending.State.SDK_PENDING
                && current.intent().intentId().equals(intentId)) {
            storage.remove(KEY);
        }
    }

    @Override
    public synchronized void clearAcknowledged(String intentId) {
        WechatSubscriptionPending current = load();
        if (current != null
                && current.state() == WechatSubscriptionPending.State.CALLBACK_CAPTURED
                && current.intent().intentId().equals(intentId)) {
            storage.remove(KEY);
        }
    }

    private void write(WechatSubscriptionPending pending) {
        try {
            JSONObject json = new JSONObject()
                    .put("intentId", pending.intent().intentId())
                    .put("templateId", pending.intent().templateId())
                    .put("scene", pending.intent().scene())
                    .put("reserved", pending.intent().reserved())
                    .put("expiresAtMillis", pending.intent().expiresAtMillis())
                    .put("state", pending.state().name());
            if (pending.callback() != null) {
                json.put("errCode", pending.callback().errCode())
                        .put("action", pending.callback().action())
                        .put("openId", pending.callback().openId())
                        .put("transaction", pending.callback().transaction());
            }
            storage.set(KEY, json.toString());
        } catch (JSONException error) {
            throw new IllegalStateException("cannot encode pending subscription", error);
        }
    }
}
