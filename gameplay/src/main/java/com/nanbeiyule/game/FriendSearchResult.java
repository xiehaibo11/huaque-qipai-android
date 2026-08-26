package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

/** Search result returned by the first-party friend search API. */
record FriendSearchResult(
        long publicPlayerId,
        String displayName,
        String avatarKey,
        Relation relation,
        String lastActiveAt) {
    enum Relation {
        NONE,
        PENDING,
        FRIEND,
        REJECTED
    }

    static FriendSearchResult fromJson(JSONObject json)
            throws JSONException {
        long publicPlayerId =
                FriendApiProtocol.requiredLongAny(
                        json, "publicPlayerId", "numid");
        String displayName =
                FriendApiProtocol.optionalStringAny(
                        json, "displayName", "nickname");
        String avatarKey =
                FriendApiProtocol.optionalStringAny(
                        json, "avatarKey", "headurl");
        Relation relation = relationFromJson(json);
        String lastActiveAt =
                FriendApiProtocol.optionalStringAny(
                        json, "lastActiveAt", "lastLoginTime");
        return new FriendSearchResult(
                publicPlayerId,
                displayName,
                avatarKey,
                relation,
                lastActiveAt);
    }

    private static Relation relationFromJson(JSONObject json) {
        String relationText = json.optString("relation");
        for (Relation candidate : Relation.values()) {
            if (candidate.name().equals(relationText)) {
                return candidate;
            }
        }
        return switch (json.optInt("state", 0)) {
            case 1 -> Relation.PENDING;
            case 2 -> Relation.REJECTED;
            case 3 -> Relation.FRIEND;
            default -> Relation.NONE;
        };
    }
}
