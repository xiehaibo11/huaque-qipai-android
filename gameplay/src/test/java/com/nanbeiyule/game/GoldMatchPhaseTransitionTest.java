package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingLayout;
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

    @Test
    public void rightSideOriginalTingSlotDoesNotOpenFortuneWhenTingPanelIsHidden() {
        TaizhouMahjongWaitingLayout.CenterButton slot =
                TaizhouMahjongWaitingLayout.TING_BUTTON;

        assertTrue(TaizhouMahjongWaitingLayout.FORTUNE_BUTTON.contains(
                slot.centerX, slot.centerY));
        assertEquals(TaizhouMahjongWaitingProjection.Action.TING,
                TaizhouMahjongWaitingProjection.actionAt(
                state(30109L),
                slot.centerX,
                slot.centerY));
    }

    @Test
    public void goldWaitingTableDoesNotExposeFriendRoomEntryActions() {
        GameplayTableState state =
                state(
                        30109L,
                        50,
                        "GOLD",
                        GameplayPhase.WAITING,
                        0,
                        List.of(seat(1, false), seat(2, true)));

        assertFalse(TaizhouMahjongWaitingProjection.showInviteAndCopy(state));
        assertFalse(TaizhouMahjongWaitingProjection.showStartButton(state));
        assertEquals(
                TaizhouMahjongWaitingProjection.Action.NONE,
                TaizhouMahjongWaitingProjection.actionAt(
                        state,
                        TaizhouMahjongWaitingLayout.INVITE_BUTTON.centerX,
                        TaizhouMahjongWaitingLayout.INVITE_BUTTON.centerY));
        assertEquals(
                TaizhouMahjongWaitingProjection.Action.NONE,
                TaizhouMahjongWaitingProjection.actionAt(
                        state,
                        TaizhouMahjongWaitingLayout.START_BUTTON.centerX,
                        TaizhouMahjongWaitingLayout.START_BUTTON.centerY));
        assertEquals(
                TaizhouMahjongWaitingProjection.Action.NONE,
                TaizhouMahjongWaitingProjection.actionAt(
                        state,
                        TaizhouMahjongWaitingLayout.COPY_BUTTON.centerX,
                        TaizhouMahjongWaitingLayout.COPY_BUTTON.centerY));
    }

    private static GameplayTableState state(long gameId) {
        return state(gameId, 0, "");
    }

    private static GameplayTableState state(long gameId, int roomMode, String roomVenue) {
        return state(gameId, roomMode, roomVenue, GameplayPhase.PLAYING, 1, List.of());
    }

    private static GameplayTableState state(
            long gameId,
            int roomMode,
            String roomVenue,
            GameplayPhase phase,
            int roundNumber,
            List<GameplaySeat> seats) {
        return new GameplayTableState(
                "session",
                "123456",
                gameId,
                roomMode,
                roomVenue,
                phase,
                roundNumber,
                1L,
                1,
                4,
                8,
                "",
                false,
                1,
                seats,
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

    private static GameplaySeat seat(int seatNumber, boolean ready) {
        return new GameplaySeat(
                seatNumber,
                "user-" + seatNumber,
                1000L + seatNumber,
                "player-" + seatNumber,
                "avatar-" + seatNumber,
                false,
                ready,
                true);
    }
}
