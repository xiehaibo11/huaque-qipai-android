package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ZhejiangLobbyHeaderOverlayLayoutTest {
    @Test
    public void keepsDynamicValuesAtTheirOriginalTopControlCoordinates() {
        assertEquals(2448.0f, ZhejiangLobbyHeaderOverlayLayout.DESIGN_WIDTH, 0.0f);
        assertEquals(130.0f, ZhejiangLobbyHeaderOverlayLayout.DESIGN_HEIGHT, 0.0f);
        assertEquals(1448.0f, ZhejiangLobbyHeaderOverlayLayout.COIN_VALUE_CENTER_X, 0.0f);
        assertEquals(1828.0f, ZhejiangLobbyHeaderOverlayLayout.DIAMOND_VALUE_CENTER_X, 0.0f);
        assertEquals(2193.0f, ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_VALUE_CENTER_X, 0.0f);
    }

    @Test
    public void confinesIdentityReplacementToTheOriginalTextArea() {
        ZhejiangLobbyHeaderOverlayLayout.Box name =
                ZhejiangLobbyHeaderOverlayLayout.PLAYER_NAME_PATCH;
        ZhejiangLobbyHeaderOverlayLayout.Box id =
                ZhejiangLobbyHeaderOverlayLayout.PLAYER_ID_PATCH;

        assertEquals(new ZhejiangLobbyHeaderOverlayLayout.Box(150, 22, 340, 64), name);
        assertEquals(new ZhejiangLobbyHeaderOverlayLayout.Box(150, 64, 340, 102), id);
    }

    /**
     * 头像绘制区必须落在原图 {@code lobby_top_controls.png}(2448×130) 的金框内芯里。
     * 实测该内芯白区为 x 47..136、y 18..106，外圈橙色描边在 y 11..14 与 110..112；
     * 绘制区四边各内缩 1px，避免头像压到描边的抗锯齿边缘。
     */
    @Test
    public void keepsAvatarInsideTheOriginalGoldFrame() {
        assertEquals(
                new ZhejiangLobbyHeaderOverlayLayout.Box(48, 19, 135, 106),
                ZhejiangLobbyHeaderOverlayLayout.AVATAR_IMAGE);
    }

    @Test
    public void keepsAvatarStrictlyInsideTheFrameChrome() {
        ZhejiangLobbyHeaderOverlayLayout.Box image =
                ZhejiangLobbyHeaderOverlayLayout.AVATAR_IMAGE;
        ZhejiangLobbyHeaderOverlayLayout.Box chrome =
                ZhejiangLobbyHeaderOverlayLayout.AVATAR_CHROME;

        assertTrue(image.left() > chrome.left());
        assertTrue(image.top() > chrome.top());
        assertTrue(image.right() < chrome.right());
        assertTrue(image.bottom() < chrome.bottom());
    }
}
