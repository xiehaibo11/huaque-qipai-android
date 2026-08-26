package com.huaque.ui;

final class LoadingAnimationModel {
    static final long TICK_MILLIS = 90L;
    private static final float BASE_WIDTH = 1920f;
    private static final float BASE_HEIGHT = 1080f;
    private static final String[] CENTER_GLYPHS = {"南", "北", "娱", "乐"};

    private LoadingAnimationModel() {
    }

    static float ringRadius(float width, float height, float outerPadding) {
        return Math.max(0f, Math.min(width, height) / 2f - Math.max(0f, outerPadding));
    }

    static float centeredTextBaseline(float centerY, float ascent, float descent) {
        return centerY - (ascent + descent) / 2f;
    }

    static String centerGlyph(float progress) {
        int index = Math.min(CENTER_GLYPHS.length - 1, Math.max(0, (int) (progress / 25f)));
        return CENTER_GLYPHS[index];
    }

    static int nextProgress(int progress) {
        return Math.min(100, progress + 1);
    }

    static int requiredVirtualTextBoxHeight(
            int fontTop,
            int fontBottom,
            float shadowRadiusPixels,
            float shadowDyPixels,
            int screenWidth,
            int screenHeight,
            int minimumVirtualHeight) {
        float scale = Math.min(screenWidth / BASE_WIDTH, screenHeight / BASE_HEIGHT);
        if (scale <= 0f) {
            return minimumVirtualHeight;
        }
        float fontHeight = Math.max(0, fontBottom - fontTop);
        float shadowHeight = Math.max(0f, shadowRadiusPixels) * 2f + Math.abs(shadowDyPixels);
        return Math.max(
                minimumVirtualHeight,
                (int) Math.ceil((fontHeight + shadowHeight) / scale));
    }

    static int centeredBoxTop(int centerY, int height) {
        return centerY - height / 2;
    }
}
