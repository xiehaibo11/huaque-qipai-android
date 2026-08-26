package com.nanbeiyule.game.mahjong;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Taizhou mahjong (ConfID 30109) rule options and rule summary text.
 *
 * <p>Ported from the recovered original
 * {@code TaiZhou/TaiZhouMahjong/Modules/RoomInfo/Module.lua}, which parses the
 * server's {@code strGameRule} with {@code getTableByString(rule, ";", "=")} and
 * renders the enabled options in a fixed order.
 *
 * <p>The rule string is the server's contract, so the option keys, their
 * accepted values and the display order below are original facts. What the
 * options <em>do</em> to a hand is decided by the server and is not described
 * here.
 */
public final class TaizhouMahjongRules {
    /** Option keys in the original's {@code orderRuleTable} order. */
    public static final String[] ORDERED_KEYS = {
        "winLostType",
        "forceGPS",
        "liaoDaZiBaoPai",
        "lianZhuang",
        "duiDuiHuFourScore",
        "noShengPaiJieDuan",
        "buSiBao",
        "DelColor",
        "FengDing",
    };

    /** {@code PayType} is rendered after the ordered options, outside teahouses. */
    public static final String KEY_PAY_TYPE = "PayType";

    /** Base score key, rendered as 底分. */
    public static final String KEY_BASE_SCORE = "basescore";

    /** Round count key, rendered as 圈 (or 局 at two seats). */
    public static final String KEY_MAX_QUAN_SHU = "maxQuanShu";

    private static final Map<String, Map<Integer, String>> LABELS = labels();

    private TaizhouMahjongRules() {}

    private static Map<String, Map<Integer, String>> labels() {
        Map<String, Map<Integer, String>> table = new LinkedHashMap<>();
        table.put("winLostType", Map.of(1, "不平搓", 2, "平搓"));
        table.put("forceGPS", Map.of(1, "防作弊"));
        table.put(KEY_PAY_TYPE, Map.of(0, "房主付", 1, "平摊付", 7, "平摊付"));
        table.put("liaoDaZiBaoPai", Map.of(1, "撩搭子包牌"));
        table.put("lianZhuang", Map.of(1, "连庄"));
        table.put("duiDuiHuFourScore", Map.of(1, "对对胡4胡"));
        table.put("noShengPaiJieDuan", Map.of(1, "无生牌阶段"));
        table.put("buSiBao", Map.of(1, "不死包"));
        table.put("DelColor", Map.of(1, "缺一色", 2, "缺二色"));
        table.put("FengDing", Map.of(0, "不封顶", 60, "60封顶", 80, "80封顶"));
        return table;
    }

    /** Returns the label for {@code key} at {@code value}, or null when unmapped. */
    public static String labelOf(String key, int value) {
        Map<Integer, String> options = LABELS.get(key);
        return options == null ? null : options.get(value);
    }

    /**
     * Parses the server's {@code strGameRule}, which is {@code key=value} pairs
     * separated by semicolons. The original strips single quotes from values.
     */
    public static Map<String, String> parse(String gameRule) {
        Map<String, String> parsed = new LinkedHashMap<>();
        if (gameRule == null || gameRule.isEmpty()) {
            return parsed;
        }
        for (String pair : gameRule.split(";")) {
            int separator = pair.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = pair.substring(0, separator);
            String value = pair.substring(separator + 1).replace("'", "");
            parsed.put(key, value);
        }
        return parsed;
    }

    /**
     * Renders the room's rule summary exactly as the original does: enabled
     * options in {@link #ORDERED_KEYS} order, then the pay type when the room is
     * not a teahouse table, then the seat count, base score and round count.
     *
     * @param chairs seats at the table; two seats render 局 instead of 圈
     * @param inTeaHouse suppresses the pay type, matching the original guard
     */
    public static String summarize(
            Map<String, String> rules, int chairs, boolean inTeaHouse) {
        StringBuilder text = new StringBuilder();
        for (String key : ORDERED_KEYS) {
            appendLabel(text, rules, key);
        }
        if (!inTeaHouse) {
            appendLabel(text, rules, KEY_PAY_TYPE);
        }
        text.append(chairs).append("人");

        String baseScore = rules.get(KEY_BASE_SCORE);
        if (baseScore != null) {
            text.append("/底分").append(baseScore);
        }

        Integer maxQuanShu = parseInt(rules.get(KEY_MAX_QUAN_SHU));
        if (maxQuanShu != null) {
            if (chairs == 2) {
                text.append("/").append(2 * maxQuanShu).append("局");
            } else {
                text.append("/").append(maxQuanShu).append("圈");
            }
        }
        return text.toString();
    }

    private static void appendLabel(
            StringBuilder text, Map<String, String> rules, String key) {
        Integer value = parseInt(rules.get(key));
        if (value == null) {
            return;
        }
        String label = labelOf(key, value);
        if (label != null) {
            text.append(label).append("/");
        }
    }

    private static Integer parseInt(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
