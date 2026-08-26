package com.nanbeiyule.game;

import java.net.URI;

/** Rejects non-HTTPS and credential-bearing links before leaving the native announcement page. */
final class AnnouncementPageUrlPolicy {
    private AnnouncementPageUrlPolicy() {}

    static boolean isSafe(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(value.trim());
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null
                    && !uri.getHost().isBlank()
                    && uri.getUserInfo() == null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
