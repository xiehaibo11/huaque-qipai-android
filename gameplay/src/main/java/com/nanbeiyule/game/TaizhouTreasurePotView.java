package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

/** Native JuBaoPen main scene with the original animation and touch state machine. */
@SuppressLint("ViewConstructor")
final class TaizhouTreasurePotView extends TaizhouToolView {
    interface Actions {
        void onCloseRequested();

        void onDescriptionRequested();

        void onInventoryRequested();

        void onDrawRequested(int count);

        void onResultReady(FortuneTreasureDrawResult result, FortuneState beforeDraw);
    }

    private static final float ORIGINAL_REVEAL_SECONDS = 1.8f;
    private static final long ORIGINAL_DRAW_AUDIO_DELAY_MS = 500L;

    private final Actions actions;
    private final TaizhouTreasurePotRenderer renderer;
    private final TaizhouTreasureAudio audio;
    private final TaizhouTreasureDrawGate drawGate = new TaizhouTreasureDrawGate();
    private boolean released;
    private final Runnable drawAudioCue;
    private FortuneState state;
    private FortuneState beforeDraw;
    private long sceneStartedNanos;
    private long mainStartedNanos;
    private long tooltipUntilNanos;
    private String mainAnimation = "cx";
    private int tooltipIndex;
    private boolean revealEventSent;
    private boolean initialCollapse;
    private boolean firstDraw = true;
    private boolean resultPosted;

    TaizhouTreasurePotView(Context context, FortuneState state, Actions actions) {
        super(context);
        this.state = state;
        this.actions = actions;
        renderer = new TaizhouTreasurePotRenderer(getResources());
        audio = new TaizhouTreasureAudio(getContext().getAssets());
        drawAudioCue = () -> {
            if (!released) audio.playDrawCue();
        };
        sceneStartedNanos = System.nanoTime();
        mainStartedNanos = sceneStartedNanos;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        long now = System.nanoTime();
        float sceneSeconds = secondsSince(sceneStartedNanos, now);
        float mainSeconds = secondsSince(mainStartedNanos, now);
        if ("cx".equals(mainAnimation)
                && mainSeconds >= renderer.animationDuration("cx")) {
            mainAnimation = "loop";
            mainStartedNanos = now;
            mainSeconds = 0.0f;
        }
        if ("cj".equals(mainAnimation)) {
            float eventTime = renderer.revealEventTime();
            if (!Float.isFinite(eventTime) || eventTime <= 0.0f) {
                eventTime = ORIGINAL_REVEAL_SECONDS;
            }
            if (!revealEventSent && mainSeconds >= eventTime) {
                revealEventSent = true;
                drawGate.onAnimationEvent("cx");
                dispatchResultWhenReady();
            }
        }
        if (tooltipIndex > 0 && now >= tooltipUntilNanos) tooltipIndex = 0;
        float collapseSeconds = initialCollapse ? mainSeconds : -1.0f;
        renderer.draw(
                canvas,
                state,
                new TaizhouTreasurePotRenderer.Frame(
                        sceneSeconds,
                        mainAnimation,
                        mainSeconds,
                        collapseSeconds,
                        tooltipIndex));
        if (isAttachedToWindow() && !released) postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) return true;
        if (drawGate.drawing()) return true;
        float x = designX(event);
        float y = designY(event);
        if (TaizhouTreasurePotLayout.BACK.contains(x, y)) {
            actions.onCloseRequested();
        } else if (TaizhouTreasurePotLayout.HELP.contains(x, y)) {
            actions.onDescriptionRequested();
        } else if (TaizhouTreasurePotLayout.MY_TREASURES.contains(x, y)) {
            actions.onInventoryRequested();
        } else if (TaizhouTreasurePotLayout.DRAW_ONE.contains(x, y)) {
            beginDraw(1, false);
        } else if (TaizhouTreasurePotLayout.DRAW_FIVE.contains(x, y)) {
            beginDraw(5, false);
        } else {
            int index = TaizhouTreasurePotLayout.itemAt(x, y);
            if (index > 0) {
                tooltipIndex = index;
                tooltipUntilNanos = System.nanoTime() + 2_000_000_000L;
                invalidate();
            }
        }
        performClick();
        return true;
    }

    void onDrawResult(FortuneTreasureDrawResult result) {
        drawGate.onServerResult(result);
        dispatchResultWhenReady();
        invalidate();
    }

    void onDrawError() {
        drawGate.onFailure();
        beforeDraw = null;
        resultPosted = false;
        initialCollapse = false;
        revealEventSent = false;
        mainAnimation = "loop";
        mainStartedNanos = System.nanoTime();
        invalidate();
    }

    void replaceState(FortuneState replacement) {
        if (replacement == null) return;
        state = replacement;
        invalidate();
    }

    void resumeAfterResult() {
        beforeDraw = null;
        resultPosted = false;
        initialCollapse = false;
        revealEventSent = false;
        tooltipIndex = 0;
        mainAnimation = "loop";
        mainStartedNanos = System.nanoTime();
        invalidate();
    }

    boolean startRepeatDraw(int count) {
        return beginDraw(count, true);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!released) {
            released = true;
            removeCallbacks(drawAudioCue);
            audio.release();
            renderer.release();
        }
    }

    private boolean beginDraw(int count, boolean repeat) {
        if (!drawGate.begin(count, repeat)) return false;
        beforeDraw = state;
        resultPosted = false;
        revealEventSent = false;
        tooltipIndex = 0;
        initialCollapse = firstDraw && !repeat;
        firstDraw = false;
        if (initialCollapse) renderer.prepareCollapse();
        mainAnimation = "cj";
        mainStartedNanos = System.nanoTime();
        removeCallbacks(drawAudioCue);
        postDelayed(drawAudioCue, ORIGINAL_DRAW_AUDIO_DELAY_MS);
        actions.onDrawRequested(count);
        invalidate();
        return true;
    }

    private void dispatchResultWhenReady() {
        if (resultPosted || !drawGate.canReveal()) return;
        FortuneTreasureDrawResult result = drawGate.consumeReveal();
        FortuneState snapshot = beforeDraw;
        if (result == null || snapshot == null) return;
        resultPosted = true;
        post(() -> actions.onResultReady(result, snapshot));
    }

    private static float secondsSince(long start, long now) {
        return Math.max(0.0f, (now - start) / 1_000_000_000.0f);
    }
}
