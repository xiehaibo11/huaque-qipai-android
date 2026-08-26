package com.nanbeiyule.game;

import android.os.Handler;
import android.os.Looper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

final class AvatarApiClient {
    interface Callback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    record UploadResult(
            String avatarKey,
            String contentType,
            String sha256,
            int width,
            int height) {}

    record DownloadResult(byte[] bytes, String contentType, String etag, boolean notModified) {
        DownloadResult {
            bytes = bytes == null ? new byte[0] : bytes.clone();
        }

        @Override
        public byte[] bytes() {
            return bytes.clone();
        }

        static DownloadResult notModified(String etag) {
            return new DownloadResult(new byte[0], "", etag == null ? "" : etag, true);
        }
    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;
    private static final int MAX_DOWNLOAD_BYTES = 2 * 1024 * 1024;

    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    AvatarApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    AvatarApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void upload(byte[] jpeg, String accessToken, Callback<UploadResult> callback) {
        if (!canRequest(accessToken, callback)) {
            return;
        }
        byte[] safeJpeg = jpeg == null ? new byte[0] : jpeg.clone();
        if (safeJpeg.length == 0 || safeJpeg.length > 8 * 1024 * 1024) {
            callback.onError("头像文件不符合要求");
            return;
        }
        executor.execute(
                () -> {
                    HttpURLConnection connection = null;
                    try {
                        String boundary = "NanbeiAvatar" + UUID.randomUUID().toString().replace("-", "");
                        byte[] body = AvatarApiProtocol.multipartBody(boundary, safeJpeg);
                        connection =
                                open(
                                        "/api/v1/profile/avatar",
                                        "PUT",
                                        accessToken);
                        connection.setRequestProperty(
                                "Content-Type",
                                "multipart/form-data; boundary=" + boundary);
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setDoOutput(true);
                        connection.setFixedLengthStreamingMode(body.length);
                        connection.getOutputStream().write(body);
                        int status = connection.getResponseCode();
                        byte[] response =
                                readLimited(
                                        responseStream(connection, status),
                                        MAX_DOWNLOAD_BYTES);
                        if (isUnauthorized(status)) {
                            postUnauthorized(callback);
                            return;
                        }
                        if (status < 200 || status >= 300) {
                            postError(callback, parseError(response, "头像保存失败，请稍后重试"));
                            return;
                        }
                        postSuccess(callback, parseUpload(response));
                    } catch (Exception exception) {
                        postError(callback, "无法连接头像服务，请检查网络后重试");
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                });
    }

    void download(
            String avatarKey,
            String accessToken,
            String etag,
            Callback<DownloadResult> callback) {
        if (!canRequest(accessToken, callback)) {
            return;
        }
        if (avatarKey == null || !avatarKey.matches("avatar_[0-9a-fA-F-]{36}")) {
            callback.onError("头像地址不正确");
            return;
        }
        executor.execute(
                () -> {
                    HttpURLConnection connection = null;
                    try {
                        connection =
                                open(
                                        "/api/v1/avatars/" + avatarKey,
                                        "GET",
                                        accessToken);
                        connection.setRequestProperty("Accept", "image/jpeg");
                        if (etag != null && !etag.isBlank()) {
                            connection.setRequestProperty("If-None-Match", etag);
                        }
                        int status = connection.getResponseCode();
                        if (isUnauthorized(status)) {
                            postUnauthorized(callback);
                            return;
                        }
                        if (status == HttpURLConnection.HTTP_NOT_MODIFIED) {
                            postSuccess(
                                    callback,
                                    DownloadResult.notModified(
                                            connection.getHeaderField("ETag")));
                            return;
                        }
                        byte[] response =
                                readLimited(
                                        responseStream(connection, status),
                                        MAX_DOWNLOAD_BYTES);
                        if (status < 200 || status >= 300) {
                            postError(callback, parseError(response, "头像读取失败"));
                            return;
                        }
                        String contentType = connection.getContentType();
                        if (contentType == null || !contentType.startsWith("image/")) {
                            throw new IOException("Avatar response is not an image");
                        }
                        String responseEtag = connection.getHeaderField("ETag");
                        if (responseEtag == null || responseEtag.isBlank()) {
                            throw new IOException("Avatar response is missing ETag");
                        }
                        postSuccess(
                                callback,
                                new DownloadResult(
                                        response,
                                        contentType,
                                        responseEtag,
                                        false));
                    } catch (Exception exception) {
                        postError(callback, "无法连接头像服务，请检查网络后重试");
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                });
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private HttpURLConnection open(String path, String method, String token) throws IOException {
        HttpURLConnection connection =
                (HttpURLConnection) new URL(baseUrl + path).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setUseCaches(false);
        return connection;
    }

    private <T> boolean canRequest(String token, Callback<T> callback) {
        if (baseUrl.isEmpty()) {
            callback.onError("头像服务地址尚未配置");
            return false;
        }
        if (token == null || token.isBlank()) {
            callback.onUnauthorized();
            return false;
        }
        return true;
    }

    private static boolean isUnauthorized(int status) {
        return status == HttpURLConnection.HTTP_UNAUTHORIZED
                || status == HttpURLConnection.HTTP_FORBIDDEN;
    }

    private static InputStream responseStream(HttpURLConnection connection, int status)
            throws IOException {
        return status >= 200 && status < 400
                ? connection.getInputStream()
                : connection.getErrorStream();
    }

    private static byte[] readLimited(InputStream input, int maxBytes) throws IOException {
        if (input == null) {
            return new byte[0];
        }
        try (InputStream closeable = input;
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while ((read = closeable.read(buffer)) != -1) {
                total += read;
                if (total > maxBytes) {
                    throw new IOException("Response exceeds size limit");
                }
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static UploadResult parseUpload(byte[] response) throws JSONException {
        JSONObject body =
                new JSONObject(new String(response, StandardCharsets.UTF_8));
        return new UploadResult(
                body.getString("avatarKey"),
                body.getString("contentType"),
                body.getString("sha256"),
                body.getInt("width"),
                body.getInt("height"));
    }

    private static String parseError(byte[] response, String fallback) {
        try {
            String detail =
                    new JSONObject(new String(response, StandardCharsets.UTF_8))
                            .optString("detail");
            return detail.isBlank() ? fallback : detail;
        } catch (JSONException exception) {
            return fallback;
        }
    }

    private <T> void postSuccess(Callback<T> callback, T result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private <T> void postUnauthorized(Callback<T> callback) {
        mainHandler.post(callback::onUnauthorized);
    }

    private <T> void postError(Callback<T> callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
