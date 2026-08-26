package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GameRuleLayoutTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void generalRuleCsdGeometryAndClickRegionsAreExact() {
        assertEquals(1920f, GameRuleLayout.DESIGN_WIDTH, EPSILON);
        assertEquals(1080f, GameRuleLayout.DESIGN_HEIGHT, EPSILON);
        assertEquals(0, GameRuleLayout.itemAt(195f, 200f, 0f, 18));
        assertEquals(1, GameRuleLayout.itemAt(195f, 300f, 100f, 18));
        assertEquals(-1, GameRuleLayout.itemAt(500f, 200f, 0f, 18));
        assertTrue(GameRuleLayout.closeContains(137f, 46f));
        assertTrue(GameRuleLayout.imageTutorialContains(1680f, 49f));
        assertEquals(30579L, GameRuleLayout.IMAGE_TUTORIAL_GAME_ID);
        assertEquals(1528f, GameRuleLayout.maxListScroll(18), EPSILON);
    }

    @Test
    public void selectedAndUnselectedStatesMatchRuleViewLua() {
        assertEquals(60f, GameRuleLayout.SELECTED_TEXT_SIZE, EPSILON);
        assertEquals(54f, GameRuleLayout.UNSELECTED_TEXT_SIZE, EPSILON);
        assertEquals(0xFFFFFBCD, GameRuleLayout.SELECTED_TEXT_COLOR);
        assertEquals(0xFFA36F48, GameRuleLayout.UNSELECTED_TEXT_COLOR);
    }
}
