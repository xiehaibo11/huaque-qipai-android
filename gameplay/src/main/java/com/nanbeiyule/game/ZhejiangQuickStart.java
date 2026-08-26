package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the recovered Zhejiang quick-start's gold, match, and friend-room meanings from real
 * self-built home state only.
 *
 * <p>The old renderer picked the first enabled game-grid entry. That has no relationship to the
 * original {@code lastGameInfo} selection and could enter a game the player never chose, so this
 * resolver accepts an explicit quick-start entry type or the already-supported friend-room route.
 */
final class ZhejiangQuickStart {
    enum Mode {
        GOLD,
        MATCH,
        FRIEND_ROOM,
        UNAVAILABLE
    }

    record Target(
            Mode mode,
            String subtitle,
            GameHomeState.Entry entry,
            String unavailableMessage) {}

    private static final String GOLD_TYPE = "GOLD_QUICK_START";
    private static final String MATCH_TYPE = "MATCH_QUICK_START";
    private static final String FRIEND_ROOM_TYPE = "FRIEND_ROOM_QUICK_START";

    private ZhejiangQuickStart() {}

    static Target resolve(List<GameHomeState.Entry> entries) {
        List<GameHomeState.Entry> explicit = new ArrayList<>();
        if (entries != null) {
            for (GameHomeState.Entry entry : entries) {
                if (entry != null && isExplicitQuickStartType(entry.entryType())) {
                    explicit.add(entry);
                }
            }
        }
        if (explicit.size() > 1) {
            return unavailable("快速开始配置存在冲突");
        }
        if (explicit.size() == 1) {
            return explicitTarget(explicit.get(0));
        }

        List<GameHomeState.Entry> friendRoomEntries = new ArrayList<>();
        if (entries != null) {
            for (GameHomeState.Entry entry : entries) {
                if (isFriendRoomRoute(entry)) {
                    friendRoomEntries.add(entry);
                }
            }
        }
        if (friendRoomEntries.size() == 1) {
            return available(Mode.FRIEND_ROOM, "好友房", friendRoomEntries.get(0));
        }
        if (friendRoomEntries.size() > 1) {
            return unavailable("快速开始配置存在冲突");
        }
        return unavailable("快速开始配置尚未同步");
    }

    private static Target explicitTarget(GameHomeState.Entry entry) {
        if (!entry.enabled()) {
            return unavailable("快速开始入口当前不可用");
        }
        return switch (entry.entryType()) {
            case GOLD_TYPE ->
                    isGoldRoute(entry)
                            ? available(Mode.GOLD, "金币场-" + entry.displayName(), entry)
                            : unavailable("快速开始配置无可用入口");
            case MATCH_TYPE ->
                    isMatchRoute(entry)
                            ? available(Mode.MATCH, "比赛场", entry)
                            : unavailable("快速开始配置无可用入口");
            case FRIEND_ROOM_TYPE ->
                    isFriendRoomRoute(entry)
                            ? available(Mode.FRIEND_ROOM, "好友房", entry)
                            : unavailable("快速开始配置无可用入口");
            default -> unavailable("快速开始配置尚未同步");
        };
    }

    private static boolean isExplicitQuickStartType(String entryType) {
        return GOLD_TYPE.equals(entryType)
                || MATCH_TYPE.equals(entryType)
                || FRIEND_ROOM_TYPE.equals(entryType);
    }

    private static boolean isGoldRoute(GameHomeState.Entry entry) {
        return "TAIZHOU_MAHJONG".equals(entry.code())
                && "game/taizhou-mahjong".equals(entry.route());
    }

    private static boolean isMatchRoute(GameHomeState.Entry entry) {
        return "MATCH".equals(entry.code()) && "match".equals(entry.route());
    }

    private static boolean isFriendRoomRoute(GameHomeState.Entry entry) {
        return entry != null
                && entry.enabled()
                && "CREATE_ROOM".equals(entry.code())
                && "room/create".equals(entry.route())
                && ("PRIMARY".equals(entry.entryType())
                        || FRIEND_ROOM_TYPE.equals(entry.entryType()));
    }

    private static Target available(Mode mode, String subtitle, GameHomeState.Entry entry) {
        return new Target(mode, subtitle, entry, null);
    }

    private static Target unavailable(String message) {
        return new Target(Mode.UNAVAILABLE, "未配置", null, message);
    }
}
