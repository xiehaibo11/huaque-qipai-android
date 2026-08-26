package com.nanbeiyule.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingProjection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public final class GoldMatchPhaseTransitionTest {
    @Test
    public void waitingSessionStaysOnOriginalMatchingTable() {
        assertFalse(MainActivityGoldChooseRoomFlow.roundReady(GameplayPhase.WAITING));
        assertFalse(MainActivityGoldChooseRoomFlow.roundReady(GameplayPhase.DISSOLVED));
    }

    @Test
    public void serverDealMovesFromMatchingTableIntoTheGame() {
        assertTrue(MainActivityGoldChooseRoomFlow.roundReady(GameplayPhase.DEALING));
        assertTrue(MainActivityGoldChooseRoomFlow.roundReady(GameplayPhase.PLAYING));
    }

    @Test
    public void taizhouGoldRoomIdentityKeepsGoldSettingSemantics() {
        assertTrue(TaizhouMahjongWaitingProjection.isGoldRoom(state(30400L)));
        assertTrue(TaizhouMahjongWaitingProjection.isGoldRoom(state(30109L, 50, "GOLD")));
        assertFalse(TaizhouMahjongWaitingProjection.isGoldRoom(state(30109L)));
    }

    private static GameplayTableState state(long gameId) {
        return state(gameId, 0, "");
    }

    private static GameplayTableState state(long gameId, int roomMode, String roomVenue) {
        return new GameplayTableState(
                "session",
                "123456",
                gameId,
                roomMode,
                roomVenue,
                GameplayPhase.PLAYING,
                1,
                1L,
                1,
                4,
                8,
                "",
                false,
                1,
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                null,
                null,
                -1,
                "2026-08-25T00:00:00Z",
                Optional.empty(),
                List.of(),
                List.of(),
                Optional.empty(),
                Optional.empty(),
                null,
                null,
                Optional.empty(),
                Optional.empty(),
                Map.of());
    }
}
