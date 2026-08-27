package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayerLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import java.util.ArrayList;
import java.util.List;

/** Draws the newest quick phrase, original emoji, or voice marker beside its seat. */
final class TaizhouRoomMessageRenderer {
    private static final String ORIGINAL_FONT_ASSET = "fonts/fangzhengcuyuan.ttf";

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap[] emojiIcons = new Bitmap[TaizhouChatEmojiResources.count()];
    private final Bitmap[] bubbleBackgrounds = new Bitmap[TaizhouMahjongTableLayout.SEAT_TOP + 1];

    TaizhouRoomMessageRenderer(Context context) {
        for (int index = 0; index < emojiIcons.length; index++) {
            emojiIcons[index] =
                    BitmapFactory.decodeResource(
                            context.getResources(), TaizhouChatEmojiResources.drawableAt(index));
        }
        for (int localSeat = TaizhouMahjongTableLayout.SEAT_LEFT;
                localSeat <= TaizhouMahjongTableLayout.SEAT_TOP;
                localSeat++) {
            bubbleBackgrounds[localSeat] =
                    BitmapFactory.decodeResource(
                            context.getResources(),
                            TaizhouRoomMessageLayout.backgroundResIdFor(localSeat));
        }
        textPaint.setTypeface(originalTypeface(context));
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
        TaizhouRoomMessagePresentation presentation =
                TaizhouRoomMessagePresentation.from(message);
        float bubbleHeight =
                presentation.kind() == TaizhouRoomMessagePresentation.Kind.TEXT
                        ? textBubbleHeight(presentation.text())
                        : TaizhouRoomMessageLayout.SPEAK_HEIGHT;
        TaizhouRoomMessageLayout.Bubble bubble =
                TaizhouRoomMessageLayout.bubbleFor(
                        localSeat, slot.centerX(), slot.centerY(), bubbleHeight);
        RectF bounds = new RectF(bubble.left(), bubble.top(), bubble.right(), bubble.bottom());
        drawScale9(canvas, bubbleBackgrounds[localSeat], bounds);
        if (presentation.kind() == TaizhouRoomMessagePresentation.Kind.EMOJI
                && presentation.emojiIndex() >= 0
                && presentation.emojiIndex() < emojiIcons.length) {
            canvas.drawBitmap(
                    emojiIcons[presentation.emojiIndex()],
                    null,
                    new RectF(
                            bounds.centerX() - 35.0f,
                            bounds.centerY() - 35.0f,
                            bounds.centerX() + 35.0f,
                            bounds.centerY() + 35.0f),
                    bitmapPaint);
            return;
        }
        String text = presentation.text().isEmpty() ? "[表情]" : presentation.text();
        drawText(canvas, text, bubble, localSeat);
    }

    private void drawText(
            Canvas canvas, String value, TaizhouRoomMessageLayout.Bubble bubble, int localSeat) {
        textPaint.setTextSize(TaizhouRoomMessageLayout.TEXT_SIZE);
        textPaint.setColor(TaizhouRoomMessageLayout.TEXT_COLOR);
        textPaint.setStyle(Paint.Style.FILL);
        List<String> lines = wrappedLines(value, TaizhouRoomMessageLayout.TEXT_AREA_WIDTH);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float lineHeight = metrics.descent - metrics.ascent;
        float centerY = textCenterY(bubble, localSeat);
        float baseline =
                centerY
                        - (metrics.ascent + (lines.size() - 1) * lineHeight + metrics.descent)
                                / 2.0f;
        if (lines.size() == 1) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(
                    lines.get(0), (bubble.left() + bubble.right()) / 2.0f, baseline, textPaint);
        } else {
            textPaint.setTextAlign(Paint.Align.LEFT);
            for (String line : lines) {
                canvas.drawText(
                        line,
                        bubble.left() + TaizhouRoomMessageLayout.TEXT_LEFT_PADDING,
                        baseline,
                        textPaint);
                baseline += lineHeight;
            }
        }
    }

    private float textBubbleHeight(String value) {
        textPaint.setTextSize(TaizhouRoomMessageLayout.TEXT_SIZE);
        List<String> lines = wrappedLines(value, TaizhouRoomMessageLayout.TEXT_AREA_WIDTH);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        return Math.max(
                TaizhouRoomMessageLayout.SPEAK_HEIGHT,
                lines.size() * (metrics.descent - metrics.ascent)
                        + TaizhouRoomMessageLayout.TEXT_VERTICAL_PADDING);
    }

    private List<String> wrappedLines(String value, float width) {
        String text = value == null ? "" : value;
        if (text.isEmpty()) {
            return List.of("");
        }
        List<String> lines = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int count =
                    Math.max(
                            1,
                            textPaint.breakText(
                                    text, start, text.length(), true, width, null));
            lines.add(text.substring(start, start + count));
            start += count;
        }
        return lines;
    }

    private static float textCenterY(TaizhouRoomMessageLayout.Bubble bubble, int localSeat) {
        float topDownPercent =
                localSeat == TaizhouMahjongTableLayout.SEAT_TOP ? 0.6f : 0.4f;
        return bubble.top() + bubble.height() * topDownPercent;
    }

    private void drawScale9(Canvas canvas, Bitmap bitmap, RectF destination) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        int leftCap = Math.round(TaizhouRoomMessageLayout.SCALE9_X);
        int rightCap =
                Math.round(
                        bitmap.getWidth()
                                - TaizhouRoomMessageLayout.SCALE9_X
                                - TaizhouRoomMessageLayout.SCALE9_WIDTH);
        int topCap = Math.round(TaizhouRoomMessageLayout.SCALE9_Y);
        int bottomCap =
                Math.round(
                        bitmap.getHeight()
                                - TaizhouRoomMessageLayout.SCALE9_Y
                                - TaizhouRoomMessageLayout.SCALE9_HEIGHT);
        if (destination.width() < leftCap + rightCap
                || destination.height() < topCap + bottomCap) {
            canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
            return;
        }
        int[] sourceX = {0, leftCap, bitmap.getWidth() - rightCap, bitmap.getWidth()};
        int[] sourceY = {0, topCap, bitmap.getHeight() - bottomCap, bitmap.getHeight()};
        float[] targetX = {
            destination.left,
            destination.left + leftCap,
            destination.right - rightCap,
            destination.right
        };
        float[] targetY = {
            destination.top,
            destination.top + topCap,
            destination.bottom - bottomCap,
            destination.bottom
        };
        Rect source = new Rect();
        RectF target = new RectF();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                source.set(sourceX[x], sourceY[y], sourceX[x + 1], sourceY[y + 1]);
                target.set(targetX[x], targetY[y], targetX[x + 1], targetY[y + 1]);
                canvas.drawBitmap(bitmap, source, target, bitmapPaint);
            }
        }
    }

    private static Typeface originalTypeface(Context context) {
        try {
            return Typeface.createFromAsset(context.getAssets(), ORIGINAL_FONT_ASSET);
        } catch (RuntimeException ignored) {
            return Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        }
    }
}
