package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

record SharedAuthSessionPayload(
        String refreshToken, String tokenType, long expiresIn, long issuedAt) {
    static SharedAuthSessionPayload fromTokens(
            AuthApiClient.SessionTokens tokens, long issuedAt) {
        return new SharedAuthSessionPayload(
                tokens.refreshToken(), tokens.tokenType(), tokens.expiresIn(), issuedAt);
    }

    static SharedAuthSessionPayload decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }
        try {
            JSONObject value = new JSONObject(encoded);
            String refreshToken = value.optString("refreshToken", "").trim();
            String tokenType = value.optString("tokenType", "").trim();
            long expiresIn = value.optLong("expiresIn", 0L);
            long issuedAt = value.optLong("issuedAt", 0L);
            if (refreshToken.isEmpty()
                    || !"Bearer".equals(tokenType)
                    || expiresIn <= 0L
                    || issuedAt < 0L) {
                return null;
            }
            return new SharedAuthSessionPayload(
                    refreshToken, tokenType, expiresIn, issuedAt);
        } catch (JSONException ignored) {
            return null;
        }
    }

    String encode() {
        try {
            return new JSONObject()
                    .put("refreshToken", refreshToken)
                    .put("tokenType", tokenType)
                    .put("expiresIn", expiresIn)
                    .put("issuedAt", issuedAt)
                    .toString();
        } catch (JSONException impossible) {
            throw new IllegalStateException("cannot encode authentication session", impossible);
        }
    }

    long accessTokenExpiresAt() {
        return issuedAt + expiresIn;
    }
}
