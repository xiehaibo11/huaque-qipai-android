package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class ShopPropCatalogTest {
    @Test
    public void washCardSectionShowsOriginalTicketPacks() {
        ShopCatalogState catalog =
                ShopOriginalCatalog.create()
                        .select(ShopCategory.PROP)
                        .selectPropSection(ShopPropSection.WASH_CARD);

        assertEquals(
                List.of("PROP_WASH_CARD_1", "PROP_WASH_CARD_5", "PROP_WASH_CARD_10"),
                catalog.selectedProducts().stream().map(ShopProduct::productCode).toList());
        assertEquals(
                List.of(20L, 90L, 160L),
                catalog.selectedProducts().stream().map(ShopProduct::priceMinor).toList());
    }

    @Test
    public void luckPropSectionShowsLuckBeadPacks() {
        ShopCatalogState catalog =
                ShopOriginalCatalog.create()
                        .select(ShopCategory.PROP)
                        .selectPropSection(ShopPropSection.LUCK_PROP);

        assertEquals(
                List.of("PROP_LUCK_BEAD_1", "PROP_LUCK_BEAD_5", "PROP_LUCK_BEAD_10"),
                catalog.selectedProducts().stream().map(ShopProduct::productCode).toList());
        assertEquals(
                List.of("转运珠1颗", "转运珠5颗", "转运珠10颗"),
                catalog.selectedProducts().stream().map(ShopProduct::displayName).toList());
    }
}
