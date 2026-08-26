package com.huaque.ui;

final class FullscreenWindowPolicy {
    static final int CUTOUT_DEFAULT = 0;
    static final int CUTOUT_SHORT_EDGES = 1;
    static final int CUTOUT_ALWAYS = 3;

    private FullscreenWindowPolicy() {}

    static int cutoutModeForApi(int apiLevel) {
        if (apiLevel >= 30) {
            return CUTOUT_ALWAYS;
        }
        if (apiLevel >= 28) {
            return CUTOUT_SHORT_EDGES;
        }
        return CUTOUT_DEFAULT;
    }

    static boolean disablesDecorFitting(int apiLevel) {
        return apiLevel >= 30;
    }
}
