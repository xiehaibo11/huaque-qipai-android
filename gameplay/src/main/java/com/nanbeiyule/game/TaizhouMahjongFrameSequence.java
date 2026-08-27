package com.nanbeiyule.game;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

final class TaizhouMahjongFrameSequence {
    private final Bitmap[] frames;
    private final long frameMillis;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private TaizhouMahjongFrameSequence(Bitmap[] frames, long frameMillis) {
        this.frames = frames;
        this.frameMillis = frameMillis;
    }

    static TaizhouMahjongFrameSequence load(
            AssetManager assets, String directory, String prefix, long frameMillis) {
        List<Bitmap> loaded = new ArrayList<>();
        for (int index = 0; ; index++) {
            String path = directory + "/" + prefix + "_" + String.format("%05d", index) + ".png";
            try (InputStream stream = assets.open(path)) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                if (bitmap != null) {
                    loaded.add(bitmap);
                }
            } catch (IOException exception) {
                break;
            }
        }
        if (loaded.isEmpty()) {
            throw new IllegalStateException("missing frame sequence " + directory + "/" + prefix);
        }
        return new TaizhouMahjongFrameSequence(loaded.toArray(new Bitmap[0]), frameMillis);
    }

    long durationMillis() {
        return frames.length * frameMillis;
    }

    void draw(Canvas canvas, long elapsedMillis, float centerX, float centerY, float scale) {
        int index = (int) Math.min(frames.length - 1, Math.max(0L, elapsedMillis) / frameMillis);
        Bitmap frame = frames[index];
        if (frame == null || frame.isRecycled()) {
            return;
        }
        float width = frame.getWidth() * scale;
        float height = frame.getHeight() * scale;
        canvas.drawBitmap(
                frame,
                null,
                new RectF(
                        centerX - width / 2.0f,
                        centerY - height / 2.0f,
                        centerX + width / 2.0f,
                        centerY + height / 2.0f),
                paint);
    }

    void recycle() {
        for (Bitmap frame : frames) {
            if (frame != null && !frame.isRecycled()) {
                frame.recycle();
            }
        }
    }
}
