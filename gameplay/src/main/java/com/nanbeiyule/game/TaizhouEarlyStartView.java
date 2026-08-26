package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import com.nanbeiyule.game.mahjong.TaizhouEarlyStartLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableAtlas;

/**
 * {@code TaiZhou/EarlyStart.csb} 确认弹层（1087×660 公共面板）。
 *
 * <p>QA 链路语义：弹层只是房主的本地确认——「同意」提交 {@code EARLY_START} 命令并收起，
 * 「拒绝」直接收起；原版的多人同意/拒绝流（{@code EarlyStartView.luac:171-197} 的玩家位
 * 状态刷新与 {@code _KW_PANEL_PLAYER_POS} 列表）不接入，{@code _KW_IMG_CLOCK} 倒计时与
 * {@code _KW_TEXT_OUT_TIME_TIP} 在 time=0 时按 {@code EarlyStartView.luac:131-151} 保持
 * 隐藏，{@code _KW_BTN_CLOSE} 按 CSB {@code visible=False} 保持隐藏。
 */
@SuppressLint("ViewConstructor")
final class TaizhouEarlyStartView extends TaizhouToolView {
    interface Actions {
        void onAgree();
    }

    private final Actions actions;
    private final String requesterName;
    private final Bitmap panel;
    private final Bitmap titleBackground;
    private final Bitmap titleBackgroundMirrored;
    private final Bitmap title;
    private final Bitmap flower;
    private final Bitmap flowerMirrored;
    private final Bitmap agree;
    private final Bitmap refuse;
    private Runnable dismissAction = () -> {};

    TaizhouEarlyStartView(Context context, String requesterName, Actions actions) {
        super(context);
        this.actions = actions;
        this.requesterName = requesterName == null ? "" : requesterName;
        panel = bitmap(R.drawable.payment_cancel_panel);
        titleBackground = bitmap(R.drawable.payment_cancel_title_background);
        titleBackgroundMirrored = mirror(titleBackground);
        title = extractTitle(context);
        flower = bitmap(R.drawable.membership_notice_flower);
        flowerMirrored = mirror(flower);
        agree = bitmap(R.drawable.taizhou_early_start_agree);
        refuse = bitmap(R.drawable.taizhou_early_start_refuse);
        setContentDescription("提前开局确认");
    }

    void setDismissAction(Runnable action) {
        dismissAction = action == null ? () -> {} : action;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        // 推断: KW_PANEL_BG 的遮罩色解析工具读不到，沿用同族弹层已验证的压暗值。
        fillPaint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(0.0f, 0.0f, 1920.0f, 1080.0f, fillPaint);
        // Image_26：common_layer_bg.png 按同族面板（VipNoticeLayer）已验证的 33px 九宫格绘制。
        drawNineSlice(
                canvas,
                panel,
                new RectF(
                        TaizhouEarlyStartLayout.PANEL_BG_LEFT,
                        TaizhouEarlyStartLayout.PANEL_BG_TOP,
                        TaizhouEarlyStartLayout.PANEL_BG_LEFT
                                + TaizhouEarlyStartLayout.PANEL_BG_WIDTH,
                        TaizhouEarlyStartLayout.PANEL_BG_TOP
                                + TaizhouEarlyStartLayout.PANEL_BG_HEIGHT),
                33,
                33);
        // Image_30 / Image_30_1：common_title_bg.png 左正右镜像两半边（同族面板已验证画法）。
        drawNineSlice(
                canvas,
                titleBackground,
                new RectF(
                        TaizhouEarlyStartLayout.TITLE_BG_LEFT_LEFT,
                        TaizhouEarlyStartLayout.TITLE_BG_TOP,
                        TaizhouEarlyStartLayout.TITLE_BG_LEFT_RIGHT,
                        TaizhouEarlyStartLayout.TITLE_BG_TOP
                                + TaizhouEarlyStartLayout.TITLE_BG_HEIGHT),
                31,
                26);
        drawNineSlice(
                canvas,
                titleBackgroundMirrored,
                new RectF(
                        TaizhouEarlyStartLayout.TITLE_BG_RIGHT_LEFT,
                        TaizhouEarlyStartLayout.TITLE_BG_TOP,
                        TaizhouEarlyStartLayout.TITLE_BG_RIGHT_RIGHT,
                        TaizhouEarlyStartLayout.TITLE_BG_TOP
                                + TaizhouEarlyStartLayout.TITLE_BG_HEIGHT),
                267,
                26);
        drawNode(canvas, title, TaizhouEarlyStartLayout.TITLE);
        drawNode(canvas, flowerMirrored, TaizhouEarlyStartLayout.FLOWER_LEFT);
        drawNode(canvas, flower, TaizhouEarlyStartLayout.FLOWER_RIGHT);
        drawRequesterLine(canvas);
        // 推断: text_0 节点 707×45 未含字号与颜色，字号按节点高度取 40，颜色沿用同族弹层深棕。
        drawText(
                canvas,
                "是否同意更换为【2人/3人玩法】立即开局",
                TaizhouEarlyStartLayout.CONFIRM_TEXT.centerX(),
                TaizhouEarlyStartLayout.CONFIRM_TEXT.centerY() + 15.0f,
                40.0f,
                Color.rgb(143, 78, 24));
        drawNode(canvas, refuse, TaizhouEarlyStartLayout.BUTTON_REFUSE);
        drawNode(canvas, agree, TaizhouEarlyStartLayout.BUTTON_AGREE);
    }

    /** 申请人行：昵称右对齐 + 「申请立即开局」左对齐（_KW_TEXT_NICK_NAME / text 节点）。 */
    private void drawRequesterLine(Canvas canvas) {
        // 推断: 字号按 38/45 节点高度取 34，颜色沿用同族弹层深棕。
        float baseline = TaizhouEarlyStartLayout.REQUEST_LINE_CENTER_Y + 12.0f;
        textPaint.setTextSize(34.0f);
        textPaint.setColor(Color.rgb(143, 78, 24));
        textPaint.setTextAlign(android.graphics.Paint.Align.RIGHT);
        canvas.drawText(
                requesterName, TaizhouEarlyStartLayout.REQUEST_NAME_RIGHT, baseline, textPaint);
        textPaint.setTextAlign(android.graphics.Paint.Align.LEFT);
        canvas.drawText(
                "申请立即开局", TaizhouEarlyStartLayout.REQUEST_LABEL_LEFT, baseline, textPaint);
        textPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        float x = designX(event);
        float y = designY(event);
        if (TaizhouEarlyStartLayout.BUTTON_AGREE.contains(x, y)) {
            performClick();
            actions.onAgree();
            dismissAction.run();
        } else if (TaizhouEarlyStartLayout.BUTTON_REFUSE.contains(x, y)) {
            performClick();
            dismissAction.run();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void drawNode(Canvas canvas, Bitmap bitmap, TaizhouEarlyStartLayout.Node node) {
        drawBitmap(
                canvas,
                bitmap,
                new RectF(node.left(), node.top(), node.right(), node.bottom()));
    }

    /** 九宫格：角部不拉伸，与 {@code MembershipPaymentCanvasDrawing} 同一画法。 */
    private void drawNineSlice(
            Canvas canvas, Bitmap bitmap, RectF bounds, int sourceCapX, int sourceCapY) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int capX = Math.min(sourceCapX, width / 2);
        int capY = Math.min(sourceCapY, height / 2);
        int[] sourceX = {0, capX, width - capX, width};
        int[] sourceY = {0, capY, height - capY, height};
        float[] destinationX = {
            bounds.left, bounds.left + capX, bounds.right - capX, bounds.right
        };
        float[] destinationY = {
            bounds.top, bounds.top + capY, bounds.bottom - capY, bounds.bottom
        };
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(
                                sourceX[column],
                                sourceY[row],
                                sourceX[column + 1],
                                sourceY[row + 1]),
                        new RectF(
                                destinationX[column],
                                destinationY[row],
                                destinationX[column + 1],
                                destinationY[row + 1]),
                        bitmapPaint);
            }
        }
    }

    /** tz_early_start_title.png 来自 earlyStart 图集（旋转帧，抽取后转正）。 */
    private static Bitmap extractTitle(Context context) {
        Bitmap atlas =
                android.graphics.BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.taizhou_mahjong_early_start);
        int index =
                TaizhouMahjongTableAtlas.indexOf(
                        TaizhouMahjongTableAtlas.EARLY_START_NAMES,
                        "tz_early_start_title.png");
        if (index < 0) {
            throw new IllegalArgumentException("Missing original earlyStart title frame");
        }
        int[] frame = TaizhouMahjongTableAtlas.EARLY_START_FRAMES[index];
        int storedWidth = frame[4] == 0 ? frame[2] : frame[3];
        int storedHeight = frame[4] == 0 ? frame[3] : frame[2];
        Bitmap stored =
                Bitmap.createBitmap(atlas, frame[0], frame[1], storedWidth, storedHeight);
        if (frame[4] == 0) {
            return stored;
        }
        Bitmap upright =
                Bitmap.createBitmap(
                        stored, 0, 0, storedWidth, storedHeight, rotate(-90.0f), true);
        if (upright != stored) {
            stored.recycle();
        }
        return upright;
    }

    private static Bitmap mirror(Bitmap source) {
        if (source == null || source.isRecycled()) {
            return source;
        }
        Matrix flip = new Matrix();
        flip.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), flip, true);
    }

    private static Matrix rotate(float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return matrix;
    }
}
