package com.nanbeiyule.game.mahjong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class TaizhouDiceLayoutTest {
    @Test
    public void doubleDiceKeepsOriginalPanelGap() {
        TaizhouDiceLayout.Node left = TaizhouDiceLayout.nodeFor(2, 0);
        TaizhouDiceLayout.Node right = TaizhouDiceLayout.nodeFor(2, 1);

        assertEquals(110.0f, right.centerX() - left.centerX(), 0.0f);
        assertEquals(left.centerY(), right.centerY(), 0.0f);
    }

    @Test
    public void originalOnlyBindsOneOrTwoDicePanels() {
        TaizhouDiceLayout.nodeFor(1, 0);
        TaizhouDiceLayout.nodeFor(2, 1);

        assertThrows(IllegalArgumentException.class, () -> TaizhouDiceLayout.nodeFor(3, 0));
    }
}
