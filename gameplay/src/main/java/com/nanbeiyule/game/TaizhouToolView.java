package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

/** Shared coordinate and drawing helpers for original 1920x1080 tool views. */
abstract class TaizhouToolView extends AdaptiveCanvasView {
    protected final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    protected final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** 原版局内 CSB 的字体资源：Common/Font/fangzhengcuyuan.TTF。 */
    private static final String ORIGINAL_FONT_ASSET = "fonts/fangzhengcuyuan.ttf";

    TaizhouToolView(Context context) {
        super(context);
        setClickable(true);
        textPaint.setTypeface(originalTypeface(context));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private static Typeface originalTypeface(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), ORIGINAL_FONT_ASSET);
        } catch (RuntimeException ignored) {
            return Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        }
    }

    @Override
    protected final void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        AdaptiveViewport viewport =
                adaptiveViewport(
                        TaizhouWaitingToolLayout.DESIGN_WIDTH,
                        TaizhouWaitingToolLayout.DESIGN_HEIGHT);
        int save = AdaptiveCanvasDrawing.apply(canvas, viewport.designTransform());
        drawDesign(canvas);
        canvas.restoreToCount(save);
    }

    protected abstract void drawDesign(Canvas canvas);

    /** 本 View 正在使用的可视区（1920×1080 设计分辨率）。 */
    protected final AdaptiveViewport designViewport() {
        return adaptiveViewport(
                TaizhouWaitingToolLayout.DESIGN_WIDTH, TaizhouWaitingToolLayout.DESIGN_HEIGHT);
    }

    protected final float designX(MotionEvent event) {
        return adaptiveViewport(
                        TaizhouWaitingToolLayout.DESIGN_WIDTH,
                        TaizhouWaitingToolLayout.DESIGN_HEIGHT)
                .designTransform()
                .unmapX(event.getX());
    }

    protected final float designY(MotionEvent event) {
        return adaptiveViewport(
                        TaizhouWaitingToolLayout.DESIGN_WIDTH,
                        TaizhouWaitingToolLayout.DESIGN_HEIGHT)
                .designTransform()
                .unmapY(event.getY());
    }

    protected final Bitmap bitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    protected final void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
        }
    }

    protected final void drawCentered(
            Canvas canvas, Bitmap bitmap, float centerX, float centerY, float width, float height) {
        drawBitmap(
                canvas,
                bitmap,
                new RectF(
                        centerX - width / 2.0f,
                        centerY - height / 2.0f,
                        centerX + width / 2.0f,
                        centerY + height / 2.0f));
    }

    protected final void drawText(
            Canvas canvas,
            String text,
            float centerX,
            float baselineY,
            float size,
            int color) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setShadowLayer(2.5f, 0.0f, 2.0f, Color.argb(150, 72, 34, 7));
        canvas.drawText(text == null ? "" : text, centerX, baselineY, textPaint);
        textPaint.clearShadowLayer();
    }

    /**
     * Cocos Label 的绘制：文字在给定盒内垂直居中，原版没有描边与投影。
     *
     * @param align 盒内水平对齐，对应 CSB 的 AnchorPoint X。
     */
    protected final void drawLabel(
            Canvas canvas,
            String text,
            RectF box,
            float size,
            int color,
            Paint.Align align) {
        if (text == null || text.isEmpty()) {
            return;
        }
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(align);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = box.centerY() - (metrics.ascent + metrics.descent) / 2.0f;
        float x = switch (align) {
            case LEFT -> box.left;
            case RIGHT -> box.right;
            default -> box.centerX();
        };
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /** 测量文本宽度，用于原版按内容排布的列表（如高级设置的语音按钮）。 */
    protected final float measureText(String text, float size) {
        textPaint.setTextSize(size);
        return textPaint.measureText(text == null ? "" : text);
    }

    /**
     * Cocos scale9 拉伸。capInsets 以图片左上角为原点，与 CSB 的
     * {@code Scale9OriginX/Y + Scale9Width/Height} 逐值对应。
     */
    protected final void drawScale9(
            Canvas canvas,
            Bitmap bitmap,
            RectF destination,
            float capX,
            float capY,
            float capWidth,
            float capHeight) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        int leftCap = Math.round(capX);
        int rightCap = Math.round(bitmap.getWidth() - capX - capWidth);
        int topCap = Math.round(capY);
        int bottomCap = Math.round(bitmap.getHeight() - capY - capHeight);
        if (leftCap < 0 || rightCap < 0 || topCap < 0 || bottomCap < 0
                || destination.width() < leftCap + rightCap
                || destination.height() < topCap + bottomCap) {
            drawBitmap(canvas, bitmap, destination);
            return;
        }
        int[] sourceX = {0, leftCap, bitmap.getWidth() - rightCap, bitmap.getWidth()};
        int[] sourceY = {0, topCap, bitmap.getHeight() - bottomCap, bitmap.getHeight()};
        float[] targetX = {
            destination.left,
            destination.left + leftCap,
            destination.right - rightCap,
            destination.right
        };
        float[] targetY = {
            destination.top,
            destination.top + topCap,
            destination.bottom - bottomCap,
            destination.bottom
        };
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(sourceX[column], sourceY[row],
                                sourceX[column + 1], sourceY[row + 1]),
                        new RectF(targetX[column], targetY[row],
                                targetX[column + 1], targetY[row + 1]),
                        bitmapPaint);
            }
        }
    }

    protected final RectF rect(TaizhouWaitingToolLayout.Box box) {
        return new RectF(box.left(), box.top(), box.right(), box.bottom());
    }
}
