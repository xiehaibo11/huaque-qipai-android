package com.huaque.ui;

import android.content.Context;
import android.content.SharedPreferences;

final class LoginAgreementConsentStore {
    private static final String PREFERENCES = "nanbei_login_agreement";
    private static final String ACCEPTED_VERSION = "accepted_version";

    private final SharedPreferences preferences;

    LoginAgreementConsentStore(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    String acceptedVersion() {
        return preferences.getString(ACCEPTED_VERSION, "");
    }

    void accept(String version) {
        preferences.edit().putString(ACCEPTED_VERSION, version).apply();
    }
}
