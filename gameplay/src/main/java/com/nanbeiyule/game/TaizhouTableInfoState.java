package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayTableState;

/**
 * Immutable display projection of the TableInfo 生牌信息层 and the 剩庄/剩局
 * room-info text, recovered from {@code BasicTaiZhouMahjong} TableInfoLayer.luac
 * and RoomInfo/View.luac plus {@code TaiZhouMahjong/Modules/GameLayer/Module.luac}.
 */
public record TaizhouTableInfoState(boolean shengPaiVisible, int shengPaiCount) {
    /**
     * The original drives visibility with {@code showShengPaiCount(true/false)}
     * and only updates the numeric label while visible. The rebuilt API currently
     * carries only the count, so positive counts are the visible phase and zero is
     * treated like the original {@code show=false} cleanup.
     */
    static TaizhouTableInfoState from(GameplayTableState state) {
        Integer count = state == null ? null : state.shengPaiCount();
        return new TaizhouTableInfoState(count != null && count > 0, count == null ? 0 : count);
    }

    /**
     * 剩庄/剩局 text of the room-info "剩 余" row: four chairs render
     * {@code "%d庄"} (RoomInfo/View.luac:showLeftBankerCount), other chair counts
     * render {@code "%d局"} (TaiZhouMahjong GameLayer Module.luac:onMsgLeftBanker).
     * Without a LEFT_BANKER message the waiting value stays {@code "-/-"} because
     * TaiZhouMahjong/View.luac suppresses generic play-count updates.
     */
    static String remainingText(Integer leftBankerCount, int chairCount) {
        if (leftBankerCount == null) {
            return "-/-";
        }
        return chairCount == 4 ? leftBankerCount + "庄" : leftBankerCount + "局";
    }
}
