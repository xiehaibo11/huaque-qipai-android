package com.nanbeiyule.game.wulong;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Draws only recovered named controls from doublekou_gamelayer.png. */
final class WuLongControlRenderer {
    private final Bitmap atlas;
    private final WuLongPlistFrameResolver frames;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    WuLongControlRenderer(Bitmap atlas, WuLongPlistFrameResolver frames) { this.atlas = atlas; this.frames = frames; }
    void draw(Canvas canvas, String frameName, RectF target) {
        if (frames != null) frames.draw(canvas, atlas, frameName, target, paint);
    }
}
