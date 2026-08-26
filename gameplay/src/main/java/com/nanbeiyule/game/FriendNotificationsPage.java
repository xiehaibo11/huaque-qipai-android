package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Friend notification list response. */
record FriendNotificationsPage(
        int total, List<FriendNotificationItem> notifications) {
    static FriendNotificationsPage fromJson(JSONObject json)
            throws JSONException {
        List<FriendNotificationItem> notifications = new ArrayList<>();
        JSONArray array = json.optJSONArray("notifications");
        if (array != null) {
            for (int index = 0; index < array.length(); index++) {
                notifications.add(
                        FriendNotificationItem.fromJson(
                                array.getJSONObject(index)));
            }
        }
        return new FriendNotificationsPage(
                json.optInt("total", notifications.size()),
                List.copyOf(notifications));
    }
}
