package com.nanbeiyule.game;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * 原版 {@code app/Tool/StringTool.lua:335-347} 的 {@code numberToString} 与
 * {@code :381-398} 的 {@code fitNumberLen} 的 Java 复刻。
 *
 * <p>定时登录页有两处用到它：页脚「注：携带金币超过<b>5万</b>不可领奖」
 * （{@code View.lua:71}）与转盘「<b>10万</b>金币」（{@code View.lua:300}）。
 */
final class TimeLoginNumberFormat {
    /** 原版默认 {@code len = 5}，且下限被夹到 4。 */
    private static final int DEFAULT_LEN = 5;
    /** 原版 tostring 走 LUAI_NUMFFORMAT "%.14g"。 */
    private static final MathContext LUA_TOSTRING = new MathContext(14, RoundingMode.HALF_EVEN);

    private TimeLoginNumberFormat() {}

    static String compact(long score) {
        if (score < 10_000L) {
            return Long.toString(score);
        }
        if (score < 100_000_000L) {
            return unit(score / 10_000.0) + "万";
        }
        return unit(score / 100_000_000.0) + "亿";
    }

    private static String unit(double value) {
        double fitted = fitNumberLen(value, DEFAULT_LEN);
        // 原版 math.floor(x * 100 + 0.0001) / 100 之后再 %.2f，最后 tonumber 去掉尾零。
        double truncated = Math.floor(fitted * 100 + 0.0001) / 100.0;
        return stripTrailingZeros(String.format(Locale.US, "%.2f", truncated));
    }

    /** 逐字符保留至多 {@code len} 位数字，小数点不计数；与原版循环同构。 */
    private static double fitNumberLen(double value, int len) {
        String text = luaToString(value);
        int digits = 0;
        StringBuilder kept = new StringBuilder();
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            kept.append(character);
            if (character != '.') {
                digits++;
            }
            if (digits >= len) {
                break;
            }
        }
        return Double.parseDouble(kept.toString());
    }

    private static String luaToString(double value) {
        return new BigDecimal(value).round(LUA_TOSTRING).stripTrailingZeros().toPlainString();
    }

    private static String stripTrailingZeros(String decimal) {
        if (decimal.indexOf('.') < 0) {
            return decimal;
        }
        String trimmed = decimal.replaceAll("0+$", "");
        return trimmed.endsWith(".") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
