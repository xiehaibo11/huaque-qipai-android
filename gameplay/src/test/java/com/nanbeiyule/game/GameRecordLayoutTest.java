package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GameRecordLayoutTest {
    @Test
    public void matchesTheRecoveredPlayerBillCsdGeometry() {
        assertEquals(1920f, GameRecordLayout.DESIGN_WIDTH, 0f);
        assertEquals(1080f, GameRecordLayout.DESIGN_HEIGHT, 0f);
        assertTrue(GameRecordLayout.GOLD_TAB.contains(1485f, 74f));
        assertTrue(GameRecordLayout.BATTLE_TAB.contains(1744f, 74f));
        assertTrue(GameRecordLayout.BACK.contains(77f, 51f));
        assertTrue(GameRecordLayout.DATE.contains(270f, 171f));
        assertTrue(GameRecordLayout.GAME.contains(630f, 171f));
        assertTrue(GameRecordLayout.GAME.contains(800f, 171f));
        assertTrue(GameRecordLayout.REFRESH.contains(1843f, 171f));
        assertTrue(GameRecordLayout.MEMBER.contains(960f, 720f));
        assertTrue(GameRecordLayout.REPLAY.contains(1756f, 1034f));
    }
}
