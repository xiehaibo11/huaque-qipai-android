package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public class ShopDecorationCatalogTest {
    @Test
    public void decorationOpensOnTheOriginalExclusiveVehiclePage() {
        ShopCatalogState catalog =
                ShopOriginalCatalog.create().select(ShopCategory.DECORATION);

        assertEquals(
                List.of(
                        "DECORATION_VEHICLE_150801",
                        "DECORATION_VEHICLE_150802",
                        "DECORATION_VEHICLE_150804",
                        "DECORATION_VEHICLE_150803",
                        "DECORATION_VEHICLE_150808",
                        "DECORATION_VEHICLE_150807",
                        "DECORATION_VEHICLE_150806",
                        "DECORATION_VEHICLE_150805",
                        "DECORATION_VEHICLE_150816"),
                catalog.selectedProducts().stream().map(ShopProduct::productCode).toList());
        assertEquals(
                List.of(
                        "二八大杠7天",
                        "北欧幽灵7天",
                        "暗夜精灵7天",
                        "冰蓝狂啸7天",
                        "红色疾风7天",
                        "极速幻影7天",
                        "跃马风情7天",
                        "英伦领航者7天",
                        "越野家7天"),
                catalog.selectedProducts().stream().map(ShopProduct::displayName).toList());
        assertEquals(300, catalog.selectedProducts().get(0).priceMinor());
        assertEquals(1500, catalog.selectedProducts().get(1).priceMinor());
    }

    @Test
    public void eachOriginalDecorationTabFiltersItsOwnProducts() {
        ShopCatalogState catalog =
                ShopOriginalCatalog.create().select(ShopCategory.DECORATION);

        assertEquals(
                List.of(
                        ShopDecorationSection.VEHICLE,
                        ShopDecorationSection.TABLE,
                        ShopDecorationSection.CARD_BACK,
                        ShopDecorationSection.AVATAR_FRAME,
                        ShopDecorationSection.CARD_PRESS),
                ShopDecorationSection.ordered());
        assertEquals(
                List.of("DECORATION_TABLE_3", "DECORATION_TABLE_5"),
                codes(catalog.selectDecorationSection(ShopDecorationSection.CARD_BACK)));
        assertEquals(
                List.of("DECORATION_TABLE_6", "DECORATION_TABLE_8"),
                codes(catalog.selectDecorationSection(ShopDecorationSection.AVATAR_FRAME)));
        assertEquals(
                List.of("DECORATION_TABLE_9"),
                codes(catalog.selectDecorationSection(ShopDecorationSection.CARD_PRESS)));
    }

    private static List<String> codes(ShopCatalogState catalog) {
        return catalog.selectedProducts().stream().map(ShopProduct::productCode).toList();
    }
}
