package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import com.nanbeiyule.game.mahjong.TaizhouDismissLayout;
import com.nanbeiyule.game.mahjong.TaizhouDismissStatus;
import com.nanbeiyule.game.mahjong.round.TaizhouDismissState;
import java.util.List;
import java.util.Locale;

/**
 * {@code Common/CSB/GameBase/DismissLayer.csb} 解散投票层。
 *
 * <p>{@code Dismiss/View.luac:196-206} 同意/拒绝各发一次 {@code sendRespondDismiss}
 * 后立刻转成等待态（{@code changeToWaiteUI(true)}：收起两个按钮、显示时钟行、隐藏超时说明）；
 * {@code :71-76} 只有回放才显示关闭按钮，正常牌局隐藏，避免关掉后收不到投票结果。
 */
@SuppressLint("ViewConstructor")
final class TaizhouDismissView extends TaizhouToolView {
    interface Actions {
        void onRespond(boolean agree);
    }

    /** 一个玩家位的展示数据。 */
    record Seat(int seat, String nickname, Bitmap avatar) {}

    private final Actions actions;
    private final TaizhouDismissState state;
    private final List<Seat> seats;
    private final boolean playback;
    private final Bitmap panel;
    private final Bitmap titleBackground;
    private final Bitmap titleBackgroundMirrored;
    private final Bitmap title;
    private final Bitmap flower;
    private final Bitmap flowerMirrored;
    private final Bitmap agree;
    private final Bitmap refuse;
    private final Bitmap close;
    private final Bitmap clock;
    private final Bitmap defaultHead;
    private boolean waiting;
    private Runnable dismissAction = () -> {};

    TaizhouDismissView(
            Context context,
            TaizhouDismissState state,
            List<Seat> seats,
            boolean playback,
            boolean alreadyResponded,
            Actions actions) {
        super(context);
        this.state = state;
        this.seats = List.copyOf(seats);
        this.playback = playback;
        this.waiting = alreadyResponded;
        this.actions = actions;
        panel = bitmap(R.drawable.payment_cancel_panel);
        titleBackground = bitmap(R.drawable.payment_cancel_title_background);
        titleBackgroundMirrored = mirror(titleBackground);
        title = bitmap(R.drawable.taizhou_dismiss_title);
        flower = bitmap(R.drawable.membership_notice_flower);
        flowerMirrored = mirror(flower);
        agree = bitmap(R.drawable.taizhou_dismiss_agree);
        refuse = bitmap(R.drawable.taizhou_dismiss_refuse);
        close = bitmap(R.drawable.taizhou_dismiss_close);
        clock = bitmap(R.drawable.taizhou_dismiss_clock);
        defaultHead = bitmap(R.drawable.taizhou_dismiss_default_head);
        setContentDescription("申请解散");
    }

    void setDismissAction(Runnable action) {
        dismissAction = action == null ? () -> {} : action;
    }

    /** 服务端刷新投票或倒计时后重绘。 */
    void refresh() {
        postInvalidateOnAnimation();
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        // 推断: KW_PANEL_BG 的遮罩色解析工具读不到，沿用同族弹层已验证的压暗值。
        fillPaint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(
                0.0f, 0.0f, TaizhouDismissLayout.DESIGN_WIDTH,
                TaizhouDismissLayout.DESIGN_HEIGHT, fillPaint);
        TaizhouTreasureCanvas.drawNineSlice(
                canvas,
                panel,
                new RectF(
                        TaizhouDismissLayout.PANEL_BG_LEFT,
                        TaizhouDismissLayout.PANEL_BG_TOP,
                        TaizhouDismissLayout.PANEL_BG_LEFT + TaizhouDismissLayout.PANEL_BG_WIDTH,
                        TaizhouDismissLayout.PANEL_BG_TOP + TaizhouDismissLayout.PANEL_BG_HEIGHT),
                TaizhouDismissLayout.PANEL_BG_CAP,
                TaizhouDismissLayout.PANEL_BG_CAP,
                panel.getWidth() - 2 * TaizhouDismissLayout.PANEL_BG_CAP,
                panel.getHeight() - 2 * TaizhouDismissLayout.PANEL_BG_CAP,
                bitmapPaint);
        drawTitleBar(canvas);
        drawNode(canvas, title, TaizhouDismissLayout.TITLE);
        drawNode(canvas, flowerMirrored, TaizhouDismissLayout.FLOWER_LEFT);
        drawNode(canvas, flower, TaizhouDismissLayout.FLOWER_RIGHT);
        drawRequestLine(canvas);
        drawSeats(canvas);
        if (waiting) {
            drawClockLine(canvas);
        } else {
            drawOutTimeTip(canvas);
            drawNode(canvas, refuse, TaizhouDismissLayout.BUTTON_REFUSE);
            drawNode(canvas, agree, TaizhouDismissLayout.BUTTON_AGREE);
        }
        if (playback) {
            drawNode(canvas, close, TaizhouDismissLayout.BUTTON_CLOSE);
        }
    }

    private void drawTitleBar(Canvas canvas) {
        RectF left =
                new RectF(
                        TaizhouDismissLayout.TITLE_BG_LEFT_LEFT,
                        TaizhouDismissLayout.TITLE_BG_TOP,
                        TaizhouDismissLayout.TITLE_BG_LEFT_RIGHT,
                        TaizhouDismissLayout.TITLE_BG_TOP + TaizhouDismissLayout.TITLE_BG_HEIGHT);
        TaizhouTreasureCanvas.drawNineSlice(
                canvas, titleBackground, left,
                TaizhouDismissLayout.TITLE_BG_CAP_LEFT, TaizhouDismissLayout.TITLE_BG_CAP_Y,
                95, 29, bitmapPaint);
        RectF right =
                new RectF(
                        TaizhouDismissLayout.TITLE_BG_RIGHT_LEFT + 543.5f,
                        TaizhouDismissLayout.TITLE_BG_TOP,
                        TaizhouDismissLayout.TITLE_BG_RIGHT_RIGHT + 543.5f,
                        TaizhouDismissLayout.TITLE_BG_TOP + TaizhouDismissLayout.TITLE_BG_HEIGHT);
        TaizhouTreasureCanvas.drawNineSlice(
                canvas, titleBackgroundMirrored, right,
                TaizhouDismissLayout.TITLE_BG_CAP_RIGHT, TaizhouDismissLayout.TITLE_BG_CAP_Y,
                95, 29, bitmapPaint);
    }

    /** {@code _KW_TEXT_NICK_NAME} 右对齐 + {@code text} 左对齐，同一基线。 */
    private void drawRequestLine(Canvas canvas) {
        float baseline = TaizhouDismissLayout.REQUEST_LINE_CENTER_Y + 12.0f;
        textPaint.setTextSize(TaizhouDismissLayout.REQUEST_FONT_SIZE);
        textPaint.setColor(Color.rgb(143, 78, 24));
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(
                state.requestNickname(), TaizhouDismissLayout.REQUEST_NAME_RIGHT, baseline,
                textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(
                TaizhouDismissLayout.REQUEST_LABEL, TaizhouDismissLayout.REQUEST_LABEL_LEFT,
                baseline, textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /** {@code setColockTime}(:118-125)：「%d秒     等待结果......」。 */
    private void drawClockLine(Canvas canvas) {
        if (state.remainingSeconds() <= 0) {
            return;
        }
        drawNode(canvas, clock, TaizhouDismissLayout.CLOCK);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(TaizhouDismissLayout.CLOCK_TIP_FONT_SIZE);
        textPaint.setColor(Color.rgb(143, 78, 24));
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(
                String.format(Locale.CHINA, "%d秒     等待结果......", state.remainingSeconds()),
                TaizhouDismissLayout.CLOCK_TIP_LEFT,
                TaizhouDismissLayout.CLOCK_TIP_CENTER_Y
                        - (metrics.ascent + metrics.descent) * 0.5f,
                textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /** {@code setColockTime}：「(超过%d秒未做选择,则默认同意)」。 */
    private void drawOutTimeTip(Canvas canvas) {
        if (state.remainingSeconds() <= 0) {
            return;
        }
        drawText(
                canvas,
                String.format(
                        Locale.CHINA, "(超过%d秒未做选择,则默认同意)", state.remainingSeconds()),
                TaizhouDismissLayout.OUT_TIME_TIP.centerX(),
                TaizhouDismissLayout.OUT_TIME_TIP.centerY() + 14.0f,
                TaizhouDismissLayout.REQUEST_FONT_SIZE,
                Color.rgb(143, 78, 24));
    }

    private void drawSeats(Canvas canvas) {
        int count = seats.size();
        for (int index = 0; index < count; index++) {
            Seat seat = seats.get(index);
            float centerX = TaizhouDismissLayout.playerCenterX(index, count);
            float headCenterY =
                    TaizhouDismissLayout.PLAYER_CENTER_Y
                            - TaizhouDismissLayout.PLAYER_HEAD_OFFSET_Y;
            Bitmap avatar = seat.avatar() != null ? seat.avatar() : defaultHead;
            drawCentered(
                    canvas, avatar, centerX, headCenterY,
                    TaizhouDismissLayout.PLAYER_HEAD_SIZE,
                    TaizhouDismissLayout.PLAYER_HEAD_SIZE);
            drawText(
                    canvas,
                    seat.nickname(),
                    centerX,
                    TaizhouDismissLayout.PLAYER_CENTER_Y
                            - TaizhouDismissLayout.PLAYER_NAME_OFFSET_Y + 11.0f,
                    TaizhouDismissLayout.PLAYER_FONT_SIZE,
                    Color.rgb(143, 78, 24));
            TaizhouDismissStatus status = state.statusOf(seat.seat());
            drawText(
                    canvas,
                    TaizhouDismissLayout.statusLabel(status),
                    centerX,
                    TaizhouDismissLayout.PLAYER_CENTER_Y
                            - TaizhouDismissLayout.PLAYER_STATE_OFFSET_Y + 11.0f,
                    TaizhouDismissLayout.PLAYER_FONT_SIZE,
                    TaizhouDismissLayout.statusColor(status));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        float x = designX(event);
        float y = designY(event);
        if (playback && TaizhouDismissLayout.BUTTON_CLOSE.contains(x, y)) {
            performClick();
            actions.onRespond(false);
            dismissAction.run();
            return true;
        }
        if (waiting) {
            return true;
        }
        if (TaizhouDismissLayout.BUTTON_AGREE.contains(x, y)) {
            performClick();
            actions.onRespond(true);
            waiting = true;
            invalidate();
        } else if (TaizhouDismissLayout.BUTTON_REFUSE.contains(x, y)) {
            performClick();
            actions.onRespond(false);
            waiting = true;
            invalidate();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void drawNode(Canvas canvas, Bitmap bitmap, TaizhouDismissLayout.Node node) {
        drawBitmap(canvas, bitmap, new RectF(node.left(), node.top(), node.right(), node.bottom()));
    }

    private static Bitmap mirror(Bitmap source) {
        if (source == null || source.isRecycled()) {
            return source;
        }
        Matrix flip = new Matrix();
        flip.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), flip, true);
    }
}
