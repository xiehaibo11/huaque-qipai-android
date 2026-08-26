package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

record MembershipProductsState(List<Product> products) {
    static MembershipProductsState fromJson(String responseText) throws JSONException {
        JSONArray body = new JSONArray(responseText);
        List<Product> products = new ArrayList<>(body.length());
        for (int index = 0; index < body.length(); index++) {
            JSONObject product = body.getJSONObject(index);
            products.add(
                    new Product(
                            product.optString("productCode"),
                            product.optString("name"),
                            product.optLong("amountMinor"),
                            product.optString("currency"),
                            product.optInt("durationDays"),
                            product.optInt("giftValueYuan"),
                            product.optString("priceText"),
                            product.optString("dayCostText"),
                            product.optString("cardStyle"),
                            product.optString("cornerTag"),
                            product.optBoolean("subscription"),
                            rewards(product.optJSONArray("rewards"))));
        }
        return new MembershipProductsState(List.copyOf(products));
    }

    private static List<Reward> rewards(JSONArray body) throws JSONException {
        if (body == null) {
            return List.of();
        }
        List<Reward> rewards = new ArrayList<>(body.length());
        for (int index = 0; index < body.length(); index++) {
            JSONObject reward = body.getJSONObject(index);
            rewards.add(
                    new Reward(
                            reward.optString("code"),
                            reward.optString("displayName"),
                            reward.optLong("quantity"),
                            reward.optString("countText"),
                            reward.optString("iconKey")));
        }
        return List.copyOf(rewards);
    }

    record Product(
            String productCode,
            String name,
            long amountMinor,
            String currency,
            int durationDays,
            int giftValueYuan,
            String priceText,
            String dayCostText,
            String cardStyle,
            String cornerTag,
            boolean subscription,
            List<Reward> rewards) {}

    record Reward(
            String code,
            String displayName,
            long quantity,
            String countText,
            String iconKey) {}
}
