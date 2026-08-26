package com.huaque.ui.wechat;

import com.nanbeiyule.game.wechat.WechatSubscriptionIntent;
import com.nanbeiyule.game.wechat.WechatSubscriptionPending;

public interface WechatSubscriptionBackend extends AutoCloseable {
    enum Failure {
        NONE,
        NETWORK,
        UNAUTHORIZED,
        REJECTED
    }

    record Result<T>(T value, Failure failure) {
        public static <T> Result<T> success(T value) {
            return new Result<>(value, Failure.NONE);
        }

        public static <T> Result<T> failure(Failure failure) {
            return new Result<>(null, failure);
        }

        public boolean isSuccess() {
            return failure == Failure.NONE && value != null;
        }
    }

    interface Callback<T> {
        void onComplete(Result<T> result);
    }

    void createIntent(String accessToken, Callback<WechatSubscriptionIntent> callback);

    void complete(
            String accessToken,
            WechatSubscriptionPending pending,
            Callback<String> callback);

    @Override
    void close();
}
