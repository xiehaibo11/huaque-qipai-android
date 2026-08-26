package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;

/** Original 900023 create-room list addition that is not a room-service game. */
final class CreateRoomEntryPolicy {
    static final long TAIZHOU_LOBBY_ID = 900023L;
    static final long SHI_SAN_ZHANG_GAME_ID = 30580L;
    static final long TAIZHOU_MAHJONG_GAME_ID = 30109L;
    static final String ROOM_CREATION_UNAVAILABLE_MESSAGE =
            "该玩法牌桌尚未接入，暂不能创建房间";

    private CreateRoomEntryPolicy() {}

    static List<CreateRoomGame> gamesForLobby(long lobbyId, List<CreateRoomGame> serverGames) {
        List<CreateRoomGame> source = serverGames == null ? List.of() : serverGames;
        if (lobbyId != TAIZHOU_LOBBY_ID) {
            return source;
        }
        List<CreateRoomGame> games = new ArrayList<>(source.size() + 1);
        games.add(new CreateRoomGame(SHI_SAN_ZHANG_GAME_ID, "边锋十三水", "", 0));
        for (CreateRoomGame game : source) {
            if (!isExternalMiniProgramGame(game.gameId())) {
                games.add(game);
            }
        }
        return List.copyOf(games);
    }

    static boolean isExternalMiniProgramGame(long gameId) {
        return gameId == SHI_SAN_ZHANG_GAME_ID;
    }

    static boolean shouldLoadRuleConfig(long gameId) {
        return !isExternalMiniProgramGame(gameId);
    }

    static boolean supportsRoomCreation(long gameId) {
        return gameId == TAIZHOU_MAHJONG_GAME_ID;
    }

    static boolean shouldReturnDirectEntryToLauncher(boolean enteringGameplay) {
        return !enteringGameplay;
    }

    static int defaultGameIndex(List<CreateRoomGame> games, long initialGameId) {
        if (games == null || games.isEmpty()) {
            return -1;
        }
        if (initialGameId > 0L) {
            for (int index = 0; index < games.size(); index++) {
                CreateRoomGame game = games.get(index);
                if (game.gameId() == initialGameId && shouldLoadRuleConfig(game.gameId())) {
                    return index;
                }
            }
        }
        for (int index = 0; index < games.size(); index++) {
            if (shouldLoadRuleConfig(games.get(index).gameId())) {
                return index;
            }
        }
        return -1;
    }
}
