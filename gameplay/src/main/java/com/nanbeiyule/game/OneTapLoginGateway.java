package com.nanbeiyule.game;

import android.app.Activity;

interface OneTapLoginGateway {
    enum Failure {
        UNAVAILABLE,
        CANCELLED,
        FAILED
    }

    interface Callback {
        void onToken(String token);

        void onFailure(Failure failure, String publicMessage);
    }

    void preload();

    void requestToken(Activity activity, Callback callback);

    void cancel();

    void destroy();
}
