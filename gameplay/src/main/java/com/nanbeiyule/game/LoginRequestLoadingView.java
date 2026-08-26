package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Silent full-screen Canvas loading layer for authentication requests. */
public final class LoginRequestLoadingView extends View
        implements LoginRequestLoadingController.Display {
    static final int MASK_COLOR = 0x7F1A1A1A;
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;
    private static final float TILE_WIDTH = 65.0f;
    private static final float TILE_HEIGHT = 98.0f;
    private static final float ANIMATION_CENTER_X = DESIGN_WIDTH / 2.0f;
    private static final float ANIMATION_CENTER_Y = DESIGN_HEIGHT / 2.0f;

    private final LoginRequestLoadingTimeline timeline =
            new LoginRequestLoadingTimeline();
    private final Map<LoginRequestLoadingTimeline.Tile, Bitmap> bitmaps =
            new EnumMap<>(LoginRequestLoadingTimeline.Tile.class);
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private AdaptiveViewport.Insets windowInsets =
            AdaptiveViewport.Insets.NONE;
    private long animationStartedNanos;
    private boolean attached;
    private boolean windowVisible;
    private boolean assetsReady;

    public LoginRequestLoadingView(Context context) {
        this(context, null);
    }

    public LoginRequestLoadingView(
            Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        setFocusable(true);
        setWillNotDraw(false);
        assetsReady = loadBitmaps();
        setVisibility(GONE);
    }

    @Override
    public void setLoadingVisible(boolean visible) {
        boolean actuallyVisible = visible && assetsReady;
        setVisibility(visible ? VISIBLE : GONE);
        if (!actuallyVisible) {
            setVisibility(GONE);
            animationStartedNanos = 0L;
            return;
        }
        animationStartedNanos = SystemClock.elapsedRealtimeNanos();
        if (attached && windowVisible) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    @SuppressWarnings("deprecation") // API 21-compatible inset access; targetSdk remains 28.
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        windowInsets =
                new AdaptiveViewport.Insets(
                        insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom());
        invalidate();
        return super.onApplyWindowInsets(insets);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        windowVisible = getWindowVisibility() == VISIBLE;
        if (getVisibility() == VISIBLE
                && assetsReady
                && windowVisible) {
            animationStartedNanos = SystemClock.elapsedRealtimeNanos();
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        windowVisible = false;
        animationStartedNanos = 0L;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        windowVisible = visibility == VISIBLE;
        if (windowVisible
                && attached
                && assetsReady
                && getVisibility() == VISIBLE) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!assetsReady
                || getVisibility() != VISIBLE
                || getWidth() <= 0
                || getHeight() <= 0) {
            return;
        }

        canvas.drawColor(MASK_COLOR);
        AdaptiveViewport viewport =
                AdaptiveViewport.create(
                        getWidth(),
                        getHeight(),
                        DESIGN_WIDTH,
                        DESIGN_HEIGHT,
                        windowInsets);
        AdaptiveViewport.Transform transform =
                viewport.layerTransform(
                        AdaptiveViewport.Layer.DESIGN_CENTER);
        int viewportSave = canvas.save();
        canvas.translate(transform.offsetX(), transform.offsetY());
        canvas.scale(transform.scaleX(), transform.scaleY());

        long nowNanos = SystemClock.elapsedRealtimeNanos();
        long elapsedNanos =
                Math.max(0L, nowNanos - animationStartedNanos);
        List<LoginRequestLoadingTimeline.DrawCommand> commands =
                timeline.sampleFrame(
                        timeline.frameAtElapsedNanos(elapsedNanos));
        for (LoginRequestLoadingTimeline.DrawCommand command :
                commands) {
            drawCommand(canvas, command);
        }
        canvas.restoreToCount(viewportSave);

        if (attached
                && windowVisible
                && getVisibility() == VISIBLE) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private boolean loadBitmaps() {
        bitmaps.put(
                LoginRequestLoadingTimeline.Tile.EAST,
                decode(R.drawable.login_loading_east));
        bitmaps.put(
                LoginRequestLoadingTimeline.Tile.SOUTH,
                decode(R.drawable.login_loading_south));
        bitmaps.put(
                LoginRequestLoadingTimeline.Tile.WEST,
                decode(R.drawable.login_loading_west));
        bitmaps.put(
                LoginRequestLoadingTimeline.Tile.NORTH,
                decode(R.drawable.login_loading_north));
        for (Bitmap bitmap : bitmaps.values()) {
            if (bitmap == null
                    || bitmap.getWidth() != (int) TILE_WIDTH
                    || bitmap.getHeight() != (int) TILE_HEIGHT) {
                return false;
            }
        }
        return bitmaps.size() == LoginRequestLoadingTimeline.Tile.values().length;
    }

    private Bitmap decode(int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(
                getResources(), resourceId, options);
    }

    private void drawCommand(
            Canvas canvas,
            LoginRequestLoadingTimeline.DrawCommand command) {
        Bitmap bitmap = bitmaps.get(command.tile());
        if (bitmap == null) {
            return;
        }
        int save = canvas.save();
        canvas.translate(
                ANIMATION_CENTER_X + command.x(),
                ANIMATION_CENTER_Y - command.y());
        canvas.scale(command.scaleX(), command.scaleY());
        bitmapPaint.setAlpha(
                Math.round(
                        Math.max(0.0f, Math.min(1.0f, command.alpha()))
                                * 255.0f));
        canvas.drawBitmap(
                bitmap,
                -TILE_WIDTH / 2.0f,
                -TILE_HEIGHT / 2.0f,
                bitmapPaint);
        canvas.restoreToCount(save);
    }
}
