package com.nanbeiyule.game;

import org.json.JSONException;
import org.json.JSONObject;

/** Request body builders and response parsers for the friend API. */
final class FriendApiProtocol {
    private FriendApiProtocol() {}

    static JSONObject applyBody(long publicPlayerId) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("publicPlayerId", publicPlayerId);
        return body;
    }

    static JSONObject shieldBody(boolean shielded) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("shielded", shielded);
        return body;
    }

    static JSONObject inviteBody(String type) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", type);
        return body;
    }

    static FriendListPage parseListPage(String responseText)
            throws JSONException {
        return FriendListPage.fromJson(new JSONObject(responseText));
    }

    static FriendSearchResult parseSearchResult(String responseText)
            throws JSONException {
        return FriendSearchResult.fromJson(new JSONObject(responseText));
    }

    static FriendInviteAllResult parseInviteAllResult(
            String responseText) throws JSONException {
        return FriendInviteAllResult.fromJson(
                new JSONObject(responseText));
    }

    static FriendApplicationsPage parseApplications(String responseText)
            throws JSONException {
        return FriendApplicationsPage.fromJson(
                new JSONObject(responseText));
    }

    static FriendNotificationsPage parseNotifications(String responseText)
            throws JSONException {
        return FriendNotificationsPage.fromJson(
                new JSONObject(responseText));
    }

    /**
     * Reads a required numeric id that the server may encode either as a
     * JSON number or as a decimal string.
     */
    static long requiredLong(JSONObject json, String key)
            throws JSONException {
        String text = json.optString(key, "").trim();
        if (text.isEmpty()) {
            throw new JSONException("Missing numeric field: " + key);
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            throw new JSONException(
                    "Invalid numeric field: " + key);
        }
    }

    static long requiredLongAny(JSONObject json, String primary, String alias)
            throws JSONException {
        if (json.has(primary) && !json.isNull(primary)) {
            return requiredLong(json, primary);
        }
        return requiredLong(json, alias);
    }

    static String optionalStringAny(
            JSONObject json, String primary, String alias) {
        if (json.has(primary) && !json.isNull(primary)) {
            return json.optString(primary, "");
        }
        return json.isNull(alias) ? null : json.optString(alias, null);
    }
}
