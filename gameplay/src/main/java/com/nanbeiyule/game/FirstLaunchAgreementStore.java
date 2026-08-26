package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;

final class FirstLaunchAgreementStore {
    static final int CURRENT_VERSION = 1;
    private static final String PREFERENCES = "first_launch_agreement";
    private static final String ACCEPTED_VERSION = "accepted_version";

    private final SharedPreferences preferences;

    FirstLaunchAgreementStore(Context context) {
        preferences =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREFERENCES,
                                Context.MODE_PRIVATE);
    }

    boolean isAccepted() {
        return preferences.getInt(ACCEPTED_VERSION, 0)
                >= CURRENT_VERSION;
    }

    void accept() {
        preferences.edit()
                .putInt(ACCEPTED_VERSION, CURRENT_VERSION)
                .apply();
    }
}
