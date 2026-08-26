package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Persists the friend drawer daily auto-expand marker and the per-friend
 * invite cooldown timestamps for the current device.
 */
final class FriendDrawerStore {
    static final long INVITE_COOLDOWN_MILLIS = 30_000L;
    private static final String PREFERENCES = "nanbei_friend_drawer";

    private final SharedPreferences preferences;

    FriendDrawerStore(Context context) {
        preferences =
                context.getSharedPreferences(
                        PREFERENCES, Context.MODE_PRIVATE);
    }

    boolean shouldAutoExpandToday(String userId, long nowMillis) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return shouldAutoExpand(
                preferences.getString(expandKey(userId), ""), nowMillis);
    }

    void markExpandedToday(String userId, long nowMillis) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        preferences
                .edit()
                .putString(expandKey(userId), dayKey(nowMillis))
                .apply();
    }

    boolean inviteCoolingDown(
            String userId, long publicPlayerId, long nowMillis) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return inviteCoolingDown(
                preferences.getLong(
                        inviteKey(userId, publicPlayerId), 0L),
                nowMillis);
    }

    void recordInvite(
            String userId, long publicPlayerId, long nowMillis) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        preferences
                .edit()
                .putLong(inviteKey(userId, publicPlayerId), nowMillis)
                .apply();
    }

    void clear() {
        preferences.edit().clear().apply();
    }

    /** Pure daily-expand decision, kept static for JVM unit tests. */
    static boolean shouldAutoExpand(String storedDay, long nowMillis) {
        return !dayKey(nowMillis).equals(storedDay);
    }

    /** Pure cooldown decision, kept static for JVM unit tests. */
    static boolean inviteCoolingDown(long lastAtMillis, long nowMillis) {
        return lastAtMillis > 0L
                && nowMillis - lastAtMillis < INVITE_COOLDOWN_MILLIS
                && nowMillis >= lastAtMillis;
    }

    static String dayKey(long epochMillis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date(epochMillis));
    }

    private static String expandKey(String userId) {
        return "auto_expand_" + userId;
    }

    private static String inviteKey(String userId, long publicPlayerId) {
        return "invite_" + userId + "_" + publicPlayerId;
    }
}
