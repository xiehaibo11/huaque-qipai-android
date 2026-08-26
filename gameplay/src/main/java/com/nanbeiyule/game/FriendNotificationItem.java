package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

/** One in-app friend invite/reserve notification row. */
record FriendNotificationItem(
        long id,
        String type,
        long actorPublicPlayerId,
        String actorDisplayName,
        String createdAt) {
    static final String TYPE_INVITE = "INVITE";
    static final String TYPE_RESERVE = "RESERVE";
    static final String TYPE_RECALL = "RECALL";

    static FriendNotificationItem fromJson(JSONObject json)
            throws JSONException {
        return new FriendNotificationItem(
                FriendApiProtocol.requiredLong(json, "id"),
                json.optString("type", ""),
                FriendApiProtocol.requiredLong(
                        json, "actorPublicPlayerId"),
                json.optString("actorDisplayName", ""),
                json.optString("createdAt", ""));
    }
}
