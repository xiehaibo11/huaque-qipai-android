package com.nanbeiyule.game.mahjong;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Retains the original touch state while preventing stale action-token reuse. */
public final class TaizhouMahjongPlayInteraction {
    public record VisualState(
            Integer selectedIndex,
            boolean dragging,
            Integer draggedIndex,
            Integer draggedValue,
            float dragNodeX,
            float dragNodeY,
            boolean dragTinted) {
        private static final VisualState EMPTY =
                new VisualState(null, false, null, null, 0.0f, 0.0f, false);
    }

    private TaizhouMahjongVisibleRound round;
    private TaizhouMahjongPlayPermission permission;
    private int renderedMeldCount;
    private TaizhouMahjongPlayGesture gesture;
    private List<TaizhouMahjongPlayGesture.Tile> tiles = List.of();
    private VisualState visualState = VisualState.EMPTY;
    private final Set<String> consumedActionTokens = new HashSet<>();

    public void replace(
            TaizhouMahjongVisibleRound nextRound,
            TaizhouMahjongPlayPermission nextPermission) {
        replace(
                nextRound,
                nextPermission,
                nextRound == null ? 0 : nextRound.handAt(nextRound.mySeat()).meldCount());
    }

    public void replace(
            TaizhouMahjongVisibleRound nextRound,
            TaizhouMahjongPlayPermission nextPermission,
            int nextRenderedMeldCount) {
        if (Objects.equals(round, nextRound)
                && Objects.equals(permission, nextPermission)
                && renderedMeldCount == nextRenderedMeldCount) {
            return;
        }
        round = nextRound;
        permission = nextPermission;
        renderedMeldCount = nextRenderedMeldCount;
        gesture = null;
        tiles = List.of();
        visualState = VisualState.EMPTY;
        if (nextPermission == null) {
            return;
        }
        if (nextRound == null) {
            throw new IllegalArgumentException("play permission requires a visible round");
        }
        tiles =
                TaizhouMahjongPlayProjection.localHand(
                        nextRound, nextPermission, nextRenderedMeldCount);
        gesture = new TaizhouMahjongPlayGesture(nextPermission.mode());
        if (!consumedActionTokens.contains(nextPermission.actionToken())) {
            gesture.replacePlayPermission(nextPermission.actionToken());
        }
    }

    /**
     * 出牌命令没有落到服务端时释放该 actionToken。
     *
     * <p>{@link #apply} 一产生 playIntent 就把 token 记为已消费，用来挡住同一个出牌权窗口内的重复
     * 提交。但命令可能根本没被受理（{@code commandInFlight}、revision 过期）或提交后失败（网络
     * 错误）；此时服务端的出牌权仍然开着，本地却已把 token 作废，手牌会永久点不动。服务端始终是
     * 权威：只要它还在下发同一个出牌权，本次尝试就没有落地，必须把手牌交还给玩家。
     */
    public void releasePlayPermission(String actionToken) {
        if (actionToken == null || !consumedActionTokens.remove(actionToken)) {
            return;
        }
        if (gesture != null
                && permission != null
                && actionToken.equals(permission.actionToken())) {
            gesture.replacePlayPermission(actionToken);
        }
    }

    public List<TaizhouMahjongPlayGesture.Tile> tiles() {
        return tiles;
    }

    public VisualState visualState() {
        return visualState;
    }

    public boolean hasPlayPermission() {
        return gesture != null && gesture.hasPlayPermission();
    }

    public TaizhouMahjongPlayGesture.Result onDown(
            float x, float cocosY, boolean animationRunning) {
        if (gesture == null) {
            return null;
        }
        TaizhouMahjongPlayGesture.Tile tile =
                TaizhouMahjongPlayProjection.topTileAt(activeTiles(), x, cocosY);
        return apply(gesture.onDown(tile, x, cocosY, animationRunning));
    }

    public TaizhouMahjongPlayGesture.Result onSelectedTileDown(float x, float cocosY) {
        Integer selectedIndex = visualState.selectedIndex();
        if (gesture == null || selectedIndex == null) {
            return null;
        }
        TaizhouMahjongPlayGesture.Tile tile =
                TaizhouMahjongPlayProjection.topTileAt(activeTiles(), x, cocosY);
        if (tile == null || tile.index() != selectedIndex) {
            return null;
        }
        return apply(gesture.onDown(tile, x, cocosY, false));
    }

    public TaizhouMahjongPlayGesture.Result onMove(float x, float cocosY) {
        return gesture == null ? null : apply(gesture.onMove(x, cocosY, activeTiles()));
    }

    public TaizhouMahjongPlayGesture.Result onEnd(float x, float cocosY) {
        return gesture == null ? null : apply(gesture.onEnd(x, cocosY, activeTiles()));
    }

    public TaizhouMahjongPlayGesture.Result onCancel(float x, float cocosY) {
        return gesture == null ? null : apply(gesture.onCancel(x, cocosY, activeTiles()));
    }

    private List<TaizhouMahjongPlayGesture.Tile> activeTiles() {
        Integer selected = visualState.selectedIndex();
        if (selected == null) {
            return tiles;
        }
        return tiles.stream()
                .map(
                        tile ->
                                tile.index() == selected
                                        ? tile.translatedY(
                                                TaizhouMahjongHandLayout.SELECTED_RAISE)
                                        : tile)
                .toList();
    }

    private TaizhouMahjongPlayGesture.Result apply(
            TaizhouMahjongPlayGesture.Result result) {
        if (result.handled) {
            visualState =
                    new VisualState(
                            result.selectedIndex,
                            result.dragging,
                            result.draggedIndex,
                            result.draggedValue,
                            result.dragNodeX,
                            result.dragNodeY,
                            result.dragTinted);
        }
        if (result.playIntent != null) {
            consumedActionTokens.add(result.playIntent.actionToken);
        }
        return result;
    }
}
