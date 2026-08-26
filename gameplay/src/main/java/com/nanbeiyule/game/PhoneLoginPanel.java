package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

final class PhoneLoginPanel extends ViewGroup {
    PhoneLoginPanel(Activity activity) {
        super(activity);
        setBackgroundResource(R.drawable.phone_login_dialog_panel);
        setClipChildren(false);
        setClipToPadding(false);
    }

    void addAt(
            View child,
            int left,
            int top,
            int width,
            int height,
            float baseTextSizePixels) {
        addView(
                child,
                new PanelLayoutParams(
                        left,
                        top,
                        width,
                        height,
                        baseTextSizePixels));
    }

    void addAt(View child, int left, int top, int width, int height) {
        addAt(child, left, top, width, height, 0.0f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth =
                measuredAxis(
                        widthMeasureSpec,
                        PhoneLoginDialogLayout.PANEL_WIDTH);
        int measuredHeight =
                measuredAxis(
                        heightMeasureSpec,
                        PhoneLoginDialogLayout.PANEL_HEIGHT);
        setMeasuredDimension(measuredWidth, measuredHeight);

        float scaleX =
                (float) measuredWidth / PhoneLoginDialogLayout.PANEL_WIDTH;
        float scaleY =
                (float) measuredHeight / PhoneLoginDialogLayout.PANEL_HEIGHT;
        float textScale = Math.min(scaleX, scaleY);
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            PanelLayoutParams params =
                    (PanelLayoutParams) child.getLayoutParams();
            int childWidth = Math.max(1, Math.round(params.baseWidth * scaleX));
            int childHeight = Math.max(1, Math.round(params.baseHeight * scaleY));
            child.measure(
                    MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
            if (child instanceof TextView textView
                    && params.baseTextSizePixels > 0.0f) {
                textView.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        params.baseTextSizePixels * textScale);
            }
            if (child instanceof EditText) {
                int horizontalPadding = Math.round(18.0f * textScale);
                child.setPadding(horizontalPadding, 0, horizontalPadding, 0);
            }
        }
    }

    @Override
    protected void onLayout(
            boolean changed,
            int left,
            int top,
            int right,
            int bottom) {
        float scaleX =
                (float) (right - left)
                        / PhoneLoginDialogLayout.PANEL_WIDTH;
        float scaleY =
                (float) (bottom - top)
                        / PhoneLoginDialogLayout.PANEL_HEIGHT;
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            PanelLayoutParams params =
                    (PanelLayoutParams) child.getLayoutParams();
            int childLeft = Math.round(params.baseLeft * scaleX);
            int childTop = Math.round(params.baseTop * scaleY);
            child.layout(
                    childLeft,
                    childTop,
                    childLeft + child.getMeasuredWidth(),
                    childTop + child.getMeasuredHeight());
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new PanelLayoutParams(0, 0, 1, 1, 0.0f);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams source) {
        return new PanelLayoutParams(source);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams params) {
        return params instanceof PanelLayoutParams;
    }

    private static int measuredAxis(int measureSpec, int fallback) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY || mode == MeasureSpec.AT_MOST) {
            return Math.max(1, size);
        }
        return fallback;
    }

    private static final class PanelLayoutParams extends ViewGroup.LayoutParams {
        private final int baseLeft;
        private final int baseTop;
        private final int baseWidth;
        private final int baseHeight;
        private final float baseTextSizePixels;

        private PanelLayoutParams(
                int baseLeft,
                int baseTop,
                int baseWidth,
                int baseHeight,
                float baseTextSizePixels) {
            super(baseWidth, baseHeight);
            this.baseLeft = baseLeft;
            this.baseTop = baseTop;
            this.baseWidth = baseWidth;
            this.baseHeight = baseHeight;
            this.baseTextSizePixels = baseTextSizePixels;
        }

        private PanelLayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            baseLeft = 0;
            baseTop = 0;
            baseWidth = Math.max(1, source.width);
            baseHeight = Math.max(1, source.height);
            baseTextSizePixels = 0.0f;
        }
    }
}
