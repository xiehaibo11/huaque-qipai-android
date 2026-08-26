package com.nanbeiyule.game;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

record MembershipPaymentReturn(String orderId, Outcome outcome) {
    enum Outcome {
        SUCCESS,
        CANCEL
    }

    static MembershipPaymentReturn parse(
            String value, String pendingOrderId) {
        if (value == null || pendingOrderId == null) {
            return null;
        }
        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || !"www.nanbeiyule.com".equalsIgnoreCase(uri.getHost())
                    || uri.getPort() != -1
                    || uri.getUserInfo() != null
                    || !"/payment/result".equals(uri.getPath())) {
                return null;
            }
            Map<String, String> query = query(uri.getRawQuery());
            if (query == null) {
                return null;
            }
            String orderId = canonicalUuid(query.get("orderId"));
            String expectedOrderId = canonicalUuid(pendingOrderId);
            if (orderId == null || !orderId.equals(expectedOrderId)) {
                return null;
            }
            Outcome outcome = switch (query.getOrDefault("outcome", "")) {
                case "success" -> Outcome.SUCCESS;
                case "cancel" -> Outcome.CANCEL;
                default -> null;
            };
            return outcome == null
                    ? null
                    : new MembershipPaymentReturn(orderId, outcome);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static Map<String, String> query(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Map.of();
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (String part : rawQuery.split("&", -1)) {
            int separator = part.indexOf('=');
            String rawKey = separator < 0 ? part : part.substring(0, separator);
            String rawValue = separator < 0 ? "" : part.substring(separator + 1);
            String key = decode(rawKey);
            String value = decode(rawValue);
            if (values.putIfAbsent(key, value) != null) {
                return null;
            }
        }
        return values;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UTF-8 is unavailable", exception);
        }
    }

    private static String canonicalUuid(String value) {
        if (value == null
                || !value.matches(
                        "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            return null;
        }
        try {
            return UUID.fromString(value).toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
