package com.nanbeiyule.game;

record SessionTokenSnapshot(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresAtEpochSecond) {
    SessionTokenSnapshot {
        accessToken = normalize(accessToken);
        refreshToken = normalize(refreshToken);
        tokenType = normalize(tokenType);
        if (tokenType.isBlank()) {
            tokenType = "Bearer";
        }
    }

    boolean hasRecoverableSession(long nowEpochSecond) {
        return !refreshToken.isBlank()
                || (!accessToken.isBlank()
                        && accessTokenExpiresAtEpochSecond > nowEpochSecond);
    }

    String usableAccessToken(long nowEpochSecond, long minimumValiditySeconds) {
        long safeMinimumValidity = Math.max(0L, minimumValiditySeconds);
        if (accessToken.isBlank()
                || accessTokenExpiresAtEpochSecond
                        <= nowEpochSecond + safeMinimumValidity) {
            return "";
        }
        return accessToken;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
