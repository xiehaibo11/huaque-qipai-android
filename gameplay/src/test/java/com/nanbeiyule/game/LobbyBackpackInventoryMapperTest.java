package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import org.junit.Test;

public final class LobbyBackpackInventoryMapperTest {
    @Test
    public void serverInventoryIsClassifiedWithoutPeakRaceOrLocalQuantities() {
        List<ShopInventoryItem> inventory =
                List.of(
                        new ShopInventoryItem("PROP_WASH_CARD", 5),
                        new ShopInventoryItem("INTERACTION_ROSE", 2),
                        new ShopInventoryItem("DECORATION_TABLE_3", 7),
                        new ShopInventoryItem("PEAK_RACE_TICKET", 9));

        List<LobbyBackpackEntry> entries =
                LobbyBackpackInventoryMapper.map(inventory, ShopOriginalCatalog.create());

        assertEquals(3, entries.size());
        assertEquals(LobbyBackpackCategory.PROP, entries.get(0).category());
        assertEquals(5, entries.get(0).quantity());
        assertEquals(LobbyBackpackCategory.INTERACTION, entries.get(1).category());
        assertEquals(2, entries.get(1).quantity());
        assertEquals(LobbyBackpackCategory.DECORATION, entries.get(2).category());
        assertEquals(7, entries.get(2).quantity());
        assertFalse(entries.stream().anyMatch(item -> item.itemCode().contains("PEAK_RACE")));
    }

    @Test
    public void unknownServerItemStaysVisibleWithItsRealCode() {
        List<LobbyBackpackEntry> entries =
                LobbyBackpackInventoryMapper.map(
                        List.of(new ShopInventoryItem("SERVER_PROP_42", 3)),
                        ShopCatalogState.empty());

        assertEquals(1, entries.size());
        assertEquals("SERVER_PROP_42", entries.get(0).displayName());
        assertEquals("剩余数量：3", entries.get(0).remainingText());
    }

    @Test
    public void selectedCategoryFiltersTheSameRealEntries() {
        LobbyBackpackState state =
                LobbyBackpackState.ready(
                        List.of(
                                LobbyBackpackEntry.counted(
                                        "P", LobbyBackpackCategory.PROP, "P", "", 1),
                                LobbyBackpackEntry.counted(
                                        "I", LobbyBackpackCategory.INTERACTION, "I", "", 2)));

        assertEquals(2, state.visibleEntries().size());
        assertEquals(1, state.selectCategory(LobbyBackpackCategory.INTERACTION).visibleEntries().size());
        assertEquals("I", state.selectCategory(LobbyBackpackCategory.INTERACTION)
                .selectedEntry().itemCode());
    }
}
