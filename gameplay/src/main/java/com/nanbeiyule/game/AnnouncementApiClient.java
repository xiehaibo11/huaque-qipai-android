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

/** Bearer-authenticated transport for the first-party announcement API. */
final class AnnouncementApiClient {
    interface Callback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    private interface Parser<T> {
        T parse(String responseText) throws JSONException;
    }

    private static final int TIMEOUT_MS = 10_000;
    private static final String LIST_PATH = "/api/v1/announcements";

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    AnnouncementApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    AnnouncementApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    static String listPath() {
        return LIST_PATH;
    }

    static String detailPath(long announcementId) {
        return LIST_PATH + "/" + positiveId(announcementId);
    }

    static String readPath(long announcementId) {
        return detailPath(announcementId) + "/read";
    }

    void loadList(
            String accessToken,
            Callback<AnnouncementApiProtocol.AnnouncementPage> callback) {
        request("GET", listPath(), accessToken, callback, AnnouncementApiProtocol::pageFromJson);
    }

    void loadDetail(
            String accessToken,
            long announcementId,
            Callback<AnnouncementApiProtocol.AnnouncementDetail> callback) {
        request(
                "GET",
                detailPath(announcementId),
                accessToken,
                callback,
                AnnouncementApiProtocol::detailFromJson);
    }

    void markRead(
            String accessToken,
            long announcementId,
            Callback<AnnouncementApiProtocol.ReadReceipt> callback) {
        request(
                "POST",
                readPath(announcementId),
                accessToken,
                callback,
                AnnouncementApiProtocol::readReceiptFromJson);
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void request(
            String method,
            String path,
            String accessToken,
            Callback<T> callback,
            Parser<T> parser) {
        if (baseUrl.isEmpty()) {
            postError(callback, "公告服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            mainHandler.post(callback::onUnauthorized);
            return;
        }
        executor.execute(() -> execute(method, path, accessToken, callback, parser));
    }

    private <T> void execute(
            String method,
            String path,
            String accessToken,
            Callback<T> callback,
            Parser<T> parser) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            if ("POST".equals(method)) {
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(0);
            }
            int status = connection.getResponseCode();
            String body =
                    readBody(
                            status >= 200 && status < 300
                                    ? connection.getInputStream()
                                    : connection.getErrorStream());
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED
                    || (status == HttpURLConnection.HTTP_FORBIDDEN && body.contains("AUTH"))) {
                mainHandler.post(callback::onUnauthorized);
            } else if (status < 200 || status >= 300) {
                postError(callback, errorMessage(body));
            } else {
                T result = parser.parse(body);
                mainHandler.post(() -> callback.onSuccess(result));
            }
        } catch (JSONException exception) {
            postError(callback, "公告数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接公告服务，请检查网络后重试");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static long positiveId(long announcementId) {
        if (announcementId <= 0L) {
            throw new IllegalArgumentException("announcementId must be positive");
        }
        return announcementId;
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

    private static String errorMessage(String body) {
        if (body != null && !body.isBlank()) {
            try {
                String detail = new JSONObject(body).optString("detail", "").trim();
                if (!detail.isEmpty()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // Use the stable user-facing fallback below.
            }
        }
        return "公告请求失败，请稍后重试";
    }

    private void postError(Callback<?> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
