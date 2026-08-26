package com.nanbeiyule.game;

import android.graphics.Rect;
import java.util.List;

/** Geometry for the Zhejiang-style bottom "更多" expanded bar. */
final class MoreMenuLayout {
    private MoreMenuLayout() {}

    static final FloatRect SOURCE = new FloatRect(8.0f, 66.0f, 2393.0f, 462.0f);
    static final FloatRect DESTINATION = new FloatRect(673.0f, 1281.0f, 2173.0f, 1530.0f);
    static final List<MoreMenuItem> ITEMS =
            List.of(
                    MoreMenuItem.HEALTH_NOTICE,
                    MoreMenuItem.RULES,
                    MoreMenuItem.SCORE_BOX,
                    MoreMenuItem.ANNOUNCEMENT,
                    MoreMenuItem.SETTINGS,
                    MoreMenuItem.ZHEJIANG_NEWS,
                    MoreMenuItem.WECHAT_PUBLIC);

    static Rect sourceRect() {
        return new Rect(
                Math.round(SOURCE.left),
                Math.round(SOURCE.top),
                Math.round(SOURCE.right),
                Math.round(SOURCE.bottom));
    }

    static FloatRect itemRect(int index) {
        float itemWidth = DESTINATION.width() / ITEMS.size();
        float left = DESTINATION.left + itemWidth * index;
        return new FloatRect(
                left,
                DESTINATION.top,
                left + itemWidth,
                DESTINATION.bottom);
    }

    static MoreMenuItem itemAt(float pageX, float pageY) {
        if (!DESTINATION.contains(pageX, pageY)) {
            return null;
        }
        float itemWidth = DESTINATION.width() / ITEMS.size();
        int index = (int) ((pageX - DESTINATION.left) / itemWidth);
        if (index < 0) {
            index = 0;
        } else if (index >= ITEMS.size()) {
            index = ITEMS.size() - 1;
        }
        return ITEMS.get(index);
    }

    static final class FloatRect {
        final float left;
        final float top;
        final float right;
        final float bottom;

        FloatRect(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        float width() {
            return right - left;
        }

        boolean contains(float x, float y) {
            return x >= left && x < right && y >= top && y < bottom;
        }
    }
}
