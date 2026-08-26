package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * {@code tComb} 牌组（吃/碰/杠）结构，msgPlayerMah、msgPlayerBack 与 msgAction
 * 共用的内嵌表。字段与解析次序来自
 * $M/BasicMahjong/Protocols/GameProtocol.luac:608-626（msgPlayerMah 内）。
 *
 * <p>{@code $M} = {@code artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong}。
 *
 * @param nFlag 组合类型标志 — $M/BasicMahjong/Protocols/GameProtocol.luac:611
 * @param nFromSeat 来源座位 — $M/BasicMahjong/Protocols/GameProtocol.luac:612
 * @param nCount 组合牌数 — $M/BasicMahjong/Protocols/GameProtocol.luac:613
 * @param nMahs 组合牌值（长度 nCount） — $M/BasicMahjong/Protocols/GameProtocol.luac:615-619
 * @param nInCount 入牌数（杠入等） — $M/BasicMahjong/Protocols/GameProtocol.luac:620
 * @param nInMahs 入牌值（长度 nInCount） — $M/BasicMahjong/Protocols/GameProtocol.luac:621-624
 */
public record MahjongComb(int nFlag, int nFromSeat, int nCount, int[] nMahs,
        int nInCount, int[] nInMahs) {
    public MahjongComb {
        Objects.requireNonNull(nMahs, "nMahs");
        Objects.requireNonNull(nInMahs, "nInMahs");
    }
}
