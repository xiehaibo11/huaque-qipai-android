package com.huaque.ui.friend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class FriendJson {
    public static FriendData.Page parsePage(String json) {
        JSONObject root = object(json);
        JSONArray values = root.optJSONArray("friends");
        if (values == null) values = root.optJSONArray("friendInfo");
        List<FriendData.Entry> friends = new ArrayList<>();
        if (values != null) {
            for (int i = 0; i < values.length(); i++) {
                friends.add(entry(values.optJSONObject(i)));
            }
        }
        boolean hasMore = root.has("hasMore")
                ? root.optBoolean("hasMore")
                : root.optInt("cur_package") + 1 < root.optInt("total_package");
        return new FriendData.Page(friends, hasMore);
    }

    public static FriendData.SearchResult parseSearch(String json) {
        JSONObject value = object(json);
        return new FriendData.SearchResult(
                playerId(value), text(value, "displayName", "nickname"),
                text(value, "avatarKey", "headurl"), relation(value));
    }

    public static List<FriendData.Application> parseApplications(String json) {
        JSONArray values = object(json).optJSONArray("applications");
        List<FriendData.Application> applications = new ArrayList<>();
        if (values == null) return applications;
        for (int i = 0; i < values.length(); i++) {
            JSONObject value = required(values.optJSONObject(i), "application");
            applications.add(new FriendData.Application(
                    value.optString("id", ""), playerId(value),
                    text(value, "displayName", "nickname"),
                    text(value, "avatarKey", "headurl")));
        }
        return applications;
    }

    public static List<FriendData.Notification> parseNotifications(String json) {
        JSONArray values = object(json).optJSONArray("notifications");
        List<FriendData.Notification> notifications = new ArrayList<>();
        if (values == null) return notifications;
        for (int i = 0; i < values.length(); i++) {
            JSONObject value = required(values.optJSONObject(i), "notification");
            notifications.add(new FriendData.Notification(
                    value.optString("id", ""), value.optLong("actorPublicPlayerId"),
                    value.optString("actorDisplayName", ""), value.optString("type", "")));
        }
        return notifications;
    }

    public static FriendData.InviteAllResult parseInviteAll(String json) {
        JSONObject value = object(json);
        return new FriendData.InviteAllResult(
                value.optInt("invitedCount"), value.optInt("cooldownSkippedCount"));
    }

    private static FriendData.Entry entry(JSONObject value) {
        value = required(value, "friend");
        return new FriendData.Entry(
                playerId(value), text(value, "displayName", "nickname"),
                text(value, "avatarKey", "headurl"), presence(value),
                lastActive(value), value.has("shielded")
                        ? value.optBoolean("shielded") : value.optInt("shieldState") == 1);
    }

    private static FriendData.Presence presence(JSONObject value) {
        String state = value.optString("state", "");
        if (!state.isEmpty()) {
            try {
                return FriendData.Presence.valueOf(state);
            } catch (IllegalArgumentException ignored) {
                return FriendData.Presence.OFFLINE;
            }
        }
        switch (value.optInt("player_state")) {
            case 4: return FriendData.Presence.ONLINE;
            case 2: return FriendData.Presence.GAMING;
            case 8: return FriendData.Presence.WAITING;
            default: return FriendData.Presence.OFFLINE;
        }
    }

    private static String relation(JSONObject value) {
        if (value.has("relation")) return value.optString("relation", "NONE");
        switch (value.optInt("state")) {
            case 1: return "PENDING";
            case 2: return "REJECTED";
            case 3: return "FRIEND";
            default: return "NONE";
        }
    }

    private static long lastActive(JSONObject value) {
        if (!value.has("lastActiveAt")) return value.optLong("last_login_time");
        try {
            return Instant.parse(value.optString("lastActiveAt")).getEpochSecond();
        } catch (DateTimeParseException ignored) {
            return 0L;
        }
    }

    private static long playerId(JSONObject value) {
        long id = value.has("publicPlayerId")
                ? value.optLong("publicPlayerId") : value.optLong("numid");
        if (id <= 0L) throw new IllegalArgumentException("missing public player id");
        return id;
    }

    private static String text(JSONObject value, String canonical, String alias) {
        String result = value.optString(canonical, "");
        return result.isEmpty() ? value.optString(alias, "") : result;
    }

    private static JSONObject object(String json) {
        try {
            return new JSONObject(json == null ? "" : json);
        } catch (JSONException error) {
            throw new IllegalArgumentException("invalid friend response", error);
        }
    }

    private static JSONObject required(JSONObject value, String name) {
        if (value == null) throw new IllegalArgumentException("invalid " + name);
        return value;
    }

    private FriendJson() {}
}
