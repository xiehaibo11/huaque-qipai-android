package com.nanbeiyule.game.mahjong;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** {@code Common/CSB/GameBase/DismissLayer.csb} 的几何换算。 */
public final class TaizhouDismissLayoutTest {
    @Test
    public void keepsTheSharedPanelAtTheOriginalDesignCentre() {
        assertEquals(416.5f, TaizhouDismissLayout.PANEL_LEFT, 0.001f);
        assertEquals(210.0f, TaizhouDismissLayout.PANEL_TOP, 0.001f);
        assertEquals(1087.0f, TaizhouDismissLayout.PANEL_WIDTH, 0.0f);
        assertEquals(660.0f, TaizhouDismissLayout.PANEL_HEIGHT, 0.0f);
    }

    @Test
    public void placesTheTitleWhereTheOriginalCsbPutsIt() {
        // Image_2 局部 (543.5,616) 223×71 → 设计 (960, 210+660-616=254)。
        TaizhouDismissLayout.Node title = TaizhouDismissLayout.TITLE;

        assertEquals(960.0f, title.centerX(), 0.001f);
        assertEquals(254.0f, title.centerY(), 0.001f);
        assertEquals(223.0f, title.width(), 0.0f);
        assertEquals(71.0f, title.height(), 0.0f);
    }

    @Test
    public void keepsTheVoteButtonsOnTheOriginalBaseline() {
        // _KW_BTN_REFUSE / _KW_BTN_AGREE 局部 y=95 → 设计 y = 210+660-95 = 775。
        assertEquals(775.0f, TaizhouDismissLayout.BUTTON_REFUSE.centerY(), 0.001f);
        assertEquals(775.0f, TaizhouDismissLayout.BUTTON_AGREE.centerY(), 0.001f);
        assertEquals(
                416.5f + 338.8793f, TaizhouDismissLayout.BUTTON_REFUSE.centerX(), 0.001f);
        assertEquals(416.5f + 762.636f, TaizhouDismissLayout.BUTTON_AGREE.centerX(), 0.001f);
    }

    @Test
    public void spreadsPlayerSeatsWithTheOriginalAverageGap() {
        // calPlayerInfo: averWidth = (1087 - 180*4)/5 = 73.4。
        float average = (1087.0f - 180.0f * 4) / 5.0f;

        assertEquals(
                416.5f + average + 90.0f,
                TaizhouDismissLayout.playerCenterX(0, 4),
                0.001f);
        assertEquals(
                416.5f + average + 90.0f + (180.0f + average) * 3,
                TaizhouDismissLayout.playerCenterX(3, 4),
                0.001f);
    }

    @Test
    public void keepsTheOriginalStatusLabelsAndColours() {
        assertEquals("选择中...", TaizhouDismissLayout.statusLabel(TaizhouDismissStatus.DEFAULT));
        assertEquals("同意", TaizhouDismissLayout.statusLabel(TaizhouDismissStatus.AGREE));
        assertEquals("拒绝", TaizhouDismissLayout.statusLabel(TaizhouDismissStatus.REFUSE));
        assertEquals("同意", TaizhouDismissLayout.statusLabel(TaizhouDismissStatus.REQUEST));
        assertEquals(0xFF868686, TaizhouDismissLayout.statusColor(TaizhouDismissStatus.DEFAULT));
        assertEquals(0xFF09A801, TaizhouDismissLayout.statusColor(TaizhouDismissStatus.AGREE));
        assertEquals(0xFFF23333, TaizhouDismissLayout.statusColor(TaizhouDismissStatus.REFUSE));
    }
}
