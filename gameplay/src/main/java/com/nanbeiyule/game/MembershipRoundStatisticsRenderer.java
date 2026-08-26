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
import java.time.format.DateTimeFormatter;

/** Draws the original SxvipStatisticsItem.csb page inside SxvipLayer.csb. */
final class MembershipRoundStatisticsRenderer {
    // Evidence: cocosStudio/hall/CSB/Sxvip/SxvipStatisticsItem.csb
    static final int ROUND_SELECTOR_NONE = -1;
    static final int ROUND_SELECTOR_MODE = 0;
    static final int ROUND_SELECTOR_GAME_PLAY = 1;
    static final int ROUND_SELECTOR_PLAYERS = 2;
    static final int ROUND_SELECTOR_START_TIME = 3;
    static final int ROUND_SELECTOR_END_TIME = 4;

    private static final float ROOT_LEFT = 433.0f;
    private static final float ROOT_TOP = 135.0f;
    private static final float ROOT_HEIGHT = 930.0f;
    private static final float INFO_PANEL_Y = 523.8898f;
    private static final float INFO_BACKGROUND_CENTER_Y = 622.4138f;
    private static final float FILTER_PANEL_Y = 724.3734f;
    private static final float SELECTOR_PANEL_Y = -160.0f;
    private static final float SELECTOR_BUTTON_CENTER_Y = FILTER_PANEL_Y + SELECTOR_PANEL_Y + 291.2399f;
    private static final float NORMAL_SELECTOR_TEXT_OFFSET_X = -18.0f;
    private static final float DATE_SELECTOR_TEXT_OFFSET_X = -23.8388f;
    private static final float NORMAL_SELECTOR_ARROW_OFFSET_X = 82.5f;
    private static final float PLAYERS_SELECTOR_ARROW_OFFSET_X = 79.4999f;
    private static final float DATE_SELECTOR_ARROW_OFFSET_X = 103.249f;
    private static final float SELECTOR_ARROW_OFFSET_Y = 1.0001f;
    private static final float SELECTOR_ARROW_SIZE = 45.0f;
    private static final float[] SELECTOR_CENTER_X = {134.2229f, 375.8962f, 620.0294f, 940.2119f, 1282.3026f};
    private static final float[] SELECTOR_WIDTH = {225.0f, 225.0f, 225.0f, 280.0f, 280.0f};
    private static final float SELECTOR_HEIGHT = 68.0f;
    private static final float DROPDOWN_ITEM_HEIGHT = 54.0f;
    private static final String[] DROPDOWN_MODE_ITEMS = new String[] {"全部模式", "好友房", "比赛场"};
    private static final String[] DROPDOWN_GAME_PLAY_ITEMS = new String[] {"全部玩法"};
    private static final String[] DROPDOWN_PLAYERS_ITEMS = new String[] {"全部人数", "2人", "3人", "4人"};
    private static final float QUICK_RANGE_BACKGROUND_CENTER_X = 1139.6685f;
    private static final float QUICK_RANGE_BACKGROUND_CENTER_Y = 765.7753f;
    private static final float STATISTICS_OPEN_BUTTON_CENTER_X = 719.07f;
    private static final float STATISTICS_OPEN_BUTTON_CENTER_Y = 299.46f;
    private static final float STATISTICS_OPEN_BUTTON_WIDTH = 408.0f;
    private static final float STATISTICS_OPEN_BUTTON_HEIGHT = 96.0f;
    private static final ZoneId ORIGINAL_TIME_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter AXIS_DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private final Typeface typeface;
    private int currentOpenDropdownIndex = ROUND_SELECTOR_NONE;

    MembershipRoundStatisticsRenderer(Typeface typeface) {
        this.typeface = typeface == null ? Typeface.DEFAULT_BOLD : typeface;
        textPaint.setTypeface(this.typeface);
    }

    static RectF openMembershipButtonBounds() {
        return csbRectFor(
                STATISTICS_OPEN_BUTTON_CENTER_X,
                STATISTICS_OPEN_BUTTON_CENTER_Y,
                STATISTICS_OPEN_BUTTON_WIDTH,
                STATISTICS_OPEN_BUTTON_HEIGHT,
                0.5f,
                0.5f);
    }

    static RectF selectorBounds(int selectorIndex) {
        if (selectorIndex < ROUND_SELECTOR_MODE || selectorIndex > ROUND_SELECTOR_END_TIME) {
            return new RectF();
        }
        return csbRectFor(SELECTOR_CENTER_X[selectorIndex], SELECTOR_BUTTON_CENTER_Y,
                SELECTOR_WIDTH[selectorIndex], SELECTOR_HEIGHT, 0.5f, 0.5f);
    }

    static int selectorIndexAt(float x, float y) {
        if (selectorBounds(ROUND_SELECTOR_MODE).contains(x, y)) {
            return ROUND_SELECTOR_MODE;
        }
        if (selectorBounds(ROUND_SELECTOR_GAME_PLAY).contains(x, y)) {
            return ROUND_SELECTOR_GAME_PLAY;
        }
        if (selectorBounds(ROUND_SELECTOR_PLAYERS).contains(x, y)) {
            return ROUND_SELECTOR_PLAYERS;
        }
        if (selectorBounds(ROUND_SELECTOR_START_TIME).contains(x, y)) {
            return ROUND_SELECTOR_START_TIME;
        }
        if (selectorBounds(ROUND_SELECTOR_END_TIME).contains(x, y)) {
            return ROUND_SELECTOR_END_TIME;
        }
        return ROUND_SELECTOR_NONE;
    }

    void draw(
            Canvas canvas,
            Bitmap statisticsBackgroundBitmap,
            Bitmap statisticsBottomBackgroundBitmap,
            Bitmap statisticsChartBitmap,
            Bitmap statisticsBlurBitmap,
            Bitmap statisticsOpenBitmap,
            Bitmap statisticsInfoBackgroundBitmap,
            Bitmap selectorBackgroundBitmap,
            Bitmap selectorArrowBitmap,
            Bitmap quickRangeBackgroundBitmap,
            Bitmap quickRangeSelectedBitmap,
            Bitmap rateIconBitmap,
            Bitmap countIconBitmap,
            Bitmap championIconBitmap,
            Bitmap scoreIconBitmap,
            Bitmap strongestFriendIconBitmap,
            Bitmap favouriteFriendIconBitmap,
            int openDropdownIndex) {
        currentOpenDropdownIndex = openDropdownIndex;
        drawBitmap(canvas, statisticsBackgroundBitmap, csbRect(720.0f, 465.0f, 1478.0f, 930.0f, 0.5f, 0.5f));
        drawBitmap(canvas, statisticsBottomBackgroundBitmap,
                csbRect(720.0f, 290.2135f, 1410.0f, 450.0f, 0.5f, 0.5f));
        drawFilters(canvas, selectorBackgroundBitmap, selectorArrowBitmap);
        drawFriendSummary(canvas, strongestFriendIconBitmap, favouriteFriendIconBitmap);
        drawQuickRanges(canvas, quickRangeBackgroundBitmap, quickRangeSelectedBitmap);
        drawMetrics(canvas, statisticsInfoBackgroundBitmap, rateIconBitmap, countIconBitmap,
                championIconBitmap, scoreIconBitmap);
        drawCharts(canvas, statisticsChartBitmap, statisticsBlurBitmap, statisticsOpenBitmap);
        drawDropdown(canvas, openDropdownIndex, selectorBackgroundBitmap);
    }

    private void drawFilters(Canvas canvas, Bitmap selectorBackgroundBitmap, Bitmap selectorArrowBitmap) {
        LocalDate today = LocalDate.now(ORIGINAL_TIME_ZONE);
        String todayText = today.format(DATE_FORMAT);
        drawSelector(canvas, "全部模式", selectorBackgroundBitmap, selectorArrowBitmap,
                134.2229f, SELECTOR_BUTTON_CENTER_Y, 225.0f,
                NORMAL_SELECTOR_TEXT_OFFSET_X, NORMAL_SELECTOR_ARROW_OFFSET_X);
        drawSelector(canvas, "全部玩法", selectorBackgroundBitmap, selectorArrowBitmap,
                375.8962f, SELECTOR_BUTTON_CENTER_Y, 225.0f,
                NORMAL_SELECTOR_TEXT_OFFSET_X, NORMAL_SELECTOR_ARROW_OFFSET_X);
        drawSelector(canvas, "全部人数", selectorBackgroundBitmap, selectorArrowBitmap,
                620.0294f, SELECTOR_BUTTON_CENTER_Y, 225.0f,
                NORMAL_SELECTOR_TEXT_OFFSET_X, PLAYERS_SELECTOR_ARROW_OFFSET_X);
        drawSelector(canvas, todayText, selectorBackgroundBitmap, selectorArrowBitmap,
                940.2119f, SELECTOR_BUTTON_CENTER_Y, 280.0f,
                DATE_SELECTOR_TEXT_OFFSET_X, DATE_SELECTOR_ARROW_OFFSET_X);
        drawText(canvas, "至", pointX(1110.1647f), pointY(852.9794f),
                40.0f, Color.rgb(162, 57, 0), Paint.Align.CENTER);
        drawSelector(canvas, todayText, selectorBackgroundBitmap, selectorArrowBitmap,
                1282.3026f, SELECTOR_BUTTON_CENTER_Y, 280.0f,
                DATE_SELECTOR_TEXT_OFFSET_X, DATE_SELECTOR_ARROW_OFFSET_X);
    }

    private void drawSelector(
            Canvas canvas,
            String label,
            Bitmap selectorBackgroundBitmap,
            Bitmap selectorArrowBitmap,
            float centerX,
            float centerY,
            float width,
            float textOffsetX,
            float arrowOffsetX) {
        RectF bounds = csbRect(centerX, centerY, width, SELECTOR_HEIGHT, 0.5f, 0.5f);
        drawBitmap(canvas, selectorBackgroundBitmap, bounds);
        drawText(canvas, label, pointX(centerX + textOffsetX), pointY(centerY),
                36.0f, Color.rgb(237, 236, 199), Paint.Align.CENTER);
        int selector = selectorIndexForCenterX(centerX);
        int openDropdownIndex = currentOpenDropdownIndex;
        float rotation = selector == openDropdownIndex ? 180.0f : 0.0f;
        if (rotation == 0.0f) {
            drawBitmapCentered(canvas, selectorArrowBitmap,
                    centerX + arrowOffsetX, centerY + SELECTOR_ARROW_OFFSET_Y,
                    SELECTOR_ARROW_SIZE, SELECTOR_ARROW_SIZE);
            return;
        }
        RectF arrowBounds = csbRect(centerX + arrowOffsetX, centerY + SELECTOR_ARROW_OFFSET_Y,
                SELECTOR_ARROW_SIZE, SELECTOR_ARROW_SIZE, 0.5f, 0.5f);
        canvas.save();
        canvas.rotate(rotation, arrowBounds.centerX(), arrowBounds.centerY());
        drawBitmap(canvas, selectorArrowBitmap, arrowBounds);
        canvas.restore();
    }

    private int selectorIndexForCenterX(float centerX) {
        for (int index = ROUND_SELECTOR_MODE; index <= ROUND_SELECTOR_END_TIME; index++) {
            if (Math.abs(centerX - SELECTOR_CENTER_X[index]) < 0.01f) {
                return index;
            }
        }
        return ROUND_SELECTOR_NONE;
    }

    private void drawDropdown(Canvas canvas, int openDropdownIndex, Bitmap selectorBackgroundBitmap) {
        if (openDropdownIndex == ROUND_SELECTOR_NONE) {
            return;
        }
        RectF bounds = selectorBounds(openDropdownIndex);
        drawDropdownItems(canvas, bounds, dropdownItemsFor(openDropdownIndex), selectorBackgroundBitmap);
    }

    private void drawDropdownItems(Canvas canvas, RectF bounds, String[] items, Bitmap selectorBackgroundBitmap) {
        // Original Lua nodes: _KWA_LISTVIEW_BG wraps _KWA_LISTVIEW under each selector panel.
        float top = bounds.bottom - 1.0f;
        RectF background = new RectF(bounds.left, top, bounds.right, top + items.length * DROPDOWN_ITEM_HEIGHT + 8.0f);
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(244, 199, 128, 78));
        canvas.drawRoundRect(background, 12.0f, 12.0f, paint);
        for (int index = 0; index < items.length; index++) {
            RectF itemBounds = new RectF(bounds.left + 4.0f, top + 4.0f + index * DROPDOWN_ITEM_HEIGHT,
                    bounds.right - 4.0f, top + 4.0f + (index + 1) * DROPDOWN_ITEM_HEIGHT);
            drawBitmap(canvas, selectorBackgroundBitmap, itemBounds);
            drawText(canvas, items[index], itemBounds.centerX(), itemBounds.centerY(),
                    31.0f, Color.rgb(237, 236, 199), Paint.Align.CENTER);
        }
    }

    private String[] dropdownItemsFor(int selectorIndex) {
        if (selectorIndex == ROUND_SELECTOR_MODE) {
            return DROPDOWN_MODE_ITEMS;
        }
        if (selectorIndex == ROUND_SELECTOR_PLAYERS) {
            return DROPDOWN_PLAYERS_ITEMS;
        }
        if (selectorIndex == ROUND_SELECTOR_START_TIME || selectorIndex == ROUND_SELECTOR_END_TIME) {
            return recentSevenDays();
        }
        return DROPDOWN_GAME_PLAY_ITEMS;
    }

    private String[] recentSevenDays() {
        LocalDate today = LocalDate.now(ORIGINAL_TIME_ZONE);
        String[] days = new String[7];
        for (int index = 0; index < days.length; index++) {
            days[index] = today.minusDays(index).format(DATE_FORMAT);
        }
        return days;
    }

    private void drawFriendSummary(
            Canvas canvas,
            Bitmap strongestFriendIconBitmap,
            Bitmap favouriteFriendIconBitmap) {
        drawFriendSummaryItem(canvas, "最香牌友:", "**", 160.1282f, 237.0f,
                134.0f, 36.0f, -41.0941f, 18.0003f, 71.0f, 77.0f, 139.0f, 18.0f,
                strongestFriendIconBitmap);
        drawFriendSummaryItem(canvas, "最强牌友:", "**", 593.6012f, 237.0f,
                134.0f, 36.0f, -38.9977f, 18.0f, 66.0f, 77.0f, 139.0f, 18.0f,
                favouriteFriendIconBitmap);
    }

    private void drawFriendSummaryItem(
            Canvas canvas,
            String label,
            String value,
            float labelCenterX,
            float labelCenterY,
            float labelWidth,
            float labelHeight,
            float iconOffsetX,
            float iconOffsetY,
            float iconWidth,
            float iconHeight,
            float valueOffsetX,
            float valueOffsetY,
            Bitmap iconBitmap) {
        float parentLeft = labelCenterX - labelWidth / 2.0f;
        float parentBottom = INFO_PANEL_Y + labelCenterY - labelHeight / 2.0f;
        drawBitmapCentered(canvas, iconBitmap, parentLeft + iconOffsetX,
                parentBottom + iconOffsetY, iconWidth, iconHeight);
        drawText(canvas, label, pointX(labelCenterX), pointY(INFO_PANEL_Y + labelCenterY),
                31.0f, Color.rgb(176, 88, 22), Paint.Align.CENTER);
        drawChildText(canvas, value, parentLeft, parentBottom, valueOffsetX, valueOffsetY,
                33.0f, Color.rgb(170, 73, 20), Paint.Align.LEFT);
    }

    private void drawQuickRanges(Canvas canvas, Bitmap quickRangeBackgroundBitmap, Bitmap quickRangeSelectedBitmap) {
        drawBitmap(canvas, quickRangeBackgroundBitmap,
                csbRect(QUICK_RANGE_BACKGROUND_CENTER_X, QUICK_RANGE_BACKGROUND_CENTER_Y,
                        601.0f, 69.0f, 0.5f, 0.5f));
        // The Lua page only makes sxvips_Btn_shijianxz visible after a quick range is selected.
        // Keep the bitmap as packaged evidence and pass it through this method; default is unselected.
        if (quickRangeSelectedBitmap != null && quickRangeSelectedBitmap.isRecycled()) {
            // Recycled evidence bitmap cannot be drawn, but the default unselected labels stay visible.
        }
        drawQuickRange(canvas, "昨日", QUICK_RANGE_BACKGROUND_CENTER_X - 180.3f, 767.2756f);
        drawQuickRange(canvas, "近3天", QUICK_RANGE_BACKGROUND_CENTER_X, 767.2753f);
        drawQuickRange(canvas, "近7天", QUICK_RANGE_BACKGROUND_CENTER_X + 180.3004f, 767.2753f);
    }

    private void drawQuickRange(Canvas canvas, String label, float centerX, float centerY) {
        drawText(canvas, label, pointX(centerX), pointY(centerY),
                36.0f, Color.rgb(162, 57, 0), Paint.Align.CENTER);
    }

    private void drawMetrics(
            Canvas canvas,
            Bitmap statisticsInfoBackgroundBitmap,
            Bitmap rateIconBitmap,
            Bitmap countIconBitmap,
            Bitmap championIconBitmap,
            Bitmap scoreIconBitmap) {
        drawBitmap(canvas, statisticsInfoBackgroundBitmap,
                csbRect(720.0f, INFO_BACKGROUND_CENTER_Y, 1410.0f, 181.0f, 0.5f, 0.5f));
        drawMetric(canvas, "期间胜率", "0%", 196.0675f, 150.48f,
                162.0f, 47.0f, -22.2786f, 23.69f, 57.0078f, -60.0f,
                rateIconBitmap);
        drawMetric(canvas, "期间场数", "0", 562.9777f, 150.4815f,
                163.0f, 47.0f, -27.7619f, 23.69f, 58.5007f, -60.0f,
                countIconBitmap);
        drawMetric(canvas, "期间冠军数", "0", 896.5595f, 148.8801f,
                203.0f, 47.0f, -26.3883f, 23.69f, 83.4939f, -60.0f,
                championIconBitmap);
        drawMetric(canvas, "期间优胜值", "0", 1234.0502f, 150.48f,
                202.0f, 47.0f, -24.5355f, 23.69f, 81.002f, -60.0f,
                scoreIconBitmap);
    }

    private void drawMetric(
            Canvas canvas,
            String label,
            String value,
            float labelCenterX,
            float labelCenterY,
            float labelWidth,
            float labelHeight,
            float iconOffsetX,
            float iconOffsetY,
            float valueOffsetX,
            float valueOffsetY,
            Bitmap iconBitmap) {
        float parentLeft = labelCenterX - labelWidth / 2.0f;
        float parentBottom = INFO_PANEL_Y + labelCenterY - labelHeight / 2.0f;
        drawBitmapCentered(canvas, iconBitmap, parentLeft + iconOffsetX,
                parentBottom + iconOffsetY, 44.0f, 38.0f);
        drawText(canvas, label, pointX(labelCenterX), pointY(INFO_PANEL_Y + labelCenterY),
                40.0f, Color.rgb(176, 88, 22), Paint.Align.CENTER);
        drawChildText(canvas, value, parentLeft, parentBottom, valueOffsetX, valueOffsetY,
                50.0f, Color.rgb(202, 50, 50), Paint.Align.CENTER);
    }

    private void drawCharts(Canvas canvas, Bitmap statisticsChartBitmap,
            Bitmap statisticsBlurBitmap, Bitmap statisticsOpenBitmap) {
        drawBitmap(canvas, statisticsChartBitmap,
                csbRect(719.0707f, 299.4599f, 1385.0f, 400.0f, 0.5f, 0.5f));
        drawChartAxisLabels(canvas);
        drawBitmap(canvas, statisticsBlurBitmap,
                csbRect(719.07f, 299.46f, 1385.0f, 400.0f, 0.5f, 0.5f));
        drawBitmap(canvas, statisticsOpenBitmap,
                openMembershipButtonBounds());
    }

    private void drawChartAxisLabels(Canvas canvas) {
        LocalDate today = LocalDate.now(ORIGINAL_TIME_ZONE);
        float[] dayX = {66.0f, 169.0f, 267.0001f, 364.0001f, 461.0005f, 559.0004f, 657.0014f};
        for (int index = 0; index < dayX.length; index++) {
            LocalDate day = today.minusDays(6L - index);
            drawText(canvas, day.format(AXIS_DATE_FORMAT), pointX(25.9994f + dayX[index]),
                    pointY(86.9808f), 26.0f, Color.rgb(109, 62, 27), Paint.Align.CENTER);
        }
        String[] weeks = {"前3周", "前2周", "前1周", "本周"};
        float[] weekX = {815.4731f, 985.1474f, 1154.956f, 1325.8855f};
        for (int index = 0; index < weeks.length; index++) {
            drawText(canvas, weeks[index], pointX(weekX[index]), pointY(86.9808f),
                    26.0f, Color.rgb(109, 62, 27), Paint.Align.CENTER);
        }
    }

    private RectF csbRect(float centerX, float centerY, float width, float height, float anchorX, float anchorY) {
        return csbRectFor(centerX, centerY, width, height, anchorX, anchorY);
    }

    private static RectF csbRectFor(
            float centerX, float centerY, float width, float height, float anchorX, float anchorY) {
        float left = ROOT_LEFT + centerX - width * anchorX;
        float bottom = centerY - height * anchorY;
        float top = ROOT_TOP + ROOT_HEIGHT - bottom - height;
        return new RectF(left, top, left + width, top + height);
    }

    private float pointX(float x) {
        return ROOT_LEFT + x;
    }

    private float pointY(float y) {
        return ROOT_TOP + ROOT_HEIGHT - y;
    }

    private void drawBitmapCentered(Canvas canvas, Bitmap bitmap,
            float centerX, float centerY, float width, float height) {
        drawBitmap(canvas, bitmap, csbRect(centerX, centerY, width, height, 0.5f, 0.5f));
    }

    private void drawChildText(Canvas canvas, String text,
            float parentLeft, float parentBottom, float offsetX, float offsetY,
            float size, int color, Paint.Align align) {
        drawText(canvas, text, pointX(parentLeft + offsetX), pointY(parentBottom + offsetY),
                size, color, align);
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
