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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONException;
import org.json.JSONObject;

/** Authenticated REST boundary for persistent match-arena list and creation. */
final class MatchArenaApiClient {
    interface Callback<T> {
        void onSuccess(T result);
        void onUnauthorized();
        void onError(String message);
    }

    private interface Parser<T> {
        T parse(String response) throws JSONException;
    }

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicLong generation = new AtomicLong();
    private Future<?> pending;

    MatchArenaApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    MatchArenaApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void list(String accessToken, Callback<List<MatchArenaSummary>> callback) {
        request(
                "GET",
                "/api/v1/match-arenas",
                accessToken,
                null,
                null,
                callback,
                MatchArenaProtocol::parseList);
    }

    void create(
            String accessToken,
            long lobbyId,
            MatchArenaCreateState state,
            String idempotencyKey,
            Callback<MatchArenaSummary> callback) {
        try {
            request(
                    "POST",
                    "/api/v1/match-arenas",
                    accessToken,
                    new JSONObject(MatchArenaProtocol.createBody(lobbyId, state)),
                    idempotencyKey,
                    callback,
                    response -> MatchArenaProtocol.parseSummary(new JSONObject(response)));
        } catch (JSONException exception) {
            postError(callback, "创建比赛场参数不正确");
        }
    }

    void cancelPending() {
        generation.incrementAndGet();
        if (pending != null) {
            pending.cancel(true);
            pending = null;
        }
    }

    void shutdown() {
        cancelPending();
        executor.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }

    private <T> void request(
            String method,
            String path,
            String accessToken,
            JSONObject body,
            String idempotencyKey,
            Callback<T> callback,
            Parser<T> parser) {
        if (baseUrl.isBlank()) {
            postError(callback, "比赛场服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            main.post(callback::onUnauthorized);
            return;
        }
        long requestGeneration = generation.get();
        pending =
                executor.submit(
                        () -> execute(
                                method,
                                path,
                                accessToken,
                                body,
                                idempotencyKey,
                                requestGeneration,
                                callback,
                                parser));
    }

    private <T> void execute(
            String method,
            String path,
            String accessToken,
            JSONObject body,
            String idempotencyKey,
            long requestGeneration,
            Callback<T> callback,
            Parser<T> parser) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(10_000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            if (idempotencyKey != null) {
                connection.setRequestProperty("Idempotency-Key", idempotencyKey);
            }
            writeBody(connection, body);
            int status = connection.getResponseCode();
            String response = readBody(
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                post(requestGeneration, callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                postError(requestGeneration, callback, errorMessage(response));
            } else {
                T value = parser.parse(response);
                post(requestGeneration, () -> callback.onSuccess(value));
            }
        } catch (JSONException exception) {
            postError(requestGeneration, callback, "比赛场数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(requestGeneration, callback, "无法连接比赛场服务，请检查网络后重试");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void writeBody(HttpURLConnection connection, JSONObject body)
            throws IOException {
        if (body == null) {
            return;
        }
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(bytes);
        }
    }

    private static String readBody(InputStream input) throws IOException {
        if (input == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    private static String errorMessage(String response) {
        try {
            String detail = new JSONObject(response).optString("detail");
            return detail.isBlank() ? "比赛场请求失败，请稍后重试" : detail;
        } catch (JSONException ignored) {
            return "比赛场请求失败，请稍后重试";
        }
    }

    private void post(long requestGeneration, Runnable action) {
        main.post(() -> {
            if (generation.get() == requestGeneration) {
                action.run();
            }
        });
    }

    private void postError(Callback<?> callback, String message) {
        main.post(() -> callback.onError(message));
    }

    private void postError(long requestGeneration, Callback<?> callback, String message) {
        post(requestGeneration, () -> callback.onError(message));
    }
}
