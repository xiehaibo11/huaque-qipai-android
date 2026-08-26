package com.nanbeiyule.game;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

record AvatarCacheMetadata(String avatarKey, String sha256, String etag) {
    private static final Pattern SHA_256 = Pattern.compile("[0-9a-f]{64}");

    AvatarCacheMetadata {
        avatarKey = Objects.requireNonNull(avatarKey, "avatarKey").trim();
        sha256 = Objects.requireNonNull(sha256, "sha256").trim().toLowerCase(Locale.ROOT);
        etag = Objects.requireNonNull(etag, "etag").trim();
        if (avatarKey.isEmpty()) {
            throw new IllegalArgumentException("avatarKey must not be blank");
        }
        if (!isValidSha256(sha256)) {
            throw new IllegalArgumentException("sha256 must be 64 lowercase hexadecimal characters");
        }
        if (etag.isEmpty()) {
            throw new IllegalArgumentException("etag must not be blank");
        }
    }

    String fileName() {
        return sha256 + ".jpg";
    }

    String toJson() {
        try {
            return new JSONObject()
                    .put("avatarKey", avatarKey)
                    .put("sha256", sha256)
                    .put("etag", etag)
                    .toString();
        } catch (JSONException exception) {
            throw new IllegalStateException("Unable to encode avatar cache metadata", exception);
        }
    }

    static AvatarCacheMetadata fromJson(String json) {
        try {
            JSONObject body = new JSONObject(json);
            return new AvatarCacheMetadata(
                    body.getString("avatarKey"),
                    body.getString("sha256"),
                    body.getString("etag"));
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid avatar cache metadata", exception);
        }
    }

    static boolean isValidSha256(String value) {
        return value != null && SHA_256.matcher(value).matches();
    }
}
