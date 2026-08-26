package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class CreateRoomEntryPolicyTest {
    @Test
    public void externalThirteenWaterStaysAboveTheFirstServerGame() {
        List<CreateRoomGame> games =
                List.of(
                        new CreateRoomGame(30580L, "边锋十三水", "", 0),
                        new CreateRoomGame(30588L, "茶苑双扣", "乌龙", 10));

        assertEquals(1, CreateRoomView.initialGameIndex(games, 0L));
    }

    @Test
    public void externalThirteenWaterDoesNotRequestRuleLoading() {
        assertTrue(CreateRoomEntryPolicy.isExternalMiniProgramGame(30580L));
        assertFalse(CreateRoomEntryPolicy.shouldLoadRuleConfig(30580L));
        assertTrue(CreateRoomEntryPolicy.shouldLoadRuleConfig(30588L));
    }

    @Test
    public void prependsTheExternalItemToTheExactSixteenServerGamesOnlyInTaizhou() {
        List<CreateRoomGame> serverGames = new ArrayList<>();
        long[] ids = {
            30588L, 30577L, 30110L, 30111L, 30109L, 30113L, 30114L, 30399L,
            30250L, 30112L, 30284L, 30156L, 30130L, 30128L, 30155L, 30227L
        };
        for (int index = 0; index < ids.length; index++) {
            serverGames.add(new CreateRoomGame(ids[index], "游戏" + ids[index], "", index));
        }

        List<CreateRoomGame> taizhou = CreateRoomEntryPolicy.gamesForLobby(900023L, serverGames);
        List<CreateRoomGame> other = CreateRoomEntryPolicy.gamesForLobby(900038L, serverGames);

        assertEquals(17, taizhou.size());
        assertEquals(30580L, taizhou.get(0).gameId());
        for (int index = 0; index < ids.length; index++) {
            assertEquals(ids[index], taizhou.get(index + 1).gameId());
        }
        assertEquals(serverGames, other);
    }

    @Test
    public void removesAnUnexpectedServerCopyOfTheExternalItemBeforePrependingIt() {
        List<CreateRoomGame> games =
                CreateRoomEntryPolicy.gamesForLobby(
                        900023L,
                        List.of(
                                new CreateRoomGame(30580L, "错误服务端项", "", 0),
                                new CreateRoomGame(30588L, "茶苑双扣", "乌龙", 10)));

        assertEquals(2, games.size());
        assertEquals(30580L, games.get(0).gameId());
        assertEquals(30588L, games.get(1).gameId());
    }

    @Test
    public void returnsToLauncherOnlyForAUserDismissalNotASuccessfulTableEntry() {
        assertTrue(CreateRoomEntryPolicy.shouldReturnDirectEntryToLauncher(false));
        assertFalse(CreateRoomEntryPolicy.shouldReturnDirectEntryToLauncher(true));
    }

    @Test
    public void buildsTheOriginalMiniProgramUrlWithThePublicPlayerId() {
        assertEquals(
                "weixin://dl/business/?appid=wx5273ca61ed6c3ede&path=&query="
                        + "sessionFrom%3D1%26gameId%3D30580%26lcc%3Dzjb_7109_boxroom_123456"
                        + "%26lwccss%3Dzjb_7109_boxroom&env_version=release",
                ShiSanZhangMiniProgram.urlFor("123456"));
    }
}
