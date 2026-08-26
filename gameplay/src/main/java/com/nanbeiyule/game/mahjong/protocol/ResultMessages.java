package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 结算域语义消息（BasicMahjong 层）：msgFanData、msgFanCnt、msgResult、
 * msgTotalResult、msgEndResult、msgResultExtInfo、msgGameMaxFan、msgBetResult、
 * msgEndType。来源 {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 *
 * <p>按座位的数组在原版 Lua 中按下标 0..3 读写，固定 4 个座位。
 */
public final class ResultMessages {
    private ResultMessages() {}

    /**
     * {@code msgFanData} 番种子结构；无 XY_ID，不作为独立线上消息。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:108。
     *
     * @param nFanID 番种 ID — :110
     * @param nCount 番种个数 — :111
     */
    public record MsgFanData(int nFanID, int nCount) {}

    /**
     * {@code msgFanCnt} 番种统计。方向 S→C（仅 bistream）。XY_ID = 548。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1120。
     *
     * @param nSeat 座位 — :1123
     * @param nCount 番种个数 — :1124
     * @param Fans 番种列表（元素结构同 msgFanData，解析 :1137-1142；
     *        字段名保持 Lua 原样首字母大写） — :1125
     */
    public record MsgFanCnt(int nSeat, int nCount, MsgFanData[] Fans) {
        public MsgFanCnt {
            Objects.requireNonNull(Fans, "Fans");
        }
    }

    /**
     * {@code msgResult} 单局结算（基础层，台州层有覆盖）。方向 S→C（仅 bistream）。
     * XY_ID = 1026。声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1351。
     *
     * @param nWinLost 四家输赢（线上 Int64，下标 0..3，:1364-1366） — :1353
     */
    public record MsgResult(long[] nWinLost) {
        public MsgResult {
            Objects.requireNonNull(nWinLost, "nWinLost");
        }
    }

    /**
     * {@code msgTotalResult} 总结算（基础层，台州层有覆盖）。方向 S→C（仅 bistream）。
     * XY_ID = 1038。声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1510。
     *
     * @param playCount 已玩局数 — :1513
     * @param boxRoomTotalWinLost 四家各局累计输赢（4 × playCount） — :1514
     * @param maxHuCount 四家最大胡数 — :1515
     * @param maxFanNum 四家最大番数 — :1516
     * @param maxFanCount 四家最大番种个数 — :1517
     * @param maxFanName 四家最大番种列表 — :1518
     * @param show 是否显示 — :1519
     */
    public record MsgTotalResult(int playCount, int[][] boxRoomTotalWinLost,
            int[] maxHuCount, int[] maxFanNum, int[] maxFanCount, int[][] maxFanName,
            boolean show) {
        public MsgTotalResult {
            Objects.requireNonNull(boxRoomTotalWinLost, "boxRoomTotalWinLost");
            Objects.requireNonNull(maxHuCount, "maxHuCount");
            Objects.requireNonNull(maxFanNum, "maxFanNum");
            Objects.requireNonNull(maxFanCount, "maxFanCount");
            Objects.requireNonNull(maxFanName, "maxFanName");
        }
    }

    /**
     * {@code msgEndResult} 结算字符串。方向 S→C（仅 bistream）。XY_ID = 549。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1147。
     *
     * @param szResult 结算内容 — :1150
     */
    public record MsgEndResult(String szResult) {
        public MsgEndResult {
            Objects.requireNonNull(szResult, "szResult");
        }
    }

    /**
     * {@code msgResultExtInfo} 结算扩展信息。方向 S→C（仅 bistream）。XY_ID = 1046。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1714。
     * desc 原版经 GB_18030_2000 转 UTF-8（:1729）。
     *
     * @param score 四家得分（下标 0..3） — :1717
     * @param desc 四家描述（下标 0..3） — :1718
     */
    public record MsgResultExtInfo(int[] score, String[] desc) {
        public MsgResultExtInfo {
            Objects.requireNonNull(score, "score");
            Objects.requireNonNull(desc, "desc");
        }
    }

    /**
     * {@code msgGameMaxFan} 最大番。方向 S→C（仅 bistream）。XY_ID = 1045，
     * 与台州层 msgTaiName 同号（原版冲突，如实保留）。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1694。
     *
     * @param maxFan 最大番 — :1698
     */
    public record MsgGameMaxFan(int maxFan) {}

    /**
     * {@code msgBetResult} 下注结果。方向 S→C（仅 bistream）。XY_ID = 1049，
     * 与台州层 msgShengPaiCnt 同号（原版冲突，如实保留）。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1762。
     *
     * @param betMahCount 下注牌数 — :1765
     * @param betMahList 下注牌（长度 betMahCount） — :1766
     * @param betMahWind 下注牌风（长度 betMahCount） — :1767
     * @param huCount 胡数 — :1768
     * @param huWind 胡风（长度 huCount） — :1769
     * @param allWind 四风（固定 4，:1795-1797） — :1770
     */
    public record MsgBetResult(int betMahCount, int[] betMahList, int[] betMahWind,
            int huCount, int[] huWind, int[] allWind) {
        public MsgBetResult {
            Objects.requireNonNull(betMahList, "betMahList");
            Objects.requireNonNull(betMahWind, "betMahWind");
            Objects.requireNonNull(huWind, "huWind");
            Objects.requireNonNull(allWind, "allWind");
        }
    }

    /**
     * {@code msgEndType} 结束类型。方向 S→C（仅 bistream）。XY_ID = 1034。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1422。
     *
     * @param sEndType 结束类型 — :1425
     * @param sSeat 座位 — :1426
     */
    public record MsgEndType(int sEndType, int sSeat) {}
}
