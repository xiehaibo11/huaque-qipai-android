package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Server-authoritative result of one or five treasure-pot draws. */
record FortuneTreasureDrawResult(
        int count,
        long spentDiamonds,
        List<Draw> draws,
        FortuneState.Wallet wallet,
        boolean replayed) {
    record Draw(
            String treasureCode,
            String name,
            String quality,
            int fortuneScore,
            int level,
            String expiresAt) {
        Draw {
            treasureCode = treasureCode == null ? "" : treasureCode;
            name = name == null ? "" : name;
            quality = quality == null ? "" : quality;
            expiresAt = expiresAt == null ? "" : expiresAt;
        }

        static Draw fromJson(JSONObject json) {
            return new Draw(
                    json.optString("treasureCode", ""),
                    json.optString("name", ""),
                    json.optString("quality", ""),
                    json.optInt("fortuneScore", 0),
                    json.optInt("level", 0),
                    json.optString("expiresAt", ""));
        }
    }

    FortuneTreasureDrawResult {
        draws = List.copyOf(draws == null ? List.of() : draws);
        wallet = wallet == null ? new FortuneState.Wallet(0, 0, 0, 0) : wallet;
    }

    static FortuneTreasureDrawResult fromJson(JSONObject json) throws JSONException {
        JSONArray drawJson = json.optJSONArray("draws");
        List<Draw> draws = new ArrayList<>();
        if (drawJson != null) {
            for (int index = 0; index < drawJson.length(); index++) {
                draws.add(Draw.fromJson(drawJson.getJSONObject(index)));
            }
        }
        JSONObject walletJson = json.optJSONObject("wallet");
        return new FortuneTreasureDrawResult(
                json.optInt("count", 0),
                json.optLong("spentDiamonds", 0),
                draws,
                walletJson == null
                        ? new FortuneState.Wallet(0, 0, 0, 0)
                        : FortuneState.Wallet.fromJson(walletJson),
                json.optBoolean("replayed", false));
    }
}
