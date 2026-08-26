package com.nanbeiyule.game;

final class FirstLaunchAgreementLayout {
    static final double LANDSCAPE_WIDTH_RATIO = 0.66d;
    static final double LANDSCAPE_HEIGHT_RATIO = 0.87d;

    static final float PAGE_WIDTH = 1056.0f;
    static final float PAGE_HEIGHT = 783.0f;
    static final float TITLE_BAND_BOTTOM = 128.0f;

    static final Box TITLE =
            new Box(0.0f, 0.0f, 1056.0f, 128.0f);
    static final Box READING_PANEL =
            new Box(48.0f, 153.0f, 1008.0f, 533.0f);
    static final Box SERVICE_LINK =
            new Box(386.0f, 537.0f, 581.0f, 635.0f);
    static final Box PRIVACY_LINK =
            new Box(631.0f, 537.0f, 826.0f, 635.0f);
    static final Box REJECT_BUTTON =
            new Box(276.0f, 658.0f, 478.0f, 770.0f);
    static final Box ACCEPT_BUTTON =
            new Box(578.0f, 658.0f, 780.0f, 770.0f);

    private FirstLaunchAgreementLayout() {}

    static Size windowSize(int displayWidth, int displayHeight) {
        int longSide = Math.max(displayWidth, displayHeight);
        int shortSide = Math.min(displayWidth, displayHeight);
        return new Size(
                (int) (longSide * LANDSCAPE_WIDTH_RATIO),
                (int) (shortSide * LANDSCAPE_HEIGHT_RATIO));
    }

    static float originalContentDensity(
            int displayWidth,
            int displayHeight) {
        int shortSide = Math.min(displayWidth, displayHeight);
        if (shortSide <= 0) {
            throw new IllegalArgumentException(
                    "Display size must be positive");
        }
        return shortSide / 360.0f;
    }

    static AdaptiveViewport viewport(
            float width,
            float height,
            AdaptiveViewport.Insets insets) {
        return AdaptiveViewport.create(
                width,
                height,
                PAGE_WIDTH,
                PAGE_HEIGHT,
                insets);
    }

    record Size(int width, int height) {
        Size {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException(
                        "Dialog size must be positive");
            }
        }
    }

    record Box(
            float left,
            float top,
            float right,
            float bottom) {
        Box {
            if (right < left || bottom < top) {
                throw new IllegalArgumentException(
                        "Box edges are inverted");
            }
        }

        boolean contains(float x, float y) {
            return x >= left
                    && x <= right
                    && y >= top
                    && y <= bottom;
        }

        boolean intersects(Box other) {
            return left < other.right
                    && right > other.left
                    && top < other.bottom
                    && bottom > other.top;
        }

        float centerX() {
            return (left + right) / 2.0f;
        }

        float centerY() {
            return (top + bottom) / 2.0f;
        }
    }
}
