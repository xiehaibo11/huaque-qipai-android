package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 台州层流程域语义消息：BasicTaiZhouMahjong 新增的 msgWallCnt、msgShengPaiCnt、
 * msgLeftBanker、msgAdvanceStart、msgReqAdvanceStart、msgAdvanceStartFlag、
 * msgReqAdPlayerAgree，以及 TaiZhouMahjong 叶子层的 msgChengBaoFlag、
 * msgPreBaoPaiMah。来源
 * {@code $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac} 与
 * {@code $M/TaiZhou/TaiZhouMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class TaizhouFlowMessages {
    private TaizhouFlowMessages() {}

    /**
     * {@code msgWallCnt} 牌墙剩余数（台州新增）。方向 S→C（仅 bistream）。
     * XY_ID = 1048。声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:181。
     *
     * @param cnt 数量（线上 UInt16） — :183
     */
    public record MsgWallCnt(int cnt) {}

    /**
     * {@code msgShengPaiCnt} 剩牌数（台州新增；与基础层 msgBetResult 共用
     * XY_ID = 1049，原版冲突如实保留）。方向 S→C（仅 bistream）。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:198。
     *
     * @param cnt 数量（线上 UInt16） — :201
     * @param bFirst 是否首次 — :202
     */
    public record MsgShengPaiCnt(int cnt, boolean bFirst) {}

    /**
     * {@code msgLeftBanker} 剩余庄家数（台州新增）。方向 S→C（仅 bistream）。
     * XY_ID = 1050。声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:277。
     *
     * @param leftBanker 剩余庄家数（线上 UInt16） — :281
     */
    public record MsgLeftBanker(int leftBanker) {}

    /**
     * {@code msgAdvanceStart} 提前开局（2/3 人，台州新增）。方向 S→C（仅 bistream）。
     * XY_ID = 1200。声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:400。
     *
     * @param nSeat 座位 — :403
     * @param nPlayerCnt 玩家数（默认 4） — :404
     */
    public record MsgAdvanceStart(int nSeat, int nPlayerCnt) {}

    /**
     * {@code msgReqAdvanceStart} 请求提前开局（台州新增）。方向 双向
     * （bistream + bostream）。XY_ID = 1201。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:421。
     *
     * @param nSeat 座位 — :424
     * @param nTime 时间 — :425
     */
    public record MsgReqAdvanceStart(int nSeat, int nTime) {}

    /**
     * {@code msgAdvanceStartFlag} 提前开局标志（台州新增）。方向 S→C（仅 bistream）。
     * XY_ID = 1202。声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:448。
     *
     * @param nFlag 标志（线上 Int32） — :451
     */
    public record MsgAdvanceStartFlag(int nFlag) {}

    /**
     * {@code msgReqAdPlayerAgree} 提前开局玩家同意（台州新增）。方向 双向
     * （bistream + bostream）。XY_ID = 1203。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:467。
     *
     * @param nSeat 座位 — :470
     * @param bAgree 是否同意 — :471
     */
    public record MsgReqAdPlayerAgree(int nSeat, boolean bAgree) {}

    /**
     * {@code msgChengBaoFlag} 承包标志（TaiZhouMahjong 叶子层新增）。方向 S→C
     * （仅 bistream）。XY_ID = 1055。
     * 声明 $M/TaiZhou/TaiZhouMahjong/Protocols/GameProtocol.luac:3。
     * 线上固定读取 4 个 UInt8（下标 0..3，:16-18）。
     *
     * @param nChengBaoFlag 四家承包标志 — :5
     */
    public record MsgChengBaoFlag(int[] nChengBaoFlag) {
        public MsgChengBaoFlag {
            Objects.requireNonNull(nChengBaoFlag, "nChengBaoFlag");
        }
    }

    /**
     * {@code msgPreBaoPaiMah} 预报包牌（TaiZhouMahjong 叶子层新增）。方向 S→C
     * （仅 bistream）。XY_ID = 1501。
     * 声明 $M/TaiZhou/TaiZhouMahjong/Protocols/GameProtocol.luac:23。
     *
     * @param nCardCount 包牌张数 — :26
     * @param nBaoPaiMahs 包牌（长度 nCardCount） — :27
     */
    public record MsgPreBaoPaiMah(int nCardCount, int[] nBaoPaiMahs) {
        public MsgPreBaoPaiMah {
            Objects.requireNonNull(nBaoPaiMahs, "nBaoPaiMahs");
        }
    }
}
