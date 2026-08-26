package com.huaque.ui.wechat;

import com.nanbeiyule.game.wechat.WechatSubscriptionCallback;
import com.nanbeiyule.game.wechat.WechatSubscriptionIntent;
import com.nanbeiyule.game.wechat.WechatSubscriptionPending;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

public final class WechatSubscriptionApiClient implements WechatSubscriptionBackend {
    interface ConnectionFactory {
        HttpURLConnection open(URL url) throws IOException;
    }

    private record HttpResult(int status, String body, boolean networkFailed) {
    }

    private final String baseUrl;
    private final Executor callbackExecutor;
    private final ConnectionFactory connectionFactory;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    public WechatSubscriptionApiClient(String baseUrl, Executor callbackExecutor) {
        this(baseUrl, callbackExecutor, url -> (HttpURLConnection) url.openConnection());
    }

    WechatSubscriptionApiClient(
            String baseUrl,
            Executor callbackExecutor,
            ConnectionFactory connectionFactory) {
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        this.callbackExecutor = callbackExecutor;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void createIntent(
            String accessToken, Callback<WechatSubscriptionIntent> callback) {
        networkExecutor.execute(() -> {
            HttpResult result = send(
                    "/api/v1/wechat/subscriptions/intents", accessToken, "");
            Result<WechatSubscriptionIntent> parsed = parseIntent(result);
            callbackExecutor.execute(() -> callback.onComplete(parsed));
        });
    }

    @Override
    public void complete(
            String accessToken,
            WechatSubscriptionPending pending,
            Callback<String> callback) {
        networkExecutor.execute(() -> {
            String path = "/api/v1/wechat/subscriptions/intents/"
                    + pathSegment(pending.intent().intentId()) + "/complete";
            HttpResult result = send(path, accessToken, completionBody(pending));
            Result<String> parsed = parseCompletion(result);
            callbackExecutor.execute(() -> callback.onComplete(parsed));
        });
    }

    private HttpResult send(String path, String accessToken, String body) {
        if (accessToken == null || accessToken.isBlank()) {
            return new HttpResult(401, "", false);
        }
        HttpURLConnection connection = null;
        try {
            connection = connectionFactory.open(new URL(baseUrl + path));
            connection.setConnectTimeout(8_000);
            connection.setReadTimeout(10_000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            if (!body.isEmpty()) {
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type", "application/json; charset=utf-8");
                connection.setFixedLengthStreamingMode(bytes.length);
                connection.getOutputStream().write(bytes);
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();
            String response = stream == null ? "" : read(stream);
            if (stream != null) {
                stream.close();
            }
            return new HttpResult(status, response, false);
        } catch (IOException error) {
            return new HttpResult(0, "", true);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static Result<WechatSubscriptionIntent> parseIntent(HttpResult result) {
        if (result.networkFailed()) {
            return Result.failure(Failure.NETWORK);
        }
        if (result.status() == 401) {
            return Result.failure(Failure.UNAUTHORIZED);
        }
        if (result.status() != 201) {
            return Result.failure(Failure.REJECTED);
        }
        try {
            JSONObject json = new JSONObject(result.body());
            return Result.success(new WechatSubscriptionIntent(
                    json.getString("intentId"),
                    json.getString("templateId"),
                    json.getInt("scene"),
                    json.getString("reserved"),
                    Instant.parse(json.getString("expiresAt")).toEpochMilli()));
        } catch (JSONException | DateTimeParseException | IllegalArgumentException error) {
            return Result.failure(Failure.REJECTED);
        }
    }

    private static Result<String> parseCompletion(HttpResult result) {
        if (result.networkFailed()) {
            return Result.failure(Failure.NETWORK);
        }
        if (result.status() == 401) {
            return Result.failure(Failure.UNAUTHORIZED);
        }
        if (result.status() < 200 || result.status() >= 300) {
            return Result.failure(Failure.REJECTED);
        }
        try {
            String status = new JSONObject(result.body()).getString("status");
            return status.isBlank()
                    ? Result.failure(Failure.REJECTED)
                    : Result.success(status);
        } catch (JSONException error) {
            return Result.failure(Failure.REJECTED);
        }
    }

    private static String completionBody(WechatSubscriptionPending pending) {
        WechatSubscriptionCallback callback = pending.callback();
        if (callback == null) {
            throw new IllegalArgumentException("subscription callback is missing");
        }
        try {
            return new JSONObject()
                    .put("errCode", callback.errCode())
                    .put("action", callback.action())
                    .put("templateId", callback.templateId())
                    .put("scene", callback.scene())
                    .put("reserved", callback.reserved())
                    .put("openId", callback.openId())
                    .put("transaction", callback.transaction())
                    .toString();
        } catch (JSONException error) {
            throw new IllegalStateException("cannot encode subscription callback", error);
        }
    }

    private static String pathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String read(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = stream.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }

    @Override
    public void close() {
        networkExecutor.shutdownNow();
    }
}
