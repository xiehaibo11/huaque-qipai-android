package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.nanbeiyule.game.gameplay.GameplayActionTip;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import org.junit.Test;

public final class TaizhouMahjongActionEffectTest {
    @Test
    public void mapsMeldKindsToOriginalAnimationNames() {
        assertEquals("chi", TaizhouMahjongActionEffect.animationName(MahjongCombType.CHOW));
        assertEquals("peng", TaizhouMahjongActionEffect.animationName(MahjongCombType.PONG));
        assertEquals("gang", TaizhouMahjongActionEffect.animationName(MahjongCombType.EXPOSED_KONG));
        assertEquals("gang", TaizhouMahjongActionEffect.animationName(MahjongCombType.FILL_KONG));
        assertEquals("angang", TaizhouMahjongActionEffect.animationName(MahjongCombType.CONCEALED_KONG));
        assertNull(TaizhouMahjongActionEffect.animationName(MahjongCombType.NONE));
    }

    @Test
    public void mapsActionTipsToOriginalAnimationNames() {
        assertEquals("chi", TaizhouMahjongActionEffect.animationName(GameplayActionTip.Kind.CHOW));
        assertEquals("peng", TaizhouMahjongActionEffect.animationName(GameplayActionTip.Kind.PONG));
        assertEquals("gang", TaizhouMahjongActionEffect.animationName(GameplayActionTip.Kind.KONG));
        assertEquals(
                "angang",
                TaizhouMahjongActionEffect.animationName(GameplayActionTip.Kind.CONCEALED_KONG));
        assertEquals("hu", TaizhouMahjongActionEffect.animationName(GameplayActionTip.Kind.HU));
        assertNull(TaizhouMahjongActionEffect.animationName(GameplayActionTip.Kind.FLOWER));
    }

    @Test
    public void usesOriginalPlayerHeadAnchors() {
        assertAnchor(TaizhouMahjongTableLayout.SEAT_LEFT, 550.0f, 480.0f);
        assertAnchor(TaizhouMahjongTableLayout.SEAT_BOTTOM, 960.0f, 680.0f);
        assertAnchor(TaizhouMahjongTableLayout.SEAT_RIGHT, 1370.0f, 480.0f);
        assertAnchor(TaizhouMahjongTableLayout.SEAT_TOP, 960.0f, 300.0f);
    }

    private static void assertAnchor(int localSeat, float x, float y) {
        TaizhouMahjongActionEffect.Anchor anchor =
                TaizhouMahjongActionEffect.anchor(localSeat);
        assertEquals(x, anchor.designX(), 0.01f);
        assertEquals(y, anchor.androidY(), 0.01f);
    }
}
