package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Incoming friend application list response. */
record FriendApplicationsPage(
        int total, List<FriendApplicationItem> applications) {
    static FriendApplicationsPage fromJson(JSONObject json)
            throws JSONException {
        List<FriendApplicationItem> applications = new ArrayList<>();
        JSONArray array = json.optJSONArray("applications");
        if (array != null) {
            for (int index = 0; index < array.length(); index++) {
                applications.add(
                        FriendApplicationItem.fromJson(
                                array.getJSONObject(index)));
            }
        }
        return new FriendApplicationsPage(
                json.optInt("total", applications.size()),
                List.copyOf(applications));
    }
}
