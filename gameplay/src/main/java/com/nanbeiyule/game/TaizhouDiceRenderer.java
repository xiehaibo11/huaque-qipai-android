package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouDiceLayout;
import com.nanbeiyule.game.mahjong.TaizhouDiceState;
import java.util.List;

/** Draws the table-centre throw-chip values from the original-shaped dice state. */
final class TaizhouDiceRenderer {
    static final long THROW_CHIP_ROLL_MILLIS = 500L;
    static final long FIXED_CHIP_HOLD_MILLIS = 700L;
    private static final long ROLL_FRAME_MILLIS = 80L;
    private static final int FRAME_WIDTH = 320;
    private static final int FRAME_HEIGHT = 240;
    private static final int FRAME_Y = 2;
    private static final int FRAME_GAP = 2;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap atlas;
    private long revision = Long.MIN_VALUE;
    private int eventOrder = Integer.MIN_VALUE;
    private long startedElapsed;

    TaizhouDiceRenderer(Context context) {
        atlas = BitmapFactory.decodeResource(context.getResources(), R.drawable.taizhou_mahjong_dice_static);
    }

    void draw(Canvas canvas, GameplayTableState tableState, long nowElapsed) {
        if (tableState == null || tableState.diceRoll().isEmpty()) {
            return;
        }
        TaizhouDiceState dice = tableState.diceRoll().get();
        ensureStarted(tableState, nowElapsed);
        List<Integer> values = dice.values();
        for (int index = 0; index < values.size(); index++) {
            drawValue(
                    canvas,
                    displayedValue(dice, values.get(index), index, nowElapsed),
                    TaizhouDiceLayout.nodeFor(values.size(), index));
        }
    }

    long nextRepaintDelayMillis(GameplayTableState tableState, long nowElapsed) {
        if (tableState == null || tableState.diceRoll().isEmpty()) {
            return 0L;
        }
        ensureStarted(tableState, nowElapsed);
        TaizhouDiceState dice = tableState.diceRoll().get();
        if (!dice.showAnimation()) {
            return 0L;
        }
        long elapsed = Math.max(0L, nowElapsed - startedElapsed);
        if (elapsed < THROW_CHIP_ROLL_MILLIS) {
            return ROLL_FRAME_MILLIS;
        }
        long total = THROW_CHIP_ROLL_MILLIS + FIXED_CHIP_HOLD_MILLIS;
        return elapsed < total ? total - elapsed : 0L;
    }

    private void ensureStarted(GameplayTableState tableState, long nowElapsed) {
        if (revision != tableState.revision() || eventOrder != tableState.eventOrder()) {
            revision = tableState.revision();
            eventOrder = tableState.eventOrder();
            startedElapsed = nowElapsed;
        }
    }

    private int displayedValue(TaizhouDiceState dice, int fixedValue, int index, long nowElapsed) {
        long elapsed = Math.max(0L, nowElapsed - startedElapsed);
        if (!dice.showAnimation() || elapsed >= THROW_CHIP_ROLL_MILLIS) {
            return fixedValue;
        }
        return (int) (((elapsed / ROLL_FRAME_MILLIS) + index) % 6L) + 1;
    }

    private void drawValue(Canvas canvas, int value, TaizhouDiceLayout.Node node) {
        if (atlas == null || atlas.isRecycled()) {
            return;
        }
        int left = 2 + (value - 1) * (FRAME_WIDTH + FRAME_GAP);
        Rect src = new Rect(left, FRAME_Y, left + FRAME_WIDTH, FRAME_Y + FRAME_HEIGHT);
        canvas.drawBitmap(
                atlas,
                src,
                new RectF(node.left(), node.top(), node.left() + node.width(), node.top() + node.height()),
                paint);
    }
}
