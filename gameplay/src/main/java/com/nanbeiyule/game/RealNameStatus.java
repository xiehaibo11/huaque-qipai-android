package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

/** Immutable snapshot of the authenticated real-name verification API. */
record RealNameStatus(
        Status status,
        String realNameMasked,
        String idCardMasked,
        String verifiedAt,
        boolean alipayOneTapEnabled) {

    enum Status {
        VERIFIED,
        UNVERIFIED
    }

    static RealNameStatus fromJson(JSONObject body) throws JSONException {
        String rawStatus = body.getString("status").trim();
        Status status;
        try {
            status = Status.valueOf(rawStatus);
        } catch (IllegalArgumentException exception) {
            throw new JSONException("Unknown real-name status: " + rawStatus);
        }
        String realNameMasked = body.optString("realNameMasked").trim();
        String idCardMasked = body.optString("idCardMasked").trim();
        if (status == Status.VERIFIED
                && (realNameMasked.isEmpty() || idCardMasked.isEmpty())) {
            throw new JSONException(
                    "Verified real-name status must include masked fields");
        }
        return new RealNameStatus(
                status,
                realNameMasked,
                idCardMasked,
                body.optString("verifiedAt").trim(),
                body.optBoolean("alipayOneTapEnabled", false));
    }
}
