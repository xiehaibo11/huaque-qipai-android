package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;

/** Canvas restoration of the original Zhejiang VipNoticeLayer.csb. */
final class MembershipNoticeView extends View {
    private static final float NOTICE_TEXT_SIZE = 34f;
    private static final int NOTICE_TEXT_COLOR = Color.rgb(205, 133, 81);

    private final Runnable dismissAction;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final TextPaint textPaint =
            new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Typeface originalTypeface;
    private final Bitmap panelBitmap;
    private final Bitmap titleBackgroundBitmap;
    private final Bitmap mirroredTitleBackgroundBitmap;
    private final Bitmap titleBitmap;
    private final Bitmap closeBitmap;
    private final Bitmap flowerBitmap;
    private final Bitmap mirroredFlowerBitmap;
    private final Bitmap noticeTitleBitmap;
    private MembershipNotice notice = MembershipNotice.originalFallback();
    private Runnable buttonClickSound = () -> {};

    MembershipNoticeView(Context context, Runnable dismissAction) {
        super(context);
        this.dismissAction = dismissAction == null ? () -> {} : dismissAction;
        originalTypeface =
                Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        panelBitmap = load(R.drawable.payment_cancel_panel);
        titleBackgroundBitmap = load(R.drawable.payment_cancel_title_background);
        mirroredTitleBackgroundBitmap = mirror(titleBackgroundBitmap);
        titleBitmap = load(R.drawable.payment_cancel_title);
        closeBitmap = load(R.drawable.payment_cancel_close);
        flowerBitmap = load(R.drawable.membership_notice_flower);
        mirroredFlowerBitmap = mirror(flowerBitmap);
        noticeTitleBitmap = load(R.drawable.membership_notice_link);
        textPaint.setTypeface(originalTypeface);
        textPaint.setTextSize(NOTICE_TEXT_SIZE);
        textPaint.setColor(NOTICE_TEXT_COLOR);
        setFocusable(true);
    }

    void setNotice(MembershipNotice notice) {
        this.notice = notice == null ? MembershipNotice.originalFallback() : notice;
        invalidate();
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
                MembershipNoticeDialogLayout.PANEL,
                bitmapPaint,
                33,
                33,
                33,
                33);
        MembershipPaymentCanvasDrawing.drawNineSlice(
                canvas,
                titleBackgroundBitmap,
                MembershipNoticeDialogLayout.TITLE_LEFT,
                bitmapPaint,
                31,
                26,
                267,
                26);
        MembershipPaymentCanvasDrawing.drawNineSlice(
                canvas,
                mirroredTitleBackgroundBitmap,
                MembershipNoticeDialogLayout.TITLE_RIGHT,
                bitmapPaint,
                267,
                26,
                31,
                26);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas, titleBitmap, MembershipNoticeDialogLayout.TITLE, bitmapPaint);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas, closeBitmap, MembershipNoticeDialogLayout.CLOSE, bitmapPaint);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                mirroredFlowerBitmap,
                MembershipNoticeDialogLayout.FLOWER_LEFT,
                bitmapPaint);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                flowerBitmap,
                MembershipNoticeDialogLayout.FLOWER_RIGHT,
                bitmapPaint);
        MembershipPaymentCanvasDrawing.drawBitmap(
                canvas,
                noticeTitleBitmap,
                MembershipNoticeDialogLayout.NOTICE_TITLE,
                bitmapPaint);
        drawNoticeText(canvas);
        canvas.restoreToCount(saveCount);
    }

    private void drawNoticeText(Canvas canvas) {
        List<String> fallback = MembershipNotice.originalFallback().items();
        List<String> items = notice.items();
        drawWrappedText(canvas, item(items, fallback, 0), MembershipNoticeDialogLayout.ITEM_ONE);
        drawWrappedText(canvas, item(items, fallback, 1), MembershipNoticeDialogLayout.ITEM_TWO);
        drawSingleLineText(
                canvas, item(items, fallback, 2), MembershipNoticeDialogLayout.ITEM_THREE);
        drawSingleLineText(
                canvas, item(items, fallback, 3), MembershipNoticeDialogLayout.ITEM_FOUR);
        String changeNotice = notice.changeNotice();
        if (changeNotice == null || changeNotice.isBlank()) {
            changeNotice = MembershipNotice.originalFallback().changeNotice();
        }
        drawWrappedText(
                canvas, "5. " + changeNotice, MembershipNoticeDialogLayout.CHANGE_NOTICE);
    }

    private void drawSingleLineText(
            Canvas canvas,
            String value,
            MembershipPaymentDialogLayout.DesignRect bounds) {
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = bounds.top() + bounds.height() * 0.5f
                - (metrics.ascent + metrics.descent) * 0.5f;
        canvas.drawText(value, bounds.left(), baseline, textPaint);
    }

    private void drawWrappedText(
            Canvas canvas,
            String value,
            MembershipPaymentDialogLayout.DesignRect bounds) {
        StaticLayout layout =
                new StaticLayout(
                        value,
                        textPaint,
                        Math.max(1, Math.round(bounds.width())),
                        Layout.Alignment.ALIGN_NORMAL,
                        1f,
                        0f,
                        false);
        canvas.save();
        float top = bounds.top() + Math.max(0f, (bounds.height() - layout.getHeight()) * 0.5f);
        canvas.translate(bounds.left(), top);
        layout.draw(canvas);
        canvas.restore();
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
        MembershipNoticeDialogLayout.Action action =
                MembershipNoticeDialogLayout.actionAt(
                        transform.designX(event.getX()), transform.designY(event.getY()));
        switch (action) {
            case CLOSE -> {
                buttonClickSound.run();
                dismissAction.run();
            }
            case DISMISS -> dismissAction.run();
            case NONE -> {
                // The original panel body consumes the touch and remains visible.
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private static String item(List<String> items, List<String> fallback, int index) {
        if (items != null && index < items.size() && !items.get(index).isBlank()) {
            return items.get(index);
        }
        return fallback.get(index);
    }

    private Bitmap load(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    private static Bitmap mirror(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.setScale(-1f, 1f);
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
