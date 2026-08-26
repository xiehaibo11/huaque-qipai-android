package com.nanbeiyule.game.mahjong;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Exact four-direction discard layout from the recovered UIMahPlayerOutArea. */
public final class TaizhouMahjongDiscardProjection {
    public record Tile(
            int tileIndex,
            int tileValue,
            float designX,
            float cocosY,
            float effectiveScale,
            float anchorX,
            float anchorY,
            int pose,
            int localZOrder) {}

    private record Rule(
            TaizhouMahjongTableLayout.Slot root,
            boolean horizontal,
            int directionX,
            int directionY,
            float anchorX,
            float anchorY,
            int pose) {}

    private record LocalPosition(float x, float y, int zOrder) {}

    private TaizhouMahjongDiscardProjection() {}

    public static List<Tile> forLocalSeat(
            int localSeat,
            int chairCount,
            List<Integer> tileValues,
            int maxLineCount) {
        validate(localSeat, chairCount, maxLineCount);
        List<Integer> values = List.copyOf(Objects.requireNonNull(tileValues, "tileValues"));
        Rule rule = rule(localSeat, chairCount);
        int singleLineMaxCount = chairCount == 2 ? 16 : 8;
        int singleLayerMaxCount = singleLineMaxCount * maxLineCount;
        int topEdge = MahjongTileSprite.topEdgeWidth(rule.pose());
        int sideEdge = MahjongTileSprite.leftRightEdgeWidth(rule.pose());
        int thickness = MahjongTileSprite.defaultThickness(rule.pose());
        List<LocalPosition> localPositions = new ArrayList<>(values.size());
        List<Tile> result = new ArrayList<>(values.size());
        for (int index = 0; index < values.size(); index++) {
            int tileValue = values.get(index);
            if (!MahjongTile.hasTaizhouFace(tileValue)) {
                throw new IllegalArgumentException("river contains an unrenderable tile");
            }
            LocalPosition local =
                    nextPosition(
                            index,
                            localPositions,
                            rule,
                            singleLineMaxCount,
                            singleLayerMaxCount,
                            topEdge,
                            sideEdge,
                            thickness);
            localPositions.add(local);
            result.add(
                    new Tile(
                            index,
                            tileValue,
                            rule.root().designX() + local.x() * rule.root().scale,
                            rule.root().cocosY() + local.y() * rule.root().scale,
                            rule.root().scale,
                            rule.anchorX(),
                            rule.anchorY(),
                            rule.pose(),
                            local.zOrder()));
        }
        return List.copyOf(result);
    }

    private static LocalPosition nextPosition(
            int totalCount,
            List<LocalPosition> previous,
            Rule rule,
            int singleLineMaxCount,
            int singleLayerMaxCount,
            int topEdge,
            int sideEdge,
            int thickness) {
        int layerCount = totalCount % singleLayerMaxCount;
        int layerIndex = (totalCount + singleLayerMaxCount - 1) / singleLayerMaxCount;
        if (layerCount > 0) {
            LocalPosition last = previous.get(previous.size() - 1);
            float x;
            float y;
            if (layerCount % singleLineMaxCount == 0) {
                x = rule.horizontal() ? 0.0f : last.x() + rule.directionX() * sideEdge;
                y =
                        rule.horizontal()
                                ? last.y() + rule.directionY() * sideEdge
                                : thickness * (layerIndex - 1);
            } else {
                x = rule.horizontal() ? last.x() + rule.directionX() * topEdge : last.x();
                y = rule.horizontal() ? last.y() : last.y() + rule.directionY() * topEdge;
            }
            return new LocalPosition(x, y, last.zOrder() - rule.directionY());
        }
        if (totalCount >= singleLayerMaxCount) {
            int zOrder =
                    rule.directionY() > 0
                            ? singleLayerMaxCount * 2
                            : singleLayerMaxCount + 1;
            return new LocalPosition(0.0f, thickness * layerIndex, zOrder);
        }
        return new LocalPosition(0.0f, 0.0f, 0);
    }

    private static void validate(int localSeat, int chairCount, int maxLineCount) {
        if (chairCount != 2 && chairCount != 4) {
            throw new IllegalArgumentException("chairCount must be 2 or 4");
        }
        if (localSeat < TaizhouMahjongTableLayout.SEAT_LEFT
                || localSeat > TaizhouMahjongTableLayout.SEAT_TOP
                || (chairCount == 2
                        && localSeat != TaizhouMahjongTableLayout.SEAT_BOTTOM
                        && localSeat != TaizhouMahjongTableLayout.SEAT_TOP)) {
            throw new IllegalArgumentException("local seat is outside the visible table");
        }
        if ((chairCount == 2 && maxLineCount != 2)
                || (chairCount == 4 && maxLineCount != 2 && maxLineCount != 3)) {
            throw new IllegalArgumentException("unconfirmed river line count");
        }
    }

    private static Rule rule(int localSeat, int chairCount) {
        return switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_LEFT ->
                    new Rule(
                            TaizhouMahjongTableLayout.OUT_LEFT,
                            false,
                            -1,
                            -1,
                            0.0f,
                            1.0f,
                            MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT);
            case TaizhouMahjongTableLayout.SEAT_BOTTOM ->
                    new Rule(
                            chairCount == 2
                                    ? TaizhouMahjongTableLayout.OUT_BOTTOM_SECOND
                                    : TaizhouMahjongTableLayout.OUT_BOTTOM,
                            true,
                            1,
                            -1,
                            0.0f,
                            0.0f,
                            MahjongTileSprite.LIE_UP_VERTICAL_UP);
            case TaizhouMahjongTableLayout.SEAT_RIGHT ->
                    new Rule(
                            TaizhouMahjongTableLayout.OUT_RIGHT,
                            false,
                            1,
                            1,
                            1.0f,
                            0.0f,
                            MahjongTileSprite.LIE_UP_HORIZONTAL_LEFT);
            case TaizhouMahjongTableLayout.SEAT_TOP ->
                    new Rule(
                            chairCount == 2
                                    ? TaizhouMahjongTableLayout.OUT_TOP_SECOND
                                    : TaizhouMahjongTableLayout.OUT_TOP,
                            true,
                            -1,
                            1,
                            1.0f,
                            0.0f,
                            MahjongTileSprite.LIE_UP_VERTICAL_UP);
            default -> throw new IllegalArgumentException("unknown local seat " + localSeat);
        };
    }
}
