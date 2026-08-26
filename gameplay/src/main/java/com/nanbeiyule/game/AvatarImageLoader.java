package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class AvatarImageLoader {
    private static final String TAG = "AvatarImageLoader";
    interface Callback {
        void onBitmap(Bitmap bitmap);

        void onUnauthorized();

        void onError(String message);
    }

    private final AvatarApiClient apiClient;
    private final AvatarDiskCache diskCache;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final LruCache<String, Bitmap> memoryCache;
    private final Bitmap defaultAvatar;

    AvatarImageLoader(Context context, AvatarApiClient apiClient) {
        this.apiClient = apiClient;
        diskCache = new AvatarDiskCache(context);
        int maxKilobytes = (int) Math.min(Integer.MAX_VALUE, Runtime.getRuntime().maxMemory() / 8192L);
        memoryCache =
                new LruCache<>(Math.max(4096, maxKilobytes)) {
                    @Override
                    protected int sizeOf(String key, Bitmap bitmap) {
                        return Math.max(1, bitmap.getByteCount() / 1024);
                    }
                };
        defaultAvatar = AvatarFrameRenderer.loadDefaultAvatar(context.getResources());
    }

    Bitmap defaultAvatar() {
        return defaultAvatar;
    }

    void load(String avatarKey, String accessToken, Callback callback) {
        if (avatarKey == null || avatarKey.isBlank() || "avatar_default".equals(avatarKey)) {
            callback.onBitmap(defaultAvatar);
            return;
        }
        Bitmap memory = memoryCache.get(avatarKey);
        if (memory != null && !memory.isRecycled()) {
            callback.onBitmap(memory);
            refresh(avatarKey, accessToken, null, true, callback);
            return;
        }
        diskExecutor.execute(
                () -> {
                    AvatarDiskCache.CachedFile cached = diskCache.find(avatarKey);
                    Bitmap diskBitmap =
                            cached == null
                                    ? null
                                    : BitmapFactory.decodeFile(
                                            cached.imageFile().getAbsolutePath());
                    if (diskBitmap != null) {
                        memoryCache.put(avatarKey, diskBitmap);
                        mainHandler.post(() -> callback.onBitmap(diskBitmap));
                    }
                    String etag =
                            cached == null ? null : cached.metadata().etag();
                    refresh(avatarKey, accessToken, etag, diskBitmap != null, callback);
                });
    }

    void putMemory(String avatarKey, Bitmap bitmap) {
        if (avatarKey != null && !avatarKey.isBlank() && bitmap != null && !bitmap.isRecycled()) {
            memoryCache.put(avatarKey, bitmap);
        }
    }

    void shutdown() {
        diskExecutor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
        memoryCache.evictAll();
    }

    private void refresh(
            String avatarKey,
            String accessToken,
            String etag,
            boolean hasFallback,
            Callback callback) {
        apiClient.download(
                avatarKey,
                accessToken,
                etag,
                new AvatarApiClient.Callback<>() {
                    @Override
                    public void onSuccess(AvatarApiClient.DownloadResult result) {
                        if (result.notModified()) {
                            return;
                        }
                        Bitmap bitmap =
                                BitmapFactory.decodeByteArray(
                                        result.bytes(),
                                        0,
                                        result.bytes().length);
                        if (bitmap == null) {
                            if (!hasFallback) {
                                callback.onError("头像图片格式不正确");
                            }
                            return;
                        }
                        String sha = shaFromEtag(result.etag());
                        if (!AvatarCacheMetadata.isValidSha256(sha)) {
                            if (!hasFallback) {
                                callback.onError("头像缓存标识不正确");
                            }
                            return;
                        }
                        memoryCache.put(avatarKey, bitmap);
                        callback.onBitmap(bitmap);
                        diskExecutor.execute(
                                () -> {
                                    try {
                                        diskCache.put(
                                                avatarKey,
                                                sha,
                                                result.etag(),
                                                result.bytes());
                                    } catch (IOException exception) {
                                        Log.w(
                                                TAG,
                                                "Unable to publish private avatar disk cache",
                                                exception);
                                    }
                                });
                    }

                    @Override
                    public void onUnauthorized() {
                        callback.onUnauthorized();
                    }

                    @Override
                    public void onError(String message) {
                        if (!hasFallback) {
                            callback.onError(message);
                        }
                    }
                });
    }

    private static String shaFromEtag(String etag) {
        return etag == null
                ? ""
                : etag.trim().replace("\"", "").toLowerCase(Locale.ROOT);
    }
}
