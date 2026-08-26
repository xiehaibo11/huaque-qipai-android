package com.nanbeiyule.game.gameplay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.mahjong.TaizhouWallState;
import java.util.List;
import java.util.Optional;
import org.json.JSONObject;
import org.junit.Test;

public final class TaizhouWallStateProtocolTest {
    @Test
    public void parsesCapturedWallAndOpenWallFieldShapesWithoutChangingIndexes()
            throws Exception {
        JSONObject payload =
                new JSONObject(
                        "{\"wallState\":{\"nWallCnt\":135,\"nAsc\":107,\"nDesc\":108,"
                                + "\"nFirstAsc\":107,\"nFirstDesc\":108,\"bShow\":1},"
                                + "\"openWall\":{\"nIndex\":109,\"nMah\":37}}");

        Optional<TaizhouWallState> parsed =
                GameplayRoundProtocol.parseOptionalWallState(payload);

        assertTrue(parsed.isPresent());
        TaizhouWallState wall = parsed.orElseThrow();
        assertEquals(135, wall.remainingCount());
        assertEquals(107, wall.asc());
        assertEquals(108, wall.desc());
        assertEquals(107, wall.firstAsc());
        assertEquals(108, wall.firstDesc());
        assertEquals(109, wall.openIndex());
        assertEquals(0x25, wall.openTile());
        assertTrue(wall.showImmediately());
    }

    @Test
    public void reducerAcceptsWallOpenedBetweenTheSecondDiceAndDeal() throws Exception {
        GameplayTableState state =
                new GameplayTableState(
                        "session",
                        "123456",
                        30109L,
                        GameplayPhase.DEALING,
                        1,
                        2L,
                        1,
                        4,
                        8,
                        "大众麻将",
                        false,
                        1,
                        List.of(),
                        "2026-08-24T00:00:00Z");
        JSONObject payload =
                new JSONObject(
                        "{\"phase\":\"DEALING\",\"remainingWallCount\":135,"
                                + "\"wallState\":{\"nWallCnt\":135,\"nAsc\":107,"
                                + "\"nDesc\":108,\"nFirstAsc\":107,\"nFirstDesc\":108,"
                                + "\"bShow\":1},\"openWall\":{\"nIndex\":109,\"nMah\":37}}");

        GameplayTableState reduced =
                GameplayReducer.reduce(
                        state,
                        new GameplayEvent("session", 2L, 2, "WALL_OPENED", payload));

        assertEquals(135, reduced.remainingWallCount());
        assertEquals(2, reduced.eventOrder());
    }
}
