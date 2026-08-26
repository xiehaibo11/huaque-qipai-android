package com.nanbeiyule.game;

import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

/** Edge-to-edge policy shared by the activity and true full-screen dialogs. */
final class FullscreenWindowPolicy {
    private FullscreenWindowPolicy() {}

    static int cutoutModeForApi(int apiLevel) {
        if (apiLevel >= Build.VERSION_CODES.R) {
            return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        }
        if (apiLevel >= Build.VERSION_CODES.P) {
            return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        return WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
    }

    static boolean disablesDecorFitting(int apiLevel) {
        return apiLevel >= Build.VERSION_CODES.R;
    }

    static void apply(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.gravity = Gravity.TOP | Gravity.START;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attributes.layoutInDisplayCutoutMode = cutoutModeForApi(Build.VERSION.SDK_INT);
        }
        if (disablesDecorFitting(Build.VERSION.SDK_INT)) {
            window.setDecorFitsSystemWindows(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }
        window.setAttributes(attributes);
    }
}
