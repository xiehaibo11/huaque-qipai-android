package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

/** One incoming friend application row. */
record FriendApplicationItem(
        long id,
        long publicPlayerId,
        String displayName,
        String avatarKey,
        String createdAt) {
    static FriendApplicationItem fromJson(JSONObject json)
            throws JSONException {
        long publicPlayerId =
                FriendApiProtocol.requiredLongAny(
                        json, "publicPlayerId", "numid");
        return new FriendApplicationItem(
                json.has("id") && !json.isNull("id")
                        ? FriendApiProtocol.requiredLong(json, "id")
                        : publicPlayerId,
                publicPlayerId,
                FriendApiProtocol.optionalStringAny(
                        json, "displayName", "nickname"),
                FriendApiProtocol.optionalStringAny(
                        json, "avatarKey", "headurl"),
                json.optString("createdAt", ""));
    }
}
