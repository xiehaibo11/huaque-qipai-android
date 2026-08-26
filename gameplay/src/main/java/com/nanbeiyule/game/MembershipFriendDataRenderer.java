package com.nanbeiyule.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Draws the original SxvipFriendInfoItem.csb page inside SxvipLayer.csb. */
final class MembershipFriendDataRenderer {
    // Evidence: cocosStudio/hall/CSB/Sxvip/SxvipFriendInfoItem.csb
    private static final RectF FRIEND_DATA_PANEL_BOUNDS = new RectF(448.0f, 170.0f, 1886.0f, 1000.0f);
    private static final RectF HEADER_BOUNDS = new RectF(448.0f, 170.0f, 1886.0f, 255.0f);
    private static final RectF OPEN_TOUCH_BOUNDS = new RectF(448.0f, 255.0f, 1886.0f, 1000.0f);
    private static final float[] COLUMN_X = {545.0f, 700.0f, 935.0f, 1135.0f, 1305.0f, 1495.0f, 1745.0f};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Typeface typeface;

    MembershipFriendDataRenderer(Typeface typeface) {
        this.typeface = typeface == null ? Typeface.DEFAULT_BOLD : typeface;
        textPaint.setTypeface(this.typeface);
    }

    static RectF openMembershipTouchBounds() {
        return new RectF(OPEN_TOUCH_BOUNDS);
    }

    static RectF openMembershipButtonBounds() {
        return MembershipGoldStatisticsRenderer.openMembershipButtonBounds();
    }

    void draw(Canvas canvas) {
        drawPanel(canvas);
        drawHeader(canvas);
        drawNoMemberMask(canvas);
    }

    private void drawPanel(Canvas canvas) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(205, 169, 115));
        canvas.drawRoundRect(FRIEND_DATA_PANEL_BOUNDS, 20.0f, 20.0f, paint);
    }

    private void drawHeader(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(166, 96, 59));
        canvas.drawRoundRect(HEADER_BOUNDS, 18.0f, 18.0f, paint);
        canvas.drawRect(HEADER_BOUNDS.left, HEADER_BOUNDS.centerY(), HEADER_BOUNDS.right, HEADER_BOUNDS.bottom, paint);
        drawText(canvas, "昵称", COLUMN_X[0], 213.0f, 34.0f, Color.rgb(248, 232, 208), Paint.Align.CENTER);
        drawText(canvas, "与我对局数\n对我胜率", COLUMN_X[1], 213.0f, 31.0f, Color.rgb(248, 232, 208), Paint.Align.CENTER);
        drawText(canvas, "牌友总胜率", COLUMN_X[2], 213.0f, 34.0f, Color.rgb(248, 232, 208), Paint.Align.CENTER);
        drawText(canvas, "出牌速度", COLUMN_X[3], 213.0f, 34.0f, Color.rgb(248, 232, 208), Paint.Align.CENTER);
        drawText(canvas, "离线次数", COLUMN_X[4], 213.0f, 34.0f, Color.rgb(248, 232, 208), Paint.Align.CENTER);
        drawText(canvas, "解散次数", COLUMN_X[5], 213.0f, 34.0f, Color.rgb(248, 232, 208), Paint.Align.CENTER);
        drawText(canvas, "我胜对方\n分数", COLUMN_X[6], 213.0f, 31.0f, Color.rgb(248, 232, 208), Paint.Align.CENTER);
    }

    private void drawNoMemberMask(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(80, 255, 245, 224));
        canvas.drawRect(OPEN_TOUCH_BOUNDS, paint);
        drawText(canvas, "开通会员，可查看牌友数据~", 1138.0f, 600.0f,
                47.0f, Color.WHITE, Paint.Align.CENTER);
    }

    private void drawText(Canvas canvas, String text, float x, float centerY,
            float size, int color, Paint.Align align) {
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(color);
        textPaint.clearShadowLayer();
        String[] lines = text.split("\\n", -1);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float lineHeight = metrics.descent - metrics.ascent;
        float base = centerY - (lines.length - 1) * lineHeight * 0.5f;
        for (int index = 0; index < lines.length; index++) {
            float baseline = base + index * lineHeight - (metrics.ascent + metrics.descent) / 2.0f;
            canvas.drawText(lines[index], x, baseline, textPaint);
        }
    }
}
