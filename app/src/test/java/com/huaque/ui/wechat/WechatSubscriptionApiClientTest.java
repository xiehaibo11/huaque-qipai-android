package com.huaque.ui.wechat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.wechat.WechatSubscriptionCallback;
import com.nanbeiyule.game.wechat.WechatSubscriptionIntent;
import com.nanbeiyule.game.wechat.WechatSubscriptionPending;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;
import org.junit.Test;

public final class WechatSubscriptionApiClientTest {
    @Test
    public void createsIntentWithEmptyBodyAndBearerToken() throws Exception {
        FakeFactory factory = new FakeFactory();
        factory.responses.add(new FakeConnection(201,
                "{\"intentId\":\"intent-1\",\"templateId\":\"template\","
                        + "\"scene\":7,\"reserved\":\"Reserved123\","
                        + "\"expiresAt\":\"2030-01-01T00:00:00Z\"}"));
        WechatSubscriptionApiClient client = new WechatSubscriptionApiClient(
                "https://api.example.test", Runnable::run, factory);
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<WechatSubscriptionBackend.Result<WechatSubscriptionIntent>>
                result = new AtomicReference<>();

        client.createIntent("jwt-token", value -> {
            result.set(value);
            done.countDown();
        });

        assertTrue(done.await(3, TimeUnit.SECONDS));
        FakeConnection request = factory.last;
        assertEquals("POST", request.getRequestMethod());
        assertEquals("/api/v1/wechat/subscriptions/intents", request.getURL().getPath());
        assertEquals("Bearer jwt-token", request.getRequestProperty("Authorization"));
        assertEquals("", request.requestBody());
        assertNotNull(result.get().value());
        client.close();
    }

    @Test
    public void completesWithExactPathAndJsonFields() throws Exception {
        FakeFactory factory = new FakeFactory();
        factory.responses.add(new FakeConnection(200, "{\"status\":\"CONFIRMED\"}"));
        WechatSubscriptionApiClient client = new WechatSubscriptionApiClient(
                "https://api.example.test", Runnable::run, factory);
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<WechatSubscriptionBackend.Result<String>> result =
                new AtomicReference<>();
        WechatSubscriptionPending pending = WechatSubscriptionPending.sdkPending(
                new WechatSubscriptionIntent(
                        "intent-1", "template", 7, "Reserved123", Long.MAX_VALUE))
                .capture(new WechatSubscriptionCallback(
                        0, "confirm", "template", 7, "Reserved123",
                        "openid", "intent-1"), 1L).pending();

        client.complete("jwt-token", pending, value -> {
            result.set(value);
            done.countDown();
        });

        assertTrue(done.await(3, TimeUnit.SECONDS));
        FakeConnection request = factory.last;
        assertEquals("/api/v1/wechat/subscriptions/intents/intent-1/complete",
                request.getURL().getPath());
        JSONObject body = new JSONObject(request.requestBody());
        assertEquals(7, body.length());
        assertEquals(0, body.getInt("errCode"));
        assertEquals("confirm", body.getString("action"));
        assertEquals("template", body.getString("templateId"));
        assertEquals(7, body.getInt("scene"));
        assertEquals("Reserved123", body.getString("reserved"));
        assertEquals("openid", body.getString("openId"));
        assertEquals("intent-1", body.getString("transaction"));
        assertEquals("CONFIRMED", result.get().value());
        client.close();
    }

    private static final class FakeFactory
            implements WechatSubscriptionApiClient.ConnectionFactory {
        final ArrayDeque<FakeConnection> responses = new ArrayDeque<>();
        FakeConnection last;
        @Override public HttpURLConnection open(URL url) {
            last = responses.remove();
            last.url = url;
            return last;
        }
    }

    private static final class FakeConnection extends HttpURLConnection {
        private final int status;
        private final byte[] response;
        private final ByteArrayOutputStream request = new ByteArrayOutputStream();
        private URL url;

        FakeConnection(int status, String response) throws Exception {
            super(new URL("https://unused.test"));
            this.status = status;
            this.response = response.getBytes(StandardCharsets.UTF_8);
        }
        @Override public URL getURL() { return url; }
        String requestBody() { return request.toString(StandardCharsets.UTF_8); }
        @Override public int getResponseCode() { return status; }
        @Override public InputStream getInputStream() {
            return new ByteArrayInputStream(response);
        }
        @Override public InputStream getErrorStream() {
            return status >= 400 ? new ByteArrayInputStream(response) : null;
        }
        @Override public ByteArrayOutputStream getOutputStream() { return request; }
        @Override public void disconnect() {}
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() throws IOException {}
    }
}
