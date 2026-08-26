package com.nanbeiyule.game.mahjong;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Original client-side meld detection, ported from
 * {@code BasicMahjong/Modules/GameLayer/MahAlgorithm.lua}.
 *
 * <p>In the original this decides which buttons the action bar lights up; the
 * server stays authoritative for what actually happens. Winning-hand detection
 * is <em>not</em> here and never was: the client receives ting/hu information
 * from the server ({@code msgTingMahInfo}, {@code CanHuMahs}), so nothing in
 * this class may be extended into a hu checker and called original behaviour.
 *
 * <p>Joker handling follows the original comment: when 3W is the joker and the
 * white dragon stands in for it, an incoming 3W is mapped to the white dragon so
 * that a 2W/4W holder can still chow it.
 *
 * <p>Tile 0 means "no tile". The original relies on its callers never asking for
 * a chow on an honour tile; this port instead returns no chow, which is the same
 * observable result without the Lua nil arithmetic.
 */
public final class MahjongMeldAlgorithm {
    /** The original's sentinel for "no such tile". */
    public static final int NO_TILE = 0;

    private MahjongMeldAlgorithm() {}

    /**
     * Returns every chow that {@code inTile} completes, in the original's order:
     * tile first, tile in the middle, then tile last.
     *
     * @param joker the joker tile, or {@link #NO_TILE}
     * @param instead the tile standing in for the joker, or {@link #NO_TILE}
     */
    public static List<int[]> findChow(int[] handTiles, int inTile, int joker, int instead) {
        List<int[]> combs = new ArrayList<>();
        int changedInTile = changeInsteadValue(inTile, joker, instead);

        int next = changeJokerValue(nextOfSameSuit(changedInTile), joker, instead);
        int nextNext =
                changeJokerValue(nextOfSameSuit(nextOfSameSuit(changedInTile)), joker, instead);
        if (countOf(handTiles, next) > 0 && countOf(handTiles, nextNext) > 0) {
            combs.add(new int[] {inTile, next, nextNext});
        }

        int previous = changeJokerValue(previousOfSameSuit(changedInTile), joker, instead);
        if (countOf(handTiles, previous) > 0 && countOf(handTiles, next) > 0) {
            combs.add(new int[] {previous, inTile, next});
        }

        int previousPrevious =
                changeJokerValue(
                        previousOfSameSuit(previousOfSameSuit(changedInTile)), joker, instead);
        if (countOf(handTiles, previousPrevious) > 0 && countOf(handTiles, previous) > 0) {
            combs.add(new int[] {previousPrevious, previous, inTile});
        }

        return combs;
    }

    /** Returns the three-tile pong for {@code inTile}, or an empty array. */
    public static int[] findPong(int[] handTiles, int inTile) {
        if (countOf(handTiles, inTile) >= 2) {
            return new int[] {inTile, inTile, inTile};
        }
        return new int[0];
    }

    /**
     * Returns every kong available. The original short-circuits: when an exposed
     * kong exists it is returned alone, otherwise concealed and fill kongs are
     * collected together.
     *
     * @param drawnTile the tile just drawn ({@code danFang}), or {@link #NO_TILE}
     * @param exposedMelds the player's already visible melds
     */
    public static List<int[]> findKong(
            int[] handTiles, int inTile, int drawnTile, int[][] exposedMelds) {
        List<int[]> combs = findExposedKong(handTiles, inTile);
        if (!combs.isEmpty()) {
            return combs;
        }
        combs.addAll(findConcealedKong(handTiles, drawnTile));
        combs.addAll(findFillKong(handTiles, drawnTile, exposedMelds));
        return combs;
    }

    /** 明杠: three in hand plus the incoming tile. */
    public static List<int[]> findExposedKong(int[] handTiles, int inTile) {
        List<int[]> combs = new ArrayList<>();
        if (countOf(handTiles, inTile) == 3) {
            combs.add(new int[] {inTile, inTile, inTile, inTile});
        }
        return combs;
    }

    /**
     * 暗杠: four of a kind across hand plus the drawn tile. The original renders
     * these as three tile backs followed by the concealed value, and sorts by
     * tile value, so this port keeps both.
     */
    public static List<int[]> findConcealedKong(int[] handTiles, int drawnTile) {
        int[] all = withDrawnTile(handTiles, drawnTile);
        List<Integer> kongTiles = new ArrayList<>();
        for (int tile : all) {
            if (countOf(all, tile) == 4 && !kongTiles.contains(tile)) {
                kongTiles.add(tile);
            }
        }
        kongTiles.sort(Integer::compare);

        List<int[]> combs = new ArrayList<>();
        for (int tile : kongTiles) {
            combs.add(
                    new int[] {
                        MahjongTile.BACK, MahjongTile.BACK, MahjongTile.BACK, tile,
                    });
        }
        return combs;
    }

    /**
     * 补杠: a tile matching one of the player's existing pongs. The original
     * treats a meld as a pong when its first two tiles are equal.
     */
    public static List<int[]> findFillKong(
            int[] handTiles, int drawnTile, int[][] exposedMelds) {
        int[] all = withDrawnTile(handTiles, drawnTile);
        List<Integer> fillTiles = new ArrayList<>();
        if (exposedMelds != null) {
            for (int[] meld : exposedMelds) {
                if (meld == null || meld.length < 2 || meld[0] != meld[1]) {
                    continue;
                }
                if (countOf(all, meld[0]) == 1 && !fillTiles.contains(meld[0])) {
                    fillTiles.add(meld[0]);
                }
            }
        }
        fillTiles.sort(Integer::compare);

        List<int[]> combs = new ArrayList<>();
        for (int tile : fillTiles) {
            combs.add(new int[] {tile, tile, tile, tile});
        }
        return combs;
    }

    /** Returns how many copies of {@code target} are in {@code tiles}. */
    public static int countOf(int[] tiles, int target) {
        if (target == NO_TILE) {
            return 0;
        }
        int count = 0;
        for (int tile : tiles) {
            if (tile == target) {
                count++;
            }
        }
        return count;
    }

    /** Returns the next tile in the same numbered suit, or {@link #NO_TILE}. */
    public static int nextOfSameSuit(int tile) {
        if (!MahjongTile.isSuited(tile) || MahjongTile.rankOf(tile) == 9) {
            return NO_TILE;
        }
        return tile + 1;
    }

    /** Returns the previous tile in the same numbered suit, or {@link #NO_TILE}. */
    public static int previousOfSameSuit(int tile) {
        if (!MahjongTile.isSuited(tile) || MahjongTile.rankOf(tile) == 1) {
            return NO_TILE;
        }
        return tile - 1;
    }

    /** Maps the joker tile onto its stand-in so the joker itself is never melded. */
    public static int changeJokerValue(int tile, int joker, int instead) {
        if (joker == NO_TILE || instead == NO_TILE) {
            return tile;
        }
        return tile == joker ? instead : tile;
    }

    /** Maps the stand-in tile back onto the joker it represents. */
    public static int changeInsteadValue(int tile, int joker, int instead) {
        if (joker == NO_TILE || instead == NO_TILE) {
            return tile;
        }
        return tile == instead ? joker : tile;
    }

    private static int[] withDrawnTile(int[] handTiles, int drawnTile) {
        if (drawnTile == NO_TILE) {
            return handTiles;
        }
        int[] all = Arrays.copyOf(handTiles, handTiles.length + 1);
        all[handTiles.length] = drawnTile;
        return all;
    }
}
