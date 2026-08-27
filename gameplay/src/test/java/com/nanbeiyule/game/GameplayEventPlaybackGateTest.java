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
    public void opponentTurnShowsBeforeTheFollowingBotEventsPlay() throws Exception {
        CapturingScheduler scheduler = new CapturingScheduler();
        List<GameplayEvent> accepted = new ArrayList<>();

        new GameplayEventPlaybackGate(scheduler)
                .accept(
                        1L,
                        state(),
                        List.of(turnEvent(2), tailEvent()),
                        false,
                        collectingCallback(accepted));

        assertEquals(List.of(GameplayEventPlaybackGate.TURN_ADVANCED_PLAYBACK_MILLIS), scheduler.delays);
        assertEquals(1, accepted.size());
        assertEquals("TURN_ADVANCED", accepted.get(0).type());

        scheduler.runNext();

        assertEquals(List.of(GameplayEventPlaybackGate.TURN_ADVANCED_PLAYBACK_MILLIS), scheduler.delays);
        assertEquals(2, accepted.size());
        assertEquals("BOT_SEATS_FILLED", accepted.get(1).type());
    }

    @Test
    public void ownTurnDoesNotDelayFollowingEvents() throws Exception {
        CapturingScheduler scheduler = new CapturingScheduler();
        List<GameplayEvent> accepted = new ArrayList<>();
        new GameplayEventPlaybackGate(scheduler)
                .accept(
                        1L,
                        state(),
                        List.of(turnEvent(1), tailEvent()),
                        false,
                        collectingCallback(accepted));

        assertEquals(List.of(), scheduler.delays);
        assertEquals(2, accepted.size());
    }

    @Test
    public void delayedSelfEventIsAppliedAfterItsPlaybackWindow() throws Exception {
        CapturingScheduler scheduler = new CapturingScheduler();
        List<GameplayEvent> accepted = new ArrayList<>();
        new GameplayEventPlaybackGate(scheduler)
                .accept(
                        1L,
                        state(),
                        List.of(dealtEvent(), tailEvent()),
                        true,
                        collectingCallback(accepted));

        assertEquals(List.of(1_000L), scheduler.delays);
        assertEquals(List.of(), accepted);

        scheduler.runNext();

        assertEquals(List.of(1_000L), scheduler.delays);
        assertEquals("DEALT", accepted.get(0).type());
        assertEquals("BOT_SEATS_FILLED", accepted.get(1).type());
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

    private static GameplayEvent turnEvent(int activeSeat) throws Exception {
        return new GameplayEvent(
                "session",
                2L,
                1,
                "TURN_ADVANCED",
                new JSONObject(
                        "{\"activeSeat\":" + activeSeat + ",\"clockRemainingSeconds\":10}"));
    }

    private static GameplayEvent tailEvent() throws Exception {
        return new GameplayEvent("session", 2L, 2, "BOT_SEATS_FILLED", new JSONObject());
    }

    private static GameplayEvent dealtEvent() throws Exception {
        return new GameplayEvent(
                "session",
                2L,
                1,
                "DEALT",
                new JSONObject(
                        """
                        {
                          "phase": "DEALING",
                          "roundNumber": 1,
                          "remainingWallCount": 135,
                          "publicRound": {
                            "chairCount": 4,
                            "jokerTiles": [],
                            "insteadTiles": [],
                            "rivers": [
                              {"seatNumber": 1, "tiles": [], "maxLineCount": 3},
                              {"seatNumber": 2, "tiles": [], "maxLineCount": 3},
                              {"seatNumber": 3, "tiles": [], "maxLineCount": 3},
                              {"seatNumber": 4, "tiles": [], "maxLineCount": 3}
                            ]
                          },
                          "multipleChoice": null,
                          "diceRoll": null
                        }
                        """));
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

    private static GameplayEventPlaybackGate.Callback collectingCallback(
            List<GameplayEvent> accepted) {
        return new GameplayEventPlaybackGate.Callback() {
            @Override
            public boolean isCurrent(long generation) {
                return true;
            }

            @Override
            public void onAccepted(
                    GameplayTableState nextState, List<GameplayEvent> events, boolean finishesCommand) {
                accepted.addAll(events);
            }

            @Override
            public void onResyncRequired(boolean finishesCommand) {
                throw new AssertionError("unexpected resync");
            }
        };
    }

    private static final class CapturingScheduler implements GameplayEventPlaybackGate.Scheduler {
        private final List<Long> delays = new ArrayList<>();
        private final List<Runnable> tasks = new ArrayList<>();

        @Override
        public void postDelayed(Runnable task, long delayMillis) {
            tasks.add(task);
            delays.add(delayMillis);
        }

        void runNext() {
            tasks.remove(0).run();
        }
    }
}
