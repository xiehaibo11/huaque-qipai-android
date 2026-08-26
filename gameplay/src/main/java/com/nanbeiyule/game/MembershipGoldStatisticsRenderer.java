package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.time.LocalDate;
import java.time.ZoneId;

/** Draws the original SxvipGoldStatisticsItem.csb page inside SxvipLayer.csb. */
final class MembershipGoldStatisticsRenderer {
    // Evidence: cocosStudio/hall/CSB/Sxvip/SxvipGoldStatisticsItem.csb
    static final int GOLD_SELECTOR_NONE = -1;
    static final int GOLD_SELECTOR_GAME_PLAY = 0;

    private static final float ROOT_LEFT = 433.0f;
    private static final float ROOT_TOP = 135.0f;
    private static final float ROOT_HEIGHT = 930.0f;
    private static final RectF PANEL_BOUNDS = new RectF(448.0f, 170.0f, 1886.0f, 1000.0f);
    private static final RectF SELECTOR_BOUNDS = new RectF(480.0f, 195.0f, 795.0f, 263.0f);
    private static final RectF SELECTOR_ARROW_BOUNDS = new RectF(742.0f, 207.0f, 792.0f, 257.0f);
    private static final RectF SELECTOR_ARROW_BUTTON_BOUNDS = new RectF(740.0f, 197.0f, 795.0f, 263.0f);
    private static final RectF GAMEPLAY_DROPDOWN_BOUNDS = new RectF(480.0f, 263.0f, 795.0f, 815.0f);
    private static final String[] GAMEPLAY_DROPDOWN_ITEMS = new String[] {"全部玩法"};
    private static final RectF TABLE_BOUNDS = new RectF(505.0f, 337.0f, 1813.0f, 918.0f);
    private static final RectF TABLE_HEADER_BOUNDS = new RectF(505.0f, 337.0f, 1813.0f, 419.0f);
    private static final RectF OPEN_BUTTON_BOUNDS = new RectF(1056.0f, 542.0f, 1257.0f, 619.0f);
    private static final RectF OPEN_PROMPT_BOUNDS = new RectF(1090.0f, 665.0f, 1390.0f, 744.0f);
    private static final float[] COLUMN_X = {650.0f, 1005.0f, 1285.0f, 1605.0f};
    private static final float[] ROW_CENTER_Y = {483.0f, 605.0f, 727.0f, 849.0f};
    private static final ZoneId ORIGINAL_TIME_ZONE = ZoneId.of("Asia/Shanghai");

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Typeface typeface;

    MembershipGoldStatisticsRenderer(Typeface typeface) {
        this.typeface = typeface == null ? Typeface.DEFAULT_BOLD : typeface;
        textPaint.setTypeface(this.typeface);
    }

    static RectF openMembershipButtonBounds() {
        return new RectF(OPEN_BUTTON_BOUNDS);
    }

    static RectF gameplaySelectorBounds() {
        return new RectF(SELECTOR_BOUNDS);
    }

    static boolean gameplaySelectorContains(float x, float y) {
        return gameplaySelectorBounds().contains(x, y);
    }

    static boolean gameplayDropdownContains(float x, float y) {
        return GAMEPLAY_DROPDOWN_BOUNDS.contains(x, y);
    }

    void draw(Canvas canvas, MembershipGoldStatisticsState state, boolean loading,
            String errorMessage, Bitmap statisticsBackgroundBitmap,
            Bitmap selectorBackgroundBitmap, Bitmap selectorArrowBitmap,
            Bitmap goldStatisticsOpenButtonBitmap, Bitmap goldStatisticsPromptButtonBitmap,
            Bitmap[] goldStatisticsBlurBitmaps, int openDropdownIndex) {
        drawBitmap(canvas, statisticsBackgroundBitmap, PANEL_BOUNDS);
        drawSelector(canvas, "全部玩法", selectorBackgroundBitmap, selectorArrowBitmap,
                openDropdownIndex == GOLD_SELECTOR_GAME_PLAY);
        drawTimeRange(canvas, state);
        drawTable(canvas);
        if (state != null && state.membershipActive()) {
            drawNormalRows(canvas, state);
        } else {
            drawBlurredRows(canvas, goldStatisticsBlurBitmaps);
            drawOpenMembershipOverlay(canvas, goldStatisticsOpenButtonBitmap,
                    goldStatisticsPromptButtonBitmap);
        }
        drawBottomTip(canvas);
        drawStatus(canvas, loading, errorMessage);
        drawGameplayDropdown(canvas, openDropdownIndex, selectorBackgroundBitmap);
    }

    private void drawSelector(Canvas canvas, String label,
            Bitmap selectorBackgroundBitmap, Bitmap selectorArrowBitmap, boolean selectorOpen) {
        drawBitmap(canvas, selectorBackgroundBitmap, SELECTOR_BOUNDS);
        drawText(canvas, label, SELECTOR_BOUNDS.centerX() - 22.0f,
                SELECTOR_BOUNDS.centerY(), 38.0f, Color.WHITE, Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(70, 195, 171));
        canvas.drawRoundRect(SELECTOR_ARROW_BUTTON_BOUNDS, 9.0f, 9.0f, paint);
        float rotation = selectorOpen ? 180.0f : 0.0f;
        if (rotation == 0.0f) {
            drawBitmap(canvas, selectorArrowBitmap, SELECTOR_ARROW_BOUNDS);
            return;
        }
        canvas.save();
        canvas.rotate(rotation, SELECTOR_ARROW_BOUNDS.centerX(), SELECTOR_ARROW_BOUNDS.centerY());
        drawBitmap(canvas, selectorArrowBitmap, SELECTOR_ARROW_BOUNDS);
        canvas.restore();
    }

    private void drawGameplayDropdown(Canvas canvas, int openDropdownIndex, Bitmap selectorBackgroundBitmap) {
        if (openDropdownIndex != GOLD_SELECTOR_GAME_PLAY) {
            return;
        }
        // Original Lua nodes: _KW_IMG_GAMELIST contains _KW_LISTVIEW_GAMELIST.
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(76, 128, 83, 46));
        RectF shadow = new RectF(GAMEPLAY_DROPDOWN_BOUNDS);
        shadow.offset(6.0f, 8.0f);
        canvas.drawRoundRect(shadow, 10.0f, 10.0f, paint);
        paint.setColor(Color.rgb(255, 252, 241));
        canvas.drawRoundRect(GAMEPLAY_DROPDOWN_BOUNDS, 10.0f, 10.0f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        paint.setColor(Color.rgb(235, 214, 177));
        canvas.drawRoundRect(GAMEPLAY_DROPDOWN_BOUNDS, 10.0f, 10.0f, paint);
        drawGameplayDropdownItems(canvas, selectorBackgroundBitmap);
    }

    private void drawGameplayDropdownItems(Canvas canvas, Bitmap selectorBackgroundBitmap) {
        for (int index = 0; index < GAMEPLAY_DROPDOWN_ITEMS.length; index++) {
            float centerY = GAMEPLAY_DROPDOWN_BOUNDS.top + 67.0f + index * 58.0f;
            drawText(canvas, GAMEPLAY_DROPDOWN_ITEMS[index], GAMEPLAY_DROPDOWN_BOUNDS.centerX(),
                    centerY, 38.0f, Color.rgb(154, 105, 74), Paint.Align.CENTER);
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(232, 216, 184));
            canvas.drawRect(GAMEPLAY_DROPDOWN_BOUNDS.left + 54.0f, centerY + 41.0f,
                    GAMEPLAY_DROPDOWN_BOUNDS.right - 54.0f, centerY + 43.0f, paint);
        }
        if (selectorBackgroundBitmap != null && selectorBackgroundBitmap.isRecycled()) {
            // Keeps recovered sxvips_Img_shijiandi in the method signature while the original list remains cream.
        }
    }

    private void drawTimeRange(Canvas canvas, MembershipGoldStatisticsState state) {
        LocalDate end = state != null && state.endDate() != null
                ? state.endDate()
                : LocalDate.now(ORIGINAL_TIME_ZONE);
        LocalDate start = state != null && state.startDate() != null
                ? state.startDate()
                : end.minusDays(7);
        String text = "统计时间" + start.getMonthValue() + "月" + start.getDayOfMonth()
                + "日—" + end.getMonthValue() + "月" + end.getDayOfMonth() + "日";
        drawText(canvas, text, 1100.0f, 225.0f, 39.0f,
                Color.rgb(89, 48, 27), Paint.Align.CENTER);
    }

    private void drawTable(Canvas canvas) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 245, 210));
        canvas.drawRoundRect(TABLE_BOUNDS, 12.0f, 12.0f, paint);
        paint.setColor(Color.rgb(247, 211, 160));
        canvas.drawRect(TABLE_HEADER_BOUNDS, paint);
        drawHeader(canvas);
        paint.setColor(Color.rgb(220, 190, 145));
        paint.setStrokeWidth(2.0f);
        for (int index = 1; index <= 3; index++) {
            float y = TABLE_HEADER_BOUNDS.bottom + index * 122.0f;
            canvas.drawLine(585.0f, y, 1695.0f, y, paint);
        }
    }

    private void drawHeader(Canvas canvas) {
        String[] headers = {"时间段", "对局数", "胜率", "金币胜负"};
        for (int index = 0; index < headers.length; index++) {
            drawText(canvas, headers[index], COLUMN_X[index], 378.0f,
                    40.0f, Color.rgb(185, 83, 63), Paint.Align.CENTER);
        }
    }

    private void drawNormalRows(Canvas canvas, MembershipGoldStatisticsState state) {
        drawPeriod(canvas, 0, state.today());
        drawPeriod(canvas, 1, state.yesterday());
        drawPeriod(canvas, 2, state.lastThree());
        drawPeriod(canvas, 3, state.lastSeven());
    }

    private void drawPeriod(Canvas canvas, int row, MembershipGoldStatisticsState.Period period) {
        drawText(canvas, period.label(), COLUMN_X[0], ROW_CENTER_Y[row],
                40.0f, Color.rgb(89, 48, 27), Paint.Align.CENTER);
        drawText(canvas, String.valueOf(period.fightCnt()), COLUMN_X[1], ROW_CENTER_Y[row],
                38.0f, Color.rgb(89, 48, 27), Paint.Align.CENTER);
        drawText(canvas, winRateText(period), COLUMN_X[2], ROW_CENTER_Y[row],
                38.0f, Color.rgb(89, 48, 27), Paint.Align.CENTER);
        drawText(canvas, String.valueOf(period.winScore()), COLUMN_X[3], ROW_CENTER_Y[row],
                38.0f, Color.rgb(89, 48, 27), Paint.Align.CENTER);
    }

    private static String winRateText(MembershipGoldStatisticsState.Period period) {
        return period.winRate() == 0 ? "--" : period.winRate() + "%";
    }

    private void drawBlurredRows(Canvas canvas, Bitmap[] blurBitmaps) {
        String[] labels = {"今日", "昨日", "最近3日", "最近7日"};
        for (int row = 0; row < ROW_CENTER_Y.length; row++) {
            drawText(canvas, labels[row], COLUMN_X[0], ROW_CENTER_Y[row],
                    40.0f, Color.rgb(89, 48, 27), Paint.Align.CENTER);
            drawBlur(canvas, blurBitmaps, row % 2, COLUMN_X[1], ROW_CENTER_Y[row]);
            drawBlur(canvas, blurBitmaps, row % 2, COLUMN_X[2], ROW_CENTER_Y[row]);
            drawBlur(canvas, blurBitmaps, 2 + row % 2, COLUMN_X[3], ROW_CENTER_Y[row]);
        }
    }

    private void drawBlur(Canvas canvas, Bitmap[] blurBitmaps, int index, float centerX, float centerY) {
        if (blurBitmaps == null || index < 0 || index >= blurBitmaps.length) {
            return;
        }
        Bitmap bitmap = blurBitmaps[index];
        if (bitmap == null) {
            return;
        }
        drawBitmap(canvas, bitmap, new RectF(
                centerX - bitmap.getWidth() * 0.5f,
                centerY - bitmap.getHeight() * 0.5f,
                centerX + bitmap.getWidth() * 0.5f,
                centerY + bitmap.getHeight() * 0.5f));
    }

    private void drawOpenMembershipOverlay(Canvas canvas,
            Bitmap goldStatisticsOpenButtonBitmap, Bitmap goldStatisticsPromptButtonBitmap) {
        drawBitmap(canvas, goldStatisticsOpenButtonBitmap, OPEN_BUTTON_BOUNDS);
        drawBitmap(canvas, goldStatisticsPromptButtonBitmap, OPEN_PROMPT_BOUNDS);
    }

    private void drawBottomTip(Canvas canvas) {
        drawText(canvas, "仅开通会员期间的近7天金币场数据", 1545.0f, 1045.0f,
                32.0f, Color.rgb(202, 92, 70), Paint.Align.CENTER);
    }

    private void drawStatus(Canvas canvas, boolean loading, String errorMessage) {
        if (loading) {
            drawText(canvas, "金币统计加载中...", 1160.0f, 955.0f,
                    30.0f, Color.rgb(126, 74, 35), Paint.Align.CENTER);
        } else if (errorMessage != null && errorMessage.isBlank()) {
            // Original SxvipGoldStatisticsItem keeps the masked panel visible on failures.
        }
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF bounds) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        destination.set(bounds);
        paint.setShader(null);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, source, destination, paint);
    }

    private void drawText(Canvas canvas, String text, float x, float centerY,
            float size, int color, Paint.Align align) {
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(color);
        textPaint.clearShadowLayer();
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(text, x, baseline, textPaint);
    }
}
