package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

record FortuneState(
        Wallet wallet,
        int wealthPoints,
        int luckPoints,
        List<PrayerProduct> prayerProducts,
        List<TreasureProduct> treasureProducts,
        List<CaishenProduct> caishenProducts,
        List<Treasure> treasures,
        String caishenExpiresAt,
        long caishenRemainingSeconds,
        long treasureOneDrawPriceDiamonds,
        long treasureFiveDrawPriceDiamonds,
        int treasureFiveDrawDiscountTenths) {
    record Wallet(long roomCards, long coins, long diamonds, long coupons) {
        static Wallet fromJson(JSONObject json) {
            return new Wallet(
                    json.optLong("roomCards", 0),
                    json.optLong("coins", 0),
                    json.optLong("diamonds", 0),
                    json.optLong("coupons", 0));
        }
    }

    record PrayerProduct(
            String productCode,
            String name,
            long priceDiamonds,
            int wealthPoints,
            int luckPoints) {
        static PrayerProduct fromJson(JSONObject json) {
            return new PrayerProduct(
                    json.optString("productCode", ""),
                    json.optString("name", ""),
                    json.optLong("priceDiamonds", 0),
                    json.optInt("wealthPoints", 0),
                    json.optInt("luckPoints", 0));
        }
    }

    record TreasureProduct(
            String treasureCode, String name, String quality, int fortuneScore) {
        static TreasureProduct fromJson(JSONObject json) {
            return new TreasureProduct(
                    json.optString("treasureCode", ""),
                    json.optString("name", ""),
                    json.optString("quality", ""),
                    json.optInt("fortuneScore", 0));
        }
    }

    record CaishenProduct(
            String productCode,
            String name,
            long priceDiamonds,
            long durationSeconds) {
        static CaishenProduct fromJson(JSONObject json) {
            return new CaishenProduct(
                    json.optString("productCode", ""),
                    json.optString("name", ""),
                    json.optLong("priceDiamonds", 0),
                    json.optLong("durationSeconds", 0));
        }
    }

    record Treasure(
            String treasureCode,
            String name,
            String quality,
            int fortuneScore,
            int level,
            String expiresAt,
            long remainingSeconds) {
        static Treasure fromJson(JSONObject json) {
            return new Treasure(
                    json.optString("treasureCode", ""),
                    json.optString("name", ""),
                    json.optString("quality", ""),
                    json.optInt("fortuneScore", 0),
                    json.optInt("level", 0),
                    json.optString("expiresAt", ""),
                    json.optLong("remainingSeconds", 0));
        }
    }

    FortuneState {
        wallet = wallet == null ? new Wallet(0, 0, 0, 0) : wallet;
        prayerProducts = List.copyOf(prayerProducts == null ? List.of() : prayerProducts);
        treasureProducts = List.copyOf(treasureProducts == null ? List.of() : treasureProducts);
        caishenProducts = List.copyOf(caishenProducts == null ? List.of() : caishenProducts);
        treasures = List.copyOf(treasures == null ? List.of() : treasures);
        caishenExpiresAt = caishenExpiresAt == null ? "" : caishenExpiresAt;
    }

    static FortuneState fromJson(JSONObject json) throws JSONException {
        return new FortuneState(
                Wallet.fromJson(json.optJSONObject("wallet") == null
                        ? new JSONObject() : json.getJSONObject("wallet")),
                json.optInt("wealthPoints", 0),
                json.optInt("luckPoints", 0),
                prayers(json.optJSONArray("prayerProducts")),
                treasureProducts(json.optJSONArray("treasureProducts")),
                caishenProducts(json.optJSONArray("caishenProducts")),
                treasures(json.optJSONArray("treasures")),
                json.optString("caishenExpiresAt", ""),
                json.optLong("caishenRemainingSeconds", 0),
                json.optLong("treasureOneDrawPriceDiamonds", 100),
                json.optLong("treasureFiveDrawPriceDiamonds", 450),
                json.optInt("treasureFiveDrawDiscountTenths", 9));
    }

    private static List<PrayerProduct> prayers(JSONArray array) throws JSONException {
        List<PrayerProduct> values = new ArrayList<>();
        if (array != null) for (int i = 0; i < array.length(); i++) {
            values.add(PrayerProduct.fromJson(array.getJSONObject(i)));
        }
        return values;
    }

    private static List<TreasureProduct> treasureProducts(JSONArray array) throws JSONException {
        List<TreasureProduct> values = new ArrayList<>();
        if (array != null) for (int i = 0; i < array.length(); i++) {
            values.add(TreasureProduct.fromJson(array.getJSONObject(i)));
        }
        return values;
    }

    private static List<CaishenProduct> caishenProducts(JSONArray array) throws JSONException {
        List<CaishenProduct> values = new ArrayList<>();
        if (array != null) for (int i = 0; i < array.length(); i++) {
            values.add(CaishenProduct.fromJson(array.getJSONObject(i)));
        }
        return values;
    }

    private static List<Treasure> treasures(JSONArray array) throws JSONException {
        List<Treasure> values = new ArrayList<>();
        if (array != null) for (int i = 0; i < array.length(); i++) {
            values.add(Treasure.fromJson(array.getJSONObject(i)));
        }
        return values;
    }
}
