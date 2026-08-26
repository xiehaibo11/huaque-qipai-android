package com.nanbeiyule.game;

/** 将会员接口的 HTTP 回调转接到统一鉴权重试边界。 */
final class MembershipCallbackAdapter {
    private MembershipCallbackAdapter() {}

    static <T> MembershipApiClient.ResponseCallback<T> from(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new MembershipApiClient.ResponseCallback<>() {
            @Override
            public void onSuccess(T result) {
                callback.onSuccess(result);
            }

            @Override
            public void onUnauthorized() {
                callback.onUnauthorized();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }
}
