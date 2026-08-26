package com.nanbeiyule.game;

import java.net.MalformedURLException;
import java.net.URL;

/** Debug-only network diagnostics for gameplay transport failures. */
final class GameplayNetworkDiagnostics {
    private static final String TAG = "NanbeiGameplayApi";

    private GameplayNetworkDiagnostics() {}

    static void requestFailed(String method, String baseUrl, String path, Exception exception) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        android.util.Log.w(
                TAG,
                "request failed method="
                        + safe(method)
                        + " host="
                        + host(baseUrl)
                        + " path="
                        + safePath(path),
                exception);
    }

    private static String host(String baseUrl) {
        try {
            String host = new URL(safe(baseUrl)).getHost();
            return host == null || host.isBlank() ? "<missing>" : host;
        } catch (MalformedURLException exception) {
            return "<invalid>";
        }
    }

    private static String safePath(String path) {
        String safe = safe(path);
        int queryStart = safe.indexOf('?');
        return queryStart < 0 ? safe : safe.substring(0, queryStart) + "?...";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
