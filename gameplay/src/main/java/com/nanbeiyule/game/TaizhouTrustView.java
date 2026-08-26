package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.MotionEvent;
import com.nanbeiyule.game.cocosarmature.ArmatureAtlas;
import com.nanbeiyule.game.cocosarmature.ArmatureData;
import com.nanbeiyule.game.cocosarmature.ArmatureExportJson;
import com.nanbeiyule.game.cocosarmature.ArmaturePlayer;
import com.nanbeiyule.game.mahjong.TaizhouTrustLayout;

/**
 * {@code Common/CSB/GameBase/TrustLayer.csb} 托管全屏层。
 *
 * <p>{@code Trust/View.luac:44-46 onTouchEventRobotPanel} 点击 {@code _KW_PANEL_ROBOT}
 * 即 {@code doSendTrust(false)} 并关闭；{@code _KW_PANEL_ROBOT} 覆盖整屏，所以点任意处都取消。
 * {@code _KW_TRUST_TIP_BG} 与三段惩罚文案只在惩罚秒数大于 0 时显示（:62-69）。
 */
@SuppressLint("ViewConstructor")
final class TaizhouTrustView extends TaizhouToolView {
    interface Actions {
        void onCancelTrust();
    }

    private final Actions actions;
    private final int punishSeconds;
    private final long startedAt = SystemClock.elapsedRealtime();
    private final Bitmap tipBackground;
    private final ArmaturePlayer robot;
    private Runnable dismissAction = () -> {};

    TaizhouTrustView(Context context, int punishSeconds, Actions actions) {
        super(context);
        this.actions = actions;
        this.punishSeconds = Math.max(0, punishSeconds);
        tipBackground =
                this.punishSeconds > 0 ? bitmap(R.drawable.taizhou_trust_tip_bg) : null;
        robot = loadRobot(context);
        setContentDescription("托管中");
    }

    private static ArmaturePlayer loadRobot(Context context) {
        try {
            ArmatureData data =
                    ArmatureExportJson.load(
                            context.getAssets(), TaizhouTrustLayout.ANIMATION_EXPORT_JSON);
            ArmatureAtlas atlas =
                    ArmatureAtlas.load(
                            context.getAssets(),
                            TaizhouTrustLayout.ANIMATION_TEXTURE,
                            TaizhouTrustLayout.ANIMATION_FRAMES);
            return new ArmaturePlayer(
                    data, atlas, data.movement(TaizhouTrustLayout.ANIMATION_MOVEMENT));
        } catch (RuntimeException ignored) {
            // 缺资源时保留遮罩与取消手势，不让托管层本身失效。
            return null;
        }
    }

    void setDismissAction(Runnable action) {
        dismissAction = action == null ? () -> {} : action;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        fillPaint.setColor(Color.argb(TaizhouTrustLayout.OVERLAY_ALPHA, 0, 0, 0));
        canvas.drawRect(
                0.0f,
                0.0f,
                TaizhouTrustLayout.DESIGN_WIDTH,
                TaizhouTrustLayout.DESIGN_HEIGHT,
                fillPaint);
        if (robot != null) {
            float elapsed = (SystemClock.elapsedRealtime() - startedAt) / 1000.0f;
            robot.draw(
                    canvas,
                    elapsed,
                    TaizhouTrustLayout.ANIMATION_ORIGIN_X,
                    TaizhouTrustLayout.ANIMATION_ORIGIN_Y,
                    1.0f);
            postInvalidateOnAnimation();
        }
        if (tipBackground != null) {
            drawTip(canvas);
        }
    }

    private void drawTip(Canvas canvas) {
        TaizhouTrustLayout.Node background = TaizhouTrustLayout.TIP_BACKGROUND;
        TaizhouTreasureCanvas.drawNineSlice(
                canvas,
                tipBackground,
                new RectF(
                        background.left(),
                        background.top(),
                        background.right(),
                        background.bottom()),
                TaizhouTrustLayout.TIP_CAP_X,
                TaizhouTrustLayout.TIP_CAP_Y,
                TaizhouTrustLayout.TIP_CAP_WIDTH,
                TaizhouTrustLayout.TIP_CAP_HEIGHT,
                bitmapPaint);
        int elapsedSeconds = (int) ((SystemClock.elapsedRealtime() - startedAt) / 1000L);
        centeredText(
                canvas,
                String.format(java.util.Locale.CHINA, "已托管 %d 秒", elapsedSeconds),
                TaizhouTrustLayout.TIME_TEXT,
                TaizhouTrustLayout.TIME_TEXT_FONT_SIZE);
        centeredText(
                canvas,
                "托管超过",
                TaizhouTrustLayout.PUNISH_PREFIX_TEXT,
                TaizhouTrustLayout.PUNISH_FONT_SIZE);
        centeredText(
                canvas,
                String.valueOf(punishSeconds),
                TaizhouTrustLayout.PUNISH_SECONDS_TEXT,
                TaizhouTrustLayout.PUNISH_FONT_SIZE);
        centeredText(
                canvas,
                "秒，将触发托管惩罚",
                TaizhouTrustLayout.PUNISH_SUFFIX_TEXT,
                TaizhouTrustLayout.PUNISH_FONT_SIZE);
    }

    private void centeredText(
            Canvas canvas, String value, TaizhouTrustLayout.Node node, float size) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(size);
        textPaint.setColor(TaizhouTrustLayout.TEXT_COLOR);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(
                value,
                node.centerX(),
                node.centerY() - (metrics.ascent + metrics.descent) * 0.5f,
                textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        performClick();
        actions.onCancelTrust();
        dismissAction.run();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (robot != null) {
            robot.recycle();
        }
        TaizhouTreasureCanvas.recycle(tipBackground);
    }
}
