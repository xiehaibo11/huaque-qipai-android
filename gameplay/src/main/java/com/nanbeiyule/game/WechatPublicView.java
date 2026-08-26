package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;

/** Native recovered WeChat-public-account surface using the original Taizhou QR code. */
@SuppressLint("ViewConstructor")
final class WechatPublicView extends AdaptiveCanvasView {
    interface Actions {
        void onDismissRequested();

        void onCopyRequested();

        void onOpenWechatRequested();
    }

    private enum Target {
        NONE,
        OUTSIDE,
        CLOSE,
        COPY,
        OPEN
    }

    private final WechatPublicModel model;
    private final RecoveredCommonDialogChrome chrome;
    private final Bitmap qrCode;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final StaticLayout noticeLayout;
    private Actions actions;
    private Runnable buttonClickSound = () -> {};
    private Target pressed = Target.NONE;

    WechatPublicView(Context context, WechatPublicModel model) {
        super(context);
        this.model = model;
        chrome = new RecoveredCommonDialogChrome(context);
        qrCode =
                BitmapFactory.decodeResource(
                        getResources(), R.drawable.wechat_public_code_900023);
        Typeface typeface =
                Typeface.createFromAsset(
                        context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        textPaint.setTypeface(typeface);
        textPaint.setColor(0xFFB16B42);
        pressedPaint.setColor(0x44000000);
        buttonPaint.setShader(
                new LinearGradient(
                        0f,
                        WechatPublicLayout.OPEN.top,
                        0f,
                        WechatPublicLayout.OPEN.bottom,
                        0xFFFFD76C,
                        0xFFEBA536,
                        Shader.TileMode.CLAMP));

        TextPaint noticePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        noticePaint.setTypeface(typeface);
        noticePaint.setTextSize(32f);
        noticePaint.setColor(0xFFB16B42);
        noticeLayout =
                StaticLayout.Builder.obtain(
                                model.notice(), 0, model.notice().length(), noticePaint, 840)
                        .setAlignment(Layout.Alignment.ALIGN_CENTER)
                        .setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE)
                        .setIncludePad(false)
                        .setLineSpacing(5f, 1f)
                        .build();
        setClickable(true);
        setFocusable(true);
        setContentDescription(
                model.notice() + "。微信搜索公众号：" + model.publicName() + "。点击前往微信");
    }

    void setActions(Actions actions) {
        this.actions = actions;
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(177, 0, 0, 0));
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        AdaptiveViewport.Transform transform =
                WechatPublicLayout.panelTransform(
                        adaptiveViewport(
                                WechatPublicLayout.DESIGN_WIDTH,
                                WechatPublicLayout.DESIGN_HEIGHT));
        int save = AdaptiveCanvasDrawing.apply(canvas, transform);
        drawPanel(canvas);
        canvas.restoreToCount(save);
    }

    private void drawPanel(Canvas canvas) {
        chrome.draw(
                canvas,
                WechatPublicLayout.PANEL_WIDTH,
                WechatPublicLayout.PANEL_HEIGHT,
                "提示",
                WechatPublicLayout.CLOSE);

        int save = canvas.save();
        canvas.translate(WechatPublicLayout.NOTICE.left, WechatPublicLayout.NOTICE.top);
        noticeLayout.draw(canvas);
        canvas.restoreToCount(save);

        canvas.drawBitmap(qrCode, null, WechatPublicLayout.QR, bitmapPaint);
        drawText(canvas, "微信搜索公众号：", 424f, 289f, 34f, Paint.Align.LEFT);
        drawText(canvas, model.displayName(), 414f, 350f, 38f, Paint.Align.LEFT);
        drawText(canvas, "点击二维码或名称可复制", 414f, 402f, 24f, Paint.Align.LEFT);

        canvas.drawRoundRect(WechatPublicLayout.OPEN, 22f, 22f, buttonPaint);
        drawText(
                canvas,
                "点击前往",
                WechatPublicLayout.OPEN.centerX(),
                WechatPublicLayout.OPEN.centerY(),
                42f,
                Paint.Align.CENTER);
        if (pressed == Target.OPEN) {
            canvas.drawRoundRect(WechatPublicLayout.OPEN, 22f, 22f, pressedPaint);
        } else if (pressed == Target.COPY) {
            canvas.drawRoundRect(WechatPublicLayout.COPY, 16f, 16f, pressedPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        AdaptiveViewport.Transform transform =
                WechatPublicLayout.panelTransform(
                        adaptiveViewport(
                                WechatPublicLayout.DESIGN_WIDTH,
                                WechatPublicLayout.DESIGN_HEIGHT));
        float x = transform.unmapX(event.getX());
        float y = transform.unmapY(event.getY());
        Target target = targetAt(x, y);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                pressed = target;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (pressed != target) {
                    pressed = Target.NONE;
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                pressed = Target.NONE;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                Target selected = pressed == target ? target : Target.NONE;
                pressed = Target.NONE;
                invalidate();
                dispatch(selected);
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        buttonClickSound.run();
        return true;
    }

    private void dispatch(Target target) {
        if (actions == null || target == Target.NONE) {
            return;
        }
        performClick();
        switch (target) {
            case OUTSIDE, CLOSE -> actions.onDismissRequested();
            case COPY -> actions.onCopyRequested();
            case OPEN -> actions.onOpenWechatRequested();
            case NONE -> { }
        }
    }

    private static Target targetAt(float x, float y) {
        if (!WechatPublicLayout.panelContains(x, y)) {
            return Target.OUTSIDE;
        }
        if (WechatPublicLayout.CLOSE.contains(x, y)) {
            return Target.CLOSE;
        }
        if (WechatPublicLayout.OPEN.contains(x, y)) {
            return Target.OPEN;
        }
        return WechatPublicLayout.COPY.contains(x, y) ? Target.COPY : Target.NONE;
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float centerY,
            float size,
            Paint.Align align) {
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(text, x, centerY - (metrics.ascent + metrics.descent) / 2f, textPaint);
    }
}
