package com.huaque.ui;

final class LegalDocumentLinks {
    private LegalDocumentLinks() {
    }

    static String userServiceUrl(String baseUrl) {
        return appendPath(baseUrl, "/terms");
    }

    static String privacyPolicyUrl(String baseUrl) {
        return appendPath(baseUrl, "/privacy");
    }

    private static String appendPath(String baseUrl, String path) {
        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return normalizedBaseUrl + path;
    }
}
