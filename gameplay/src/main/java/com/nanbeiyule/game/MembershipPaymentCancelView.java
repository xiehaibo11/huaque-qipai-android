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

/** Canvas restoration of MessageBox.csb in its single-confirm payment-cancel state. */
final class MembershipPaymentCancelView extends View {
    private static final float MESSAGE_TEXT_SIZE = 36.0f;
    private static final int MESSAGE_TEXT_COLOR = Color.rgb(205, 133, 81);

    private final Runnable dismissAction;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Typeface originalTypeface;
    private final Bitmap panelBitmap;
    private final Bitmap titleBackgroundBitmap;
    private final Bitmap mirroredTitleBackgroundBitmap;
    private final Bitmap titleBitmap;
    private final Bitmap closeBitmap;
    private final Bitmap confirmBitmap;
    private Runnable buttonClickSound = () -> {};

    MembershipPaymentCancelView(Context context, Runnable dismissAction) {
        super(context);
        this.dismissAction = dismissAction == null ? () -> {} : dismissAction;
        originalTypeface =
                Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        panelBitmap = load(R.drawable.payment_cancel_panel);
        titleBackgroundBitmap = load(R.drawable.payment_cancel_title_background);
        mirroredTitleBackgroundBitmap = mirror(titleBackgroundBitmap);
        titleBitmap = load(R.drawable.payment_cancel_title);
        closeBitmap = load(R.drawable.payment_cancel_close);
        confirmBitmap = load(R.drawable.payment_cancel_confirm);
        setFocusable(true);
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        this.buttonClickSound = buttonClickSound == null ? () -> {} : buttonClickSound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        MembershipPaymentCanvasDrawing.Transform transform =
                MembershipPaymentCanvasDrawing.transform(getWidth(), getHeight());
        int saveCount = MembershipPaymentCanvasDrawing.apply(canvas, transform);
        MembershipPaymentCanvasDrawing.drawNineSlice(
                canvas,
                panelBitmap,
                MembershipPaymentDialogLayout.CANCEL_PANEL,
                bitmapPaint,
                33, 33, 33, 33);
        MembershipPaymentCanvasDrawing.drawNineSlice(
                canvas,
                titleBackgroundBitmap,
                MembershipPaymentDialogLayout.CANCEL_TITLE_LEFT,
                bitmapPaint,
                31, 26, 267, 26);
        MembershipPaymentCanvasDrawing.drawNineSlice(
                canvas,
                mirroredTitleBackgroundBitmap,
                MembershipPaymentDialogLayout.CANCEL_TITLE_RIGHT,
                bitmapPaint,
                267, 26, 31, 26);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                titleBitmap,
                MembershipPaymentDialogLayout.CANCEL_TITLE,
                bitmapPaint);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                closeBitmap,
                MembershipPaymentDialogLayout.CANCEL_CLOSE,
                bitmapPaint);
        MembershipPaymentCanvasDrawing.drawCenteredText(
                canvas,
                textPaint,
                originalTypeface,
                "支付取消",
                MembershipPaymentDialogLayout.CANCEL_MESSAGE_CENTER_X,
                MembershipPaymentDialogLayout.CANCEL_MESSAGE_CENTER_Y,
                MESSAGE_TEXT_SIZE,
                MESSAGE_TEXT_COLOR,
                255);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                confirmBitmap,
                MembershipPaymentDialogLayout.CANCEL_CONFIRM,
                bitmapPaint);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return event.getActionMasked() == MotionEvent.ACTION_MOVE;
        }
        performClick();
        MembershipPaymentCanvasDrawing.Transform transform =
                MembershipPaymentCanvasDrawing.transform(getWidth(), getHeight());
        MembershipPaymentDialogLayout.CancelAction action =
                MembershipPaymentDialogLayout.cancelActionAt(
                        transform.designX(event.getX()),
                        transform.designY(event.getY()));
        switch (action) {
            case CLOSE, CONFIRM -> {
                buttonClickSound.run();
                dismissAction.run();
            }
            case DISMISS -> dismissAction.run();
            case NONE -> {
                // Keep the prompt visible when the body itself is tapped.
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private Bitmap load(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    private static Bitmap mirror(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.setScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(
                source,
                0,
                0,
                source.getWidth(),
                source.getHeight(),
                matrix,
                true);
    }
}
