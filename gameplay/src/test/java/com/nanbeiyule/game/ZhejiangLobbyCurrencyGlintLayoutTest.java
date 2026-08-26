package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ZhejiangLobbyCurrencyGlintLayoutTest {
    @Test
    public void anchorsEffectsToTheThreeMarkedIconCenters() {
        assertEquals(
                new ZhejiangLobbyHeaderOverlayLayout.Box(1288, 43, 1355, 100),
                ZhejiangLobbyHeaderOverlayLayout.COIN_ICON);
        assertEquals(
                new ZhejiangLobbyHeaderOverlayLayout.Box(1662, 43, 1729, 97),
                ZhejiangLobbyHeaderOverlayLayout.DIAMOND_ICON);
        assertEquals(
                new ZhejiangLobbyHeaderOverlayLayout.Box(2049, 40, 2135, 97),
                ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_ICON);

        assertEquals(1317.0f, ZhejiangLobbyHeaderOverlayLayout.COIN_GLINT_CENTER_X, 0.0f);
        assertEquals(69.0f, ZhejiangLobbyHeaderOverlayLayout.COIN_GLINT_CENTER_Y, 0.0f);
        assertEquals(1691.0f, ZhejiangLobbyHeaderOverlayLayout.DIAMOND_GLINT_CENTER_X, 0.0f);
        assertEquals(66.0f, ZhejiangLobbyHeaderOverlayLayout.DIAMOND_GLINT_CENTER_Y, 0.0f);
        assertEquals(2091.0f, ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_GLINT_CENTER_X, 0.0f);
        assertEquals(64.0f, ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_GLINT_CENTER_Y, 0.0f);
    }
}
