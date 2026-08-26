package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;

@SuppressLint("ViewConstructor")
final class AvatarEditorView extends AdaptiveCanvasView {
    interface Listener {
        void onChooseRequested();

        void onSaveRequested();

        void onCloseRequested();
    }

    private static final float PAGE_WIDTH = 1672.0f;
    private static final float PAGE_HEIGHT = 941.0f;
    private static final RectF PANEL = new RectF(190.0f, 90.0f, 1482.0f, 850.0f);
    private static final RectF EDIT_AREA = new RectF(350.0f, 225.0f, 800.0f, 675.0f);
    private static final RectF PREVIEW = new RectF(1040.0f, 285.0f, 1300.0f, 545.0f);
    private static final RectF CHOOSE = new RectF(420.0f, 735.0f, 690.0f, 812.0f);
    private static final RectF SAVE = new RectF(982.0f, 735.0f, 1252.0f, 812.0f);
    private static final RectF CLOSE = new RectF(1390.0f, 108.0f, 1450.0f, 168.0f);

    private final AvatarFrameRenderer frameRenderer;
    private final int membershipLevel;
    private final Paint scrimPaint = new Paint();
    private final Paint panelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private Listener listener;
    private Bitmap selectedAvatar;
    private boolean hasNewSelection;
    private boolean uploading;

    AvatarEditorView(Context context, Bitmap initialAvatar, int membershipLevel) {
        super(context);
        this.selectedAvatar = initialAvatar;
        this.membershipLevel = membershipLevel;
        frameRenderer = new AvatarFrameRenderer(getResources());
        configurePaints();
        setFocusable(true);
        setContentDescription(getResources().getString(R.string.avatar_editor_title));
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    void setSelectedAvatar(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            selectedAvatar = bitmap;
            hasNewSelection = true;
            uploading = false;
            invalidate();
        }
    }

    void setUploading(boolean uploading) {
        this.uploading = uploading;
        invalidate();
    }

    boolean canSave() {
        return hasNewSelection && !uploading;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        canvas.drawRect(0.0f, 0.0f, getWidth(), getHeight(), scrimPaint);
        int saveCount =
                AdaptiveCanvasDrawing.apply(
                        canvas,
                        dialogTransform());

        panelPaint.setShader(
                new LinearGradient(
                        PANEL.left,
                        PANEL.top,
                        PANEL.left,
                        PANEL.bottom,
                        new int[] {
                            Color.rgb(255, 249, 218),
                            Color.rgb(240, 195, 92),
                            Color.rgb(125, 70, 25)
                        },
                        new float[] {0.0f, 0.72f, 1.0f},
                        Shader.TileMode.CLAMP));
        canvas.drawRoundRect(PANEL, 34.0f, 34.0f, panelPaint);
        panelPaint.setShader(null);
        borderPaint.setStrokeWidth(8.0f);
        borderPaint.setColor(Color.rgb(255, 228, 130));
        canvas.drawRoundRect(PANEL, 34.0f, 34.0f, borderPaint);
        borderPaint.setStrokeWidth(3.0f);
        borderPaint.setColor(Color.rgb(111, 55, 19));
        RectF inner = new RectF(PANEL);
        inner.inset(15.0f, 15.0f);
        canvas.drawRoundRect(inner, 26.0f, 26.0f, borderPaint);

        canvas.drawText(
                getResources().getString(R.string.avatar_editor_title),
                PANEL.centerX(),
                166.0f,
                titlePaint);
        drawClose(canvas);
        drawPhotoArea(canvas);
        drawPreview(canvas);
        drawButton(
                canvas,
                CHOOSE,
                getResources().getString(R.string.avatar_editor_choose_photo),
                !uploading);
        drawButton(
                canvas,
                SAVE,
                uploading
                        ? getResources().getString(R.string.avatar_editor_saving)
                        : getResources().getString(R.string.avatar_editor_save),
                canSave());

        canvas.restoreToCount(saveCount);
        if (isShown()) {
            postInvalidateDelayed(33L);
        }
    }

    private void drawPhotoArea(Canvas canvas) {
        buttonPaint.setColor(Color.argb(224, 255, 250, 228));
        canvas.drawRoundRect(EDIT_AREA, 24.0f, 24.0f, buttonPaint);
        borderPaint.setStrokeWidth(5.0f);
        borderPaint.setColor(Color.rgb(178, 112, 36));
        canvas.drawRoundRect(EDIT_AREA, 24.0f, 24.0f, borderPaint);
        RectF photo = new RectF(EDIT_AREA);
        photo.inset(30.0f, 30.0f);
        AvatarBitmapRenderer.drawCenterCrop(
                canvas, selectedAvatar, photo, bitmapPaint);
        bodyPaint.setTextAlign(Paint.Align.CENTER);
        bodyPaint.setTextSize(24.0f);
        bodyPaint.setColor(Color.rgb(105, 63, 26));
        canvas.drawText(
                getResources().getString(R.string.avatar_editor_crop_hint),
                EDIT_AREA.centerX(),
                EDIT_AREA.bottom + 38.0f,
                bodyPaint);
    }

    private void drawPreview(Canvas canvas) {
        bodyPaint.setTextAlign(Paint.Align.CENTER);
        bodyPaint.setTextSize(30.0f);
        bodyPaint.setColor(Color.rgb(103, 53, 20));
        canvas.drawText(
                getResources().getString(R.string.avatar_editor_preview),
                PREVIEW.centerX(),
                PREVIEW.top - 34.0f,
                bodyPaint);
        frameRenderer.draw(
                canvas,
                selectedAvatar,
                PREVIEW,
                membershipLevel,
                android.os.SystemClock.uptimeMillis());
    }

    private void drawClose(Canvas canvas) {
        buttonPaint.setColor(Color.rgb(172, 91, 25));
        canvas.drawCircle(CLOSE.centerX(), CLOSE.centerY(), 28.0f, buttonPaint);
        titlePaint.setTextSize(36.0f);
        canvas.drawText("×", CLOSE.centerX(), CLOSE.centerY() + 12.0f, titlePaint);
        titlePaint.setTextSize(43.0f);
    }

    private void drawButton(Canvas canvas, RectF bounds, String text, boolean enabled) {
        buttonPaint.setShader(
                new LinearGradient(
                        bounds.left,
                        bounds.top,
                        bounds.left,
                        bounds.bottom,
                        enabled
                                ? new int[] {
                                    Color.rgb(255, 224, 101),
                                    Color.rgb(221, 128, 29)
                                }
                                : new int[] {
                                    Color.rgb(205, 190, 145),
                                    Color.rgb(142, 116, 73)
                                },
                        null,
                        Shader.TileMode.CLAMP));
        canvas.drawRoundRect(bounds, 28.0f, 28.0f, buttonPaint);
        buttonPaint.setShader(null);
        borderPaint.setColor(enabled ? Color.rgb(120, 61, 17) : Color.rgb(105, 93, 72));
        borderPaint.setStrokeWidth(4.0f);
        canvas.drawRoundRect(bounds, 28.0f, 28.0f, borderPaint);
        bodyPaint.setTextAlign(Paint.Align.CENTER);
        bodyPaint.setTextSize(32.0f);
        bodyPaint.setColor(Color.WHITE);
        Paint.FontMetrics metrics = bodyPaint.getFontMetrics();
        float baseline = bounds.centerY() - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(text, bounds.centerX(), baseline, bodyPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return super.onTouchEvent(event);
        }
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        AdaptiveViewport.Transform transform = dialogTransform();
        float x = transform.unmapX(event.getX());
        float y = transform.unmapY(event.getY());
        if (CLOSE.contains(x, y)) {
            performClick();
            if (listener != null) {
                listener.onCloseRequested();
            }
            return true;
        }
        if (!uploading && CHOOSE.contains(x, y)) {
            performClick();
            if (listener != null) {
                listener.onChooseRequested();
            }
            return true;
        }
        if (canSave() && SAVE.contains(x, y)) {
            performClick();
            if (listener != null) {
                listener.onSaveRequested();
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private AdaptiveViewport.Transform dialogTransform() {
        return adaptiveViewport(PAGE_WIDTH, PAGE_HEIGHT)
                .dialogTransform(
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        1.0f,
                        1.0f);
    }

    private void configurePaints() {
        scrimPaint.setColor(Color.argb(190, 16, 22, 48));
        borderPaint.setStyle(Paint.Style.STROKE);
        Typeface typeface;
        try {
            typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException exception) {
            typeface = Typeface.DEFAULT_BOLD;
        }
        titlePaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(43.0f);
        titlePaint.setColor(Color.WHITE);
        titlePaint.setShadowLayer(4.0f, 0.0f, 3.0f, Color.rgb(96, 43, 10));
        bodyPaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
        bodyPaint.setShadowLayer(2.0f, 0.0f, 2.0f, Color.rgb(93, 46, 16));
    }

}
