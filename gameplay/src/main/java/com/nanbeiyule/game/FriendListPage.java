package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** One page of the friend list response. */
record FriendListPage(
        int page, int size, boolean hasMore, List<FriendEntry> friends) {
    static FriendListPage fromJson(JSONObject json) throws JSONException {
        List<FriendEntry> friends = new ArrayList<>();
        JSONArray array = json.optJSONArray("friends");
        if (array == null) {
            array = json.optJSONArray("friendInfo");
        }
        if (array != null) {
            for (int index = 0; index < array.length(); index++) {
                friends.add(
                        FriendEntry.fromJson(array.getJSONObject(index)));
            }
        }
        int page =
                json.has("page")
                        ? json.optInt("page", 0)
                        : json.optInt("cur_package", 0);
        int size =
                json.has("size")
                        ? json.optInt("size", friends.size())
                        : json.optInt("count", friends.size());
        boolean hasMore =
                json.has("hasMore")
                        ? json.optBoolean("hasMore", false)
                        : json.optInt("total_package", page + 1)
                                > page + 1;
        return new FriendListPage(
                page,
                size,
                hasMore,
                List.copyOf(friends));
    }
}
