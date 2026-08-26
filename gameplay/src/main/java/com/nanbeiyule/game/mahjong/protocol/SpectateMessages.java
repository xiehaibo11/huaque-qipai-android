package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 旁观（specify）域与调试语义消息（BasicMahjong 层）：msgSpecfReq、msgSpecfData、
 * msgSpecfPower、msgSpecfHand、msgSpecfDanFang、msgSpecfWall、msgSpecfEnd、
 * msgTest。来源 {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class SpectateMessages {
    private SpectateMessages() {}

    /**
     * {@code msgSpecfReq} 旁观请求。方向 双向（bistream + bostream）。XY_ID = 522。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:322。
     *
     * @param nSeat 座位 — :325
     */
    public record MsgSpecfReq(int nSeat) {}

    /**
     * {@code msgSpecfData} 旁观数据。方向 S→C（仅 bistream）。XY_ID = 523。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:347。
     *
     * @param nCount 张数 — :350
     * @param nMahs 牌值（长度 nCount） — :351
     */
    public record MsgSpecfData(int nCount, int[] nMahs) {
        public MsgSpecfData {
            Objects.requireNonNull(nMahs, "nMahs");
        }
    }

    /**
     * {@code msgSpecfPower} 旁观权限。方向 S→C（仅 bistream）。XY_ID = 524。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:370。
     *
     * @param nSeat 座位 — :373
     * @param nPower 权限 — :374
     */
    public record MsgSpecfPower(int nSeat, int nPower) {}

    /**
     * {@code msgSpecfHand} 旁观手牌。方向 双向（bistream + bostream）。XY_ID = 525。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:390。
     *
     * @param nSeat 座位 — :393
     * @param nCount 张数 — :394
     * @param nMahs 牌值（长度 nCount） — :395
     */
    public record MsgSpecfHand(int nSeat, int nCount, int[] nMahs) {
        public MsgSpecfHand {
            Objects.requireNonNull(nMahs, "nMahs");
        }
    }

    /**
     * {@code msgSpecfDanFang} 旁观单放。方向 C→S（仅 bostream）。XY_ID = 526。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:425。
     *
     * @param nSeat 座位 — :428
     * @param nDanFang 单放 — :429
     */
    public record MsgSpecfDanFang(int nSeat, int nDanFang) {}

    /**
     * {@code msgSpecfWall} 旁观牌墙。方向 C→S（仅 bostream）。XY_ID = 527。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:445。
     *
     * @param nCount 张数 — :448
     * @param nMahs 牌值（长度 nCount） — :449
     */
    public record MsgSpecfWall(int nCount, int[] nMahs) {
        public MsgSpecfWall {
            Objects.requireNonNull(nMahs, "nMahs");
        }
    }

    /**
     * {@code msgSpecfEnd} 旁观结束。方向 双向（bistream + bostream）。XY_ID = 528。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:467。
     *
     * @param nSeat 座位 — :470
     */
    public record MsgSpecfEnd(int nSeat) {}

    /**
     * {@code msgTest} 测试。方向 S→C（仅 bistream）。XY_ID = 1025。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1333。线上为 UInt16。
     *
     * @param nFlag 标志 — :1335
     */
    public record MsgTest(int nFlag) {}
}
