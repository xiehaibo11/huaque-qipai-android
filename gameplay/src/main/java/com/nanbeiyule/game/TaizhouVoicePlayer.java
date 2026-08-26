package com.nanbeiyule.game;

import android.content.Context;
import android.media.MediaPlayer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Serialized native playback for authenticated room voice bytes. */
final class TaizhouVoicePlayer {
    interface Callback {
        void onComplete();

        void onError(String message);
    }

    private final Context context;
    private MediaPlayer player;
    private File input;
    private float volume = 0.5f;

    TaizhouVoicePlayer(Context context) {
        this.context = context.getApplicationContext();
    }

    void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    void play(byte[] data, Callback callback) {
        stop();
        if (data == null || data.length == 0 || data.length > TaizhouVoiceRecorder.MAX_BYTES) {
            callback.onError("语音内容无效");
            return;
        }
        try {
            input = File.createTempFile("taizhou-play-", ".m4a", context.getCacheDir());
            try (FileOutputStream output = new FileOutputStream(input)) {
                output.write(data);
            }
            player = new MediaPlayer();
            player.setDataSource(input.getAbsolutePath());
            player.setVolume(volume, volume);
            player.setOnPreparedListener(MediaPlayer::start);
            player.setOnCompletionListener(
                    ignored -> {
                        stop();
                        callback.onComplete();
                    });
            player.setOnErrorListener(
                    (ignored, what, extra) -> {
                        stop();
                        callback.onError("语音播放失败");
                        return true;
                    });
            player.prepareAsync();
        } catch (IOException | RuntimeException exception) {
            stop();
            callback.onError("语音播放失败");
        }
    }

    void stop() {
        if (player != null) {
            try {
                player.stop();
            } catch (IllegalStateException ignored) {
                // The asynchronous player may not have reached prepared state.
            }
            player.release();
            player = null;
        }
        if (input != null) {
            //noinspection ResultOfMethodCallIgnored
            input.delete();
            input = null;
        }
    }
}
