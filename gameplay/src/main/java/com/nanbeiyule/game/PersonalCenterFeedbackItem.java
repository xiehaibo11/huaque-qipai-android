package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** One authenticated feedback-history entry returned by the first-party API. */
record PersonalCenterFeedbackItem(
        String id,
        Category category,
        String content,
        String status,
        String createdAt) {
    enum Category {
        FEEDBACK,
        REPORT
    }

    static PersonalCenterFeedbackItem fromJson(JSONObject body)
            throws JSONException {
        return new PersonalCenterFeedbackItem(
                required(body, "id"),
                Category.valueOf(required(body, "category")),
                required(body, "content"),
                required(body, "status"),
                required(body, "createdAt"));
    }

    static List<PersonalCenterFeedbackItem> fromJson(JSONArray body)
            throws JSONException {
        List<PersonalCenterFeedbackItem> items =
                new ArrayList<>(body.length());
        for (int index = 0; index < body.length(); index++) {
            items.add(fromJson(body.getJSONObject(index)));
        }
        return List.copyOf(items);
    }

    private static String required(JSONObject body, String field)
            throws JSONException {
        String value = body.getString(field).trim();
        if (value.isEmpty()) {
            throw new JSONException(field + " must not be blank");
        }
        return value;
    }
}
