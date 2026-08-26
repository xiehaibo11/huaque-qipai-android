package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.List;

/** Original-asset Canvas reconstruction in 1920x1080 top-left design space. */
final class CreateRoomRenderer {
    /** KW_OPTION_ITEM_TEXT / KW_TEXT_COST_INFO_PRE / KW_TEXT_TIPS 的 CSB 颜色。 */
    private static final int BROWN = Color.rgb(163, 111, 72);
    /** _KW_TEXT_ZHU 的 CSB 颜色。 */
    private static final int NOTE = Color.rgb(174, 137, 93);
    private static final int DISABLED = Color.rgb(160, 160, 160);
    private final CreateRoomDrawableSet drawables;
    private final Paint bitmapPaint = new Paint(
            Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final RectF destination = new RectF();
    private final CreateRoomGameListRenderer gameListRenderer;
    private final CreateRoomRulePanelRenderer rulePanelRenderer;

    CreateRoomRenderer(CreateRoomDrawableSet drawables, Typeface typeface) {
        this.drawables = drawables;
        textPaint.setTypeface(typeface);
        gameListRenderer = new CreateRoomGameListRenderer(drawables, bitmapPaint, textPaint);
        rulePanelRenderer =
                new CreateRoomRulePanelRenderer(drawables, bitmapPaint, textPaint);
    }

    void draw(
            Canvas canvas,
            List<CreateRoomGame> games,
            int selectedGameIndex,
            CreateRoomState state,
            float gameScroll,
            float ruleScroll,
            String openTipNode,
            String openDropdownNode,
            boolean loading,
            String error,
            CreateRoomResult result) {
        drawBitmap(canvas, drawables.background, 0, 0, 1920, 1080);
        drawHeader(canvas);
        gameListRenderer.draw(canvas, games, selectedGameIndex, gameScroll);
        drawBitmap(canvas, drawables.splitLine, 394, 109, 422, 1069);
        rulePanelRenderer.draw(canvas, state, ruleScroll, openTipNode, openDropdownNode);
        drawFooter(canvas, state);
        if (loading) {
            drawCentered(canvas, "正在加载房间规则…", 1170, 525, 42, BROWN);
        } else if (error != null && !error.isBlank()) {
            drawCentered(canvas, error, 1170, 525, 38, Color.rgb(174, 67, 48));
        }
        if (result != null) {
            drawResult(canvas, result);
        }
    }

    private void drawHeader(Canvas canvas) {
        drawBitmap(
                canvas,
                drawables.topBackground,
                CreateRoomLayout.TOP_BACKGROUND_LEFT,
                CreateRoomLayout.TOP_BACKGROUND_TOP,
                CreateRoomLayout.TOP_BACKGROUND_RIGHT,
                CreateRoomLayout.TOP_BACKGROUND_BOTTOM);
        drawBitmap(
                canvas,
                drawables.titleBackground,
                CreateRoomLayout.TITLE_BACKGROUND_LEFT,
                CreateRoomLayout.TITLE_BACKGROUND_TOP,
                CreateRoomLayout.TITLE_BACKGROUND_RIGHT,
                CreateRoomLayout.TITLE_BACKGROUND_BOTTOM);
        drawBitmap(
                canvas,
                drawables.title,
                CreateRoomLayout.TITLE_LEFT,
                CreateRoomLayout.TITLE_TOP,
                CreateRoomLayout.TITLE_RIGHT,
                CreateRoomLayout.TITLE_BOTTOM);
        drawBitmap(
                canvas,
                drawables.back,
                CreateRoomLayout.BACK_IMAGE_LEFT,
                6,
                CreateRoomLayout.BACK_IMAGE_RIGHT,
                110);
    }

    private void drawFooter(Canvas canvas, CreateRoomState state) {
        float createHalfWidth = CreateRoomLayout.CREATE_BUTTON_WIDTH * 0.5f;
        float createHalfHeight = CreateRoomLayout.CREATE_BUTTON_HEIGHT * 0.5f;
        drawBitmap(
                canvas,
                drawables.create,
                CreateRoomLayout.CREATE_BUTTON_CENTER_X - createHalfWidth,
                CreateRoomLayout.CREATE_BUTTON_CENTER_Y - createHalfHeight,
                CreateRoomLayout.CREATE_BUTTON_CENTER_X + createHalfWidth,
                CreateRoomLayout.CREATE_BUTTON_CENTER_Y + createHalfHeight);
        drawBitmap(
                canvas,
                drawables.roomCard,
                CreateRoomLayout.COST_ICON_LEFT,
                CreateRoomLayout.COST_ICON_TOP,
                CreateRoomLayout.COST_ICON_RIGHT,
                CreateRoomLayout.COST_ICON_BOTTOM);
        drawTextAtCenterY(
                canvas,
                "房卡消耗",
                CreateRoomLayout.COST_TEXT_LEFT,
                CreateRoomLayout.COST_TEXT_CENTER_Y,
                CreateRoomLayout.COST_TEXT_SIZE,
                BROWN);
        boolean hasFee = state != null && state.hasCompleteBusinessSelection();
        long centi = hasFee ? state.roomFeeCenti() : 0L;
        String value = !hasFee
                ? "—"
                : centi % 100 == 0
                        ? Long.toString(centi / 100)
                        : String.format(java.util.Locale.US, "%.2f", centi / 100.0);
        if (hasFee && state.isPerPlayerCost()) {
            value += "(每人)";
        }
        drawTextAtCenterY(
                canvas,
                "x " + value,
                CreateRoomLayout.COST_VALUE_LEFT,
                CreateRoomLayout.COST_TEXT_CENTER_Y,
                CreateRoomLayout.COST_TEXT_SIZE,
                BROWN);
        // _KW_TEXT_ZHU：anchor=(1,0.5)、size 496x76、字号 32，两行左对齐同一个左边界。
        drawTextAtCenterY(
                canvas,
                "注：房卡在完成第一局游戏后扣除，",
                CreateRoomLayout.NOTE_TEXT_LEFT,
                CreateRoomLayout.NOTE_TEXT_FIRST_LINE_CENTER_Y,
                CreateRoomLayout.NOTE_TEXT_SIZE,
                NOTE);
        drawTextAtCenterY(
                canvas,
                "提前解散不扣房卡",
                CreateRoomLayout.NOTE_TEXT_LEFT,
                CreateRoomLayout.NOTE_TEXT_FIRST_LINE_CENTER_Y
                        + CreateRoomLayout.NOTE_TEXT_LINE_HEIGHT,
                CreateRoomLayout.NOTE_TEXT_SIZE,
                NOTE);
        drawBitmap(
                canvas,
                drawables.feedback,
                CreateRoomLayout.FEEDBACK_LEFT,
                CreateRoomLayout.FEEDBACK_TOP,
                CreateRoomLayout.FEEDBACK_RIGHT,
                CreateRoomLayout.FEEDBACK_BOTTOM);
    }

    private void drawResult(Canvas canvas, CreateRoomResult result) {
        bitmapPaint.setColor(Color.argb(185, 32, 24, 20));
        canvas.drawRect(0, 0, 1920, 1080, bitmapPaint);
        bitmapPaint.setColor(Color.WHITE);
        drawBitmap(canvas, drawables.tipBubble, 660, 405, 1260, 675);
        // Modern handoff while the room gameplay scene is not implemented;
        // no recovered original success overlay is claimed.
        drawCentered(canvas, "房间创建成功", 960, 475, 42, BROWN);
        drawCentered(canvas, "房间号  " + result.roomNumber(), 960, 555, 58, BROWN);
        drawCentered(canvas, "点击关闭返回大厅", 960, 625, 28, BROWN);
    }


    private void drawCentered(
            Canvas canvas, String value, float centerX, float centerY, float size, int color) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(value, centerX - textPaint.measureText(value) * 0.5f,
                centerY - (metrics.ascent + metrics.descent) * 0.5f, textPaint);
    }

    private void drawTextAtCenterY(
            Canvas canvas, String value, float left, float centerY, float size, int color) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(
                value == null ? "" : value,
                left,
                centerY - (metrics.ascent + metrics.descent) * 0.5f,
                textPaint);
    }

    private void drawBitmap(
            Canvas canvas, Bitmap bitmap, float left, float top, float right, float bottom) {
        destination.set(left, top, right, bottom);
        canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
    }
}
