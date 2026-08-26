package com.nanbeiyule.game.mahjong;

/**
 * Flower-row (补花区) geometry for the 2D Taizhou mahjong table, recovered from
 * the original BasicMahjong client:
 *
 * <ul>
 *   <li>{@code View2D/UIMahFlowerArea.luac:15-55} — per-seat tile orientation
 *       ({@code _getMahType}) and the anchor/direction stepping
 *       ({@code _updateMahsPosition}).
 *   <li>{@code UIMahConfig2D.luac FlowerAreaLayout} — anchor point, advancing
 *       axis and direction per seat (already mirrored in
 *       {@link MahjongSeatAreaLayout#flowerStacking(int)}).
 *   <li>{@code MahjongLayer.csb KW_FLOWER} — the four area roots, drawn at the
 *       original 0.4 scale ({@link TaizhouMahjongTableLayout#FLOWER_BOTTOM} and
 *       siblings).
 * </ul>
 */
public final class TaizhouMahjongFlowerLayout {
    private TaizhouMahjongFlowerLayout() {}

    /** Returns one flower tile's anchor position in design space. */
    public static TaizhouMahjongHandLayout.TilePosition flowerTile(
            int localSeat, int zeroBasedIndex) {
        if (zeroBasedIndex < 0) {
            throw new IllegalArgumentException("negative flower index " + zeroBasedIndex);
        }
        TaizhouMahjongTableLayout.Slot root = root(localSeat);
        MahjongSeatAreaLayout.Stacking stacking = MahjongSeatAreaLayout.flowerStacking(localSeat);
        int pose = orientation(localSeat);
        float advance = zeroBasedIndex * MahjongTileSprite.topEdgeWidth(pose);
        float localX = 0.0f;
        float localY = 0.0f;
        if (stacking.horizontalAdvance) {
            localX = advance * stacking.directionX;
        } else {
            localY = advance * stacking.directionY;
        }
        return new TaizhouMahjongHandLayout.TilePosition(
                root.designX() + localX * root.scale,
                root.cocosY() + localY * root.scale,
                root.scale,
                stacking.anchorX,
                stacking.anchorY,
                pose);
    }

    /** {@code UIMahFlowerArea:_getMahType}: side seats lie sideways so their
     * faces point across the table, the vertical seats lie face-up upright. */
    public static int orientation(int localSeat) {
        return switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_BOTTOM -> MahjongTileSprite.LIE_UP_VERTICAL_UP;
            case TaizhouMahjongTableLayout.SEAT_RIGHT -> MahjongTileSprite.LIE_UP_HORIZONTAL_LEFT;
            case TaizhouMahjongTableLayout.SEAT_TOP -> MahjongTileSprite.LIE_UP_VERTICAL_UP;
            case TaizhouMahjongTableLayout.SEAT_LEFT -> MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT;
            default -> throw new IllegalArgumentException("unknown local seat " + localSeat);
        };
    }

    private static TaizhouMahjongTableLayout.Slot root(int localSeat) {
        return switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_BOTTOM -> TaizhouMahjongTableLayout.FLOWER_BOTTOM;
            case TaizhouMahjongTableLayout.SEAT_RIGHT -> TaizhouMahjongTableLayout.FLOWER_RIGHT;
            case TaizhouMahjongTableLayout.SEAT_TOP -> TaizhouMahjongTableLayout.FLOWER_TOP;
            case TaizhouMahjongTableLayout.SEAT_LEFT -> TaizhouMahjongTableLayout.FLOWER_LEFT;
            default -> throw new IllegalArgumentException("unknown local seat " + localSeat);
        };
    }
}
