package com.nanbeiyule.game.gameplay;

import com.nanbeiyule.game.mahjong.MahjongTile;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.List;
import java.util.Objects;

/**
 * One applied meld (the {@code MELD_APPLIED} PUBLIC event and the {@code melds}
 * snapshot list). {@code tiles} are the real tile values in the order the
 * server broadcasts them; the renderer derives the original display form
 * (concealed-kong backs, from-seat rotation marker) from {@code combType} and
 * {@code fromSeat}.
 */
public record GameplayMeld(int seat, MahjongCombType combType, List<Integer> tiles, int fromSeat) {
    public GameplayMeld {
        if (seat <= 0 || fromSeat <= 0) {
            throw new IllegalArgumentException("meld seats must be positive");
        }
        Objects.requireNonNull(combType, "combType");
        tiles = List.copyOf(Objects.requireNonNull(tiles, "tiles"));
        if (tiles.isEmpty()) {
            throw new IllegalArgumentException("a meld needs at least one tile");
        }
        for (Integer tile : tiles) {
            if (tile == null || !MahjongTile.isValid(tile)) {
                throw new IllegalArgumentException("meld contains an undefined tile");
            }
        }
    }
}
