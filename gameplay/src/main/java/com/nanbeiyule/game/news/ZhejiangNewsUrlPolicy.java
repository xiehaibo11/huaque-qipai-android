package com.nanbeiyule.game.news;

import java.net.URI;
import java.util.Locale;

/** Restricts the native news WebView to Zhejiang Online HTTPS pages. */
public final class ZhejiangNewsUrlPolicy {
    public static final String DEFAULT_URL = "https://zjnews.zjol.com.cn/";

    public boolean permits(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(rawUrl.trim());
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || host == null
                    || uri.getRawUserInfo() != null
                    || uri.getPort() != -1) {
                return false;
            }
            String normalizedHost = host.toLowerCase(Locale.ROOT);
            return normalizedHost.equals("zjol.com.cn")
                    || normalizedHost.endsWith(".zjol.com.cn");
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
