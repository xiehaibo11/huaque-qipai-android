package com.huaque.ui.auth;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.nanbeiyule.game.auth.SecureStringStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LuaPlatformGateway {
    public interface HttpResultListener {
        void onResult(String requestId, int status, String body);
    }

    private static final int TIMEOUT_MILLIS = 10_000;

    private final String baseUrl;
    private final SecureStringStorage storage;
    private final HttpResultListener resultListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean closed;

    public LuaPlatformGateway(Context context, String baseUrl, HttpResultListener resultListener) {
        this.baseUrl = trimTrailingSlash(baseUrl == null ? "" : baseUrl.trim());
        this.storage = new SecureStringStorage(context);
        this.resultListener = resultListener;
    }

    public void post(String requestId, String path, String body) {
        if (closed) {
            return;
        }
        if (baseUrl.isEmpty() || !isRelativeApiPath(path)) {
            deliver(requestId, 0, "{\"code\":\"SERVICE_UNAVAILABLE\"}");
            return;
        }
        executor.execute(() -> executePost(requestId, path, body == null ? "" : body));
    }

    public String get(String key) {
        return storage.get(key);
    }

    public void set(String key, String value) {
        storage.set(key, value);
    }

    public void close() {
        closed = true;
        executor.shutdownNow();
    }

    private void executePost(String requestId, String path, String body) {
        HttpURLConnection connection = null;
        try {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setFixedLengthStreamingMode(bytes.length);
            connection.setDoOutput(true);
            connection.getOutputStream().write(bytes);
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            deliver(requestId, status, readFully(stream));
        } catch (IOException ignored) {
            deliver(requestId, 0, "{\"code\":\"NETWORK_ERROR\"}");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void deliver(String requestId, int status, String body) {
        mainHandler.post(() -> {
            if (!closed) {
                resultListener.onResult(requestId, status, body);
            }
        });
    }

    private static String readFully(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (InputStream input = stream; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static boolean isRelativeApiPath(String path) {
        return path != null
                && path.startsWith("/api/")
                && !path.startsWith("//")
                && !path.contains("://")
                && !path.contains("\\");
    }

    private static String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
