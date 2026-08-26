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

final class DailyMissionApiClient {
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

    DailyMissionApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    DailyMissionApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    /**
     * 打开弹层时由服务端决定 pageList 的第一个页签，客户端不写死页面编码。
     *
     * <p>`/api/v1/missions/page` 是本轮新增的端点；服务端还没上线时回退到既有的目录端点，
     * 取 displayOrder 最小的页面再拉该页，保证客户端先于后端发布时仍能打开。
     */
    void loadFirstPage(String accessToken, ResponseCallback<DailyMissionState> callback) {
        request(
                "GET",
                "/api/v1/missions/page",
                accessToken,
                null,
                new ResponseCallback<>() {
                    @Override public void onSuccess(DailyMissionState result) {
                        callback.onSuccess(result);
                    }

                    @Override public void onUnauthorized() {
                        callback.onUnauthorized();
                    }

                    @Override public void onError(String message) {
                        loadFirstPageFromCatalog(accessToken, callback);
                    }
                },
                text -> DailyMissionState.fromJson(new JSONObject(text)));
    }

    private void loadFirstPageFromCatalog(
            String accessToken, ResponseCallback<DailyMissionState> callback) {
        request(
                "GET",
                "/api/v1/missions",
                accessToken,
                null,
                new ResponseCallback<String>() {
                    @Override public void onSuccess(String pageCode) {
                        if (pageCode.isEmpty()) {
                            callback.onError("任务页面不存在");
                            return;
                        }
                        loadPage(accessToken, pageCode, callback);
                    }

                    @Override public void onUnauthorized() {
                        callback.onUnauthorized();
                    }

                    @Override public void onError(String message) {
                        callback.onError(message);
                    }
                },
                DailyMissionApiClient::firstPageCode);
    }

    private static String firstPageCode(String responseText) throws JSONException {
        org.json.JSONArray pages = new JSONObject(responseText).optJSONArray("pages");
        if (pages == null || pages.length() == 0) return "";
        return pages.getJSONObject(0).optString("pageCode", "");
    }

    void loadPage(
            String accessToken,
            String pageCode,
            ResponseCallback<DailyMissionState> callback) {
        if (!validCode(pageCode)) {
            callback.onError("任务页面不存在");
            return;
        }
        request(
                "GET",
                "/api/v1/missions/pages/" + pageCode,
                accessToken,
                null,
                callback,
                text -> DailyMissionState.fromJson(new JSONObject(text)));
    }

    void claimTask(
            String accessToken,
            String taskCode,
            String idempotencyKey,
            ResponseCallback<DailyMissionState> callback) {
        if (!validCode(taskCode)) {
            callback.onError("任务不存在");
            return;
        }
        request(
                "POST",
                "/api/v1/missions/tasks/" + taskCode + "/claim",
                accessToken,
                idempotencyKey,
                callback,
                text -> DailyMissionState.fromJson(new JSONObject(text)));
    }

    void claimMilestone(
            String accessToken,
            String pageCode,
            long target,
            String idempotencyKey,
            ResponseCallback<DailyMissionState> callback) {
        if (!validCode(pageCode) || target <= 0) {
            callback.onError("阶段奖励不存在");
            return;
        }
        request(
                "POST",
                "/api/v1/missions/pages/" + pageCode + "/milestones/" + target + "/claim",
                accessToken,
                idempotencyKey,
                callback,
                text -> DailyMissionState.fromJson(new JSONObject(text)));
    }

    void close() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void request(
            String method,
            String path,
            String accessToken,
            String idempotencyKey,
            ResponseCallback<T> callback,
            ResponseParser<T> parser) {
        if (baseUrl.isEmpty()) {
            postError(callback, "任务服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            postUnauthorized(callback);
            return;
        }
        executor.execute(() -> execute(
                method, path, accessToken, idempotencyKey, callback, parser));
    }

    private <T> void execute(
            String method,
            String path,
            String accessToken,
            String idempotencyKey,
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
            if ("POST".equals(method)) {
                byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setFixedLengthStreamingMode(body.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body);
                }
            }
            int status = connection.getResponseCode();
            String responseText = readBody(
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED
                    || status == HttpURLConnection.HTTP_FORBIDDEN
                            && responseText.contains("AUTH")) {
                postUnauthorized(callback);
            } else if (status < 200 || status >= 300) {
                postError(callback, parseError(responseText));
            } else {
                postSuccess(callback, parser.parse(responseText));
            }
        } catch (JSONException exception) {
            postError(callback, "任务数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接任务服务，请检查网络后重试");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String readBody(InputStream input) throws IOException {
        if (input == null) return "";
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }
        return body.toString();
    }

    private static String parseError(String responseText) {
        if (!responseText.isBlank()) {
            try {
                JSONObject body = new JSONObject(responseText);
                String detail = body.optString("detail", body.optString("message", ""));
                if (!detail.isBlank()) return detail;
            } catch (JSONException ignored) {
                // Stable public fallback below.
            }
        }
        return "任务请求失败，请稍后重试";
    }

    private static boolean validCode(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{1,64}");
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
