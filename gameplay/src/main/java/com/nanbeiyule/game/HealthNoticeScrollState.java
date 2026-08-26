package com.nanbeiyule.game;

/** Vertical drag state for the long health notice. */
final class HealthNoticeScrollState {
    private float contentHeight;
    private final float viewportHeight;
    private float offset;

    HealthNoticeScrollState(float contentHeight, float viewportHeight) {
        if (!Float.isFinite(contentHeight) || contentHeight < 0f) {
            throw new IllegalArgumentException("contentHeight must be finite and non-negative");
        }
        if (!Float.isFinite(viewportHeight) || viewportHeight <= 0f) {
            throw new IllegalArgumentException("viewportHeight must be finite and positive");
        }
        this.contentHeight = contentHeight;
        this.viewportHeight = viewportHeight;
    }

    float offset() {
        return offset;
    }

    void setContentHeight(float contentHeight) {
        if (!Float.isFinite(contentHeight) || contentHeight < 0f) {
            throw new IllegalArgumentException("contentHeight must be finite and non-negative");
        }
        this.contentHeight = contentHeight;
        offset = clamp(offset);
    }

    void moveByFingerDelta(float deltaY) {
        if (!Float.isFinite(deltaY)) {
            return;
        }
        offset = clamp(offset - deltaY);
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(Math.max(0f, contentHeight - viewportHeight), value));
    }
}
