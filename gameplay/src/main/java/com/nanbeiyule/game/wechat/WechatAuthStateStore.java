package com.nanbeiyule.game.wechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.security.SecureRandom;

public final class WechatAuthStateStore {
    private static final String PREFERENCES = "nanbei_wechat_auth";
    private static final String PENDING_STATE = "pending_state";
    private static final String CREATED_AT_MILLIS = "created_at_millis";
    private static final long STATE_TTL_MILLIS = 10L * 60L * 1_000L;

    private final SharedPreferences preferences;
    private final SecureRandom secureRandom = new SecureRandom();

    public WechatAuthStateStore(Context context) {
        preferences =
                context.getApplicationContext()
                        .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public String begin() {
        byte[] random = new byte[32];
        secureRandom.nextBytes(random);
        String state =
                Base64.encodeToString(
                        random, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        boolean persisted =
                preferences
                        .edit()
                        .putString(PENDING_STATE, state)
                        .putLong(CREATED_AT_MILLIS, System.currentTimeMillis())
                        .commit();
        return persisted ? state : "";
    }

    public boolean consume(String actualState) {
        String expectedState = preferences.getString(PENDING_STATE, "");
        long createdAtMillis = preferences.getLong(CREATED_AT_MILLIS, 0L);
        long nowMillis = System.currentTimeMillis();
        if (expectedState == null || expectedState.isBlank() || createdAtMillis <= 0L) {
            return false;
        }

        WechatAuthState pending =
                new WechatAuthState(expectedState, createdAtMillis, STATE_TTL_MILLIS);
        boolean accepted = pending.consume(actualState, nowMillis);
        if (accepted
                || nowMillis < createdAtMillis
                || nowMillis - createdAtMillis > STATE_TTL_MILLIS) {
            clear();
        }
        return accepted;
    }

    public void clear() {
        preferences.edit().clear().commit();
    }
}
