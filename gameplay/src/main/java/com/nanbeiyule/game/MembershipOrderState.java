package com.nanbeiyule.game;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;

record MembershipOrderState(
        String id,
        String merchantOrderNo,
        String provider,
        long amountMinor,
        String currency,
        String status,
        String providerOrderNo,
        Map<String, String> paymentParameters) {
    MembershipOrderState {
        Map<String, String> safeParameters =
                paymentParameters == null ? Map.of() : paymentParameters;
        paymentParameters = Collections.unmodifiableMap(new LinkedHashMap<>(safeParameters));
    }

    static MembershipOrderState fromJson(JSONObject body) {
        return new MembershipOrderState(
                body.optString("id"),
                body.optString("merchantOrderNo"),
                body.optString("provider"),
                body.optLong("amountMinor"),
                body.optString("currency"),
                body.optString("status"),
                body.optString("providerOrderNo"),
                paymentParameters(body.optJSONObject("paymentParameters")));
    }

    String paymentParameter(String key) {
        return paymentParameters.getOrDefault(key, "");
    }

    private static Map<String, String> paymentParameters(JSONObject parameters) {
        if (parameters == null) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        Iterator<String> keys = parameters.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            result.put(key, parameters.optString(key, ""));
        }
        return result;
    }
}
