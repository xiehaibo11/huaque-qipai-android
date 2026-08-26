package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

/** Loads the authenticated home snapshot without client-synthesized business data. */
public final class GameHomeApiClient {
    public interface Callback {
        void onSuccess(GameHomeState state);

        void onUnauthorized();

        void onError(String message);
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public GameHomeApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    public GameHomeApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    public void loadHome(String accessToken, Callback callback) {
        if (baseUrl.isEmpty()) {
            callback.onError("首页服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            callback.onUnauthorized();
            return;
        }
        executor.execute(
                () -> {
                    HttpURLConnection connection = null;
                    try {
                        connection =
                                (HttpURLConnection)
                                        new URL(baseUrl + "/api/v1/home").openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                        connection.setReadTimeout(READ_TIMEOUT_MS);
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setRequestProperty(
                                "Authorization", "Bearer " + accessToken);

                        int statusCode = connection.getResponseCode();
                        String responseText =
                                readBody(
                                        statusCode >= 200 && statusCode < 300
                                                ? connection.getInputStream()
                                                : connection.getErrorStream());
                        if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
                                || statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                            postUnauthorized(callback);
                            return;
                        }
                        if (statusCode < 200 || statusCode >= 300) {
                            postError(callback, parseError(responseText));
                            return;
                        }
                        postSuccess(
                                callback,
                                GameHomeState.fromJson(new JSONObject(responseText)));
                    } catch (JSONException exception) {
                        postError(callback, "首页数据格式不正确，请稍后重试");
                    } catch (Exception exception) {
                        postError(callback, "无法连接首页服务，请检查网络后重试");
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                });
    }

    public void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private static String readBody(InputStream input) throws IOException {
        if (input == null) {
            return "";
        }
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(input, StandardCharsets.UTF_8))) {
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
                String detail = new JSONObject(responseText).optString("detail");
                if (!detail.isBlank()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // Fall through to the stable public message.
            }
        }
        return "首页请求失败，请稍后重试";
    }

    private void postSuccess(Callback callback, GameHomeState state) {
        mainHandler.post(() -> callback.onSuccess(state));
    }

    private void postUnauthorized(Callback callback) {
        mainHandler.post(callback::onUnauthorized);
    }

    private void postError(Callback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
