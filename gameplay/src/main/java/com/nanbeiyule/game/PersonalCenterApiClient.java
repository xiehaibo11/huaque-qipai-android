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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Authenticated first-party client for personal-center state, privacy, and feedback. */
final class PersonalCenterApiClient {
    interface ResultCallback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    interface Callback extends ResultCallback<PersonalCenterState> {}

    interface PrivacyCallback
            extends ResultCallback<PersonalCenterPrivacySettings> {}

    interface FeedbackCallback
            extends ResultCallback<PersonalCenterFeedbackItem> {}

    interface FeedbackHistoryCallback
            extends ResultCallback<List<PersonalCenterFeedbackItem>> {}

    record PhoneCodeResult(long expiresInSeconds) {}

    record PhoneBindingResult(
            String maskedPhone, boolean reloginRequired) {
        static PhoneBindingResult fromJson(JSONObject body)
                throws JSONException {
            return new PhoneBindingResult(
                    body.getString("maskedPhone"),
                    body.optBoolean("reloginRequired", false));
        }
    }

    @FunctionalInterface
    private interface ResponseParser<T> {
        T parse(String responseText) throws JSONException;
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final String baseUrl;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    PersonalCenterApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    PersonalCenterApiClient(String baseUrl) {
        this.baseUrl =
                baseUrl == null
                        ? ""
                        : baseUrl.trim().replaceAll("/+$", "");
    }

    void load(String accessToken, Callback callback) {
        request(
                "GET",
                "/api/v1/personal-center",
                accessToken,
                null,
                responseText ->
                        PersonalCenterState.fromJson(
                                new JSONObject(responseText)),
                callback);
    }

    void loadPrivacy(
            String accessToken, PrivacyCallback callback) {
        request(
                "GET",
                "/api/v1/personal-center/privacy",
                accessToken,
                null,
                responseText ->
                        PersonalCenterPrivacySettings.fromJson(
                                new JSONObject(responseText)),
                callback);
    }

    void updatePrivacy(
            String accessToken,
            PersonalCenterPrivacySettings settings,
            PrivacyCallback callback) {
        if (settings == null) {
            callback.onError("隐私设置无效");
            return;
        }
        try {
            request(
                    "PUT",
                    "/api/v1/personal-center/privacy",
                    accessToken,
                    settings.toJson(),
                    responseText ->
                            PersonalCenterPrivacySettings.fromJson(
                                    new JSONObject(responseText)),
                    callback);
        } catch (JSONException exception) {
            callback.onError("隐私设置格式不正确");
        }
    }

    void submitFeedback(
            String accessToken,
            PersonalCenterFeedbackItem.Category category,
            String content,
            FeedbackCallback callback) {
        String normalized = content == null ? "" : content.trim();
        if (category == null
                || normalized.isEmpty()
                || normalized.length() > 500) {
            callback.onError("反馈内容须为1至500个字符");
            return;
        }
        try {
            JSONObject body =
                    new JSONObject()
                            .put("category", category.name())
                            .put("content", normalized);
            request(
                    "POST",
                    "/api/v1/personal-center/feedback",
                    accessToken,
                    body,
                    responseText ->
                            PersonalCenterFeedbackItem.fromJson(
                                    new JSONObject(responseText)),
                    callback);
        } catch (JSONException exception) {
            callback.onError("反馈内容格式不正确");
        }
    }

    void loadFeedbackHistory(
            String accessToken, FeedbackHistoryCallback callback) {
        request(
                "GET",
                "/api/v1/personal-center/feedback",
                accessToken,
                null,
                responseText ->
                        PersonalCenterFeedbackItem.fromJson(
                                new JSONArray(responseText)),
                callback);
    }

    void requestPhoneCode(
            String accessToken,
            String rawPhoneNumber,
            ResultCallback<PhoneCodeResult> callback) {
        try {
            PersonalCenterPhoneForm form =
                    PersonalCenterPhoneForm.phoneOnly(rawPhoneNumber);
            request(
                    "POST",
                    "/api/v1/personal-center/phone/code",
                    accessToken,
                    new JSONObject().put("phoneNumber", form.phoneNumber()),
                    responseText -> {
                        JSONObject body = new JSONObject(responseText);
                        return new PhoneCodeResult(
                                body.getLong("expiresInSeconds"));
                    },
                    callback);
        } catch (IllegalArgumentException | JSONException exception) {
            callback.onError(exception.getMessage());
        }
    }

    void bindPhone(
            String accessToken,
            String rawPhoneNumber,
            String rawCode,
            ResultCallback<PhoneBindingResult> callback) {
        try {
            PersonalCenterPhoneForm form =
                    PersonalCenterPhoneForm.validated(
                            rawPhoneNumber, rawCode);
            request(
                    "PUT",
                    "/api/v1/personal-center/phone",
                    accessToken,
                    new JSONObject()
                            .put("phoneNumber", form.phoneNumber())
                            .put("code", form.code()),
                    responseText -> {
                        JSONObject body = new JSONObject(responseText);
                        return PhoneBindingResult.fromJson(body);
                    },
                    callback);
        } catch (IllegalArgumentException | JSONException exception) {
            callback.onError(exception.getMessage());
        }
    }

    void deactivateAccount(
            String accessToken, ResultCallback<Boolean> callback) {
        try {
            request(
                    "DELETE",
                    "/api/v1/personal-center/account",
                    accessToken,
                    new JSONObject().put("confirmation", "注销账号"),
                    responseText -> true,
                    callback);
        } catch (JSONException exception) {
            callback.onError("账号注销请求格式不正确");
        }
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private <T> void request(
            String method,
            String path,
            String accessToken,
            JSONObject requestBody,
            ResponseParser<T> parser,
            ResultCallback<T> callback) {
        if (baseUrl.isEmpty()) {
            callback.onError("个人中心服务地址尚未配置");
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
            postError(
                    callback,
                    "个人中心数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(
                    callback,
                    "无法连接个人中心服务，请检查网络后重试");
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
                String detail =
                        new JSONObject(responseText)
                                .optString("detail");
                if (!detail.isBlank()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // Fall through to the stable public message.
            }
        }
        return "个人中心请求失败，请稍后重试";
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
