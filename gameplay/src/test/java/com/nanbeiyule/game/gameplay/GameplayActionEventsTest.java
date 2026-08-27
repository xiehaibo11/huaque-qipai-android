package com.nanbeiyule.game.gameplay;

import static org.junit.Assert.assertEquals;

import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.Test;

public final class GameplayActionEventsTest {
    @Test
    public void meldActionTipKeepsActingSeat() throws Exception {
        GameplayTableState next =
                GameplayActionEvents.applyMeldApplied(
                        state(),
                        event(
                                "MELD_APPLIED",
                                new JSONObject()
                                        .put("seat", 3)
                                        .put("combType", "PONG")
                                        .put("tiles", List.of(17, 17, 17))
                                        .put("fromSeat", 2)));

        GameplayActionTip tip = next.actionTip().orElseThrow();
        assertEquals(GameplayActionTip.Kind.PONG, tip.kind());
        assertEquals(3, tip.seatNumber());
    }

    @Test
    public void concealedKongActionTipKeepsOriginalEffectKind() throws Exception {
        GameplayTableState next =
                GameplayActionEvents.applyMeldApplied(
                        state(),
                        event(
                                "MELD_APPLIED",
                                new JSONObject()
                                        .put("seat", 2)
                                        .put("combType", "CONCEALED_KONG")
                                        .put("tiles", List.of(17, 17, 17, 17))
                                        .put("fromSeat", 2)));

        GameplayActionTip tip = next.actionTip().orElseThrow();
        assertEquals(GameplayActionTip.Kind.CONCEALED_KONG, tip.kind());
        assertEquals(2, tip.seatNumber());
    }

    @Test
    public void flowerActionTipKeepsActingSeat() throws Exception {
        GameplayTableState next =
                GameplayActionEvents.applyFlowerReplaced(
                        state(),
                        event(
                                "FLOWER_REPLACED",
                                new JSONObject()
                                        .put("seat", 2)
                                        .put("flower", 65)
                                        .put("replacement", 17)));

        GameplayActionTip tip = next.actionTip().orElseThrow();
        assertEquals(GameplayActionTip.Kind.FLOWER, tip.kind());
        assertEquals(2, tip.seatNumber());
    }

    @Test
    public void winActionTipKeepsWinnerSeat() throws Exception {
        GameplayTableState next =
                GameplayActionEvents.applyWinDeclared(
                        state(),
                        event(
                                "WIN_DECLARED",
                                new JSONObject()
                                        .put("winnerSeat", 4)
                                        .put("winType", "DIANPAO")));

        GameplayActionTip tip = next.actionTip().orElseThrow();
        assertEquals(GameplayActionTip.Kind.HU, tip.kind());
        assertEquals(4, tip.seatNumber());
    }

    private static GameplayEvent event(String type, JSONObject payload) {
        return new GameplayEvent("session", 2L, 3, type, payload);
    }

    private static GameplayTableState state() {
        return new GameplayTableState(
                "session",
                "123456",
                30109L,
                0,
                "",
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
}
