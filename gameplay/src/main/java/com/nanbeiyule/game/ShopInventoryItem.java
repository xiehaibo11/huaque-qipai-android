package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

record ShopInventoryItem(String itemCode, long quantity) {
    ShopInventoryItem {
        itemCode = itemCode == null ? "" : itemCode.trim();
        if (itemCode.isEmpty() || quantity <= 0) {
            throw new IllegalArgumentException("invalid shop inventory item");
        }
    }

    static List<ShopInventoryItem> listFromJson(String responseText) throws JSONException {
        JSONArray array = new JSONArray(responseText);
        ArrayList<ShopInventoryItem> result = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject item = array.getJSONObject(index);
            result.add(
                    new ShopInventoryItem(
                            item.getString("itemCode"), item.getLong("quantity")));
        }
        return Collections.unmodifiableList(result);
    }
}
