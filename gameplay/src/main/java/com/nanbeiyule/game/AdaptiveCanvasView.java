package com.nanbeiyule.game;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Insets;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;

/**
 * Base class for interactive Canvas pages that need the same dynamic window-safe-area source.
 *
 * <p>Background-only views intentionally do not use this class because full-bleed artwork is
 * allowed underneath system UI and display cutouts.
 */
abstract class AdaptiveCanvasView extends View {
    private AdaptiveViewport.Insets adaptiveSafeInsets = AdaptiveViewport.Insets.NONE;

    protected AdaptiveCanvasView(Context context) {
        super(context);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        AdaptiveViewport.Insets updated = safeInsetsFrom(windowInsets);
        if (!updated.equals(adaptiveSafeInsets)) {
            adaptiveSafeInsets = updated;
            invalidate();
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
    }

    protected final AdaptiveViewport adaptiveViewport(float designWidth, float designHeight) {
        return AdaptiveViewport.create(
                getWidth(),
                getHeight(),
                designWidth,
                designHeight,
                adaptiveSafeInsets);
    }

    protected final AdaptiveViewport.Insets adaptiveSafeInsets() {
        return adaptiveSafeInsets;
    }

    static AdaptiveViewport.Insets safeInsetsFrom(WindowInsets windowInsets) {
        if (windowInsets == null) {
            return AdaptiveViewport.Insets.NONE;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Api30.safeInsets(windowInsets);
        }

        int left = Math.max(windowInsets.getStableInsetLeft(), windowInsets.getSystemWindowInsetLeft());
        int top = Math.max(windowInsets.getStableInsetTop(), windowInsets.getSystemWindowInsetTop());
        int right =
                Math.max(windowInsets.getStableInsetRight(), windowInsets.getSystemWindowInsetRight());
        int bottom =
                Math.max(
                        windowInsets.getStableInsetBottom(),
                        windowInsets.getSystemWindowInsetBottom());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Insets systemGestures = windowInsets.getSystemGestureInsets();
            Insets mandatoryGestures = windowInsets.getMandatorySystemGestureInsets();
            Insets tappable = windowInsets.getTappableElementInsets();
            left = maximum(left, systemGestures.left, mandatoryGestures.left, tappable.left);
            top = maximum(top, systemGestures.top, mandatoryGestures.top, tappable.top);
            right = maximum(right, systemGestures.right, mandatoryGestures.right, tappable.right);
            bottom =
                    maximum(
                            bottom,
                            systemGestures.bottom,
                            mandatoryGestures.bottom,
                            tappable.bottom);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout displayCutout = windowInsets.getDisplayCutout();
            if (displayCutout != null) {
                left = Math.max(left, displayCutout.getSafeInsetLeft());
                top = Math.max(top, displayCutout.getSafeInsetTop());
                right = Math.max(right, displayCutout.getSafeInsetRight());
                bottom = Math.max(bottom, displayCutout.getSafeInsetBottom());
            }
        }
        return new AdaptiveViewport.Insets(left, top, right, bottom);
    }

    private static int maximum(int first, int second, int third, int fourth) {
        return Math.max(Math.max(first, second), Math.max(third, fourth));
    }

    @TargetApi(Build.VERSION_CODES.R)
    private static final class Api30 {
        private Api30() {}

        static AdaptiveViewport.Insets safeInsets(WindowInsets windowInsets) {
            int types =
                    WindowInsets.Type.systemBars()
                            | WindowInsets.Type.displayCutout()
                            | WindowInsets.Type.systemGestures()
                            | WindowInsets.Type.mandatorySystemGestures()
                            | WindowInsets.Type.tappableElement();
            Insets insets = windowInsets.getInsetsIgnoringVisibility(types);
            return new AdaptiveViewport.Insets(
                    insets.left, insets.top, insets.right, insets.bottom);
        }
    }
}
