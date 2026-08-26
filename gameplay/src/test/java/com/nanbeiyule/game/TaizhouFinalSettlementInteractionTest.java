package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouSettleLayout;
import com.nanbeiyule.game.mahjong.TaizhouSettleState;
import com.nanbeiyule.game.mahjong.TaizhouTotalResultState;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public final class TaizhouFinalSettlementInteractionTest {
    @Test
    public void completedFinalRoundStillShowsSettlementBeforeTotalResult() {
        GameplayTableState state = completedFinalRound();

        assertTrue(TaizhouSettleInteraction.showing(state));
        assertEquals(
                TaizhouSettleInteraction.Action.TOTAL_RESULT,
                TaizhouSettleInteraction.actionAt(
                        state,
                        TaizhouTotalResultLayout.DESIGN_WIDTH / 2.0f,
                        TaizhouSettleInteractionTestPoint.checkBillCenterY()));
    }

    @Test
    public void totalResultCanOpenFromCompletedFinalRound() {
        TaizhouTotalResultInteraction interaction = new TaizhouTotalResultInteraction();

        assertTrue(interaction.enter(completedFinalRound()));
        assertTrue(interaction.showing(completedFinalRound()));
    }

    private static GameplayTableState completedFinalRound() {
        TaizhouSettleState settlement =
                new TaizhouSettleState(
                        TaizhouSettleState.Result.DRAWN,
                        "123456",
                        "第8/8局",
                        "2026-08-24 12:00",
                        "台州麻将",
                        List.of());
        TaizhouTotalResultState totalResult =
                new TaizhouTotalResultState(8, true, List.of());
        return new GameplayTableState(
                "session",
                "123456",
                30109L,
                GameplayPhase.COMPLETED,
                8,
                9L,
                2,
                4,
                8,
                "台州麻将",
                true,
                1,
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(settlement),
                Optional.empty(),
                null,
                null,
                0,
                "2026-08-24T12:00:00Z",
                Optional.empty(),
                List.of(),
                List.of(),
                Optional.empty(),
                Optional.empty(),
                null,
                null,
                Optional.empty(),
                Optional.of(totalResult));
    }

    private static final class TaizhouSettleInteractionTestPoint {
        private static float checkBillCenterY() {
            return TaizhouSettleLayout.BUTTON_CHECK_BILL.centerY();
        }
    }
}
