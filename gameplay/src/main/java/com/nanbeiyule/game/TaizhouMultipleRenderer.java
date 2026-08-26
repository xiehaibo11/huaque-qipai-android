package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import com.nanbeiyule.game.mahjong.TaizhouMultipleLayout;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** Draws Common/CSB/GameBase/AddMultipleLayer.csb from server-projected gold-room state. */
final class TaizhouMultipleRenderer {
    private static final int TEXT_COST = Color.rgb(255, 255, 255);

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Bitmap buttonNone;
    private final Bitmap buttonAdd;
    private final Bitmap buttonSuper;
    private final Bitmap card;
    private final Bitmap tipRed;
    private final Map<TaizhouMultipleState.Choice, Bitmap> plates =
            new EnumMap<>(TaizhouMultipleState.Choice.class);

    TaizhouMultipleRenderer(Context context) {
        buttonNone = bitmap(context, R.drawable.taizhou_multiple_button_none);
        buttonAdd = bitmap(context, R.drawable.taizhou_multiple_button_add);
        buttonSuper = bitmap(context, R.drawable.taizhou_multiple_button_super);
        card = bitmap(context, R.drawable.taizhou_multiple_card);
        tipRed = bitmap(context, R.drawable.taizhou_multiple_tip_red);
        plates.put(
                TaizhouMultipleState.Choice.PASS,
                bitmap(context, R.drawable.taizhou_multiple_plate_none));
        plates.put(
                TaizhouMultipleState.Choice.DEFAULT,
                bitmap(context, R.drawable.taizhou_multiple_plate_add));
        plates.put(
                TaizhouMultipleState.Choice.SUPER,
                bitmap(context, R.drawable.taizhou_multiple_plate_super));
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    }

    void draw(Canvas canvas, GameplayTableState tableState) {
        if (tableState == null || tableState.multipleChoice().isEmpty()) {
            return;
        }
        TaizhouMultipleState state = tableState.multipleChoice().get();
        if (!state.goldMode()) {
            return;
        }
        drawChoiceButtons(canvas, tableState, state);
        drawSeatChoices(canvas, tableState, state);
    }

    private void drawChoiceButtons(
            Canvas canvas, GameplayTableState tableState, TaizhouMultipleState state) {
        if (!localChoicePending(tableState, state)) {
            return;
        }
        drawButton(
                canvas,
                buttonNone,
                TaizhouMultipleLayout.BUTTON_NONE,
                state.canChoose(TaizhouMultipleState.Choice.PASS));
        drawButton(
                canvas,
                buttonAdd,
                TaizhouMultipleLayout.BUTTON_ADD,
                state.canChoose(TaizhouMultipleState.Choice.DEFAULT));
        drawButton(
                canvas,
                buttonSuper,
                TaizhouMultipleLayout.BUTTON_SUPER,
                state.canChoose(TaizhouMultipleState.Choice.SUPER));
        if (state.canChoose(TaizhouMultipleState.Choice.SUPER)) {
            drawSuperCost(canvas, state);
        }
    }

    private void drawButton(
            Canvas canvas, Bitmap bitmap, TaizhouMultipleLayout.Node node, boolean enabled) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        int alpha = bitmapPaint.getAlpha();
        bitmapPaint.setAlpha(enabled ? 255 : 116);
        drawNode(canvas, bitmap, node);
        bitmapPaint.setAlpha(alpha);
    }

    private void drawSeatChoices(
            Canvas canvas, GameplayTableState tableState, TaizhouMultipleState state) {
        for (int seat = 1; seat <= tableState.chairCount(); seat++) {
            Optional<TaizhouMultipleState.Choice> choice = state.choiceForSeat(seat);
            if (choice.isEmpty()) {
                continue;
            }
            Bitmap plate = plates.get(choice.get());
            if (plate == null || plate.isRecycled()) {
                continue;
            }
            drawCenteredInside(canvas, plate, plateNodeForSeat(tableState, seat));
        }
    }

    private TaizhouMultipleLayout.Node plateNodeForSeat(GameplayTableState tableState, int seat) {
        int localSeat =
                TaizhouMahjongSeatMapper.toLocalSeat(
                        seat, tableState.mySeat(), tableState.chairCount());
        return switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_LEFT -> TaizhouMultipleLayout.PLATE_LEFT;
            case TaizhouMahjongTableLayout.SEAT_TOP -> TaizhouMultipleLayout.PLATE_TOP;
            case TaizhouMahjongTableLayout.SEAT_RIGHT -> TaizhouMultipleLayout.PLATE_RIGHT;
            case TaizhouMahjongTableLayout.SEAT_BOTTOM -> TaizhouMultipleLayout.PLATE_BOTTOM;
            default -> throw new IllegalArgumentException("unknown local seat " + localSeat);
        };
    }

    private static boolean localChoicePending(
            GameplayTableState tableState, TaizhouMultipleState state) {
        return state.choiceActive() && state.choiceForSeat(tableState.mySeat()).isEmpty();
    }

    private void drawSuperCost(Canvas canvas, TaizhouMultipleState state) {
        TaizhouMultipleLayout.Node button = TaizhouMultipleLayout.BUTTON_SUPER;
        drawBitmap(canvas, tipRed, button.right() - 120.0f, button.top() - 32.0f, 86.0f, 64.0f);
        if (state.cardUseCount() > 0) {
            drawBitmap(canvas, card, button.right() - 102.0f, button.top() - 18.0f, 35.0f, 34.0f);
            drawText(
                    canvas,
                    String.valueOf(state.cardUseCount()),
                    button.right() - 54.0f,
                    button.top() + 9.0f,
                    28.0f,
                    TEXT_COST,
                    Paint.Align.LEFT);
        } else if (state.diamondUseCount() > 0) {
            drawText(
                    canvas,
                    String.valueOf(state.diamondUseCount()),
                    button.right() - 80.0f,
                    button.top() + 10.0f,
                    30.0f,
                    TEXT_COST,
                    Paint.Align.LEFT);
        }
    }

    private void drawNode(Canvas canvas, Bitmap bitmap, TaizhouMultipleLayout.Node node) {
        drawBitmap(canvas, bitmap, node.left(), node.top(), node.width(), node.height());
    }

    private void drawCenteredInside(
            Canvas canvas, Bitmap bitmap, TaizhouMultipleLayout.Node node) {
        float width = Math.min(node.width(), bitmap.getWidth());
        float height = Math.min(node.height(), bitmap.getHeight());
        drawBitmap(
                canvas,
                bitmap,
                node.centerX() - width / 2.0f,
                node.centerY() - height / 2.0f,
                width,
                height);
    }

    private void drawBitmap(
            Canvas canvas, Bitmap bitmap, float left, float top, float width, float height) {
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, null, new RectF(left, top, left + width, top + height), bitmapPaint);
        }
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float baseline,
            float size,
            int color,
            Paint.Align align) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(align);
        textPaint.setShadowLayer(3.0f, 0.0f, 2.0f, Color.argb(180, 20, 25, 12));
        canvas.drawText(text, x, baseline, textPaint);
        textPaint.clearShadowLayer();
    }

    private static Bitmap bitmap(Context context, int resourceId) {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }
}
