package com.huaque.ui;

final class StretchedBoxModel {
    private static final float BASE_WIDTH = 1920f;
    private static final float BASE_HEIGHT = 1080f;

    private StretchedBoxModel() {}

    static int[] map(
            int screenWidth,
            int screenHeight,
            int x,
            int y,
            int width,
            int height) {
        float scaleX = screenWidth / BASE_WIDTH;
        float scaleY = screenHeight / BASE_HEIGHT;
        return new int[]{
                Math.round(x * scaleX),
                Math.round(y * scaleY),
                Math.round(width * scaleX),
                Math.round(height * scaleY)
        };
    }
}
