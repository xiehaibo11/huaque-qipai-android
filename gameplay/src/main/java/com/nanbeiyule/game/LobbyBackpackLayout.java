package com.nanbeiyule.game;

import java.util.Objects;

final class LobbyBackpackLayout {
    enum Kind {
        NONE,
        CLOSE,
        SHOP,
        CATEGORY,
        ITEM,
        USE
    }

    record Target(Kind kind, int index) {
        Target {
            Objects.requireNonNull(kind, "kind");
        }

        static Target none() { return new Target(Kind.NONE, -1); }
        static Target close() { return new Target(Kind.CLOSE, -1); }
        static Target shop() { return new Target(Kind.SHOP, -1); }
        static Target category(int index) { return new Target(Kind.CATEGORY, index); }
        static Target item(int index) { return new Target(Kind.ITEM, index); }
        static Target use() { return new Target(Kind.USE, -1); }
    }

    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;
    static final float GRID_LEFT = 430f;
    static final float GRID_TOP = 114f;
    static final float CELL_WIDTH = 332f;
    static final float CELL_HEIGHT = 420f;
    static final int COLUMNS = 3;
    static final float DETAIL_LEFT = 1414f;
    static final float USE_LEFT = 1500f;
    static final float USE_TOP = 728f;
    static final float USE_RIGHT = 1855f;
    static final float USE_BOTTOM = 841f;

    private LobbyBackpackLayout() {}

    static Target targetAt(float x, float y, int itemCount, boolean canUse) {
        if (inside(x, y, 0f, 0f, 200f, 134f)) return Target.close();
        if (inside(x, y, 1661f, 29f, 1899f, 116f)) return Target.shop();
        for (int index = 0; index < LobbyBackpackCategory.values().length; index++) {
            float top = 137f + index * 147f;
            if (inside(x, y, 0f, top, 298f, top + 147f)) {
                return Target.category(index);
            }
        }
        if (canUse && inside(x, y, USE_LEFT, USE_TOP, USE_RIGHT, USE_BOTTOM)) {
            return Target.use();
        }
        int item = itemIndexAt(x, y, itemCount);
        if (item >= 0) return Target.item(item);
        return Target.none();
    }

    static int itemIndexAt(float x, float contentY, int itemCount) {
        if (x < GRID_LEFT || x >= GRID_LEFT + COLUMNS * CELL_WIDTH || contentY < GRID_TOP) {
            return -1;
        }
        int column = (int) ((x - GRID_LEFT) / CELL_WIDTH);
        int row = (int) ((contentY - GRID_TOP) / CELL_HEIGHT);
        int index = row * COLUMNS + column;
        return index >= 0 && index < itemCount ? index : -1;
    }

    private static boolean inside(
            float x, float y, float left, float top, float right, float bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
}
