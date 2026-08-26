package com.nanbeiyule.game;

import org.json.JSONObject;

/** Aggregate result of the batch invite-all friend API call. */
record FriendInviteAllResult(int invitedCount, int cooldownSkippedCount) {
    static FriendInviteAllResult fromJson(JSONObject json) {
        return new FriendInviteAllResult(
                json.optInt("invitedCount", 0),
                json.optInt("cooldownSkippedCount", 0));
    }
}
