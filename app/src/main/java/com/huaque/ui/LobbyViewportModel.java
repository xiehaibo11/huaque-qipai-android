package com.huaque.ui;

final class LobbyViewportModel {
    private static final float DESIGN_WIDTH = 1920f;
    private static final float DESIGN_HEIGHT = 1080f;
    private static final float PHONE_MIN_ASPECT = DESIGN_WIDTH / DESIGN_HEIGHT;

    private LobbyViewportModel() {}

    static int[] map(
            int screenWidth,
            int screenHeight,
            int x,
            int y,
            int width,
            int height) {
        Transform transform = transform(screenWidth, screenHeight);
        return new int[]{
                Math.round(transform.offsetX + x * transform.scaleX),
                Math.round(transform.offsetY + y * transform.scaleY),
                Math.round(width * transform.scaleX),
                Math.round(height * transform.scaleY)
        };
    }

    static float unmapX(int screenWidth, int screenHeight, float screenX) {
        Transform transform = transform(screenWidth, screenHeight);
        return (screenX - transform.offsetX) / transform.scaleX;
    }

    static float unmapY(int screenWidth, int screenHeight, float screenY) {
        Transform transform = transform(screenWidth, screenHeight);
        return (screenY - transform.offsetY) / transform.scaleY;
    }

    private static Transform transform(int screenWidth, int screenHeight) {
        float scaleX = screenWidth / DESIGN_WIDTH;
        float scaleY = screenHeight / DESIGN_HEIGHT;
        if ((float) screenWidth / screenHeight >= PHONE_MIN_ASPECT) {
            return new Transform(scaleX, scaleY, 0f, 0f);
        }
        float scale = Math.min(scaleX, scaleY);
        return new Transform(
                scale,
                scale,
                (screenWidth - DESIGN_WIDTH * scale) / 2f,
                (screenHeight - DESIGN_HEIGHT * scale) / 2f);
    }

    private record Transform(float scaleX, float scaleY, float offsetX, float offsetY) {}
}
