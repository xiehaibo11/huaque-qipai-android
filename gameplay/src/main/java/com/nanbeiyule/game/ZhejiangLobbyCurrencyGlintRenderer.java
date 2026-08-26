package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;

/** Draws the original Xianyi Doudizhu currency glints over the Zhejiang header icons. */
final class ZhejiangLobbyCurrencyGlintRenderer {
    private static final float ORIGINAL_ICON_WIDTH = 52.0f;
    private static final float MONEY_EFFECT_NODE_SCALE = 0.85f;

    private final Bitmap star;
    private final Bitmap cardCornerStar;
    private final Bitmap[] cardSweepFrames;
    private final Paint normalPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint additivePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final XianyiCurrencyGlintAnimation.Transform transform =
            new XianyiCurrencyGlintAnimation.Transform();
    private final RectF destination = new RectF();

    ZhejiangLobbyCurrencyGlintRenderer(Resources resources) {
        star = decode(resources, R.drawable.xianyi_glint_xg2);
        cardCornerStar = decode(resources, R.drawable.xianyi_glint_dd2);
        cardSweepFrames =
                new Bitmap[] {
                    decode(resources, R.drawable.xianyi_glint_zg1),
                    decode(resources, R.drawable.xianyi_glint_zg2),
                    decode(resources, R.drawable.xianyi_glint_zg3),
                    decode(resources, R.drawable.xianyi_glint_zg4),
                    decode(resources, R.drawable.xianyi_glint_zg5)
                };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            additivePaint.setBlendMode(BlendMode.PLUS);
        } else {
            additivePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        }
    }

    void draw(Canvas canvas, long elapsedMillis) {
        float douFrame =
                XianyiCurrencyGlintAnimation.frameAt(
                        elapsedMillis, XianyiCurrencyGlintAnimation.DOU_CYCLE_FRAMES);
        float goldFrame =
                XianyiCurrencyGlintAnimation.frameAt(
                        elapsedMillis, XianyiCurrencyGlintAnimation.GOLD_CYCLE_FRAMES);
        float cardFrame =
                XianyiCurrencyGlintAnimation.frameAt(
                        elapsedMillis, XianyiCurrencyGlintAnimation.CARD_CYCLE_FRAMES);

        drawMoneyStars(
                canvas,
                douFrame,
                false,
                ZhejiangLobbyHeaderOverlayLayout.COIN_ICON,
                ZhejiangLobbyHeaderOverlayLayout.COIN_GLINT_CENTER_X,
                ZhejiangLobbyHeaderOverlayLayout.COIN_GLINT_CENTER_Y);
        drawMoneyStars(
                canvas,
                goldFrame,
                true,
                ZhejiangLobbyHeaderOverlayLayout.DIAMOND_ICON,
                ZhejiangLobbyHeaderOverlayLayout.DIAMOND_GLINT_CENTER_X,
                ZhejiangLobbyHeaderOverlayLayout.DIAMOND_GLINT_CENTER_Y);
        drawCardGlint(canvas, cardFrame);
    }

    private void drawMoneyStars(
            Canvas canvas,
            float frame,
            boolean gold,
            ZhejiangLobbyHeaderOverlayLayout.Box icon,
            float centerX,
            float centerY) {
        float nodeScale = iconWidth(icon) / ORIGINAL_ICON_WIDTH * MONEY_EFFECT_NODE_SCALE;
        for (int index = 0; index < 4; index++) {
            if (gold) {
                XianyiCurrencyGlintAnimation.sampleGoldStar(index, frame, transform);
            } else {
                XianyiCurrencyGlintAnimation.sampleDouStar(index, frame, transform);
            }
            boolean additive =
                    gold
                            && index == 0
                            && XianyiCurrencyGlintAnimation.goldMainStarUsesAdditiveBlend(frame);
            drawTransformedBitmap(
                    canvas,
                    star,
                    centerX,
                    centerY,
                    nodeScale,
                    transform,
                    additive ? additivePaint : normalPaint);
        }
    }

    private void drawCardGlint(Canvas canvas, float frame) {
        float nodeScale =
                iconWidth(ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_ICON)
                        / ORIGINAL_ICON_WIDTH;
        float centerX = ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_GLINT_CENTER_X;
        float centerY = ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_GLINT_CENTER_Y;
        for (int index = 0; index < cardSweepFrames.length; index++) {
            int alpha = XianyiCurrencyGlintAnimation.cardSweepAlpha(index, frame);
            if (alpha == 0) {
                continue;
            }
            additivePaint.setAlpha(alpha);
            drawCenteredBitmap(
                    canvas,
                    cardSweepFrames[index],
                    centerX,
                    centerY,
                    nodeScale,
                    0.0f,
                    additivePaint);
        }
        XianyiCurrencyGlintAnimation.sampleCardStar(frame, transform);
        drawTransformedBitmap(
                canvas,
                cardCornerStar,
                centerX,
                centerY,
                nodeScale,
                transform,
                normalPaint);
    }

    private void drawTransformedBitmap(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerY,
            float nodeScale,
            XianyiCurrencyGlintAnimation.Transform value,
            Paint paint) {
        if (value.alpha == 0) {
            return;
        }
        paint.setAlpha(value.alpha);
        drawCenteredBitmap(
                canvas,
                bitmap,
                centerX + value.x * nodeScale,
                centerY - value.y * nodeScale,
                value.scale * nodeScale,
                value.rotation,
                paint);
    }

    private void drawCenteredBitmap(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerY,
            float scale,
            float rotation,
            Paint paint) {
        float halfWidth = bitmap.getWidth() * scale / 2.0f;
        float halfHeight = bitmap.getHeight() * scale / 2.0f;
        destination.set(-halfWidth, -halfHeight, halfWidth, halfHeight);
        int save = canvas.save();
        canvas.translate(centerX, centerY);
        canvas.rotate(rotation);
        canvas.drawBitmap(bitmap, null, destination, paint);
        canvas.restoreToCount(save);
    }

    private static float iconWidth(ZhejiangLobbyHeaderOverlayLayout.Box icon) {
        return icon.right() - icon.left();
    }

    private static Bitmap decode(Resources resources, int resourceId) {
        return BitmapFactory.decodeResource(resources, resourceId);
    }
}
