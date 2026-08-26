package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 手牌域语义消息：msgPlayerMah、msgPlayerBack、msgJoker、msgReplace、msgTake、
 * msgFlower。全部来自 BasicMahjong 层
 * {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 *
 * <p>"可选尾字段" 指原版 bistream 用 {@code bis:getAvailableSize() > 0} 守卫、
 * 只在剩余缓冲区非空时才读取的扩展字段。
 */
public final class HandMessages {
    private HandMessages() {}

    /**
     * {@code msgPlayerMah} 玩家牌面。方向 S→C（仅 bistream）。XY_ID = 534。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:583。
     *
     * @param nSeat 座位 — :586
     * @param nCombCount 牌组个数 — :587
     * @param tCombs 牌组（结构见 {@link MahjongComb}） — :588
     * @param nHandCount 手牌张数 — :589
     * @param nHands 手牌（长度 nHandCount） — :590
     * @param nDFCount 单放张数 — :591
     * @param nDanFang 单放（nDFCount &gt; 0 时读取，:633-636） — :592
     * @param nHuMah 胡的牌；可选尾字段（:638-640） — :594
     * @param nIndex 索引；可选尾字段（:642-644） — :595
     */
    public record MsgPlayerMah(int nSeat, int nCombCount, MahjongComb[] tCombs,
            int nHandCount, int[] nHands, int nDFCount, int nDanFang, int nHuMah,
            int nIndex) {
        public MsgPlayerMah {
            Objects.requireNonNull(tCombs, "tCombs");
            Objects.requireNonNull(nHands, "nHands");
        }
    }

    /**
     * {@code msgPlayerBack} 玩家牌背。方向 双向（bistream + bostream）。XY_ID = 535。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:650。
     *
     * @param nSeat 座位 — :653
     * @param nCombCount 牌组个数 — :654
     * @param tCombs 牌组 — :655
     * @param nHandCount 手牌张数 — :656
     * @param nDFCount 单放张数 — :657
     * @param nIndex 索引；可选尾字段（:695-697） — :658
     * @param nMahs 手牌（长度 nHandCount）；可选尾字段（:699-704） — :659
     * @param nDanFang 单放；可选尾字段（:706-708） — :660
     */
    public record MsgPlayerBack(int nSeat, int nCombCount, MahjongComb[] tCombs,
            int nHandCount, int nDFCount, int nIndex, int[] nMahs, int nDanFang) {
        public MsgPlayerBack {
            Objects.requireNonNull(tCombs, "tCombs");
            Objects.requireNonNull(nMahs, "nMahs");
        }
    }

    /**
     * {@code msgJoker} 财神及替代牌。方向 S→C（仅 bistream）。XY_ID = 536。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:743。
     *
     * @param nJokerCount 财神张数 — :746
     * @param nJokers 财神（长度 nJokerCount） — :747
     * @param nInstdCount 财神替代张数 — :748
     * @param nInstds 财神替代（长度 nInstdCount） — :749
     */
    public record MsgJoker(int nJokerCount, int[] nJokers, int nInstdCount,
            int[] nInstds) {
        public MsgJoker {
            Objects.requireNonNull(nJokers, "nJokers");
            Objects.requireNonNull(nInstds, "nInstds");
        }
    }

    /**
     * {@code msgReplace} 补花/补牌。方向 双向（bistream + bostream）。XY_ID = 537。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:775。
     *
     * @param nSeat 座位 — :778
     * @param nMah 补的牌值 — :779
     */
    public record MsgReplace(int nSeat, int nMah) {}

    /**
     * {@code msgTake} 抓牌。方向 双向（bistream + bostream）。XY_ID = 538。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:802。
     *
     * @param nSeat 座位 — :805
     * @param nMah 抓的牌值 — :806
     * @param nIndex 抓牌索引；可选尾字段（:821-823） — :808
     * @param isFront 是否正抓；可选尾字段，线上为 UInt8 0/1（:824-826） — :809
     */
    public record MsgTake(int nSeat, int nMah, int nIndex, boolean isFront) {}

    /**
     * {@code msgFlower} 花牌。方向 S→C（仅 bistream）。XY_ID = 546。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1068。
     *
     * @param nSeat 座位 — :1071
     * @param nCount 花牌张数 — :1072
     * @param nMahs 花牌值（长度 nCount） — :1073
     */
    public record MsgFlower(int nSeat, int nCount, int[] nMahs) {
        public MsgFlower {
            Objects.requireNonNull(nMahs, "nMahs");
        }
    }
}
