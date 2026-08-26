package com.nanbeiyule.game;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class AvatarApiProtocol {
    private AvatarApiProtocol() {}

    static byte[] multipartBody(String boundary, byte[] jpeg) {
        Objects.requireNonNull(boundary, "boundary");
        Objects.requireNonNull(jpeg, "jpeg");
        if (boundary.isBlank() || boundary.contains("\r") || boundary.contains("\n")) {
            throw new IllegalArgumentException("Invalid multipart boundary");
        }
        byte[] prefix =
                ("--"
                                + boundary
                                + "\r\n"
                                + "Content-Disposition: form-data; name=\"file\"; filename=\"avatar.jpg\"\r\n"
                                + "Content-Type: image/jpeg\r\n\r\n")
                        .getBytes(StandardCharsets.UTF_8);
        byte[] suffix =
                ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[prefix.length + jpeg.length + suffix.length];
        System.arraycopy(prefix, 0, body, 0, prefix.length);
        System.arraycopy(jpeg, 0, body, prefix.length, jpeg.length);
        System.arraycopy(suffix, 0, body, prefix.length + jpeg.length, suffix.length);
        return body;
    }

    static String quotedEtag(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        if (normalized.length() >= 2
                && normalized.startsWith("\"")
                && normalized.endsWith("\"")) {
            return normalized;
        }
        return "\"" + normalized.replace("\"", "") + "\"";
    }
}
