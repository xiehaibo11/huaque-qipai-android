package com.nanbeiyule.game.mahjong;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Builds exact Cocos-space touch bounds for the local authoritative hand. */
public final class TaizhouMahjongPlayProjection {
    private TaizhouMahjongPlayProjection() {}

    public static List<TaizhouMahjongPlayGesture.Tile> localHand(
            TaizhouMahjongVisibleRound round, TaizhouMahjongPlayPermission permission) {
        Objects.requireNonNull(round, "round");
        return localHand(round, permission, round.handAt(round.mySeat()).meldCount());
    }

    public static List<TaizhouMahjongPlayGesture.Tile> localHand(
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongPlayPermission permission,
            int renderedMeldCount) {
        Objects.requireNonNull(round, "round");
        Objects.requireNonNull(permission, "permission");
        List<TaizhouMahjongHandProjection.Tile> hand =
                TaizhouMahjongHandProjection.forSeat(
                        round, round.mySeat(), renderedMeldCount);
        Set<Integer> presentIndexes = new HashSet<>();
        List<TaizhouMahjongPlayGesture.Tile> result = new ArrayList<>(hand.size());
        for (TaizhouMahjongHandProjection.Tile tile : hand) {
            int originalIndex = tile.drawn() ? 0 : tile.handIndex() + 1;
            presentIndexes.add(originalIndex);
            TaizhouMahjongHandLayout.TilePosition position = tile.position();
            OriginalMahjongTileGeometry.Composition composition =
                    OriginalMahjongTileGeometry.defaultTile(position.pose, tile.tileValue());
            float width = composition.width * position.effectiveScale;
            float height = composition.height * position.effectiveScale;
            float left = position.designX - position.anchorX * width;
            float bottom = position.cocosY - position.anchorY * height;
            result.add(
                    new TaizhouMahjongPlayGesture.Tile(
                            originalIndex,
                            tile.tileValue(),
                            position.designX,
                            position.cocosY,
                            left,
                            bottom,
                            left + width,
                            bottom + height,
                            true,
                            permission.playableOriginalIndexes().contains(originalIndex),
                            permission.tingOriginalIndexes().contains(originalIndex),
                            permission.actionMaskOriginalIndexes().contains(originalIndex),
                            permission.preBaoOriginalIndexes().contains(originalIndex)));
        }
        validateIndexes(permission, presentIndexes);
        return List.copyOf(result);
    }

    public static TaizhouMahjongPlayGesture.Tile topTileAt(
            List<TaizhouMahjongPlayGesture.Tile> tiles, float x, float cocosY) {
        Objects.requireNonNull(tiles, "tiles");
        for (int index = tiles.size() - 1; index >= 0; index--) {
            TaizhouMahjongPlayGesture.Tile tile = tiles.get(index);
            if (tile.touchEnabled() && tile.contains(x, cocosY)) {
                return tile;
            }
        }
        return null;
    }

    private static void validateIndexes(
            TaizhouMahjongPlayPermission permission, Set<Integer> presentIndexes) {
        Set<Integer> referenced = new HashSet<>(permission.playableOriginalIndexes());
        referenced.addAll(permission.tingOriginalIndexes());
        referenced.addAll(permission.actionMaskOriginalIndexes());
        referenced.addAll(permission.preBaoOriginalIndexes());
        if (!presentIndexes.containsAll(referenced)) {
            throw new IllegalArgumentException("play permission does not match the visible hand");
        }
    }
}
