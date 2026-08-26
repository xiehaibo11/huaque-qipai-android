package com.nanbeiyule.game.goldroom;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Original choose-room text rules.
 *
 * <p>Ported from {@code lobby/Modules/GoldNew/Views/ChooseRoom.lua} of 浙江游戏大厅 1.5.4:
 * {@code getRichString} folds a gold threshold into 万/亿, {@code _txtGoldLimit} joins the
 * min/max pair, and {@code _fontBaseScore} appends 以上 when the level is {@code dynamic_cost}.
 * See android/docs/ORIGINAL-GOLD-CHOOSE-ROOM-EVIDENCE.md.
 */
public final class GoldRoomText {
    /** Original sentinel: {@code maxrich == -1} means "no upper bound". */
    public static final long UNBOUNDED_MAX_RICH = -1L;

    private static final long WAN = 10_000L;
    private static final long YI = 100_000_000L;
    /** {@code getRichString} floors any negative threshold to this literal. */
    private static final long NEGATIVE_FALLBACK = 1000L;

    private GoldRoomText() {}

    /**
     * {@code ChooseRoom.lua getRichString}: below 万 the raw number, below 亿 a 万 multiple, else a
     * 亿 multiple. Lua prints the quotient as a plain number, so a whole result keeps no decimals.
     */
    public static String richString(long rich) {
        if (rich < 0) {
            return Long.toString(NEGATIVE_FALLBACK);
        }
        if (rich < WAN) {
            return Long.toString(rich);
        }
        if (rich < YI) {
            return quotient(rich, WAN) + "万";
        }
        return quotient(rich, YI) + "亿";
    }

    /** {@code _txtGoldLimit}: "<min>以上" when unbounded, otherwise "<min>-<max>". */
    public static String goldLimitText(long minRich, long maxRich) {
        if (maxRich == UNBOUNDED_MAX_RICH) {
            return richString(minRich) + "以上";
        }
        return richString(minRich) + "-" + richString(maxRich);
    }

    /** {@code _fontBaseScore}: dynamic levels render "<baseScore>以上", fixed ones the number. */
    public static String baseScoreText(long baseScore, boolean dynamicCost) {
        return dynamicCost ? baseScore + "以上" : Long.toString(baseScore);
    }

    private static String quotient(long value, long unit) {
        BigDecimal scaled =
                BigDecimal.valueOf(value)
                        .divide(BigDecimal.valueOf(unit), 4, RoundingMode.HALF_UP)
                        .stripTrailingZeros();
        return scaled.toPlainString();
    }
}
