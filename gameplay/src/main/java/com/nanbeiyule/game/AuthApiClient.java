package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

final class AuthApiClient implements AuthSessionCoordinator.TokenRefresher {
    interface Callback<T> {
        void onSuccess(T result);

        void onError(String message);
    }

    record OtpRequested(long expiresIn) {
    }

    record SessionTokens(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn) {
    }

    private interface ResponseParser<T> {
        T parse(JSONObject body) throws JSONException;
    }

    private interface HttpCallback<T> {
        void onSuccess(T result);

        void onHttpError(int statusCode, String message);
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    AuthApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    AuthApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void requestOtp(String phoneNumber, Callback<OtpRequested> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("phoneNumber", phoneNumber);
        } catch (JSONException impossible) {
            callback.onError("无法创建登录请求");
            return;
        }
        post(
                "/api/v1/auth/otp/request",
                body,
                response -> new OtpRequested(response.optLong("expiresIn", 300L)),
                callback);
    }

    void verifyOtp(String phoneNumber, String code, Callback<SessionTokens> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("phoneNumber", phoneNumber);
            body.put("code", code);
        } catch (JSONException impossible) {
            callback.onError("无法创建登录请求");
            return;
        }
        post(
                "/api/v1/auth/otp/verify",
                body,
                response ->
                        new SessionTokens(
                                response.getString("accessToken"),
                                response.getString("refreshToken"),
                                response.optString("tokenType", "Bearer"),
                                response.optLong("expiresIn", 900L)),
                callback);
    }

    void loginWithProvider(
            String provider,
            String credential,
            Callback<SessionTokens> callback) {
        if (provider == null || !provider.matches("[a-z][a-z0-9_]*")) {
            callback.onError("登录方式无效");
            return;
        }
        if (credential == null || credential.isBlank()) {
            callback.onError("第三方登录凭证无效");
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("credential", credential);
        } catch (JSONException impossible) {
            callback.onError("无法创建登录请求");
            return;
        }
        post(
                "/api/v1/auth/providers/" + provider + "/login",
                body,
                AuthApiClient::parseSessionTokens,
                callback);
    }

    @Override
    public void refresh(
            String refreshToken,
            AuthSessionCoordinator.RefreshCallback callback) {
        if (refreshToken == null || refreshToken.isBlank()) {
            callback.onRejected();
            return;
        }
        JSONObject body;
        try {
            body = AuthApiProtocol.refreshBody(refreshToken);
        } catch (JSONException impossible) {
            callback.onRejected();
            return;
        }
        postDetailed(
                "/api/v1/auth/refresh",
                body,
                AuthApiClient::parseSessionTokens,
                new HttpCallback<>() {
                    @Override
                    public void onSuccess(SessionTokens result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onHttpError(int statusCode, String message) {
                        if (AuthApiProtocol.isRefreshRejectedStatus(statusCode)) {
                            callback.onRejected();
                        } else {
                            callback.onError(message);
                        }
                    }
                });
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void post(
            String path,
            JSONObject requestBody,
            ResponseParser<T> parser,
            Callback<T> callback) {
        postDetailed(
                path,
                requestBody,
                parser,
                new HttpCallback<>() {
                    @Override
                    public void onSuccess(T result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onHttpError(int statusCode, String message) {
                        callback.onError(message);
                    }
                });
    }

    private <T> void postDetailed(
            String path,
            JSONObject requestBody,
            ResponseParser<T> parser,
            HttpCallback<T> callback) {
        if (baseUrl.isEmpty()) {
            callback.onHttpError(0, "登录服务地址尚未配置");
            return;
        }
        executor.execute(
                () -> {
                    HttpURLConnection connection = null;
                    try {
                        connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
                        connection.setRequestMethod("POST");
                        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                        connection.setReadTimeout(READ_TIMEOUT_MS);
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setRequestProperty(
                                "Content-Type", "application/json; charset=UTF-8");
                        byte[] payload =
                                requestBody.toString().getBytes(StandardCharsets.UTF_8);
                        connection.setFixedLengthStreamingMode(payload.length);
                        try (OutputStream output = connection.getOutputStream()) {
                            output.write(payload);
                        }

                        int statusCode = connection.getResponseCode();
                        String responseText =
                                readBody(
                                        statusCode >= 200 && statusCode < 300
                                                ? connection.getInputStream()
                                                : connection.getErrorStream());
                        if (statusCode < 200 || statusCode >= 300) {
                            postHttpError(
                                    callback,
                                    statusCode,
                                    parseError(responseText, statusCode));
                            return;
                        }
                        T result = parser.parse(new JSONObject(responseText));
                        postHttpSuccess(callback, result);
                    } catch (Exception exception) {
                        postHttpError(
                                callback,
                                0,
                                "无法连接登录服务，请检查网络后重试");
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                });
    }

    private static String readBody(InputStream input) throws IOException {
        if (input == null) {
            return "";
        }
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    private static String parseError(String responseText, int statusCode) {
        if (!responseText.isBlank()) {
            try {
                String detail = new JSONObject(responseText).optString("detail");
                if (!detail.isBlank()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // Fall back to a stable public message.
            }
        }
        return statusCode == 429 ? "验证码请求过于频繁，请稍后重试" : "登录请求失败，请稍后重试";
    }

    private static SessionTokens parseSessionTokens(JSONObject response)
            throws JSONException {
        return new SessionTokens(
                response.getString("accessToken"),
                response.getString("refreshToken"),
                response.optString("tokenType", "Bearer"),
                response.optLong("expiresIn", 900L));
    }

    private <T> void postHttpSuccess(HttpCallback<T> callback, T result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postHttpError(
            HttpCallback<?> callback, int statusCode, String message) {
        mainHandler.post(() -> callback.onHttpError(statusCode, message));
    }
}
