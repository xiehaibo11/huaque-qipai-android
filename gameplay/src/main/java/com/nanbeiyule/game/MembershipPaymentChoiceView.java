package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

/** Canvas restoration of the original PayType.csb with Alipay as the sole visible choice. */
final class MembershipPaymentChoiceView extends View {
    interface Actions {
        void onConfirm();

        void onDismiss();
    }

    private static final float TITLE_TEXT_SIZE = 64.0f;
    private static final float PROMPT_TEXT_SIZE = 40.0f;
    private static final float PROVIDER_TEXT_SIZE = 40.0f;
    private static final float RECOMMEND_TEXT_SIZE = 34.0f;
    private static final float CONFIRM_TEXT_SIZE = 56.0f;
    private static final int PRIMARY_TEXT_COLOR = Color.rgb(71, 78, 105);
    private static final int CONFIRM_TEXT_COLOR = Color.rgb(206, 92, 4);

    private final MembershipPurchaseSelection selection;
    private final Actions actions;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Typeface originalTypeface;
    private final Bitmap panelBitmap;
    private final Bitmap alipayBitmap;
    private final Bitmap selectedBitmap;
    private final Bitmap recommendBitmap;
    private final Bitmap confirmBitmap;
    private final Bitmap closeBitmap;
    private Runnable buttonClickSound = () -> {};

    MembershipPaymentChoiceView(
            Context context,
            MembershipPurchaseSelection selection,
            Actions actions) {
        super(context);
        this.selection = selection;
        this.actions = actions;
        originalTypeface =
                Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        panelBitmap = load(R.drawable.payment_choice_panel);
        alipayBitmap = load(R.drawable.payment_choice_alipay);
        selectedBitmap = load(R.drawable.payment_choice_selected);
        recommendBitmap = load(R.drawable.payment_choice_recommend_background);
        confirmBitmap = load(R.drawable.payment_choice_confirm);
        closeBitmap = load(R.drawable.payment_choice_close);
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
        drawOriginalContent(canvas);
        canvas.restoreToCount(saveCount);
    }

    private void drawOriginalContent(Canvas canvas) {
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                panelBitmap,
                MembershipPaymentDialogLayout.PAYMENT_PANEL,
                bitmapPaint);
        drawText(
                canvas,
                "支付",
                MembershipPaymentDialogLayout.PAYMENT_TITLE_CENTER_X,
                MembershipPaymentDialogLayout.PAYMENT_TITLE_CENTER_Y,
                TITLE_TEXT_SIZE,
                PRIMARY_TEXT_COLOR,
                255);
        drawText(
                canvas,
                selection.prompt(),
                MembershipPaymentDialogLayout.PAYMENT_PROMPT_CENTER_X,
                MembershipPaymentDialogLayout.PAYMENT_PROMPT_CENTER_Y,
                PROMPT_TEXT_SIZE,
                PRIMARY_TEXT_COLOR,
                178);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                alipayBitmap,
                MembershipPaymentDialogLayout.ALIPAY_ICON,
                bitmapPaint);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                selectedBitmap,
                MembershipPaymentDialogLayout.ALIPAY_SELECTED,
                bitmapPaint);
        drawText(
                canvas,
                "支付宝",
                MembershipPaymentDialogLayout.ALIPAY_LABEL_CENTER_X,
                MembershipPaymentDialogLayout.ALIPAY_LABEL_CENTER_Y,
                PROVIDER_TEXT_SIZE,
                PRIMARY_TEXT_COLOR,
                255);
        MembershipPaymentCanvasDrawing.drawNineSlice(
                canvas,
                recommendBitmap,
                MembershipPaymentDialogLayout.PAYMENT_RECOMMEND,
                bitmapPaint,
                17, 17, 17, 17);
        drawText(
                canvas,
                "推荐使用",
                MembershipPaymentDialogLayout.RECOMMEND_CENTER_X,
                MembershipPaymentDialogLayout.RECOMMEND_CENTER_Y,
                RECOMMEND_TEXT_SIZE,
                Color.WHITE,
                255);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                confirmBitmap,
                MembershipPaymentDialogLayout.PAYMENT_CONFIRM,
                bitmapPaint);
        drawText(
                canvas,
                "确定",
                MembershipPaymentDialogLayout.PAYMENT_CONFIRM_CENTER_X,
                MembershipPaymentDialogLayout.PAYMENT_CONFIRM_CENTER_Y,
                CONFIRM_TEXT_SIZE,
                CONFIRM_TEXT_COLOR,
                255);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                closeBitmap,
                MembershipPaymentDialogLayout.PAYMENT_CLOSE,
                bitmapPaint);
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
        MembershipPaymentDialogLayout.PaymentAction action =
                MembershipPaymentDialogLayout.paymentActionAt(
                        transform.designX(event.getX()),
                        transform.designY(event.getY()));
        switch (action) {
            case CONFIRM -> {
                buttonClickSound.run();
                actions.onConfirm();
            }
            case CLOSE -> {
                buttonClickSound.run();
                actions.onDismiss();
            }
            case DISMISS -> actions.onDismiss();
            case NONE -> {
                // The unpainted reserved provider slot intentionally has no action.
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void drawText(
            Canvas canvas,
            String text,
            float centerX,
            float centerY,
            float size,
            int color,
            int alpha) {
        MembershipPaymentCanvasDrawing.drawCenteredText(
                canvas,
                textPaint,
                originalTypeface,
                text,
                centerX,
                centerY,
                size,
                color,
                alpha);
    }

    private Bitmap load(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }
}
