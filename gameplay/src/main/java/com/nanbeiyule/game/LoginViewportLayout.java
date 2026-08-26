package com.nanbeiyule.game;

/**
 * Separates the full-screen login-flow background from the centered 16:9 content safe area.
 *
 * <p>The recovered login and loading controls keep their original page geometry, while the
 * background fills ultra-wide displays instead of exposing solid-color side gutters.
 */
final class LoginViewportLayout {
    static final float PAGE_WIDTH = 1672.0f;
    static final float PAGE_HEIGHT = 941.0f;
    static final float PAGE_ASPECT_RATIO = PAGE_WIDTH / PAGE_HEIGHT;

    private final AdaptiveViewport viewport;

    private LoginViewportLayout(AdaptiveViewport viewport) {
        this.viewport = viewport;
    }

    static LoginViewportLayout calculate(float viewportWidth, float viewportHeight) {
        return calculate(viewportWidth, viewportHeight, AdaptiveViewport.Insets.NONE);
    }

    static LoginViewportLayout calculate(
            float viewportWidth,
            float viewportHeight,
            AdaptiveViewport.Insets insets) {
        return new LoginViewportLayout(
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

    float backgroundLeft() {
        return 0.0f;
    }

    float backgroundTop() {
        return 0.0f;
    }

    float backgroundRight() {
        return viewport.viewportWidth();
    }

    float backgroundBottom() {
        return viewport.viewportHeight();
    }

    float pageScale() {
        return viewport.scale();
    }

    float pageLeft() {
        return viewport.designLeft();
    }

    float pageTop() {
        return viewport.designTop();
    }

    float pageRight() {
        return pageLeft() + pageWidth();
    }

    float pageBottom() {
        return pageTop() + pageHeight();
    }

    float pageWidth() {
        return PAGE_WIDTH * pageScale();
    }

    float pageHeight() {
        return PAGE_HEIGHT * pageScale();
    }

    float toPageX(float viewportX) {
        return viewport.designTransform().unmapX(viewportX);
    }

    float toPageY(float viewportY) {
        return viewport.designTransform().unmapY(viewportY);
    }
}
