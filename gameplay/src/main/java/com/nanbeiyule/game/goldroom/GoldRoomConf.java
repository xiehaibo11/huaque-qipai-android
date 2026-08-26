package com.nanbeiyule.game.goldroom;

import java.util.List;

/**
 * The whole choose-room payload of one gold game, mirroring the original {@code roomConf}.
 *
 * <p>{@code showsPlayerCount} follows the mode-50 live count response; the original hides
 * {@code _panelPlayerCount} before that response is available.
 */
public record GoldRoomConf(
        long lobbyId,
        long gameId,
        String displayName,
        long boxGameId,
        int chairCount,
        List<GoldRoomLevel> levels,
        boolean showsPlayerCount) {

    /**
     * The original opens the choose-room page only when the game has more than one level; a single
     * level joins straight away ({@code GoldNew/Module.lua joinGoldRoomFirst}).
     */
    public boolean needsChooseRoomPage() {
        return levels != null && levels.size() > 1;
    }
}
