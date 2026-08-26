package com.nanbeiyule.game;

final class PhoneLoginDialogLayout {
    static final int PANEL_WIDTH = 1304;
    static final int PANEL_HEIGHT = 767;
    static final double PANEL_ASPECT_RATIO = (double) PANEL_WIDTH / PANEL_HEIGHT;

    private static final double MAX_SCREEN_WIDTH_FRACTION = 0.88;
    private static final double MAX_SCREEN_HEIGHT_FRACTION = 0.86;

    private PhoneLoginDialogLayout() {}

    static Dimensions fit(int screenWidth, int screenHeight) {
        return fit(
                screenWidth,
                screenHeight,
                AdaptiveViewport.Insets.NONE);
    }

    static Dimensions fit(
            int screenWidth,
            int screenHeight,
            AdaptiveViewport.Insets insets) {
        AdaptiveViewport viewport =
                AdaptiveViewport.create(
                        screenWidth,
                        screenHeight,
                        screenWidth,
                        screenHeight,
                        insets);
        AdaptiveViewport.Transform transform =
                viewport.dialogTransform(
                        PANEL_WIDTH,
                        PANEL_HEIGHT,
                        (float) MAX_SCREEN_WIDTH_FRACTION,
                        (float) MAX_SCREEN_HEIGHT_FRACTION);
        int width =
                Math.max(
                        1,
                        (int) Math.floor(PANEL_WIDTH * transform.scaleX()));
        int height =
                Math.max(
                        1,
                        (int) Math.floor(PANEL_HEIGHT * transform.scaleY()));
        int xOffset =
                Math.round(
                        transform.mapX(PANEL_WIDTH / 2.0f)
                                - screenWidth / 2.0f);
        int yOffset =
                Math.round(
                        transform.mapY(PANEL_HEIGHT / 2.0f)
                                - screenHeight / 2.0f);
        return new Dimensions(width, height, xOffset, yOffset);
    }

    record Dimensions(int width, int height, int xOffset, int yOffset) {}
}
