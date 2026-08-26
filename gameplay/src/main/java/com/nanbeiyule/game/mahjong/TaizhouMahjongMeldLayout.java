package com.nanbeiyule.game.mahjong;

import com.nanbeiyule.game.gameplay.GameplayMeld;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.ArrayList;
import java.util.List;

/**
 * Meld-area (副露) geometry for the 2D Taizhou mahjong table, recovered from
 * the original BasicMahjong client:
 *
 * <ul>
 *   <li>{@code UIMahConfig2D.luac:383-480 CombMahLayout} — per-seat normal /
 *       face-down poses and the from-seat rotation marker (倒牌显示喂牌方向).
 *   <li>{@code UIMahConfig2D.luac:482-565 HandAreaLayout} — comb scale, comb
 *       distance, anchor and sequence direction per seat.
 *   <li>{@code UIMahConfigBase.luac:20-45 CombAlignIndexConfig} — which tile
 *       the fourth (kong) tile stacks on.
 *   <li>{@code View2D/UIMahComb.luac:26-215} — per-tile pose selection
 *       (_resetMahs/_getMahType), chow reversal on TOP/LEFT
 *       (_sortChowMahValues) and the within-comb layouts.
 *   <li>{@code View2D/UIMahHandArea.luac:321-400} — comb anchor, scale, start
 *       position and bounding-box sequencing (_updateCombsPosition).
 * </ul>
 *
 * <p>The server {@code tiles} order is consumed as the original comb display
 * order (the incoming tile first, matching {@code getShowMahValues}); only the
 * original TOP/LEFT chow reversal is applied on top. The renderer draws each
 * placement with an anchor of (0.5,0.5), the tile-centre convention of
 * {@code _updateVerticalLayoutAndSize}/{_updateHorizontalLayoutAndSize}.
 */
public final class TaizhouMahjongMeldLayout {
    /** One resolved tile: pose, value, centre in design space and draw scale. */
    public static final class TilePlacement {
        public final int pose;
        public final int tileValue;
        public final float designX;
        public final float cocosY;
        public final float scale;

        private TilePlacement(int pose, int tileValue, float designX, float cocosY, float scale) {
            this.pose = pose;
            this.tileValue = tileValue;
            this.designX = designX;
            this.cocosY = cocosY;
            this.scale = scale;
        }
    }

    private static final class SeatRule {
        final TaizhouMahjongTableLayout.Slot root;
        final float combScale;
        final float anchorX;
        final float anchorY;
        final boolean sequenceOnX;
        final int addDirection;
        final float combDistance;
        final boolean rowLayout;
        final int normalPose;
        final int faceDownPose;

        SeatRule(
                TaizhouMahjongTableLayout.Slot root,
                float combScale,
                float anchorX,
                float anchorY,
                boolean sequenceOnX,
                int addDirection,
                float combDistance,
                boolean rowLayout,
                int normalPose,
                int faceDownPose) {
            this.root = root;
            this.combScale = combScale;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.sequenceOnX = sequenceOnX;
            this.addDirection = addDirection;
            this.combDistance = combDistance;
            this.rowLayout = rowLayout;
            this.normalPose = normalPose;
            this.faceDownPose = faceDownPose;
        }
    }

    // UIMahConfig2D.CombMahLayout: [localSeat][fromLocalSeat] -> {rotatePose, mahIndex}.
    // Indexed by the engine LOCAL_SEAT enum (LEFT 1, BOTTOM 2, RIGHT 3, TOP 4).
    private static final int[][][] ROTATE = {
        null,
        {null, {8, 0}, {6, 1}, {8, 2}, {5, 3}}, // LEFT
        {null, {7, 1}, {5, 0}, {8, 3}, {5, 2}}, // BOTTOM
        {null, {7, 2}, {6, 1}, {7, 0}, {5, 3}}, // RIGHT
        {null, {7, 1}, {5, 2}, {8, 3}, {5, 0}}, // TOP
    };

    // UIMahConfigBase.CombAlignIndexConfig[localSeat][arrowMahIndex][index-3]:
    // the fourth+ tile stacks on the listed 1-based tile; arrowMahIndex is 0..3.
    private static final int[][][] ALIGN = {
        null,
        {{2, 3, 1, 4, 6}, {2, 3, 4, 5, 6}, {2, 3, 1, 4, 6}, {2, 1, 4, 5, 6}}, // LEFT
        {{2, 1, 3, 4, 5}, {2, 3, 4, 5, 6}, {2, 1, 3, 4, 5}, {2, 1, 4, 5, 6}}, // BOTTOM
        {{2, 1, 3, 4, 5}, {2, 3, 4, 5, 6}, {2, 1, 3, 4, 5}, {2, 1, 4, 5, 6}}, // RIGHT
        {{2, 3, 1, 4, 6}, {2, 3, 4, 5, 6}, {2, 3, 1, 4, 6}, {2, 1, 4, 5, 6}}, // TOP
    };

    // UIMahHandArea:_getCombScale applies this factor for the default
    // ARROW_BY_MAH meld mode before the per-seat HandAreaLayout scale.
    private static final float ARROW_BY_MAH_COMB_SCALE = 0.9f;

    private TaizhouMahjongMeldLayout() {}

    /**
     * Resolves every meld tile of one seat in apply order. {@code melds} must
     * already be filtered to melds whose local seat is {@code localSeat}.
     */
    public static List<TilePlacement> seatMelds(
            int localSeat, List<GameplayMeld> melds, int mySeat, int chairCount) {
        SeatRule rule = rule(localSeat);
        List<TilePlacement> placements = new ArrayList<>();
        float cursor = 0.0f;
        for (GameplayMeld meld : melds) {
            int fromLocalSeat =
                    TaizhouMahjongSeatMapper.toLocalSeat(meld.fromSeat(), mySeat, chairCount);
            CombTiles comb = displayTiles(rule, localSeat, meld, fromLocalSeat);
            float extent = rule.rowLayout ? comb.contentWidth : comb.contentHeight;
            float anchorPos = cursor;
            float originX =
                    rule.anchorX == 1.0f
                            ? (rule.sequenceOnX ? anchorPos : 0.0f) - comb.contentWidth * rule.combScale
                            : rule.sequenceOnX ? anchorPos : 0.0f;
            float originY =
                    rule.anchorY == 1.0f
                            ? (rule.sequenceOnX ? 0.0f : anchorPos) - comb.contentHeight * rule.combScale
                            : rule.sequenceOnX ? 0.0f : anchorPos;
            float effectiveScale = rule.combScale * rule.root.scale;
            for (int index = 0; index < comb.values.size(); index++) {
                placements.add(
                        new TilePlacement(
                                comb.poses.get(index),
                                comb.values.get(index),
                                rule.root.designX()
                                        + (originX + comb.centersX.get(index) * rule.combScale)
                                                * rule.root.scale,
                                rule.root.cocosY()
                                        + (originY + comb.centersY.get(index) * rule.combScale)
                                                * rule.root.scale,
                                effectiveScale));
            }
            cursor = anchorPos + rule.addDirection * (extent * rule.combScale + rule.combDistance);
        }
        return List.copyOf(placements);
    }

    /**
     * 结算页行内副露（{@code WinLost/ItemMahsArea.luac}）：每行都按 BOTTOM 座位组合，
     * comb 缩放恒为 1（{@code _getCombScale} 覆盖），起点 {@code (0,0)}，组间距
     * {@code CombDistance=20}；{@code updataHandMah} 不带 fromLocalSeat，所以结算行
     * 不画喂牌旋转标记。返回的坐标是 {@code _KW_PANEL_HAND_CARD} 面板内的未缩放单位，
     * 面板的 0.5 缩放由调用方换算。
     */
    public static SettleMeldRow settleRowMelds(List<GameplayMeld> melds) {
        SeatRule rule = rule(TaizhouMahjongTableLayout.SEAT_BOTTOM);
        List<TilePlacement> placements = new ArrayList<>();
        float cursor = 0.0f;
        for (GameplayMeld meld : melds) {
            CombTiles comb =
                    displayTiles(
                            rule,
                            TaizhouMahjongTableLayout.SEAT_BOTTOM,
                            meld,
                            TaizhouMahjongTableLayout.SEAT_BOTTOM);
            for (int index = 0; index < comb.values.size(); index++) {
                placements.add(
                        new TilePlacement(
                                comb.poses.get(index),
                                comb.values.get(index),
                                cursor + comb.centersX.get(index),
                                comb.centersY.get(index),
                                1.0f));
            }
            cursor += comb.contentWidth + 20.0f;
        }
        // 手牌起点 = 末组右缘 + 20（_getHandMahsStartPos），与组间距同值时即最终游标。
        return new SettleMeldRow(List.copyOf(placements), cursor);
    }

    /** 结算行副露的解析结果：面板内牌位与手牌起点（无副露时为 0）。 */
    public record SettleMeldRow(List<TilePlacement> placements, float handStartX) {
        public SettleMeldRow {
            placements = List.copyOf(placements);
        }
    }

    private static final class CombTiles {
        final List<Integer> values = new ArrayList<>(4);
        final List<Integer> poses = new ArrayList<>(4);
        final List<Float> centersX = new ArrayList<>(4);
        final List<Float> centersY = new ArrayList<>(4);
        float contentWidth;
        float contentHeight;
    }

    private static CombTiles displayTiles(
            SeatRule rule, int localSeat, GameplayMeld meld, int fromLocalSeat) {
        List<Integer> values = displayValues(rule, localSeat, meld);
        int arrowMahIndex = 0;
        int rotatePose = rule.normalPose;
        if (meld.combType() != MahjongCombType.CONCEALED_KONG && fromLocalSeat != localSeat) {
            int[] rotate = ROTATE[localSeat][fromLocalSeat];
            if (rotate[1] > 0) {
                arrowMahIndex = rotate[1];
                rotatePose = rotate[0];
            }
        }
        CombTiles comb = new CombTiles();
        float advance = 0.0f;
        float maxCross = 0.0f;
        for (int index = 0; index < values.size(); index++) {
            int value = values.get(index);
            int pose =
                            value == MahjongTile.BACK
                            ? rule.faceDownPose
                            : (index + 1 == arrowMahIndex ? rotatePose : rule.normalPose);
            OriginalMahjongTileGeometry.Composition tile =
                    OriginalMahjongTileGeometry.defaultTile(pose, value);
            comb.values.add(value);
            comb.poses.add(pose);
            if (index < 3) {
                placeRowTile(rule, comb, tile, pose, advance, index);
                if (rule.rowLayout) {
                    advance += tile.width;
                    maxCross = Math.max(maxCross, tile.height);
                } else {
                    if (index == 0) {
                        advance = tile.height;
                    } else {
                        advance += MahjongTileSprite.topEdgeWidth(pose);
                    }
                    maxCross = Math.max(maxCross, tile.width);
                }
            } else {
                int alignIndex = ALIGN[localSeat][arrowMahIndex][index - 3] - 1;
                comb.centersX.add(comb.centersX.get(alignIndex));
                comb.centersY.add(
                        comb.centersY.get(alignIndex)
                                + MahjongTileSprite.defaultThickness(pose));
            }
        }
        comb.contentWidth = rule.rowLayout ? advance : maxCross;
        comb.contentHeight = rule.rowLayout ? maxCross : advance;
        return comb;
    }

    private static void placeRowTile(
            SeatRule rule,
            CombTiles comb,
            OriginalMahjongTileGeometry.Composition tile,
            int pose,
            float advance,
            int index) {
        if (rule.rowLayout) {
            comb.centersX.add(advance + tile.width / 2.0f);
            comb.centersY.add(tile.height / 2.0f);
        } else {
            comb.centersX.add(tile.width / 2.0f);
            comb.centersY.add(
                    index == 0
                            ? tile.height / 2.0f
                            : advance - tile.height / 2.0f + MahjongTileSprite.topEdgeWidth(pose));
        }
    }

    /** The original display values: concealed kongs show three backs plus one
     * face; chow combs on TOP/LEFT are reversed ({@code _sortChowMahValues}). */
    private static List<Integer> displayValues(
            SeatRule rule, int localSeat, GameplayMeld meld) {
        List<Integer> values = new ArrayList<>(meld.tiles());
        if (meld.combType() == MahjongCombType.CONCEALED_KONG) {
            values.clear();
            values.add(MahjongTile.BACK);
            values.add(MahjongTile.BACK);
            values.add(MahjongTile.BACK);
            values.add(meld.tiles().get(0));
            return values;
        }
        if (meld.combType() == MahjongCombType.CHOW
                && (localSeat == TaizhouMahjongTableLayout.SEAT_TOP
                        || localSeat == TaizhouMahjongTableLayout.SEAT_LEFT)) {
            List<Integer> reversed = new ArrayList<>(values.size());
            for (int index = values.size() - 1; index >= 0; index--) {
                reversed.add(values.get(index));
            }
            return reversed;
        }
        return values;
    }

    private static SeatRule rule(int localSeat) {
        switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_BOTTOM:
                return new SeatRule(
                        TaizhouMahjongTableLayout.HAND_BOTTOM,
                        0.93f * ARROW_BY_MAH_COMB_SCALE,
                        0.0f,
                        0.0f,
                        true,
                        1,
                        20.0f,
                        true,
                        MahjongTileSprite.LIE_UP_VERTICAL_UP,
                        MahjongTileSprite.LIE_DOWN_VERTICAL);
            case TaizhouMahjongTableLayout.SEAT_RIGHT:
                return new SeatRule(
                        TaizhouMahjongTableLayout.HAND_RIGHT,
                        1.0f * ARROW_BY_MAH_COMB_SCALE,
                        1.0f,
                        0.0f,
                        false,
                        1,
                        2.0f,
                        false,
                        MahjongTileSprite.LIE_UP_HORIZONTAL_LEFT,
                        MahjongTileSprite.LIE_DOWN_HORIZONTAL);
            case TaizhouMahjongTableLayout.SEAT_TOP:
                return new SeatRule(
                        TaizhouMahjongTableLayout.HAND_TOP,
                        1.0f * ARROW_BY_MAH_COMB_SCALE,
                        1.0f,
                        0.0f,
                        true,
                        -1,
                        20.0f,
                        true,
                        MahjongTileSprite.LIE_UP_VERTICAL_UP,
                        MahjongTileSprite.LIE_DOWN_VERTICAL);
            case TaizhouMahjongTableLayout.SEAT_LEFT:
                return new SeatRule(
                        TaizhouMahjongTableLayout.HAND_LEFT,
                        1.0f * ARROW_BY_MAH_COMB_SCALE,
                        0.0f,
                        1.0f,
                        false,
                        -1,
                        2.0f,
                        false,
                        MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT,
                        MahjongTileSprite.LIE_DOWN_HORIZONTAL);
            default:
                throw new IllegalArgumentException("unknown local seat " + localSeat);
        }
    }
}
