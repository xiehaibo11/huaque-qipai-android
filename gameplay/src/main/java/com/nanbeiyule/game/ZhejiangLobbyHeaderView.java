package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;

/** Renders authenticated account data inside the existing Zhejiang lobby top-control artwork. */
public final class ZhejiangLobbyHeaderView extends View {
    private final ZhejiangLobbyHeaderPresentation presentation;
    private final Bitmap originalHeader;
    private final AvatarApiClient avatarApiClient;
    private final AvatarImageLoader avatarImageLoader;
    private final ZhejiangLobbyCurrencyGlintRenderer currencyGlintRenderer;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Path avatarClip = new Path();
    private final Path foregroundClip = new Path();
    private Bitmap avatarBitmap;
    private long glintStartedAtMillis;

    public ZhejiangLobbyHeaderView(
            Context context,
            GameHomeState state,
            int originalHeaderResourceId,
            String apiBaseUrl,
            String accessToken) {
        super(context);
        presentation = ZhejiangLobbyHeaderPresentation.from(state);
        originalHeader = BitmapFactory.decodeResource(getResources(), originalHeaderResourceId);
        avatarApiClient = new AvatarApiClient(apiBaseUrl);
        avatarImageLoader = new AvatarImageLoader(context, avatarApiClient);
        currencyGlintRenderer = new ZhejiangLobbyCurrencyGlintRenderer(getResources());
        avatarBitmap = avatarImageLoader.defaultAvatar();
        avatarImageLoader.load(
                state.player().avatarKey(),
                accessToken,
                new AvatarImageLoader.Callback() {
                    @Override
                    public void onBitmap(Bitmap bitmap) {
                        avatarBitmap = bitmap;
                        invalidate();
                    }

                    @Override
                    public void onUnauthorized() {}

                    @Override
                    public void onError(String message) {}
                });
        textPaint.setTypeface(loadTypeface(context));
        setContentDescription(
                presentation.displayName()
                        + "，"
                        + presentation.playerId()
                        + "，欢乐豆"
                        + presentation.coins()
                        + "，钻石"
                        + presentation.diamonds()
                        + "，房卡"
                        + presentation.roomCards());
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        int save = canvas.save();
        canvas.scale(
                getWidth() / ZhejiangLobbyHeaderOverlayLayout.DESIGN_WIDTH,
                getHeight() / ZhejiangLobbyHeaderOverlayLayout.DESIGN_HEIGHT);
        drawOriginalForeground(canvas);
        drawAvatar(canvas);

        replaceTextBackground(
                canvas,
                ZhejiangLobbyHeaderOverlayLayout.PLAYER_NAME_PATCH_SOURCE,
                ZhejiangLobbyHeaderOverlayLayout.PLAYER_NAME_PATCH);
        replaceTextBackground(
                canvas,
                ZhejiangLobbyHeaderOverlayLayout.PLAYER_ID_PATCH_SOURCE,
                ZhejiangLobbyHeaderOverlayLayout.PLAYER_ID_PATCH);
        replaceTextBackground(
                canvas,
                ZhejiangLobbyHeaderOverlayLayout.COIN_VALUE_PATCH_SOURCE,
                ZhejiangLobbyHeaderOverlayLayout.COIN_VALUE_PATCH);
        replaceTextBackground(
                canvas,
                ZhejiangLobbyHeaderOverlayLayout.DIAMOND_VALUE_PATCH_SOURCE,
                ZhejiangLobbyHeaderOverlayLayout.DIAMOND_VALUE_PATCH);
        replaceTextBackground(
                canvas,
                ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_VALUE_PATCH_SOURCE,
                ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_VALUE_PATCH);

        drawIdentity(canvas);
        drawWalletValue(
                canvas,
                presentation.coins(),
                ZhejiangLobbyHeaderOverlayLayout.COIN_VALUE_PATCH);
        drawWalletValue(
                canvas,
                presentation.diamonds(),
                ZhejiangLobbyHeaderOverlayLayout.DIAMOND_VALUE_PATCH);
        drawWalletValue(
                canvas,
                presentation.roomCards(),
                ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_VALUE_PATCH);
        currencyGlintRenderer.draw(canvas, SystemClock.uptimeMillis() - glintStartedAtMillis);
        canvas.restoreToCount(save);
        if (isAttachedToWindow() && getWindowVisibility() == VISIBLE) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        glintStartedAtMillis = SystemClock.uptimeMillis();
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        avatarImageLoader.shutdown();
        avatarApiClient.shutdown();
        super.onDetachedFromWindow();
    }

    private void drawAvatar(Canvas canvas) {
        ZhejiangLobbyHeaderOverlayLayout.Box bounds =
                ZhejiangLobbyHeaderOverlayLayout.AVATAR_IMAGE;
        RectF destination =
                new RectF(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        int save = canvas.save();
        avatarClip.reset();
        avatarClip.addRoundRect(destination, 7.0f, 7.0f, Path.Direction.CW);
        canvas.clipPath(avatarClip);
        AvatarBitmapRenderer.drawCenterCrop(
                canvas,
                avatarBitmap,
                destination,
                bitmapPaint);
        canvas.restoreToCount(save);
    }

    private void drawOriginalForeground(Canvas canvas) {
        drawOriginalClip(canvas, ZhejiangLobbyHeaderOverlayLayout.PLAYER_PANEL, 0.0f);
        drawOriginalClip(canvas, ZhejiangLobbyHeaderOverlayLayout.AVATAR_CHROME, 18.0f);
        drawOriginalClip(canvas, ZhejiangLobbyHeaderOverlayLayout.COIN_CONTROL, 39.0f);
        drawOriginalClip(canvas, ZhejiangLobbyHeaderOverlayLayout.DIAMOND_CONTROL, 39.0f);
        drawOriginalClip(canvas, ZhejiangLobbyHeaderOverlayLayout.ROOM_CARD_CONTROL, 39.0f);
    }

    private void drawOriginalClip(
            Canvas canvas,
            ZhejiangLobbyHeaderOverlayLayout.Box box,
            float cornerRadius) {
        RectF destination = new RectF(box.left(), box.top(), box.right(), box.bottom());
        int save = canvas.save();
        if (cornerRadius > 0.0f) {
            foregroundClip.reset();
            foregroundClip.addRoundRect(
                    destination, cornerRadius, cornerRadius, Path.Direction.CW);
            canvas.clipPath(foregroundClip);
        } else {
            canvas.clipRect(destination);
        }
        canvas.drawBitmap(
                originalHeader,
                new Rect(box.left(), box.top(), box.right(), box.bottom()),
                destination,
                bitmapPaint);
        canvas.restoreToCount(save);
    }

    private void drawIdentity(Canvas canvas) {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(34.0f);
        textPaint.setShadowLayer(2.0f, 0.0f, 2.0f, Color.rgb(55, 44, 39));
        drawFittedText(canvas, presentation.displayName(), 159.0f, 58.0f, 181.0f, 22.0f);

        textPaint.setColor(Color.rgb(255, 209, 45));
        textPaint.setTextSize(31.0f);
        drawFittedText(canvas, presentation.playerId(), 159.0f, 96.0f, 181.0f, 18.0f);
        textPaint.clearShadowLayer();
    }

    private void drawWalletValue(
            Canvas canvas,
            String value,
            ZhejiangLobbyHeaderOverlayLayout.Box bounds) {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(48.0f);
        textPaint.setShadowLayer(2.0f, 0.0f, 2.0f, Color.rgb(76, 76, 76));
        drawCenteredFittedText(canvas, value, bounds, 30.0f);
        textPaint.clearShadowLayer();
    }

    private void replaceTextBackground(
            Canvas canvas,
            ZhejiangLobbyHeaderOverlayLayout.Box source,
            ZhejiangLobbyHeaderOverlayLayout.Box destination) {
        canvas.drawBitmap(
                originalHeader,
                new Rect(source.left(), source.top(), source.right(), source.bottom()),
                new RectF(
                        destination.left(),
                        destination.top(),
                        destination.right(),
                        destination.bottom()),
                bitmapPaint);
    }

    private void drawFittedText(
            Canvas canvas,
            String text,
            float x,
            float baseline,
            float maximumWidth,
            float minimumSize) {
        float originalSize = textPaint.getTextSize();
        while (textPaint.getTextSize() > minimumSize
                && textPaint.measureText(text) > maximumWidth) {
            textPaint.setTextSize(textPaint.getTextSize() - 1.0f);
        }
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.setTextSize(originalSize);
    }

    private void drawCenteredFittedText(
            Canvas canvas,
            String text,
            ZhejiangLobbyHeaderOverlayLayout.Box bounds,
            float minimumSize) {
        float originalSize = textPaint.getTextSize();
        Paint.Align originalAlign = textPaint.getTextAlign();
        textPaint.setTextAlign(Paint.Align.CENTER);
        float width = bounds.right() - bounds.left();
        while (textPaint.getTextSize() > minimumSize
                && textPaint.measureText(text) > width) {
            textPaint.setTextSize(textPaint.getTextSize() - 1.0f);
        }
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float centerY = (bounds.top() + bounds.bottom()) / 2.0f;
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(text, (bounds.left() + bounds.right()) / 2.0f, baseline, textPaint);
        textPaint.setTextSize(originalSize);
        textPaint.setTextAlign(originalAlign);
    }

    private static Typeface loadTypeface(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException exception) {
            return Typeface.DEFAULT_BOLD;
        }
    }
}
