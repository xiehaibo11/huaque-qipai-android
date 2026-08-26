package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import com.nanbeiyule.game.goldroom.GoldRoomConf;
import java.util.List;
import org.junit.Test;

public final class GoldRuleGameIdTest {
    @Test
    public void goldEntryUsesItsUnderlyingGameForTheRuleDocument() {
        GoldRoomConf conf =
                new GoldRoomConf(
                        900023L, 30400L, "台州麻将", 30109L, 4, List.of(), false);

        assertEquals(30109L, MainActivityGoldChooseRoomFlow.ruleGameId(conf));
    }

    @Test
    public void gameWithoutAnUnderlyingIdUsesItsEntryId() {
        GoldRoomConf conf =
                new GoldRoomConf(900023L, 30400L, "台州麻将", 0L, 4, List.of(), false);

        assertEquals(30400L, MainActivityGoldChooseRoomFlow.ruleGameId(conf));
    }
}
