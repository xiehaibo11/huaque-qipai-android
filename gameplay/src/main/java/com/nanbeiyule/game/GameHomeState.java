package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Immutable, validated representation of the authenticated game-home response.
 *
 * <p>The Android client does not invent balances, player identities, regions, or entries. Every
 * value rendered by {@link GameHomeView} must pass through this parser from the real backend
 * response.
 */
public record GameHomeState(
        Player player,
        Wallet wallet,
        Region region,
        List<Entry> entries,
        List<Announcement> announcements) {

    public GameHomeState {
        entries = List.copyOf(entries);
        announcements = List.copyOf(announcements);
    }

    public GameHomeState(
            Player player,
            Wallet wallet,
            Region region,
            List<Entry> entries) {
        this(player, wallet, region, entries, List.of());
    }

    public record Player(
            String userId,
            long publicPlayerId,
            String displayName,
            String avatarKey,
            int membershipLevel) {}

    public record Wallet(long roomCards, long coins, long diamonds) {}

    public record Region(long lobbyId, String areaName) {}

    public record Announcement(String content) {}

    /**
     * 大厅入口。{@code bubbleText}、{@code bubbleType}、{@code bubbleIntervalSeconds}
     * 对应原版游戏卡片 hall_tip_type_2 节点的服务端气泡配置，可空表示不展示气泡。
     */
    public record Entry(
            String code,
            String displayName,
            String entryType,
            String route,
            String iconKey,
            int sortOrder,
            boolean enabled,
            String bubbleText,
            Integer bubbleType,
            Integer bubbleIntervalSeconds) {}

    public static GameHomeState fromJson(JSONObject body) throws JSONException {
        JSONObject playerBody = body.getJSONObject("player");
        Player player =
                new Player(
                        requiredString(playerBody, "userId"),
                        positiveLong(playerBody, "publicPlayerId"),
                        requiredString(playerBody, "displayName"),
                        requiredString(playerBody, "avatarKey"),
                        nonNegativeInt(playerBody, "membershipLevel"));

        JSONObject walletBody = body.getJSONObject("wallet");
        Wallet wallet =
                new Wallet(
                        nonNegativeLong(walletBody, "roomCards"),
                        nonNegativeLong(walletBody, "coins"),
                        nonNegativeLong(walletBody, "diamonds"));

        JSONObject regionBody = body.getJSONObject("region");
        Region region =
                new Region(
                        positiveLong(regionBody, "lobbyId"),
                        requiredString(regionBody, "areaName"));

        JSONArray entryBodies = body.getJSONArray("entries");
        if (entryBodies.length() == 0) {
            throw new JSONException("entries must not be empty");
        }
        List<Entry> entries = new ArrayList<>(entryBodies.length());
        for (int index = 0; index < entryBodies.length(); index++) {
            JSONObject entryBody = entryBodies.getJSONObject(index);
            entries.add(
                    new Entry(
                            requiredString(entryBody, "code"),
                            requiredString(entryBody, "displayName"),
                            requiredString(entryBody, "entryType"),
                            requiredString(entryBody, "route"),
                            requiredString(entryBody, "iconKey"),
                            nonNegativeInt(entryBody, "sortOrder"),
                            entryBody.getBoolean("enabled"),
                            optionalString(entryBody, "bubbleText"),
                            optionalInt(entryBody, "bubbleType"),
                            optionalInt(entryBody, "bubbleIntervalSeconds")));
        }

        JSONArray announcementBodies = body.optJSONArray("announcements");
        List<Announcement> announcements = new ArrayList<>();
        if (announcementBodies != null) {
            for (int index = 0; index < announcementBodies.length(); index++) {
                announcements.add(
                        new Announcement(
                                requiredString(
                                        announcementBodies.getJSONObject(index),
                                        "content")));
            }
        }
        return new GameHomeState(player, wallet, region, entries, announcements);
    }

    /** 旧版服务端不下发气泡字段，缺失与 null 都按“无气泡”处理。 */
    private static String optionalString(JSONObject body, String field) {
        if (!body.has(field) || body.isNull(field)) {
            return null;
        }
        String value = body.optString(field, "").trim();
        return value.isEmpty() ? null : value;
    }

    private static Integer optionalInt(JSONObject body, String field) {
        if (!body.has(field) || body.isNull(field)) {
            return null;
        }
        return body.optInt(field);
    }

    private static String requiredString(JSONObject body, String field) throws JSONException {
        String value = body.getString(field).trim();
        if (value.isEmpty()) {
            throw new JSONException(field + " must not be blank");
        }
        return value;
    }

    private static long positiveLong(JSONObject body, String field) throws JSONException {
        long value = body.getLong(field);
        if (value <= 0L) {
            throw new JSONException(field + " must be positive");
        }
        return value;
    }

    private static long nonNegativeLong(JSONObject body, String field) throws JSONException {
        long value = body.getLong(field);
        if (value < 0L) {
            throw new JSONException(field + " must not be negative");
        }
        return value;
    }

    private static int nonNegativeInt(JSONObject body, String field) throws JSONException {
        int value = body.getInt(field);
        if (value < 0) {
            throw new JSONException(field + " must not be negative");
        }
        return value;
    }
}
