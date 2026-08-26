package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.view.View;

/** Bottom layer for SxvipDailyGiftView: page base fills below original animation nodes. */
final class MembershipDailyGiftBackgroundView extends View {
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;

    private final MembershipDailyGiftChromeRenderer chromeRenderer;
    private final Bitmap topBackgroundBitmap;

    MembershipDailyGiftBackgroundView(Context context) {
        super(context);
        chromeRenderer = new MembershipDailyGiftChromeRenderer(loadOriginalTypeface(context));
        topBackgroundBitmap = loadBitmap(R.drawable.shop_new_bgdi);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scaleX = getWidth() / DESIGN_WIDTH;
        float scaleY = getHeight() / DESIGN_HEIGHT;
        canvas.save();
        canvas.scale(scaleX, scaleY);
        chromeRenderer.drawBaseLayer(canvas, topBackgroundBitmap);
        canvas.restore();
    }

    private Bitmap loadBitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    private static Typeface loadOriginalTypeface(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException exception) {
            return Typeface.DEFAULT_BOLD;
        }
    }
}
