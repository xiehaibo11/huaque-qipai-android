package com.nanbeiyule.game.mahjong;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MahjongTileAppearanceTest {
    @Test
    public void area7109DefaultsMatchTheOperatorPlan() {
        // MahSettingConfig.lua tab[7109].CUSTOM_STYLE[1]
        MahjongTileAppearance appearance = MahjongTileAppearance.area7109Defaults();
        assertEquals("circle", appearance.shapeName());
        assertEquals("light", appearance.lightName());
        assertEquals("green", appearance.colorName());
        assertEquals(2, appearance.faceType());
        // CARD_HEIGHT=0 -> HandMahMinHeight；CARD_WIDTH=0.2 -> MahMaxAddThick*0.2
        assertEquals(170.0f, appearance.faceGroundHeight(), 0.001f);
        assertEquals(3.0f, appearance.addedThickness(), 0.001f);
        // CARD_WORD_SIZE=1 -> MahFaceMaxScale，横躺牌再乘 0.9
        assertEquals(1.0f, appearance.faceScale(false), 0.001f);
        assertEquals(0.9f, appearance.faceScale(true), 0.001f);
    }

    @Test
    public void engineDefaultsKeepTheOriginalMidPointValues() {
        MahjongTileAppearance appearance = MahjongTileAppearance.engineDefaults();
        assertEquals(180.0f, appearance.faceGroundHeight(), 0.001f);
        assertEquals(0.0f, appearance.addedThickness(), 0.001f);
        assertEquals(0.925f, appearance.faceScale(false), 0.001f);
        // 0.925*0.9 低于 MahFaceMinScale，原版 _updateFaceSize 会抬回 0.85。
        assertEquals(0.85f, appearance.faceScale(true), 0.001f);
    }

    @Test
    public void backColorNamesFollowTheOriginalEnum() {
        assertEquals("orange", withColor(1).colorName());
        assertEquals("yellow", withColor(2).colorName());
        assertEquals("green", withColor(3).colorName());
        assertEquals("blue", withColor(4).colorName());
        assertEquals("xg", withColor(6).colorName());
    }

    private static MahjongTileAppearance withColor(int color) {
        return new MahjongTileAppearance(1, 1, color, 2, 0.0f, 0.2f, 1.0f);
    }
}
