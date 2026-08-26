package com.nanbeiyule.game;

/** Selects a prefiltered reference-artwork level without changing the design coordinate space. */
final class GameHomeArtworkResolutionSelector {
    private static final float HALF_SCALE = 0.5f;
    private static final float HALF_SCALE_TOLERANCE = 0.025f;

    enum Resolution {
        FULL,
        HALF
    }

    private GameHomeArtworkResolutionSelector() {}

    static Resolution select(int viewportWidth, int viewportHeight) {
        AdaptiveViewport viewport =
                AdaptiveViewport.create(
                        viewportWidth,
                        viewportHeight,
                        GameHomeViewportLayout.PAGE_WIDTH,
                        GameHomeViewportLayout.PAGE_HEIGHT,
                        AdaptiveViewport.Insets.NONE);
        return Math.abs(viewport.scale() - HALF_SCALE)
                        <= HALF_SCALE_TOLERANCE
                ? Resolution.HALF
                : Resolution.FULL;
    }
}
