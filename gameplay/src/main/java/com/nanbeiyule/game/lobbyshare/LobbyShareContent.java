package com.nanbeiyule.game.lobbyshare;

import com.nanbeiyule.game.GameHomeState;
import java.net.URI;
import java.util.Optional;

/** Validated first-party webpage content for a lobby share. */
public final class LobbyShareContent {
    public static final String PRODUCTION_DOWNLOAD_URL =
            "https://www.nanbeiyule.com/download";

    private final String shareTitle;
    private final String webpageUrl;
    private final String inviterDisplayName;
    private final Long inviterPublicPlayerId;

    private LobbyShareContent(
            String shareTitle,
            String webpageUrl,
            String inviterDisplayName,
            Long inviterPublicPlayerId) {
        this.shareTitle = shareTitle;
        this.webpageUrl = webpageUrl;
        this.inviterDisplayName = inviterDisplayName;
        this.inviterPublicPlayerId = inviterPublicPlayerId;
    }

    public static LobbyShareContent create(
            String productName,
            String configuredDownloadUrl,
            GameHomeState.Player currentPlayer) {
        String title = requireText(productName, "productName");
        String url = requireProductionDownloadUrl(configuredDownloadUrl);
        String displayName = null;
        Long publicPlayerId = null;
        if (currentPlayer != null) {
            String candidate = currentPlayer.displayName();
            if (candidate != null && !candidate.isBlank()) {
                displayName = candidate.trim();
            }
            if (currentPlayer.publicPlayerId() > 0L) {
                publicPlayerId = currentPlayer.publicPlayerId();
            }
        }
        return new LobbyShareContent(title, url, displayName, publicPlayerId);
    }

    public String shareTitle() {
        return shareTitle;
    }

    public String shareDescription() {
        if (inviterDisplayName == null) {
            return "";
        }
        if (inviterPublicPlayerId == null) {
            return inviterDisplayName;
        }
        return inviterDisplayName + "（ID：" + inviterPublicPlayerId + "）";
    }

    public String webpageUrl() {
        return webpageUrl;
    }

    public Optional<String> inviterDisplayName() {
        return Optional.ofNullable(inviterDisplayName);
    }

    public Optional<Long> inviterPublicPlayerId() {
        return Optional.ofNullable(inviterPublicPlayerId);
    }

    private static String requireProductionDownloadUrl(String value) {
        String url = requireText(value, "configuredDownloadUrl");
        try {
            URI uri = URI.create(url);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || !"www.nanbeiyule.com".equalsIgnoreCase(uri.getHost())
                    || !"/download".equals(uri.getPath())
                    || uri.getPort() != -1
                    || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("Untrusted download URL");
            }
            return uri.toString();
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Untrusted download URL", exception);
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
