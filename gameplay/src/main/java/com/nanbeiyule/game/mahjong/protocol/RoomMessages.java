package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 房间域语义消息（BasicMahjong 层）：msgGameRule、msgGameRuleUser、
 * msgRequestDismiss、msgRespondDismiss、msgDismissFlag、msgRoomHostSeat、
 * msgPlayCount、msgStartGame、msgEndGame。来源
 * {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class RoomMessages {
    private RoomMessages() {}

    /**
     * {@code msgGameRule} 游戏规则串。方向 双向（bistream + bostream）。XY_ID = 1037。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1486。
     *
     * @param strGameRule 规则串 — :1489
     */
    public record MsgGameRule(String strGameRule) {
        public MsgGameRule {
            Objects.requireNonNull(strGameRule, "strGameRule");
        }
    }

    /**
     * {@code msgGameRuleUser} 用户规则串。方向 双向（bistream + bostream）。XY_ID = 1100。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1973。
     *
     * @param strRule 规则串 — :1976
     */
    public record MsgGameRuleUser(String strRule) {
        public MsgGameRuleUser {
            Objects.requireNonNull(strRule, "strRule");
        }
    }

    /**
     * {@code msgRequestDismiss} 请求解散（基础层，台州层有覆盖）。方向 双向。
     * XY_ID = 1039。声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1549。
     *
     * @param seat 座位（默认 4） — :1552
     */
    public record MsgRequestDismiss(int seat) {}

    /**
     * {@code msgRespondDismiss} 回应解散。方向 双向（bistream + bostream）。XY_ID = 1040。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1573。
     *
     * @param seat 座位（默认 4） — :1576
     * @param agree 是否同意 — :1577
     */
    public record MsgRespondDismiss(int seat, boolean agree) {}

    /**
     * {@code msgDismissFlag} 解散标志。方向 双向（bistream + bostream）。XY_ID = 1041。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1599。
     *
     * @param flag 标志 — :1602
     */
    public record MsgDismissFlag(int flag) {}

    /**
     * {@code msgRoomHostSeat} 房主座位。方向 双向（bistream + bostream）。XY_ID = 1035。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1442。
     *
     * @param seat 座位（默认 4） — :1445
     */
    public record MsgRoomHostSeat(int seat) {}

    /**
     * {@code msgPlayCount} 已玩局数。方向 S→C（仅 bistream）。XY_ID = 1036。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1466。
     *
     * @param playCount 已玩局数 — :1469
     * @param maxPlayCount 总局数 — :1470
     */
    public record MsgPlayCount(int playCount, int maxPlayCount) {}

    /**
     * {@code msgStartGame} 开局。方向 S→C（仅 bistream）。XY_ID = 513。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:127。
     *
     * @param nFlag 标志 — :130
     */
    public record MsgStartGame(int nFlag) {}

    /**
     * {@code msgEndGame} 结束。方向 S→C（仅 bistream）。XY_ID = 514。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:145。
     *
     * @param nFlag 标志 — :148
     */
    public record MsgEndGame(int nFlag) {}
}
