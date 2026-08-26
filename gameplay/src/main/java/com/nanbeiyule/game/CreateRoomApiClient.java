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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONException;
import org.json.JSONObject;

/** Authenticated room boundary; rendering/state never depend on HTTP details. */
final class CreateRoomApiClient {
    enum FailureKind { RETRYABLE, DEFINITIVE }

    /** 服务端「您已经在游戏房间中了」的错误码，等价于原版 {@code ERROR_INAPPID}。 */
    static final String CODE_ROOM_ALREADY_OPEN = "ROOM_ALREADY_OPEN";

    interface ResponseCallback<T> {
        void onSuccess(T value);
        void onUnauthorized();
        void onError(String message, FailureKind failureKind);

        /**
         * 服务端拒绝并给出返场坐标时的分支，对标原版 {@code ERROR_INAPPID} 的「点击确认返场」
         * （{@code lobby/Modules/Gold/Module.lua:308}）。默认退回普通错误提示，只有建房链路需要覆写。
         */
        default void onAlreadyInRoom(RoomPlacement placement, String message) {
            onError(message, FailureKind.DEFINITIVE);
        }
    }

    private interface Parser<T> {
        T parse(String response) throws JSONException;
    }

    private static final int TIMEOUT_MS = 10_000;
    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong requestGeneration = new AtomicLong();
    private final CopyOnWriteArrayList<Future<?>> pending = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<HttpURLConnection> openConnections =
            new CopyOnWriteArrayList<>();

    CreateRoomApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    CreateRoomApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void loadGames(
            String accessToken,
            long lobbyId,
            ResponseCallback<List<CreateRoomGame>> callback) {
        request(
                "GET",
                "/api/v1/rooms/games?lobbyId=" + lobbyId,
                accessToken,
                null,
                null,
                callback,
                CreateRoomApiProtocol::parseGames);
    }

    /**
     * 查询玩家当前所在的未结束房间，对标原版 {@code PlayerData:flushPlayerPosition()}
     * （{@code app/Data/PlayerData.lua:965}）。
     */
    void loadCurrentRoom(String accessToken, ResponseCallback<RoomPlacement> callback) {
        request(
                "GET",
                "/api/v1/rooms/current",
                accessToken,
                null,
                null,
                callback,
                CreateRoomApiProtocol::parsePlacement);
    }

    void loadRuleConfig(
            String accessToken,
            long lobbyId,
            long gameId,
            ResponseCallback<CreateRoomRuleConfig> callback) {
        request(
                "GET",
                "/api/v1/rooms/rule-config?lobbyId=" + lobbyId + "&gameId=" + gameId,
                accessToken,
                null,
                null,
                callback,
                CreateRoomApiProtocol::parseRuleConfig);
    }

    void create(
            String accessToken,
            CreateRoomState state,
            String idempotencyKey,
            ResponseCallback<CreateRoomResult> callback) {
        if (state == null || !state.isCreateReady()) {
            postError(callback, "规则配置尚未通过服务器校验", FailureKind.DEFINITIVE);
            return;
        }
        try {
            request(
                    "POST",
                    "/api/v1/rooms",
                    accessToken,
                    CreateRoomApiProtocol.createBody(
                            state.lobbyId(),
                            state.gameId(),
                            state.categoryIndex(),
                            state.selectedNodeNames()),
                    idempotencyKey,
                    callback,
                    CreateRoomApiProtocol::parseCreateResult);
        } catch (JSONException exception) {
            postError(callback, "创建房间参数不正确", FailureKind.DEFINITIVE);
        }
    }

    void join(
            String accessToken,
            String roomNumber,
            ResponseCallback<CreateRoomResult> callback) {
        if (roomNumber == null || !roomNumber.matches("[0-9]{6}")) {
            postError(callback, "请输入六位房间号", FailureKind.DEFINITIVE);
            return;
        }
        request(
                "POST",
                "/api/v1/rooms/" + roomNumber + "/join",
                accessToken,
                null,
                null,
                callback,
                CreateRoomApiProtocol::parseCreateResult);
    }

    void leave(
            String accessToken,
            String roomNumber,
            ResponseCallback<RoomPlacement> callback) {
        if (roomNumber == null || !roomNumber.matches("[0-9]{6}")) {
            postError(callback, "房间号不正确", FailureKind.DEFINITIVE);
            return;
        }
        request(
                "POST",
                "/api/v1/rooms/" + roomNumber + "/leave",
                accessToken,
                null,
                null,
                callback,
                CreateRoomApiProtocol::parsePlacement);
    }

    void dissolve(
            String accessToken,
            String roomNumber,
            ResponseCallback<CreateRoomResult> callback) {
        if (roomNumber == null || !roomNumber.matches("[0-9]{6}")) {
            postError(callback, "房间号不正确", FailureKind.DEFINITIVE);
            return;
        }
        request(
                "POST",
                "/api/v1/rooms/" + roomNumber + "/dissolve",
                accessToken,
                null,
                null,
                callback,
                CreateRoomApiProtocol::parseCreateResult);
    }

    void shutdown() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        cancelPending();
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    void cancelPending() {
        requestGeneration.incrementAndGet();
        for (HttpURLConnection connection : openConnections) {
            connection.disconnect();
        }
        openConnections.clear();
        for (Future<?> future : pending) {
            future.cancel(true);
        }
        pending.clear();
    }

    private <T> void request(
            String method,
            String path,
            String accessToken,
            JSONObject body,
            String idempotencyKey,
            ResponseCallback<T> callback,
            Parser<T> parser) {
        if (closed.get()) {
            return;
        }
        if (baseUrl.isBlank()) {
            postError(callback, "房间服务地址尚未配置", FailureKind.RETRYABLE);
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            mainHandler.post(callback::onUnauthorized);
            return;
        }
        long generation = requestGeneration.get();
        pruneFinishedRequests();
        try {
            pending.add(
                    executor.submit(
                            () -> execute(
                                    method,
                                    path,
                                    accessToken,
                                    body,
                                    idempotencyKey,
                                    generation,
                                    callback,
                                    parser)));
        } catch (RejectedExecutionException exception) {
            if (!closed.get()) {
                postError(callback, "房间请求暂时无法执行，请稍后重试", FailureKind.RETRYABLE);
            }
        }
    }

    private void pruneFinishedRequests() {
        for (Future<?> future : pending) {
            if (future.isDone()) {
                pending.remove(future);
            }
        }
    }

    private <T> void execute(
            String method,
            String path,
            String accessToken,
            JSONObject body,
            String idempotencyKey,
            long generation,
            ResponseCallback<T> callback,
            Parser<T> parser) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            openConnections.add(connection);
            if (requestGeneration.get() != generation
                    || Thread.currentThread().isInterrupted()) {
                return;
            }
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
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
                postIfCurrent(generation, callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                RoomPlacement placement = alreadyInRoomPlacement(response);
                String message = parseError(response);
                if (placement.hasRoom()) {
                    postIfCurrent(
                            generation, () -> callback.onAlreadyInRoom(placement, message));
                } else {
                    postError(generation, callback, message, failureKindForStatus(status));
                }
            } else {
                T value = parser.parse(response);
                postIfCurrent(generation, () -> callback.onSuccess(value));
            }
        } catch (JSONException exception) {
            postError(
                    generation,
                    callback,
                    "房间数据格式不正确，请稍后重试",
                    FailureKind.RETRYABLE);
        } catch (Exception exception) {
            postError(
                    generation,
                    callback,
                    "无法连接房间服务，请检查网络后重试",
                    FailureKind.RETRYABLE);
        } finally {
            if (connection != null) {
                openConnections.remove(connection);
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
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    /**
     * 只有服务端确实回了 {@code ROOM_ALREADY_OPEN} 且带可用返场坐标时才走返场分支；其余错误保持
     * 原有提示，避免把普通失败误判成「已在房间中」。
     */
    static RoomPlacement alreadyInRoomPlacement(String response) {
        try {
            JSONObject error = new JSONObject(response == null ? "" : response);
            if (!CODE_ROOM_ALREADY_OPEN.equals(error.optString("code"))) {
                return RoomPlacement.none();
            }
        } catch (JSONException ignored) {
            return RoomPlacement.none();
        }
        return CreateRoomApiProtocol.parseErrorPlacement(response);
    }

    private static String parseError(String response) {
        try {
            JSONObject error = new JSONObject(response == null ? "" : response);
            String detail = error.optString("detail");
            return detail.isBlank() ? "房间操作失败，请稍后重试" : detail;
        } catch (JSONException ignored) {
            return "房间操作失败，请稍后重试";
        }
    }

    static FailureKind failureKindForStatus(int status) {
        if (status == HttpURLConnection.HTTP_CLIENT_TIMEOUT
                || status == 425
                || status == 429
                || status >= 500) {
            return FailureKind.RETRYABLE;
        }
        return FailureKind.DEFINITIVE;
    }

    private void postError(
            ResponseCallback<?> callback, String message, FailureKind failureKind) {
        mainHandler.post(() -> callback.onError(message, failureKind));
    }

    private void postError(
            long generation,
            ResponseCallback<?> callback,
            String message,
            FailureKind failureKind) {
        postIfCurrent(generation, () -> callback.onError(message, failureKind));
    }

    private void postIfCurrent(long generation, Runnable action) {
        mainHandler.post(
                () -> {
                    if (requestGeneration.get() == generation) {
                        action.run();
                    }
                });
    }
}
