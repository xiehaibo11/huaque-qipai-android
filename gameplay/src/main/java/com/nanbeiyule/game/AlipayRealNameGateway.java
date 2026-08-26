package com.nanbeiyule.game;

import android.app.Activity;

/**
 * Boundary for the optional Alipay one-tap real-name authorization. The
 * returned auth code is passed directly to the backend; it is never logged
 * or persisted.
 */
interface AlipayRealNameGateway {
    interface Callback {
        void onAuthCode(String authCode);

        void onUnavailable();

        void onCancel();
    }

    boolean isAvailable();

    void startAuth(Activity activity, Callback callback);
}
