package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayInteraction;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingProjection;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;
import com.nanbeiyule.game.mahjong.TaizhouSettleState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public final class TaizhouSettleChromeTouchTest {
    @Test
    public void settlementLetsTopMenuOpenSettings() {
        Fixture fixture = new Fixture();
        tap(fixture, TaizhouMahjongWaitingLayout.MENU_BUTTON);

        assertEquals(List.of(TaizhouMahjongWaitingProjection.Action.MENU), fixture.listener.actions);
        assertEquals(0, fixture.listener.settleActions);
    }

    @Test
    public void settlementLetsTopTrustButtonStartTrust() {
        Fixture fixture = new Fixture();
        tap(fixture, TaizhouMahjongWaitingLayout.TRUST_BUTTON);

        assertEquals(List.of(TaizhouMahjongWaitingProjection.Action.TRUST), fixture.listener.actions);
        assertEquals(0, fixture.listener.settleActions);
    }

    private static void tap(
            Fixture fixture, TaizhouMahjongWaitingLayout.CenterButton button) {
        assertTrue(
                fixture.controller.onDown(
                        fixture.state, button.centerX, button.centerY, button.centerY));
        assertTrue(
                fixture.controller.onUp(
                        fixture.state, button.centerX, button.centerY, button.centerY));
    }

    private static final class Fixture {
        final RecordingListener listener = new RecordingListener();
        final GameplayTableState state = state();
        final TaizhouTableTouchController controller =
                new TaizhouTableTouchController(
                        new TaizhouMahjongPlayInteraction(),
                        new TaizhouMahjongVoiceGesture(),
                        null,
                        new TaizhouCanHuTracker(),
                        listener);
    }

    private static GameplayTableState state() {
        return new GameplayTableState(
                "session",
                "123456",
                30400L,
                50,
                "GOLD",
                GameplayPhase.ROUND_RESULT,
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
                Optional.of(
                        new TaizhouSettleState(
                                TaizhouSettleState.Result.DRAWN,
                                "123456",
                                "第1/8局",
                                "2026-08-26 12:00",
                                "台州麻将",
                                List.of())),
                Optional.empty(),
                null,
                null,
                -1,
                "2026-08-26T00:00:00Z",
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

    private static final class RecordingListener implements TaizhouTableTouchController.Listener {
        final List<TaizhouMahjongWaitingProjection.Action> actions = new ArrayList<>();
        int settleActions;

        @Override public void onPlayResult(TaizhouMahjongPlayGesture.Result result) {}
        @Override public void onVoiceResult(TaizhouMahjongVoiceGesture.Result result) {}

        @Override
        public void onWaitingAction(TaizhouMahjongWaitingProjection.Action action) {
            actions.add(action);
        }

        @Override
        public void onSettleAction(TaizhouSettleInteraction.Action action) {
            settleActions++;
        }

        @Override public void onTotalResultAction(TaizhouTotalResultInteraction.Action action) {}
        @Override public void onEarlyStartRequested() {}
        @Override public void onMultipleChoice(TaizhouMultipleState.Choice choice) {}
        @Override public void onGestureClick() {}
        @Override public void onCanHuDismissed() {}
        @Override public void onPlayerHeadTapped(int seatNumber) {}
    }
}
