package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;

/** Caches the latest real-name status per user id for the current device. */
final class RealNameStatusStore {
    private static final String PREFERENCES = "nanbei_real_name_status";

    private final SharedPreferences preferences;

    RealNameStatusStore(Context context) {
        preferences =
                context.getSharedPreferences(
                        PREFERENCES, Context.MODE_PRIVATE);
    }

    RealNameStatus.Status statusFor(String userId) {
        if (userId == null || userId.isBlank()) {
            return RealNameStatus.Status.UNVERIFIED;
        }
        String stored = preferences.getString(key(userId), "");
        if (RealNameStatus.Status.VERIFIED.name().equals(stored)) {
            return RealNameStatus.Status.VERIFIED;
        }
        if (RealNameStatus.Status.UNVERIFIED.name().equals(stored)) {
            return RealNameStatus.Status.UNVERIFIED;
        }
        return null;
    }

    void save(String userId, RealNameStatus.Status status) {
        if (userId == null || userId.isBlank() || status == null) {
            return;
        }
        preferences
                .edit()
                .putString(key(userId), status.name())
                .apply();
    }

    void clear() {
        preferences.edit().clear().apply();
    }

    private static String key(String userId) {
        return "status_" + userId;
    }
}
