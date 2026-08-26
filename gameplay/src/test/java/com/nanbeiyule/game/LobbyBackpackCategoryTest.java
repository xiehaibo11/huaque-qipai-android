package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public final class LobbyBackpackCategoryTest {
    @Test
    public void productionTabsAreExactlyTheRequestedFourWithoutPeakRace() {
        List<String> titles =
                Arrays.stream(LobbyBackpackCategory.values())
                        .map(LobbyBackpackCategory::title)
                        .toList();

        assertEquals(List.of("全部", "道具", "互动", "装扮"), titles);
        assertFalse(titles.contains("巅峰赛"));
    }
}
