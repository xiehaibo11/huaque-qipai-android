package com.nanbeiyule.game;

import android.content.Context;

/** Warms the immutable shop drawables while the lobby is already visible. */
public final class ShopAssetPreloader {
    private ShopAssetPreloader() {}

    public static void preload(Context context) {
        if (context == null) {
            return;
        }
        ShopDrawableSet.preload(
                context.getApplicationContext().getResources());
    }
}
