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

final class FreeDrawApiClient {
    interface ResponseCallback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    private interface Parser<T> {
        T parse(String json) throws JSONException;
    }

    private static final int TIMEOUT_MS = 10_000;
    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    FreeDrawApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    FreeDrawApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void loadState(String token, ResponseCallback<FreeDrawState> callback) {
        request("GET", "/api/v1/free-draw/state", token, null, callback, FreeDrawProtocol::parseState);
    }

    void openSession(String token, ResponseCallback<FreeDrawSession> callback) {
        request(
                "POST",
                "/api/v1/free-draw/ad-sessions",
                token,
                "{}",
                callback,
                FreeDrawProtocol::parseSession);
    }

    void claim(
            String token,
            FreeDrawSession session,
            RewardedAdGateway.Evidence evidence,
            ResponseCallback<FreeDrawResult> callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("placementId", session.adPlacementId());
            body.put("adSourceId", evidence.adSourceId());
            body.put("showId", evidence.showId());
        } catch (JSONException exception) {
            postError(callback, "广告奖励凭证构造失败，请重试");
            return;
        }
        request(
                "POST",
                "/api/v1/free-draw/ad-sessions/" + session.sessionId() + "/reward",
                token,
                body.toString(),
                callback,
                FreeDrawProtocol::parseResult);
    }

    void shutdown() {
        executor.shutdownNow();
    }

    private <T> void request(
            String method,
            String path,
            String token,
            String body,
            ResponseCallback<T> callback,
            Parser<T> parser) {
        if (token == null || token.isBlank()) {
            mainHandler.post(callback::onUnauthorized);
            return;
        }
        executor.execute(() -> execute(method, path, token, body, callback, parser));
    }

    private <T> void execute(
            String method,
            String path,
            String token,
            String body,
            ResponseCallback<T> callback,
            Parser<T> parser) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            if (body != null) writeBody(connection, body);
            int status = connection.getResponseCode();
            String response = read(status < 300 ? connection.getInputStream() : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                mainHandler.post(callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                postError(callback, parseError(response));
            } else {
                T result = parser.parse(response);
                mainHandler.post(() -> callback.onSuccess(result));
            }
        } catch (JSONException exception) {
            postError(callback, "免费抽奖数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接免费抽奖服务，请检查网络后重试");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static void writeBody(HttpURLConnection connection, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(bytes);
        }
    }

    private static String read(InputStream input) throws IOException {
        if (input == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }

    private static String parseError(String body) {
        if (!body.isBlank()) {
            try {
                JSONObject root = new JSONObject(body);
                String message = root.optString("detail", root.optString("message", ""));
                if (!message.isBlank()) return message;
            } catch (JSONException ignored) {
                // Stable public fallback below.
            }
        }
        return "免费抽奖请求失败，请稍后重试";
    }

    private void postError(ResponseCallback<?> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
