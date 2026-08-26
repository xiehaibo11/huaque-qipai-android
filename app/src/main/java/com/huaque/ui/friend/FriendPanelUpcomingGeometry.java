package com.huaque.ui.friend;

import java.util.Objects;

final class FriendPanelUpcomingGeometry {
    private FriendPanelUpcomingGeometry() {
    }

    static Rect filterHeader() {
        return new Rect(21, 135, 536, 75);
    }

    static Rect filterIcon() {
        return new Rect(42, 157, 42, 44);
    }

    static Rect selectedName() {
        return new Rect(96, 149, 174, 50);
    }

    static Rect filterButton() {
        return new Rect(434, 142, 126, 61);
    }

    static Rect guideBubble() {
        return new Rect(423, 22, 432, 132);
    }

    static Rect guideText() {
        return new Rect(457, 38, 364, 84);
    }

    static Rect filterList() {
        return new Rect(26, 215, 526, 150);
    }

    static Rect emptyMessage() {
        return new Rect(59, 410, 442, 141);
    }

    static Rect refreshButton() {
        return new Rect(183, 986, 201, 77);
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
