package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

record MembershipNoticeResult(MembershipNotice notice) {
    static MembershipNoticeResult fromJson(JSONObject body) throws JSONException {
        if (body == null) {
            throw new JSONException("membership notice is missing");
        }
        int version = body.getInt("version");
        if (version <= 0) {
            throw new JSONException("membership notice version is invalid");
        }
        JSONArray array = body.getJSONArray("items");
        List<String> items = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            String item = array.getString(index).trim();
            if (!item.isEmpty()) {
                items.add(item);
            }
        }
        if (items.isEmpty()) {
            throw new JSONException("membership notice items are missing");
        }
        String agreementUrl = body.getString("agreementUrl");
        if (!"https://www.nanbeiyule.com/terms".equals(agreementUrl)) {
            throw new JSONException("membership notice agreement URL is invalid");
        }
        return new MembershipNoticeResult(
                new MembershipNotice(
                        version,
                        body.getString("title"),
                        items,
                        body.getString("changeNotice"),
                        body.getString("agreementTitle"),
                        agreementUrl,
                        body.optString("updatedAt")));
    }
}
