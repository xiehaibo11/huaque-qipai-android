package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class ShopGoldGiftCatalogTest {
    @Test
    public void parsesAndFiltersOriginalHotShopSections() throws Exception {
        JSONObject body =
                new JSONObject(
                        """
                        {
                          "wallet":{"roomCards":2,"coins":1000,"diamonds":3,"coupons":0},
                          "products":[
                            {
                              "productCode":"HOT_DAILY_GIFT",
                              "category":"hot_recommendation",
                              "section":"value_recommendation",
                              "displayName":"每日礼包",
                              "iconKey":"coin_gift",
                              "priceCurrency":"CNY",
                              "priceAmount":600,
                              "rewardType":"COIN",
                              "rewardQuantity":78000,
                              "dailyLimit":3,
                              "purchasedToday":1,
                              "remainingPurchases":2,
                              "enabled":true
                            },
                            {
                              "productCode":"GOLD_GIFT_6",
                              "category":"hot_recommendation",
                              "section":"gold_gift",
                              "displayName":"6元金币礼包",
                              "iconKey":"coin_gift",
                              "priceCurrency":"CNY",
                              "priceAmount":600,
                              "rewardType":"COIN",
                              "rewardQuantity":78000,
                              "purchasedToday":0,
                              "enabled":true
                            }
                          ]
                        }
                        """);

        ShopCatalogState catalog = ShopCatalogResult.fromJson(body).catalog();

        assertEquals("HOT_DAILY_GIFT", catalog.selectedProducts().get(0).productCode());
        ShopProduct dailyGift = catalog.selectedProducts().get(0);
        assertEquals(3, dailyGift.dailyLimit().intValue());
        assertEquals(1, dailyGift.purchasedToday());
        assertEquals(2, dailyGift.remainingPurchases().intValue());
        assertTrue(dailyGift.available());

        ShopCatalogState goldGifts = catalog.selectHotSection(ShopHotSection.GOLD_GIFT);
        assertEquals(1, goldGifts.selectedProducts().size());
        assertEquals("GOLD_GIFT_6", goldGifts.selectedProducts().get(0).productCode());
    }

    @Test
    public void soldOutLimitedProductCannotBePurchased() {
        ShopProduct product =
                new ShopProduct(
                        "HOT_DAILY_GIFT",
                        ShopCategory.HOT_RECOMMENDATION,
                        ShopHotSection.VALUE_RECOMMENDATION.id(),
                        "每日礼包",
                        "coin_gift",
                        ShopProduct.Currency.CNY,
                        600,
                        ShopProduct.Reward.COIN,
                        78000,
                        3,
                        null,
                        3,
                        0,
                        0,
                        true);

        assertFalse(product.available());
    }
}
