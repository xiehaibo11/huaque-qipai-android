package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

/** {@code TIP_LAYER_TYPE.OK} 的单确定按钮提示层（{@code share_tips.csb}）。 */
@SuppressLint("ViewConstructor")
final class TaizhouTipView extends TaizhouToolView {
    private final String message;
    private final Runnable confirmed;
    private final Bitmap background;
    private final Bitmap titlePlate;
    private final Bitmap title;
    private final Bitmap confirm;
    private final Bitmap close;
    private Runnable dismissAction = () -> {};

    TaizhouTipView(Context context, String message, Runnable confirmed) {
        super(context);
        this.message = message;
        this.confirmed = confirmed;
        background = bitmap(R.drawable.taizhou_tool_tip_bg);
        titlePlate = bitmap(R.drawable.taizhou_tool_tip_title_bg);
        title = bitmap(R.drawable.taizhou_tool_tip_title);
        confirm = bitmap(R.drawable.taizhou_tool_tip_confirm);
        close = bitmap(R.drawable.taizhou_tool_tip_close);
        setContentDescription(message);
    }

    void setDismissAction(Runnable action) {
        dismissAction = action == null ? () -> {} : action;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        fillPaint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(0.0f, 0.0f, 1920.0f, 1080.0f, fillPaint);
        drawBitmap(canvas, background,
                new RectF(TaizhouTipLayout.PANEL_LEFT, TaizhouTipLayout.PANEL_TOP,
                        TaizhouTipLayout.PANEL_RIGHT, TaizhouTipLayout.PANEL_BOTTOM));
        drawCentered(canvas, titlePlate,
                TaizhouTipLayout.TITLE_CENTER_X, TaizhouTipLayout.TITLE_CENTER_Y,
                TaizhouTipLayout.TITLE_PLATE_WIDTH, TaizhouTipLayout.TITLE_PLATE_HEIGHT);
        drawCentered(canvas, title,
                TaizhouTipLayout.TITLE_CENTER_X, TaizhouTipLayout.TITLE_CENTER_Y,
                TaizhouTipLayout.TITLE_WIDTH, TaizhouTipLayout.TITLE_HEIGHT);
        drawLabel(
                canvas,
                message,
                new RectF(
                        TaizhouTipLayout.MESSAGE_CENTER_X - TaizhouTipLayout.MESSAGE_WIDTH / 2.0f,
                        TaizhouTipLayout.MESSAGE_CENTER_Y - 75.0f,
                        TaizhouTipLayout.MESSAGE_CENTER_X + TaizhouTipLayout.MESSAGE_WIDTH / 2.0f,
                        TaizhouTipLayout.MESSAGE_CENTER_Y + 75.0f),
                TaizhouTipLayout.MESSAGE_SIZE,
                TaizhouTipLayout.MESSAGE_COLOR,
                Paint.Align.CENTER);
        drawCentered(canvas, confirm,
                TaizhouTipLayout.OK_CENTER_X, TaizhouTipLayout.OK_CENTER_Y,
                TaizhouTipLayout.OK_WIDTH, TaizhouTipLayout.OK_HEIGHT);
        drawCentered(canvas, close,
                TaizhouTipLayout.CLOSE_CENTER_X, TaizhouTipLayout.CLOSE_CENTER_Y,
                TaizhouTipLayout.CLOSE_SIZE_WIDTH, TaizhouTipLayout.CLOSE_SIZE_HEIGHT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        float x = designX(event);
        float y = designY(event);
        if (TaizhouTipLayout.okContains(x, y)) {
            performClick();
            dismissAction.run();
            confirmed.run();
            return true;
        }
        if (TaizhouTipLayout.closeContains(x, y)) {
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
}
