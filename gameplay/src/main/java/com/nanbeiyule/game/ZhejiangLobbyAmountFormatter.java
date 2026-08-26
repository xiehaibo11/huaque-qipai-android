package com.nanbeiyule.game;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formats Zhejiang lobby wallet values without inventing or hiding balance digits. */
public final class ZhejiangLobbyAmountFormatter {
    private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000L);

    private ZhejiangLobbyAmountFormatter() {}

    public static String format(long value) {
        if (value < 10_000L) {
            return Long.toString(value);
        }
        return BigDecimal.valueOf(value)
                        .divide(TEN_THOUSAND, 2, RoundingMode.HALF_UP)
                        .stripTrailingZeros()
                        .toPlainString()
                + "万";
    }
}
