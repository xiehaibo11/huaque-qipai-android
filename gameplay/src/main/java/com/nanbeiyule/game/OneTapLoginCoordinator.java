package com.nanbeiyule.game;

import android.app.Activity;

final class OneTapLoginCoordinator {
    interface ProviderLogin {
        void login(
                String provider,
                String credential,
                ProviderCallback callback);
    }

    interface ProviderCallback {
        void onSuccess(AuthApiClient.SessionTokens tokens);

        void onError(String message);
    }

    interface TokenStore {
        void save(AuthApiClient.SessionTokens tokens);
    }

    interface View {
        void onOneTapLoginStarted();

        void onOneTapLoginSucceeded();

        void onSmsFallback(String errorMessage);
    }

    private static final String PROVIDER = "one_tap";
    private static final String SDK_FAILURE_MESSAGE =
            "本机号码认证失败，请使用短信验证码登录";
    private static final String BACKEND_FAILURE_MESSAGE =
            "本机号码登录失败，请使用短信验证码登录";

    private final Activity activity;
    private final OneTapLoginGateway gateway;
    private final ProviderLogin providerLogin;
    private final TokenStore tokenStore;
    private final View view;

    private boolean destroyed;
    private boolean inFlight;
    private long generation;
    private String pendingCredential;

    OneTapLoginCoordinator(
            Activity activity,
            OneTapLoginGateway gateway,
            ProviderLogin providerLogin,
            TokenStore tokenStore,
            View view) {
        this.activity = activity;
        this.gateway = gateway;
        this.providerLogin = providerLogin;
        this.tokenStore = tokenStore;
        this.view = view;
    }

    void preload() {
        synchronized (this) {
            if (destroyed) {
                return;
            }
        }
        gateway.preload();
    }

    void start() {
        final long requestGeneration;
        synchronized (this) {
            if (destroyed || inFlight) {
                return;
            }
            inFlight = true;
            requestGeneration = ++generation;
        }

        view.onOneTapLoginStarted();
        gateway.requestToken(
                activity,
                new OneTapLoginGateway.Callback() {
                    @Override
                    public void onToken(String token) {
                        handleCarrierToken(requestGeneration, token);
                    }

                    @Override
                    public void onFailure(
                            OneTapLoginGateway.Failure failure,
                            String publicMessage) {
                        handleGatewayFailure(requestGeneration, failure);
                    }
                });
    }

    void cancel() {
        synchronized (this) {
            if (destroyed) {
                return;
            }
            generation++;
            inFlight = false;
            pendingCredential = null;
        }
        gateway.cancel();
    }

    void destroy() {
        synchronized (this) {
            if (destroyed) {
                return;
            }
            destroyed = true;
            generation++;
            inFlight = false;
            pendingCredential = null;
        }
        gateway.destroy();
    }

    boolean isInFlightForTest() {
        synchronized (this) {
            return inFlight;
        }
    }

    private void handleCarrierToken(
            long requestGeneration, String token) {
        if (token == null || token.isBlank()) {
            finishWithFallback(
                    requestGeneration, SDK_FAILURE_MESSAGE);
            return;
        }

        final String credential;
        synchronized (this) {
            if (!isCurrentLocked(requestGeneration)) {
                return;
            }
            pendingCredential = token;
            credential = pendingCredential;
            pendingCredential = null;
        }

        providerLogin.login(
                PROVIDER,
                credential,
                new ProviderCallback() {
                    @Override
                    public void onSuccess(
                            AuthApiClient.SessionTokens tokens) {
                        handleBackendSuccess(
                                requestGeneration, tokens);
                    }

                    @Override
                    public void onError(String message) {
                        String safeMessage =
                                message == null || message.isBlank()
                                        ? BACKEND_FAILURE_MESSAGE
                                        : message;
                        finishWithFallback(
                                requestGeneration, safeMessage);
                    }
                });
    }

    private void handleGatewayFailure(
            long requestGeneration,
            OneTapLoginGateway.Failure failure) {
        String message =
                failure == OneTapLoginGateway.Failure.FAILED
                        ? SDK_FAILURE_MESSAGE
                        : null;
        finishWithFallback(requestGeneration, message);
    }

    private void handleBackendSuccess(
            long requestGeneration,
            AuthApiClient.SessionTokens tokens) {
        if (!validTokens(tokens)) {
            finishWithFallback(
                    requestGeneration, BACKEND_FAILURE_MESSAGE);
            return;
        }

        synchronized (this) {
            if (!isCurrentLocked(requestGeneration)) {
                return;
            }
            inFlight = false;
            pendingCredential = null;
        }
        tokenStore.save(tokens);
        view.onOneTapLoginSucceeded();
    }

    private void finishWithFallback(
            long requestGeneration, String errorMessage) {
        synchronized (this) {
            if (!isCurrentLocked(requestGeneration)) {
                return;
            }
            inFlight = false;
            pendingCredential = null;
        }
        view.onSmsFallback(errorMessage);
    }

    private boolean isCurrentLocked(long requestGeneration) {
        return !destroyed
                && inFlight
                && requestGeneration == generation;
    }

    private static boolean validTokens(
            AuthApiClient.SessionTokens tokens) {
        return tokens != null
                && tokens.accessToken() != null
                && !tokens.accessToken().isBlank()
                && tokens.refreshToken() != null
                && !tokens.refreshToken().isBlank();
    }
}
