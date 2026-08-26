package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class GameRuleStateTest {
    @Test
    public void selectionLoadsOnlyTheSelectedOfficialPageAndKeepsMissingExplicit() {
        GameRuleState state = new GameRuleState(GameRuleCatalog.taizhou());
        assertEquals(30579L, state.selected().gameId());
        assertEquals(GameRuleState.Content.LOADING, state.content());

        state.select(6);
        assertEquals(30109L, state.selected().gameId());
        state.missing();
        assertEquals(GameRuleState.Content.MISSING, state.content());
        assertNull(state.error());
    }
}
