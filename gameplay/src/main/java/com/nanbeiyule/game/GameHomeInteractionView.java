package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Touch routing for the final independent-asset game-home composition. */

abstract class GameHomeInteractionView extends GameHomeContentRenderer {
    @FunctionalInterface
    interface TapEffectPlayer {
        void play(float x, float y);
    }

    private TapEffectPlayer tapEffectPlayer = (x, y) -> {};

    protected GameHomeInteractionView(Context context, GameHomeState state) {
        super(context, state);
    }

    protected GameHomeInteractionView(
            Context context,
            GameHomeState state,
            boolean drawBackgroundEnabled) {
        super(context, state, drawBackgroundEnabled);
    }

    public void setOnHomeActionListener(OnHomeActionListener listener) {
        actionListener = listener;
    }

    public void setButtonClickSound(Runnable buttonClickSound) {
        this.buttonClickSound =
                buttonClickSound == null ? () -> {} : buttonClickSound;
    }

    void setTapEffectPlayer(TapEffectPlayer tapEffectPlayer) {
        this.tapEffectPlayer = tapEffectPlayer == null ? (x, y) -> {} : tapEffectPlayer;
    }

    public void setAvatarBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            avatarBitmap = bitmap;
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        GameHomeViewportLayout layout =
                GameHomeViewportLayout.calculate(
                        getWidth(),
                        getHeight(),
                        adaptiveSafeInsets());
        float pageX = layout.toPageX(event.getX());
        float pageY = layout.toPageY(event.getY());
        HitTarget target = findHit(pageX, pageY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                tapEffectPlayer.play(event.getX(), event.getY());
                pressedTargetKey = target == null ? null : target.key();
                invalidate();
                return target != null;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (pressedTargetKey != null
                        && (target == null
                                || !pressedTargetKey.equals(target.key()))) {
                    pressedTargetKey = null;
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                pressedTargetKey = null;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                String pressed = pressedTargetKey;
                pressedTargetKey = null;
                invalidate();
                if (target != null && target.key().equals(pressed)) {
                    performClick();
                    dispatch(target);
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

    protected void dispatch(HitTarget target) {
        if (actionListener == null) {
            return;
        }
        switch (target.kind()) {
            case PERSONAL_CENTER ->
                    actionListener.onPersonalCenterRequested();
            case MEMBERSHIP_CENTER ->
                    actionListener.onMembershipCenterRequested();
            case SHOP -> actionListener.onShopRequested();
            case SHOP_DECORATION ->
                    actionListener.onShopRequested(ShopCategory.DECORATION);
            case SHOP_ROOM_CARD ->
                    actionListener.onShopRequested(ShopCategory.ROOM_CARD);
            case SHOP_COIN ->
                    actionListener.onShopRequested(ShopCategory.COIN);
            case SHOP_DIAMOND ->
                    actionListener.onShopRequested(ShopCategory.DIAMOND_RECHARGE);
            case SHOP_INVENTORY -> actionListener.onBagRequested();
            case ACTIVITY_CENTER -> actionListener.onActivityCenterRequested();
            case SHARE -> actionListener.onShareRequested();
            case DAILY_MISSION -> actionListener.onDailyMissionRequested();
            case MAIL -> actionListener.onMailRequested();
            case GAME_RECORDS -> actionListener.onGameRecordsRequested();
            case MORE_MENU -> actionListener.onMoreRequested();
            case PERSONAL_CENTER_SETTINGS -> actionListener.onSettingsRequested();
            case CHANGE_REGION ->
                    actionListener.onChangeRegionRequested();
            case LOGOUT -> actionListener.onLogoutRequested();
            case ENTRY -> {
                if (target.entry() != null && target.entry().enabled()) {
                    actionListener.onEntryRequested(target.entry());
                } else {
                    actionListener.onUnavailableFeatureRequested(target.label());
                }
            }
            case LOBBY_STATUS -> actionListener.onLobbyStatusRequested(target.label());
            case UNAVAILABLE ->
                    actionListener.onUnavailableFeatureRequested(target.label());
        }
    }

    protected HitTarget findHit(float x, float y) {
        for (int index = hitTargets.size() - 1; index >= 0; index--) {
            HitTarget target = hitTargets.get(index);
            if (target.bounds().contains(x, y)) {
                return target;
            }
        }
        return null;
    }

    protected void configurePaints() {
        springBackgroundPaint.setColorFilter(
                new ColorMatrixColorFilter(
                        new ColorMatrix(
                                new float[] {
                                    GameHomeHeaderColorCalibration.RED_SCALE,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    GameHomeHeaderColorCalibration.GREEN_SCALE,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    GameHomeHeaderColorCalibration.BLUE_SCALE,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    1,
                                    0
                                })));
        Typeface typeface;
        try {
            typeface =
                    Typeface.createFromAsset(
                            getContext().getAssets(),
                            "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException exception) {
            typeface = Typeface.DEFAULT_BOLD;
        }
        titlePaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setShadowLayer(4.0f, 0.0f, 3.0f, Color.rgb(39, 56, 123));

        valuePaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
        valuePaint.setTextAlign(Paint.Align.LEFT);
        valuePaint.setShadowLayer(4.0f, 0.0f, 3.0f, Color.rgb(76, 62, 25));
    }

    protected void drawFittedText(
            Canvas canvas,
            String value,
            float x,
            float baseline,
            float maximumWidth,
            Paint paint,
            float minimumSize) {
        float originalSize = paint.getTextSize();
        while (paint.getTextSize() > minimumSize
                && paint.measureText(value) > maximumWidth) {
            paint.setTextSize(paint.getTextSize() - 1.0f);
        }
        canvas.drawText(value, x, baseline, paint);
        paint.setTextSize(originalSize);
    }

    protected void drawCenteredFittedText(
            Canvas canvas,
            String value,
            RectF bounds,
            Paint paint,
            float minimumSize) {
        float originalSize = paint.getTextSize();
        Paint.Align originalAlign = paint.getTextAlign();
        paint.setTextAlign(Paint.Align.CENTER);
        while (paint.getTextSize() > minimumSize
                && paint.measureText(value) > bounds.width() - 10.0f) {
            paint.setTextSize(paint.getTextSize() - 1.0f);
        }
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline =
                bounds.centerY()
                        - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(value, bounds.centerX(), baseline, paint);
        paint.setTextSize(originalSize);
        paint.setTextAlign(originalAlign);
    }

    protected void drawCenterCrop(
            Canvas canvas,
            Bitmap bitmap,
            RectF destination) {
        float sourceRatio = bitmap.getWidth() / (float) bitmap.getHeight();
        float targetRatio = destination.width() / destination.height();
        Rect source;
        if (sourceRatio > targetRatio) {
            int width = Math.round(bitmap.getHeight() * targetRatio);
            int left = (bitmap.getWidth() - width) / 2;
            source = new Rect(left, 0, left + width, bitmap.getHeight());
        } else {
            int height = Math.round(bitmap.getWidth() / targetRatio);
            int top = (bitmap.getHeight() - height) / 2;
            source = new Rect(0, top, bitmap.getWidth(), top + height);
        }
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }

    protected static RectF rect(GameHomeV3Layout.Box box) {
        return new RectF(
                box.left(),
                box.top(),
                box.right(),
                box.bottom());
    }

    protected static Rect sourceRect(GameHomeV3Layout.Box box) {
        return new Rect(
                Math.round(box.left()),
                Math.round(box.top()),
                Math.round(box.right()),
                Math.round(box.bottom()));
    }

    protected Bitmap loadBitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }
}
