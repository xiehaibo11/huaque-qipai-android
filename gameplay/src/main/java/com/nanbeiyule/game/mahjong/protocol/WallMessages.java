package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 牌墙与掷骰域语义消息：msgWallMah、msgOpenWall、msgTakeFirst、msgThrowChip、
 * msgAllThrowChip。全部来自 BasicMahjong 层
 * {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 *
 * <p>纯语义模型：字段名与 Lua 表一致，不包含网络/JSON 映射（Wave 2 范围）。
 */
public final class WallMessages {
    private WallMessages() {}

    /**
     * {@code msgWallMah} 牌墙状态。方向 S→C（仅 bistream）。XY_ID = 529。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:491。
     *
     * @param nWallCnt 剩余可抓张数 — :494
     * @param nAsc 正向待抓索引 — :495
     * @param nDesc 反向待抓索引 — :496
     * @param nFirstAsc 初始正向待抓索引 — :497
     * @param nFirstDesc 初始反向待抓索引 — :498
     * @param bShow 收到时是否即刻显示牌墙 — :499
     */
    public record MsgWallMah(int nWallCnt, int nAsc, int nDesc, int nFirstAsc,
            int nFirstDesc, int bShow) {}

    /**
     * {@code msgOpenWall} 开墙翻牌。方向 S→C（仅 bistream）。XY_ID = 530。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:519。
     *
     * @param nIndex 开墙索引 — :522
     * @param nMah 翻出的牌值 — :523
     */
    public record MsgOpenWall(int nIndex, int nMah) {}

    /**
     * {@code msgTakeFirst} 首次抓牌标志。方向 S→C（仅 bistream）。XY_ID = 533。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:565。
     *
     * @param nFlag 标志 — :568
     */
    public record MsgTakeFirst(int nFlag) {}

    /**
     * {@code msgThrowChip} 掷骰。方向 S→C（仅 bistream）。XY_ID = 532。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:540。
     *
     * @param nSeat 座位 — :543
     * @param nCount 骰子个数 — :544
     * @param nChips 骰子点数（长度 nCount） — :545
     */
    public record MsgThrowChip(int nSeat, int nCount, int[] nChips) {
        public MsgThrowChip {
            Objects.requireNonNull(nChips, "nChips");
        }
    }

    /**
     * {@code msgAllThrowChip} 全部掷骰结果。方向 S→C（仅 bistream）。XY_ID = 564。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1946。
     * 原版按 i=1..3 循环读取三个座位（:1961-1968），数组长度固定为 3。
     *
     * @param nSeat 座位（3 个） — :1949
     * @param nCount 骰子个数（3 个） — :1950
     * @param nChips 骰子点数（每座位一组） — :1951
     */
    public record MsgAllThrowChip(int[] nSeat, int[] nCount, int[][] nChips) {
        public MsgAllThrowChip {
            Objects.requireNonNull(nSeat, "nSeat");
            Objects.requireNonNull(nCount, "nCount");
            Objects.requireNonNull(nChips, "nChips");
        }
    }
}
