package com.nanbeiyule.game;

final class MailResponseCallbackAdapter<T> implements MailApiClient.ResponseCallback<T> {
    private final AuthSessionCoordinator.CallCallback<T> callback;

    MailResponseCallbackAdapter(AuthSessionCoordinator.CallCallback<T> callback) {
        this.callback = callback;
    }

    @Override public void onSuccess(T result) { callback.onSuccess(result); }
    @Override public void onUnauthorized() { callback.onUnauthorized(); }
    @Override public void onError(String message) { callback.onError(message); }
}
