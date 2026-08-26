package com.nanbeiyule.game.gameplay;

import com.nanbeiyule.game.mahjong.MahjongTile;
import java.util.List;
import java.util.Objects;

/**
 * Accumulated flower tiles of one seat (the {@code flowers} snapshot list;
 * {@code FLOWER_REPLACED} events append one flower at a time).
 */
public record GameplaySeatFlowers(int seatNumber, List<Integer> tiles) {
    public GameplaySeatFlowers {
        if (seatNumber <= 0) {
            throw new IllegalArgumentException("seatNumber must be positive");
        }
        tiles = List.copyOf(Objects.requireNonNull(tiles, "tiles"));
        for (Integer tile : tiles) {
            if (tile == null || !MahjongTile.isValid(tile)) {
                throw new IllegalArgumentException("flower area contains an undefined tile");
            }
        }
    }

    public GameplaySeatFlowers withFlower(int flower) {
        if (!MahjongTile.isValid(flower)) {
            throw new IllegalArgumentException("undefined flower tile");
        }
        List<Integer> next = new java.util.ArrayList<>(tiles);
        next.add(flower);
        return new GameplaySeatFlowers(seatNumber, next);
    }
}
