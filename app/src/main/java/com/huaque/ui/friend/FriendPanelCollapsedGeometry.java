package com.huaque.ui.friend;

import java.util.Objects;

final class FriendPanelCollapsedGeometry {
    static final int ROOT_WIDTH = 246;
    static final int ROOT_HEIGHT = 630;

    private static final float READY_SCALE = 0.84f;
    private static final float TITLE_SCALE = 0.97f;

    private FriendPanelCollapsedGeometry() {
    }

    static Rect backgroundBounds() {
        return new Rect(0, 0, scaled(260), scaled(750));
    }

    static Rect titleBounds() {
        float width = 231 * TITLE_SCALE;
        float height = 92 * TITLE_SCALE;
        return new Rect(
                scaled(115 - width / 2),
                scaled(750 - 683 - height / 2),
                scaled(width),
                scaled(height));
    }

    static Rect emptyLabelBounds() {
        return new Rect(scaled(33.5f), scaled(351.5f), scaled(163), scaled(47));
    }

    static Rect openArrowBounds() {
        return new Rect(Math.round(178.2f), Math.round(470.5f - 225), 68, 139);
    }

    static float emptyLabelTextSize() {
        return 40 * READY_SCALE;
    }

    private static int scaled(float value) {
        return Math.round(value * READY_SCALE);
    }

    static final class Rect {
        final int x;
        final int y;
        final int width;
        final int height;

        Rect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object value) {
            if (this == value) return true;
            if (!(value instanceof Rect)) return false;
            Rect rect = (Rect) value;
            return x == rect.x && y == rect.y
                    && width == rect.width && height == rect.height;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, width, height);
        }
    }
}
