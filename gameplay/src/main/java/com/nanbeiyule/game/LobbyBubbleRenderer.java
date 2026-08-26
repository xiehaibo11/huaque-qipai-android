package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * 绘制原版大厅气泡 BubbleItem。
 *
 * <p>底图是原版 {@code hall_tip_type_8.png}（{@code BubbleItem.csb} 里 bub 的默认贴图，
 * 游戏卡片路径不调用 {@code setBuble()}，因此保持该贴图与 (0,0) 锚点）。
 * 九宫格、宽度公式、文字位置见 {@link LobbyBubbleLayout}，播放时间线见
 * {@link LobbyBubbleAnimation}。
 *
 * <p>气泡挂点是南北自有决定：原版挂在游戏卡片 CSB 的 {@code hall_tip_type_2} 节点上，
 * 而当前十二槽玩法矩阵是南北自己的布局，没有该节点，因此按槽位左上角摆放。
 */
final class LobbyBubbleRenderer {

    /** 锚点相对槽位左上角的偏移，南北布局决定值。 */
    private static final float ANCHOR_INSET_X = 40.0f;

    /**
     * 气泡以锚点为底边向上绘制，因此该偏移决定气泡顶边落在哪里。
     *
     * <p>取 48 是为了让第一排槽位（top = 220）的气泡顶边落在
     * {@code 220 + 48 - 43 × 3200/1334 = 164.9}，刚好在钱包胶囊面板底边 164 之下；
     * 原值 24 会让顶边落到 140.9，压住顶栏。后两排槽位用同一偏移也更远离上一排卡片。
     */
    private static final float ANCHOR_INSET_Y = 48.0f;

    private final Bitmap bubble;
    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();

    LobbyBubbleRenderer(Context context) {
        bubble =
                BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.lobby_bubble_type_8);
        // BubbleItem.csb 的 fontName 为空、串表为 font/sysfont.ttf，即系统默认字体。
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(LobbyBubbleLayout.designFontSize());
        textPaint.setColor(LobbyBubbleLayout.TEXT_COLOR);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    boolean hasArtwork() {
        return bubble != null && !bubble.isRecycled();
    }

    /**
     * @param tile 承载气泡的槽位
     * @param entry 服务端入口配置
     * @param elapsedSeconds 大厅显示以来的秒数
     * @return 该入口是否存在可播放的气泡
     */
    boolean draw(
            Canvas canvas,
            GameHomeV3Layout.Tile tile,
            GameHomeState.Entry entry,
            float elapsedSeconds) {
        if (!hasArtwork() || entry == null) {
            return false;
        }
        String text = entry.bubbleText();
        if (text == null || text.isEmpty()) {
            return false;
        }
        LobbyBubbleAnimation animation =
                LobbyBubbleAnimation.of(
                        entry.bubbleType(),
                        LobbyBubbleLayout.intervalSeconds(entry.bubbleIntervalSeconds()));
        if (animation == null) {
            return false;
        }
        LobbyBubbleAnimation.Frame frame = animation.frameAt(elapsedSeconds);
        if (!frame.visible() || frame.alpha() <= 0.0f || frame.scaleX() <= 0.0f) {
            return true;
        }

        float width = LobbyBubbleLayout.designWidth(text);
        float height = LobbyBubbleLayout.designHeight();
        float anchorX = tile.destination().left() + ANCHOR_INSET_X;
        float anchorY = tile.destination().top() + ANCHOR_INSET_Y;
        int alpha = Math.round(Math.max(0.0f, Math.min(1.0f, frame.alpha())) * 255.0f);

        int save = canvas.save();
        // cocos 的 MoveTo Y 轴向上，设计空间 Y 轴向下，所以取负。
        canvas.translate(
                anchorX, anchorY - frame.offsetY() * LobbyBubbleLayout.DESIGN_SCALE);
        // ScaleTo 以锚点为中心，锚点即 (0,0)。
        canvas.scale(frame.scaleX(), frame.scaleY());

        bitmapPaint.setAlpha(alpha);
        drawNinePatch(canvas, width, height);

        textPaint.setAlpha(alpha);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = -height / 2.0f - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(
                text,
                LobbyBubbleLayout.originalTextCentreX(text) * LobbyBubbleLayout.DESIGN_SCALE,
                baseline,
                textPaint);
        canvas.restoreToCount(save);
        return true;
    }

    /** 横向三段九宫格；capInsets 的高度覆盖整张图，纵向不拉伸。 */
    private void drawNinePatch(Canvas canvas, float width, float height) {
        float leftCap = LobbyBubbleLayout.designCapLeft();
        float rightCap = LobbyBubbleLayout.designCapRight();
        int leftCapPx = Math.round(LobbyBubbleLayout.ORIGINAL_CAP_LEFT);
        int rightCapPx = Math.round(LobbyBubbleLayout.originalCapRight());
        int sheetWidth = bubble.getWidth();
        int sheetHeight = bubble.getHeight();
        float top = -height;

        source.set(0, 0, leftCapPx, sheetHeight);
        destination.set(0.0f, top, leftCap, 0.0f);
        canvas.drawBitmap(bubble, source, destination, bitmapPaint);

        source.set(leftCapPx, 0, sheetWidth - rightCapPx, sheetHeight);
        destination.set(leftCap, top, width - rightCap, 0.0f);
        canvas.drawBitmap(bubble, source, destination, bitmapPaint);

        source.set(sheetWidth - rightCapPx, 0, sheetWidth, sheetHeight);
        destination.set(width - rightCap, top, width, 0.0f);
        canvas.drawBitmap(bubble, source, destination, bitmapPaint);
    }

    void recycle() {
        if (bubble != null && !bubble.isRecycled()) {
            bubble.recycle();
        }
    }
}
