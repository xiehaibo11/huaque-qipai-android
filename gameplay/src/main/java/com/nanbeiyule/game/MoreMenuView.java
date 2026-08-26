package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

/** Transparent full-screen hit layer for the Zhejiang lobby "更多" expanded menu. */
@SuppressLint("ViewConstructor")
final class MoreMenuView extends AdaptiveCanvasView {
    interface Actions {
        void onItemSelected(MoreMenuItem item);

        void onDismissRequested();
    }

    private final Actions actions;
    private final Bitmap barBitmap;
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint pressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private MoreMenuItem pressedItem;
    private Runnable buttonClickSound = () -> {};

    MoreMenuView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        barBitmap =
                BitmapFactory.decodeResource(
                        getResources(), R.drawable.game_home_more_menu_bar);
        pressedPaint.setColor(Color.argb(72, 22, 40, 120));
        setClickable(true);
        setFocusable(true);
        setContentDescription("更多");
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (barBitmap == null || barBitmap.isRecycled()) {
            return;
        }
        GameHomeViewportLayout layout =
                GameHomeViewportLayout.calculate(
                        getWidth(), getHeight(), adaptiveSafeInsets());
        int save = AdaptiveCanvasDrawing.apply(canvas, layout.pageTransform());
        canvas.drawBitmap(
                barBitmap,
                MoreMenuLayout.sourceRect(),
                rect(MoreMenuLayout.DESTINATION),
                bitmapPaint);
        if (pressedItem != null) {
            int index = MoreMenuLayout.ITEMS.indexOf(pressedItem);
            if (index >= 0) {
                RectF itemRect = rect(MoreMenuLayout.itemRect(index));
                canvas.drawRoundRect(itemRect, 26.0f, 26.0f, pressedPaint);
            }
        }
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        GameHomeViewportLayout layout =
                GameHomeViewportLayout.calculate(
                        getWidth(), getHeight(), adaptiveSafeInsets());
        MoreMenuItem target =
                MoreMenuLayout.itemAt(
                        layout.toPageX(event.getX()),
                        layout.toPageY(event.getY()));
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                pressedItem = target;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (pressedItem != null && pressedItem != target) {
                    pressedItem = null;
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                pressedItem = null;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                MoreMenuItem pressed = pressedItem;
                pressedItem = null;
                invalidate();
                if (pressed != null && pressed == target) {
                    performClick();
                    actions.onItemSelected(pressed);
                } else if (target == null) {
                    actions.onDismissRequested();
                }
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

    private static RectF rect(MoreMenuLayout.FloatRect source) {
        return new RectF(source.left, source.top, source.right, source.bottom);
    }
}
