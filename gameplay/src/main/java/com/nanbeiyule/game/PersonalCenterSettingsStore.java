package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;

/** Persists device-only personal-center system settings without affecting session data. */
public final class PersonalCenterSettingsStore {
    private static final String PREFERENCES_NAME =
            "personal_center_system_settings";
    private static final String MUSIC_ENABLED = "music_enabled";
    private static final String MUSIC_VOLUME = "music_volume";
    private static final String SOUND_ENABLED = "sound_enabled";
    private static final String SOUND_VOLUME = "sound_volume";
    private static final String VOICE_ENABLED = "voice_enabled";
    private static final String VOICE_VOLUME = "voice_volume";
    private static final String MALE_VOICE = "male_voice";
    private static final String VIBRATION_ENABLED = "vibration_enabled";
    private static final String GRAPHICS_QUALITY = "graphics_quality";
    private static final String EFFECTS_QUALITY = "effects_quality";
    private static final String BATTERY_SAVER = "battery_saver";

    private final SharedPreferences preferences;

    public PersonalCenterSettingsStore(Context context) {
        preferences =
                context.getApplicationContext()
                        .getSharedPreferences(
                                PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public PersonalCenterSystemSettings load() {
        PersonalCenterSystemSettings defaults =
                PersonalCenterSystemSettings.defaults();
        return new PersonalCenterSystemSettings(
                preferences.getBoolean(
                        MUSIC_ENABLED, defaults.musicEnabled()),
                clamp(
                        preferences.getInt(
                                MUSIC_VOLUME, defaults.musicVolume()),
                        0,
                        100),
                preferences.getBoolean(
                        SOUND_ENABLED, defaults.soundEnabled()),
                clamp(
                        preferences.getInt(
                                SOUND_VOLUME, defaults.soundVolume()),
                        0,
                        100),
                preferences.getBoolean(
                        VOICE_ENABLED, defaults.voiceEnabled()),
                clamp(
                        preferences.getInt(
                                VOICE_VOLUME, defaults.voiceVolume()),
                        0,
                        100),
                preferences.getBoolean(MALE_VOICE, defaults.maleVoice()),
                preferences.getBoolean(
                        VIBRATION_ENABLED, defaults.vibrationEnabled()),
                clamp(
                        preferences.getInt(
                                GRAPHICS_QUALITY,
                                defaults.graphicsQuality()),
                        0,
                        3),
                clamp(
                        preferences.getInt(
                                EFFECTS_QUALITY,
                                defaults.effectsQuality()),
                        0,
                        2),
                preferences.getBoolean(
                        BATTERY_SAVER, defaults.batterySaver()));
    }

    public void save(PersonalCenterSystemSettings settings) {
        preferences.edit()
                .putBoolean(MUSIC_ENABLED, settings.musicEnabled())
                .putInt(MUSIC_VOLUME, settings.musicVolume())
                .putBoolean(SOUND_ENABLED, settings.soundEnabled())
                .putInt(SOUND_VOLUME, settings.soundVolume())
                .putBoolean(VOICE_ENABLED, settings.voiceEnabled())
                .putInt(VOICE_VOLUME, settings.voiceVolume())
                .putBoolean(MALE_VOICE, settings.maleVoice())
                .putBoolean(VIBRATION_ENABLED, settings.vibrationEnabled())
                .putInt(
                        GRAPHICS_QUALITY,
                        settings.graphicsQuality())
                .putInt(EFFECTS_QUALITY, settings.effectsQuality())
                .putBoolean(BATTERY_SAVER, settings.batterySaver())
                .apply();
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
