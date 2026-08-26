package com.nanbeiyule.game.wechat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public record WechatSubscriptionPending(
        WechatSubscriptionIntent intent,
        State state,
        WechatSubscriptionCallback callback) {
    public enum State {
        SDK_PENDING,
        CALLBACK_CAPTURED
    }

    public enum CaptureResult {
        CAPTURED,
        DUPLICATE,
        TAMPERED,
        EXPIRED,
        MISSING
    }

    public record CaptureAttempt(
            CaptureResult result,
            WechatSubscriptionPending pending) {
    }

    public WechatSubscriptionPending {
        if (intent == null || state == null) {
            throw new IllegalArgumentException("pending subscription is incomplete");
        }
        if ((state == State.SDK_PENDING) != (callback == null)) {
            throw new IllegalArgumentException("pending subscription state is invalid");
        }
    }

    public static WechatSubscriptionPending sdkPending(WechatSubscriptionIntent intent) {
        return new WechatSubscriptionPending(intent, State.SDK_PENDING, null);
    }

    public CaptureAttempt capture(WechatSubscriptionCallback response, long nowMillis) {
        if (response == null) {
            return new CaptureAttempt(CaptureResult.TAMPERED, this);
        }
        if (state == State.CALLBACK_CAPTURED) {
            return new CaptureAttempt(
                    callback.equals(response)
                            ? CaptureResult.DUPLICATE
                            : CaptureResult.TAMPERED,
                    this);
        }
        if (intent.isExpired(nowMillis)) {
            return new CaptureAttempt(CaptureResult.EXPIRED, this);
        }
        if (!intent.intentId().equals(response.transaction())
                || !intent.templateId().equals(response.templateId())
                || intent.scene() != response.scene()
                || !constantTimeEquals(intent.reserved(), response.reserved())) {
            return new CaptureAttempt(CaptureResult.TAMPERED, this);
        }
        return new CaptureAttempt(
                CaptureResult.CAPTURED,
                new WechatSubscriptionPending(intent, State.CALLBACK_CAPTURED, response));
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
