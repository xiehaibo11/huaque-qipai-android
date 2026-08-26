package com.nanbeiyule.game;

import java.time.Instant;
import org.json.JSONException;
import org.json.JSONObject;

/** One friend row returned by the first-party friend list API. */
record FriendEntry(
        long publicPlayerId,
        String displayName,
        String avatarKey,
        State state,
        String lastActiveAt,
        boolean shielded,
        int chairCount,
        int userCount,
        int roomId,
        long gameId,
        boolean inTea) {
    enum State {
        ONLINE,
        GAMING,
        WAITING,
        OFFLINE
    }

    FriendEntry(
            long publicPlayerId,
            String displayName,
            String avatarKey,
            State state,
            String lastActiveAt,
            boolean shielded) {
        this(
                publicPlayerId,
                displayName,
                avatarKey,
                state,
                lastActiveAt,
                shielded,
                0,
                0,
                0,
                0L,
                false);
    }

    static FriendEntry fromJson(JSONObject json) throws JSONException {
        long publicPlayerId =
                FriendApiProtocol.requiredLongAny(
                        json, "publicPlayerId", "numid");
        String displayName =
                FriendApiProtocol.optionalStringAny(
                        json, "displayName", "nickname");
        String avatarKey =
                FriendApiProtocol.optionalStringAny(
                        json, "avatarKey", "headurl");
        State state = parseState(json);
        String lastActiveAt =
                FriendApiProtocol.optionalStringAny(
                        json, "lastActiveAt", "last_login_time");
        boolean shielded =
                json.optBoolean("shielded", false)
                        || json.optInt("shieldState", 0) != 0;
        return new FriendEntry(
                publicPlayerId,
                displayName,
                avatarKey,
                state,
                lastActiveAt,
                shielded,
                json.optInt("chair_count", 0),
                json.optInt("user_count", 0),
                json.optInt("roomid", 0),
                json.optLong("gameid", 0L),
                json.optBoolean("bInTea", false)
                        || json.optBoolean("inTea", false));
    }

    private static State parseState(JSONObject json) {
        String text = json.optString("state", "");
        if (!text.isBlank()) {
            try {
                return State.valueOf(text);
            } catch (IllegalArgumentException ignored) {
                // Fall through to the original numeric player_state.
            }
        }
        return switch (json.optInt("player_state", 1)) {
            case 2 -> State.GAMING;
            case 4, 99 -> State.ONLINE;
            case 8 -> State.WAITING;
            default -> State.OFFLINE;
        };
    }

    /** Parses the server ISO-8601 timestamp; returns null when unparsable. */
    static Long lastActiveAtMillis(String lastActiveAt) {
        if (lastActiveAt == null || lastActiveAt.isBlank()) {
            return null;
        }
        String trimmed = lastActiveAt.trim();
        if (trimmed.matches("\\d+")) {
            try {
                return Long.parseLong(trimmed) * 1000L;
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        try {
            return Instant.parse(trimmed).toEpochMilli();
        } catch (RuntimeException ignored) {
            // Fall through to the offset-date-time form.
        }
        try {
            return java.time.OffsetDateTime.parse(trimmed)
                    .toInstant()
                    .toEpochMilli();
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
