package com.nanbeiyule.game;

import android.app.Activity;

/** Fallback used when the Alipay SDK AAR is not bundled in this build. */
final class UnavailableAlipayRealNameGateway
        implements AlipayRealNameGateway {
    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void startAuth(Activity activity, Callback callback) {
        if (callback != null) {
            callback.onUnavailable();
        }
    }
}
