package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Native 台州麻将 loading page.
 *
 * <p>The progress geometry follows the recovered Zhejiang lobby
 * {@code Progress/Progress.csb} and the 33 ms smoothed scheduler used by the
 * original Lua hot-update scene. The background is the 南北娱乐-provided Taizhou
 * Mahjong artwork, not the Zhejiang packaged background.
 */
public final class TaizhouMahjongLoadingView extends AdaptiveCanvasView {
    public interface OnProgressCompleteListener {
        void onProgressComplete();
    }

    private static final long FRAME_INTERVAL_MS = 33L;

    private static final float DESIGN_WIDTH = LoginViewportLayout.PAGE_WIDTH;
    private static final float DESIGN_HEIGHT = LoginViewportLayout.PAGE_HEIGHT;

    private static final float CSB_DESIGN_WIDTH = 1920.0f;
    private static final float CSB_DESIGN_HEIGHT = 1080.0f;
    private static final float CSB_SCALE_X = DESIGN_WIDTH / CSB_DESIGN_WIDTH;
    private static final float CSB_SCALE_Y = DESIGN_HEIGHT / CSB_DESIGN_HEIGHT;

    private static final float FRAME_LEFT = (960.0f - 1495.0f / 2.0f) * CSB_SCALE_X;
    private static final float FRAME_TOP =
            (CSB_DESIGN_HEIGHT - (200.0f + 88.0f / 2.0f)) * CSB_SCALE_Y;
    private static final float FRAME_WIDTH = 1495.0f * CSB_SCALE_X;
    private static final float FRAME_HEIGHT = 88.0f * CSB_SCALE_Y;
    private static final float FRAME_LEFT_CAP = 90.0f * CSB_SCALE_X;
    private static final float FRAME_RIGHT_CAP = 90.0f * CSB_SCALE_X;

    private static final float FILL_LEFT =
            (960.0f
                            - 1495.0f / 2.0f
                            + 745.0208f
                            - 1348.0415f / 2.0f)
                    * CSB_SCALE_X;
    private static final float FILL_TOP =
            (CSB_DESIGN_HEIGHT
                            - (200.0f
                                    - 88.0f / 2.0f
                                    + 50.0f
                                    + 57.0f / 2.0f))
                    * CSB_SCALE_Y;
    private static final float FILL_WIDTH = 1348.0415f * CSB_SCALE_X;
    private static final float FILL_HEIGHT = 57.0f * CSB_SCALE_Y;

    private static final float PERCENT_CENTER_X = DESIGN_WIDTH / 2.0f;
    private static final float PERCENT_CENTER_Y =
            (CSB_DESIGN_HEIGHT - 206.0f) * CSB_SCALE_Y;
    private static final float STATUS_CENTER_Y =
            (CSB_DESIGN_HEIGHT - 138.0f) * CSB_SCALE_Y;

    private final Bitmap backgroundBitmap;
    private final Bitmap frameBitmap;
    private final Bitmap fillBitmap;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint percentagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float displayedProgress;
    private float targetProgress;
    private boolean progressTickScheduled;
    private boolean progressCompleteNotified;
    private String statusText = "正在加载台州麻将...";
    private OnProgressCompleteListener onProgressCompleteListener;

    private final Runnable progressTick =
            new Runnable() {
                @Override
                public void run() {
                    progressTickScheduled = false;
                    advanceProgress();
                }
            };

    public TaizhouMahjongLoadingView(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(75, 197, 243));

        backgroundBitmap =
                BitmapFactory.decodeResource(
                        getResources(), R.drawable.taizhou_mahjong_loading_background);
        frameBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.login_loading_bg);
        fillBitmap =
                BitmapFactory.decodeResource(getResources(), R.drawable.login_loading_process);

        Typeface loadingTypeface;
        try {
            loadingTypeface =
                    Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException ignored) {
            loadingTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        }

        percentagePaint.setColor(Color.WHITE);
        percentagePaint.setTextAlign(Paint.Align.CENTER);
        percentagePaint.setTextSize(36.0f * CSB_SCALE_Y);
        percentagePaint.setTypeface(loadingTypeface);
        percentagePaint.setShadowLayer(1.0f, 0.0f, 1.0f, Color.rgb(175, 96, 29));

        statusPaint.setColor(Color.rgb(255, 230, 160));
        statusPaint.setTextAlign(Paint.Align.CENTER);
        statusPaint.setTextSize(32.0f * CSB_SCALE_Y);
        statusPaint.setTypeface(loadingTypeface);
        statusPaint.setShadowLayer(1.0f, 0.0f, 1.0f, Color.rgb(83, 38, 30));
    }

    public void setTargetProgress(float percent) {
        targetProgress = Math.max(displayedProgress, clampPercent(percent));
        scheduleProgressTick();
        invalidate();
    }

    public void setStatusText(String text) {
        statusText = text == null ? "" : text;
        invalidate();
    }

    public void setOnProgressCompleteListener(OnProgressCompleteListener listener) {
        onProgressCompleteListener = listener;
        notifyProgressCompleteIfReady();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        LoginViewportLayout layout =
                LoginViewportLayout.calculate(getWidth(), getHeight(), adaptiveSafeInsets());
        AdaptiveViewport viewport = layout.adaptiveViewport();
        AdaptiveCanvasDrawing.drawFullBleedBitmap(
                canvas,
                backgroundBitmap,
                bitmapPaint,
                viewport,
                DESIGN_WIDTH,
                DESIGN_HEIGHT);

        int saveCount =
                AdaptiveCanvasDrawing.apply(
                        canvas,
                        viewport.designTransform());

        drawScale9Frame(canvas);
        drawProgressFill(canvas);
        drawCenteredText(
                canvas,
                ((int) Math.ceil(displayedProgress)) + "%",
                PERCENT_CENTER_X,
                PERCENT_CENTER_Y,
                percentagePaint);
        drawCenteredText(
                canvas,
                statusText,
                PERCENT_CENTER_X,
                STATUS_CENTER_Y,
                statusPaint);

        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(progressTick);
        progressTickScheduled = false;
        super.onDetachedFromWindow();
    }

    private void drawProgressFill(Canvas canvas) {
        float visibleWidth = FILL_WIDTH * displayedProgress / 100.0f;

        int saveCount = canvas.save();
        canvas.clipRect(
                FILL_LEFT,
                FILL_TOP,
                FILL_LEFT + visibleWidth,
                FILL_TOP + FILL_HEIGHT);
        canvas.drawBitmap(
                fillBitmap,
                null,
                new RectF(
                        FILL_LEFT,
                        FILL_TOP,
                        FILL_LEFT + FILL_WIDTH,
                        FILL_TOP + FILL_HEIGHT),
                bitmapPaint);
        canvas.restoreToCount(saveCount);
    }

    private void drawScale9Frame(Canvas canvas) {
        int sourceWidth = frameBitmap.getWidth();
        int sourceHeight = frameBitmap.getHeight();
        int leftCap = Math.round(FRAME_LEFT_CAP);
        int rightCap = Math.round(FRAME_RIGHT_CAP);

        Rect sourceLeft = new Rect(0, 0, leftCap, sourceHeight);
        Rect sourceMiddle = new Rect(leftCap, 0, sourceWidth - rightCap, sourceHeight);
        Rect sourceRight = new Rect(sourceWidth - rightCap, 0, sourceWidth, sourceHeight);

        RectF targetLeft =
                new RectF(
                        FRAME_LEFT,
                        FRAME_TOP,
                        FRAME_LEFT + FRAME_LEFT_CAP,
                        FRAME_TOP + FRAME_HEIGHT);
        RectF targetMiddle =
                new RectF(
                        FRAME_LEFT + FRAME_LEFT_CAP,
                        FRAME_TOP,
                        FRAME_LEFT + FRAME_WIDTH - FRAME_RIGHT_CAP,
                        FRAME_TOP + FRAME_HEIGHT);
        RectF targetRight =
                new RectF(
                        FRAME_LEFT + FRAME_WIDTH - FRAME_RIGHT_CAP,
                        FRAME_TOP,
                        FRAME_LEFT + FRAME_WIDTH,
                        FRAME_TOP + FRAME_HEIGHT);

        canvas.drawBitmap(frameBitmap, sourceLeft, targetLeft, bitmapPaint);
        canvas.drawBitmap(frameBitmap, sourceMiddle, targetMiddle, bitmapPaint);
        canvas.drawBitmap(frameBitmap, sourceRight, targetRight, bitmapPaint);
    }

    private void advanceProgress() {
        if (displayedProgress >= targetProgress) {
            return;
        }

        displayedProgress = Math.min(targetProgress, Math.min(100.0f, displayedProgress + 1.0f));
        invalidate();
        notifyProgressCompleteIfReady();

        if (displayedProgress < targetProgress) {
            scheduleProgressTick();
        }
    }

    private void scheduleProgressTick() {
        if (progressTickScheduled || displayedProgress >= targetProgress) {
            return;
        }
        progressTickScheduled = true;
        postDelayed(progressTick, FRAME_INTERVAL_MS);
    }

    private void notifyProgressCompleteIfReady() {
        if (progressCompleteNotified
                || displayedProgress < 100.0f
                || targetProgress < 100.0f
                || onProgressCompleteListener == null) {
            return;
        }
        progressCompleteNotified = true;
        onProgressCompleteListener.onProgressComplete();
    }

    private static void drawCenteredText(
            Canvas canvas, String text, float centerX, float centerY, Paint paint) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(text, centerX, baseline, paint);
    }

    private static float clampPercent(float percent) {
        return Math.max(0.0f, Math.min(100.0f, percent));
    }
}
