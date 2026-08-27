package com.nanbeiyule.game;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import com.nanbeiyule.game.cocosarmature.ArmatureAtlas;
import com.nanbeiyule.game.cocosarmature.ArmatureData;
import com.nanbeiyule.game.cocosarmature.ArmatureExportJson;
import com.nanbeiyule.game.cocosarmature.ArmaturePlayer;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouDiceLayout;
import com.nanbeiyule.game.mahjong.TaizhouDiceState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Draws the table-centre throw-chip values from the original-shaped dice state. */
final class TaizhouDiceRenderer {
    static final long THROW_CHIP_ROLL_MILLIS = 500L;
    static final long FIXED_CHIP_HOLD_MILLIS = 700L;
    private static final long ARMATURE_FRAME_MILLIS = 17L;
    private static final String DIR = "taizhou_mahjong_dice_effects/saizi_ani";

    private final ArmatureData data;
    private final ArmatureAtlas atlas;
    private final Map<String, ArmaturePlayer> players = new HashMap<>();
    private long revision = Long.MIN_VALUE;
    private int eventOrder = Integer.MIN_VALUE;
    private long startedElapsed;

    TaizhouDiceRenderer(Context context) {
        AssetManager assets = context.getAssets();
        data = ArmatureExportJson.load(assets, DIR + "/saizi_ani.ExportJson");
        atlas = ArmatureAtlas.load(assets, DIR + "/saizi_ani0.png", DIR + "/saizi_ani0.json");
    }

    void draw(Canvas canvas, GameplayTableState tableState, long nowElapsed) {
        if (tableState == null || tableState.diceRoll().isEmpty()) {
            return;
        }
        TaizhouDiceState dice = tableState.diceRoll().get();
        ensureStarted(tableState, nowElapsed);
        long elapsed = Math.max(0L, nowElapsed - startedElapsed);
        if (!isVisible(dice, elapsed)) {
            return;
        }
        List<Integer> values = dice.values();
        int visibleCount = Math.min(values.size(), TaizhouDiceLayout.MAX_ORIGINAL_DICE);
        for (int index = 0; index < visibleCount; index++) {
            drawValue(canvas, dice, values.get(index), elapsed,
                    TaizhouDiceLayout.nodeFor(visibleCount, index));
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
        if (!isVisible(dice, elapsed)) {
            return 0L;
        }
        if (elapsed < THROW_CHIP_ROLL_MILLIS) {
            return ARMATURE_FRAME_MILLIS;
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

    private boolean isVisible(TaizhouDiceState dice, long elapsed) {
        return !dice.showAnimation() || elapsed < THROW_CHIP_ROLL_MILLIS + FIXED_CHIP_HOLD_MILLIS;
    }

    private void drawValue(
            Canvas canvas, TaizhouDiceState dice, int fixedValue, long elapsed,
            TaizhouDiceLayout.Node node) {
        String movement = movementName(dice, fixedValue, elapsed);
        float animationSeconds = "loop".equals(movement) ? elapsed / 1000.0f : 0.0f;
        players.computeIfAbsent(movement, name -> new ArmaturePlayer(data, atlas, data.movement(name)))
                .draw(canvas, animationSeconds, node.centerX(), node.centerY(),
                        TaizhouDiceLayout.SPRITE_SCALE);
    }

    private static String movementName(TaizhouDiceState dice, int fixedValue, long elapsed) {
        if (dice.showAnimation() && elapsed < THROW_CHIP_ROLL_MILLIS) {
            return "loop";
        }
        return String.valueOf(fixedValue);
    }

    void release() {
        players.clear();
        atlas.recycle();
    }
}
