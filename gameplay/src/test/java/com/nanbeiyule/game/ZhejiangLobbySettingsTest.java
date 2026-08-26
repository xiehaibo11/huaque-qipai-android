package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ZhejiangLobbySettingsTest {
    @Test
    public void defaultsMatchTheRecoveredLobbySettingModule() {
        PersonalCenterSystemSettings settings =
                PersonalCenterSystemSettings.defaults();

        assertEquals(100, settings.musicVolume());
        assertEquals(100, settings.soundVolume());
        assertEquals(50, settings.voiceVolume());
        assertTrue(settings.maleVoice());
    }

    @Test
    public void updatesVoiceVolumeAndPromptVoiceWithoutChangingOtherValues() {
        PersonalCenterSystemSettings defaults =
                PersonalCenterSystemSettings.defaults();

        PersonalCenterSystemSettings updated =
                defaults.withVoiceVolume(36).withMaleVoice(false);

        assertEquals(36, updated.voiceVolume());
        assertFalse(updated.maleVoice());
        assertEquals(defaults.musicVolume(), updated.musicVolume());
        assertEquals(defaults.soundVolume(), updated.soundVolume());
    }

    @Test
    public void rejectsVoiceVolumeOutsideTheOriginalSliderRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PersonalCenterSystemSettings.defaults().withVoiceVolume(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> PersonalCenterSystemSettings.defaults().withVoiceVolume(101));
    }

    @Test
    public void sliderCoordinateMapsAndClampsToZeroThroughOneHundred() {
        assertEquals(0, ZhejiangLobbySettingsLayout.percentForSliderX(309.5f));
        assertEquals(50, ZhejiangLobbySettingsLayout.percentForSliderX(620f));
        assertEquals(100, ZhejiangLobbySettingsLayout.percentForSliderX(930.5f));
        assertEquals(0, ZhejiangLobbySettingsLayout.percentForSliderX(100f));
        assertEquals(100, ZhejiangLobbySettingsLayout.percentForSliderX(1_000f));
    }
}
