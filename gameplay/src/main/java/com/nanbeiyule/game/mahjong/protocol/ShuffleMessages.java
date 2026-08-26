package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 换座域语义消息（BasicMahjong 层）：msgReqShuffle、msgShuffleSeats、
 * msgToTalShuffle、msgShuffleFinish。来源
 * {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class ShuffleMessages {
    private ShuffleMessages() {}

    /**
     * {@code msgReqShuffle} 请求换座。方向 双向（bistream + bostream）。XY_ID = 559。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1802。
     *
     * @param sSeat 座位 — :1805
     */
    public record MsgReqShuffle(int sSeat) {}

    /**
     * {@code msgShuffleSeats} 换座。方向 双向（bistream + bostream）。XY_ID = 560。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1826。
     *
     * @param sSeat 座位 — :1829
     */
    public record MsgShuffleSeats(int sSeat) {}

    /**
     * {@code msgToTalShuffle} 全部换座结果（字段名为原版拼写）。方向 双向。
     * XY_ID = 563。声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1850。
     * bistream 按下标 0..shuffleNum-1 读取（:1875-1877）。
     *
     * @param shuffleNum 换座数量 — :1854
     * @param shuffleSeats 换座座位（长度 shuffleNum） — :1855
     */
    public record MsgToTalShuffle(int shuffleNum, int[] shuffleSeats) {
        public MsgToTalShuffle {
            Objects.requireNonNull(shuffleSeats, "shuffleSeats");
        }
    }

    /**
     * {@code msgShuffleFinish} 换座完成。方向 双向（bistream + bostream）。XY_ID = 561。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1883。线上为 Bool。
     *
     * @param bFlag 标志 — :1886
     */
    public record MsgShuffleFinish(boolean bFlag) {}
}
