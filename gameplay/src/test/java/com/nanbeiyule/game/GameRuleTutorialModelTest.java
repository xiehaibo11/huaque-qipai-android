package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GameRuleTutorialModelTest {
    @Test
    public void original30579TutorialBindsExactlyFourRecoveredPages() {
        GameRuleTutorialModel model = new GameRuleTutorialModel();

        assertEquals(30579L, GameRuleTutorialModel.GAME_ID);
        assertEquals(4, GameRuleTutorialModel.PAGE_COUNT);
        assertEquals("game_rule_tutorial_30579_1", model.pageResourceName());
        assertFalse(model.isLastPage());

        assertEquals(GameRuleTutorialModel.Next.PAGE_CHANGED, model.next());
        assertEquals("game_rule_tutorial_30579_2", model.pageResourceName());
        assertEquals(GameRuleTutorialModel.Next.PAGE_CHANGED, model.next());
        assertEquals("game_rule_tutorial_30579_3", model.pageResourceName());
        assertEquals(GameRuleTutorialModel.Next.PAGE_CHANGED, model.next());
        assertEquals("game_rule_tutorial_30579_4", model.pageResourceName());
        assertTrue(model.isLastPage());
        assertEquals(GameRuleTutorialModel.Next.START_GAME, model.next());
        assertEquals(3, model.pageIndex());
    }

    @Test
    public void swipeNavigationCannotLeaveRecoveredPageRange() {
        GameRuleTutorialModel model = new GameRuleTutorialModel();

        model.previous();
        assertEquals(0, model.pageIndex());
        model.select(3);
        model.select(9);
        assertEquals(3, model.pageIndex());
        model.previous();
        assertEquals(2, model.pageIndex());
    }
}
