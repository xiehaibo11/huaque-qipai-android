package com.nanbeiyule.game;

/**
 * 「返场」决策，即原版发现玩家已在房间时把他送回房间的那一步。
 *
 * <p>原版大厅从不因为「已有未结束的房间」把玩家挡在建房页：
 *
 * <ul>
 *   <li>{@code lobby/Modules/Lobby/View.lua:725 showBackBoom()} 按
 *       {@code position.gameID ~= 0} 把同一个入口的贴图在
 *       {@code lobby_title_create_box.png} 与 {@code lobby_title_back_box.png} 之间切换；
 *   <li>{@code lobby/Modules/Lobby/View.lua:794 on_KWA_BTND_CREATE_BACK_BOX_ROOM}
 *       在 {@code position.gameID == 0} 时才打开 {@code CreateBoxRoomView}，否则直接
 *       {@code XH.roomManager:gameStart(param)} 回到房间；
 *   <li>服务端兜底分支 {@code lobby/Modules/Gold/Module.lua:308} 收到
 *       {@code ERROR_INAPPID} 时弹「已在游戏房间中,点击确认返场」，确认后用错误响应自带的坐标返场。
 * </ul>
 *
 * <p>南北娱乐当前接入 30109 台州麻将和 30588 茶苑双扣（乌龙）的原生牌桌；其他玩法仍不伪造返场。
 * 这里显式区分两种情况，不对没有牌桌的玩法伪造返场。
 */
final class RoomReentry {
    enum Origin {
        LOBBY_ENTRY,
        CREATE_CONFLICT
    }

    enum Action {
        OPEN_DIRECTLY,
        CONFIRM_OPEN,
        SHOW_UNAVAILABLE
    }

    /** 原版 {@code ERROR_INAPPID} 分支的确认文案，见 {@code lobby/Modules/Gold/Module.lua:315}。 */
    static final String CONFIRM_MESSAGE = "已在游戏房间中,点击确认返场";

    /** 当前唯一接入原生牌桌的玩法。 */
    static final long TAIZHOU_MAHJONG_GAME_ID = 30109L;
    static final long WULONG_GAME_ID = 30588L;

    private RoomReentry() {}

    /** 是否可以真的把玩家送回牌桌。 */
    static boolean canOpenTable(RoomPlacement placement) {
        return placement != null
                && placement.hasRoom()
                && (placement.gameId() == TAIZHOU_MAHJONG_GAME_ID
                        || placement.gameId() == WULONG_GAME_ID);
    }

    /** 按原版入口来源决定直接返场、确认返场或显示未接入提示。 */
    static Action actionFor(RoomPlacement placement, Origin origin) {
        if (!canOpenTable(placement)) {
            return Action.SHOW_UNAVAILABLE;
        }
        return origin == Origin.CREATE_CONFLICT
                ? Action.CONFIRM_OPEN
                : Action.OPEN_DIRECTLY;
    }

    /**
     * 没有牌桌可返时的说明文案。
     *
     * <p>只陈述房号和已知规则摘要，不声称已经返场，也不提供伪造的进桌入口。
     */
    static String unavailableMessage(RoomPlacement placement) {
        if (placement == null || !placement.hasRoom()) {
            return "您已经在游戏房间中了";
        }
        StringBuilder message =
                new StringBuilder("您在房间 ").append(placement.roomNumber()).append(" 中");
        String rule = placement.gameRuleDisplay();
        if (rule != null && !rule.isBlank()) {
            message.append("（").append(rule).append("）");
        }
        message.append("，该玩法的牌桌尚未接入，暂时无法返场。");
        return message.toString();
    }
}
