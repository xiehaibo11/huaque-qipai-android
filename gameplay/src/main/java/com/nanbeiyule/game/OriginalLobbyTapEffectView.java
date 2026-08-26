package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/** Transparent one-shot HallScene touch effect rendered from the four recovered Spine assets. */
public final class OriginalLobbyTapEffectView extends View {
    private static final String TAG = "LobbyTapEffect";
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;

    private final ExecutorService assetExecutor = Executors.newSingleThreadExecutor();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final PorterDuffXfermode additive = new PorterDuffXfermode(PorterDuff.Mode.ADD);
    private final List<Tap> taps = new ArrayList<>();
    private List<OriginalLobbyTapEffectAssets.Variant> variants;
    private boolean detached;

    public OriginalLobbyTapEffectView(Context context) {
        this(context, null);
    }

    public OriginalLobbyTapEffectView(Context context, AttributeSet attributes) {
        super(context, attributes);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        assetExecutor.execute(this::loadAssets);
    }

    public void playAt(float x, float y) {
        if (detached || !Float.isFinite(x) || !Float.isFinite(y)) {
            return;
        }
        int variant = ThreadLocalRandom.current().nextInt(
                OriginalLobbyTapEffectSpec.variants().size());
        taps.add(new Tap(x, y, variant, System.nanoTime()));
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (variants == null || taps.isEmpty() || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        long now = System.nanoTime();
        float viewportScale = Math.min(
                getWidth() / DESIGN_WIDTH,
                getHeight() / DESIGN_HEIGHT);
        for (int tapIndex = taps.size() - 1; tapIndex >= 0; tapIndex--) {
            Tap tap = taps.get(tapIndex);
            OriginalLobbyTapEffectAssets.Variant variant = variants.get(tap.variant());
            float elapsed = (now - tap.startedNanos()) / 1_000_000_000.0f;
            if (OriginalLobbyTapEffectFrame.isFinished(
                    elapsed, variant.durationSeconds())) {
                taps.remove(tapIndex);
                continue;
            }
            drawTap(canvas, tap, variant, elapsed, viewportScale);
        }
        if (!taps.isEmpty()) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        detached = true;
        assetExecutor.shutdownNow();
        taps.clear();
        recycle(variants);
        variants = null;
        super.onDetachedFromWindow();
    }

    private void drawTap(
            Canvas canvas,
            Tap tap,
            OriginalLobbyTapEffectAssets.Variant variant,
            float elapsed,
            float viewportScale) {
        float scale = variant.spec().scale() * viewportScale;
        for (Spine37Runtime.DrawCommand command :
                variant.runtime().sample(variant.spec().animationName(), elapsed)) {
            OriginalLobbyTapEffectAssets.RegionTexture texture =
                    variant.textures().get(command.attachmentName());
            if (texture == null || command.vertices().length != 8) {
                continue;
            }
            float[] mesh = OriginalLobbyTapEffectFrame.toCanvasMesh(
                    command.vertices(),
                    tap.x(),
                    tap.y(),
                    scale,
                    texture.rotated());
            paint.setAlpha(Math.round(command.color().alpha() * 255.0f));
            paint.setXfermode("additive".equals(command.blend()) ? additive : null);
            canvas.drawBitmapMesh(
                    texture.bitmap(),
                    1,
                    1,
                    mesh,
                    0,
                    null,
                    0,
                    paint);
        }
        paint.setXfermode(null);
        paint.setAlpha(255);
    }

    private void loadAssets() {
        List<OriginalLobbyTapEffectAssets.Variant> loaded = new ArrayList<>();
        try {
            for (OriginalLobbyTapEffectSpec spec : OriginalLobbyTapEffectSpec.variants()) {
                loaded.add(OriginalLobbyTapEffectAssets.load(getContext().getAssets(), spec));
            }
            if (!post(() -> acceptLoaded(loaded))) {
                recycle(loaded);
            }
        } catch (Exception exception) {
            recycle(loaded);
            Log.e(TAG, "Unable to load recovered lobby tap effects", exception);
        }
    }

    private void acceptLoaded(List<OriginalLobbyTapEffectAssets.Variant> loaded) {
        if (detached) {
            recycle(loaded);
            return;
        }
        variants = List.copyOf(loaded);
        postInvalidateOnAnimation();
    }

    private static void recycle(List<OriginalLobbyTapEffectAssets.Variant> loaded) {
        if (loaded == null) {
            return;
        }
        for (OriginalLobbyTapEffectAssets.Variant variant : loaded) {
            variant.recycle();
        }
    }

    private record Tap(float x, float y, int variant, long startedNanos) {}
}
