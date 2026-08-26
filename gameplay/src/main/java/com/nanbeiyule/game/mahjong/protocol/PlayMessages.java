package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 出牌域语义消息：msgPlay、msgCancel、msgOutMah、msgOutMahRefresh、
 * msgAllOutMahRefresh、msgFollowMah、msgPlayLmts。全部来自 BasicMahjong 层
 * {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class PlayMessages {
    private PlayMessages() {}

    /**
     * {@code msgPlay} 出牌。方向 双向（bistream + bostream）。XY_ID = 539。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:840。
     *
     * @param nSeat 座位 — :843
     * @param nMah 出的牌值 — :844
     * @param nActionID 动作 ID；声明表未列出，仅 bostream 写出（:863），
     *        为原版客户端发送时动态挂载的字段
     */
    public record MsgPlay(int nSeat, int nMah, int nActionID) {}

    /**
     * {@code msgCancel} 取消操作。方向 C→S（仅 bostream）。XY_ID = 540。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:868。
     *
     * @param nSeat 座位 — :871
     * @param nActionID 动作 ID — :872
     */
    public record MsgCancel(int nSeat, int nActionID) {}

    /**
     * {@code msgOutMah} 已出牌列表。方向 S→C（仅 bistream）。XY_ID = 547。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1093。
     *
     * @param nSeat 座位 — :1096
     * @param nCount 张数 — :1097
     * @param nMahs 牌值（长度 nCount） — :1098
     */
    public record MsgOutMah(int nSeat, int nCount, int[] nMahs) {
        public MsgOutMah {
            Objects.requireNonNull(nMahs, "nMahs");
        }
    }

    /**
     * {@code msgOutMahRefresh} 出牌刷新。方向 S→C（仅 bistream）。XY_ID = 556。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1284。
     *
     * @param nSeat 座位 — :1287
     * @param nCount 张数 — :1288
     * @param nMahs 牌值（长度 nCount） — :1289
     */
    public record MsgOutMahRefresh(int nSeat, int nCount, int[] nMahs) {
        public MsgOutMahRefresh {
            Objects.requireNonNull(nMahs, "nMahs");
        }
    }

    /**
     * {@code msgAllOutMahRefresh} 全部出牌刷新。方向 S→C（仅 bistream）。XY_ID = 557。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1309。
     *
     * @param nCount 张数 — :1312
     * @param nMahs 牌值（长度 nCount） — :1313
     */
    public record MsgAllOutMahRefresh(int nCount, int[] nMahs) {
        public MsgAllOutMahRefresh {
            Objects.requireNonNull(nMahs, "nMahs");
        }
    }

    /**
     * {@code msgFollowMah} 跟牌。方向 S→C（仅 bistream）。XY_ID = 1047。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1735。
     * 原版附带两个常量表：ST_TYPE.ST_CENTER = 4（:1738-1740）、
     * RT_TYPE.RT_SI_GEN = 1 / RT_SAN_GEN = 2（:1741-1744）。
     *
     * @param seat 座位 — :1746
     * @param result 结果 — :1747
     */
    public record MsgFollowMah(int seat, int result) {
        /** {@code ST_TYPE.ST_CENTER} — $M/BasicMahjong/Protocols/GameProtocol.luac:1739 */
        public static final int ST_CENTER = 4;
        /** {@code RT_TYPE.RT_SI_GEN} — $M/BasicMahjong/Protocols/GameProtocol.luac:1742 */
        public static final int RT_SI_GEN = 1;
        /** {@code RT_TYPE.RT_SAN_GEN} — $M/BasicMahjong/Protocols/GameProtocol.luac:1743 */
        public static final int RT_SAN_GEN = 2;
    }

    /**
     * {@code msgPlayLmts} 出牌限制。方向 S→C（仅 bistream）。XY_ID = 553。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1225。
     *
     * @param nSeat 座位 — :1228
     * @param nCount 张数 — :1229
     * @param nMahs 限制的牌（长度 nCount） — :1230
     */
    public record MsgPlayLmts(int nSeat, int nCount, int[] nMahs) {
        public MsgPlayLmts {
            Objects.requireNonNull(nMahs, "nMahs");
        }
    }
}
