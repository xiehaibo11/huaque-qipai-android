package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 台州层听牌域语义消息（BasicTaiZhouMahjong 覆盖/新增）：msgTingMahInfo 台州版、
 * msgTestSingleTingMah、msgAllWaitInfo。来源
 * {@code $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class TaizhouTingMessages {
    private TaizhouTingMessages() {}

    /**
     * {@code msgTingMahInfo} 听牌信息（台州覆盖版）。方向 双向：bistream 解析 +
     * bostream 空实现（不写任何字段，:340-343）。XY_ID = 562。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:326。
     * 字段名与基础层一致；台州层按 0..size-1 循环、计数为 UInt16（:352-364），
     * 与基础层的 1..size / Int16 不同，如实记录。
     *
     * @param sDelMahSize 出哪几张能听的数量 — :328
     * @param nDealMah 出哪几张能听（长度 sDelMahSize） — :329
     * @param sTingSize 每张能听几张（长度 sDelMahSize） — :330
     * @param bUniversalHu 每张是否万能听（长度 sDelMahSize） — :331
     * @param nTingMah 具体听哪几张（每组长度 sTingSize[i]） — :332
     * @param sTingMahCount 每张剩余几张（每组长度 sTingSize[i]） — :333
     * @param sTingMahFan 每张番数（每组长度 sTingSize[i]） — :334
     */
    public record MsgTingMahInfoTZ(int sDelMahSize, int[] nDealMah, int[] sTingSize,
            boolean[] bUniversalHu, int[][] nTingMah, int[][] sTingMahCount,
            int[][] sTingMahFan) {
        public MsgTingMahInfoTZ {
            Objects.requireNonNull(nDealMah, "nDealMah");
            Objects.requireNonNull(sTingSize, "sTingSize");
            Objects.requireNonNull(bUniversalHu, "bUniversalHu");
            Objects.requireNonNull(nTingMah, "nTingMah");
            Objects.requireNonNull(sTingMahCount, "sTingMahCount");
            Objects.requireNonNull(sTingMahFan, "sTingMahFan");
        }
    }

    /**
     * {@code msgTestSingleTingMah} 单吊听牌测试（台州新增；与基础层 msgBaseScore
     * 共用 XY_ID = 1053，原版冲突如实保留）。方向 S→C（仅 bistream）。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:370。
     * 注释与字段语义在 :373-376（原版注释错位，如实保留字段名）。
     *
     * @param nSeat 座位 — :373
     * @param outmah 打出的牌 — :374
     * @param count 可听牌数 — :375
     * @param nMahFan 各听牌番数（长度 count） — :376
     */
    public record MsgTestSingleTingMah(int nSeat, int outmah, int count,
            int[] nMahFan) {
        public MsgTestSingleTingMah {
            Objects.requireNonNull(nMahFan, "nMahFan");
        }
    }

    /**
     * {@code msgAllWaitInfo} 全部听牌信息（台州新增）。方向 S→C（仅 bistream）。
     * XY_ID = 1500。声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:520。
     *
     * @param nOutMahCnt 可打出牌数 — :523
     * @param bShowFanNum 是否显示番数 — :524
     * @param bShowHuNum 是否显示胡数 — :525
     * @param waitMahInfo 每张打出牌的听牌信息（长度 nOutMahCnt，
     *        元素见 {@link WaitMahEntry}） — :526
     */
    public record MsgAllWaitInfo(int nOutMahCnt, boolean bShowFanNum,
            boolean bShowHuNum, WaitMahEntry[] waitMahInfo) {
        public MsgAllWaitInfo {
            Objects.requireNonNull(waitMahInfo, "waitMahInfo");
        }
    }

    /**
     * msgAllWaitInfo 的单条听牌条目（原版内嵌匿名表，解析
     * $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:541-562）。
     *
     * @param nOutMah 打出的牌 — :543
     * @param nWaitCnt 可听张数（线上紧随 nOutMah 读取，:544） — :545
     * @param nWaitMahs 听的牌（长度 nWaitCnt） — :546
     * @param nLeftCnt 每张剩余数（长度 nWaitCnt） — :547
     * @param nFanPoint 每张番数（长度 nWaitCnt，线上 Int32） — :548
     * @param nHuPoint 每张胡数（长度 nWaitCnt，线上 Int32） — :549
     */
    public record WaitMahEntry(int nOutMah, int nWaitCnt, int[] nWaitMahs,
            int[] nLeftCnt, int[] nFanPoint, int[] nHuPoint) {
        public WaitMahEntry {
            Objects.requireNonNull(nWaitMahs, "nWaitMahs");
            Objects.requireNonNull(nLeftCnt, "nLeftCnt");
            Objects.requireNonNull(nFanPoint, "nFanPoint");
            Objects.requireNonNull(nHuPoint, "nHuPoint");
        }
    }
}
