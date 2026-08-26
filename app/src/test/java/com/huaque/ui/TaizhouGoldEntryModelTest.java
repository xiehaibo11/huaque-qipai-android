package com.huaque.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TaizhouGoldEntryModelTest {
    @Test
    public void lobbyTileRoutesToGoldGameInsteadOfCreateRoomGame() {
        assertEquals(900023L, TaizhouGoldEntryModel.LOBBY_ID);
        assertEquals(30400L, TaizhouGoldEntryModel.GOLD_GAME_ID);
        assertEquals(30109L, TaizhouGoldEntryModel.BOX_GAME_ID);
        assertEquals("OPEN_TAIZHOU_GOLD", TaizhouGoldEntryModel.ACTION);
    }
}
