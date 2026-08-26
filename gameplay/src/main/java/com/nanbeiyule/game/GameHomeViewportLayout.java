package com.nanbeiyule.game;

/**
 * Separates the full-screen lobby background from the centered interactive safe area.
 *
 * <p>The background uses a center-crop transform so ultra-wide displays never expose side
 * gutters. Lobby controls retain their recovered page geometry and share the same transform for
 * drawing and hit testing.
 */
final class GameHomeViewportLayout {
    static final float PAGE_WIDTH = 3200.0f;
    static final float PAGE_HEIGHT = 1792.0f;

    private final AdaptiveViewport viewport;
    private final AdaptiveViewport.Transform pageTransform;

    private GameHomeViewportLayout(AdaptiveViewport viewport) {
        this.viewport = viewport;
        pageTransform =
                new AdaptiveViewport.Transform(
                        viewport.viewportWidth() / PAGE_WIDTH,
                        viewport.viewportHeight() / PAGE_HEIGHT,
                        0.0f,
                        0.0f);
    }

    static GameHomeViewportLayout calculate(float viewportWidth, float viewportHeight) {
        return calculate(viewportWidth, viewportHeight, AdaptiveViewport.Insets.NONE);
    }

    static GameHomeViewportLayout calculate(
            float viewportWidth,
            float viewportHeight,
            AdaptiveViewport.Insets insets) {
        return new GameHomeViewportLayout(
                AdaptiveViewport.create(
                        viewportWidth,
                        viewportHeight,
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        insets));
    }

    AdaptiveViewport adaptiveViewport() {
        return viewport;
    }

    AdaptiveViewport.Transform pageTransform() {
        return pageTransform;
    }

    float backgroundScale() {
        return pageTransform.scaleX();
    }

    float backgroundLeft() {
        return pageTransform.offsetX();
    }

    float backgroundTop() {
        return pageTransform.offsetY();
    }

    float backgroundRight() {
        return pageTransform.mapX(PAGE_WIDTH);
    }

    float backgroundBottom() {
        return pageTransform.mapY(PAGE_HEIGHT);
    }

    float pageScale() {
        return pageTransform.scaleY();
    }

    float pageLeft() {
        return pageTransform.offsetX();
    }

    float pageTop() {
        return pageTransform.offsetY();
    }

    float pageRight() {
        return pageTransform.mapX(PAGE_WIDTH);
    }

    float pageBottom() {
        return pageTransform.mapY(PAGE_HEIGHT);
    }

    float toPageX(float viewportX) {
        return pageTransform.unmapX(viewportX);
    }

    float toPageY(float viewportY) {
        return pageTransform.unmapY(viewportY);
    }
}
