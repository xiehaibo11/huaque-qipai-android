package com.huaque.ui.wechat;

import com.nanbeiyule.game.auth.SecureStringStorage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class WechatSubscriptionOfferStore {
    private static final String PREFIX = "wechat_subscription_offered_";
    private final SecureStringStorage storage;

    public WechatSubscriptionOfferStore(SecureStringStorage storage) {
        this.storage = storage;
    }

    public boolean markIfFirst(String userIdentity) {
        if (userIdentity == null || userIdentity.isBlank()) {
            return false;
        }
        String key = PREFIX + sha256(userIdentity);
        if (storage.get(key) != null) {
            return false;
        }
        storage.set(key, "1");
        return true;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                hex.append(String.format("%02x", item & 0xff));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
