package com.nanbeiyule.game;

/** Immutable device-local system settings shared by the lobby and game table. */
public record PersonalCenterSystemSettings(
        boolean musicEnabled,
        int musicVolume,
        boolean soundEnabled,
        int soundVolume,
        boolean voiceEnabled,
        int voiceVolume,
        boolean maleVoice,
        boolean vibrationEnabled,
        int graphicsQuality,
        int effectsQuality,
        boolean batterySaver) {
    public PersonalCenterSystemSettings {
        requireRange("musicVolume", musicVolume, 0, 100);
        requireRange("soundVolume", soundVolume, 0, 100);
        requireRange("voiceVolume", voiceVolume, 0, 100);
        requireRange("graphicsQuality", graphicsQuality, 0, 3);
        requireRange("effectsQuality", effectsQuality, 0, 2);
    }

    public static PersonalCenterSystemSettings defaults() {
        return new PersonalCenterSystemSettings(
                true,
                100,
                true,
                100,
                true,
                50,
                true,
                true,
                2,
                2,
                false);
    }

    public PersonalCenterSystemSettings withMusicEnabled(boolean value) {
        return copy(value, musicVolume, soundEnabled, soundVolume,
                voiceEnabled, voiceVolume, maleVoice, vibrationEnabled, graphicsQuality,
                effectsQuality, batterySaver);
    }

    public PersonalCenterSystemSettings withMusicVolume(int value) {
        return copy(musicEnabled, value, soundEnabled, soundVolume,
                voiceEnabled, voiceVolume, maleVoice, vibrationEnabled, graphicsQuality,
                effectsQuality, batterySaver);
    }

    public PersonalCenterSystemSettings withSoundEnabled(boolean value) {
        return copy(musicEnabled, musicVolume, value, soundVolume,
                voiceEnabled, voiceVolume, maleVoice, vibrationEnabled, graphicsQuality,
                effectsQuality, batterySaver);
    }

    public PersonalCenterSystemSettings withSoundVolume(int value) {
        return copy(musicEnabled, musicVolume, soundEnabled, value,
                voiceEnabled, voiceVolume, maleVoice, vibrationEnabled, graphicsQuality,
                effectsQuality, batterySaver);
    }

    public PersonalCenterSystemSettings withVoiceEnabled(boolean value) {
        return copy(musicEnabled, musicVolume, soundEnabled, soundVolume,
                value, voiceVolume, maleVoice, vibrationEnabled, graphicsQuality,
                effectsQuality, batterySaver);
    }

    public PersonalCenterSystemSettings withVoiceVolume(int value) {
        return copy(musicEnabled, musicVolume, soundEnabled, soundVolume,
                voiceEnabled, value, maleVoice, vibrationEnabled, graphicsQuality,
                effectsQuality, batterySaver);
    }

    public PersonalCenterSystemSettings withMaleVoice(boolean value) {
        return copy(musicEnabled, musicVolume, soundEnabled, soundVolume,
                voiceEnabled, voiceVolume, value, vibrationEnabled, graphicsQuality,
                effectsQuality, batterySaver);
    }

    public PersonalCenterSystemSettings withVibrationEnabled(boolean value) {
        return copy(musicEnabled, musicVolume, soundEnabled, soundVolume,
                voiceEnabled, voiceVolume, maleVoice, value, graphicsQuality,
                effectsQuality, batterySaver);
    }

    public PersonalCenterSystemSettings withGraphicsQuality(int value) {
        return copy(musicEnabled, musicVolume, soundEnabled, soundVolume,
                voiceEnabled, voiceVolume, maleVoice, vibrationEnabled, value,
                effectsQuality, batterySaver);
    }

    public PersonalCenterSystemSettings withEffectsQuality(int value) {
        return copy(musicEnabled, musicVolume, soundEnabled, soundVolume,
                voiceEnabled, voiceVolume, maleVoice, vibrationEnabled, graphicsQuality,
                value, batterySaver);
    }

    public PersonalCenterSystemSettings withBatterySaver(boolean value) {
        return copy(musicEnabled, musicVolume, soundEnabled, soundVolume,
                voiceEnabled, voiceVolume, maleVoice, vibrationEnabled, graphicsQuality,
                effectsQuality, value);
    }

    private static PersonalCenterSystemSettings copy(
            boolean musicEnabled,
            int musicVolume,
            boolean soundEnabled,
            int soundVolume,
            boolean voiceEnabled,
            int voiceVolume,
            boolean maleVoice,
            boolean vibrationEnabled,
            int graphicsQuality,
            int effectsQuality,
            boolean batterySaver) {
        return new PersonalCenterSystemSettings(
                musicEnabled,
                musicVolume,
                soundEnabled,
                soundVolume,
                voiceEnabled,
                voiceVolume,
                maleVoice,
                vibrationEnabled,
                graphicsQuality,
                effectsQuality,
                batterySaver);
    }

    private static void requireRange(
            String name, int value, int minimum, int maximum) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be between " + minimum + " and " + maximum);
        }
    }
}
