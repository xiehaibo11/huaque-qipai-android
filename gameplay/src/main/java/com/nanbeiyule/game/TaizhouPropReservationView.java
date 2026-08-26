package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;

/** Canvas reconstruction of shuffle_tips_ok_cancel_new.csb. */
@SuppressLint("ViewConstructor")
final class TaizhouPropReservationView extends TaizhouToolView {
    private final TaizhouRoomToolsState.Tool tool;
    private final boolean reserved;
    private final TaizhouPropReservationDialog.Actions actions;
    private final Bitmap background;
    private final Bitmap confirm;
    private final Bitmap cancel;
    private final Bitmap close;
    private final Bitmap icon;
    private Runnable dismissAction = () -> {};

    TaizhouPropReservationView(
            Context context,
            TaizhouRoomToolsState.Tool tool,
            boolean reserved,
            TaizhouPropReservationDialog.Actions actions) {
        super(context);
        this.tool = tool;
        this.reserved = reserved;
        this.actions = actions;
        background = bitmap(R.drawable.taizhou_tool_tip_bg);
        confirm = bitmap(R.drawable.taizhou_tool_tip_confirm);
        cancel = bitmap(R.drawable.taizhou_tool_tip_cancel);
        close = bitmap(R.drawable.taizhou_tool_tip_close);
        icon =
                bitmap(
                        tool.type() == TaizhouRoomToolType.CHANGE_CARD
                                ? R.drawable.taizhou_mahjong_change_card
                                : R.drawable.taizhou_mahjong_shuffle);
        setContentDescription(tool.displayName() + "预约");
    }

    void setDismissAction(Runnable action) {
        dismissAction = action == null ? () -> {} : action;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        fillPaint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(0.0f, 0.0f, 1920.0f, 1080.0f, fillPaint);
        drawBitmap(canvas, background, new RectF(566.0f, 285.5f, 1354.0f, 794.5f));
        drawCentered(canvas, close, 1334.5f, 316.5f, 99.0f, 102.0f);
        drawCentered(canvas, icon, 960.0f, 430.0f, 128.0f, 118.0f);
        drawText(canvas, reserved ? "取消下局预约" : "预约下局" + tool.displayName(),
                960.0f, 520.0f, 48.0f, Color.rgb(143, 78, 24));
        String price = tool.priceAmount() <= 0
                ? "本次仅预约，下一局开始前不扣费"
                : "预计消耗 " + tool.priceAmount() + currencyName(tool.priceCurrency())
                        + "，下一局开始时结算";
        drawText(canvas, price, 960.0f, 585.0f, 31.0f, Color.rgb(130, 88, 49));
        drawCentered(canvas, cancel, 736.0f, 705.0f, 301.0f, 131.0f);
        drawCentered(canvas, confirm, 1186.0f, 705.0f, 301.0f, 131.0f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        float x = designX(event);
        float y = designY(event);
        if (TaizhouWaitingToolLayout.RESERVATION_CONFIRM.contains(x, y)) {
            performClick();
            actions.onReservationChanged(!reserved);
            dismissAction.run();
        } else if (TaizhouWaitingToolLayout.RESERVATION_CANCEL.contains(x, y)
                || x >= 1280.0f && y <= 380.0f) {
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

    private static String currencyName(String value) {
        if ("ROOM_CARD".equals(value)) {
            return "房卡";
        }
        if ("DIAMOND".equals(value)) {
            return "钻石";
        }
        return "";
    }
}
