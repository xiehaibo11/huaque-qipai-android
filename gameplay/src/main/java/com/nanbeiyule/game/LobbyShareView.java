package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import com.nanbeiyule.game.lobbyshare.LobbyShareContent;
import com.nanbeiyule.game.lobbyshare.LobbyShareRewardOffer;

/** Native reconstruction of the original LobbyShareView reward prompt. */
@SuppressLint("ViewConstructor")
final class LobbyShareView extends AdaptiveCanvasView {
    interface Actions {
        void onWechatShareRequested();
        void onCopyLinkRequested();
        void onCloseRequested();
    }

    private static final float WIDTH = 1920f;
    private static final float HEIGHT = 1080f;
    private final LobbyShareContent content;
    private final LobbyShareRewardOffer reward;
    private final Actions actions;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Typeface typeface;
    private final Bitmap panel;
    private final Bitmap titleBackground;
    private final Bitmap title;
    private final Bitmap close;
    private final Bitmap confirm;
    private final Bitmap rewardCell;
    private final Bitmap diamond;
    private boolean copyVisible;
    private LobbySharePromptLayout.Target pressed = LobbySharePromptLayout.Target.NONE;

    LobbyShareView(
            Context context,
            LobbyShareContent content,
            LobbyShareRewardOffer reward,
            Actions actions) {
        super(context);
        this.content = content;
        this.reward = reward;
        this.actions = actions;
        typeface = loadTypeface(context);
        panel = bitmap(R.drawable.taizhou_tool_tip_bg);
        titleBackground = bitmap(R.drawable.taizhou_tool_tip_title_bg);
        title = bitmap(R.drawable.taizhou_tool_tip_title);
        close = bitmap(R.drawable.taizhou_tool_tip_close);
        confirm = bitmap(R.drawable.taizhou_tool_tip_confirm);
        rewardCell = bitmap(R.drawable.lobby_share_reward_cell);
        diamond = bitmap(R.drawable.game_home_final_resource_diamond);
        setClickable(true);
        setFocusable(true);
        updateContentDescription();
    }

    void setCopyVisible(boolean visible) {
        if (copyVisible == visible) return;
        copyVisible = visible;
        updateContentDescription();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(178, 0, 0, 0));
        if (getWidth() <= 0 || getHeight() <= 0) return;
        AdaptiveViewport viewport = adaptiveViewport(WIDTH, HEIGHT);
        int save = AdaptiveCanvasDrawing.apply(canvas, viewport.designTransform());
        drawBitmap(canvas, panel, 417.7f, 289f, 1502.3f, 870f);
        drawBitmap(canvas, titleBackground, 796.5f, 210f, 1123.5f, 284f);
        drawBitmap(canvas, title, 906.5f, 219f, 1013.5f, 275f);
        drawBitmap(
                canvas,
                close,
                LobbySharePromptLayout.CLOSE_LEFT,
                LobbySharePromptLayout.CLOSE_TOP,
                LobbySharePromptLayout.CLOSE_RIGHT,
                LobbySharePromptLayout.CLOSE_BOTTOM);
        drawPromptBody(canvas);
        drawBitmap(
                canvas,
                confirm,
                LobbySharePromptLayout.CONFIRM_LEFT,
                LobbySharePromptLayout.CONFIRM_TOP,
                LobbySharePromptLayout.CONFIRM_RIGHT,
                LobbySharePromptLayout.CONFIRM_BOTTOM);
        if (copyVisible) drawCopyFallback(canvas);
        drawPressed(canvas);
        canvas.restoreToCount(save);
    }

    private void drawPromptBody(Canvas canvas) {
        drawText(
                canvas,
                reward.hasReward() ? "分享可领取" : "分享给微信好友",
                960f,
                367f,
                48f,
                Color.rgb(223, 97, 76));
        if (!reward.hasReward()) {
            drawText(canvas, content.shareTitle(), 960f, 535f, 42f, Color.rgb(185, 115, 69));
            return;
        }
        drawBitmap(canvas, rewardCell, 865.5f, 420f, 1054.5f, 608f);
        drawBitmap(canvas, diamond, 910f, 446f, 1010f, 546f);
        drawText(canvas, reward.rewardLabel(), 960f, 626f, 40f, Color.rgb(205, 133, 81));
    }

    private void drawCopyFallback(Canvas canvas) {
        RectF rect = rect(
                LobbySharePromptLayout.COPY_LEFT,
                LobbySharePromptLayout.COPY_TOP,
                LobbySharePromptLayout.COPY_RIGHT,
                LobbySharePromptLayout.COPY_BOTTOM);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(64, 181, 176));
        canvas.drawRoundRect(rect, 24f, 24f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(rect, 24f, 24f, paint);
        paint.setStyle(Paint.Style.FILL);
        drawText(canvas, "复制官方下载链接", rect.centerX(), rect.centerY(), 30f, Color.WHITE);
    }

    private void drawPressed(Canvas canvas) {
        RectF rect = switch (pressed) {
            case CONFIRM -> rect(809.5f, 694.5f, 1110.5f, 825.5f);
            case COPY -> rect(1130f, 704f, 1430f, 816f);
            case CLOSE -> rect(1434.5f, 187.9f, 1533.5f, 289.9f);
            case NONE -> null;
        };
        if (rect == null) return;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(64, 0, 0, 0));
        canvas.drawRoundRect(rect, 24f, 24f, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) return false;
        AdaptiveViewport.Transform transform = adaptiveViewport(WIDTH, HEIGHT).designTransform();
        LobbySharePromptLayout.Target target =
                LobbySharePromptLayout.targetAt(
                        transform.unmapX(event.getX()),
                        transform.unmapY(event.getY()),
                        copyVisible);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> pressed = target;
            case MotionEvent.ACTION_MOVE -> {
                if (target != pressed) pressed = LobbySharePromptLayout.Target.NONE;
            }
            case MotionEvent.ACTION_CANCEL -> pressed = LobbySharePromptLayout.Target.NONE;
            case MotionEvent.ACTION_UP -> {
                LobbySharePromptLayout.Target activated = target == pressed
                        ? target
                        : LobbySharePromptLayout.Target.NONE;
                pressed = LobbySharePromptLayout.Target.NONE;
                if (activated != LobbySharePromptLayout.Target.NONE) {
                    performClick();
                    activate(activated);
                }
            }
            default -> { return true; }
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void activate(LobbySharePromptLayout.Target target) {
        switch (target) {
            case CONFIRM -> actions.onWechatShareRequested();
            case COPY -> actions.onCopyLinkRequested();
            case CLOSE -> actions.onCloseRequested();
            case NONE -> { }
        }
    }

    private void drawText(Canvas canvas, String value, float x, float centerY, float size, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(typeface);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(size);
        paint.setColor(color);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(value, x, centerY - (metrics.ascent + metrics.descent) / 2f, paint);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, float l, float t, float r, float b) {
        canvas.drawBitmap(bitmap, null, rect(l, t, r, b), paint);
    }

    private Bitmap bitmap(int resource) {
        return BitmapFactory.decodeResource(getResources(), resource);
    }

    private static RectF rect(float l, float t, float r, float b) {
        return new RectF(l, t, r, b);
    }

    private void updateContentDescription() {
        String rewardText = reward.hasReward() ? "，分享可领取钻石" + reward.diamondCount() : "";
        String copyText = copyVisible ? "，可复制官方下载链接" : "";
        setContentDescription("分享提示" + rewardText + copyText);
    }

    private static Typeface loadTypeface(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        } catch (RuntimeException ignored) {
            return Typeface.DEFAULT_BOLD;
        }
    }
}
