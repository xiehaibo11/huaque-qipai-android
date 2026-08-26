package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 桌面状态域语义消息（BasicMahjong 层）：msgPlayerTimer、msgRelinkEnter、
 * msgLookerEnter、msgGameStep、msgClock、msgPanData、msgTurnData、msgBaseScore、
 * msgServicePay。来源 {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class TableStateMessages {
    private TableStateMessages() {}

    /**
     * {@code msgPlayerTimer} 玩家定时器。方向 S→C（仅 bistream）。XY_ID = 9。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:3。
     * 原版 bistream 把 nPower 读了两次、从未读 nSeat（:25-27），为原版线上
     * 实现的既存怪癖，如实记录。
     *
     * @param nBrandID 品牌ID — :7
     * @param nNumberID 数字ID — :8
     * @param nSecond 定时器时间（单位秒） — :11
     * @param nPower 定时器权限 — :12
     * @param nSeat 定时器位置（声明存在但 bistream 未读取） — :13
     */
    public record MsgPlayerTimer(int nBrandID, int nNumberID, int nSecond,
            int nPower, int nSeat) {}

    /**
     * {@code msgRelinkEnter} 断线重连进入。方向 S→C（仅 bistream）。XY_ID = 515。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:163。
     *
     * @param nFlag 标志 — :166
     */
    public record MsgRelinkEnter(int nFlag) {}

    /**
     * {@code msgLookerEnter} 旁观者进入。方向 S→C（仅 bistream）。XY_ID = 516。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:181。
     *
     * @param nFlag 标志 — :184
     */
    public record MsgLookerEnter(int nFlag) {}

    /**
     * {@code msgGameStep} 游戏阶段。方向 S→C（仅 bistream）。XY_ID = 519。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:250。
     *
     * @param nStepID 阶段 ID — :253
     */
    public record MsgGameStep(int nStepID) {}

    /**
     * {@code msgClock} 座位时钟。方向 双向（bistream + bostream）。XY_ID = 520。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:268。
     *
     * @param nSeat 座位 — :271
     * @param nTime 时间 — :272
     */
    public record MsgClock(int nSeat, int nTime) {}

    /**
     * {@code msgPanData} 盘/圈/局数据。方向 S→C（仅 bistream）。XY_ID = 544。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:999。
     *
     * @param nPanNum 盘数 — :1002
     * @param nQuanNum 圈数 — :1003
     * @param nJuNum 局数 — :1004
     * @param nFirstBanker 第一个庄家座位 — :1005
     * @param nQuanFeng 圈风 — :1006
     * @param nBanker 庄家座位 — :1007
     * @param nLaoZhuang 牢庄 — :1008
     * @param nBaseScore 底 — :1009
     */
    public record MsgPanData(int nPanNum, int nQuanNum, int nJuNum, int nFirstBanker,
            int nQuanFeng, int nBanker, int nLaoZhuang, int nBaseScore) {}

    /**
     * {@code msgTurnData} 回合数据。方向 S→C（仅 bistream）。XY_ID = 545。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1031。
     *
     * @param nStateCount 状态个数 — :1034
     * @param nStates 状态（长度 nStateCount） — :1035
     * @param nJustReplaced 刚刚补的牌 — :1036
     * @param nJustPlayed 刚刚出的牌 — :1037
     * @param nJustTook 刚刚抓的牌 — :1038
     * @param nJustKong 刚刚杠的牌 — :1039
     * @param nJustIndex 刚刚抓的牌的索引 — :1040
     * @param nJustSeat 刚刚出牌的座位 — :1041
     * @param nWhoPlay 当前权限座位 — :1042
     */
    public record MsgTurnData(int nStateCount, int[] nStates, int nJustReplaced,
            int nJustPlayed, int nJustTook, int nJustKong, int nJustIndex,
            int nJustSeat, int nWhoPlay) {
        public MsgTurnData {
            Objects.requireNonNull(nStates, "nStates");
        }
    }

    /**
     * {@code msgBaseScore} 底分与倍率。方向 S→C（仅 bistream）。XY_ID = 1053，
     * 与台州层 msgTestSingleTingMah 同号（原版冲突，如实保留）。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:88。
     *
     * @param nBaseScore 底分 — :91
     * @param nBaseDi 倍率 — :92
     */
    public record MsgBaseScore(int nBaseScore, int nBaseDi) {}

    /**
     * {@code msgServicePay} 服务费。方向 S→C（仅 bistream）。XY_ID = 1033。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1404。
     *
     * @param nServicePay 服务费 — :1407
     */
    public record MsgServicePay(int nServicePay) {}
}
