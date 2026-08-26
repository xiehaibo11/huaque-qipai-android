package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GameRuleTutorialLayoutTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void tutorialGeometryMatchesRecoveredViewCsdAndLua() {
        assertEquals(239f, GameRuleTutorialLayout.PANEL_LEFT, EPSILON);
        assertEquals(126f, GameRuleTutorialLayout.PANEL_TOP, EPSILON);
        assertEquals(1442f, GameRuleTutorialLayout.PANEL_WIDTH, EPSILON);
        assertEquals(828f, GameRuleTutorialLayout.PANEL_HEIGHT, EPSILON);
        assertEquals(1320f, GameRuleTutorialLayout.NEXT_LEFT, EPSILON);
        assertEquals(826f, GameRuleTutorialLayout.NEXT_TOP, EPSILON);
        assertEquals(60f, GameRuleTutorialLayout.INDICATOR_BOTTOM_MARGIN, EPSILON);
        assertTrue(GameRuleTutorialLayout.nextContains(1486f, 880f));
        assertTrue(GameRuleTutorialLayout.closeContains(1627f, 187f));
        assertFalse(GameRuleTutorialLayout.closeContains(1500f, 187f));
    }
}
