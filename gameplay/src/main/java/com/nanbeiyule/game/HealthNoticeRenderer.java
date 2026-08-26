package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

/** Renders the recovered notice as licensed-font text instead of the old rasterized copy. */
final class HealthNoticeRenderer {
    private static final int HEADING_COLOR = 0xFF1AA7AF;
    private static final int BODY_COLOR = 0xFFC97943;
    private static final float CONTENT_PADDING_X = 44f;
    private static final float CONTENT_PADDING_Y = 18f;

    private final RecoveredCommonDialogChrome chrome;
    private final Paint contentBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scrollTrack = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scrollThumb = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final StaticLayout textLayout;

    HealthNoticeRenderer(Context context) {
        chrome = new RecoveredCommonDialogChrome(context);
        contentBackground.setColor(0x72FFF8E3);
        scrollTrack.setColor(0x24A96C47);
        scrollThumb.setColor(0xAAA96C47);

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint.setTypeface(
                Typeface.createFromAsset(
                        context.getAssets(), "fonts/zihun_jingdian_lihei.ttf"));
        textPaint.setTextSize(34f);
        SpannableStringBuilder content = styledContent();
        int width = Math.round(HealthNoticeLayout.CONTENT.width() - 2f * CONTENT_PADDING_X);
        textLayout =
                StaticLayout.Builder.obtain(content, 0, content.length(), textPaint, width)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE)
                        .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
                        .setIncludePad(false)
                        .setLineSpacing(10f, 1f)
                        .build();
    }

    float contentHeight() {
        return textLayout.getHeight() + 2f * CONTENT_PADDING_Y;
    }

    void draw(Canvas canvas, float scroll) {
        chrome.draw(
                canvas,
                HealthNoticeLayout.PANEL_WIDTH,
                HealthNoticeLayout.PANEL_HEIGHT,
                "健康须知",
                HealthNoticeLayout.CLOSE);
        canvas.drawRoundRect(HealthNoticeLayout.CONTENT, 16f, 16f, contentBackground);

        int save = canvas.save();
        canvas.clipRect(HealthNoticeLayout.CONTENT);
        canvas.translate(
                HealthNoticeLayout.CONTENT.left + CONTENT_PADDING_X,
                HealthNoticeLayout.CONTENT.top + CONTENT_PADDING_Y - scroll);
        textLayout.draw(canvas);
        canvas.restoreToCount(save);
        drawScrollBar(canvas, scroll);
    }

    private void drawScrollBar(Canvas canvas, float scroll) {
        float contentHeight = contentHeight();
        if (contentHeight <= HealthNoticeLayout.CONTENT_VIEWPORT_HEIGHT) {
            return;
        }
        RectF track =
                new RectF(
                        HealthNoticeLayout.CONTENT.right - 13f,
                        HealthNoticeLayout.CONTENT.top + 16f,
                        HealthNoticeLayout.CONTENT.right - 7f,
                        HealthNoticeLayout.CONTENT.bottom - 16f);
        canvas.drawRoundRect(track, 3f, 3f, scrollTrack);
        float viewport = HealthNoticeLayout.CONTENT_VIEWPORT_HEIGHT;
        float thumbHeight = Math.max(48f, track.height() * viewport / contentHeight);
        float maxScroll = contentHeight - viewport;
        float top = track.top + (track.height() - thumbHeight) * scroll / maxScroll;
        canvas.drawRoundRect(
                new RectF(track.left - 2f, top, track.right + 2f, top + thumbHeight),
                5f,
                5f,
                scrollThumb);
    }

    private static SpannableStringBuilder styledContent() {
        SpannableStringBuilder text = new SpannableStringBuilder();
        for (HealthNoticeContent.Block block : HealthNoticeContent.blocks()) {
            if (text.length() > 0) {
                text.append("\n\n");
            }
            int start = text.length();
            text.append(block.text());
            int end = text.length();
            text.setSpan(
                    new ForegroundColorSpan(block.heading() ? HEADING_COLOR : BODY_COLOR),
                    start,
                    end,
                    0);
            text.setSpan(
                    new AbsoluteSizeSpan(block.heading() ? 42 : 34),
                    start,
                    end,
                    0);
        }
        return text;
    }
}
