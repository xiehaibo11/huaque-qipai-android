package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

final class AuthSessionCoordinator {
    interface TokenStore {
        SessionTokenSnapshot snapshot();

        void save(AuthApiClient.SessionTokens tokens);

        void clear();
    }

    interface TokenRefresher {
        void refresh(String refreshToken, RefreshCallback callback);
    }

    interface RefreshCallback {
        void onSuccess(AuthApiClient.SessionTokens tokens);

        void onRejected();

        void onError(String message);
    }

    interface AuthenticatedCall<T> {
        void execute(String accessToken, CallCallback<T> callback);
    }

    interface CallCallback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    interface Callback<T> {
        void onSuccess(T result);

        void onLoginRequired();

        void onError(String message);
    }

    private interface AccessTokenCallback {
        void onReady(String accessToken);

        void onLoginRequired();

        void onError(String message);
    }

    private static final long MINIMUM_ACCESS_TOKEN_VALIDITY_SECONDS = 60L;

    private final TokenStore tokenStore;
    private final TokenRefresher tokenRefresher;
    private final LongSupplier clock;
    private final List<AccessTokenCallback> refreshWaiters = new ArrayList<>();

    private boolean refreshInFlight;
    private long sessionGeneration;

    AuthSessionCoordinator(TokenStore tokenStore, TokenRefresher tokenRefresher) {
        this(
                tokenStore,
                tokenRefresher,
                () -> System.currentTimeMillis() / 1_000L);
    }

    AuthSessionCoordinator(
            TokenStore tokenStore,
            TokenRefresher tokenRefresher,
            LongSupplier clock) {
        this.tokenStore = tokenStore;
        this.tokenRefresher = tokenRefresher;
        this.clock = clock;
    }

    boolean hasRecoverableSession() {
        return tokenStore
                .snapshot()
                .hasRecoverableSession(clock.getAsLong());
    }

    void clearSession() {
        List<AccessTokenCallback> waiters;
        synchronized (this) {
            sessionGeneration++;
            tokenStore.clear();
            waiters = drainRefreshWaitersLocked();
        }
        for (AccessTokenCallback waiter : waiters) {
            waiter.onLoginRequired();
        }
    }

    <T> void execute(AuthenticatedCall<T> call, Callback<T> callback) {
        acquireAccessToken(
                false,
                new AccessTokenCallback() {
                    @Override
                    public void onReady(String accessToken) {
                        executeWithToken(call, callback, accessToken, false);
                    }

                    @Override
                    public void onLoginRequired() {
                        callback.onLoginRequired();
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    private <T> void executeWithToken(
            AuthenticatedCall<T> call,
            Callback<T> callback,
            String accessToken,
            boolean retriedAfterRefresh) {
        call.execute(
                accessToken,
                new CallCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onUnauthorized() {
                        if (retriedAfterRefresh) {
                            clearSession();
                            callback.onLoginRequired();
                            return;
                        }
                        acquireAccessToken(
                                true,
                                new AccessTokenCallback() {
                                    @Override
                                    public void onReady(String refreshedAccessToken) {
                                        executeWithToken(
                                                call,
                                                callback,
                                                refreshedAccessToken,
                                                true);
                                    }

                                    @Override
                                    public void onLoginRequired() {
                                        callback.onLoginRequired();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                });
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
    }

    private void acquireAccessToken(
            boolean forceRefresh,
            AccessTokenCallback callback) {
        String immediateAccessToken = "";
        String refreshTokenToUse = "";
        boolean loginRequired = false;
        long refreshGenerationToUse = -1L;

        synchronized (this) {
            SessionTokenSnapshot snapshot = tokenStore.snapshot();
            long now = clock.getAsLong();
            if (!forceRefresh) {
                immediateAccessToken =
                        snapshot.usableAccessToken(
                                now,
                                MINIMUM_ACCESS_TOKEN_VALIDITY_SECONDS);
            }
            if (immediateAccessToken.isBlank()) {
                refreshTokenToUse = snapshot.refreshToken();
                if (refreshTokenToUse.isBlank()) {
                    loginRequired = true;
                } else {
                    refreshWaiters.add(callback);
                    if (refreshInFlight) {
                        return;
                    }
                    refreshInFlight = true;
                    refreshGenerationToUse = sessionGeneration;
                }
            }
        }

        if (!immediateAccessToken.isBlank()) {
            callback.onReady(immediateAccessToken);
            return;
        }
        if (loginRequired) {
            clearSession();
            callback.onLoginRequired();
            return;
        }

        long startedRefreshGeneration = refreshGenerationToUse;
        tokenRefresher.refresh(
                refreshTokenToUse,
                new RefreshCallback() {
                    @Override
                    public void onSuccess(AuthApiClient.SessionTokens tokens) {
                        if (tokens == null
                                || tokens.accessToken() == null
                                || tokens.accessToken().isBlank()
                                || tokens.refreshToken() == null
                                || tokens.refreshToken().isBlank()) {
                            finishRefreshRejected(startedRefreshGeneration);
                            return;
                        }
                        finishRefreshSuccess(startedRefreshGeneration, tokens);
                    }

                    @Override
                    public void onRejected() {
                        finishRefreshRejected(startedRefreshGeneration);
                    }

                    @Override
                    public void onError(String message) {
                        finishRefreshError(startedRefreshGeneration, message);
                    }
                });
    }

    private void finishRefreshSuccess(
            long refreshGeneration, AuthApiClient.SessionTokens tokens) {
        List<AccessTokenCallback> waiters;
        synchronized (this) {
            if (!isCurrentRefreshLocked(refreshGeneration)) {
                return;
            }
            tokenStore.save(tokens);
            waiters = drainRefreshWaitersLocked();
        }
        for (AccessTokenCallback waiter : waiters) {
            waiter.onReady(tokens.accessToken());
        }
    }

    private void finishRefreshRejected(long refreshGeneration) {
        List<AccessTokenCallback> waiters;
        synchronized (this) {
            if (!isCurrentRefreshLocked(refreshGeneration)) {
                return;
            }
            sessionGeneration++;
            tokenStore.clear();
            waiters = drainRefreshWaitersLocked();
        }
        for (AccessTokenCallback waiter : waiters) {
            waiter.onLoginRequired();
        }
    }

    private void finishRefreshError(long refreshGeneration, String message) {
        String safeMessage =
                message == null || message.isBlank()
                        ? "无法刷新登录状态，请检查网络后重试"
                        : message;
        List<AccessTokenCallback> waiters;
        synchronized (this) {
            if (!isCurrentRefreshLocked(refreshGeneration)) {
                return;
            }
            waiters = drainRefreshWaitersLocked();
        }
        for (AccessTokenCallback waiter : waiters) {
            waiter.onError(safeMessage);
        }
    }

    private boolean isCurrentRefreshLocked(long refreshGeneration) {
        return refreshInFlight && refreshGeneration == sessionGeneration;
    }

    private List<AccessTokenCallback> drainRefreshWaitersLocked() {
        List<AccessTokenCallback> waiters = new ArrayList<>(refreshWaiters);
        refreshWaiters.clear();
        refreshInFlight = false;
        return waiters;
    }
}
