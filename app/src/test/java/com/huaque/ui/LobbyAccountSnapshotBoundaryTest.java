package com.huaque.ui;

import static org.junit.Assert.assertEquals;

import com.nanbeiyule.game.GameHomeState;
import org.json.JSONObject;
import org.junit.Test;

public final class LobbyAccountSnapshotBoundaryTest {
    @Test
    public void exposesAuthenticatedIdentityAndWalletToTheLobbyModule() throws Exception {
        GameHomeState state =
                GameHomeState.fromJson(
                        new JSONObject(
                                """
                                {
                                  "player": {
                                    "userId": "f2797746-09ed-4aaa-877d-dd0b9da530f1",
                                    "publicPlayerId": 1000000001,
                                    "displayName": "手机用户6092",
                                    "avatarKey": "avatar_default",
                                    "membershipLevel": 0
                                  },
                                  "wallet": {
                                    "roomCards": 20000,
                                    "coins": 1000300,
                                    "diamonds": 0
                                  },
                                  "region": {"lobbyId": 900021, "areaName": "台州"},
                                  "announcements": [
                                    {"content": "游戏公告:适当游戏益脑，沉迷游戏伤身"},
                                    {"content": "比赛公告:今晚八点开赛"}
                                  ],
                                  "entries": [{
                                    "code": "TAIZHOU_MAHJONG",
                                    "displayName": "台州麻将",
                                    "entryType": "GAME",
                                    "route": "TAIZHOU_MAHJONG",
                                    "iconKey": "taizhou",
                                    "sortOrder": 0,
                                    "enabled": true
                                  }]
                                }
                                """));

        assertEquals("手机用户6092", state.player().displayName());
        assertEquals(1_000_000_001L, state.player().publicPlayerId());
        assertEquals(20_000L, state.wallet().roomCards());
        assertEquals(1_000_300L, state.wallet().coins());
        assertEquals(0L, state.wallet().diamonds());
        assertEquals(2, state.announcements().size());
        assertEquals(
                "游戏公告:适当游戏益脑，沉迷游戏伤身",
                state.announcements().get(0).content());
    }
}
