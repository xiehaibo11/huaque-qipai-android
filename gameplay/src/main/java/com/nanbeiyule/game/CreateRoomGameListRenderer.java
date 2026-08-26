package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.List;

/**
 * 左侧 {@code _KW_LISTVIEW_CHOOSE_GAME} 玩法列表。
 *
 * <p>几何全部来自 {@code CreateBoxRoomDynamic.csb} 的 {@code _KW_BTN_MODEl} 模板与
 * {@code hall/CSB/Mark.csb}，取色与字号来自同一批节点属性；选中/未选中的字号与颜色来自
 * {@code lobby/Modules/CreateBoxRoom/View.lua:onTouchEventChooseGameDynamic}。
 */
final class CreateRoomGameListRenderer {
    /** {@code TabTextSelectedColor}：未被选中的页签文字。 */
    private static final int TAB_TEXT_NORMAL = Color.rgb(163, 111, 72);
    /** {@code TabTextDefaultColor}，同时也是 {@code KW_TEXT_MODEl} 的 CSB 默认色。 */
    private static final int TAB_TEXT_SELECTED = Color.rgb(255, 251, 205);

    private final CreateRoomDrawableSet drawables;
    private final Paint bitmapPaint;
    private final Paint textPaint;
    private final RectF destination = new RectF();

    CreateRoomGameListRenderer(
            CreateRoomDrawableSet drawables, Paint bitmapPaint, Paint textPaint) {
        this.drawables = drawables;
        this.bitmapPaint = bitmapPaint;
        this.textPaint = textPaint;
    }

    void draw(Canvas canvas, List<CreateRoomGame> games, int selectedIndex, float scroll) {
        canvas.save();
        canvas.clipRect(
                CreateRoomLayout.GAME_LIST_LEFT,
                CreateRoomLayout.GAME_LIST_TOP,
                CreateRoomLayout.GAME_LIST_RIGHT,
                CreateRoomLayout.GAME_LIST_BOTTOM);
        float contentTop = CreateRoomLayout.gameContentTop(games.size());
        for (int index = 0; index < games.size(); index++) {
            float top = contentTop + index * CreateRoomLayout.GAME_TAB_STRIDE - scroll;
            if (top > CreateRoomLayout.GAME_LIST_BOTTOM
                    || top + CreateRoomLayout.GAME_TAB_HEIGHT < CreateRoomLayout.GAME_LIST_TOP) {
                continue;
            }
            drawTab(canvas, games.get(index), top, index == selectedIndex);
        }
        canvas.restore();
    }

    private void drawTab(Canvas canvas, CreateRoomGame game, float top, boolean selected) {
        float left = CreateRoomLayout.GAME_TAB_LEFT;
        float bottom = top + CreateRoomLayout.GAME_TAB_HEIGHT;
        // _KW_BTN_MODEl 未开启 ignoreContentAdaptWithSize，节点尺寸恒为 360x136；
        // 选中态只是切到 disabled 贴图 com_btn_label_select.png，由引擎拉伸回节点尺寸。
        Bitmap bitmap = selected ? drawables.tabSelected : drawables.tabNormal;
        drawBitmap(canvas, bitmap, left, top, left + CreateRoomLayout.GAME_TAB_WIDTH, bottom);
        drawCentered(
                canvas,
                game.displayName(),
                left + CreateRoomLayout.GAME_TAB_TEXT_LOCAL_X,
                top + CreateRoomLayout.GAME_TAB_TEXT_LOCAL_Y,
                selected
                        ? CreateRoomLayout.GAME_TAB_TEXT_SIZE_SELECTED
                        : CreateRoomLayout.GAME_TAB_TEXT_SIZE,
                selected ? TAB_TEXT_SELECTED : TAB_TEXT_NORMAL);
        if (CreateRoomEntryPolicy.isExternalMiniProgramGame(game.gameId())) {
            drawExternalMark(canvas, left, top);
        } else if (!game.badge().isBlank()) {
            drawMark(canvas, game.badge(), left, bottom);
        }
    }

    private void drawExternalMark(Canvas canvas, float tabLeft, float tabTop) {
        float left = tabLeft + CreateRoomLayout.EXTERNAL_BADGE_LEFT_OFFSET;
        float top = tabTop + CreateRoomLayout.EXTERNAL_BADGE_TOP_OFFSET;
        drawBitmap(
                canvas,
                drawables.externalGameBadge,
                left,
                top,
                left + CreateRoomLayout.EXTERNAL_BADGE_WIDTH,
                top + CreateRoomLayout.EXTERNAL_BADGE_HEIGHT);
    }

    /** {@code Mark.new(gameID)} 挂在页签本地 (0,0)：左下对齐，向上占满 150。 */
    private void drawMark(Canvas canvas, String badge, float tabLeft, float tabBottom) {
        float markTop = tabBottom - CreateRoomLayout.GAME_MARK_SIZE;
        drawBitmap(
                canvas,
                drawables.gameBadge,
                tabLeft,
                markTop,
                tabLeft + CreateRoomLayout.GAME_MARK_SIZE,
                tabBottom);
        float centerX = tabLeft + CreateRoomLayout.GAME_MARK_TEXT_LOCAL_X;
        float centerY = markTop + CreateRoomLayout.GAME_MARK_TEXT_LOCAL_Y;
        canvas.save();
        canvas.rotate(CreateRoomLayout.GAME_MARK_ROTATION_DEGREES, centerX, centerY);
        drawCentered(
                canvas, badge, centerX, centerY, CreateRoomLayout.markTextSize(badge), Color.WHITE);
        canvas.restore();
    }

    private void drawCentered(
            Canvas canvas, String value, float centerX, float centerY, float size, int color) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(
                value,
                centerX - textPaint.measureText(value) * 0.5f,
                centerY - (metrics.ascent + metrics.descent) * 0.5f,
                textPaint);
    }

    private void drawBitmap(
            Canvas canvas, Bitmap bitmap, float left, float top, float right, float bottom) {
        destination.set(left, top, right, bottom);
        canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
    }
}
