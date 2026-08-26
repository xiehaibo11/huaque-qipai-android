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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

/** Authenticated first-party client for the real-name verification API. */
final class RealNameApiClient {
    interface ResultCallback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    interface Callback extends ResultCallback<RealNameStatus> {}

    @FunctionalInterface
    private interface ResponseParser<T> {
        T parse(String responseText) throws JSONException;
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private static final Map<String, String> ERROR_MESSAGES =
            Map.of(
                    "REALNAME_INVALID_FORMAT", "姓名或身份证号格式不正确",
                    "REALNAME_UNDERAGE", "未满十八周岁，无法完成实名认证",
                    "REALNAME_MISMATCH", "姓名与身份证号不一致，请核对后重试",
                    "REALNAME_ALREADY_BOUND", "该身份信息已被其他账号使用",
                    "REALNAME_ALREADY_VERIFIED", "该账号已完成实名认证，不可更换实名信息",
                    "REALNAME_RATE_LIMITED", "认证请求过于频繁，请稍后重试",
                    "REALNAME_UNAVAILABLE", "实名认证服务暂不可用，请稍后重试");

    private final String baseUrl;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    RealNameApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    RealNameApiClient(String baseUrl) {
        this.baseUrl =
                baseUrl == null
                        ? ""
                        : baseUrl.trim().replaceAll("/+$", "");
    }

    void getStatus(String accessToken, Callback callback) {
        request(
                "GET",
                "/api/v1/real-name/status",
                accessToken,
                null,
                callback);
    }

    void verify(
            String accessToken,
            String realName,
            String idCardNumber,
            Callback callback) {
        try {
            request(
                    "POST",
                    "/api/v1/real-name/verify",
                    accessToken,
                    RealNameApiProtocol.verifyBody(
                            realName, idCardNumber),
                    callback);
        } catch (JSONException exception) {
            callback.onError("实名信息格式不正确");
        }
    }

    void verifyAlipay(
            String accessToken, String authCode, Callback callback) {
        try {
            request(
                    "POST",
                    "/api/v1/real-name/alipay/verify",
                    accessToken,
                    RealNameApiProtocol.alipayVerifyBody(authCode),
                    callback);
        } catch (JSONException exception) {
            callback.onError("支付宝授权信息格式不正确");
        }
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void request(
            String method,
            String path,
            String accessToken,
            JSONObject requestBody,
            Callback callback) {
        request(
                method,
                path,
                accessToken,
                requestBody,
                responseText ->
                        RealNameStatus.fromJson(
                                new JSONObject(responseText)),
                callback);
    }

    private <T> void request(
            String method,
            String path,
            String accessToken,
            JSONObject requestBody,
            ResponseParser<T> parser,
            ResultCallback<T> callback) {
        if (baseUrl.isEmpty()) {
            callback.onError("实名认证服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            callback.onUnauthorized();
            return;
        }
        executor.execute(
                () ->
                        execute(
                                method,
                                path,
                                accessToken,
                                requestBody,
                                parser,
                                callback));
    }

    private <T> void execute(
            String method,
            String path,
            String accessToken,
            JSONObject requestBody,
            ResponseParser<T> parser,
            ResultCallback<T> callback) {
        HttpURLConnection connection = null;
        try {
            connection =
                    (HttpURLConnection)
                            new URL(baseUrl + path).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty(
                    "Authorization", "Bearer " + accessToken);
            if (requestBody != null) {
                byte[] bytes =
                        requestBody.toString()
                                .getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type", "application/json; charset=utf-8");
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }
            }

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
            postSuccess(callback, parser.parse(responseText));
        } catch (JSONException exception) {
            postError(callback, "实名认证数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接实名认证服务，请检查网络后重试");
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
                new BufferedReader(
                        new InputStreamReader(
                                input, StandardCharsets.UTF_8))) {
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
                JSONObject body = new JSONObject(responseText);
                String mapped = ERROR_MESSAGES.get(body.optString("code"));
                if (mapped != null) {
                    return mapped;
                }
                String detail = body.optString("detail");
                if (!detail.isBlank()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // Fall through to the stable public message.
            }
        }
        return "实名认证请求失败，请稍后重试";
    }

    private <T> void postSuccess(
            ResultCallback<T> callback, T result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postUnauthorized(ResultCallback<?> callback) {
        mainHandler.post(callback::onUnauthorized);
    }

    private void postError(
            ResultCallback<?> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
