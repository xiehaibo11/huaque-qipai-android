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
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Bearer-authenticated transport for score-ledger commands and queries. */
final class ScoreAssistantApiClient {
    interface Callback<T> {
        void onSuccess(T result);
        void onUnauthorized();
        void onError(String message);
    }

    private interface Parser<T> {
        T parse(String text) throws JSONException;
    }

    private static final String ROOT = "/api/v1/score-ledgers";
    private static final int TIMEOUT_MS = 10_000;
    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    ScoreAssistantApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    ScoreAssistantApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    static String createPath() { return ROOT; }
    static String inProgressPath() { return ROOT + "/in-progress"; }
    static String historyPath(int page) {
        if (page < 1) throw new IllegalArgumentException("page must be one-based");
        return ROOT + "/history?page=" + page + "&pageSize=10";
    }
    static String detailPath(UUID id) { return ROOT + "/" + requiredId(id); }
    static String roundPath(UUID id) { return detailPath(id) + "/rounds"; }
    static String endPath(UUID id) { return detailPath(id) + "/end"; }
    static String favoritePath(UUID id) { return detailPath(id) + "/favorite"; }
    static String monthlyPath(String month) {
        String normalized = YearMonth.parse(month).toString();
        return ROOT + "/statistics/monthly?month=" + normalized;
    }

    void loadInProgress(String token, Callback<List<ScoreAssistantApiProtocol.LedgerSummary>> callback) {
        request("GET", inProgressPath(), token, null, callback,
                ScoreAssistantApiProtocol::inProgressFromJson);
    }

    void create(
            String token,
            List<ScoreAssistantInputValidator.PlayerDraft> players,
            Callback<ScoreAssistantApiProtocol.LedgerDetail> callback) {
        JSONArray array = new JSONArray();
        for (ScoreAssistantInputValidator.PlayerDraft player : players) {
            JSONObject item = new JSONObject();
            put(item, "name", player.name());
            put(item, "ownerPlayer", player.ownerPlayer());
            array.put(item);
        }
        JSONObject body = new JSONObject();
        put(body, "players", array);
        request("POST", createPath(), token, body.toString(), callback,
                ScoreAssistantApiProtocol::detailFromJson);
    }

    void addRound(
            String token,
            UUID ledgerId,
            List<ScoreAssistantInputValidator.ScoreDelta> scores,
            Callback<ScoreAssistantApiProtocol.RoundResult> callback) {
        JSONArray array = new JSONArray();
        for (ScoreAssistantInputValidator.ScoreDelta score : scores) {
            JSONObject item = new JSONObject();
            put(item, "playerId", score.playerId().toString());
            put(item, "scoreDelta", score.scoreDelta());
            array.put(item);
        }
        JSONObject body = new JSONObject();
        put(body, "scores", array);
        request("POST", roundPath(ledgerId), token, body.toString(), callback,
                ScoreAssistantApiProtocol::roundFromJson);
    }

    void end(String token, UUID ledgerId, Callback<ScoreAssistantApiProtocol.LedgerState> callback) {
        request("POST", endPath(ledgerId), token, "", callback,
                ScoreAssistantApiProtocol::stateFromJson);
    }

    void setFavorite(
            String token,
            UUID ledgerId,
            boolean favorite,
            Callback<ScoreAssistantApiProtocol.LedgerState> callback) {
        JSONObject body = new JSONObject();
        put(body, "favorite", favorite);
        request("PUT", favoritePath(ledgerId), token, body.toString(), callback,
                ScoreAssistantApiProtocol::stateFromJson);
    }

    void delete(String token, UUID ledgerId, Callback<ScoreAssistantApiProtocol.DeleteReceipt> callback) {
        request("DELETE", detailPath(ledgerId), token, null, callback,
                ScoreAssistantApiProtocol::deleteFromJson);
    }

    void loadHistory(
            String token,
            int page,
            Callback<ScoreAssistantApiProtocol.HistoryPage> callback) {
        request("GET", historyPath(page), token, null, callback,
                ScoreAssistantApiProtocol::historyFromJson);
    }

    void loadDetail(
            String token,
            UUID ledgerId,
            Callback<ScoreAssistantApiProtocol.LedgerDetail> callback) {
        request("GET", detailPath(ledgerId), token, null, callback,
                ScoreAssistantApiProtocol::detailFromJson);
    }

    void loadMonthly(
            String token,
            YearMonth month,
            Callback<ScoreAssistantApiProtocol.MonthlyStatistics> callback) {
        request("GET", monthlyPath(month.toString()), token, null, callback,
                ScoreAssistantApiProtocol::monthlyFromJson);
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void request(
            String method,
            String path,
            String token,
            String body,
            Callback<T> callback,
            Parser<T> parser) {
        if (baseUrl.isEmpty()) {
            postError(callback, "计分服务地址尚未配置");
        } else if (token == null || token.isBlank()) {
            postError(callback, "登录状态不可用，请重新登录");
        } else {
            executor.execute(() -> execute(method, path, token, body, callback, parser));
        }
    }

    private <T> void execute(
            String method,
            String path,
            String token,
            String requestBody,
            Callback<T> callback,
            Parser<T> parser) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            if (requestBody != null) writeBody(connection, requestBody);
            int status = connection.getResponseCode();
            String response = readBody(status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                mainHandler.post(callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                postError(callback, errorMessage(response));
            } else {
                T value = parser.parse(response);
                mainHandler.post(() -> callback.onSuccess(value));
            }
        } catch (JSONException exception) {
            postError(callback, "计分服务返回了无法识别的数据");
        } catch (Exception exception) {
            postError(callback, "无法连接计分服务，请检查网络后重试");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static void writeBody(HttpURLConnection connection, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(bytes);
        }
    }

    private static String readBody(InputStream input) throws IOException {
        if (input == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }

    private static String errorMessage(String body) {
        if (body != null && !body.isBlank()) {
            try {
                JSONObject source = new JSONObject(body);
                String detail = source.optString("detail", "").trim();
                if (!detail.isEmpty()) return detail;
                String message = source.optString("message", "").trim();
                if (!message.isEmpty()) return message;
            } catch (JSONException ignored) {
                // Use the stable user-facing fallback.
            }
        }
        return "计分请求失败，请稍后重试";
    }

    private static UUID requiredId(UUID id) {
        if (id == null) throw new IllegalArgumentException("ledgerId must not be null");
        return id;
    }

    private static void put(JSONObject target, String key, Object value) {
        try {
            target.put(key, value);
        } catch (JSONException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void postError(Callback<?> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
