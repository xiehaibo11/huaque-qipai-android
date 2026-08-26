package com.nanbeiyule.game.mahjong.round;

import java.util.List;

/**
 * Taizhou {@code GameDefine.totalResultInfoStr[30109]} total-result row labels,
 * recovered from
 * {@code src/game/Mahjong/TaiZhou/BasicTaiZhouMahjong/Define/GameDefine.luac:16-23}.
 *
 * <p>The trailing space inside each label is part of the original string and
 * is kept verbatim.
 */
public final class TaizhouTotalResultInfo {
    /** The 30109 table key of 台州麻将 at :17. */
    public static final int GAME_ID_TAIZHOU_MAHJONG = 30109;

    /** {@code totalResultInfoStr[30109][1..5]}, in original order (:18-22). */
    public static final List<String> TOTAL_RESULT_INFO_30109 =
            List.of("自摸次数 ", "接炮次数 ", "点炮次数 ", "包牌次数 ", "腊子次数 ");

    private TaizhouTotalResultInfo() {}
}
