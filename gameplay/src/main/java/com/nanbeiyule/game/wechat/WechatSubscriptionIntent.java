package com.nanbeiyule.game.wechat;

import java.nio.charset.StandardCharsets;

public record WechatSubscriptionIntent(
        String intentId,
        String templateId,
        int scene,
        String reserved,
        long expiresAtMillis) {
    public WechatSubscriptionIntent {
        requireText(intentId, "intentId");
        requireText(templateId, "templateId");
        if (scene < 0 || scene > 10_000) {
            throw new IllegalArgumentException("scene is outside 0..10000");
        }
        requireText(reserved, "reserved");
        if (!reserved.matches("[A-Za-z0-9]+")
                || reserved.getBytes(StandardCharsets.UTF_8).length > 128) {
            throw new IllegalArgumentException("reserved is invalid");
        }
        if (expiresAtMillis <= 0L) {
            throw new IllegalArgumentException("expiresAt is invalid");
        }
    }

    public boolean isExpired(long nowMillis) {
        return nowMillis >= expiresAtMillis;
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
