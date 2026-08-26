package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public final class GameRuleCatalogTest {
    @Test
    public void taizhouOriginalRuleCatalogHasTheRecoveredEighteenEntriesInOrder() {
        List<GameRuleCatalog.Entry> entries = GameRuleCatalog.taizhou();

        assertEquals(18, entries.size());
        assertEquals(
                List.of(30579L, 30116L, 30588L, 30577L, 30110L, 30111L, 30109L,
                        30113L, 30114L, 30399L, 30250L, 30112L, 30284L, 30156L,
                        30130L, 30128L, 30155L, 30227L),
                entries.stream().map(GameRuleCatalog.Entry::gameId).toList());
        assertEquals(
                List.of("暗斗双扣", "茶苑双扣", "茶苑双扣", "干瞪眼", "温岭玩法", "玉环玩法",
                        "台州麻将", "三门玩法", "临海麻将", "大溪玩法", "天台三阿磨玩法",
                        "推倒胡玩法", "挖花玩法", "斗地主玩法", "茶苑双扣", "茶苑关牌",
                        "玉环打通", "茶苑双扣"),
                entries.stream().map(GameRuleCatalog.Entry::title).toList());
        assertEquals(List.of("暗斗", "千变", "乌龙", "台州", "两帮"),
                List.of(entries.get(0).mark(), entries.get(1).mark(), entries.get(2).mark(),
                        entries.get(14).mark(), entries.get(17).mark()));
        assertEquals(
                "https://wechat.hzxuanming.com/game_center/game_rule/33313/7109/30109.html",
                entries.get(6).ruleUrl());
        assertEquals(
                "https://wechat.hzxuanming.com/game_center/game_rule/33313/7128/30577.html",
                entries.get(3).ruleUrl());
    }
}
