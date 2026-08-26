package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;

public final class GameplayEventPlaybackGateTest {
    @Test
    public void opponentTurnPreservesFifteenSecondPlaybackDelay() throws Exception {
        assertEquals(15_000L, scheduledDelay(15_000L));
    }

    @Test
    public void opponentTurnDelayIsClampedToTheEightToFifteenSecondWindow() throws Exception {
        assertEquals(8_000L, scheduledDelay(7_000L));
        assertEquals(15_000L, scheduledDelay(16_000L));
    }

    private static long scheduledDelay(long requestedDelay) throws Exception {
        CapturingScheduler scheduler = new CapturingScheduler();
        new GameplayEventPlaybackGate(scheduler)
                .accept(1L, state(), List.of(turnEvent(requestedDelay), tailEvent()), false, callback());
        return scheduler.delays.get(0);
    }

    private static GameplayTableState state() {
        return new GameplayTableState(
                "session",
                "123456",
                30109L,
                GameplayPhase.DEALING,
                1,
                1L,
                Integer.MAX_VALUE,
                4,
                8,
                "大众麻将",
                false,
                1,
                List.of(),
                "2026-08-24T00:00:00Z");
    }

    private static GameplayEvent turnEvent(long requestedDelay) throws Exception {
        return new GameplayEvent(
                "session",
                2L,
                1,
                "TURN_ADVANCED",
                new JSONObject(
                        "{\"activeSeat\":2,\"clockRemainingSeconds\":10,\"playbackDelayMillis\":"
                                + requestedDelay
                                + "}"));
    }

    private static GameplayEvent tailEvent() throws Exception {
        return new GameplayEvent("session", 2L, 2, "BOT_SEATS_FILLED", new JSONObject());
    }

    private static GameplayEventPlaybackGate.Callback callback() {
        return new GameplayEventPlaybackGate.Callback() {
            @Override
            public boolean isCurrent(long generation) {
                return true;
            }

            @Override
            public void onAccepted(
                    GameplayTableState nextState, List<GameplayEvent> events, boolean finishesCommand) {}

            @Override
            public void onResyncRequired(boolean finishesCommand) {
                throw new AssertionError("unexpected resync");
            }
        };
    }

    private static final class CapturingScheduler implements GameplayEventPlaybackGate.Scheduler {
        private final List<Long> delays = new ArrayList<>();

        @Override
        public void postDelayed(Runnable task, long delayMillis) {
            delays.add(delayMillis);
        }
    }
}
