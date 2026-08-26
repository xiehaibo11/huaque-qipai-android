package com.huaque.ui;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

final class LoginAgreementConfig {
    static final String BUNDLED_VERSION = "2026-08-23";
    private static final String OPERATOR_NAME = "南北娱乐";
    private static final String TRUSTED_HOST = "www.nanbeiyule.com";

    private final String userServiceUrl;
    private final String privacyPolicyUrl;
    private final String version;

    private LoginAgreementConfig(
            String userServiceUrl,
            String privacyPolicyUrl,
            String version
    ) {
        this.userServiceUrl = userServiceUrl;
        this.privacyPolicyUrl = privacyPolicyUrl;
        this.version = version;
    }

    static LoginAgreementConfig defaults(String legalBaseUrl) {
        return new LoginAgreementConfig(
                LegalDocumentLinks.userServiceUrl(legalBaseUrl),
                LegalDocumentLinks.privacyPolicyUrl(legalBaseUrl),
                BUNDLED_VERSION);
    }

    static LoginAgreementConfig fromJson(String json, String legalBaseUrl) {
        LoginAgreementConfig fallback = defaults(legalBaseUrl);
        try {
            JSONObject root = new JSONObject(json);
            if (!OPERATOR_NAME.equals(root.optString("operatorName"))) {
                return fallback;
            }
            String terms = fallback.userServiceUrl;
            String privacy = fallback.privacyPolicyUrl;
            JSONArray agreements = root.optJSONArray("agreements");
            if (agreements == null) {
                return fallback;
            }
            for (int index = 0; index < agreements.length(); index++) {
                JSONObject agreement = agreements.optJSONObject(index);
                if (agreement == null) {
                    continue;
                }
                String url = agreement.optString("url");
                if (!isTrustedUrl(url)) {
                    continue;
                }
                if ("SERVER".equals(agreement.optString("type"))) {
                    terms = url;
                } else if ("PRIVACY".equals(agreement.optString("type"))) {
                    privacy = url;
                }
            }
            String version = root.optString("version", BUNDLED_VERSION).trim();
            if (version.isEmpty()) {
                version = BUNDLED_VERSION;
            }
            return new LoginAgreementConfig(terms, privacy, version);
        } catch (JSONException ignored) {
            return fallback;
        }
    }

    String userServiceUrl() {
        return userServiceUrl;
    }

    String privacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    String version() {
        return version;
    }

    private static boolean isTrustedUrl(String value) {
        try {
            URI uri = new URI(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && TRUSTED_HOST.equalsIgnoreCase(uri.getHost());
        } catch (URISyntaxException ignored) {
            return false;
        }
    }
}
