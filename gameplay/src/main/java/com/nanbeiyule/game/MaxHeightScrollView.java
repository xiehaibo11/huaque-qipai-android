package com.nanbeiyule.game;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * A ScrollView that clamps its measured height to an explicit pixel maximum
 * regardless of the height spec it receives. Dialog windows sized with
 * WRAP_CONTENT measure their content with an UNSPECIFIED height, so a plain
 * ScrollView never scrolls in that context; this forces the cap so tall
 * dialog content scrolls instead of overflowing off-screen.
 */
final class MaxHeightScrollView extends ScrollView {
    private int maxHeight = -1;

    public MaxHeightScrollView(Context context) {
        super(context);
    }

    public MaxHeightScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHeightScrollView(
            Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight >= 0
                && MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
