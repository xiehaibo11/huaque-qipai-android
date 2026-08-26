package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public final class ZhejiangLobbyHeaderPresentationTest {
    @Test
    public void mapsTheAuthenticatedPlayerSnapshotWithoutSwappingWalletFields() {
        GameHomeState state =
                new GameHomeState(
                        new GameHomeState.Player(
                                "user-1", 1_000_000_001L, "手机用户6092", "avatar_default", 0),
                        new GameHomeState.Wallet(20_000L, 1_000_300L, 0L),
                        new GameHomeState.Region(90_0021L, "台州"),
                        List.of());

        ZhejiangLobbyHeaderPresentation header =
                ZhejiangLobbyHeaderPresentation.from(state);

        assertEquals("手机用户6092", header.displayName());
        assertEquals("ID:1000000001", header.playerId());
        assertEquals("100.03万", header.coins());
        assertEquals("0", header.diamonds());
        assertEquals("2万", header.roomCards());
    }
}
