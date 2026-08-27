package com.nanbeiyule.game.mahjong;

/**
 * Exact default 2D hand positions for Taizhou Mahjong 30109.
 *
 * <p>The seat rules come from {@code UIMahConfig2D.HandAreaLayout}; tile stepping and the
 * separate drawn tile come from {@code UIMahHandArea:_updateHandMahsPosition}. The generic
 * game data defaults to the FOURTEEN layout mode, and neither 30109 class in its inheritance
 * chain overrides that value. This is a UI layout mode, not evidence for an initial deal size.
 */
public final class TaizhouMahjongHandLayout {
    public static final int TAIZHOU_HAND_LAYOUT_MODE = 14;
    public static final int GENERIC_DRAWABLE_CAPACITY = 17;
    public static final float DRAWN_TILE_GAP = 15.0f;
    public static final float BOTTOM_MELD_LENGTH = 405.0f;
    public static final float SELECTED_RAISE = 40.0f;

    /** One tile's anchor position in the original 1920x1080 design space. */
    public static final class TilePosition {
        public final float designX;
        public final float cocosY;
        public final float effectiveScale;
        public final float anchorX;
        public final float anchorY;
        public final int pose;

        public TilePosition(
                float designX,
                float cocosY,
                float effectiveScale,
                float anchorX,
                float anchorY,
                int pose) {
            this.designX = designX;
            this.cocosY = cocosY;
            this.effectiveScale = effectiveScale;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.pose = pose;
        }

        public float androidY() {
            return TaizhouMahjongTableLayout.designY(cocosY);
        }
    }

    private static final class SeatRule {
        final TaizhouMahjongTableLayout.Slot root;
        final boolean horizontal;
        final int direction;
        final float anchorX;
        final float anchorY;
        final int pose;
        final float tileScale;

        SeatRule(
                TaizhouMahjongTableLayout.Slot root,
                boolean horizontal,
                int direction,
                float anchorX,
                float anchorY,
                int pose,
                float tileScale) {
            this.root = root;
            this.horizontal = horizontal;
            this.direction = direction;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.pose = pose;
            this.tileScale = tileScale;
        }
    }

    private TaizhouMahjongHandLayout() {}

    public static TilePosition handTile(
            int localSeat, int zeroBasedIndex, float meldStartOffset, boolean selected) {
        validateIndex(zeroBasedIndex);
        SeatRule rule = rule(localSeat);
        float step = MahjongTileSprite.topEdgeWidth(rule.pose) * rule.tileScale;
        float localAdvance = meldStartOffset + rule.direction * zeroBasedIndex * step;
        float localX = rule.horizontal ? localAdvance : 0.0f;
        float localY = rule.horizontal ? 0.0f : localAdvance;
        if (selected) {
            localY += SELECTED_RAISE;
        }
        return position(rule, localX, localY);
    }

    public static TilePosition drawnTile(int localSeat, int handCount, float meldStartOffset) {
        return drawnTile(localSeat, handCount, meldStartOffset, false);
    }

    public static TilePosition drawnTile(
            int localSeat, int handCount, float meldStartOffset, boolean selected) {
        if (handCount < 0 || handCount > GENERIC_DRAWABLE_CAPACITY) {
            throw new IllegalArgumentException("invalid hand count " + handCount);
        }
        SeatRule rule = rule(localSeat);
        float tilesLength =
                handCount * MahjongTileSprite.topEdgeWidth(rule.pose) * rule.tileScale;
        float localAdvance = meldStartOffset + rule.direction * (tilesLength + DRAWN_TILE_GAP);
        float localX = rule.horizontal ? localAdvance : 0.0f;
        float localY = rule.horizontal ? 0.0f : localAdvance;
        if (selected) {
            localY += SELECTED_RAISE;
        }
        return position(rule, localX, localY);
    }

    private static TilePosition position(SeatRule rule, float localX, float localY) {
        float areaScale = rule.root.scale;
        return new TilePosition(
                rule.root.designX() + localX * areaScale,
                rule.root.cocosY() + localY * areaScale,
                areaScale * rule.tileScale,
                rule.anchorX,
                rule.anchorY,
                rule.pose);
    }

    /**
     * {@code UIMahHandArea:_getHandMahsStartPos} 的 {@code CombTotalLength} 分支：只有
     * BOTTOM 的 {@code HandAreaLayout} 带 {@code CombTotalLength = 405}，手牌起点直接按
     * 组数乘固定长度再乘 {@code AddDirection}；其余三家用实际副露包围盒，见
     * {@link TaizhouMahjongMeldLayout#handStartOffset}。
     */
    public static float bottomMeldStartOffset(int meldCount) {
        if (meldCount < 0 || meldCount > MahjongSeatAreaLayout.MAX_COMBS_COUNT) {
            throw new IllegalArgumentException("invalid meld count " + meldCount);
        }
        return meldCount * BOTTOM_MELD_LENGTH
                * rule(TaizhouMahjongTableLayout.SEAT_BOTTOM).direction;
    }

    private static void validateIndex(int zeroBasedIndex) {
        if (zeroBasedIndex < 0 || zeroBasedIndex >= GENERIC_DRAWABLE_CAPACITY) {
            throw new IllegalArgumentException("invalid tile index " + zeroBasedIndex);
        }
    }

    private static SeatRule rule(int localSeat) {
        switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_LEFT:
                return new SeatRule(
                        TaizhouMahjongTableLayout.HAND_LEFT,
                        false,
                        -1,
                        0.0f,
                        1.0f,
                        MahjongTileSprite.STAND_FACE_TO_LEFT,
                        0.9f);
            case TaizhouMahjongTableLayout.SEAT_BOTTOM:
                return new SeatRule(
                        TaizhouMahjongTableLayout.HAND_BOTTOM,
                        true,
                        1,
                        0.0f,
                        0.0f,
                        MahjongTileSprite.STAND_FACE_FORWARD,
                        1.0f);
            case TaizhouMahjongTableLayout.SEAT_RIGHT:
                return new SeatRule(
                        TaizhouMahjongTableLayout.HAND_RIGHT,
                        false,
                        1,
                        1.0f,
                        0.0f,
                        MahjongTileSprite.STAND_FACE_TO_RIGHT,
                        0.9f);
            case TaizhouMahjongTableLayout.SEAT_TOP:
                return new SeatRule(
                        TaizhouMahjongTableLayout.HAND_TOP,
                        true,
                        -1,
                        1.0f,
                        0.0f,
                        MahjongTileSprite.STAND_FACE_BACKWARD,
                        0.9f);
            default:
                throw new IllegalArgumentException("unknown local seat " + localSeat);
        }
    }
}
