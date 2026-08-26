package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouEarlyStartLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableAtlas;

/**
 * Draws the {@code TableInfo.csb} 提前开局按钮与「N人也能开！」气泡。
 * 帧来自已恢复的 {@code earlyStart} 图集（tz_btn_early_start.png /
 * tz_bg_early_start_tip.png），绘制位置完全由 {@link TaizhouEarlyStartProjection} 决定。
 */
final class TaizhouEarlyStartRenderer {
    // 推断: CSB Text 节点 209×49 未含字号与颜色；气泡是同族白底气泡
    // （与 taizhou_mahjong_copy_tip 同族），沿用其已验证的描边画法。
    private static final int TIP_TEXT_STROKE = Color.rgb(151, 61, 24);

    private static final int TIP_TEXT_FILL = Color.rgb(255, 239, 177);

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Bitmap button;
    private final Bitmap bubble;

    TaizhouEarlyStartRenderer(Context context) {
        Bitmap atlas =
                BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.taizhou_mahjong_early_start);
        button = extract(atlas, "tz_btn_early_start.png");
        bubble = extract(atlas, "tz_bg_early_start_tip.png");
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    void draw(Canvas canvas, GameplayTableState tableState) {
        if (!TaizhouEarlyStartProjection.showButton(tableState)) {
            return;
        }
        TaizhouEarlyStartLayout.Node node = TaizhouEarlyStartProjection.buttonNode(tableState);
        drawBitmap(canvas, button, node.left(), node.top(), node.width(), node.height());
        TaizhouEarlyStartLayout.Node bubbleNode = TaizhouEarlyStartLayout.BUBBLE;
        float bubbleCenterX = bubbleNode.centerX();
        drawBitmap(
                canvas,
                bubble,
                bubbleNode.left(),
                bubbleNode.top(),
                bubbleNode.width(),
                bubbleNode.height());
        // 推断: 字号按节点高度取 30，基线按描边文字的 FontMetrics 居中。
        String tip = TaizhouEarlyStartProjection.tipText(tableState);
        float textCenterY = TaizhouEarlyStartLayout.TIP_TEXT.centerY();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(30.0f);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = textCenterY - (metrics.ascent + metrics.descent) * 0.5f;
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(2.5f);
        textPaint.setColor(TIP_TEXT_STROKE);
        canvas.drawText(tip, bubbleCenterX, baseline, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(TIP_TEXT_FILL);
        canvas.drawText(tip, bubbleCenterX, baseline, textPaint);
    }

    private void drawBitmap(
            Canvas canvas, Bitmap bitmap, float left, float top, float width, float height) {
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(
                    bitmap, null, new RectF(left, top, left + width, top + height), bitmapPaint);
        }
    }

    /** Extracts one upright frame from the recovered {@code earlyStart} atlas. */
    private static Bitmap extract(Bitmap atlas, String frameName) {
        if (atlas == null
                || atlas.isRecycled()
                || atlas.getWidth() != TaizhouMahjongTableAtlas.EARLY_START_WIDTH
                || atlas.getHeight() != TaizhouMahjongTableAtlas.EARLY_START_HEIGHT) {
            throw new IllegalArgumentException("Invalid original earlyStart atlas");
        }
        int index =
                TaizhouMahjongTableAtlas.indexOf(
                        TaizhouMahjongTableAtlas.EARLY_START_NAMES, frameName);
        if (index < 0) {
            throw new IllegalArgumentException("Missing original earlyStart frame " + frameName);
        }
        int[] frame = TaizhouMahjongTableAtlas.EARLY_START_FRAMES[index];
        int storedWidth = frame[4] == 0 ? frame[2] : frame[3];
        int storedHeight = frame[4] == 0 ? frame[3] : frame[2];
        Bitmap stored =
                Bitmap.createBitmap(atlas, frame[0], frame[1], storedWidth, storedHeight);
        if (frame[4] == 0) {
            return stored;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(-90.0f);
        Bitmap upright =
                Bitmap.createBitmap(stored, 0, 0, storedWidth, storedHeight, matrix, true);
        if (upright != stored) {
            stored.recycle();
        }
        return upright;
    }
}
