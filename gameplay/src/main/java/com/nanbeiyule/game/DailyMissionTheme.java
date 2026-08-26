package com.nanbeiyule.game;

import android.graphics.Color;

/**
 * 对应 lobby/Modules/LuckyMission/Config.lua 的 TAB_INFO。
 *
 * <p>原版 {@code getCurTabConfig} 按当前页签的 {@code pageDesc} 去 TAB_INFO 里找同名条目，
 * 找不到就回落到 CUSTOM，所以主题是由页签**文案**决定的，不是由页面编码决定的。
 */
enum DailyMissionTheme {
    DAILY("每日任务", Color.rgb(41, 84, 164)),
    WEEK("每周任务", Color.rgb(68, 58, 179)),
    TEAM("惊喜任务", Color.rgb(170, 71, 17)),
    LUCKY("幸运任务", Color.rgb(136, 45, 44)),
    /** TAB_INFO 的 CUSTOM 条目：TAB_NAME 为空，资源与幸运任务同一套。 */
    CUSTOM("", Color.rgb(136, 45, 44));

    private final String tabName;
    private final int progressColor;

    DailyMissionTheme(String tabName, int progressColor) {
        this.tabName = tabName;
        this.progressColor = progressColor;
    }

    String tabName() {
        return tabName;
    }

    /** Config.lua 的 PROGRESS_CLOLOR，用于 _KW_PROGRESS_BG 与 KW_UN_STAGE 的染色。 */
    int progressColor() {
        return progressColor;
    }

    static DailyMissionTheme forDisplayName(String displayName) {
        if (displayName == null) return CUSTOM;
        String name = displayName.trim();
        for (DailyMissionTheme theme : values()) {
            if (theme != CUSTOM && theme.tabName.equals(name)) return theme;
        }
        return CUSTOM;
    }
}
