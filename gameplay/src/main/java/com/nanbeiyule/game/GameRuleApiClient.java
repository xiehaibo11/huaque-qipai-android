package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import com.nanbeiyule.game.goldroom.GoldHallGameRuleDocument;
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

/**
 * Transport-only client for the first-party game-rule document API.
 *
 * <p>原版规则正文由 {@code RuleView:updateRuleWebView} 的 WebView 从
 * {@code UrlConf.GAME_RULE_HTML_ADDR}（浙江服务器）加载。本项目不请求原版服务，正文改由
 * 南北娱乐后端 {@code GET /api/v1/game-rules/{gameId}} 下发。
 */
final class GameRuleApiClient {
    interface ResponseCallback {
        void onSuccess(GoldHallGameRuleDocument document);

        void onUnauthorized();

        void onError(String message);
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    GameRuleApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    GameRuleApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void loadDocument(String accessToken, long gameId, ResponseCallback callback) {
        if (baseUrl.isEmpty()) {
            postError(callback, "规则服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            mainHandler.post(callback::onUnauthorized);
            return;
        }
        String path = "/api/v1/game-rules/" + gameId;
        executor.execute(() -> execute(path, accessToken, callback));
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void execute(String path, String accessToken, ResponseCallback callback) {
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
                GoldHallGameRuleDocument document =
                        GameRuleApiProtocol.documentFromJson(new JSONObject(responseText));
                mainHandler.post(() -> callback.onSuccess(document));
            }
        } catch (JSONException exception) {
            postError(callback, "规则数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接规则服务，请检查网络后重试");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
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
        return "获取规则失败，请稍后重试";
    }

    private void postError(ResponseCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
