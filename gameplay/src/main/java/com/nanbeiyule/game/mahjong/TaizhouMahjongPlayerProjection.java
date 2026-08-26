package com.nanbeiyule.game.mahjong;

import com.nanbeiyule.game.gameplay.GameplaySeat;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Projects authoritative server seats onto the original local player roots. */
public final class TaizhouMahjongPlayerProjection {
    private static final String DEFAULT_AVATAR_KEY = "avatar_default";

    private TaizhouMahjongPlayerProjection() {}

    public static List<Player> players(GameplayTableState state) {
        List<Player> players = new ArrayList<>();
        for (GameplaySeat seat : state.seats()) {
            int localSeat =
                    TaizhouMahjongSeatMapper.toLocalSeat(
                            seat.seatNumber(), state.mySeat(), state.chairCount());
            players.add(
                    new Player(
                            seat,
                            localSeat,
                            TaizhouMahjongPlayerLayout.forLocalSeat(localSeat),
                            state.chairCount() == 4
                                    && state.chengBaoFlagsBySeat()
                                            .getOrDefault(seat.seatNumber(), false)));
        }
        return List.copyOf(players);
    }

    public static List<String> remoteAvatarKeys(GameplayTableState state) {
        Set<String> keys = new LinkedHashSet<>();
        for (GameplaySeat seat : state.seats()) {
            if (!DEFAULT_AVATAR_KEY.equals(seat.avatarKey())) {
                keys.add(seat.avatarKey());
            }
        }
        return List.copyOf(keys);
    }

    public record Player(
            GameplaySeat seat,
            int localSeat,
            TaizhouMahjongPlayerLayout.PlayerSlot slot,
            boolean chengBaoVisible) {}
}
