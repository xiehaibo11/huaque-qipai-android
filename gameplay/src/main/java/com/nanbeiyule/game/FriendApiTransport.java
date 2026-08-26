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

/**
 * HTTP transport for the friend API: threading, Bearer wiring, and the
 * ProblemDetail-to-public-copy error mapping. Endpoint shapes live in
 * {@link FriendApiClient}.
 */
final class FriendApiTransport {
    interface ResultCallback<T> {
        void onSuccess(T result);

        void onUnauthorized();

        void onError(String message);
    }

    /** Resolves string resources so mapped copy stays in res/values. */
    interface MessageResolver {
        String resolve(int resourceId);
    }

    @FunctionalInterface
    interface ResponseParser<T> {
        T parse(String responseText) throws JSONException;
    }

    static final ResponseParser<Void> VOID_PARSER = text -> null;

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private static final Map<String, Integer> ERROR_MESSAGES =
            Map.of(
                    "FRIEND_NOT_FOUND", R.string.friend_error_not_found,
                    "FRIEND_ALREADY_FRIEND",
                            R.string.friend_error_already_friend,
                    "FRIEND_APPLICATION_EXISTS",
                            R.string.friend_error_application_exists,
                    "FRIEND_SELF_OPERATION",
                            R.string.friend_error_self_operation,
                    "FRIEND_NOT_FRIEND",
                            R.string.friend_error_not_friend,
                    "FRIEND_INVITE_TOO_FREQUENT",
                            R.string.friend_error_invite_too_frequent,
                    "FRIEND_RECALL_TOO_FREQUENT",
                            R.string.friend_error_recall_too_frequent);

    private final String baseUrl;
    private final MessageResolver messages;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    FriendApiTransport(String baseUrl, MessageResolver messages) {
        this.baseUrl =
                baseUrl == null
                        ? ""
                        : baseUrl.trim().replaceAll("/+$", "");
        this.messages = messages;
    }

    void shutdown() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    <T> void request(
            String method,
            String path,
            String accessToken,
            JSONObject requestBody,
            ResponseParser<T> parser,
            ResultCallback<T> callback) {
        if (baseUrl.isEmpty()) {
            callback.onError(
                    messages.resolve(R.string.friend_error_unconfigured));
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
            // Only 401 ends the session; business errors such as the 403
            // FRIEND_NOT_FRIEND invite rejection map to public copy.
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
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
                    messages.resolve(R.string.friend_error_bad_response));
        } catch (Exception exception) {
            postError(
                    callback,
                    messages.resolve(R.string.friend_error_network));
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

    private String parseError(String responseText) {
        if (!responseText.isBlank()) {
            try {
                JSONObject body = new JSONObject(responseText);
                Integer mapped =
                        ERROR_MESSAGES.get(body.optString("code"));
                if (mapped != null) {
                    return messages.resolve(mapped);
                }
                String detail = body.optString("detail");
                if (!detail.isBlank()) {
                    return detail;
                }
            } catch (JSONException ignored) {
                // Fall through to the stable public message.
            }
        }
        return messages.resolve(R.string.friend_error_generic);
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
