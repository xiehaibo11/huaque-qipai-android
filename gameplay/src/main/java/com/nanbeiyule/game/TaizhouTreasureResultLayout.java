package com.nanbeiyule.game;

/** JuBaoPenLotteryView.csb geometry including the Lua one/five draw placement rules. */
final class TaizhouTreasureResultLayout {
    static final TaizhouTreasurePotLayout.Node CLOSE =
            node(1712.11f, 919.98f, 100.0f, 100.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node CLOSE_ART =
            node(1696.8854f, 942.8273f, 60.0f, 60.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node TITLE =
            node(958.64f, 894.93f, 688.0f, 343.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node FORTUNE =
            node(958.64f, 811.48f, 442.0f, 99.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node CONTENT =
            node(960.0f, 496.0f, 1920.0f, 500.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node DRAW =
            node(959.788f, 155.059f, 490.0f, 128.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node DRAW_ART =
            node(959.788f, 155.059f, 502.0f, 141.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node DRAW_LABEL =
            node(887.366f, 169.1774f, 199.0f, 57.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node DRAW_DIAMOND =
            node(1029.662f, 154.0606f, 62.56f, 48.28f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node DRAW_PRICE =
            node(1061.267f, 159.1806f, 74.0f, 39.0f, 0.0f, 0.5f);
    static final TaizhouTreasurePotLayout.Node DISCOUNT =
            node(1133.787f, 237.401f, 154.0f, 72.0f, 0.5f, 0.5f);
    static final TaizhouTreasurePotLayout.Node DISCOUNT_TEXT =
            node(1133.787f, 243.161f, 66.0f, 47.0f, 0.5f, 0.5f);

    private TaizhouTreasureResultLayout() {}

    static float itemCenterX(int count, int index) {
        if (count == 1) return 960.0f;
        return 960.0f + (index - 2) * 350.0f;
    }

    static float itemCenterY() {
        return 584.0f;
    }

    static float itemScale(int count) {
        return count == 1 ? 1.0f : 0.9f;
    }

    private static TaizhouTreasurePotLayout.Node node(
            float x, float y, float width, float height, float anchorX, float anchorY) {
        return TaizhouTreasurePotLayout.fromCocos(
                x, y, width, height, anchorX, anchorY);
    }
}
