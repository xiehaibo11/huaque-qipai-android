package com.nanbeiyule.game;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class MembershipPaymentUrl {
    private MembershipPaymentUrl() {}

    static URI parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getHost().isBlank()
                    || uri.getUserInfo() != null) {
                return null;
            }
            return uri;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    static URI alipayH5DeepLink(URI paymentUrl) {
        if (paymentUrl == null) {
            return null;
        }
        String encodedUrl;
        try {
            encodedUrl =
                    URLEncoder.encode(
                            paymentUrl.toString(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UTF-8 charset unavailable", exception);
        }
        return URI.create(
                "alipays://platformapi/startapp?appId=20000067&url="
                        + encodedUrl);
    }
}
