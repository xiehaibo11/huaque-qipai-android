package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.nanbeiyule.game.goldroom.GoldChooseRoomAtlas;
import com.nanbeiyule.game.goldroom.GoldHallGameRuleDocument;
import com.nanbeiyule.game.goldroom.GoldHallGameRuleLayout;
import com.nanbeiyule.game.goldroom.GoldHallGameRuleTextLayout;
import java.util.List;

/**
 * 画 {@code GameRuleLayer.csb} 的规则弹层。
 *
 * <p>外框（金色抬头、左栏、白底、标题、关闭、页签）全部用原版 Pop 图集帧按 CSB 坐标绘制；
 * 正文是原版 WebView 远程 HTML 的原生替代，排版常量见 {@link GoldHallGameRuleLayout}。
 */
final class GoldHallGameRuleRenderer {
    private final GoldChooseRoomBitmaps bitmaps;
    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint tabTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint headingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint loadingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();

    GoldHallGameRuleRenderer(Context context, GoldChooseRoomBitmaps bitmaps) {
        this.bitmaps = bitmaps;
        Typeface typeface =
                Typeface.createFromAsset(
                        context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        tabTextPaint.setTextAlign(Paint.Align.CENTER);
        tabTextPaint.setTextSize(GoldHallGameRuleLayout.ITEM_TEXT_SIZE);
        tabTextPaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));

        bodyPaint.setColor(GoldHallGameRuleLayout.SCREENSHOT_TEXT_COLOR);
        bodyPaint.setTextSize(GoldHallGameRuleLayout.SCREENSHOT_BODY_TEXT_SIZE);
        bodyPaint.setTypeface(typeface);

        headingPaint.setColor(GoldHallGameRuleLayout.SCREENSHOT_TEXT_COLOR);
        headingPaint.setTextSize(GoldHallGameRuleLayout.SCREENSHOT_HEADING_TEXT_SIZE);
        headingPaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));

        loadingPaint.setColor(GoldHallGameRuleLayout.LOADING_TEXT_COLOR);
        loadingPaint.setTextSize(GoldHallGameRuleLayout.LOADING_TEXT_SIZE);
        loadingPaint.setTextAlign(Paint.Align.CENTER);
        loadingPaint.setTypeface(typeface);
    }

    /** 正文折行用的测量器，直接复用绘制用的 Paint，保证折行与落笔一致。 */
    GoldHallGameRuleTextLayout.Measurer measurer() {
        return (text, textSize, bold) -> (bold ? headingPaint : bodyPaint).measureText(text);
    }

    /** 白底、左栏、金色抬头、标题与关闭键，绘制顺序与 CSB 子节点顺序一致。 */
    void drawChrome(Canvas canvas) {
        drawNineSlice(
                canvas,
                bitmaps.pop("Img_tc_huang_di2.png"),
                GoldHallGameRuleLayout.PANEL_LEFT,
                GoldHallGameRuleLayout.PANEL_TOP,
                GoldHallGameRuleLayout.PANEL_WIDTH,
                GoldHallGameRuleLayout.PANEL_HEIGHT,
                GoldHallGameRuleLayout.PANEL_CAP_INSET);
        drawStretched(
                canvas,
                bitmaps.pop("Img_tc_huang_zuo.png"),
                GoldHallGameRuleLayout.LEFT_COLUMN_LEFT,
                GoldHallGameRuleLayout.LEFT_COLUMN_TOP,
                GoldHallGameRuleLayout.LEFT_COLUMN_WIDTH,
                GoldHallGameRuleLayout.LEFT_COLUMN_HEIGHT);
        drawStretched(
                canvas,
                bitmaps.pop("Img_tc_huang_big.png"),
                GoldHallGameRuleLayout.HEADER_LEFT,
                GoldHallGameRuleLayout.HEADER_TOP,
                GoldHallGameRuleLayout.HEADER_WIDTH,
                GoldHallGameRuleLayout.HEADER_HEIGHT);
        drawStretched(
                canvas,
                bitmaps.pop("Img_tc_title4.png"),
                GoldHallGameRuleLayout.TITLE_LEFT,
                GoldHallGameRuleLayout.TITLE_CENTER_Y - GoldHallGameRuleLayout.TITLE_HEIGHT / 2.0f,
                GoldHallGameRuleLayout.TITLE_WIDTH,
                GoldHallGameRuleLayout.TITLE_HEIGHT);
        drawStretched(
                canvas,
                bitmaps.pop("Btn_guanbi.png"),
                GoldHallGameRuleLayout.CLOSE_CENTER_X - GoldHallGameRuleLayout.CLOSE_WIDTH / 2.0f,
                GoldHallGameRuleLayout.CLOSE_CENTER_Y - GoldHallGameRuleLayout.CLOSE_HEIGHT / 2.0f,
                GoldHallGameRuleLayout.CLOSE_WIDTH,
                GoldHallGameRuleLayout.CLOSE_HEIGHT);
    }

    /**
     * 左栏页签。{@code RuleView:setBtnSelectState} 只换 {@code _imgBg} 贴图与 {@code _txtName}
     * 字色，几何不动；未选中帧是裁剪帧，按 plist offset 回位。
     */
    void drawTabs(Canvas canvas, List<String> titles, int selectedIndex) {
        for (int index = 0; index < titles.size(); index++) {
            boolean selected = index == selectedIndex;
            float itemTop = GoldHallGameRuleLayout.itemTop(index);
            String frameName = selected ? "Btn_tc_xz_di1.png" : "Btn_tc_xz_di2.png";
            drawItemBackground(canvas, frameName, itemTop);
            tabTextPaint.setColor(
                    selected
                            ? GoldHallGameRuleLayout.ITEM_TEXT_COLOR_SELECTED
                            : GoldHallGameRuleLayout.ITEM_TEXT_COLOR_UNSELECTED);
            float centerX =
                    GoldHallGameRuleLayout.ITEM_LEFT + GoldHallGameRuleLayout.ITEM_TEXT_CENTER_X;
            float centerY = itemTop + GoldHallGameRuleLayout.ITEM_TEXT_CENTER_Y;
            Paint.FontMetrics metrics = tabTextPaint.getFontMetrics();
            float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
            canvas.drawText(titles.get(index), centerX, baseline, tabTextPaint);
        }
    }

    /**
     * 页签底图。图集里未选中帧被裁掉了透明边，按 {@code offset} 换算回未裁剪矩形的中心，
     * 与 {@link GoldChooseRoomAtlas} 的注释同一套约定：Android Y 向下用 {@code -offsetY}。
     */
    private void drawItemBackground(Canvas canvas, String frameName, float itemTop) {
        Bitmap bitmap = bitmaps.pop(frameName);
        if (bitmap == null) {
            return;
        }
        int[] frame = frameOf(frameName);
        float centerX =
                GoldHallGameRuleLayout.ITEM_LEFT + GoldHallGameRuleLayout.ITEM_BG_CENTER_X;
        float centerY = itemTop + GoldHallGameRuleLayout.ITEM_BG_CENTER_Y;
        float offsetX = frame == null ? 0.0f : frame[5];
        float offsetY = frame == null ? 0.0f : frame[6];
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        destination.set(
                centerX + offsetX - width / 2.0f,
                centerY - offsetY - height / 2.0f,
                centerX + offsetX + width / 2.0f,
                centerY - offsetY + height / 2.0f);
        canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
    }

    private static int[] frameOf(String fileName) {
        String frameName = "hall/Image/NewGoldHall/Pop/" + fileName;
        for (int index = 0; index < GoldChooseRoomAtlas.POP_NAMES.length; index++) {
            if (GoldChooseRoomAtlas.POP_NAMES[index].equals(frameName)) {
                return GoldChooseRoomAtlas.POP_FRAMES[index];
            }
        }
        return null;
    }

    /** {@code _panelLoading} 的等待文案，原版在 WebView 完成加载前一直显示。 */
    void drawLoading(Canvas canvas) {
        Paint.FontMetrics metrics = loadingPaint.getFontMetrics();
        canvas.drawText(
                GoldHallGameRuleLayout.LOADING_TEXT,
                GoldHallGameRuleLayout.LOADING_TEXT_CENTER_X,
                GoldHallGameRuleLayout.LOADING_TEXT_CENTER_Y
                        - (metrics.ascent + metrics.descent) / 2.0f,
                loadingPaint);
    }

    /** 正文；超出 {@code _listRight} 可视区的部分被裁掉，由调用方提供滚动量。 */
    void drawContent(Canvas canvas, GoldHallGameRuleTextLayout layout, float scroll) {
        if (layout == null || layout.lines().isEmpty()) {
            return;
        }
        float viewportHeight = GoldHallGameRuleLayout.contentHeight(false);
        canvas.save();
        canvas.clipRect(
                GoldHallGameRuleLayout.CONTENT_LEFT,
                GoldHallGameRuleLayout.CONTENT_TOP,
                GoldHallGameRuleLayout.CONTENT_LEFT + GoldHallGameRuleLayout.CONTENT_WIDTH,
                GoldHallGameRuleLayout.CONTENT_TOP + viewportHeight);
        for (GoldHallGameRuleTextLayout.Line line : layout.lines()) {
            float top = GoldHallGameRuleLayout.CONTENT_TOP + line.top() - scroll;
            if (top + GoldHallGameRuleLayout.SCREENSHOT_LINE_PITCH
                            < GoldHallGameRuleLayout.CONTENT_TOP
                    || top > GoldHallGameRuleLayout.CONTENT_TOP + viewportHeight) {
                continue;
            }
            Paint paint = line.bold() ? headingPaint : bodyPaint;
            Paint.FontMetrics metrics = paint.getFontMetrics();
            canvas.drawText(
                    line.text(),
                    GoldHallGameRuleLayout.CONTENT_LEFT + line.x(),
                    top - metrics.ascent,
                    paint);
        }
        canvas.restore();
    }

    /** {@code _KW_BTN_IMAGE_TEXT} + {@code _KW_TXT_BUTTON}，仅 30579 可见。 */
    void drawImageTextButton(Canvas canvas) {
        drawStretched(
                canvas,
                bitmaps.pop("Btn_huang_1.png"),
                GoldHallGameRuleLayout.IMAGE_TEXT_CENTER_X
                        - GoldHallGameRuleLayout.IMAGE_TEXT_WIDTH / 2.0f,
                GoldHallGameRuleLayout.IMAGE_TEXT_CENTER_Y
                        - GoldHallGameRuleLayout.IMAGE_TEXT_HEIGHT / 2.0f,
                GoldHallGameRuleLayout.IMAGE_TEXT_WIDTH,
                GoldHallGameRuleLayout.IMAGE_TEXT_HEIGHT);
        Paint paint = new Paint(loadingPaint);
        paint.setColor(GoldHallGameRuleLayout.IMAGE_TEXT_TEXT_COLOR);
        paint.setTextSize(GoldHallGameRuleLayout.IMAGE_TEXT_TEXT_SIZE);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(
                GoldHallGameRuleLayout.IMAGE_TEXT_LABEL,
                GoldHallGameRuleLayout.IMAGE_TEXT_CENTER_X,
                GoldHallGameRuleLayout.IMAGE_TEXT_CENTER_Y
                        - (metrics.ascent + metrics.descent) / 2.0f,
                paint);
    }

    private void drawStretched(
            Canvas canvas, Bitmap bitmap, float left, float top, float width, float height) {
        if (bitmap == null) {
            return;
        }
        destination.set(left, top, left + width, top + height);
        canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
    }

    /** 九宫格：只保证四角不被拉伸，中段与边条按目标尺寸拉伸。 */
    private void drawNineSlice(
            Canvas canvas,
            Bitmap bitmap,
            float left,
            float top,
            float width,
            float height,
            int inset) {
        if (bitmap == null) {
            return;
        }
        int sourceWidth = bitmap.getWidth();
        int sourceHeight = bitmap.getHeight();
        int cap = Math.min(inset, Math.min(sourceWidth, sourceHeight) / 2);
        if (cap <= 0 || width <= 2 * cap || height <= 2 * cap) {
            drawStretched(canvas, bitmap, left, top, width, height);
            return;
        }
        int[] sourceX = {0, cap, sourceWidth - cap, sourceWidth};
        int[] sourceY = {0, cap, sourceHeight - cap, sourceHeight};
        float[] destinationX = {left, left + cap, left + width - cap, left + width};
        float[] destinationY = {top, top + cap, top + height - cap, top + height};
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                source.set(sourceX[column], sourceY[row], sourceX[column + 1], sourceY[row + 1]);
                destination.set(
                        destinationX[column],
                        destinationY[row],
                        destinationX[column + 1],
                        destinationY[row + 1]);
                canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
            }
        }
    }
}
