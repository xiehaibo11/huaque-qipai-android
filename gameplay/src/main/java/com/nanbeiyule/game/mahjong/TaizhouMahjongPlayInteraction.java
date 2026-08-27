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
    private TaizhouMahjongPlayGesture.Mode fallbackMode =
            TaizhouMahjongPlayGesture.Mode.SINGLE_CLICK;
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
        replace(nextRound, nextPermission, nextRenderedMeldCount, fallbackMode);
    }

    /**
     * 原版 {@code UIMahTouchHandArea} 一直挂在手牌上：没轮到自己也能选中、抬牌、拖动，
     * 只是 {@code UIMahLayer:_onPlayMah} 里 {@code getPlayPower()} 为假时不发出牌。
     * 所以手势对象必须随手牌存在，而不是随出牌权存在；出牌权只决定 {@code dispatch} 能否成牌。
     *
     * @param nextMode 没有出牌权时用玩家设置里的单击/双击（原版 {@code MahSettingKey.PlayType}）。
     */
    public void replace(
            TaizhouMahjongVisibleRound nextRound,
            TaizhouMahjongPlayPermission nextPermission,
            int nextRenderedMeldCount,
            TaizhouMahjongPlayGesture.Mode nextMode) {
        TaizhouMahjongPlayGesture.Mode mode =
                nextPermission != null ? nextPermission.mode() : nextMode;
        if (Objects.equals(round, nextRound)
                && Objects.equals(permission, nextPermission)
                && renderedMeldCount == nextRenderedMeldCount
                && fallbackMode == mode) {
            return;
        }
        round = nextRound;
        permission = nextPermission;
        renderedMeldCount = nextRenderedMeldCount;
        fallbackMode = mode;
        gesture = null;
        tiles = List.of();
        visualState = VisualState.EMPTY;
        if (nextRound == null) {
            if (nextPermission != null) {
                throw new IllegalArgumentException("play permission requires a visible round");
            }
            return;
        }
        tiles =
                TaizhouMahjongPlayProjection.localHand(
                        nextRound, nextPermission, nextRenderedMeldCount);
        gesture = new TaizhouMahjongPlayGesture(mode);
        if (nextPermission != null
                && !consumedActionTokens.contains(nextPermission.actionToken())) {
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
        if (tile == null) {
            tile = selectedBaseTileAt(x, cocosY);
        }
        return apply(gesture.onDown(tile, x, cocosY, animationRunning));
    }

    public TaizhouMahjongPlayGesture.Result onSelectedTileDown(float x, float cocosY) {
        Integer selectedIndex = visualState.selectedIndex();
        if (gesture == null || selectedIndex == null) {
            return null;
        }
        TaizhouMahjongPlayGesture.Tile tile =
                TaizhouMahjongPlayProjection.topTileAt(activeTiles(), x, cocosY);
        if (tile == null) {
            tile = selectedBaseTileAt(x, cocosY);
        }
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

    private TaizhouMahjongPlayGesture.Tile selectedBaseTileAt(float x, float cocosY) {
        Integer selected = visualState.selectedIndex();
        if (selected == null) {
            return null;
        }
        for (TaizhouMahjongPlayGesture.Tile tile : tiles) {
            if (tile.index() == selected && tile.touchEnabled() && tile.contains(x, cocosY)) {
                return tile;
            }
        }
        return null;
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
