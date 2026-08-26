package com.nanbeiyule.game.lobbyshare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.GameHomeState;
import org.junit.Test;

public class LobbyShareContentTest {
    @Test
    public void usesCurrentProductDownloadConfigurationAndPlayerIdentity() {
        GameHomeState.Player player =
                new GameHomeState.Player("user-9", 90009L, "小台", "avatar-9", 2);

        LobbyShareContent content =
                LobbyShareContent.create(
                        "南北娱乐", LobbyShareContent.PRODUCTION_DOWNLOAD_URL, player);

        assertEquals("南北娱乐", content.shareTitle());
        assertEquals("小台（ID：90009）", content.shareDescription());
        assertEquals(
                "https://www.nanbeiyule.com/download", content.webpageUrl());
    }

    @Test
    public void doesNotInventPlayerIdentityWhenCurrentPlayerIsUnavailable() {
        LobbyShareContent content =
                LobbyShareContent.create(
                        "南北娱乐", LobbyShareContent.PRODUCTION_DOWNLOAD_URL, null);

        assertEquals("", content.shareDescription());
        assertTrue(content.inviterDisplayName().isEmpty());
        assertTrue(content.inviterPublicPlayerId().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonProductionDownloadHost() {
        LobbyShareContent.create("南北娱乐", "https://example.com/download", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInsecureDownloadUrl() {
        LobbyShareContent.create("南北娱乐", "http://www.nanbeiyule.com/download", null);
    }
}
