package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class AvatarDiskCache {
    record CachedFile(AvatarCacheMetadata metadata, File imageFile) {}

    private static final String PREFERENCES = "nanbei_avatar_cache";
    private final File directory;
    private final SharedPreferences preferences;

    AvatarDiskCache(Context context) {
        Context applicationContext = context.getApplicationContext();
        directory = new File(applicationContext.getCacheDir(), "avatars");
        preferences =
                applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    synchronized CachedFile find(String avatarKey) {
        String json = preferences.getString(avatarKey, "");
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            AvatarCacheMetadata metadata = AvatarCacheMetadata.fromJson(json);
            if (!metadata.avatarKey().equals(avatarKey)) {
                remove(avatarKey);
                return null;
            }
            File image = new File(directory, metadata.fileName());
            if (!image.isFile() || image.length() <= 0) {
                remove(avatarKey);
                return null;
            }
            return new CachedFile(metadata, image);
        } catch (IllegalArgumentException exception) {
            remove(avatarKey);
            return null;
        }
    }

    synchronized void put(
            String avatarKey, String sha256, String etag, byte[] imageBytes)
            throws IOException {
        AvatarCacheMetadata metadata =
                new AvatarCacheMetadata(
                        avatarKey,
                        sha256,
                        AvatarApiProtocol.quotedEtag(etag));
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Unable to create avatar cache directory");
        }
        File target = new File(directory, metadata.fileName());
        File temporary = new File(directory, metadata.fileName() + ".tmp");
        try (FileOutputStream output = new FileOutputStream(temporary)) {
            output.write(imageBytes);
            output.flush();
            output.getFD().sync();
        }
        if (target.exists() && !target.delete()) {
            temporary.delete();
            throw new IOException("Unable to replace avatar cache entry");
        }
        if (!temporary.renameTo(target)) {
            temporary.delete();
            throw new IOException("Unable to publish avatar cache entry");
        }
        preferences.edit().putString(avatarKey, metadata.toJson()).apply();
    }

    private void remove(String avatarKey) {
        preferences.edit().remove(avatarKey).apply();
    }
}
