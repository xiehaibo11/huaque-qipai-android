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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

final class ShopApiClient {
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

    ShopApiClient() {
        this(BuildConfig.API_BASE_URL);
    }

    ShopApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
    }

    void loadCatalog(String accessToken, ResponseCallback<ShopCatalogResult> callback) {
        request(
                "GET",
                "/api/v1/shop/catalog",
                accessToken,
                null,
                null,
                null,
                callback,
                responseText -> ShopCatalogResult.fromJson(new JSONObject(responseText)));
    }

    void loadMembershipNotice(
            String accessToken,
            ResponseCallback<MembershipNoticeResult> callback) {
        request(
                "GET",
                "/api/v1/membership/notice",
                accessToken,
                null,
                null,
                null,
                callback,
                responseText ->
                        MembershipNoticeResult.fromJson(new JSONObject(responseText)));
    }

    void exchange(
            String accessToken,
            String productCode,
            ResponseCallback<ShopPurchaseResult> callback) {
        if (productCode == null || productCode.isBlank()) {
            postError(callback, "商城商品不存在");
            return;
        }
        try {
            request(
                    "POST",
                    "/api/v1/shop/exchanges",
                    accessToken,
                    new JSONObject().put("productCode", productCode),
                    "Idempotency-Key",
                    "shop-exchange-" + productCode + "-" + UUID.randomUUID(),
                    callback,
                    responseText ->
                            ShopPurchaseResult.fromJson(new JSONObject(responseText)));
        } catch (JSONException exception) {
            postError(callback, "商城兑换参数不正确");
        }
    }

    void loadInventory(
            String accessToken,
            ResponseCallback<List<ShopInventoryItem>> callback) {
        request(
                "GET",
                "/api/v1/shop/inventory",
                accessToken,
                null,
                null,
                null,
                callback,
                ShopInventoryItem::listFromJson);
    }

    void createOrder(
            String accessToken,
            String productCode,
            String provider,
            ResponseCallback<MembershipOrderState> callback) {
        if (productCode == null || productCode.isBlank()) {
            postError(callback, "商城商品不存在");
            return;
        }
        try {
            request(
                    "POST",
                    "/api/v1/payments/orders",
                    accessToken,
                    new JSONObject()
                            .put("productCode", productCode)
                            .put("provider", provider),
                    "Idempotency-Key",
                    "shop-payment-" + productCode + "-" + UUID.randomUUID(),
                    callback,
                    responseText ->
                            MembershipOrderState.fromJson(new JSONObject(responseText)));
        } catch (JSONException exception) {
            postError(callback, "商城订单参数不正确");
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
            String headerName,
            String headerValue,
            ResponseCallback<T> callback,
            ResponseParser<T> parser) {
        if (baseUrl.isEmpty()) {
            postError(callback, "商城服务地址尚未配置");
            return;
        }
        if (accessToken == null || accessToken.isBlank()) {
            postUnauthorized(callback);
            return;
        }
        executor.execute(
                () ->
                        execute(
                                method,
                                path,
                                accessToken,
                                requestBody,
                                headerName,
                                headerValue,
                                callback,
                                parser));
    }

    private <T> void execute(
            String method,
            String path,
            String accessToken,
            JSONObject requestBody,
            String headerName,
            String headerValue,
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
            if (headerName != null && headerValue != null) {
                connection.setRequestProperty(headerName, headerValue);
            }
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
            postError(callback, "商城数据格式不正确，请稍后重试");
        } catch (Exception exception) {
            postError(callback, "无法连接商城服务，请检查网络后重试");
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
        return "商城请求失败，请稍后重试";
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
