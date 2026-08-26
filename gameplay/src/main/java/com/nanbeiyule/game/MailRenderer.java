package com.nanbeiyule.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Canvas renderer for the native mail page. Geometry and bitmaps come from the recovered
 * NewGoldHall mail CSD and texture atlases.
 */
final class MailRenderer {
    // 颜色取样自原版实机截图空态文字与按钮文本（推断）。
    private static final int EMPTY_TEXT_COLOR = Color.rgb(88, 100, 119);
    private static final int BUTTON_YELLOW_TEXT_STROKE = Color.rgb(196, 132, 32);

    private final MailDrawableSet drawables;
    private final Resources resources;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint panelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect source = new Rect();
    private boolean animatedMailboxVisible;
    private long rowEntranceStartedNanos = System.nanoTime();

    MailRenderer(Context context) {
        resources = context.getResources();
        drawables = new MailDrawableSet(resources);
        Typeface typeface = Typeface.createFromAsset(
                context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        textPaint.setTypeface(typeface);
    }

    void setAnimatedMailboxVisible(boolean visible) {
        animatedMailboxVisible = visible;
    }

    void restartRowEntrance() {
        rowEntranceStartedNanos = System.nanoTime();
    }

    boolean rowEntranceRunning(int count) {
        return rowEntranceElapsed() < MailRowMotion.totalDuration(count);
    }

    void draw(
            Canvas canvas,
            MailState state,
            float scroll,
            boolean loading,
            String errorMessage) {
        drawBitmap(canvas, drawables.paperPanel, MailLayout.PAPER);
        drawBitmap(canvas, drawables.backArrow, MailLayout.CLOSE_ARROW);
        drawBitmap(canvas, drawables.title, MailLayout.CLOSE_TITLE);
        if (!animatedMailboxVisible) {
            drawBitmap(canvas, drawables.youxiang, MailLayout.MAILBOX);
        }
        // 气泡恒可见，文字标按 Lua changeMailState 切换 zi1（您有信件）/zi2（空空如也）。
        drawBitmap(canvas, drawables.tips, MailLayout.TIPS);
        boolean empty = state.mails().isEmpty();
        drawBitmap(canvas, empty ? drawables.ziEmpty : drawables.ziHave,
                empty ? MailLayout.ZI_EMPTY : MailLayout.ZI_HAVE);

        if (empty && !loading) {
            drawBitmap(canvas, drawables.emptyBox, MailLayout.KZT);
            drawCenteredText(canvas, resources.getString(R.string.mail_empty_hint),
                    MailLayout.EMPTY_TEXT.centerX(), MailLayout.EMPTY_TEXT.centerY(),
                    40f, EMPTY_TEXT_COLOR);
        } else {
            canvas.save();
            MailLayout.Box viewport = MailLayout.LIST;
            canvas.clipRect(viewport.left(), viewport.top(),
                    viewport.right(), viewport.bottom());
            List<MailApiProtocol.MailEntry> mails = state.mails();
            Instant now = Instant.now();
            float entranceElapsed = rowEntranceElapsed();
            for (int index = 0; index < mails.size(); index++) {
                MailLayout.Box row = MailLayout.rowRect(index, mails.size(), scroll);
                MailRowMotion.Frame motion = MailRowMotion.frame(index, entranceElapsed);
                int checkpoint = canvas.saveLayerAlpha(
                        new RectF(row.left(), row.top(), row.right(), row.bottom() + 150f),
                        Math.round(255f * motion.alpha()));
                canvas.translate(0f, motion.offsetY());
                drawRow(canvas, state, mails.get(index), index, row, now);
                canvas.restoreToCount(checkpoint);
            }
            canvas.restore();
        }
        if (MailLayout.bottomButtonsVisible(state.mails().size())) {
            drawBottomButtons(canvas, state);
        }
        drawLoadingOrError(canvas, loading, errorMessage);
    }

    void drawFullBleedBackground(Canvas canvas, int width, int height) {
        drawBitmap(canvas, drawables.background, new RectF(0f, 0f, width, height));
    }

    private void drawRow(Canvas canvas, MailState state,
            MailApiProtocol.MailEntry entry, int index, MailLayout.Box row, Instant now) {
        // 行底 Img_zj_tiao（原版帧 1294x44，CSB 节点拉伸为 1294x160）。
        drawBitmap(canvas, drawables.rowBackground, row);
        // Lua updateMailInfo：红点只在未读时显示。
        if (!entry.read()) {
            drawBitmap(canvas, drawables.redPoint,
                    MailLayout.rowChild(row, MailLayout.RED_POINT_LOCAL));
        }
        if (state.selectMode()) {
            drawCheckbox(canvas, MailLayout.rowChild(row, MailLayout.CHECKBOX_LOCAL),
                    state.isSelected(entry.mailId()));
        }
        // Lua updateMailInfo：未读用 Img_mail_yj_1，已读用 Img_mail_yj_2。
        drawBitmap(canvas, entry.read() ? drawables.iconTwo : drawables.iconOne,
                MailLayout.rowChild(row, MailLayout.ICON_LOCAL));
        if (entry.hasAttachment() && !entry.claimed()) {
            drawBitmap(canvas, drawables.awardBadge,
                    MailLayout.rowChild(row, MailLayout.AWARD_BADGE_LOCAL));
        }
        int titleColor = entry.read()
                ? Color.rgb(146, 134, 114) : Color.rgb(74, 59, 42);
        drawLeftText(canvas, entry.title(),
                row.left() + MailLayout.TITLE_LOCAL_LEFT,
                row.top() + MailLayout.TITLE_LOCAL_CENTER_Y, 44f, titleColor);
        drawLeftText(canvas, entry.intro(), row.left() + MailLayout.DESC_LOCAL_LEFT,
                row.top() + MailLayout.DESC_LOCAL_CENTER_Y, 36f, Color.rgb(138, 123, 99));
        String remaining = MailState.remainingText(entry, now);
        if (!remaining.isEmpty()) {
            drawBitmap(canvas, drawables.timeIcon,
                    MailLayout.rowChild(row, MailLayout.TIME_ICON_LOCAL));
            drawLeftText(canvas, remaining, row.left() + MailLayout.TIME_TEXT_LOCAL_LEFT,
                    row.top() + MailLayout.TIME_LOCAL_CENTER_Y, 28f,
                    remainingColor(entry, now));
        }
    }

    /** 勾选框 Btn_mail_off 41x41；选中时叠加 Btn_mail_on 28x22 对勾（居中）。 */
    private void drawCheckbox(Canvas canvas, MailLayout.Box box, boolean selected) {
        drawBitmap(canvas, drawables.checkboxOff, box);
        if (selected) {
            Bitmap on = drawables.checkboxOn;
            drawBitmap(canvas, on, new RectF(
                    box.centerX() - 14f, box.centerY() - 11f,
                    box.centerX() + 14f, box.centerY() + 11f));
        }
    }

    /**
     * 底部三按钮 306x105：全选删除/全部已读用 Btn_mail_lv 蓝按钮、一键领取用
     * Btn_mail_yjlq 黄按钮，文本为 CSB Text_15 子节点（195x56 居中，推断白色，
     * 黄按钮加棕描边保持可读）；Lua 中按钮恒可用，无灰色禁用态。
     */
    private void drawBottomButtons(Canvas canvas, MailState state) {
        drawButton(canvas, MailLayout.BTN_DELETE_ALL, drawables.buttonBlue,
                state.selectMode()
                        ? resources.getString(R.string.mail_delete)
                        : resources.getString(R.string.mail_select_delete),
                false);
        drawButton(canvas, MailLayout.BTN_READ_ALL, drawables.buttonBlue,
                resources.getString(R.string.mail_read_all), false);
        drawButton(canvas, MailLayout.BTN_CLAIM_ALL, drawables.buttonYellow,
                resources.getString(R.string.mail_claim_all), true);
    }

    private void drawButton(Canvas canvas, MailLayout.Box box, Bitmap bitmap,
            String label, boolean strokeText) {
        drawBitmap(canvas, bitmap, box);
        if (strokeText) {
            configureText(38f, BUTTON_YELLOW_TEXT_STROKE, Paint.Align.CENTER);
            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(5f);
            textPaint.setFakeBoldText(true);
            canvas.drawText(label, box.centerX(), baselineAt(box.centerY(), 38f), textPaint);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setFakeBoldText(false);
        }
        drawCenteredText(canvas, label, box.centerX(), box.centerY(), 38f, Color.WHITE);
    }

    private int remainingColor(MailApiProtocol.MailEntry entry, Instant now) {
        if (entry.expireTime() == null || now == null) {
            return Color.rgb(27, 148, 104);
        }
        long seconds = Duration.between(now, entry.expireTime()).getSeconds();
        long days = seconds / 86_400L;
        return days > 7 ? Color.rgb(27, 148, 104) : Color.rgb(213, 101, 71);
    }

    private float rowEntranceElapsed() {
        return (System.nanoTime() - rowEntranceStartedNanos) / 1_000_000_000f;
    }

    private void drawLoadingOrError(Canvas canvas, boolean loading, String errorMessage) {
        if (!loading && (errorMessage == null || errorMessage.isBlank())) {
            return;
        }
        RectF panel = new RectF(720f, 555f, 1320f, 685f);
        panelPaint.setColor(Color.argb(205, 28, 43, 88));
        canvas.drawRoundRect(panel, 26f, 26f, panelPaint);
        String text = loading
                ? resources.getString(R.string.mail_loading) : errorMessage;
        drawCenteredText(canvas, text, panel.centerX(), panel.centerY(),
                38f, Color.WHITE);
    }

    private void drawCenteredText(
            Canvas canvas, String text, float x, float centerY, float size, int color) {
        configureText(size, color, Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        canvas.drawText(text, x, baselineAt(centerY, size), textPaint);
        textPaint.setFakeBoldText(false);
    }

    private void drawLeftText(
            Canvas canvas, String text, float x, float centerY, float size, int color) {
        configureText(size, color, Paint.Align.LEFT);
        canvas.drawText(text, x, baselineAt(centerY, size), textPaint);
    }

    private float baselineAt(float centerY, float size) {
        textPaint.setTextSize(size);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        return centerY - (metrics.ascent + metrics.descent) * 0.5f;
    }

    private void configureText(float size, int color, Paint.Align align) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(align);
        textPaint.setStyle(Paint.Style.FILL);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, MailLayout.Box box) {
        drawBitmap(canvas, bitmap, rect(box));
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        source.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawBitmap(bitmap, source, destination, bitmapPaint);
    }

    private static RectF rect(MailLayout.Box box) {
        return new RectF(box.left(), box.top(), box.right(), box.bottom());
    }
}
