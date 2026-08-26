package com.nanbeiyule.game;

/** Shared value types and validation helpers for adaptive viewport calculations. */
abstract class AdaptiveViewportTypes {
    enum ScalePolicy {
        FIXED_WIDTH,
        FIXED_HEIGHT
    }

    enum Layer {
        FULL_BLEED,
        DESIGN_CENTER,
        SAFE_EDGE,
        DIALOG
    }

    record Insets(float left, float top, float right, float bottom) {
        static final Insets NONE = new Insets(0.0f, 0.0f, 0.0f, 0.0f);

        Insets {
            requireFiniteNonNegative(left, "left");
            requireFiniteNonNegative(top, "top");
            requireFiniteNonNegative(right, "right");
            requireFiniteNonNegative(bottom, "bottom");
        }

        private static void requireFiniteNonNegative(float value, String name) {
            if (!Float.isFinite(value) || value < 0.0f) {
                throw new IllegalArgumentException(
                        "Inset " + name + " must be finite and non-negative");
            }
        }
    }

    record Rect(float left, float top, float right, float bottom) {
        Rect {
            requireFinite(left, "left");
            requireFinite(top, "top");
            requireFinite(right, "right");
            requireFinite(bottom, "bottom");
            if (right < left || bottom < top) {
                throw new IllegalArgumentException("Rect edges are inverted");
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
    }

    record Transform(float scaleX, float scaleY, float offsetX, float offsetY) {
        Transform {
            requireFinite(scaleX, "scaleX");
            requireFinite(scaleY, "scaleY");
            requireFinite(offsetX, "offsetX");
            requireFinite(offsetY, "offsetY");
            if (scaleX == 0.0f || scaleY == 0.0f) {
                throw new IllegalArgumentException("Transform scales must be non-zero");
            }
        }

        static Transform uniform(float scale, float offsetX, float offsetY) {
            return new Transform(scale, scale, offsetX, offsetY);
        }

        static Transform identity() {
            return uniform(1.0f, 0.0f, 0.0f);
        }

        float mapX(float sourceX) {
            return offsetX + sourceX * scaleX;
        }

        float mapY(float sourceY) {
            return offsetY + sourceY * scaleY;
        }

        float unmapX(float targetX) {
            return (targetX - offsetX) / scaleX;
        }

        float unmapY(float targetY) {
            return (targetY - offsetY) / scaleY;
        }

        Rect map(Rect source) {
            float firstX = mapX(source.left());
            float secondX = mapX(source.right());
            float firstY = mapY(source.top());
            float secondY = mapY(source.bottom());
            return new Rect(
                    Math.min(firstX, secondX),
                    Math.min(firstY, secondY),
                    Math.max(firstX, secondX),
                    Math.max(firstY, secondY));
        }

        Rect unmap(Rect target) {
            float firstX = unmapX(target.left());
            float secondX = unmapX(target.right());
            float firstY = unmapY(target.top());
            float secondY = unmapY(target.bottom());
            return new Rect(
                    Math.min(firstX, secondX),
                    Math.min(firstY, secondY),
                    Math.max(firstX, secondX),
                    Math.max(firstY, secondY));
        }
    }
    protected static float interpolate(float fraction, float start, float end) {
        return start + fraction * (end - start);
    }

    protected static float[] fitInsetsInside(float start, float end, float available) {
        float total = start + end;
        if (total < available) {
            return new float[] {start, end};
        }
        float maximumTotal = Math.max(0.0f, available - 1.0f);
        if (total == 0.0f) {
            return new float[] {0.0f, 0.0f};
        }
        float factor = maximumTotal / total;
        return new float[] {start * factor, end * factor};
    }

    protected static void requirePositive(float value, String name) {
        requireFinite(value, name);
        if (value <= 0.0f) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    protected static void requireFraction(float value, String name) {
        requireFinite(value, name);
        if (value <= 0.0f || value > 1.0f) {
            throw new IllegalArgumentException(name + " must be in (0, 1]");
        }
    }

    protected static void requireFinite(float value, String name) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
