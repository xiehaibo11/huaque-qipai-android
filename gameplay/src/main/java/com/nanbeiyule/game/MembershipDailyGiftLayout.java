package com.nanbeiyule.game;

import android.graphics.RectF;

/** Shared original SxvipDailyGiftView placement offsets. */
final class MembershipDailyGiftLayout {
    static final float CONTENT_OFFSET_Y = 60.0f;

    private MembershipDailyGiftLayout() {}

    static RectF offsetY(RectF rect) {
        return new RectF(
                rect.left,
                rect.top + CONTENT_OFFSET_Y,
                rect.right,
                rect.bottom + CONTENT_OFFSET_Y);
    }

    static float offsetY(float y) {
        return y + CONTENT_OFFSET_Y;
    }
}
