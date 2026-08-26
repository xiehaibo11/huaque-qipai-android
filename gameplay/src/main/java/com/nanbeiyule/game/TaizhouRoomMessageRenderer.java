package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayerLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;

/** Draws the newest quick phrase, original emoji, or voice marker beside its seat. */
final class TaizhouRoomMessageRenderer {
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap[] emojiIcons = new Bitmap[TaizhouChatEmojiResources.count()];

    TaizhouRoomMessageRenderer(Context context) {
        for (int index = 0; index < emojiIcons.length; index++) {
            emojiIcons[index] =
                    BitmapFactory.decodeResource(
                            context.getResources(), TaizhouChatEmojiResources.drawableAt(index));
        }
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
    }

    void draw(
            Canvas canvas,
            TaizhouRoomToolsState.Message message,
            GameplayTableState tableState) {
        int localSeat;
        try {
            localSeat =
                    TaizhouMahjongSeatMapper.toLocalSeat(
                            message.senderSeat(), tableState.mySeat(), tableState.chairCount());
        } catch (IllegalArgumentException exception) {
            return;
        }
        TaizhouMahjongPlayerLayout.PlayerSlot slot =
                TaizhouMahjongPlayerLayout.forLocalSeat(localSeat);
        float centerX =
                localSeat == TaizhouMahjongTableLayout.SEAT_RIGHT
                        ? slot.centerX() - 190.0f
                        : slot.centerX() + 190.0f;
        float centerY = slot.centerY() + 10.0f;
        RectF bubble = new RectF(centerX - 190, centerY - 45, centerX + 190, centerY + 45);
        bubblePaint.setColor(Color.argb(235, 255, 246, 211));
        bubblePaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(bubble, 18, 18, bubblePaint);
        bubblePaint.setStyle(Paint.Style.STROKE);
        bubblePaint.setStrokeWidth(3);
        bubblePaint.setColor(Color.rgb(186, 134, 58));
        canvas.drawRoundRect(bubble, 18, 18, bubblePaint);

        TaizhouRoomMessagePresentation presentation =
                TaizhouRoomMessagePresentation.from(message);
        if (presentation.kind() == TaizhouRoomMessagePresentation.Kind.EMOJI
                && presentation.emojiIndex() >= 0
                && presentation.emojiIndex() < emojiIcons.length) {
            canvas.drawBitmap(
                    emojiIcons[presentation.emojiIndex()],
                    null,
                    new RectF(centerX - 35, centerY - 35, centerX + 35, centerY + 35),
                    bitmapPaint);
            return;
        }
        textPaint.setTextSize(27.0f);
        textPaint.setColor(Color.rgb(99, 66, 35));
        String text = presentation.text().isEmpty() ? "[表情]" : presentation.text();
        canvas.drawText(text, centerX, centerY + 10, textPaint);
    }
}
