package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class SharedAuthSessionPayloadTest {
    @Test
    public void persistsOnlyRefreshCredentialAndMetadata() {
        SharedAuthSessionPayload payload =
                SharedAuthSessionPayload.fromTokens(
                        new AuthApiClient.SessionTokens(
                                "access-secret", "refresh-secret", "Bearer", 900L),
                        1_000L);

        String encoded = payload.encode();
        SharedAuthSessionPayload decoded = SharedAuthSessionPayload.decode(encoded);

        assertFalse(encoded.contains("access-secret"));
        assertEquals("refresh-secret", decoded.refreshToken());
        assertEquals("Bearer", decoded.tokenType());
        assertEquals(900L, decoded.expiresIn());
        assertEquals(1_000L, decoded.issuedAt());
    }

    @Test
    public void rejectsIncompleteOrMalformedSession() {
        assertEquals(null, SharedAuthSessionPayload.decode("not-json"));
        assertEquals(null, SharedAuthSessionPayload.decode("{\"refreshToken\":\"\"}"));
    }
}
