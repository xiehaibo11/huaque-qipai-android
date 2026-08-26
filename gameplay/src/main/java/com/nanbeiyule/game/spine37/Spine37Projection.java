package com.nanbeiyule.game.spine37;

public final class Spine37Projection {
    private final float scaleX;
    private final float scaleY;
    private final float offsetX;
    private final float offsetY;

    private Spine37Projection(
            float scaleX,
            float scaleY,
            float offsetX,
            float offsetY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public static Spine37Projection fitCenter(
            float contentWidth,
            float contentHeight,
            int viewportWidth,
            int viewportHeight) {
        float contentScale =
                contentScale(
                        contentWidth,
                        contentHeight,
                        viewportWidth,
                        viewportHeight);
        float renderedWidth = contentWidth * contentScale;
        float renderedHeight = contentHeight * contentScale;
        return new Spine37Projection(
                contentScale * 2.0f / viewportWidth,
                contentScale * 2.0f / viewportHeight,
                -renderedWidth / viewportWidth,
                -renderedHeight / viewportHeight);
    }

    public static Spine37Projection fitCentered(
            float contentWidth,
            float contentHeight,
            int viewportWidth,
            int viewportHeight) {
        float contentScale =
                contentScale(
                        contentWidth,
                        contentHeight,
                        viewportWidth,
                        viewportHeight);
        return new Spine37Projection(
                contentScale * 2.0f / viewportWidth,
                contentScale * 2.0f / viewportHeight,
                0.0f,
                0.0f);
    }

    /**
     * Converts a centered content transform expressed in viewport pixels into OpenGL NDC.
     *
     * <p>The centered source coordinates use positive Y upward, matching the recovered Spine
     * skeleton. {@code centerYInPixels} uses Android's top-left pixel coordinate system.
     */
    public static Spine37Projection fromCenteredPixelTransform(
            int viewportWidth,
            int viewportHeight,
            float pixelScale,
            float centerXInPixels,
            float centerYInPixels) {
        return fromCenteredPixelTransform(
                viewportWidth,
                viewportHeight,
                pixelScale,
                pixelScale,
                centerXInPixels,
                centerYInPixels);
    }

    /**
     * Converts a centered content transform with independent source-axis scales into OpenGL NDC.
     *
     * <p>This overload is used when recovered Cocos coordinates first pass through a rounded
     * logical page size whose X and Y ratios differ slightly.
     */
    public static Spine37Projection fromCenteredPixelTransform(
            int viewportWidth,
            int viewportHeight,
            float pixelScaleX,
            float pixelScaleY,
            float centerXInPixels,
            float centerYInPixels) {
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            throw new IllegalArgumentException("Projection dimensions must be positive");
        }
        if (!Float.isFinite(pixelScaleX)
                || pixelScaleX <= 0.0f
                || !Float.isFinite(pixelScaleY)
                || pixelScaleY <= 0.0f
                || !Float.isFinite(centerXInPixels)
                || !Float.isFinite(centerYInPixels)) {
            throw new IllegalArgumentException(
                    "Projection transform must contain finite positive scale");
        }
        return new Spine37Projection(
                pixelScaleX * 2.0f / viewportWidth,
                pixelScaleY * 2.0f / viewportHeight,
                centerXInPixels * 2.0f / viewportWidth - 1.0f,
                1.0f - centerYInPixels * 2.0f / viewportHeight);
    }

    public float[] toNdc(float[] xyPairs) {
        if (xyPairs == null || xyPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Vertex coordinates must contain x/y pairs");
        }
        float[] result = new float[xyPairs.length];
        for (int index = 0; index < xyPairs.length; index += 2) {
            result[index] = offsetX + xyPairs[index] * scaleX;
            result[index + 1] = offsetY + xyPairs[index + 1] * scaleY;
        }
        return result;
    }

    private static float contentScale(
            float contentWidth,
            float contentHeight,
            int viewportWidth,
            int viewportHeight) {
        if (contentWidth <= 0.0f
                || contentHeight <= 0.0f
                || viewportWidth <= 0
                || viewportHeight <= 0) {
            throw new IllegalArgumentException("Projection dimensions must be positive");
        }
        return Math.min(
                viewportWidth / contentWidth,
                viewportHeight / contentHeight);
    }
}
