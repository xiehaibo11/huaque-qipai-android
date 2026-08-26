package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

/** Mirrors the original once-per-day locked Sxvip record red point. */
final class SxvipRecordBadgeStore {
    private static final String PREFERENCES = "sxvip_record_badge";
    private static final String KEY_SEEN_DATE = "seen_date";

    private final SharedPreferences preferences;

    SxvipRecordBadgeStore(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    boolean shouldShow(boolean membershipAccessGranted) {
        int today = todayKey();
        return shouldShow(
                membershipAccessGranted,
                preferences.getInt(KEY_SEEN_DATE, 0),
                today);
    }

    void markSeen() {
        preferences.edit().putInt(KEY_SEEN_DATE, todayKey()).apply();
    }

    static boolean shouldShow(
            boolean membershipAccessGranted, int seenDate, int today) {
        return !membershipAccessGranted && seenDate != today;
    }

    private static int todayKey() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) * 10_000
                + (calendar.get(Calendar.MONTH) + 1) * 100
                + calendar.get(Calendar.DAY_OF_MONTH);
    }
}
