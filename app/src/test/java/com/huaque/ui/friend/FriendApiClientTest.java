package com.huaque.ui.friend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class FriendApiClientTest {
    @Test
    public void sendsBearerJsonRequestAndReturnsTheResponse() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<FriendApiClient.Response> result = new AtomicReference<>();
        AtomicReference<FakeConnection> connection = new AtomicReference<>();
        FriendApiClient client = new FriendApiClient(
                "https://api.example.test", "jwt-token", Runnable::run, url -> {
                    FakeConnection fake = new FakeConnection(url);
                    connection.set(fake);
                    return fake;
                });

        client.execute(FriendApiRequest.apply(42), response -> {
            result.set(response);
            done.countDown();
        });

        assertTrue(done.await(3, TimeUnit.SECONDS));
        assertEquals("Bearer jwt-token", connection.get().getRequestProperty("Authorization"));
        assertEquals("POST", connection.get().getRequestMethod());
        assertEquals("/api/v1/friends/applications", connection.get().getURL().getPath());
        assertEquals("{\"publicPlayerId\":42}", connection.get().requestBody());
        assertEquals(202, result.get().status);
        assertEquals("{\"accepted\":true}", result.get().body);
        assertTrue(result.get().isSuccessful());
        client.close();
    }

    private static final class FakeConnection extends HttpURLConnection {
        private final ByteArrayOutputStream request = new ByteArrayOutputStream();

        FakeConnection(URL url) {
            super(url);
        }

        String requestBody() {
            return new String(request.toByteArray(), StandardCharsets.UTF_8);
        }

        @Override public int getResponseCode() { return 202; }
        @Override public InputStream getInputStream() {
            return new ByteArrayInputStream(
                    "{\"accepted\":true}".getBytes(StandardCharsets.UTF_8));
        }
        @Override public ByteArrayOutputStream getOutputStream() { return request; }
        @Override public void disconnect() {}
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() throws IOException {}
    }
}
