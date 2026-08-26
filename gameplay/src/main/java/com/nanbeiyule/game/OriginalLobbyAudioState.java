package com.nanbeiyule.game;

/** Pure lifecycle policy for lobby background music. */
final class OriginalLobbyAudioState {
    private boolean activityStarted;
    private boolean lobbyActive;
    private boolean musicEnabled = true;
    private boolean destroyed;

    void onStart() {
        if (!destroyed) {
            activityStarted = true;
        }
    }

    void onStop() {
        activityStarted = false;
    }

    void setLobbyActive(boolean active) {
        if (!destroyed) {
            lobbyActive = active;
        }
    }

    void setMusicEnabled(boolean enabled) {
        if (!destroyed) {
            musicEnabled = enabled;
        }
    }

    void destroy() {
        destroyed = true;
        activityStarted = false;
        lobbyActive = false;
    }

    boolean shouldPlayMusic() {
        return !destroyed && activityStarted && lobbyActive && musicEnabled;
    }

    boolean lobbyActive() {
        return lobbyActive;
    }

    boolean destroyed() {
        return destroyed;
    }
}
