package com.nanbeiyule.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class OriginalLobbyAudioStateTest {
    @Test
    public void playsMusicOnlyWhileStartedOnTheLobbyWithMusicEnabled() {
        OriginalLobbyAudioState state = new OriginalLobbyAudioState();

        assertFalse(state.shouldPlayMusic());

        state.onStart();
        state.setLobbyActive(true);
        assertTrue(state.shouldPlayMusic());

        state.setMusicEnabled(false);
        assertFalse(state.shouldPlayMusic());

        state.setMusicEnabled(true);
        state.onStop();
        assertFalse(state.shouldPlayMusic());
    }

    @Test
    public void destroyedStateNeverRestartsMusic() {
        OriginalLobbyAudioState state = new OriginalLobbyAudioState();
        state.onStart();
        state.setLobbyActive(true);
        state.destroy();

        state.onStart();
        state.setMusicEnabled(true);

        assertFalse(state.shouldPlayMusic());
    }
}
