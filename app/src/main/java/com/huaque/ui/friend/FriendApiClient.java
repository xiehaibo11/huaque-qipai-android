package com.huaque.ui.friend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FriendApiClient implements FriendGateway {
    interface ConnectionFactory {
        HttpURLConnection open(URL url) throws IOException;
    }

    public interface Callback {
        void onComplete(Response response);
    }

    public static final class Response {
        public final int status;
        public final String body;
        public final IOException networkError;

        Response(int status, String body, IOException networkError) {
            this.status = status;
            this.body = body;
            this.networkError = networkError;
        }

        public boolean isSuccessful() {
            return networkError == null && status >= 200 && status < 300;
        }
    }

    private final String baseUrl;
    private final String accessToken;
    private final Executor callbackExecutor;
    private final ConnectionFactory connectionFactory;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    public FriendApiClient(String baseUrl, String accessToken, Executor callbackExecutor) {
        this(baseUrl, accessToken, callbackExecutor,
                url -> (HttpURLConnection) url.openConnection());
    }

    FriendApiClient(String baseUrl, String accessToken, Executor callbackExecutor,
            ConnectionFactory connectionFactory) {
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.accessToken = accessToken == null ? "" : accessToken;
        this.callbackExecutor = callbackExecutor;
        this.connectionFactory = connectionFactory;
    }

    public void execute(FriendApiRequest request, Callback callback) {
        networkExecutor.execute(() -> {
            Response response = send(request);
            callbackExecutor.execute(() -> callback.onComplete(response));
        });
    }

    private Response send(FriendApiRequest request) {
        HttpURLConnection connection = null;
        try {
            connection = connectionFactory.open(new URL(baseUrl + request.path));
            connection.setConnectTimeout(8_000);
            connection.setReadTimeout(10_000);
            connection.setRequestMethod(request.method);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            if (!request.body.isEmpty()) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.getOutputStream().write(request.body.getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400
                    ? connection.getErrorStream() : connection.getInputStream();
            String body = stream == null ? "" : read(stream);
            if (stream != null) stream.close();
            return new Response(status, body, null);
        } catch (IOException error) {
            return new Response(0, "", error);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String read(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toString(StandardCharsets.UTF_8.name());
    }

    @Override
    public void close() {
        networkExecutor.shutdownNow();
    }
}
