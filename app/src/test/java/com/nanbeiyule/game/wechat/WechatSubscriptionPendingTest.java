package com.nanbeiyule.game.wechat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class WechatSubscriptionPendingTest {
    private static final long NOW = 1_000L;

    @Test
    public void capturesMatchingConfirmWithoutClearingPending() {
        WechatSubscriptionPending pending = pending();
        WechatSubscriptionPending.CaptureAttempt attempt =
                pending.capture(callback(0, "confirm", "openid"), NOW);

        assertEquals(WechatSubscriptionPending.CaptureResult.CAPTURED, attempt.result());
        assertEquals(WechatSubscriptionPending.State.CALLBACK_CAPTURED,
                attempt.pending().state());
        assertTrue(attempt.pending().callback().isConfirmed());
    }

    @Test
    public void acceptsCancelAndDeniedAsNonConfirmTerminalCallbacks() {
        WechatSubscriptionPending.CaptureAttempt cancelled =
                pending().capture(callback(-2, "cancel", ""), NOW);
        WechatSubscriptionPending.CaptureAttempt denied =
                pending().capture(callback(-4, "", ""), NOW);

        assertEquals(WechatSubscriptionPending.CaptureResult.CAPTURED, cancelled.result());
        assertEquals(WechatSubscriptionPending.CaptureResult.CAPTURED, denied.result());
        assertFalse(cancelled.pending().callback().isConfirmed());
        assertFalse(denied.pending().callback().isConfirmed());
    }

    @Test
    public void tamperExpiryAndDuplicateNeverReplaceTheValidPending() {
        WechatSubscriptionPending original = pending();
        WechatSubscriptionCallback tampered = new WechatSubscriptionCallback(
                0, "confirm", "template", 7, "Wrong", "openid", "intent-1");
        WechatSubscriptionPending.CaptureAttempt tamperAttempt =
                original.capture(tampered, NOW);
        WechatSubscriptionPending.CaptureAttempt expiredAttempt =
                original.capture(callback(0, "confirm", "openid"), 2_001L);
        WechatSubscriptionPending captured =
                original.capture(callback(0, "confirm", "openid"), NOW).pending();
        WechatSubscriptionPending.CaptureAttempt duplicate =
                captured.capture(callback(0, "confirm", "openid"), NOW);

        assertEquals(WechatSubscriptionPending.CaptureResult.TAMPERED,
                tamperAttempt.result());
        assertEquals(original, tamperAttempt.pending());
        assertEquals(WechatSubscriptionPending.CaptureResult.EXPIRED,
                expiredAttempt.result());
        assertEquals(original, expiredAttempt.pending());
        assertEquals(WechatSubscriptionPending.CaptureResult.DUPLICATE,
                duplicate.result());
        assertEquals(captured, duplicate.pending());
    }

    private static WechatSubscriptionPending pending() {
        return WechatSubscriptionPending.sdkPending(new WechatSubscriptionIntent(
                "intent-1", "template", 7, "Reserved123", 2_000L));
    }

    private static WechatSubscriptionCallback callback(
            int errCode, String action, String openId) {
        return new WechatSubscriptionCallback(
                errCode, action, "template", 7, "Reserved123", openId, "intent-1");
    }
}
