package com.nanbeiyule.game;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import java.io.IOException;

/** Plays the original JuBaoPen draw cue recovered with the Zhejiang client evidence. */
final class TaizhouTreasureAudio {
    private static final String DRAW_CUE = "taizhou_treasure_audio/choujiang.mp3";

    private final AssetManager assets;
    private MediaPlayer player;

    TaizhouTreasureAudio(AssetManager assets) {
        this.assets = assets;
    }

    void playDrawCue() {
        release();
        MediaPlayer next = new MediaPlayer();
        try (AssetFileDescriptor descriptor = assets.openFd(DRAW_CUE)) {
            next.setDataSource(
                    descriptor.getFileDescriptor(),
                    descriptor.getStartOffset(),
                    descriptor.getLength());
            next.setOnCompletionListener(this::onFinished);
            next.setOnErrorListener((mediaPlayer, what, extra) -> {
                onFinished(mediaPlayer);
                return true;
            });
            next.prepare();
            player = next;
            next.start();
        } catch (IOException | RuntimeException error) {
            if (player == next) player = null;
            next.release();
        }
    }

    void release() {
        MediaPlayer current = player;
        player = null;
        if (current != null) current.release();
    }

    private void onFinished(MediaPlayer finished) {
        if (player == finished) player = null;
        finished.release();
    }
}
