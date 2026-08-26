package com.huaque.ui.friend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FriendPanelUpcomingStateTest {
    @Test
    public void firstEntryShowsGuideBeforeFilterList() {
        FriendPanelUpcomingState state = new FriendPanelUpcomingState(false);

        state.enterUpcoming();

        assertTrue(state.isGuideVisible());
        assertFalse(state.isFilterListVisible());
        assertEquals("所有房间", state.selectedName());
    }

    @Test
    public void filterTapConsumesGuideAndOpensOriginalChoices() {
        FriendPanelUpcomingState state = new FriendPanelUpcomingState(false);
        state.enterUpcoming();

        state.tapFilter();

        assertTrue(state.hasSeenGuide());
        assertFalse(state.isGuideVisible());
        assertTrue(state.isFilterListVisible());
        assertEquals(2, state.filterOptions().size());
        assertEquals("所有房间", state.filterOptions().get(0).label);
        assertEquals("比赛场", state.filterOptions().get(1).label);
    }

    @Test
    public void selectionClosesFilterAndIsRestoredOnLaterEntry() {
        FriendPanelUpcomingState state = new FriendPanelUpcomingState(true);
        state.enterUpcoming();
        state.tapFilter();

        state.select(FriendPanelUpcomingState.Filter.MATCH_ARENA);
        state.enterUpcoming();

        assertEquals("比赛场", state.selectedName());
        assertFalse(state.isGuideVisible());
        assertFalse(state.isFilterListVisible());
    }
}
