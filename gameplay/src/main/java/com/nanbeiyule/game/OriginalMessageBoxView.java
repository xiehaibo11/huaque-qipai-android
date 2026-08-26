package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

/** Canvas renderer for the original MessageBox.csb single-confirm state. */
final class OriginalMessageBoxView extends View {
    private final String message;
    private final Runnable dismiss;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Typeface typeface;
    private final Bitmap panel;
    private final Bitmap titleBackground;
    private final Bitmap mirroredTitleBackground;
    private final Bitmap title;
    private final Bitmap close;
    private final Bitmap confirm;
    private Runnable buttonClickSound = () -> {};

    OriginalMessageBoxView(Context context, String message, Runnable dismiss) {
        super(context);
        this.message = message;
        this.dismiss = dismiss;
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        panel = load(R.drawable.payment_cancel_panel);
        titleBackground = load(R.drawable.payment_cancel_title_background);
        mirroredTitleBackground = mirror(titleBackground);
        title = load(R.drawable.payment_cancel_title);
        close = load(R.drawable.payment_cancel_close);
        confirm = load(R.drawable.payment_cancel_confirm);
        setFocusable(true);
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        MembershipPaymentCanvasDrawing.Transform transform =
                MembershipPaymentCanvasDrawing.transform(getWidth(), getHeight());
        int save = MembershipPaymentCanvasDrawing.apply(canvas, transform);
        MembershipPaymentCanvasDrawing.drawNineSlice(
                canvas, panel, MembershipPaymentDialogLayout.CANCEL_PANEL, bitmapPaint, 33, 33, 33, 33);
        MembershipPaymentCanvasDrawing.drawNineSlice(
                canvas,
                titleBackground,
                MembershipPaymentDialogLayout.CANCEL_TITLE_LEFT,
                bitmapPaint,
                31,
                26,
                267,
                26);
        MembershipPaymentCanvasDrawing.drawNineSlice(
                canvas,
                mirroredTitleBackground,
                MembershipPaymentDialogLayout.CANCEL_TITLE_RIGHT,
                bitmapPaint,
                267,
                26,
                31,
                26);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas, title, MembershipPaymentDialogLayout.CANCEL_TITLE, bitmapPaint);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas, close, MembershipPaymentDialogLayout.CANCEL_CLOSE, bitmapPaint);
        MembershipPaymentCanvasDrawing.drawCenteredText(
                canvas,
                textPaint,
                typeface,
                message,
                MembershipPaymentDialogLayout.CANCEL_MESSAGE_CENTER_X,
                MembershipPaymentDialogLayout.CANCEL_MESSAGE_CENTER_Y,
                36,
                Color.rgb(205, 133, 81),
                255);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas, confirm, MembershipPaymentDialogLayout.CANCEL_CONFIRM, bitmapPaint);
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) return true;
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return event.getActionMasked() == MotionEvent.ACTION_MOVE;
        }
        MembershipPaymentCanvasDrawing.Transform transform =
                MembershipPaymentCanvasDrawing.transform(getWidth(), getHeight());
        MembershipPaymentDialogLayout.CancelAction action =
                MembershipPaymentDialogLayout.cancelActionAt(
                        transform.designX(event.getX()), transform.designY(event.getY()));
        if (action == MembershipPaymentDialogLayout.CancelAction.CLOSE
                || action == MembershipPaymentDialogLayout.CancelAction.CONFIRM) {
            buttonClickSound.run();
            dismiss.run();
        } else if (action == MembershipPaymentDialogLayout.CancelAction.DISMISS) {
            dismiss.run();
        }
        return true;
    }

    private Bitmap load(int id) { return BitmapFactory.decodeResource(getResources(), id); }

    private static Bitmap mirror(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
