package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 动作/权限域语义消息：msgAction、msgHu、msgHuEx、msgPower、msgTWait、
 * msgJustWaiting、msgWaiting、msgEndWait、msgTrust。全部来自 BasicMahjong 层
 * {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class ActionMessages {
    private ActionMessages() {}

    /**
     * {@code msgAction} 吃碰杠动作。方向 双向（bistream + bostream）。XY_ID = 543。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:941。
     *
     * @param nSeat 座位 — :944
     * @param tComb 动作牌组（结构见 {@link MahjongComb}，解析 :957-973） — :945
     * @param nActionID 动作 ID；声明表未列出，仅 bostream 写出（:993），
     *        为原版客户端发送时动态挂载的字段
     */
    public record MsgAction(int nSeat, MahjongComb tComb, int nActionID) {
        public MsgAction {
            Objects.requireNonNull(tComb, "tComb");
        }
    }

    /**
     * {@code msgHu} 胡牌。方向 双向（bistream + bostream）。XY_ID = 541。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:888。
     *
     * @param nSeat 座位 — :891
     * @param nActionID 动作 ID；声明表未列出，仅 bostream 写出（:908），
     *        为原版客户端发送时动态挂载的字段
     */
    public record MsgHu(int nSeat, int nActionID) {}

    /**
     * {@code msgHuEx} 一炮多响。方向 S→C（仅 bistream）。XY_ID = 542。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:913。
     *
     * @param nCount 胡牌人数 — :916
     * @param nSeats 胡牌座位（长度 nCount） — :917
     */
    public record MsgHuEx(int nCount, int[] nSeats) {
        public MsgHuEx {
            Objects.requireNonNull(nSeats, "nSeats");
        }
    }

    /**
     * {@code msgPower} 操作权限。方向 S→C（仅 bistream）。XY_ID = 518。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:226。
     *
     * @param nSeat 座位 — :229
     * @param nPower 权限位 — :230
     * @param nActionID 动作 ID；可选尾字段（:243-245） — :231
     */
    public record MsgPower(int nSeat, int nPower, int nActionID) {}

    /**
     * {@code msgTWait} 等待。方向 双向（bistream + bostream）。XY_ID = 550。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1165。
     *
     * @param nSeat 座位 — :1168
     */
    public record MsgTWait(int nSeat) {}

    /**
     * {@code msgJustWaiting} 正在等待。方向 S→C（仅 bistream）。XY_ID = 551。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1189。
     *
     * @param nSeat 座位 — :1192
     */
    public record MsgJustWaiting(int nSeat) {}

    /**
     * {@code msgWaiting} 等待。方向 S→C（仅 bistream）。XY_ID = 552。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1207。
     *
     * @param nSeat 座位 — :1210
     */
    public record MsgWaiting(int nSeat) {}

    /**
     * {@code msgEndWait} 结束等待。方向 双向（bistream + bostream）。XY_ID = 521。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:295。
     *
     * @param nStepID 阶段 ID — :298
     * @param nSeat 座位 — :299
     */
    public record MsgEndWait(int nStepID, int nSeat) {}

    /**
     * {@code msgTrust} 托管标志。方向 双向（bistream + bostream）。XY_ID = 517。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:199。
     *
     * @param nSeat 座位 — :202
     * @param nFlag 标志 — :203
     */
    public record MsgTrust(int nSeat, int nFlag) {}
}
