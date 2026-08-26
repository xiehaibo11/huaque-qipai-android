package com.nanbeiyule.game;

import android.app.Activity;

interface RewardedAdGateway {
    record Evidence(String adSourceId, String showId) {}

    interface Listener {
        void onShown();

        void onRewardVerified(Evidence evidence);

        void onClosed();

        void onError(String message);
    }

    void loadAndShow(Activity activity, FreeDrawSession session, Listener listener);

    void release();
}
