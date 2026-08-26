package com.nanbeiyule.game;

import java.util.List;

/** Original Taizhou rule menu: two fixed leisure games followed by lobby 900023 box games. */
public final class GameRuleCatalog {
    private static final String RULE_ROOT =
            "https://wechat.hzxuanming.com/game_center/game_rule/33313/";

    public record Entry(long gameId, String title, String mark, long areaId) {
        public String ruleUrl() {
            return RULE_ROOT + areaId + "/" + gameId + ".html";
        }
    }

    private static final List<Entry> TAIZHOU = List.of(
            new Entry(30579L, "暗斗双扣", "暗斗", 7128L),
            new Entry(30116L, "茶苑双扣", "千变", 7128L),
            new Entry(30588L, "茶苑双扣", "乌龙", 7128L),
            new Entry(30577L, "干瞪眼", "", 7128L),
            new Entry(30110L, "温岭玩法", "", 7109L),
            new Entry(30111L, "玉环玩法", "", 7109L),
            new Entry(30109L, "台州麻将", "", 7109L),
            new Entry(30113L, "三门玩法", "", 7109L),
            new Entry(30114L, "临海麻将", "", 7109L),
            new Entry(30399L, "大溪玩法", "", 7109L),
            new Entry(30250L, "天台三阿磨玩法", "", 7109L),
            new Entry(30112L, "推倒胡玩法", "", 7109L),
            new Entry(30284L, "挖花玩法", "", 7109L),
            new Entry(30156L, "斗地主玩法", "", 7109L),
            new Entry(30130L, "茶苑双扣", "台州", 7109L),
            new Entry(30128L, "茶苑关牌", "", 7109L),
            new Entry(30155L, "玉环打通", "", 7109L),
            new Entry(30227L, "茶苑双扣", "两帮", 7109L));

    private GameRuleCatalog() {}

    public static List<Entry> taizhou() {
        return TAIZHOU;
    }
}
