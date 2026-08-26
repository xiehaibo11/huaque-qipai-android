package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 听牌域语义消息（BasicMahjong 层）：msgTingMahInfo、msgObviousMahsData。来源
 * {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class TingMessages {
    private TingMessages() {}

    /**
     * {@code msgTingMahInfo} 听牌信息（基础层，台州层有覆盖）。方向 S→C（仅 bistream）。
     * XY_ID = 562。声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1907。
     * 基础层按 1..sDelMahSize / 1..sTingSize 循环，计数为 Int16（:1927-1939）。
     *
     * @param sDelMahSize 出哪几张能听的数量 — :1910
     * @param nDealMah 出哪几张能听（长度 sDelMahSize） — :1911
     * @param sTingSize 每张能听几张（长度 sDelMahSize） — :1912
     * @param bUniversalHu 每张是否万能听（长度 sDelMahSize） — :1913
     * @param nTingMah 具体听哪几张（每组长度 sTingSize[i]） — :1914
     * @param sTingMahCount 每张剩余几张（每组长度 sTingSize[i]） — :1915
     * @param sTingMahFan 每张番数（每组长度 sTingSize[i]） — :1916
     */
    public record MsgTingMahInfo(int sDelMahSize, int[] nDealMah, int[] sTingSize,
            boolean[] bUniversalHu, int[][] nTingMah, int[][] sTingMahCount,
            int[][] sTingMahFan) {
        public MsgTingMahInfo {
            Objects.requireNonNull(nDealMah, "nDealMah");
            Objects.requireNonNull(sTingSize, "sTingSize");
            Objects.requireNonNull(bUniversalHu, "bUniversalHu");
            Objects.requireNonNull(nTingMah, "nTingMah");
            Objects.requireNonNull(sTingMahCount, "sTingMahCount");
            Objects.requireNonNull(sTingMahFan, "sTingMahFan");
        }
    }

    /**
     * {@code msgObviousMahsData} 明牌数据。方向 S→C（仅 bistream）。XY_ID = 555。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1250。
     * 原版按 0..nCount 读取后把 mahsData[0] 置 nil（:1265-1279），
     * 实际有效下标为 1..nCount。
     *
     * @param nCount 数量（线上 Int32） — :1253
     * @param mahsData 明牌数据（长度 nCount，元素见 {@link ObviousMahsEntry}） — :1254
     */
    public record MsgObviousMahsData(int nCount, ObviousMahsEntry[] mahsData) {
        public MsgObviousMahsData {
            Objects.requireNonNull(mahsData, "mahsData");
        }
    }

    /**
     * msgObviousMahsData 的单人明牌条目（原版内嵌匿名表，解析
     * $M/BasicMahjong/Protocols/GameProtocol.luac:1266-1277）。
     *
     * @param handMahsCount 手牌张数 — :1267
     * @param handMahs 手牌（长度 handMahsCount） — :1268
     * @param takeMahsCount 抓牌张数 — :1272
     * @param takeMahs 抓牌（长度 takeMahsCount） — :1273
     */
    public record ObviousMahsEntry(int handMahsCount, int[] handMahs,
            int takeMahsCount, int[] takeMahs) {
        public ObviousMahsEntry {
            Objects.requireNonNull(handMahs, "handMahs");
            Objects.requireNonNull(takeMahs, "takeMahs");
        }
    }
}
