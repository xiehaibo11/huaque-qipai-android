package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

record ShopPurchaseResult(
        String purchaseId,
        String productCode,
        String status,
        boolean duplicate,
        ShopWalletState wallet) {
    static ShopPurchaseResult fromJson(JSONObject body) throws JSONException {
        return new ShopPurchaseResult(
                body.getString("purchaseId"),
                body.getString("productCode"),
                body.getString("status"),
                body.optBoolean("duplicate", false),
                ShopCatalogResult.wallet(body.getJSONObject("wallet")));
    }
}
