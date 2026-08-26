package com.nanbeiyule.game.wechat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

final class WechatAuthState {
    private final String expectedState;
    private final long createdAtMillis;
    private final long ttlMillis;
    private boolean consumed;

    WechatAuthState(String expectedState, long createdAtMillis, long ttlMillis) {
        this.expectedState = expectedState == null ? "" : expectedState;
        this.createdAtMillis = createdAtMillis;
        this.ttlMillis = ttlMillis;
    }

    synchronized boolean consume(String actualState, long nowMillis) {
        if (consumed || actualState == null || actualState.isBlank()) {
            return false;
        }
        if (ttlMillis <= 0L
                || nowMillis < createdAtMillis
                || nowMillis - createdAtMillis > ttlMillis) {
            consumed = true;
            return false;
        }
        if (!constantTimeEquals(expectedState, actualState)) {
            return false;
        }
        consumed = true;
        return true;
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
