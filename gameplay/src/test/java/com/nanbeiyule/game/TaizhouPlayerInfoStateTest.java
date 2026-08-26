package com.nanbeiyule.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@code PlayerInfo/View.luac:662-676 initKickUser} 的四条隐藏规则。 */
public final class TaizhouPlayerInfoStateTest {
    @Test
    public void hostSeesKickOnAFreshBoxRoomForOtherPlayers() {
        assertTrue(TaizhouPlayerInfoState.kickVisible(true, false, false, 0, true));
    }

    @Test
    public void hiddenOnceARoundHasBeenPlayed() {
        assertFalse(TaizhouPlayerInfoState.kickVisible(true, false, false, 1, true));
    }

    @Test
    public void hiddenWhenTheViewerIsNotTheHost() {
        assertFalse(TaizhouPlayerInfoState.kickVisible(false, false, false, 0, true));
    }

    @Test
    public void hiddenWhenTheTargetIsTheHost() {
        assertFalse(TaizhouPlayerInfoState.kickVisible(true, true, false, 0, true));
    }

    @Test
    public void hiddenOnYourOwnSeat() {
        assertFalse(TaizhouPlayerInfoState.kickVisible(true, false, true, 0, true));
    }

    @Test
    public void hiddenOutsideBoxRooms() {
        assertFalse(TaizhouPlayerInfoState.kickVisible(true, false, false, 0, false));
    }
}
