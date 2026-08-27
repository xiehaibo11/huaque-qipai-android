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

    /**
     * @param permission 没有出牌权时为 null。原版 {@code UIMahTouchHandArea:_createMah} 对每张立牌
     *     无条件 {@code setTouchEnabled(true)}，轮次不影响触摸；只有服务端 {@code msgPlayLmts}
     *     下发的限制牌会被 {@code setMahTouchLimit} 关掉触摸。出牌与否由
     *     {@code UIMahLayer:_onPlayMah} 的 {@code getPlayPower()} 决定，不是由触摸决定。
     */
    public static List<TaizhouMahjongPlayGesture.Tile> localHand(
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongPlayPermission permission,
            int renderedMeldCount) {
        Objects.requireNonNull(round, "round");
        List<TaizhouMahjongHandProjection.Tile> hand =
                TaizhouMahjongHandProjection.forSeat(
                        round,
                        round.mySeat(),
                        TaizhouMahjongHandLayout.bottomMeldStartOffset(renderedMeldCount));
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
                            // 限制牌之外都可触摸；没有出牌权时同样可选中、可拖动。
                            permission == null
                                    || permission.playableOriginalIndexes()
                                            .contains(originalIndex),
                            contains(permission, TaizhouMahjongPlayPermission::tingOriginalIndexes,
                                    originalIndex),
                            contains(permission,
                                    TaizhouMahjongPlayPermission::actionMaskOriginalIndexes,
                                    originalIndex),
                            contains(permission,
                                    TaizhouMahjongPlayPermission::preBaoOriginalIndexes,
                                    originalIndex)));
        }
        if (permission != null) {
            validateIndexes(permission, presentIndexes);
        }
        return List.copyOf(result);
    }

    private static boolean contains(
            TaizhouMahjongPlayPermission permission,
            java.util.function.Function<TaizhouMahjongPlayPermission, Set<Integer>> indexes,
            int originalIndex) {
        return permission != null && indexes.apply(permission).contains(originalIndex);
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
