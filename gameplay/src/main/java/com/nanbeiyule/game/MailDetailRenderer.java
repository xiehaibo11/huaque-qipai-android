package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class MailDetailRenderer {
    private static final int TEXT_COLOR = Color.rgb(71, 78, 105);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private final MailDetailDrawableSet drawables;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect source = new Rect();

    MailDetailRenderer(Context context) {
        drawables = new MailDetailDrawableSet(context.getResources());
        textPaint.setTypeface(Typeface.createFromAsset(
                context.getAssets(), "fonts/zihun_jingdian_lihei.ttf"));
    }

    void draw(Canvas canvas, MailApiProtocol.MailDetail detail) {
        MailApiProtocol.MailEntry entry = detail.entry();
        drawBitmap(canvas, drawables.close, centered(MailLayout.DETAIL_CLOSE, 60f, 60f));
        drawText(canvas, entry.title(), 1027f, 230f, 50f, Paint.Align.CENTER, TEXT_COLOR);
        drawSendTime(canvas, entry.sendTime());
        drawContent(canvas, detail);
        drawExpireTime(canvas, entry.expireTime());
        drawAwards(canvas, detail.attachments(), entry.claimed());
        drawButtons(canvas, detail);
    }

    private void drawSendTime(Canvas canvas, Instant sendTime) {
        String value = sendTime == null ? "" : TIME_FORMAT.format(sendTime);
        drawText(canvas, value, 1027f, 281f, 28f, Paint.Align.CENTER, Color.rgb(115, 132, 160));
        float halfText = Math.max(95f, textPaint.measureText(value) * 0.5f + 28f);
        drawBitmap(canvas, drawables.separator,
                new RectF(1027f - halfText - 210f, 264.5f, 1027f - halfText, 297.5f));
        canvas.save();
        canvas.scale(-1f, 1f, 1027f, 0f);
        drawBitmap(canvas, drawables.separator,
                new RectF(1027f - halfText - 210f, 264.5f, 1027f - halfText, 297.5f));
        canvas.restore();
    }

    private void drawContent(Canvas canvas, MailApiProtocol.MailDetail detail) {
        MailLayout.Box box = MailLayout.DETAIL_CONTENT;
        float bottom = detail.attachments().isEmpty() ? box.bottom() : box.top() + 320f;
        textPaint.setTextSize(38f);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextAlign(Paint.Align.LEFT);
        Spanned content = Html.fromHtml(detail.content(), Html.FROM_HTML_MODE_LEGACY);
        StaticLayout layout = new StaticLayout(
                content,
                textPaint,
                Math.round(box.width()),
                Layout.Alignment.ALIGN_NORMAL,
                1.12f,
                0f,
                false);
        canvas.save();
        canvas.clipRect(box.left(), box.top(), box.right(), bottom);
        canvas.translate(box.left(), box.top());
        layout.draw(canvas);
        canvas.restore();
    }

    private void drawExpireTime(Canvas canvas, Instant expireTime) {
        if (expireTime == null) return;
        drawText(canvas, "有效期至：" + TIME_FORMAT.format(expireTime),
                1650f, 678f, 28f, Paint.Align.RIGHT, Color.rgb(115, 132, 160));
    }

    private void drawAwards(
            Canvas canvas, List<MailApiProtocol.MailAttachment> attachments, boolean claimed) {
        if (attachments.isEmpty()) return;
        int count = Math.min(5, attachments.size());
        float width = count * 160f;
        float left = MailLayout.DETAIL_AWARD_LIST.centerX() - width * 0.5f;
        for (int index = 0; index < count; index++) {
            drawAward(canvas, attachments.get(index), claimed, left + index * 160f);
        }
    }

    private void drawAward(
            Canvas canvas, MailApiProtocol.MailAttachment attachment, boolean claimed, float left) {
        drawBitmap(canvas, drawables.awardBackground,
                new RectF(left + 4f, 695f, left + 156f, 847f));
        Bitmap icon = drawables.rewardIcon(attachment.rewardType());
        if (icon != null) {
            drawBitmap(canvas, icon, new RectF(left + 30f, 715f, left + 130f, 815f));
        }
        drawText(canvas, "x" + attachment.amount(), left + 146f, 814f,
                28f, Paint.Align.RIGHT, Color.WHITE);
        String label = attachment.description().isBlank()
                ? attachment.rewardType() : attachment.description();
        drawText(canvas, label, left + 80f, 872f,
                25f, Paint.Align.CENTER, Color.rgb(71, 78, 105));
        if (claimed) {
            drawBitmap(canvas, drawables.claimedMask,
                    new RectF(left + 4f, 695f, left + 156f, 847f));
            drawBitmap(canvas, drawables.claimedStamp,
                    new RectF(left + 16f, 720f, left + 144f, 821f));
        }
    }

    private void drawButtons(Canvas canvas, MailApiProtocol.MailDetail detail) {
        boolean hasAwards = !detail.attachments().isEmpty();
        if (!hasAwards) {
            drawButton(canvas, MailLayout.DETAIL_DELETE_ONLY, drawables.blueButton, "删除", false);
            return;
        }
        drawButton(canvas, MailLayout.DETAIL_DELETE, drawables.blueButton, "删除", false);
        drawButton(canvas, MailLayout.DETAIL_CLAIM,
                detail.entry().claimed() ? drawables.blueButton : drawables.yellowButton,
                detail.entry().claimed() ? "已领取" : "领取", detail.entry().claimed());
    }

    private void drawButton(
            Canvas canvas, MailLayout.Box box, Bitmap bitmap, String text, boolean disabled) {
        bitmapPaint.setAlpha(disabled ? 160 : 255);
        drawBitmap(canvas, bitmap, new RectF(box.left(), box.top(), box.right(), box.bottom()));
        bitmapPaint.setAlpha(255);
        drawText(canvas, text, box.centerX(), box.centerY(), 38f,
                Paint.Align.CENTER, disabled ? Color.LTGRAY : Color.WHITE);
    }

    private void drawText(
            Canvas canvas, String text, float x, float centerY, float size,
            Paint.Align align, int color) {
        textPaint.setTextSize(size);
        textPaint.setTextAlign(align);
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(text, x, centerY - (metrics.ascent + metrics.descent) * 0.5f, textPaint);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }

    private static RectF centered(MailLayout.Box box, float width, float height) {
        return new RectF(
                box.centerX() - width * 0.5f,
                box.centerY() - height * 0.5f,
                box.centerX() + width * 0.5f,
                box.centerY() + height * 0.5f);
    }
}
