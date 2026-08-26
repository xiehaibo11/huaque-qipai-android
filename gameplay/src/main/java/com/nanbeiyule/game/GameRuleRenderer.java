package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.List;

/** Native GameRuleLayer chrome and recovered 18-item selector; rule body remains a WebView. */
final class GameRuleRenderer {
    private final Bitmap background;
    private final Bitmap top;
    private final Bitmap title;
    private final Bitmap back;
    private final Bitmap normal;
    private final Bitmap selected;
    private final Bitmap divider;
    private final Bitmap mark;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final RectF destination = new RectF();

    GameRuleRenderer(Context context) {
        background = decode(context, R.drawable.tea_house_rank_bg);
        top = decode(context, R.drawable.tea_house_rank_top_bg);
        title = decode(context, R.drawable.tea_house_rank_title_bg);
        back = decode(context, R.drawable.com_btn_back);
        normal = decode(context, R.drawable.com_btn_label_normal);
        selected = decode(context, R.drawable.com_btn_label_select);
        divider = decode(context, R.drawable.com_ui_line_2);
        mark = decode(context, R.drawable.create_box_room_mark);
        textPaint.setTypeface(Typeface.createFromAsset(
                context.getAssets(), "fonts/zihun_jingdian_lihei.ttf"));
    }

    void drawChrome(Canvas canvas, GameRuleState state, float listScroll) {
        draw(background, canvas, 0f, 0f, 1920f, 1080f);
        draw(top, canvas, 0f, 1.5f, 1920f, 121f);
        draw(title, canvas, 668f, -7f, 584f, 120f);
        draw(back, canvas, 43f, 2f, 94f, 102f);
        draw(divider, canvas, 394f, 109f, 24f, 960f);
        centered(canvas, "规  则", 960f, 73f, 60f, Color.rgb(255, 248, 199), true);
        if (state.selected().gameId() == GameRuleLayout.IMAGE_TUTORIAL_GAME_ID) {
            drawImageTutorial(canvas);
        }
        drawItems(canvas, state.entries(), state.selectedIndex(), listScroll);
    }

    private void drawImageTutorial(Canvas canvas) {
        Paint button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(Color.rgb(255, 222, 107));
        canvas.drawRoundRect(GameRuleLayout.IMAGE_TUTORIAL_LEFT,
                GameRuleLayout.IMAGE_TUTORIAL_TOP + 10f,
                GameRuleLayout.IMAGE_TUTORIAL_RIGHT,
                GameRuleLayout.IMAGE_TUTORIAL_BOTTOM - 5f, 8f, 8f, button);
        centered(canvas, "图文教程", 1680f, 53f, 40f, Color.rgb(206, 92, 4), true);
    }

    void drawStatus(Canvas canvas, GameRuleState state) {
        String message = switch (state.content()) {
            case LOADING -> "规则加载中请稍后...";
            case MISSING -> "该玩法规则数据缺失";
            case ERROR -> state.error();
            case DOCUMENT -> null;
        };
        if (message == null) return;
        Paint shade = new Paint(Paint.ANTI_ALIAS_FLAG);
        shade.setColor(Color.argb(232, 250, 240, 204));
        canvas.drawRect(GameRuleLayout.CONTENT_LEFT, GameRuleLayout.CONTENT_TOP,
                GameRuleLayout.CONTENT_LEFT + GameRuleLayout.CONTENT_WIDTH,
                GameRuleLayout.CONTENT_TOP + GameRuleLayout.CONTENT_HEIGHT, shade);
        centered(canvas, message, 1157f, 665f, 40f, Color.rgb(191, 108, 29), false);
    }

    private void drawItems(Canvas canvas, List<GameRuleCatalog.Entry> entries, int selectedIndex,
            float scroll) {
        int save = canvas.save();
        canvas.clipRect(GameRuleLayout.LIST_LEFT, GameRuleLayout.LIST_TOP,
                GameRuleLayout.LIST_LEFT + GameRuleLayout.LIST_WIDTH,
                GameRuleLayout.LIST_TOP + GameRuleLayout.LIST_HEIGHT);
        for (int index = 0; index < entries.size(); index++) {
            float itemTop = GameRuleLayout.itemTop(index, scroll);
            boolean isSelected = index == selectedIndex;
            draw(isSelected ? selected : normal, canvas, GameRuleLayout.ITEM_LEFT, itemTop,
                    GameRuleLayout.ITEM_WIDTH, GameRuleLayout.ITEM_HEIGHT);
            GameRuleCatalog.Entry entry = entries.get(index);
            float size = isSelected ? GameRuleLayout.SELECTED_TEXT_SIZE
                    : GameRuleLayout.UNSELECTED_TEXT_SIZE;
            int color = isSelected ? GameRuleLayout.SELECTED_TEXT_COLOR
                    : GameRuleLayout.UNSELECTED_TEXT_COLOR;
            size = fitSize(entry.title(), size, 310f);
            centered(canvas, entry.title(), GameRuleLayout.ITEM_LEFT + 182f,
                    itemTop + 75f, size, color, isSelected);
            if (!entry.mark().isEmpty()) drawMark(canvas, entry.mark(), itemTop);
        }
        canvas.restoreToCount(save);
    }

    private void drawMark(Canvas canvas, String value, float itemTop) {
        draw(mark, canvas, GameRuleLayout.ITEM_LEFT, itemTop, 112f, 112f);
        int save = canvas.save();
        canvas.rotate(-45f, GameRuleLayout.ITEM_LEFT + 44f, itemTop + 42f);
        centered(canvas, value, GameRuleLayout.ITEM_LEFT + 44f, itemTop + 52f,
                34f, Color.WHITE, true);
        canvas.restoreToCount(save);
    }

    private float fitSize(String value, float start, float width) {
        textPaint.setTextSize(start);
        if (textPaint.measureText(value) <= width) return start;
        return Math.max(38f, start * width / textPaint.measureText(value));
    }

    private void centered(Canvas canvas, String value, float x, float centerY, float size,
            int color, boolean bold) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setFakeBoldText(bold);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(value == null ? "" : value, x,
                centerY - (metrics.ascent + metrics.descent) * 0.5f, textPaint);
    }

    private void draw(Bitmap bitmap, Canvas canvas, float left, float top, float width,
            float height) {
        destination.set(left, top, left + width, top + height);
        canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
    }

    void release() {
        recycle(background);
        recycle(top);
        recycle(title);
        recycle(back);
        recycle(normal);
        recycle(selected);
        recycle(divider);
        recycle(mark);
    }

    private static Bitmap decode(Context context, int id) {
        return BitmapFactory.decodeResource(context.getResources(), id);
    }

    private static void recycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
    }
}
