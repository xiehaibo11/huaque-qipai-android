package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 台州层结算域语义消息（BasicTaiZhouMahjong 覆盖/新增）：msgResult、
 * msgTotalResult、msgTaiName、msgSpeak 的台州版。来源
 * {@code $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 *
 * <p>按座位的数组在原版 Lua 中按下标 0..3 读写，固定 4 个座位。
 */
public final class TaizhouResultMessages {
    private TaizhouResultMessages() {}

    /**
     * {@code msgResult} 单局结算（台州覆盖版，替换基础层同 XY_ID 消息）。
     * 方向 S→C（仅 bistream）。XY_ID = 1026。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:3。
     *
     * @param nWinLost 本局优胜值（0..3，线上 Int32） — :6
     * @param nCountHu 牌面胡数（0..3） — :7
     * @param nCountTai 麻将台数（0..3） — :8
     * @param nToTalCountHu 总计弧数（0..3，字段名为原版拼写） — :9
     * @param nPlayerState 玩家状态（0..3） — :10
     * @param bLazi 是否腊子 — :11
     * @param nDanFang 胡单放 — :12
     * @param bFinal 是否最终局 — :13
     * @param bFengDing 封顶情况（0..3）；可选尾字段（:43-47） — :14
     */
    public record MsgResultTZ(int[] nWinLost, int[] nCountHu, int[] nCountTai,
            int[] nToTalCountHu, int[] nPlayerState, boolean bLazi, int nDanFang,
            boolean bFinal, boolean[] bFengDing) {
        public MsgResultTZ {
            Objects.requireNonNull(nWinLost, "nWinLost");
            Objects.requireNonNull(nCountHu, "nCountHu");
            Objects.requireNonNull(nCountTai, "nCountTai");
            Objects.requireNonNull(nToTalCountHu, "nToTalCountHu");
            Objects.requireNonNull(nPlayerState, "nPlayerState");
            Objects.requireNonNull(bFengDing, "bFengDing");
        }
    }

    /**
     * {@code msgTotalResult} 总结算（台州覆盖版）。方向 S→C（仅 bistream）。
     * XY_ID = 1038。声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:53。
     * 前 7 个字段与基础层一致（:56-62），后 7 个统计数组为台州新增可选尾字段
     * （:102-119）。
     *
     * @param playCount 已玩局数 — :56
     * @param boxRoomTotalWinLost 四家各局累计输赢（4 × playCount） — :57
     * @param maxHuCount 四家最大胡数 — :58
     * @param maxFanNum 四家最大番数 — :59
     * @param maxFanCount 四家最大番种个数 — :60
     * @param maxFanName 四家最大番种列表 — :61
     * @param show 是否显示 — :62
     * @param nWinByOwn 自摸胡次数（0..3）；可选尾字段 — :64
     * @param nWinScoreNum 胜分次数（0..3）；可选尾字段 — :65
     * @param nJiePaoNum 接炮次数（0..3）；可选尾字段 — :66
     * @param nDiscardNum 点炮次数（0..3）；可选尾字段 — :67
     * @param nMaxSorceOfTotal 最高分（0..3，字段名为原版拼写）；可选尾字段 — :68
     * @param nLaZiNum 辣子次数（0..3）；可选尾字段 — :69
     * @param nChengBaoNum 包三家次数（0..3）；可选尾字段 — :70
     */
    public record MsgTotalResultTZ(int playCount, int[][] boxRoomTotalWinLost,
            int[] maxHuCount, int[] maxFanNum, int[] maxFanCount, int[][] maxFanName,
            boolean show, int[] nWinByOwn, int[] nWinScoreNum, int[] nJiePaoNum,
            int[] nDiscardNum, int[] nMaxSorceOfTotal, int[] nLaZiNum,
            int[] nChengBaoNum) {
        public MsgTotalResultTZ {
            Objects.requireNonNull(boxRoomTotalWinLost, "boxRoomTotalWinLost");
            Objects.requireNonNull(maxHuCount, "maxHuCount");
            Objects.requireNonNull(maxFanNum, "maxFanNum");
            Objects.requireNonNull(maxFanCount, "maxFanCount");
            Objects.requireNonNull(maxFanName, "maxFanName");
            Objects.requireNonNull(nWinByOwn, "nWinByOwn");
            Objects.requireNonNull(nWinScoreNum, "nWinScoreNum");
            Objects.requireNonNull(nJiePaoNum, "nJiePaoNum");
            Objects.requireNonNull(nDiscardNum, "nDiscardNum");
            Objects.requireNonNull(nMaxSorceOfTotal, "nMaxSorceOfTotal");
            Objects.requireNonNull(nLaZiNum, "nLaZiNum");
            Objects.requireNonNull(nChengBaoNum, "nChengBaoNum");
        }
    }

    /**
     * {@code msgTaiName} 台数名称（台州新增；与基础层 msgGameMaxFan 共用
     * XY_ID = 1045，原版冲突如实保留）。方向 S→C（仅 bistream）。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:157。
     * 每家名称列表按 0..nFanCount[i] 读取（:170-176），比计数多一项。
     *
     * @param nFanCount 台数（0..3） — :159
     * @param nTaiName 台数名称（每家一组） — :160
     */
    public record MsgTaiName(int[] nFanCount, int[][] nTaiName) {
        public MsgTaiName {
            Objects.requireNonNull(nFanCount, "nFanCount");
            Objects.requireNonNull(nTaiName, "nTaiName");
        }
    }

    /**
     * {@code msgSpeak} 语音说话（台州覆盖版）。方向 S→C（仅 bistream，基础层为
     * 双向，覆盖后收窄）。XY_ID = 1028。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:218。
     *
     * @param id 语音 ID — :221
     * @param bIsMan 是否男声 — :222
     * @param speakSeat 说话座位 — :223
     * @param strData 数据 — :224
     */
    public record MsgSpeakTZ(int id, boolean bIsMan, int speakSeat, String strData) {
        public MsgSpeakTZ {
            Objects.requireNonNull(strData, "strData");
        }
    }
}
