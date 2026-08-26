package com.nanbeiyule.game;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Reads every asset used by the two startup pages and reports byte-based verification progress.
 *
 * <p>This supplies the real target values that the original application receives from its
 * HotFixManager listener. The custom loading view remains responsible only for the 33 ms display
 * smoothing.
 */
final class LocalResourceVerifier {
    interface Listener {
        void onProgress(float percent);

        void onFailure(String message);
    }

    private static final int BUFFER_SIZE = 32 * 1024;
    private static final int[] DRAWABLE_RESOURCES = {
        R.drawable.startup_page,
        R.drawable.second_loading_background,
        R.drawable.login_loading_bg,
        R.drawable.login_loading_process,
        R.drawable.game_home_background_nanbei_v1,
        R.drawable.home_panel_blue,
        R.drawable.home_panel_gold,
        R.drawable.home_panel_player,
        R.drawable.home_icon_create_room,
        R.drawable.home_icon_join_room,
        R.drawable.home_card_create_room_v2,
        R.drawable.home_card_join_room_v2,
        R.drawable.home_card_competition_v2,
        R.drawable.game_home_original_primary,
        R.drawable.game_home_original_primary_1600,
        R.drawable.home_icon_customer_service,
        R.drawable.home_icon_bag,
        R.drawable.home_icon_game_center,
        R.drawable.home_button_quick_start
    };
    private static final String[] ASSET_PATHS = {"fonts/fangzhengcuyuan.ttf"};

    private final Context context;
    private final Listener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean cancelled;
    private Thread workerThread;
    private long completedBytes;
    private long totalBytes;
    private int lastPublishedPercent = -1;

    LocalResourceVerifier(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    void start() {
        if (workerThread != null) {
            return;
        }
        workerThread = new Thread(this::verifyResources, "local-resource-verifier");
        workerThread.start();
    }

    void cancel() {
        cancelled = true;
        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void verifyResources() {
        try {
            totalBytes = measureTotalBytes();
            publishProgress(0);

            for (int resourceId : DRAWABLE_RESOURCES) {
                if (cancelled) {
                    return;
                }
                try (InputStream stream = context.getResources().openRawResource(resourceId)) {
                    verifyStream(stream);
                }
            }
            for (String assetPath : ASSET_PATHS) {
                if (cancelled) {
                    return;
                }
                try (InputStream stream = context.getAssets().open(assetPath)) {
                    verifyStream(stream);
                }
            }

            publishProgress(100);
        } catch (IOException | NoSuchAlgorithmException exception) {
            publishFailure("本地资源校验失败");
        }
    }

    private long measureTotalBytes() throws IOException {
        long bytes = 0L;
        for (int resourceId : DRAWABLE_RESOURCES) {
            try (InputStream stream = context.getResources().openRawResource(resourceId)) {
                bytes += Math.max(1, stream.available());
            }
        }
        for (String assetPath : ASSET_PATHS) {
            try (InputStream stream = context.getAssets().open(assetPath)) {
                bytes += Math.max(1, stream.available());
            }
        }
        return Math.max(1L, bytes);
    }

    private void verifyStream(InputStream stream)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[BUFFER_SIZE];
        int count;
        while (!cancelled && (count = stream.read(buffer)) != -1) {
            digest.update(buffer, 0, count);
            completedBytes += count;
            int percent = (int) Math.min(99L, completedBytes * 100L / totalBytes);
            publishProgress(percent);
        }
        // Complete the digest so each stream is fully exercised rather than only counted.
        digest.digest();
    }

    private void publishProgress(int percent) {
        int boundedPercent = Math.max(0, Math.min(100, percent));
        if (cancelled || boundedPercent == lastPublishedPercent) {
            return;
        }
        lastPublishedPercent = boundedPercent;
        mainHandler.post(
                () -> {
                    if (!cancelled) {
                        listener.onProgress(boundedPercent);
                    }
                });
    }

    private void publishFailure(String message) {
        mainHandler.post(
                () -> {
                    if (!cancelled) {
                        listener.onFailure(message);
                    }
                });
    }
}
