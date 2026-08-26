package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.TaizhouSettingStyle.Choice;
import org.junit.Test;

public final class TaizhouSettingNewInteractionTest {
    @Test
    public void rootBasicFunctionHitBoxesMatchSettingNewMenu() {
        // _KW_PANAEL_SET_SWITCH(39.2196,853.46) 里的 _KW_IMG_CPYY_10(235,75) 与 _KW_TRUST_BTN(243.261,5)。
        assertTrue(TaizhouSettingNewInteraction.voiceSwitchContains(274.22f, 151.54f));
        assertTrue(TaizhouSettingNewInteraction.trustButtonContains(282.48f, 221.54f));
        assertNull(TaizhouSettingNewInteraction.menuPageAt(282.48f, 221.54f));
    }

    @Test
    public void advancedTabIsTheBottomBarNotTheWholeBasicPanel() {
        assertSame(
                TaizhouSettingNewLayout.Page.ADVANCED,
                TaizhouSettingNewInteraction.menuPageAt(331.92f, 308.51f));
        // 608×291 的基础功能面板上半部分不是页签，点击不切页。
        assertNull(TaizhouSettingNewInteraction.menuPageAt(331.92f, 150.0f));
    }

    @Test
    public void bottomButtonFollowsRoomType() {
        assertTrue(TaizhouSettingNewInteraction.roomButtonContains(333.5f, 1007.05f, true));
        assertTrue(TaizhouSettingNewInteraction.roomButtonContains(333.0f, 1006.0f, false));
    }

    @Test
    public void detailOptionsHitTheOriginalItemBoxes() {
        // _KW_TABLE_STYLE_1 的 470×300 底图中心：内容器顶对齐后位于详情局部 (335,185)。
        TaizhouSettingNewInteraction.Selection selection =
                TaizhouSettingNewInteraction.optionAt(
                        TaizhouSettingNewLayout.Page.TABLE, 335.0f, 185.0f, 0.0f);
        assertEquals(Choice.TABLE_STYLE, selection.choice());
        assertEquals(0, selection.index());
    }

    @Test
    public void advancedTogglesSplitIntoTwoSegments() {
        TaizhouSettingNewInteraction.ToggleHit on =
                TaizhouSettingNewInteraction.toggleAt(300.0f, 134.37f, 134.37f);
        assertSame(TaizhouSettingNewAdvancedLayout.Toggle.TING_HINT, on.toggle());
        assertTrue(on.on());
        assertTrue(!TaizhouSettingNewInteraction.toggleAt(560.0f, 134.37f, 134.37f).on());
    }

    @Test
    public void soundAndMusicFollowThePanelBottomEdge() {
        // 音效在 CSB 里没有 VerticalEdge：面板拉高 45 时它跟着下移，命中也要用底边坐标。
        TaizhouSettingNewInteraction.ToggleHit hit =
                TaizhouSettingNewInteraction.toggleAt(1016.2f, 0.0f, 290.31f);
        assertSame(TaizhouSettingNewAdvancedLayout.Toggle.SOUND, hit.toggle());
    }
}
