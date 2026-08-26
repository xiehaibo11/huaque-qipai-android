package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ShopRuntimeLayoutTest {
    private static final float EPSILON = 0.01f;

    @Test
    public void alignsThreeRecommendationCardsWithOriginalContentEdge() {
        ShopLayout.Rect first =
                ShopRuntimeLayout.productCard(
                        ShopCategory.HOT_RECOMMENDATION, 0, 0f);
        ShopLayout.Rect second =
                ShopRuntimeLayout.productCard(
                        ShopCategory.HOT_RECOMMENDATION, 1, 0f);
        ShopLayout.Rect third =
                ShopRuntimeLayout.productCard(
                        ShopCategory.HOT_RECOMMENDATION, 2, 0f);

        assertEquals(369.936005f, first.left(), EPSILON);
        assertEquals(829.936005f, first.right(), EPSILON);
        assertEquals(869.936005f, second.left(), EPSILON);
        assertEquals(1369.936005f, third.left(), EPSILON);
        assertEquals(1829.936005f, third.right(), EPSILON);
    }

    @Test
    public void resolvesBothHotRecommendationSecondLevelTabs() {
        assertEquals(0, ShopRuntimeLayout.hotSectionIndexAt(300f, 200f));
        assertEquals(1, ShopRuntimeLayout.hotSectionIndexAt(300f, 400f));
        assertEquals(-1, ShopRuntimeLayout.hotSectionIndexAt(500f, 400f));
    }

    @Test
    public void resolvesAllDecorationSecondLevelTabs() {
        assertEquals(0, ShopRuntimeLayout.decorationSectionIndexAt(300f, 200f));
        assertEquals(1, ShopRuntimeLayout.decorationSectionIndexAt(300f, 400f));
        assertEquals(2, ShopRuntimeLayout.decorationSectionIndexAt(300f, 650f));
        assertEquals(3, ShopRuntimeLayout.decorationSectionIndexAt(300f, 850f));
        assertEquals(4, ShopRuntimeLayout.decorationSectionIndexAt(300f, 1000f));
        assertEquals(
                ShopLayout.PAGE_HEIGHT,
                ShopRuntimeLayout.decorationSectionRow(4).bottom(),
                0.01f);
        assertEquals(-1, ShopRuntimeLayout.decorationSectionIndexAt(500f, 850f));
    }
}
