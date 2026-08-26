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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

/** Transport-only client for the first-party mail REST API. */
final class MailApiClient {
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

    MailApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    MailApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void loadSummary(
            String accessToken, ResponseCallback<MailApiProtocol.MailSummary> callback) {
        request(
                "GET",
                "/api/v1/mails/summary",
                accessToken,
                null,
                callback,
                responseText ->
                        MailApiProtocol.MailSummary.fromJson(new JSONObject(responseText)));
    }

    void loadMails(
            String accessToken,
            int page,
            ResponseCallback<MailApiProtocol.MailPage> callback) {
        request(
                "GET",
                "/api/v1/mails?page=" + Math.max(1, page),
                accessToken,
                null,
                callback,
                MailApiProtocol::mailPageFromJson);
    }

    void loadDetail(
            String accessToken,
            String mailId,
            ResponseCallback<MailApiProtocol.MailDetail> callback) {
        if (mailId == null || mailId.isBlank()) {
            postError(callback, "邮件不存在");
            return;
        }
        String encodedMailId;
        try {
            encodedMailId = URLEncoder.encode(mailId, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException exception) {
            postError(callback, "邮件参数不正确");
            return;
        }
        request(
                "GET",
                "/api/v1/mails/" + encodedMailId,
                accessToken,
                null,
                callback,
                responseText ->
                        MailApiProtocol.MailDetail.fromJson(new JSONObject(responseText)));
    }

    void readAll(
            String accessToken,
            ResponseCallback<MailApiProtocol.MailMarkedCount> callback) {
        request(
                "POST",
                "/api/v1/mails/read-all",
                accessToken,
                new JSONObject(),
                callback,
                responseText ->
                        MailApiProtocol.MailMarkedCount.fromJson(
                                new JSONObject(responseText)));
    }

    void delete(
            String accessToken,
            List<String> mailIds,
            ResponseCallback<MailApiProtocol.MailDeletedCount> callback) {
        postWithMailIds(
                accessToken,
                "/api/v1/mails/delete",
                mailIds,
                callback,
                responseText ->
                        MailApiProtocol.MailDeletedCount.fromJson(
                                new JSONObject(responseText)));
    }

    void claim(
            String accessToken,
            List<String> mailIds,
            ResponseCallback<MailApiProtocol.MailClaimResult> callback) {
        postWithMailIds(
                accessToken,
                "/api/v1/mails/claim",
                mailIds,
                callback,
                responseText ->
                        MailApiProtocol.MailClaimResult.fromJson(
                                new JSONObject(responseText)));
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void postWithMailIds(
            String accessToken,
            String path,
            List<String> mailIds,
            ResponseCallback<T> callback,
            ResponseParser<T> parser) {
        if (mailIds == null || mailIds.isEmpty()) {
            postError(callback, "请先选择邮件");
            return;
        }
        try {
            request(
                    "POST",
                    path,
                    accessToken,
                    new JSONObject()
                            .put("mailIds", MailApiProtocol.mailIdsBody(mailIds)),
                    callback,
                    parser);
        } catch (JSONException exception) {
            postError(callback, "邮件参数不正确");
        }
    }

    private <T> void request(
            String method,
            String path,
            String accessToken,
            JSONObject requestBody,
            ResponseCallback<T> callback,
            ResponseParser<T> parser) {
        if (baseUrl.isEmpty()) {
            postError(callback, "邮件服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            postUnauthorized(callback);
            return;
        }
        executor.execute(
                () -> execute(method, path, accessToken, requestBody, callback, parser));
    }

    private <T> void execute(
            String method,
            String path,
            String accessToken,
            JSONObject requestBody,
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
            writeBody(connection, requestBody);
            int statusCode = connection.getResponseCode();
            String responseText =
                    readBody(
                            statusCode >= 200 && statusCode < 300
                                    ? connection.getInputStream()
                                    : connection.getErrorStream());
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
                    || (statusCode == HttpURLConnection.HTTP_FORBIDDEN
                            && responseText.contains("AUTH"))) {
                postUnauthorized(callback);
            } else if (statusCode < 200 || statusCode >= 300) {
                postError(callback, parseError(responseText));
            } else {
                postSuccess(callback, parser.parse(responseText));
            }
        } catch (JSONException exception) {
            postError(callback, "邮件数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接邮件服务，请检查网络后重试");
        } finally {
            if (connection != null) {
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
        return "邮件请求失败，请稍后重试";
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
