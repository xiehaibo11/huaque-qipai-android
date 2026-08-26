package com.nanbeiyule.game;

import java.util.List;

/** Shared personal-center geometry types and collection builders. */
abstract class PersonalCenterLayoutGeometry {
    record Box(float left, float top, float right, float bottom) {
        Box {
            if (right <= left || bottom <= top) {
                throw new IllegalArgumentException(
                        "Box must have positive size");
            }
        }

        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }

        float centerX() {
            return (left + right) / 2.0f;
        }

        float centerY() {
            return (top + bottom) / 2.0f;
        }

        boolean contains(float x, float y) {
            return x >= left
                    && x <= right
                    && y >= top
                    && y <= bottom;
        }

        boolean contains(Box other) {
            return other.left >= left
                    && other.top >= top
                    && other.right <= right
                    && other.bottom <= bottom;
        }
    }

    record Viewport(
            float scaleX,
            float scaleY,
            float offsetX,
            float offsetY,
            Box visiblePanel) {}

    protected static List<Box> equalColumns(
            float left,
            float top,
            float right,
            float bottom,
            int count) {
        float width = (right - left) / count;
        java.util.ArrayList<Box> boxes =
                new java.util.ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            boxes.add(
                    new Box(
                            left + width * index,
                            top,
                            left + width * (index + 1),
                            bottom));
        }
        return List.copyOf(boxes);
    }

    protected static List<Box> verticalRows(
            float left,
            float top,
            float right,
            float height,
            float spacing,
            int count) {
        java.util.ArrayList<Box> boxes =
                new java.util.ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            float rowTop = top + spacing * index;
            boxes.add(new Box(left, rowTop, right, rowTop + height));
        }
        return List.copyOf(boxes);
    }
}
