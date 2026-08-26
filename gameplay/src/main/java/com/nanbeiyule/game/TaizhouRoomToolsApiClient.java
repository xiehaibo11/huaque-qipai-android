package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONException;
import org.json.JSONObject;

/** Authenticated transport for Taizhou room reservations, chat and voice. */
final class TaizhouRoomToolsApiClient implements TaizhouRoomToolsTransport {
    private interface JsonParser<T> {
        T parse(JSONObject json) throws JSONException;
    }

    private static final int TIMEOUT_MILLIS = 10_000;
    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final CopyOnWriteArrayList<Future<?>> pending = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<HttpURLConnection> connections = new CopyOnWriteArrayList<>();
    private final AtomicLong generation = new AtomicLong();
    private final AtomicBoolean closed = new AtomicBoolean();

    TaizhouRoomToolsApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    TaizhouRoomToolsApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    @Override
    public void loadState(
            String token, String roomNumber, Callback<TaizhouRoomToolsState> callback) {
        requestJson("GET", toolsPath(roomNumber), token, null, null, callback,
                TaizhouRoomToolsState::fromJson);
    }

    @Override
    public void setReservation(
            String token,
            String roomNumber,
            String idempotencyKey,
            TaizhouRoomToolType type,
            boolean active,
            Callback<TaizhouRoomToolsState.Reservation> callback) {
        try {
            requestJson(
                    "PUT",
                    toolsPath(roomNumber) + "/reservations/" + type.name(),
                    token,
                    new JSONObject().put("active", active),
                    idempotencyKey,
                    callback,
                    json ->
                            new TaizhouRoomToolsState.Reservation(
                                    type,
                                    json.optInt("targetRound", 0),
                                    json.optBoolean("active", false),
                                    ""));
        } catch (JSONException exception) {
            postError(callback, "预约参数不正确");
        }
    }

    @Override
    public void sendMessage(
            String token,
            String roomNumber,
            String idempotencyKey,
            String type,
            int contentIndex,
            Callback<TaizhouRoomToolsState.Message> callback) {
        try {
            requestJson(
                    "POST",
                    toolsPath(roomNumber) + "/messages",
                    token,
                    new JSONObject().put("type", type).put("contentIndex", contentIndex),
                    idempotencyKey,
                    callback,
                    json -> TaizhouRoomToolsState.Message.fromJson(json.getJSONObject("message")));
        } catch (JSONException exception) {
            postError(callback, "聊天参数不正确");
        }
    }

    @Override
    public void sendVoice(
            String token,
            String roomNumber,
            String idempotencyKey,
            int durationMillis,
            byte[] data,
            Callback<TaizhouRoomToolsState.Message> callback) {
        if (data == null) {
            postError(callback, "语音内容为空");
            return;
        }
        submit(
                requestGeneration ->
                        executeVoice(
                                toolsPath(roomNumber) + "/voice",
                                token,
                                idempotencyKey,
                                durationMillis,
                                data,
                                requestGeneration,
                                callback));
    }

    @Override
    public void loadVoice(
            String token,
            String roomNumber,
            String messageId,
            Callback<byte[]> callback) {
        if (messageId == null || !messageId.matches("[0-9a-fA-F-]{36}")) {
            postError(callback, "语音消息不存在");
            return;
        }
        submit(
                requestGeneration ->
                        executeBytes(
                                toolsPath(roomNumber) + "/voice/" + messageId,
                                token,
                                requestGeneration,
                                callback));
    }

    @Override
    public void cancelPending() {
        generation.incrementAndGet();
        for (HttpURLConnection connection : connections) connection.disconnect();
        connections.clear();
        for (Future<?> future : pending) future.cancel(true);
        pending.clear();
    }

    @Override
    public void shutdown() {
        if (!closed.compareAndSet(false, true)) return;
        cancelPending();
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void requestJson(
            String method,
            String path,
            String token,
            JSONObject body,
            String idempotencyKey,
            Callback<T> callback,
            JsonParser<T> parser) {
        submit(
                requestGeneration ->
                        executeJson(
                                method,
                                path,
                                token,
                                body,
                                idempotencyKey,
                                requestGeneration,
                                callback,
                                parser));
    }

    private <T> void executeJson(
            String method,
            String path,
            String token,
            JSONObject body,
            String idempotencyKey,
            long requestGeneration,
            Callback<T> callback,
            JsonParser<T> parser) {
        HttpURLConnection connection = null;
        try {
            connection = connection(method, path, token);
            connections.add(connection);
            if (idempotencyKey != null) {
                connection.setRequestProperty("Idempotency-Key", idempotencyKey);
            }
            if (body != null) {
                write(connection, "application/json; charset=utf-8",
                        body.toString().getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            byte[] bytes = read(status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                post(requestGeneration, callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                post(requestGeneration, () -> callback.onError(error(bytes)));
            } else {
                T result = parser.parse(new JSONObject(new String(bytes, StandardCharsets.UTF_8)));
                post(requestGeneration, () -> callback.onSuccess(result));
            }
        } catch (JSONException exception) {
            post(requestGeneration, () -> callback.onError("房间工具数据格式不正确"));
        } catch (Exception exception) {
            post(requestGeneration, () -> callback.onError("无法连接房间工具服务，请检查网络"));
        } finally {
            release(connection);
        }
    }

    private void executeVoice(
            String path,
            String token,
            String idempotencyKey,
            int durationMillis,
            byte[] data,
            long requestGeneration,
            Callback<TaizhouRoomToolsState.Message> callback) {
        HttpURLConnection connection = null;
        try {
            connection = connection("POST", path, token);
            connections.add(connection);
            connection.setRequestProperty("Idempotency-Key", idempotencyKey);
            connection.setRequestProperty("X-Voice-Duration-Millis", Integer.toString(durationMillis));
            write(connection, "audio/mp4", data);
            int status = connection.getResponseCode();
            byte[] bytes = read(status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                post(requestGeneration, callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                post(requestGeneration, () -> callback.onError(error(bytes)));
            } else {
                JSONObject body = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
                TaizhouRoomToolsState.Message message =
                        TaizhouRoomToolsState.Message.fromJson(body.getJSONObject("message"));
                post(requestGeneration, () -> callback.onSuccess(message));
            }
        } catch (JSONException exception) {
            post(requestGeneration, () -> callback.onError("语音数据格式不正确"));
        } catch (Exception exception) {
            post(requestGeneration, () -> callback.onError("语音发送失败，请检查网络"));
        } finally {
            release(connection);
        }
    }

    private void executeBytes(
            String path,
            String token,
            long requestGeneration,
            Callback<byte[]> callback) {
        HttpURLConnection connection = null;
        try {
            connection = connection("GET", path, token);
            connections.add(connection);
            int status = connection.getResponseCode();
            byte[] bytes = read(status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                post(requestGeneration, callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                post(requestGeneration, () -> callback.onError(error(bytes)));
            } else {
                post(requestGeneration, () -> callback.onSuccess(bytes));
            }
        } catch (Exception exception) {
            post(requestGeneration, () -> callback.onError("语音读取失败，请检查网络"));
        } finally {
            release(connection);
        }
    }

    private HttpURLConnection connection(String method, String path, String token) throws IOException {
        if (baseUrl.isBlank()) throw new IOException("missing base URL");
        if (token == null || token.isBlank()) throw new IOException("missing token");
        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        return connection;
    }

    private interface Work { void run(long requestGeneration); }

    private void submit(Work work) {
        if (closed.get()) return;
        long requestGeneration = generation.get();
        for (Future<?> future : pending) {
            if (future.isDone()) pending.remove(future);
        }
        pending.add(executor.submit(() -> work.run(requestGeneration)));
    }

    private static void write(HttpURLConnection connection, String mediaType, byte[] bytes)
            throws IOException {
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", mediaType);
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream output = connection.getOutputStream()) { output.write(bytes); }
    }

    private static byte[] read(InputStream input) throws IOException {
        if (input == null) return new byte[0];
        try (input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toByteArray();
        }
    }

    private static String error(byte[] bytes) {
        try {
            String detail = new JSONObject(new String(bytes, StandardCharsets.UTF_8))
                    .optString("detail", "");
            return detail.isBlank() ? "房间工具请求失败" : detail;
        } catch (JSONException ignored) {
            return "房间工具请求失败";
        }
    }

    private static String toolsPath(String roomNumber) {
        if (roomNumber == null || !roomNumber.matches("\\d{6}")) {
            throw new IllegalArgumentException("roomNumber must contain six digits");
        }
        return "/api/v1/game-sessions/" + roomNumber + "/tools";
    }

    private void post(long requestGeneration, Runnable action) {
        mainHandler.post(() -> {
            if (!closed.get() && generation.get() == requestGeneration) action.run();
        });
    }

    private void postError(Callback<?> callback, String message) {
        if (!closed.get()) mainHandler.post(() -> callback.onError(message));
    }

    private void release(HttpURLConnection connection) {
        if (connection != null) {
            connections.remove(connection);
            connection.disconnect();
        }
    }
}
