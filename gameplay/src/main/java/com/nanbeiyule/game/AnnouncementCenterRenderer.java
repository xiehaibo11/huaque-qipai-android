package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.util.ArrayList;
import java.util.List;

/** Server-backed announcements rendered inside the original shared ActivityLayer chrome. */
final class AnnouncementCenterRenderer {
    private static final int TITLE_COLOR = Color.rgb(23, 161, 153);
    private static final int CONTENT_COLOR = Color.rgb(185, 115, 69);
    private static final int ERROR_COLOR = Color.rgb(150, 82, 48);
    private final OriginalActivityCenterChrome chrome;
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    AnnouncementCenterRenderer(Context context) {
        chrome = new OriginalActivityCenterChrome(context);
        textPaint.setTypeface(
                Typeface.createFromAsset(context.getAssets(), "fonts/zihun_jingdian_lihei.ttf"));
    }

    void draw(
            Canvas canvas,
            AnnouncementCenterState state,
            float listScroll,
            float detailScroll) {
        boolean pageDetail =
                state.detail() != null
                        && AnnouncementPageUrlPolicy.isSafe(state.detail().pageUrl());
        chrome.draw(canvas, OriginalActivityCenterChrome.SelectedTab.ANNOUNCEMENT, !pageDetail);
        if (state.pageState() == AnnouncementCenterState.PageState.CONTENT) {
            drawList(canvas, state, listScroll);
            drawDetail(canvas, state, detailScroll);
        } else if (state.pageState() == AnnouncementCenterState.PageState.ERROR) {
            drawError(canvas, state.message());
        }
    }

    float detailContentHeight(AnnouncementApiProtocol.AnnouncementDetail detail) {
        if (detail == null || detail.bodyText().isBlank()) return 0f;
        return wrappedLines(detail.bodyText(), AnnouncementCenterLayout.DETAIL_BODY.width(), 40f)
                        .size()
                * 55f;
    }

    private void drawList(Canvas canvas, AnnouncementCenterState state, float scroll) {
        int save = canvas.save();
        canvas.clipRect(rect(AnnouncementCenterLayout.LIST));
        List<AnnouncementApiProtocol.AnnouncementSummary> items = state.announcements();
        for (int index = 0; index < items.size(); index++) {
            AnnouncementCenterLayout.Box row = AnnouncementCenterLayout.rowRect(index, scroll);
            chrome.drawRow(
                    canvas,
                    new AdaptiveViewport.Rect(row.left(), row.top(), row.right(), row.bottom()),
                    ellipsize(items.get(index).title(), 285f, 42f),
                    items.get(index).announcementId() == state.selectedAnnouncementId(),
                    textPaint);
        }
        canvas.restoreToCount(save);
    }

    private void drawDetail(Canvas canvas, AnnouncementCenterState state, float scroll) {
        AnnouncementApiProtocol.AnnouncementDetail detail = state.detail();
        if (detail == null) return;
        String headline = detail.subtitle().isBlank() ? detail.title() : detail.subtitle();
        text(canvas, headline, 513f, 218f, 42f, TITLE_COLOR, Paint.Align.LEFT, false);
        if (AnnouncementPageUrlPolicy.isSafe(detail.pageUrl())) {
            drawButton(canvas, AnnouncementCenterLayout.OPEN_PAGE, "查看详情");
            return;
        }
        int save = canvas.save();
        canvas.clipRect(rect(AnnouncementCenterLayout.DETAIL_BODY));
        float y = AnnouncementCenterLayout.DETAIL_BODY.top() + 28f - scroll;
        for (String line :
                wrappedLines(detail.bodyText(), AnnouncementCenterLayout.DETAIL_BODY.width(), 40f)) {
            text(canvas, line, 513f, y, 40f, CONTENT_COLOR, Paint.Align.LEFT, false);
            y += 55f;
        }
        canvas.restoreToCount(save);
    }

    private void drawError(Canvas canvas, String message) {
        text(
                canvas,
                message,
                AnnouncementCenterLayout.DETAIL.centerX(),
                510f,
                34f,
                ERROR_COLOR,
                Paint.Align.CENTER,
                false);
        drawButton(canvas, AnnouncementCenterLayout.RETRY, "重新加载");
    }

    private void drawButton(Canvas canvas, AnnouncementCenterLayout.Box box, String label) {
        buttonPaint.setColor(Color.rgb(221, 151, 52));
        canvas.drawRoundRect(rect(box), 18f, 18f, buttonPaint);
        text(
                canvas,
                label,
                box.centerX(),
                box.centerY(),
                34f,
                Color.WHITE,
                Paint.Align.CENTER,
                true);
    }

    private List<String> wrappedLines(String value, float width, float size) {
        List<String> lines = new ArrayList<>();
        if (value == null || value.isBlank()) return lines;
        configureText(size, CONTENT_COLOR, Paint.Align.LEFT, false);
        for (String paragraph : value.replace("\r", "").split("\n", -1)) {
            String remaining = paragraph;
            if (remaining.isEmpty()) {
                lines.add("");
                continue;
            }
            while (!remaining.isEmpty()) {
                int count = Math.max(1, textPaint.breakText(remaining, true, width, null));
                lines.add(remaining.substring(0, count));
                remaining = remaining.substring(count);
            }
        }
        return lines;
    }

    private String ellipsize(String value, float width, float size) {
        String result = value == null ? "" : value;
        configureText(size, CONTENT_COLOR, Paint.Align.LEFT, false);
        if (textPaint.measureText(result) <= width) return result;
        float available = width - textPaint.measureText("…");
        int count = Math.max(0, textPaint.breakText(result, true, available, null));
        return result.substring(0, count) + "…";
    }

    private void text(
            Canvas canvas,
            String value,
            float x,
            float centerY,
            float size,
            int color,
            Paint.Align align,
            boolean bold) {
        configureText(size, color, align, bold);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) * 0.5f;
        canvas.drawText(value == null ? "" : value, x, baseline, textPaint);
    }

    private void configureText(float size, int color, Paint.Align align, boolean bold) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(align);
        textPaint.setFakeBoldText(bold);
    }

    private static RectF rect(AnnouncementCenterLayout.Box box) {
        return new RectF(box.left(), box.top(), box.right(), box.bottom());
    }
}
