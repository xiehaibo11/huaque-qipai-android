package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import com.nanbeiyule.game.auth.SecureStringStorage;

final class LoginSessionStore implements AuthSessionCoordinator.TokenStore {
    private static final String SESSION_KEY = "auth.session";
    private static final String LEGACY_PREFERENCES = "nanbei_login_session";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String TOKEN_TYPE = "token_type";
    private static final String EXPIRES_AT = "expires_at";

    private final SecureStringStorage storage;
    private final SharedPreferences legacyPreferences;
    private SessionTokenSnapshot activeSession;

    LoginSessionStore(Context context) {
        storage = new SecureStringStorage(context);
        legacyPreferences =
                context.getSharedPreferences(
                        LEGACY_PREFERENCES, Context.MODE_PRIVATE);
        migrateLegacySession();
    }

    @Override
    public void save(AuthApiClient.SessionTokens tokens) {
        long issuedAt = System.currentTimeMillis() / 1_000L;
        SharedAuthSessionPayload payload =
                SharedAuthSessionPayload.fromTokens(tokens, issuedAt);
        storage.set(SESSION_KEY, payload.encode());
        activeSession =
                new SessionTokenSnapshot(
                        tokens.accessToken(),
                        tokens.refreshToken(),
                        tokens.tokenType(),
                        payload.accessTokenExpiresAt());
    }

    @Override
    public SessionTokenSnapshot snapshot() {
        SharedAuthSessionPayload persisted =
                SharedAuthSessionPayload.decode(storage.get(SESSION_KEY));
        if (persisted == null) {
            activeSession = null;
            return new SessionTokenSnapshot("", "", "Bearer", 0L);
        }
        if (activeSession == null
                || !persisted.refreshToken().equals(activeSession.refreshToken())) {
            activeSession =
                    new SessionTokenSnapshot(
                            "",
                            persisted.refreshToken(),
                            persisted.tokenType(),
                            persisted.accessTokenExpiresAt());
        }
        return activeSession;
    }

    @Override
    public void clear() {
        activeSession = null;
        storage.set(SESSION_KEY, "");
        legacyPreferences.edit().clear().apply();
    }

    private void migrateLegacySession() {
        String refreshToken = legacyPreferences.getString(REFRESH_TOKEN, "");
        if (refreshToken == null || refreshToken.isBlank()) {
            legacyPreferences.edit().clear().apply();
            return;
        }
        SharedAuthSessionPayload current =
                SharedAuthSessionPayload.decode(storage.get(SESSION_KEY));
        long legacyExpiresAt = legacyPreferences.getLong(EXPIRES_AT, 0L);
        if (current == null || legacyExpiresAt > current.accessTokenExpiresAt()) {
            long now = System.currentTimeMillis() / 1_000L;
            long expiresIn = Math.max(1L, legacyExpiresAt - now);
            String tokenType = legacyPreferences.getString(TOKEN_TYPE, "Bearer");
            storage.set(
                    SESSION_KEY,
                    new SharedAuthSessionPayload(
                                    refreshToken,
                                    tokenType == null || tokenType.isBlank()
                                            ? "Bearer"
                                            : tokenType,
                                    expiresIn,
                                    now)
                            .encode());
        }
        legacyPreferences.edit().clear().apply();
    }
}
