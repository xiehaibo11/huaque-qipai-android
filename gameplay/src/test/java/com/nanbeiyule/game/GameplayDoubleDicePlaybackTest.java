package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import com.nanbeiyule.game.gameplay.GameplayEvent;
import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.round.MahjongGameStep;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;

public final class GameplayDoubleDicePlaybackTest {
    @Test
    public void bothOriginalThrowChipMessagesReceiveTheirOwnPlaybackWindow()
            throws Exception {
        CapturingScheduler scheduler = new CapturingScheduler();
        GameplayEventPlaybackGate gate = new GameplayEventPlaybackGate(scheduler);
        List<GameplayTableState> accepted = new ArrayList<>();
        GameplayTableState initial = state();

        gate.accept(
                7L,
                initial,
                List.of(
                        diceEvent(1, 1, 4, 3, MahjongGameStep.GAME_STEP_THROW_CHIP_1.value()),
                        diceEvent(2, 4, 6, 1, MahjongGameStep.GAME_STEP_THROW_CHIP_2.value()),
                        wallEvent()),
                true,
                new GameplayEventPlaybackGate.Callback() {
                    @Override
                    public boolean isCurrent(long generation) {
                        return generation == 7L;
                    }

                    @Override
                    public void onAccepted(
                            GameplayTableState nextState,
                            List<GameplayEvent> events,
                            boolean finishesCommand) {
                        accepted.add(nextState);
                    }

                    @Override
                    public void onResyncRequired(boolean finishesCommand) {
                        throw new AssertionError("unexpected resync");
                    }
                });

        assertEquals(List.of(1_200L), scheduler.delays);
        assertEquals(MahjongGameStep.GAME_STEP_THROW_CHIP_1,
                accepted.get(0).diceRoll().orElseThrow().gameStep());

        scheduler.runNext();
        assertEquals(List.of(1_200L, 1_200L), scheduler.delays);
        assertEquals(MahjongGameStep.GAME_STEP_THROW_CHIP_2,
                accepted.get(1).diceRoll().orElseThrow().gameStep());

        scheduler.runNext();
        assertEquals(135, accepted.get(2).remainingWallCount());
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

    private static GameplayEvent diceEvent(
            int order, int seat, int first, int second, int gameStep) throws Exception {
        return new GameplayEvent(
                "session",
                2L,
                order,
                "DICE_ROLLED",
                new JSONObject(
                        "{\"phase\":\"DEALING\",\"remainingWallCount\":136,"
                                + "\"diceRoll\":{\"nSeat\":" + seat
                                + ",\"nCount\":2,\"nChips\":[" + first + "," + second
                                + "],\"showAni\":true,\"gameStep\":" + gameStep + "}}"));
    }

    private static GameplayEvent wallEvent() throws Exception {
        return new GameplayEvent(
                "session",
                2L,
                3,
                "WALL_OPENED",
                new JSONObject(
                        "{\"phase\":\"DEALING\",\"remainingWallCount\":135,"
                                + "\"wallState\":{\"nWallCnt\":135,\"nAsc\":107,"
                                + "\"nDesc\":108,\"nFirstAsc\":107,\"nFirstDesc\":108,"
                                + "\"bShow\":1},\"openWall\":{\"nIndex\":109,\"nMah\":37}}"));
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
