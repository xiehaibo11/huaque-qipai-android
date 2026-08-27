package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.gameplay.GameplayActionTip;
import org.junit.Test;

public final class TaizhouActionTipTrackerTest {
    @Test
    public void visibleTipKeepsSeatAndElapsedTime() {
        TaizhouActionTipTracker tracker = new TaizhouActionTipTracker();
        tracker.update(new GameplayActionTip(GameplayActionTip.Kind.PONG, 2, 1L, 1), 1000L);
        tracker.update(new GameplayActionTip(GameplayActionTip.Kind.KONG, 3, 2L, 1), 1200L);

        TaizhouActionTipTracker.VisibleTip visible = tracker.visibleTip(1500L).orElseThrow();
        assertEquals(GameplayActionTip.Kind.KONG, visible.tip().kind());
        assertEquals(3, visible.tip().seatNumber());
        assertEquals(300L, visible.elapsedMillis());
    }

    @Test
    public void hidesAtOriginalNineHundredMillisecondBoundary() {
        TaizhouActionTipTracker tracker = new TaizhouActionTipTracker();
        tracker.update(new GameplayActionTip(GameplayActionTip.Kind.PONG, 2, 1L, 1), 1000L);
        tracker.update(new GameplayActionTip(GameplayActionTip.Kind.KONG, 3, 2L, 1), 1200L);

        assertTrue(tracker.visibleTip(2099L).isPresent());
        assertTrue(tracker.visibleTip(2100L).isEmpty());
    }
}
