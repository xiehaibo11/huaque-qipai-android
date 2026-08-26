package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

public final class GameRecordApiClient {
    public interface Callback {
        void onSuccess(GameRecordPage page);

        void onUnauthorized();

        void onError(String message);
    }

    private static final int TIMEOUT_MS = 10_000;
    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public GameRecordApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    public GameRecordApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    public void load(
            String accessToken,
            String date,
            long gameId,
            boolean gold,
            Callback callback) {
        if (baseUrl.isBlank()) {
            callback.onError("战绩服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            callback.onUnauthorized();
            return;
        }
        executor.execute(() -> request(accessToken, date, gameId, gold, callback));
    }

    public void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void request(
            String token,
            String date,
            long gameId,
            boolean gold,
            Callback callback) {
        HttpURLConnection connection = null;
        try {
            String query = "?date=" + encode(date)
                    + "&gameId=" + Math.max(0L, gameId)
                    + "&mode=" + (gold ? "GOLD" : "BATTLE");
            connection = (HttpURLConnection)
                    new URL(baseUrl + "/api/v1/game-records" + query).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            int status = connection.getResponseCode();
            String body = readBody(status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream());
            if (status == 401 || status == 403) {
                mainHandler.post(callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                postError(callback, errorMessage(body));
            } else {
                GameRecordPage page = GameRecordPage.fromJson(new JSONObject(body));
                mainHandler.post(() -> callback.onSuccess(page));
            }
        } catch (JSONException exception) {
            postError(callback, "战绩数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接战绩服务，请检查网络后重试");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(
                    value == null ? "" : value, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException exception) {
            throw new IllegalStateException("UTF-8 charset unavailable", exception);
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

    private static String errorMessage(String body) {
        try {
            String detail = new JSONObject(body).optString("detail");
            if (!detail.isBlank()) return detail;
        } catch (JSONException ignored) {
            // Use the stable public message below.
        }
        return "战绩请求失败，请稍后重试";
    }

    private void postError(Callback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
