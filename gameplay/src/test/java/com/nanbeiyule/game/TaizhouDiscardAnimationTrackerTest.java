package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.List;
import org.junit.Test;

public final class TaizhouDiscardAnimationTrackerTest {
    @Test
    public void startsOnlyForNewLastDiscard() {
        TaizhouDiscardAnimationTracker tracker = new TaizhouDiscardAnimationTracker();

        assertFalse(tracker.update(round(List.of()), 1000L));
        assertTrue(tracker.update(round(List.of(17)), 1100L));
        assertFalse(tracker.update(round(List.of(17)), 1200L));

        TaizhouDiscardAnimationTracker.RunningDiscard running = tracker.running(1250L);
        assertEquals(1, running.serverSeat());
        assertEquals(17, running.tileValue());
        assertEquals(0.75f, running.progress(), 0.01f);
    }

    @Test
    public void keepsCursorAfterFlightEnds() {
        TaizhouDiscardAnimationTracker tracker = new TaizhouDiscardAnimationTracker();
        tracker.update(round(List.of(33)), 2000L);

        assertFalse(tracker.hasRunningDiscard(2220L));
        assertEquals(1, tracker.lastDiscard().serverSeat());
        assertEquals(0, tracker.lastDiscard().tileIndex());
    }

    @Test
    public void tracksElapsedTimeForTheOriginalShowOutMahWindow() {
        TaizhouDiscardAnimationTracker tracker = new TaizhouDiscardAnimationTracker();
        tracker.update(round(List.of(35)), 3000L);

        assertEquals(0L, tracker.lastDiscardElapsedMillis(2990L));
        assertEquals(600L, tracker.lastDiscardElapsedMillis(3600L));
        assertEquals(1100L, tracker.lastDiscardElapsedMillis(4100L));
    }

    private static TaizhouMahjongVisibleRound round(List<Integer> river) {
        return new TaizhouMahjongVisibleRound(
                4,
                1,
                List.of(
                        new TaizhouMahjongVisibleRound.SeatHand(1, List.of(17, 18), null, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(2, 2, false, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(3, 2, false, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(4, 2, false, 0)),
                List.of(),
                List.of(),
                List.of(
                        new TaizhouMahjongVisibleRound.SeatRiver(1, river, 3),
                        new TaizhouMahjongVisibleRound.SeatRiver(2, List.of(), 3),
                        new TaizhouMahjongVisibleRound.SeatRiver(3, List.of(), 3),
                        new TaizhouMahjongVisibleRound.SeatRiver(4, List.of(), 3)),
                river.isEmpty() ? null : new TaizhouMahjongVisibleRound.LastDiscard(1, river.size() - 1));
    }
}
