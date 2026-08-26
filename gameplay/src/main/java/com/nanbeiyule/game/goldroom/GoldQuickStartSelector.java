package com.nanbeiyule.game.goldroom;

import java.util.List;

/** Original quick-start selection: scan roomLevelInfos from high to low by current coins. */
public final class GoldQuickStartSelector {
    private GoldQuickStartSelector() {}

    public static int selectIndex(GoldRoomConf conf, long coins) {
        if (conf == null || conf.levels() == null || conf.levels().isEmpty()) {
            return 0;
        }
        List<GoldRoomLevel> levels = conf.levels();
        for (int index = levels.size() - 1; index >= 0; index--) {
            GoldRoomLevel level = levels.get(index);
            if (coins >= level.minRich()
                    && (level.maxRich() == GoldRoomText.UNBOUNDED_MAX_RICH
                            || coins <= level.maxRich())) {
                return index;
            }
        }
        return 0;
    }

    public static GoldRoomLevel selectLevel(GoldRoomConf conf, long coins) {
        if (conf == null || conf.levels() == null || conf.levels().isEmpty()) {
            throw new IllegalArgumentException("gold-room levels must not be empty");
        }
        return conf.levels().get(selectIndex(conf, coins));
    }
}
