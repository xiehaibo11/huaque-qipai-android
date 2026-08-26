package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import com.nanbeiyule.game.goldroom.GoldRoomConf;
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

/** Transport-only client for the first-party gold-room catalog API. */
final class GoldRoomApiClient {
    interface ResponseCallback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    GoldRoomApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    GoldRoomApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void loadConf(
            String accessToken,
            long lobbyId,
            long gameId,
            ResponseCallback<GoldRoomConf> callback) {
        if (baseUrl.isEmpty()) {
            postError(callback, "金币场服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            mainHandler.post(callback::onUnauthorized);
            return;
        }
        String path = "/api/v1/gold-rooms/games/" + gameId + "?lobbyId=" + lobbyId;
        executor.execute(() -> execute(path, accessToken, callback));
    }

    void join(
            String accessToken,
            long lobbyId,
            long gameId,
            int roomNameFlag,
            String idempotencyKey,
            ResponseCallback<GoldRoomJoinResponse> callback) {
        if (baseUrl.isEmpty()) {
            postError(callback, "金币场服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            mainHandler.post(callback::onUnauthorized);
            return;
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            postError(callback, "金币场进房请求不正确");
            return;
        }
        String path = "/api/v1/gold-rooms/games/" + gameId + "/join";
        executor.execute(
                () -> executeJoin(path, accessToken, lobbyId, roomNameFlag, idempotencyKey, callback));
    }

    /**
     * Fire-and-forget match cancel for the original PlayerLeaveRequest endpoint: the stock Lua
     * client never wired it, but our four-real-player fill model requires the server to release
     * the seat, so errors are surfaced as success and never block the back navigation.
     */
    void leave(
            String accessToken,
            long lobbyId,
            long gameId,
            int roomNameFlag,
            ResponseCallback<Void> callback) {
        if (baseUrl.isEmpty() || accessToken == null || accessToken.isBlank()) {
            return;
        }
        String path = "/api/v1/gold-rooms/games/" + gameId + "/leave";
        executor.execute(
                () -> executeLeave(path, accessToken, lobbyId, roomNameFlag, callback));
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void execute(
            String path, String accessToken, ResponseCallback<GoldRoomConf> callback) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            int statusCode = connection.getResponseCode();
            String responseText =
                    readBody(
                            statusCode >= 200 && statusCode < 300
                                    ? connection.getInputStream()
                                    : connection.getErrorStream());
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
                    || (statusCode == HttpURLConnection.HTTP_FORBIDDEN
                            && responseText.contains("AUTH"))) {
                mainHandler.post(callback::onUnauthorized);
            } else if (statusCode < 200 || statusCode >= 300) {
                postError(callback, parseError(responseText));
            } else {
                GoldRoomConf conf =
                        GoldRoomApiProtocol.confFromJson(new JSONObject(responseText));
                mainHandler.post(() -> callback.onSuccess(conf));
            }
        } catch (JSONException exception) {
            postError(callback, "金币场数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接金币场服务，请检查网络后重试");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void executeJoin(
            String path,
            String accessToken,
            long lobbyId,
            int roomNameFlag,
            String idempotencyKey,
            ResponseCallback<GoldRoomJoinResponse> callback) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Idempotency-Key", idempotencyKey);
            writeBody(connection, GoldRoomApiProtocol.joinBody(lobbyId, roomNameFlag));
            int statusCode = connection.getResponseCode();
            String responseText =
                    readBody(
                            statusCode >= 200 && statusCode < 300
                                    ? connection.getInputStream()
                                    : connection.getErrorStream());
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
                    || (statusCode == HttpURLConnection.HTTP_FORBIDDEN
                            && responseText.contains("AUTH"))) {
                mainHandler.post(callback::onUnauthorized);
            } else if (statusCode < 200 || statusCode >= 300) {
                postError(callback, parseError(responseText));
            } else {
                GoldRoomJoinResponse response =
                        GoldRoomApiProtocol.joinFromJson(new JSONObject(responseText));
                mainHandler.post(() -> callback.onSuccess(response));
            }
        } catch (JSONException exception) {
            postError(callback, "金币场匹配数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接金币场服务，请检查网络后重试");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void executeLeave(
            String path,
            String accessToken,
            long lobbyId,
            int roomNameFlag,
            ResponseCallback<Void> callback) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            writeBody(connection, GoldRoomApiProtocol.leaveBody(lobbyId, roomNameFlag));
            connection.getResponseCode();
            mainHandler.post(() -> callback.onSuccess(null));
        } catch (Exception exception) {
            // 取消匹配是尽力而为：失败不重试、不提示，占位由服务端回归逻辑与重开房兼锁。
            mainHandler.post(() -> callback.onSuccess(null));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void writeBody(HttpURLConnection connection, JSONObject body)
            throws IOException {
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

    private static String parseError(String responseText) {
        if (responseText != null && !responseText.isBlank()) {
            try {
                String detail = new JSONObject(responseText).optString("detail");
                if (!detail.isBlank()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // Fall through to a stable user-facing message.
            }
        }
        return "获取房间信息失败，请稍后重试";
    }

    private void postError(ResponseCallback<?> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
