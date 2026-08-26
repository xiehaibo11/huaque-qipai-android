package com.huaque.ui;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class LoginAgreementConfigClient {
    interface Listener {
        void onLoaded(LoginAgreementConfig config);
    }

    private static final String PATH = "/api/v1/public/login-agreements";
    private static final int TIMEOUT_MILLIS = 5_000;

    private final String apiBaseUrl;
    private final String legalBaseUrl;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean closed;

    LoginAgreementConfigClient(String apiBaseUrl, String legalBaseUrl) {
        this.apiBaseUrl = trimTrailingSlash(apiBaseUrl == null ? "" : apiBaseUrl.trim());
        this.legalBaseUrl = legalBaseUrl;
    }

    void load(Listener listener) {
        if (closed) {
            return;
        }
        if (apiBaseUrl.isEmpty()) {
            deliver(listener, LoginAgreementConfig.defaults(legalBaseUrl));
            return;
        }
        executor.execute(() -> executeLoad(listener));
    }

    void close() {
        closed = true;
        executor.shutdownNow();
    }

    private void executeLoad(Listener listener) {
        LoginAgreementConfig config = LoginAgreementConfig.defaults(legalBaseUrl);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(apiBaseUrl + PATH).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            connection.setRequestProperty("Accept", "application/json");
            if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
                config = LoginAgreementConfig.fromJson(
                        readFully(connection.getInputStream()), legalBaseUrl);
            }
        } catch (IOException ignored) {
            // The production legal URLs remain available when configuration cannot be loaded.
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        deliver(listener, config);
    }

    private void deliver(Listener listener, LoginAgreementConfig config) {
        mainHandler.post(() -> {
            if (!closed) {
                listener.onLoaded(config);
            }
        });
    }

    private static String readFully(InputStream stream) throws IOException {
        try (InputStream input = stream;
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4_096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
