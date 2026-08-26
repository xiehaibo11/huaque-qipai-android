package com.nanbeiyule.game.mahjong;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** {@code Common/CSB/GameBase/PlayerInfoLayer.csb} 的几何换算。 */
public final class TaizhouPlayerInfoLayoutTest {
    @Test
    public void anchorsThePanelAtTheOriginalDesignCentre() {
        // _KW_PANAEL_USER_INFO 1700×774 中心 (960,550.044) → 内容原点 (110,163.044)。
        assertEquals(110.0f, TaizhouPlayerInfoLayout.PANEL_ORIGIN_X, 0.001f);
        assertEquals(163.044f, TaizhouPlayerInfoLayout.PANEL_ORIGIN_COCOS_Y, 0.001f);
        assertEquals(110.0f, TaizhouPlayerInfoLayout.designX(0.0f), 0.001f);
        assertEquals(1080.0f - 163.044f, TaizhouPlayerInfoLayout.designY(0.0f), 0.001f);
    }

    @Test
    public void placesTheHeadAndNicknameFromTheNestedInfoPanel() {
        // _KW_PANEL_INFO 内容原点 (5,10)；_KW_PANAEL_HEAD_POS 局部 (100,662)。
        assertEquals(110.0f + 105.0f, TaizhouPlayerInfoLayout.HEAD.centerX(), 0.001f);
        assertEquals(
                1080.0f - (163.044f + 672.0f), TaizhouPlayerInfoLayout.HEAD.centerY(), 0.001f);
        assertEquals(110.0f + 195.0f, TaizhouPlayerInfoLayout.NICKNAME_LEFT, 0.001f);
        assertEquals(
                1080.0f - (163.044f + 714.43f),
                TaizhouPlayerInfoLayout.NICKNAME_CENTER_Y,
                0.001f);
    }

    @Test
    public void keepsTheKickButtonAtTheOriginalSize() {
        assertEquals(190.0f, TaizhouPlayerInfoLayout.BUTTON_KICK.width(), 0.0f);
        assertEquals(80.0f, TaizhouPlayerInfoLayout.BUTTON_KICK.height(), 0.0f);
        assertEquals(110.0f + 1120.0f, TaizhouPlayerInfoLayout.BUTTON_KICK.centerX(), 0.001f);
    }

    @Test
    public void scalesTheBuyVipButtonByItsCsbNodeScale() {
        // _KW_BTN_BUY_VIP 332.01×108，scale 0.75。
        assertEquals(332.01f * 0.75f, TaizhouPlayerInfoLayout.BUTTON_BUY_VIP.width(), 0.001f);
        assertEquals(108.0f * 0.75f, TaizhouPlayerInfoLayout.BUTTON_BUY_VIP.height(), 0.001f);
        assertEquals(45.0f, TaizhouPlayerInfoLayout.BUY_VIP_FONT_SIZE, 0.001f);
    }

    @Test
    public void keepsBothBlurLayoutsFromTheOriginalPanels() {
        assertEquals(7, TaizhouPlayerInfoLayout.VIP_OTHER_BLUR.length);
        assertEquals(4, TaizhouPlayerInfoLayout.VIP_SELF_BLUR.length);
    }
}
