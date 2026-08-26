package com.nanbeiyule.game;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Strict parser for the first-party room invitation App Link. */
final class RoomInvitationLink {
    private RoomInvitationLink() {}

    static String parse(String value) {
        if (value == null) {
            return null;
        }
        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || !"www.nanbeiyule.com".equalsIgnoreCase(uri.getHost())
                    || uri.getPort() != -1
                    || uri.getUserInfo() != null
                    || uri.getFragment() != null
                    || !"/download".equals(uri.getPath())) {
                return null;
            }
            Map<String, String> query = query(uri.getRawQuery());
            if (query == null || query.size() != 1) {
                return null;
            }
            String roomNumber = query.get("key");
            return roomNumber != null && roomNumber.matches("[0-9]{6}")
                    ? roomNumber
                    : null;
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
            if (values.putIfAbsent(decode(rawKey), decode(rawValue)) != null) {
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
}
