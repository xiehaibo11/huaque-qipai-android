package com.nanbeiyule.game;

import android.app.Activity;

final class UnavailableOneTapLoginGateway
        implements OneTapLoginGateway {
    private static final String MESSAGE =
            "本机号码认证组件尚未安装";

    @Override
    public void preload() {}

    @Override
    public void requestToken(
            Activity activity, Callback callback) {
        callback.onFailure(Failure.UNAVAILABLE, MESSAGE);
    }

    @Override
    public void cancel() {}

    @Override
    public void destroy() {}
}
