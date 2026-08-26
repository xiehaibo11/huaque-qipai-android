package com.nanbeiyule.game.mahjong;

/**
 * Per-seat stacking rules for the discard river and the flower row, ported from
 * the recovered original
 * {@code BasicMahjong/Modules/GameLayer/Config/UIMahConfig2D.lua} and
 * {@code UIMahConfigBase.lua}.
 *
 * <p>Seat ids are the engine's {@code GameDefine.LOCAL_SEAT} values, the same
 * ones {@link TaizhouMahjongTableLayout} uses for its containers.
 *
 * <p>The original drives both areas the same way: an anchor inside the area
 * root, a flag saying whether tiles advance horizontally, and per-axis
 * directions. A renderer places tile {@code n} by stepping {@code n} times from
 * the anchor along the advancing axis, wrapping to the next line after
 * {@link #OUT_SINGLE_LINE_MAX_COUNT} tiles.
 */
public final class MahjongSeatAreaLayout {
    /** {@code MAH_TYPE.LIE_FACE_UP_VERTICAL_TOUP}. */
    public static final int ORIENTATION_LIE_UP_VERTICAL = 5;

    /** {@code MAH_TYPE.LIE_FACE_UP_HORIZONTAL_TOLEFT}. */
    public static final int ORIENTATION_LIE_UP_HORIZONTAL_LEFT = 7;

    /** {@code MAH_TYPE.LIE_FACE_UP_HORIZONTAL_TORIGHT}. */
    public static final int ORIENTATION_LIE_UP_HORIZONTAL_RIGHT = 8;

    /** {@code OutMahsCountConf.SingleLineMaxCount}. */
    public static final int OUT_SINGLE_LINE_MAX_COUNT = 16;

    /**
     * {@code OutMahsCountConf.EmptyCount}, recorded verbatim. The client config
     * does not state what the six slots are indexed by, so nothing here may be
     * described as a confirmed per-seat or per-chair rule.
     */
    public static final int[] OUT_EMPTY_COUNT = {0, 8, 8, 8, 0, 0};

    /** {@code UIMahConfigBase.MaxCombsCount}. */
    public static final int MAX_COMBS_COUNT = 6;

    /** {@code UIMahConfigBase.MaxCombMahsCount}. */
    public static final int MAX_COMB_TILES_COUNT = 8;

    /** {@code UIMahConfigBase.MaxHandMahsCount}. */
    public static final int MAX_HAND_TILES_COUNT = 17;

    /** {@code UIMahConfigBase.MahRenYiValue}: the "any tile" placeholder value. */
    public static final int ANY_TILE_VALUE = 255;

    /** How one area stacks its tiles. */
    public static final class Stacking {
        public final int seat;
        public final float anchorX;
        public final float anchorY;
        public final boolean horizontalAdvance;
        public final int directionX;
        public final int directionY;

        Stacking(
                int seat,
                float anchorX,
                float anchorY,
                boolean horizontalAdvance,
                int directionX,
                int directionY) {
            this.seat = seat;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.horizontalAdvance = horizontalAdvance;
            this.directionX = directionX;
            this.directionY = directionY;
        }
    }

    // OutMahsLayout.FourDirection, in LOCAL_SEAT order 1..4.
    private static final Stacking[] OUT_STACKING = {
        new Stacking(TaizhouMahjongTableLayout.SEAT_LEFT, 0.0f, 1.0f, false, -1, -1),
        new Stacking(TaizhouMahjongTableLayout.SEAT_BOTTOM, 0.0f, 0.0f, true, 1, -1),
        new Stacking(TaizhouMahjongTableLayout.SEAT_RIGHT, 1.0f, 0.0f, false, 1, 1),
        new Stacking(TaizhouMahjongTableLayout.SEAT_TOP, 1.0f, 0.0f, true, -1, 1),
    };

    // FlowerAreaLayout, in LOCAL_SEAT order 1..4. The original uses a single
    // AddDirection here, applied to whichever axis advances.
    private static final Stacking[] FLOWER_STACKING = {
        new Stacking(TaizhouMahjongTableLayout.SEAT_LEFT, 0.0f, 1.0f, false, -1, -1),
        new Stacking(TaizhouMahjongTableLayout.SEAT_BOTTOM, 0.0f, 0.0f, true, 1, 1),
        new Stacking(TaizhouMahjongTableLayout.SEAT_RIGHT, 1.0f, 0.0f, false, 1, 1),
        new Stacking(TaizhouMahjongTableLayout.SEAT_TOP, 1.0f, 1.0f, true, -1, -1),
    };

    // OutMahsMahType.FourDirection, in LOCAL_SEAT order 1..4.
    private static final int[] OUT_ORIENTATION = {
        ORIENTATION_LIE_UP_HORIZONTAL_RIGHT,
        ORIENTATION_LIE_UP_VERTICAL,
        ORIENTATION_LIE_UP_HORIZONTAL_LEFT,
        ORIENTATION_LIE_UP_VERTICAL,
    };

    private MahjongSeatAreaLayout() {}

    /** Returns the discard-river stacking rule for {@code seat}. */
    public static Stacking outStacking(int seat) {
        return OUT_STACKING[seatIndex(seat)];
    }

    /** Returns the flower-row stacking rule for {@code seat}. */
    public static Stacking flowerStacking(int seat) {
        return FLOWER_STACKING[seatIndex(seat)];
    }

    /**
     * Returns the tile orientation the original lays discards in for {@code seat}:
     * the two vertical seats lie face-up upright, the side seats lie sideways so
     * their faces point across the table.
     */
    public static int outOrientation(int seat) {
        return OUT_ORIENTATION[seatIndex(seat)];
    }

    /** Returns the zero-based row and column of discard {@code index}. */
    public static int[] outRowColumn(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("negative discard index " + index);
        }
        return new int[] {index / OUT_SINGLE_LINE_MAX_COUNT, index % OUT_SINGLE_LINE_MAX_COUNT};
    }

    private static int seatIndex(int seat) {
        if (seat < TaizhouMahjongTableLayout.SEAT_LEFT
                || seat > TaizhouMahjongTableLayout.SEAT_TOP) {
            throw new IllegalArgumentException("unknown seat " + seat);
        }
        return seat - 1;
    }
}
