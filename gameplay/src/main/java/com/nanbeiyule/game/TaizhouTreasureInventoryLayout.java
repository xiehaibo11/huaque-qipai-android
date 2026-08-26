package com.nanbeiyule.game;

/** JuBaoPenMyView.csb geometry converted from Cocos bottom-origin coordinates. */
final class TaizhouTreasureInventoryLayout {
    static final TaizhouTreasurePotLayout.Node PANEL =
            node(966.93f, 508.55f, 1607.0f, 870.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node HEADER =
            node(939.72f, 892.88f, 1655.0f, 285.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node TITLE =
            node(417.58f, 917.73f, 258.0f, 66.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node CLOSE =
            node(1712.11f, 919.98f, 100.0f, 100.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node CLOSE_ART =
            node(1712.11f, 919.98f, 53.0f, 54.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node RIGHT_BACKGROUND =
            node(1440.76f, 503.1f, 653.0f, 822.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node LIST =
            node(639.6f, 490.82f, 900.0f, 730.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node EMPTY_DECORATION =
            node(315.98f, 239.824f, 289.0f, 301.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node FOOTER_TEXT =
            node(941.72f, 46.93f, 610.0f, 38.0f, 0.5f, 0.5f);

    static final TaizhouTreasurePotLayout.Node DETAIL_ICON =
            rightChild(331.59f, 638.146f, 180.0f, 180.0f, 0.5f, 0.5f, 1.7f);
    static final TaizhouTreasurePotLayout.Node DETAIL_NAME =
            rightChild(332.699f, 457.723f, 300.0f, 50.0f, 0.5f, 0.5f, 1.0f);
    static final TaizhouTreasurePotLayout.Node DETAIL_QUALITY =
            rightChild(187.651f, 390.129f, 91.0f, 41.0f, 0.5f, 0.5f, 1.0f);
    static final TaizhouTreasurePotLayout.Node DETAIL_LEVEL =
            rightChild(498.028f, 453.655f, 91.0f, 41.0f, 0.5f, 0.5f, 1.0f);
    static final TaizhouTreasurePotLayout.Node DETAIL_FORTUNE =
            rightChild(242.208f, 393.358f, 215.0f, 47.0f, 0.0f, 0.5f, 1.0f);
    static final TaizhouTreasurePotLayout.Node DETAIL_DESCRIPTION =
            rightChild(349.342f, 285.817f, 573.0f, 150.0f, 0.5f, 0.5f, 1.0f);
    static final TaizhouTreasurePotLayout.Node DETAIL_REMAINING =
            rightChild(152.693f, 179.679f, 391.0f, 42.0f, 0.0f, 0.5f, 1.0f);
    static final TaizhouTreasurePotLayout.Node PLACE =
            rightChild(348.638f, 86.4284f, 268.0f, 84.0f, 0.5f, 0.5f, 1.0f);

    static final float CELL_WIDTH = 224.0f;
    static final float CELL_HEIGHT = 220.0f;
    static final int COLUMNS = 4;
    static final int ROWS = 4;
    static final float MAX_SCROLL = CELL_HEIGHT * ROWS - LIST.height();

    private TaizhouTreasureInventoryLayout() {}

    static TaizhouTreasurePotLayout.Node cell(int index, float scroll) {
        int column = index % COLUMNS;
        int row = index / COLUMNS;
        float left = LIST.left() + column * CELL_WIDTH;
        float top = LIST.top() + row * CELL_HEIGHT - scroll;
        return new TaizhouTreasurePotLayout.Node(
                left, top, left + CELL_WIDTH, top + CELL_HEIGHT);
    }

    static int cellAt(float x, float y, float scroll) {
        if (!LIST.contains(x, y)) return -1;
        int column = (int) ((x - LIST.left()) / CELL_WIDTH);
        int row = (int) ((y - LIST.top() + scroll) / CELL_HEIGHT);
        int index = row * COLUMNS + column;
        return index >= 0 && index < COLUMNS * ROWS ? index : -1;
    }

    private static TaizhouTreasurePotLayout.Node rightChild(
            float x, float y, float width, float height,
            float anchorX, float anchorY, float scale) {
        float parentLeft = 1428.29f - 667.0f * 0.5f;
        float parentBottom = 478.61f - 778.0f * 0.5f;
        return node(parentLeft + x, parentBottom + y,
                width * scale, height * scale, anchorX, anchorY);
    }

    private static TaizhouTreasurePotLayout.Node node(
            float x, float y, float width, float height, float anchorX, float anchorY) {
        return TaizhouTreasurePotLayout.fromCocos(
                x, y, width, height, anchorX, anchorY);
    }
}
