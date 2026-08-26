package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

record ShopCatalogResult(ShopCatalogState catalog, ShopWalletState wallet) {
    static ShopCatalogResult fromJson(JSONObject body) throws JSONException {
        if (body == null) {
            throw new JSONException("shop catalog is missing");
        }
        EnumMap<ShopCategory, List<ShopProduct>> products =
                new EnumMap<>(ShopCategory.class);
        for (ShopCategory category : ShopCategory.ordered()) {
            products.put(category, new ArrayList<>());
        }
        JSONArray array = body.getJSONArray("products");
        for (int index = 0; index < array.length(); index++) {
            JSONObject item = array.getJSONObject(index);
            ShopCategory category = category(item.getString("category"));
            products.get(category).add(
                    new ShopProduct(
                            item.getString("productCode"),
                            category,
                            item.optString(
                                    "section",
                                    category == ShopCategory.HOT_RECOMMENDATION
                                            ? ShopHotSection.VALUE_RECOMMENDATION.id()
                                            : "default"),
                            item.getString("displayName"),
                            item.getString("iconKey"),
                            currency(item.getString("priceCurrency")),
                            item.getLong("priceAmount"),
                            reward(item.getString("rewardType")),
                            item.getLong("rewardQuantity"),
                            nullableInt(item, "dailyLimit"),
                            nullableInt(item, "lifetimeLimit"),
                            item.optLong("purchasedToday", 0),
                            item.optLong("purchasedLifetime", 0),
                            nullableInt(item, "remainingPurchases"),
                            item.optBoolean("enabled", true)));
        }
        return new ShopCatalogResult(
                ShopCatalogState.create(products),
                wallet(body.getJSONObject("wallet")));
    }

    private static Integer nullableInt(JSONObject item, String name) {
        return item.has(name) && !item.isNull(name) ? item.optInt(name) : null;
    }

    static ShopWalletState wallet(JSONObject body) throws JSONException {
        return new ShopWalletState(
                body.getLong("roomCards"),
                body.getLong("coins"),
                body.getLong("diamonds"),
                body.optLong("coupons", 0L));
    }

    private static ShopCategory category(String value) throws JSONException {
        try {
            return ShopCategory.fromId(value);
        } catch (IllegalArgumentException exception) {
            throw new JSONException("unknown shop category: " + value);
        }
    }

    private static ShopProduct.Currency currency(String value) throws JSONException {
        try {
            return ShopProduct.Currency.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new JSONException("unknown shop currency: " + value);
        }
    }

    private static ShopProduct.Reward reward(String value) throws JSONException {
        try {
            return ShopProduct.Reward.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new JSONException("unknown shop reward: " + value);
        }
    }
}
