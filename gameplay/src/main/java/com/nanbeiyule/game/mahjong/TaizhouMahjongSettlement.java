package com.nanbeiyule.game.mahjong;

/**
 * Taizhou mahjong (ConfID 30109) per-round settlement text and 腊子 detection.
 *
 * <p>Ported from the recovered original
 * {@code TaiZhou/TaiZhouMahjong/Modules/WinLost/WinLostData.lua:onMsgTaiName},
 * which builds each seat's summary line from the {@code msgResult} (XY_ID 1026)
 * fields {@code nCountHu}, {@code nCountTai}, {@code nToTalCountHu} and
 * {@code nPlayerState}.
 *
 * <p>Note that the base Taizhou family clears these names; only 30109 renders
 * them, so this formatting must not be reused for the sibling variants without
 * checking their own override.
 */
public final class TaizhouMahjongSettlement {
    /** {@code endPlayerState.EPS_NULL}. */
    public static final int STATE_NULL = 0;

    /** {@code endPlayerState.EPS_HU}. */
    public static final int STATE_HU = 1;

    /** {@code endPlayerState.EPS_DISCARD}: 点炮. */
    public static final int STATE_DISCARD = 2;

    /** {@code endPlayerState.EPS_ROBKONG}: 抢杠胡. */
    public static final int STATE_ROB_KONG = 3;

    /** {@code endPlayerState.EPS_GANGSHANGKAIHUA}: 杠上开花. */
    public static final int STATE_KONG_BLOOM = 4;

    /** {@code endPlayerState.EPS_CHENGBAO}: 包牌. */
    public static final int STATE_CHENG_BAO = 5;

    /** {@code endPlayerState.EPS_DRAWN}: 荒庄. */
    public static final int STATE_DRAWN = 9;

    /**
     * Total 胡 at or above which the original marks the seat as 腊子胡.
     * {@code onMsgTaiName} pushes the seat into {@code _nLaZiHuSeats} when
     * {@code hushu >= 100}.
     */
    public static final int LAZI_TOTAL_HU_THRESHOLD = 100;

    /**
     * 胡数 above which a winning seat's line is split into {@code (n-10)胡+10胡}.
     */
    public static final int WIN_HU_SPLIT_THRESHOLD = 10;

    private TaizhouMahjongSettlement() {}

    /** Returns whether {@code state} is one of the original's winning states. */
    public static boolean isWinningState(int state) {
        return state == STATE_HU || state == STATE_ROB_KONG || state == STATE_KONG_BLOOM;
    }

    /**
     * Returns the original Taizhou table-info frame used by {@code _KW_IMG_END_ICON}.
     *
     * <p>The generic {@code settle_icon_*} frames are not the Taizhou row badges. The Taizhou
     * settlement screenshot uses the dedicated {@code tableInfo.plist} frames: 胡, 放铳, 抢杠胡,
     * 杠上开花, 包 and 黄牌.
     */
    public static String endIconFrameName(int state) {
        return switch (state) {
            case STATE_HU -> "tz_settle_icon_1.png";
            case STATE_DISCARD -> "tz_settle_icon_2.png";
            case STATE_ROB_KONG -> "tz_settle_icon_3.png";
            case STATE_KONG_BLOOM -> "tz_settle_icon_4.png";
            case STATE_CHENG_BAO -> "tz_settle_icon_5.png";
            case STATE_DRAWN -> "tz_settle_icon_9.png";
            default -> "";
        };
    }

    /**
     * Returns whether the seat's total 胡 marks it as 腊子胡.
     *
     * @param totalHu the seat's {@code nToTalCountHu}
     */
    public static boolean isLaziHu(int totalHu) {
        return totalHu >= LAZI_TOTAL_HU_THRESHOLD;
    }

    /**
     * Builds a seat's settlement line.
     *
     * @param handHu the seat's {@code nCountHu}, 牌面胡数
     * @param tai the seat's {@code nCountTai}, 台数
     * @param totalHu the seat's {@code nToTalCountHu}, 总胡数
     * @param state the seat's {@code nPlayerState}
     */
    public static String describeSeat(int handHu, int tai, int totalHu, int state) {
        StringBuilder text = new StringBuilder();
        if (isWinningState(state) && handHu > WIN_HU_SPLIT_THRESHOLD) {
            text.append("胡数:")
                    .append(handHu - WIN_HU_SPLIT_THRESHOLD)
                    .append("胡+")
                    .append(WIN_HU_SPLIT_THRESHOLD)
                    .append("胡\t");
        } else {
            text.append("胡数:").append(handHu).append("胡\t");
        }
        text.append("台数:").append(tai).append("台\t");
        text.append("总胡数:").append(totalHu).append("胡");
        return text.toString();
    }
}
