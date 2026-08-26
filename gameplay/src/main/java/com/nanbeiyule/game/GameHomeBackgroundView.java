package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** Static original Zhejiang lobby composition behind the real interactive data layer. */
final class GameHomeBackgroundView extends View {
    private final Bitmap finalBackgroundMaster;
    private Bitmap halfResolutionMip;
    private Bitmap background;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    GameHomeBackgroundView(Context context) {
        super(context);
        finalBackgroundMaster =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.game_home_final_background);
        background = finalBackgroundMaster;
    }

    @Override
    protected void onSizeChanged(
            int width,
            int height,
            int oldWidth,
            int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width <= 0 || height <= 0) {
            return;
        }
        GameHomeArtworkResolutionSelector.Resolution resolution =
                GameHomeArtworkResolutionSelector.select(width, height);
        if (resolution
                == GameHomeArtworkResolutionSelector.Resolution.HALF) {
            if (halfResolutionMip == null || halfResolutionMip.isRecycled()) {
                halfResolutionMip =
                        BitmapFactory.decodeResource(
                                getResources(),
                                R.drawable.game_home_final_background_1600);
            }
            background = halfResolutionMip;
        } else {
            background = finalBackgroundMaster;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0
                || getHeight() <= 0
                || background == null
                || background.isRecycled()) {
            return;
        }
        GameHomeViewportLayout layout =
                GameHomeViewportLayout.calculate(
                        getWidth(),
                        getHeight());
        AdaptiveCanvasDrawing.drawTransformedBitmap(
                canvas,
                background,
                bitmapPaint,
                layout.pageTransform(),
                getWidth(),
                getHeight(),
                GameHomeViewportLayout.PAGE_WIDTH,
                GameHomeViewportLayout.PAGE_HEIGHT);
    }
}
