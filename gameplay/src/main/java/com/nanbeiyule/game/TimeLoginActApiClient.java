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

/**
 * 定时登录有礼的受保护 API。三个端点都要求 Bearer Token，两个写端点强制
 * {@code Idempotency-Key}；客户端不在本地推导领取资格，也不伪造钱包。
 */
final class TimeLoginActApiClient {
    interface ResponseCallback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    private interface ResponseParser<T> {
        T parse(String responseText) throws JSONException;
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    TimeLoginActApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    TimeLoginActApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void loadState(String accessToken, ResponseCallback<TimeLoginActState> callback) {
        request(
                "GET",
                "/api/v1/time-login/state",
                accessToken,
                null,
                null,
                callback,
                TimeLoginActProtocol::parseState);
    }

    void claimSlot(
            String accessToken,
            String idempotencyKey,
            String rewardId,
            ResponseCallback<TimeLoginClaimResult> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("rewardId", rewardId);
        } catch (JSONException exception) {
            postError(callback, "领取请求构造失败，请稍后重试");
            return;
        }
        request(
                "POST",
                "/api/v1/time-login/claims",
                accessToken,
                idempotencyKey,
                body.toString(),
                callback,
                TimeLoginActProtocol::parseClaim);
    }

    void drawWheel(
            String accessToken,
            String idempotencyKey,
            ResponseCallback<TimeLoginClaimResult> callback) {
        request(
                "POST",
                "/api/v1/time-login/wheel-draws",
                accessToken,
                idempotencyKey,
                "{}",
                callback,
                TimeLoginActProtocol::parseClaim);
    }

    void shutdown() {
        executor.shutdownNow();
    }

    private <T> void request(
            String method,
            String path,
            String accessToken,
            String idempotencyKey,
            String body,
            ResponseCallback<T> callback,
            ResponseParser<T> parser) {
        if (accessToken == null || accessToken.isBlank()) {
            postUnauthorized(callback);
            return;
        }
        executor.execute(
                () -> execute(method, path, accessToken, idempotencyKey, body, callback, parser));
    }

    private <T> void execute(
            String method,
            String path,
            String accessToken,
            String idempotencyKey,
            String body,
            ResponseCallback<T> callback,
            ResponseParser<T> parser) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            if (idempotencyKey != null) {
                connection.setRequestProperty("Idempotency-Key", idempotencyKey);
            }
            if (body != null) {
                byte[] payload = body.getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setFixedLengthStreamingMode(payload.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(payload);
                }
            }
            int status = connection.getResponseCode();
            String responseText =
                    readBody(
                            status >= 200 && status < 300
                                    ? connection.getInputStream()
                                    : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                postUnauthorized(callback);
            } else if (status < 200 || status >= 300) {
                postError(callback, parseError(responseText));
            } else {
                postSuccess(callback, parser.parse(responseText));
            }
        } catch (JSONException exception) {
            postError(callback, "定时登录数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接定时登录服务，请检查网络后重试");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readBody(InputStream input) throws IOException {
        if (input == null) {
            return "";
        }
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    private static String parseError(String responseText) {
        if (!responseText.isBlank()) {
            try {
                JSONObject payload = new JSONObject(responseText);
                String detail = payload.optString("detail", payload.optString("message", ""));
                if (!detail.isBlank()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // Stable public fallback below.
            }
        }
        return "定时登录请求失败，请稍后重试";
    }

    private <T> void postSuccess(ResponseCallback<T> callback, T result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postUnauthorized(ResponseCallback<?> callback) {
        mainHandler.post(callback::onUnauthorized);
    }

    private void postError(ResponseCallback<?> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
