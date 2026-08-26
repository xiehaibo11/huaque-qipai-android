package com.nanbeiyule.game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

/** Owns the recovered lobby button effect and authenticated-lobby background music. */
public final class OriginalLobbyAudioController {
    private final SoundPool soundPool;
    private final OriginalLobbyAudioState state = new OriginalLobbyAudioState();
    private int buttonClickSoundId;
    private MediaPlayer lobbyMusicPlayer;

    private boolean buttonClickLoaded;
    private boolean soundEnabled = true;
    private float musicVolume = 0.4f;
    private float soundVolume = 1.0f;

    public OriginalLobbyAudioController(Context context) {
        Context applicationContext = context.getApplicationContext();
        AudioAttributes effectAttributes =
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(
                                AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
        soundPool =
                new SoundPool.Builder()
                        .setMaxStreams(4)
                        .setAudioAttributes(effectAttributes)
                        .build();
        soundPool.setOnLoadCompleteListener(
                (pool, sampleId, status) -> {
                    if (!state.destroyed()
                            && sampleId == buttonClickSoundId
                            && status == 0) {
                        buttonClickLoaded = true;
                    }
                });
        buttonClickSoundId =
                soundPool.load(
                        applicationContext,
                        R.raw.original_lobby_button_click,
                        1);

        lobbyMusicPlayer =
                MediaPlayer.create(
                        applicationContext,
                        R.raw.original_lobby_bgm);
        if (lobbyMusicPlayer != null) {
            lobbyMusicPlayer.setLooping(true);
            lobbyMusicPlayer.setVolume(musicVolume, musicVolume);
        }
    }

    public void playButtonClick() {
        if (state.destroyed()
                || !soundEnabled
                || !buttonClickLoaded
                || buttonClickSoundId == 0) {
            return;
        }
        soundPool.play(
                buttonClickSoundId,
                soundVolume,
                soundVolume,
                1,
                0,
                1.0f);
    }

    public void applySettings(PersonalCenterSystemSettings settings) {
        if (state.destroyed() || settings == null) {
            return;
        }
        state.setMusicEnabled(settings.musicEnabled());
        soundEnabled = settings.soundEnabled();
        musicVolume = settings.musicVolume() / 100.0f;
        soundVolume = settings.soundVolume() / 100.0f;
        if (lobbyMusicPlayer != null) {
            try {
                lobbyMusicPlayer.setVolume(musicVolume, musicVolume);
            } catch (IllegalStateException ignored) {
                // A failed player stays silent.
            }
        }
        if (state.shouldPlayMusic()) {
            startLobbyMusicIfNeeded();
        } else {
            pauseLobbyMusic(false);
        }
    }

    public void setLobbyActive(boolean active) {
        if (state.destroyed() || state.lobbyActive() == active) {
            return;
        }
        state.setLobbyActive(active);
        if (active) {
            startLobbyMusicIfNeeded();
        } else {
            pauseLobbyMusic(true);
        }
    }

    public void onStart() {
        if (state.destroyed()) {
            return;
        }
        state.onStart();
        startLobbyMusicIfNeeded();
    }

    public void onStop() {
        state.onStop();
        pauseLobbyMusic(false);
    }

    public void destroy() {
        if (state.destroyed()) {
            return;
        }
        state.destroy();
        soundPool.release();
        if (lobbyMusicPlayer != null) {
            lobbyMusicPlayer.release();
            lobbyMusicPlayer = null;
        }
    }

    private void startLobbyMusicIfNeeded() {
        if (!state.shouldPlayMusic()
                || lobbyMusicPlayer == null) {
            return;
        }
        try {
            if (!lobbyMusicPlayer.isPlaying()) {
                lobbyMusicPlayer.start();
            }
        } catch (IllegalStateException ignored) {
            // A released or failed player is kept silent instead of breaking navigation.
        }
    }

    private void pauseLobbyMusic(boolean rewind) {
        if (state.destroyed() || lobbyMusicPlayer == null) {
            return;
        }
        try {
            if (lobbyMusicPlayer.isPlaying()) {
                lobbyMusicPlayer.pause();
            }
            if (rewind) {
                lobbyMusicPlayer.seekTo(0);
            }
        } catch (IllegalStateException ignored) {
            // A failed player is kept silent instead of breaking navigation.
        }
    }
}
