package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public final class GameRecordPageTest {
    @Test
    public void parsesServerBackedBattleSummaryAndPlayers() throws Exception {
        GameRecordPage page =
                GameRecordPage.fromJson(
                        new JSONObject(
                                """
                                {
                                  "date":"2026-08-24",
                                  "membershipActive":true,
                                  "gameIds":[30109],
                                  "summary":{"championCount":1,"score":12,"roundCount":1},
                                  "records":[{
                                    "sessionId":"30000000-0000-0000-0000-000000000001",
                                    "roomNumber":"123456",
                                    "gameId":30109,
                                    "gameName":"台州麻将",
                                    "gold":false,
                                    "finishedRounds":8,
                                    "totalRounds":8,
                                    "finishedAt":"2026-08-24T12:30:00Z",
                                    "players":[{
                                      "publicPlayerId":1084375590,
                                      "displayName":"WhimSeeker",
                                      "score":12,
                                      "host":true,
                                      "self":true
                                    }]
                                  }]
                                }
                                """));

        assertEquals("2026-08-24", page.date());
        assertEquals(1, page.summary().championCount());
        assertEquals(12L, page.summary().score());
        assertEquals(1, page.records().size());
        GameRecordPage.Record record = page.records().get(0);
        assertEquals("台州麻将", record.gameName());
        assertFalse(record.gold());
        assertEquals(12L, record.players().get(0).score());
        assertTrue(record.players().get(0).self());
    }

    @Test
    public void keepsNonMemberGoldPageLockedWithoutSyntheticRecords() throws Exception {
        GameRecordPage page =
                GameRecordPage.fromJson(
                        new JSONObject(
                                """
                                {
                                  "date":"2026-08-24",
                                  "membershipActive":false,
                                  "gameIds":[],
                                  "summary":{"championCount":0,"score":0,"roundCount":0},
                                  "records":[]
                                }
                                """));

        assertFalse(page.membershipActive());
        assertTrue(page.records().isEmpty());
        assertTrue(page.gameIds().isEmpty());
    }
}
