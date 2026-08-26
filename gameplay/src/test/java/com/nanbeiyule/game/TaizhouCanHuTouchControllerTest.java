package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.gameplay.GameplayTingInfo;
import com.nanbeiyule.game.mahjong.TaizhouMahjongHandLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayInteraction;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayPermission;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingProjection;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public final class TaizhouCanHuTouchControllerTest {
    @Test
    public void secondTapOnSelectedTingTileDiscardsInsteadOfOnlyClosingCanHu() {
        Fixture fixture = new Fixture();

        assertTrue(
                fixture.controller.onDown(
                        fixture.state,
                        fixture.touchX,
                        0.0f,
                        fixture.touchY + TaizhouMahjongHandLayout.SELECTED_RAISE));

        assertEquals(1, fixture.listener.playIntents.size());
        TaizhouMahjongPlayGesture.PlayIntent intent = fixture.listener.playIntents.get(0);
        assertEquals(fixture.tile.index(), intent.tileIndex);
        assertEquals(fixture.tile.value(), intent.tileValue);
        assertEquals("play-1", intent.actionToken);
        assertEquals(0, fixture.listener.canHuDismissals);
        assertFalse(fixture.tracker.current().visible());
    }

    @Test
    public void tapOutsideSelectedTileStillOnlyClosesCanHu() {
        Fixture fixture = new Fixture();

        assertTrue(fixture.controller.onDown(fixture.state, 50.0f, 50.0f, 50.0f));

        assertTrue(fixture.listener.playIntents.isEmpty());
        assertEquals(1, fixture.listener.canHuDismissals);
        assertFalse(fixture.tracker.current().visible());
    }

    private static final class Fixture {
        final TaizhouMahjongPlayInteraction interaction = new TaizhouMahjongPlayInteraction();
        final TaizhouCanHuTracker tracker = new TaizhouCanHuTracker();
        final RecordingListener listener = new RecordingListener();
        final GameplayTableState state;
        final TaizhouMahjongPlayGesture.Tile tile;
        final float touchX;
        final float touchY;
        final TaizhouTableTouchController controller;

        Fixture() {
            TaizhouMahjongVisibleRound round =
                    new TaizhouMahjongVisibleRound(
                            2,
                            1,
                            List.of(
                                    new TaizhouMahjongVisibleRound.SeatHand(
                                            1, List.of(0x11), 0x19, 0),
                                    TaizhouMahjongVisibleRound.SeatHand.opponent(
                                            2, 2, false, 0)),
                            List.of(),
                            List.of());
            TaizhouMahjongPlayPermission permission =
                    new TaizhouMahjongPlayPermission(
                            "play-1",
                            TaizhouMahjongPlayGesture.Mode.DOUBLE_CLICK,
                            Set.of(0),
                            Set.of(0),
                            Set.of(),
                            Set.of());
            interaction.replace(round, permission);
            tile = interaction.tiles().stream().filter(candidate -> candidate.index() == 0)
                    .findFirst().orElseThrow();
            touchX = (tile.left() + tile.right()) / 2.0f;
            touchY = (tile.bottom() + tile.top()) / 2.0f;
            TaizhouMahjongPlayGesture.Result firstTap =
                    interaction.onDown(touchX, touchY, false);
            assertNotNull(firstTap);
            assertNull(firstTap.playIntent);
            TaizhouMahjongPlayGesture.Result firstTapUp =
                    interaction.onEnd(touchX, touchY);
            assertNotNull(firstTapUp);
            assertNull(firstTapUp.playIntent);
            state = tableState(round, permission, tile.value());
            tracker.update(state, TaizhouMahjongPreferences.defaults(), interaction);
            assertTrue(tracker.current().visible());
            controller =
                    new TaizhouTableTouchController(
                            interaction,
                            new TaizhouMahjongVoiceGesture(),
                            null,
                            tracker,
                            listener);
        }
    }

    private static GameplayTableState tableState(
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongPlayPermission permission,
            int discard) {
        return new GameplayTableState(
                "session",
                "147514",
                30109L,
                GameplayPhase.PLAYING,
                1,
                1L,
                1,
                2,
                8,
                "台州麻将",
                false,
                1,
                List.of(),
                Optional.of(round),
                Optional.of(permission),
                Optional.empty(),
                Optional.empty(),
                1,
                null,
                100,
                "2026-08-25T00:00:00Z",
                Optional.empty(),
                List.of(),
                List.of(),
                Optional.empty(),
                Optional.of(new GameplayTingInfo(1, Map.of(discard, List.of(0x11)))),
                null,
                null,
                Optional.empty(),
                Optional.empty());
    }

    private static final class RecordingListener implements TaizhouTableTouchController.Listener {
        final List<TaizhouMahjongPlayGesture.PlayIntent> playIntents = new ArrayList<>();
        int canHuDismissals;

        @Override
        public void onPlayResult(TaizhouMahjongPlayGesture.Result result) {
            if (result != null && result.playIntent != null) {
                playIntents.add(result.playIntent);
            }
        }

        @Override public void onVoiceResult(TaizhouMahjongVoiceGesture.Result result) {}
        @Override public void onWaitingAction(TaizhouMahjongWaitingProjection.Action action) {}
        @Override public void onSettleAction(TaizhouSettleInteraction.Action action) {}
        @Override public void onTotalResultAction(TaizhouTotalResultInteraction.Action action) {}
        @Override public void onEarlyStartRequested() {}
        @Override public void onMultipleChoice(TaizhouMultipleState.Choice choice) {}
        @Override public void onGestureClick() {}

        @Override
        public void onCanHuDismissed() {
            canHuDismissals++;
        }

        @Override public void onPlayerHeadTapped(int seatNumber) {}
    }
}
