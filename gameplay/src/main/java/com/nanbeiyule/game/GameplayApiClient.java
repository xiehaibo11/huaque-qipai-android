package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import com.nanbeiyule.game.gameplay.GameplayApiProtocol;
import com.nanbeiyule.game.gameplay.GameplayCommandResult;
import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.gameplay.GameplaySnapshot;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONException;
import org.json.JSONObject;

/** Transport-only client for authenticated persistent gameplay sessions. */
final class GameplayApiClient implements GameplayTransport {
    private interface ResponseParser<T> {
        T parse(String response) throws JSONException;
    }

    private static final int TIMEOUT_MS = 10_000;

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong generation = new AtomicLong();
    private final CopyOnWriteArrayList<Future<?>> pending = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<HttpURLConnection> connections =
            new CopyOnWriteArrayList<>();

    GameplayApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    GameplayApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    @Override
    public void open(
            String accessToken,
            String roomNumber,
            Callback<GameplaySnapshot> callback) {
        request(
                "POST",
                sessionPath(roomNumber),
                accessToken,
                null,
                null,
                callback,
                GameplayApiProtocol::parseSnapshot);
    }

    @Override
    public void loadSnapshot(
            String accessToken,
            String roomNumber,
            Callback<GameplaySnapshot> callback) {
        request(
                "GET",
                sessionPath(roomNumber),
                accessToken,
                null,
                null,
                callback,
                GameplayApiProtocol::parseSnapshot);
    }

    @Override
    public void submitCommand(
            String accessToken,
            String roomNumber,
            String idempotencyKey,
            String type,
            long expectedRevision,
            JSONObject payload,
            Callback<GameplayCommandResult> callback) {
        try {
            request(
                    "POST",
                    sessionPath(roomNumber) + "/commands",
                    accessToken,
                    GameplayApiProtocol.commandBody(type, expectedRevision, payload),
                    idempotencyKey,
                    callback,
                    GameplayApiProtocol::parseCommandResult);
        } catch (JSONException | IllegalArgumentException exception) {
            postError(callback, "牌局操作参数不正确");
        }
    }

    @Override
    public void loadEvents(
            String accessToken,
            String roomNumber,
            long afterRevision,
            Callback<List<GameplayEvent>> callback) {
        if (afterRevision < 0) {
            postError(callback, "牌局修订号不正确");
            return;
        }
        request(
                "GET",
                sessionPath(roomNumber) + "/events?afterRevision=" + afterRevision,
                accessToken,
                null,
                null,
                callback,
                GameplayApiProtocol::parseEvents);
    }

    @Override
    public void cancelPending() {
        generation.incrementAndGet();
        for (HttpURLConnection connection : connections) {
            connection.disconnect();
        }
        connections.clear();
        for (Future<?> future : pending) {
            future.cancel(true);
        }
        pending.clear();
    }

    @Override
    public void shutdown() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        cancelPending();
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void request(
            String method,
            String path,
            String accessToken,
            JSONObject body,
            String idempotencyKey,
            Callback<T> callback,
            ResponseParser<T> parser) {
        if (closed.get()) {
            return;
        }
        if (baseUrl.isBlank()) {
            postError(callback, "牌局服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            mainHandler.post(callback::onUnauthorized);
            return;
        }
        long requestGeneration = generation.get();
        pruneFinished();
        try {
            pending.add(
                    executor.submit(
                            () ->
                                    execute(
                                            method,
                                            path,
                                            accessToken,
                                            body,
                                            idempotencyKey,
                                            requestGeneration,
                                            callback,
                                            parser)));
        } catch (RejectedExecutionException exception) {
            if (!closed.get()) {
                postError(callback, "牌局请求暂时无法执行，请稍后重试");
            }
        }
    }

    private <T> void execute(
            String method,
            String path,
            String accessToken,
            JSONObject body,
            String idempotencyKey,
            long requestGeneration,
            Callback<T> callback,
            ResponseParser<T> parser) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connections.add(connection);
            if (!isCurrent(requestGeneration)) {
                return;
            }
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                connection.setRequestProperty("Idempotency-Key", idempotencyKey);
            }
            writeBody(connection, body);
            int status = connection.getResponseCode();
            String response =
                    readBody(
                            status >= 200 && status < 300
                                    ? connection.getInputStream()
                                    : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                postIfCurrent(requestGeneration, callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                postIfCurrent(requestGeneration, () -> callback.onError(parseError(response)));
            } else {
                T result = parser.parse(response);
                postIfCurrent(requestGeneration, () -> callback.onSuccess(result));
            }
        } catch (JSONException | IllegalArgumentException exception) {
            postIfCurrent(requestGeneration, () -> callback.onError("牌局数据格式不正确"));
        } catch (Exception exception) {
            GameplayNetworkDiagnostics.requestFailed(method, baseUrl, path, exception);
            postIfCurrent(requestGeneration, () -> callback.onError("无法连接牌局服务，请检查网络"));
        } finally {
            if (connection != null) {
                connections.remove(connection);
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

    private static String parseError(String response) {
        try {
            String detail = new JSONObject(response).optString("detail", "").trim();
            return detail.isEmpty() ? "牌局请求失败" : detail;
        } catch (JSONException exception) {
            return "牌局请求失败";
        }
    }

    private static String sessionPath(String roomNumber) {
        if (roomNumber == null || !roomNumber.matches("\\d{6}")) {
            throw new IllegalArgumentException("roomNumber must contain six digits");
        }
        return "/api/v1/game-sessions/" + roomNumber;
    }

    private void pruneFinished() {
        for (Future<?> future : pending) {
            if (future.isDone()) {
                pending.remove(future);
            }
        }
    }

    private boolean isCurrent(long requestGeneration) {
        return !closed.get()
                && generation.get() == requestGeneration
                && !Thread.currentThread().isInterrupted();
    }

    private void postIfCurrent(long requestGeneration, Runnable action) {
        mainHandler.post(
                () -> {
                    if (isCurrent(requestGeneration)) {
                        action.run();
                    }
                });
    }

    private <T> void postError(Callback<T> callback, String message) {
        if (!closed.get()) {
            mainHandler.post(() -> callback.onError(message));
        }
    }
}
