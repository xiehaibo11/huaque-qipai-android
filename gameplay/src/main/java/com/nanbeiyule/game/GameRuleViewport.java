package com.nanbeiyule.game;

/** Uniform 1920x1080 rule projection fitted entirely inside live system/cutout insets. */
final class GameRuleViewport {
    private final float scale;
    private final float left;
    private final float top;

    private GameRuleViewport(float scale, float left, float top) {
        this.scale = scale;
        this.left = left;
        this.top = top;
    }

    static GameRuleViewport fit(float width, float height, AdaptiveViewport.Insets insets) {
        AdaptiveViewport adaptive = AdaptiveViewport.create(width, height,
                GameRuleLayout.DESIGN_WIDTH, GameRuleLayout.DESIGN_HEIGHT, insets);
        AdaptiveViewport.Rect safe = adaptive.safeViewportRect();
        float scale = Math.min(safe.width() / GameRuleLayout.DESIGN_WIDTH,
                safe.height() / GameRuleLayout.DESIGN_HEIGHT);
        float left = safe.left() + (safe.width() - GameRuleLayout.DESIGN_WIDTH * scale) * 0.5f;
        float top = safe.top() + (safe.height() - GameRuleLayout.DESIGN_HEIGHT * scale) * 0.5f;
        return new GameRuleViewport(scale, left, top);
    }

    float scale() { return scale; }
    float left() { return left; }
    float top() { return top; }
    float right() { return mapX(GameRuleLayout.DESIGN_WIDTH); }
    float bottom() { return mapY(GameRuleLayout.DESIGN_HEIGHT); }
    float mapX(float x) { return left + x * scale; }
    float mapY(float y) { return top + y * scale; }
    float unmapX(float x) { return (x - left) / scale; }
    float unmapY(float y) { return (y - top) / scale; }
}
