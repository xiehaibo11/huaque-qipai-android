package com.nanbeiyule.game.mahjong;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class TaizhouMahjongTableLayoutTest {
    @Test
    public void showOutMahPanelsMatchTheOriginalCenterCsb() {
        assertSlot(
                "KW_PANEL_SHOW_OUT_MAH_1",
                380.0f,
                540.0f,
                TaizhouMahjongTableLayout.showOutMahPanel(
                        TaizhouMahjongTableLayout.SEAT_LEFT));
        assertSlot(
                "KW_PANEL_SHOW_OUT_MAH_2",
                960.0f,
                310.0f,
                TaizhouMahjongTableLayout.showOutMahPanel(
                        TaizhouMahjongTableLayout.SEAT_BOTTOM));
        assertSlot(
                "KW_PANEL_SHOW_OUT_MAH_3",
                1540.0f,
                540.0f,
                TaizhouMahjongTableLayout.showOutMahPanel(
                        TaizhouMahjongTableLayout.SEAT_RIGHT));
        assertSlot(
                "KW_PANEL_SHOW_OUT_MAH_4",
                960.0f,
                783.0f,
                TaizhouMahjongTableLayout.showOutMahPanel(
                        TaizhouMahjongTableLayout.SEAT_TOP));
    }

    private static void assertSlot(
            String name, float expectedX, float expectedCocosY, TaizhouMahjongTableLayout.Slot slot) {
        assertEquals(name, slot.name);
        assertEquals(TaizhouMahjongTableLayout.CENTER, slot.seat);
        assertEquals(expectedX, slot.localX, 0.001f);
        assertEquals(expectedCocosY, slot.localY, 0.001f);
        assertEquals(
                TaizhouMahjongTableLayout.panel(TaizhouMahjongTableLayout.CENTER).originX()
                        + expectedX,
                slot.designX(),
                0.001f);
        assertEquals(
                TaizhouMahjongTableLayout.panel(TaizhouMahjongTableLayout.CENTER).originY()
                        + expectedCocosY,
                slot.cocosY(),
                0.001f);
    }
}
