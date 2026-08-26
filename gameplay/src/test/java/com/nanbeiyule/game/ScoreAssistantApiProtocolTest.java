package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Test;

public class ScoreAssistantApiProtocolTest {
    private static final UUID LEDGER =
            UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID PLAYER_ONE =
            UUID.fromString("40000000-0000-0000-0000-000000000004");

    @Test
    public void parsesDetailPlayersRoundsAndAuthoritativeTotals() throws Exception {
        ScoreAssistantApiProtocol.LedgerDetail detail =
                ScoreAssistantApiProtocol.detailFromJson(
                        """
                        {
                          "ledgerId":"30000000-0000-0000-0000-000000000003",
                          "status":"IN_PROGRESS",
                          "favorite":false,
                          "roundCount":1,
                          "startedAt":"2026-08-24T12:00:00Z",
                          "endedAt":null,
                          "players":[{
                            "playerId":"40000000-0000-0000-0000-000000000004",
                            "position":1,"name":"本人","ownerPlayer":true,"totalScore":18
                          },{
                            "playerId":"50000000-0000-0000-0000-000000000005",
                            "position":2,"name":"牌友","ownerPlayer":false,"totalScore":-18
                          }],
                          "rounds":[{
                            "roundId":"60000000-0000-0000-0000-000000000006",
                            "roundNumber":1,
                            "recordedAt":"2026-08-24T12:05:00Z",
                            "scores":[{
                              "playerId":"40000000-0000-0000-0000-000000000004",
                              "playerName":"本人","scoreDelta":18,"totalAfter":18
                            }]
                          }]
                        }
                        """);

        assertEquals(LEDGER, detail.ledgerId());
        assertEquals(ScoreAssistantApiProtocol.Status.IN_PROGRESS, detail.status());
        assertEquals(2, detail.players().size());
        assertTrue(detail.players().get(0).ownerPlayer());
        assertEquals(18L, detail.players().get(0).totalScore());
        assertEquals(1, detail.rounds().size());
        assertEquals(18L, detail.rounds().get(0).scores().get(0).scoreDelta());
        assertNull(detail.endedAt());
    }

    @Test
    public void parsesOneBasedHistoryAndMonthlyStatistics() throws Exception {
        ScoreAssistantApiProtocol.HistoryPage page =
                ScoreAssistantApiProtocol.historyFromJson(
                        """
                        {
                          "page":2,"pageSize":10,"totalCount":13,"totalPages":2,
                          "ledgers":[{
                            "ledgerId":"30000000-0000-0000-0000-000000000003",
                            "status":"ENDED","favorite":true,"roundCount":3,
                            "startedAt":"2026-08-01T12:00:00Z",
                            "endedAt":"2026-08-01T13:00:00Z",
                            "players":[]
                          }]
                        }
                        """);
        ScoreAssistantApiProtocol.MonthlyStatistics monthly =
                ScoreAssistantApiProtocol.monthlyFromJson(
                        """
                        {"month":"2026-08","totalPlay":2,"winPlay":1,"lossPlay":1,
                         "totalScore":20,"winScore":30,"lossScore":-10,
                         "winMax":"牌友乙","lostMax":null}
                        """);

        assertEquals(2, page.page());
        assertEquals(13L, page.totalCount());
        assertTrue(page.ledgers().get(0).favorite());
        assertEquals("2026-08", monthly.month().toString());
        assertEquals(20L, monthly.totalScore());
        assertEquals("牌友乙", monthly.winMax());
        assertEquals("", monthly.lostMax());
    }

    @Test
    public void parsesRoundStateAndDeleteResponsesWithoutLocalTotals() throws Exception {
        ScoreAssistantApiProtocol.RoundResult round =
                ScoreAssistantApiProtocol.roundFromJson(
                        """
                        {"roundId":"60000000-0000-0000-0000-000000000006",
                         "roundNumber":2,"recordedAt":"2026-08-24T12:10:00Z",
                         "scores":[{"playerId":"40000000-0000-0000-0000-000000000004",
                                    "scoreDelta":-5,"totalAfter":13}]}
                        """);
        ScoreAssistantApiProtocol.LedgerState state =
                ScoreAssistantApiProtocol.stateFromJson(
                        """
                        {"ledgerId":"30000000-0000-0000-0000-000000000003",
                         "status":"ENDED","favorite":true,"roundCount":2,
                         "endedAt":"2026-08-24T12:11:00Z"}
                        """);
        ScoreAssistantApiProtocol.DeleteReceipt deleted =
                ScoreAssistantApiProtocol.deleteFromJson(
                        """
                        {"ledgerId":"30000000-0000-0000-0000-000000000003",
                         "deletedAt":"2026-08-24T12:12:00Z"}
                        """);

        assertEquals(PLAYER_ONE, round.scores().get(0).playerId());
        assertEquals(13L, round.scores().get(0).totalAfter());
        assertEquals(ScoreAssistantApiProtocol.Status.ENDED, state.status());
        assertTrue(state.favorite());
        assertEquals(LEDGER, deleted.ledgerId());
    }

    @Test
    public void clientUsesOnlyTheStableAuthenticatedRoutes() {
        assertEquals("/api/v1/score-ledgers", ScoreAssistantApiClient.createPath());
        assertEquals("/api/v1/score-ledgers/in-progress", ScoreAssistantApiClient.inProgressPath());
        assertEquals("/api/v1/score-ledgers/history?page=2&pageSize=10", ScoreAssistantApiClient.historyPath(2));
        assertEquals("/api/v1/score-ledgers/" + LEDGER, ScoreAssistantApiClient.detailPath(LEDGER));
        assertEquals("/api/v1/score-ledgers/" + LEDGER + "/rounds", ScoreAssistantApiClient.roundPath(LEDGER));
        assertEquals("/api/v1/score-ledgers/" + LEDGER + "/end", ScoreAssistantApiClient.endPath(LEDGER));
        assertEquals("/api/v1/score-ledgers/" + LEDGER + "/favorite", ScoreAssistantApiClient.favoritePath(LEDGER));
        assertEquals("/api/v1/score-ledgers/statistics/monthly?month=2026-08", ScoreAssistantApiClient.monthlyPath("2026-08"));
        assertFalse(ScoreAssistantApiClient.historyPath(1).contains("page=0"));
    }
}
