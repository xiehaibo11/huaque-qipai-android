package com.nanbeiyule.game.goldroom;

import java.util.List;

/**
 * Repository-evidence fallback for the original 台州麻将 gold choose-room page.
 *
 * <p>The authoritative catalog still comes from {@code /api/v1/gold-rooms}. This class only covers
 * the known 900023 / 30400 static page recovered in
 * android/docs/ORIGINAL-GOLD-CHOOSE-ROOM-EVIDENCE.md, so a stale or unavailable backend does not
 * collapse the already-restored UI into a blank error state. It does not provide wallet values,
 * player counts, matchmaking, seating, or dealing authority.
 */
public final class GoldRoomEvidenceCatalog {
    public static final long TAIZHOU_LOBBY_ID = 900023L;
    public static final long TAIZHOU_GOLD_GAME_ID = 30400L;

    private GoldRoomEvidenceCatalog() {}

    public static GoldRoomConf confOrNull(long lobbyId, long gameId) {
        if (lobbyId == TAIZHOU_LOBBY_ID && gameId == TAIZHOU_GOLD_GAME_ID) {
            return taizhouMahjong();
        }
        return null;
    }

    private static GoldRoomConf taizhouMahjong() {
        return new GoldRoomConf(
                TAIZHOU_LOBBY_ID,
                TAIZHOU_GOLD_GAME_ID,
                "台州麻将",
                30109L,
                4,
                List.of(
                        new GoldRoomLevel(
                                1,
                                1,
                                4,
                                200L,
                                true,
                                1000L,
                                60_000L,
                                0L,
                                null,
                                null,
                                null,
                                null),
                        new GoldRoomLevel(
                                2,
                                2,
                                4,
                                600L,
                                true,
                                30_000L,
                                200_000L,
                                0L,
                                null,
                                null,
                                "2#底分进阶，挑战高分",
                                null),
                        new GoldRoomLevel(
                                3,
                                3,
                                4,
                                1000L,
                                true,
                                50_000L,
                                GoldRoomText.UNBOUNDED_MAX_RICH,
                                0L,
                                null,
                                null,
                                "3#EEEE55_支持加倍！强者之战",
                                null)),
                false);
    }
}
