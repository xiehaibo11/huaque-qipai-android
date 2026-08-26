package com.nanbeiyule.game;

import java.util.List;

/** Top-origin 1920x1080 geometry converted directly from JuBaoPenMainView.csb. */
final class TaizhouTreasurePotLayout {
    static final float DESIGN_WIDTH = 1920.0f;
    static final float DESIGN_HEIGHT = 1080.0f;

    static final Node BACKGROUND = fromCocos(960.0f, 540.0f, 2340.0f, 1080.0f, 0.5f, 0.5f);
    static final Node PEDESTAL = fromCocos(967.175f, 464.662f, 1352.0f, 596.0f, 0.5f, 0.7f);
    static final Node LEFT_TOP = fromCocos(206.72f, 1018.47f, 400.0f, 120.0f, 0.5f, 0.5f);
    static final Node RIGHT_TOP = fromCocos(1719.44f, 1020.25f, 400.0f, 120.0f, 0.5f, 0.5f);

    static final Node BACK = centered(55.72f, 50.59f, 150.0f, 100.0f);
    static final Node BACK_ART = centered(55.72f, 50.59f, 72.0f, 88.0f);
    static final Node TITLE = centered(194.44f, 50.59f, 199.0f, 70.0f);
    static final Node HELP = centered(349.99f, 50.59f, 120.0f, 100.0f);
    static final Node HELP_ART = centered(349.99f, 50.59f, 77.0f, 77.0f);
    static final Node MY_TREASURES = fromCocos(156.992f, 112.727f, 283.0f, 100.0f, 0.5f, 0.5f);
    static final Node MY_TREASURES_ART = centered(156.992f, 967.273f, 283.0f, 94.0f);
    static final Node FORTUNE_BANNER = fromCocos(964.75f, 932.36f, 674.0f, 218.0f, 0.5f, 0.5f);
    static final Node DRAW_ONE = fromCocos(658.62f, 114.45f, 490.0f, 128.0f, 0.5f, 0.5f);
    static final Node DRAW_ONE_ART = DRAW_ONE;
    static final Node DRAW_FIVE = fromCocos(1259.28f, 114.45f, 490.0f, 128.0f, 0.5f, 0.5f);
    static final Node DRAW_FIVE_ART = centered(1259.28f, 965.55f, 502.0f, 141.0f);
    static final Node DRAW_FIVE_DISCOUNT =
            fromCocos(1417.893f, 175.199f, 154.0f, 72.0f, 0.4f, 0.2f);
    static final Node DRAW_FIVE_DISCOUNT_TEXT = centered(1433.293f, 877.441f, 66.0f, 47.0f);

    static final Node DRAW_ONE_LABEL = centered(578.554f, 958.254f, 180.0f, 57.0f);
    static final Node DRAW_ONE_DIAMOND = centered(720.85f, 973.3836f, 62.56f, 48.28f);
    static final Node DRAW_ONE_PRICE = fromCocos(752.455f, 111.7364f, 74.0f, 39.0f, 0.0f, 0.5f);
    static final Node DRAW_FIVE_LABEL = centered(1186.858f, 951.4316f, 199.0f, 57.0f);
    static final Node DRAW_FIVE_DIAMOND = centered(1329.154f, 966.5484f, 62.56f, 48.28f);
    static final Node DRAW_FIVE_PRICE = fromCocos(1360.759f, 118.5716f, 74.0f, 39.0f, 0.0f, 0.5f);

    static final Node DIAMOND_BACKGROUND = centered(1790.16f, 47.81f, 235.0f, 58.0f);
    static final Node DIAMOND_ICON = centered(1680.16f, 48.81f, 92.0f, 71.0f);
    static final Node DIAMOND_TEXT = fromCocos(1760.99f, 1033.19f, 83.0f, 46.0f, 0.0f, 0.5f);

    static final List<Node> ITEMS =
            List.of(
                    itemFromCocos(300.484f, 865.484f),
                    itemFromCocos(1431.91f, 811.96f),
                    itemFromCocos(365.44f, 264.62f),
                    itemFromCocos(1652.43f, 499.635f),
                    itemFromCocos(365.297f, 648.308f),
                    itemFromCocos(1626.06f, 864.17f),
                    itemFromCocos(294.302f, 478.066f),
                    itemFromCocos(1833.47f, 504.41f),
                    itemFromCocos(137.337f, 713.328f),
                    itemFromCocos(1581.64f, 669.874f),
                    itemFromCocos(173.105f, 319.939f),
                    itemFromCocos(1749.44f, 322.92f),
                    itemFromCocos(489.19f, 807.972f),
                    itemFromCocos(1784.61f, 716.97f),
                    itemFromCocos(86.74f, 503.11f),
                    itemFromCocos(1563.34f, 282.32f));

    private TaizhouTreasurePotLayout() {}

    static Node fromCocos(
            float x,
            float y,
            float width,
            float height,
            float anchorX,
            float anchorY) {
        float left = x - width * anchorX;
        float cocosBottom = y - height * anchorY;
        return new Node(
                left,
                DESIGN_HEIGHT - (cocosBottom + height),
                left + width,
                DESIGN_HEIGHT - cocosBottom);
    }

    static Node item(int index) {
        requireItem(index);
        return ITEMS.get(index - 1);
    }

    static Node itemFrame(int index) {
        Node item = item(index);
        return centered(item.centerX(), item.centerY(), 163.0f, 164.0f);
    }

    static Node itemIcon(int index) {
        Node item = item(index);
        return centered(item.centerX(), item.centerY() - 5.1f, 156.0f, 156.0f);
    }

    static Node itemLevel(int index) {
        Node item = item(index);
        return centered(item.centerX(), item.centerY() + 42.5f, 65.0f, 40.0f);
    }

    static int itemAt(float x, float y) {
        for (int offset = 0; offset < ITEMS.size(); offset++) {
            if (ITEMS.get(offset).contains(x, y)) return offset + 1;
        }
        return -1;
    }

    private static Node itemFromCocos(float x, float y) {
        return fromCocos(x, y, 170.0f, 170.0f, 0.5f, 0.5f);
    }

    private static Node centered(float x, float y, float width, float height) {
        return new Node(x - width * 0.5f, y - height * 0.5f, x + width * 0.5f, y + height * 0.5f);
    }

    private static void requireItem(int index) {
        if (index < 1 || index > ITEMS.size()) {
            throw new IllegalArgumentException("Treasure index must be between 1 and 16");
        }
    }

    record Node(float left, float top, float right, float bottom) {
        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }

        float centerX() {
            return (left + right) * 0.5f;
        }

        float centerY() {
            return (top + bottom) * 0.5f;
        }

        boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }
    }
}
