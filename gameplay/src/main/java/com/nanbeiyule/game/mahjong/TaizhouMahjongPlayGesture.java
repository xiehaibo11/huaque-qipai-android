package com.nanbeiyule.game.mahjong;

import java.util.List;
import java.util.Objects;

/** Exact 2D hand gesture port, gated by a one-use authoritative play token. */
public final class TaizhouMahjongPlayGesture {
    public enum Mode {
        SINGLE_CLICK,
        DOUBLE_CLICK
    }

    public record Tile(
            int index,
            int value,
            float nodeX,
            float nodeY,
            float left,
            float bottom,
            float right,
            float top,
            boolean visible,
            boolean touchEnabled,
            boolean ting,
            boolean actionMask,
            boolean preBao) {
        public Tile {
            if (index < 0 || right < left || top < bottom) {
                throw new IllegalArgumentException("invalid hand tile bounds");
            }
        }

        public Tile withTing(boolean nextTing) {
            return copy(visible, touchEnabled, nextTing, actionMask, preBao);
        }

        public Tile withTouchEnabled(boolean nextTouchEnabled) {
            return copy(visible, nextTouchEnabled, ting, actionMask, preBao);
        }

        public Tile withActionMask(boolean nextActionMask) {
            return copy(visible, touchEnabled, ting, nextActionMask, preBao);
        }

        public Tile withPreBao(boolean nextPreBao) {
            return copy(visible, touchEnabled, ting, actionMask, nextPreBao);
        }

        public Tile translatedY(float deltaY) {
            return new Tile(
                    index,
                    value,
                    nodeX,
                    nodeY + deltaY,
                    left,
                    bottom + deltaY,
                    right,
                    top + deltaY,
                    visible,
                    touchEnabled,
                    ting,
                    actionMask,
                    preBao);
        }

        float height() {
            return top - bottom;
        }

        boolean contains(float x, float y) {
            return visible && x >= left && x <= right && y >= bottom && y <= top;
        }

        private Tile copy(
                boolean nextVisible,
                boolean nextTouchEnabled,
                boolean nextTing,
                boolean nextActionMask,
                boolean nextPreBao) {
            return new Tile(
                    index,
                    value,
                    nodeX,
                    nodeY,
                    left,
                    bottom,
                    right,
                    top,
                    nextVisible,
                    nextTouchEnabled,
                    nextTing,
                    nextActionMask,
                    nextPreBao);
        }
    }

    public static final class PlayIntent {
        public final int tileIndex;
        public final int tileValue;
        public final String actionToken;

        private PlayIntent(int tileIndex, int tileValue, String actionToken) {
            this.tileIndex = tileIndex;
            this.tileValue = tileValue;
            this.actionToken = actionToken;
        }
    }

    public static final class Result {
        public final boolean handled;
        public final Integer selectedIndex;
        public final boolean dragging;
        public final Integer draggedIndex;
        public final Integer draggedValue;
        public final float dragNodeX;
        public final float dragNodeY;
        public final boolean dragTinted;
        public final PlayIntent playIntent;

        private Result(
                boolean handled,
                Integer selectedIndex,
                boolean dragging,
                Integer draggedIndex,
                Integer draggedValue,
                float dragNodeX,
                float dragNodeY,
                boolean dragTinted,
                PlayIntent playIntent) {
            this.handled = handled;
            this.selectedIndex = selectedIndex;
            this.dragging = dragging;
            this.draggedIndex = draggedIndex;
            this.draggedValue = draggedValue;
            this.dragNodeX = dragNodeX;
            this.dragNodeY = dragNodeY;
            this.dragTinted = dragTinted;
            this.playIntent = playIntent;
        }
    }

    private final Mode mode;
    private Integer selectedIndex;
    private Tile pressedTile;
    private float touchBeginX;
    private float touchBeginY;
    private int movePointCount;
    private boolean dragging;
    private float dragNodeX;
    private float dragNodeY;
    private boolean dragTinted;
    private String playActionToken;

    public TaizhouMahjongPlayGesture(Mode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public void replacePlayPermission(String actionToken) {
        if (actionToken == null || actionToken.isBlank()) {
            throw new IllegalArgumentException("authoritative action token is required");
        }
        playActionToken = actionToken;
    }

    public boolean hasPlayPermission() {
        return playActionToken != null;
    }

    public void resetInteraction() {
        selectedIndex = null;
        clearActiveTouch();
    }

    public Result onDown(Tile tile, float x, float y, boolean animationRunning) {
        if (animationRunning
                || tile == null
                || !tile.visible()
                || !tile.touchEnabled()
                || !tile.contains(x, y)) {
            return result(false, null);
        }
        pressedTile = tile;
        touchBeginX = x;
        touchBeginY = y;
        movePointCount = 1;
        dragging = false;
        PlayIntent intent = null;
        if (isSingleClick(tile)) {
            selectedIndex = tile.index();
        } else if (Objects.equals(selectedIndex, tile.index())) {
            selectedIndex = null;
            intent = dispatch(tile);
        } else {
            selectedIndex = tile.index();
        }
        return result(true, intent);
    }

    public Result onMove(float x, float y, List<Tile> currentTiles) {
        if (pressedTile == null) {
            return result(false, null);
        }
        movePointCount++;
        if (!dragging
                && movePointCount < 6
                && moveAngle(touchBeginX, touchBeginY, x, y) > 40.0) {
            dragging = true;
            selectedIndex = null;
            dragTinted = !pressedTile.actionMask() && !pressedTile.preBao();
        }
        if (dragging) {
            dragNodeX = x - touchBeginX + pressedTile.nodeX();
            dragNodeY = y - touchBeginY + pressedTile.nodeY();
        } else {
            for (Tile tile : currentTiles) {
                if (tile.visible()
                        && tile.touchEnabled()
                        && tile.contains(x, y)
                        && !Objects.equals(selectedIndex, tile.index())) {
                    selectedIndex = tile.index();
                }
            }
        }
        return result(true, null);
    }

    public Result onEnd(float x, float y, List<Tile> currentTiles) {
        return finish(x, y, currentTiles);
    }

    public Result onCancel(float x, float y, List<Tile> currentTiles) {
        return finish(x, y, currentTiles);
    }

    private Result finish(float x, float y, List<Tile> currentTiles) {
        if (pressedTile == null) {
            return result(false, null);
        }
        Tile originalPressedTile = pressedTile;
        PlayIntent intent = null;
        if (dragging) {
            if (dragNodeY
                    > originalPressedTile.nodeY() + originalPressedTile.height() * 0.75f) {
                intent = dispatch(originalPressedTile);
            }
            clearActiveTouch();
            return result(true, intent);
        }

        Tile selected = findTile(selectedIndex, currentTiles);
        if (touchBeginY - y >= 20.0f && selected != null && selected.visible()) {
            selectedIndex = null;
        }
        selected = findTile(selectedIndex, currentTiles);
        if (isSingleClick(originalPressedTile)) {
            selectedIndex = null;
            if (selected != null && selected.visible() && selected.contains(x, y)) {
                intent = dispatch(selected);
            }
        } else if (selected == null || !selected.visible() || !selected.contains(x, y)) {
            selectedIndex = null;
        }
        clearActiveTouch();
        return result(true, intent);
    }

    private PlayIntent dispatch(Tile tile) {
        if (playActionToken == null) {
            return null;
        }
        PlayIntent intent = new PlayIntent(tile.index(), tile.value(), playActionToken);
        playActionToken = null;
        return intent;
    }

    private boolean isSingleClick(Tile tile) {
        return mode == Mode.SINGLE_CLICK && !tile.ting();
    }

    private void clearActiveTouch() {
        pressedTile = null;
        dragging = false;
        dragTinted = false;
        movePointCount = 0;
    }

    private Result result(boolean handled, PlayIntent intent) {
        Integer draggedIndex = dragging && pressedTile != null ? pressedTile.index() : null;
        Integer draggedValue = dragging && pressedTile != null ? pressedTile.value() : null;
        return new Result(
                handled,
                selectedIndex,
                dragging,
                draggedIndex,
                draggedValue,
                dragNodeX,
                dragNodeY,
                dragTinted,
                intent);
    }

    private static Tile findTile(Integer index, List<Tile> tiles) {
        if (index == null) {
            return null;
        }
        for (Tile tile : tiles) {
            if (tile.index() == index) {
                return tile;
            }
        }
        return null;
    }

    static double moveAngle(float startX, float startY, float endX, float endY) {
        double distanceX = Math.abs(endX - startX);
        double distanceY = Math.abs(endY - startY);
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        if (distanceY <= 0.0 || distance <= 40.0) {
            return 0.0;
        }
        return distanceX == 0.0
                ? 90.0
                : Math.toDegrees(Math.atan(Math.abs(distanceY / distanceX)));
    }
}
