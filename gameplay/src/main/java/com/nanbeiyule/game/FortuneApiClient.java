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
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

/** Transport-only client for the server-authoritative fortune catalog and wallet operations. */
final class FortuneApiClient {
    interface ResponseCallback<T> {
        void onSuccess(T result);
        void onUnauthorized();
        void onError(String message);
    }

    private interface Parser<T> { T parse(String body) throws JSONException; }

    private static final int TIMEOUT_MILLIS = 10_000;
    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean closed = new AtomicBoolean();

    FortuneApiClient() { this(BuildConfig.API_BASE_URL); }

    FortuneApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void loadState(String token, ResponseCallback<FortuneState> callback) {
        request("GET", "/api/v1/fortune/state", token, null, null, callback,
                body -> FortuneState.fromJson(new JSONObject(body)));
    }

    void pray(
            String token,
            String key,
            String productCode,
            int quantity,
            ResponseCallback<Boolean> callback) {
        try {
            request(
                    "POST",
                    "/api/v1/fortune/prayers",
                    token,
                    key,
                    new JSONObject().put("productCode", productCode).put("quantity", quantity),
                    callback,
                    ignored -> Boolean.TRUE);
        } catch (JSONException exception) {
            postError(callback, "求财运参数不正确");
        }
    }

    void drawTreasures(
            String token,
            String key,
            int count,
            ResponseCallback<FortuneTreasureDrawResult> callback) {
        try {
            request(
                    "POST",
                    "/api/v1/fortune/treasure-draws",
                    token,
                    key,
                    new JSONObject().put("count", count),
                    callback,
                    body -> FortuneTreasureDrawResult.fromJson(new JSONObject(body)));
        } catch (JSONException exception) {
            postError(callback, "聚宝盆参数不正确");
        }
    }

    void activateCaishen(
            String token,
            String key,
            String productCode,
            ResponseCallback<Boolean> callback) {
        try {
            request(
                    "POST",
                    "/api/v1/fortune/caishen-activations",
                    token,
                    key,
                    new JSONObject().put("productCode", productCode),
                    callback,
                    ignored -> Boolean.TRUE);
        } catch (JSONException exception) {
            postError(callback, "请财神参数不正确");
        }
    }

    void shutdown() {
        if (!closed.compareAndSet(false, true)) return;
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void request(
            String method,
            String path,
            String token,
            String key,
            JSONObject body,
            ResponseCallback<T> callback,
            Parser<T> parser) {
        if (closed.get()) return;
        if (baseUrl.isBlank()) {
            postError(callback, "财运服务地址尚未配置");
            return;
        }
        if (token == null || token.isBlank()) {
            mainHandler.post(callback::onUnauthorized);
            return;
        }
        executor.execute(() -> execute(method, path, token, key, body, callback, parser));
    }

    private <T> void execute(
            String method,
            String path,
            String token,
            String key,
            JSONObject body,
            ResponseCallback<T> callback,
            Parser<T> parser) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            if (key != null) connection.setRequestProperty("Idempotency-Key", key);
            write(connection, body);
            int status = connection.getResponseCode();
            String response = read(status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                post(callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                post(() -> callback.onError(error(response)));
            } else {
                T result = parser.parse(response);
                post(() -> callback.onSuccess(result));
            }
        } catch (JSONException exception) {
            post(() -> callback.onError("财运数据格式不正确"));
        } catch (Exception exception) {
            post(() -> callback.onError("无法连接财运服务，请检查网络"));
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static void write(HttpURLConnection connection, JSONObject body) throws IOException {
        if (body == null) return;
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream output = connection.getOutputStream()) { output.write(bytes); }
    }

    private static String read(InputStream input) throws IOException {
        if (input == null) return "";
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }
        return body.toString();
    }

    private static String error(String body) {
        try {
            String detail = new JSONObject(body).optString("detail", "");
            return detail.isBlank() ? "财运请求失败" : detail;
        } catch (JSONException ignored) {
            return "财运请求失败";
        }
    }

    private void post(Runnable action) {
        if (!closed.get()) mainHandler.post(() -> { if (!closed.get()) action.run(); });
    }

    private void postError(ResponseCallback<?> callback, String message) {
        post(() -> callback.onError(message));
    }
}
