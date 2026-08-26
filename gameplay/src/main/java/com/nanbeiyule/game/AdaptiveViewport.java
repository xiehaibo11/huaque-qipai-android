package com.nanbeiyule.game;

/**
 * Immutable viewport model shared by Canvas drawing, OpenGL projection, dialog fitting, and touch
 * hit testing.
 *
 * <p>The scale policy mirrors the recovered Cocos configuration: windows wider than the design
 * aspect ratio use {@link ScalePolicy#FIXED_HEIGHT}; all other windows use {@link
 * ScalePolicy#FIXED_WIDTH}. The resulting transform is always uniform.
 */
final class AdaptiveViewport extends AdaptiveViewportTypes {
    static final float ULTRA_WIDE_ASPECT_RATIO = 1.8f;
    static final float ORIGINAL_SAFE_MARGIN_DESIGN_UNITS = 80.0f;


    private final float viewportWidth;
    private final float viewportHeight;
    private final float designWidth;
    private final float designHeight;
    private final ScalePolicy scalePolicy;
    private final float scale;
    private final float designLeft;
    private final float designTop;
    private final Transform designTransform;
    private final Rect visibleDesignRect;
    private final Rect safeViewportRect;
    private final Rect safeDesignRect;

    private AdaptiveViewport(
            float viewportWidth,
            float viewportHeight,
            float designWidth,
            float designHeight,
            ScalePolicy scalePolicy,
            float scale,
            float designLeft,
            float designTop,
            Rect safeViewportRect) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.designWidth = designWidth;
        this.designHeight = designHeight;
        this.scalePolicy = scalePolicy;
        this.scale = scale;
        this.designLeft = designLeft;
        this.designTop = designTop;
        designTransform = Transform.uniform(scale, designLeft, designTop);
        visibleDesignRect =
                designTransform.unmap(new Rect(0.0f, 0.0f, viewportWidth, viewportHeight));
        this.safeViewportRect = safeViewportRect;
        safeDesignRect = designTransform.unmap(safeViewportRect);
    }

    static AdaptiveViewport create(
            float viewportWidth,
            float viewportHeight,
            float designWidth,
            float designHeight,
            Insets insets) {
        requirePositive(viewportWidth, "viewportWidth");
        requirePositive(viewportHeight, "viewportHeight");
        requirePositive(designWidth, "designWidth");
        requirePositive(designHeight, "designHeight");
        if (insets == null) {
            throw new IllegalArgumentException("Insets must not be null");
        }

        float viewportAspect = viewportWidth / viewportHeight;
        float designAspect = designWidth / designHeight;
        ScalePolicy policy =
                viewportAspect > designAspect
                        ? ScalePolicy.FIXED_HEIGHT
                        : ScalePolicy.FIXED_WIDTH;
        float scale =
                policy == ScalePolicy.FIXED_HEIGHT
                        ? viewportHeight / designHeight
                        : viewportWidth / designWidth;
        float designLeft = (viewportWidth - designWidth * scale) / 2.0f;
        float designTop = (viewportHeight - designHeight * scale) / 2.0f;

        float fallbackPixels =
                viewportAspect > ULTRA_WIDE_ASPECT_RATIO
                        ? ORIGINAL_SAFE_MARGIN_DESIGN_UNITS * scale
                        : 0.0f;
        float effectiveLeft = Math.max(insets.left(), fallbackPixels);
        float effectiveRight = Math.max(insets.right(), fallbackPixels);
        float effectiveTop = insets.top();
        float effectiveBottom = insets.bottom();
        float[] horizontal =
                fitInsetsInside(effectiveLeft, effectiveRight, viewportWidth);
        float[] vertical =
                fitInsetsInside(effectiveTop, effectiveBottom, viewportHeight);
        Rect safeViewport =
                new Rect(
                        horizontal[0],
                        vertical[0],
                        viewportWidth - horizontal[1],
                        viewportHeight - vertical[1]);

        return new AdaptiveViewport(
                viewportWidth,
                viewportHeight,
                designWidth,
                designHeight,
                policy,
                scale,
                designLeft,
                designTop,
                safeViewport);
    }

    float viewportWidth() {
        return viewportWidth;
    }

    float viewportHeight() {
        return viewportHeight;
    }

    float designWidth() {
        return designWidth;
    }

    float designHeight() {
        return designHeight;
    }

    ScalePolicy scalePolicy() {
        return scalePolicy;
    }

    float scale() {
        return scale;
    }

    float logicalWidth() {
        return viewportWidth / scale;
    }

    float logicalHeight() {
        return viewportHeight / scale;
    }

    float designLeft() {
        return designLeft;
    }

    float designTop() {
        return designTop;
    }

    Transform designTransform() {
        return designTransform;
    }

    Transform layerTransform(Layer layer) {
        if (layer == null) {
            throw new IllegalArgumentException("Layer must not be null");
        }
        return switch (layer) {
            case FULL_BLEED -> Transform.identity();
            case DESIGN_CENTER, SAFE_EDGE, DIALOG -> designTransform;
        };
    }

    Rect visibleDesignRect() {
        return visibleDesignRect;
    }

    Rect safeViewportRect() {
        return safeViewportRect;
    }

    Rect safeDesignRect() {
        return safeDesignRect;
    }

    float safeEdgeX(float designX) {
        return interpolate(designX / designWidth, safeDesignRect.left(), safeDesignRect.right());
    }

    float safeEdgeY(float designY) {
        return interpolate(designY / designHeight, safeDesignRect.top(), safeDesignRect.bottom());
    }

    float safeEdgeOffsetX(float designX) {
        return safeEdgeX(designX) - designX;
    }

    float safeEdgeOffsetY(float designY) {
        return safeEdgeY(designY) - designY;
    }

    Transform fullBleedCover(float contentWidth, float contentHeight) {
        requirePositive(contentWidth, "contentWidth");
        requirePositive(contentHeight, "contentHeight");
        float coverScale =
                Math.max(viewportWidth / contentWidth, viewportHeight / contentHeight);
        return Transform.uniform(
                coverScale,
                (viewportWidth - contentWidth * coverScale) / 2.0f,
                (viewportHeight - contentHeight * coverScale) / 2.0f);
    }

    Transform dialogTransform(
            float contentWidth,
            float contentHeight,
            float maximumWidthFraction,
            float maximumHeightFraction) {
        requirePositive(contentWidth, "contentWidth");
        requirePositive(contentHeight, "contentHeight");
        requireFraction(maximumWidthFraction, "maximumWidthFraction");
        requireFraction(maximumHeightFraction, "maximumHeightFraction");

        float dialogScale =
                Math.min(
                        safeViewportRect.width() * maximumWidthFraction / contentWidth,
                        safeViewportRect.height() * maximumHeightFraction / contentHeight);
        float renderedWidth = contentWidth * dialogScale;
        float renderedHeight = contentHeight * dialogScale;
        return Transform.uniform(
                dialogScale,
                safeViewportRect.centerX() - renderedWidth / 2.0f,
                safeViewportRect.centerY() - renderedHeight / 2.0f);
    }

}
