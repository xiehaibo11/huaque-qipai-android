package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

final class AvatarFrameRenderer {
    private static final Rect GOLD_FRAME_SOURCE = new Rect(856, 1628, 974, 1745);
    private static final Rect DEFAULT_AVATAR_SOURCE = new Rect(642, 1527, 762, 1647);
    private static final Rect MEMBERSHIP_ROTATED_SOURCE = new Rect(984, 793, 1018, 891);
    private static final Rect VIP_ICON_SOURCE = new Rect(1, 1, 48, 42);

    private final Bitmap lobbyAtlas;
    private final Bitmap glow;
    private final Bitmap vipAtlas;
    private final Bitmap membershipPlaque;
    private final Bitmap vipIcon;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint inactiveMembershipPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path avatarClip = new Path();

    AvatarFrameRenderer(Resources resources) {
        lobbyAtlas = BitmapFactory.decodeResource(resources, R.drawable.avatar_lobby_atlas);
        glow = BitmapFactory.decodeResource(resources, R.drawable.avatar_glow_atlas);
        vipAtlas = BitmapFactory.decodeResource(resources, R.drawable.avatar_vip_atlas);
        Matrix rotate = new Matrix();
        rotate.setRotate(-90.0f);
        membershipPlaque =
                Bitmap.createBitmap(
                        lobbyAtlas,
                        MEMBERSHIP_ROTATED_SOURCE.left,
                        MEMBERSHIP_ROTATED_SOURCE.top,
                        MEMBERSHIP_ROTATED_SOURCE.width(),
                        MEMBERSHIP_ROTATED_SOURCE.height(),
                        rotate,
                        true);
        vipIcon =
                Bitmap.createBitmap(
                        vipAtlas,
                        VIP_ICON_SOURCE.left,
                        VIP_ICON_SOURCE.top,
                        VIP_ICON_SOURCE.width(),
                        VIP_ICON_SOURCE.height());
        ColorMatrix grayscale = new ColorMatrix();
        grayscale.setSaturation(0.0f);
        inactiveMembershipPaint.setColorFilter(new ColorMatrixColorFilter(grayscale));
        particlePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
    }

    static Bitmap loadDefaultAvatar(Resources resources) {
        Bitmap atlas = BitmapFactory.decodeResource(resources, R.drawable.avatar_lobby_atlas);
        try {
            return Bitmap.createBitmap(
                    atlas,
                    DEFAULT_AVATAR_SOURCE.left,
                    DEFAULT_AVATAR_SOURCE.top,
                    DEFAULT_AVATAR_SOURCE.width(),
                    DEFAULT_AVATAR_SOURCE.height());
        } finally {
            atlas.recycle();
        }
    }

    void draw(
            Canvas canvas,
            Bitmap avatar,
            RectF bounds,
            int membershipLevel,
            long animationTimeMs) {
        if (avatar == null || avatar.isRecycled()) {
            return;
        }
        AvatarMembershipStyle membershipStyle =
                AvatarMembershipStyle.forLevel(membershipLevel);
        if (membershipStyle.showActiveEffects()) {
            float pulse =
                    0.5f
                            + 0.5f
                                    * (float)
                                            Math.sin(
                                                    animationTimeMs
                                                            * Math.PI
                                                            * 2.0
                                                            / 1200.0);
            RectF glowBounds = new RectF(bounds);
            glowBounds.inset(-bounds.width() * 0.08f, -bounds.height() * 0.08f);
            glowPaint.setAlpha(70 + Math.round(150.0f * pulse));
            canvas.drawBitmap(glow, null, glowBounds, glowPaint);
        }

        float inset = bounds.width() * 0.055f;
        RectF imageBounds =
                new RectF(
                        bounds.left + inset,
                        bounds.top + inset,
                        bounds.right - inset,
                        bounds.bottom - inset);
        int saveCount = canvas.save();
        avatarClip.reset();
        avatarClip.addRoundRect(
                imageBounds,
                bounds.width() * 0.075f,
                bounds.width() * 0.075f,
                Path.Direction.CW);
        canvas.clipPath(avatarClip);
        drawCenterCrop(canvas, avatar, imageBounds);
        canvas.restoreToCount(saveCount);

        canvas.drawBitmap(lobbyAtlas, GOLD_FRAME_SOURCE, bounds, bitmapPaint);

        float plaqueWidth = bounds.width() * 0.82f;
        float plaqueHeight = plaqueWidth * membershipPlaque.getHeight() / membershipPlaque.getWidth();
        RectF plaqueBounds =
                new RectF(
                        bounds.centerX() - plaqueWidth / 2.0f,
                        bounds.bottom - plaqueHeight * 0.68f,
                        bounds.centerX() + plaqueWidth / 2.0f,
                        bounds.bottom + plaqueHeight * 0.32f);
        Paint membershipPaint =
                membershipStyle.grayscaleMembershipAssets()
                        ? inactiveMembershipPaint
                        : bitmapPaint;
        canvas.drawBitmap(membershipPlaque, null, plaqueBounds, membershipPaint);

        float vipWidth = bounds.width() * 0.34f;
        float vipHeight = vipWidth * vipIcon.getHeight() / vipIcon.getWidth();
        RectF vipBounds =
                new RectF(
                        bounds.right - vipWidth * 0.78f,
                        bounds.top - vipHeight * 0.18f,
                        bounds.right + vipWidth * 0.22f,
                        bounds.top + vipHeight * 0.82f);
        canvas.drawBitmap(vipIcon, null, vipBounds, membershipPaint);

        if (membershipStyle.showActiveEffects()) {
            drawSparkles(canvas, bounds, animationTimeMs);
        }
    }

    private void drawSparkles(Canvas canvas, RectF bounds, long timeMs) {
        for (int index = 0; index < 6; index++) {
            double phase = timeMs / 720.0 + index * 1.37;
            float progress = (float) (phase - Math.floor(phase));
            float angle = (float) (index * Math.PI * 2.0 / 6.0 + progress * 0.8);
            float radius = bounds.width() * (0.47f + progress * 0.11f);
            float x = bounds.centerX() + (float) Math.cos(angle) * radius;
            float y = bounds.centerY() + (float) Math.sin(angle) * radius;
            int alpha = Math.max(0, Math.round(220.0f * (1.0f - progress)));
            particlePaint.setColor(Color.argb(alpha, 255, 229, 118));
            canvas.drawCircle(x, y, bounds.width() * (0.012f + progress * 0.018f), particlePaint);
        }
    }

    private static void drawCenterCrop(Canvas canvas, Bitmap bitmap, RectF destination) {
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
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, source, destination, paint);
    }
}
