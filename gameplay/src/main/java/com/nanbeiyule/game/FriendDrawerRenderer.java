package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Canvas renderer for the friend drawer chrome, drawn with the original
 * Zhejiang lobby friends artwork: the collapsed vertical tab, the beige
 * panel background, the title label and the two entry rows. List items
 * live in {@link FriendDrawerItemRenderer}. All text copy comes from
 * res/values.
 */
final class FriendDrawerRenderer {
    static final int PRIMARY_TEXT = Color.rgb(107, 62, 30);
    static final int SECONDARY_TEXT = Color.rgb(148, 112, 74);
    /** Empty-state copy size, matched to the original panel's label. */
    static final float EMPTY_TEXT_SIZE = 86.0f;

    /** Visible body of friend_list_bg; the bitmap's right columns are
     * a transparent drop shadow that must not size the panel. */
    private static final Rect PANEL_BG_SOURCE = new Rect(0, 0, 580, 1080);

    private final Context context;
    private final FriendDrawerItemRenderer itemRenderer;
    private final FriendDrawerReadyRenderer readyRenderer;
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap panelBackground;
    private final Bitmap readyBackground;
    private final Bitmap readyTitle;
    private final Bitmap closeArrow;
    private final Bitmap openArrow;
    private final Bitmap messagePoint;
    private final Bitmap tabListInactive;
    private final Bitmap tabListActive;
    private final Bitmap tabStartingInactive;
    private final Bitmap tabStartingActive;
    private final Bitmap tabRecallInactive;
    private final Bitmap tabRecallActive;
    private final Bitmap inviteAllButton;
    private final Bitmap refreshListButton;

    FriendDrawerRenderer(Context context) {
        this.context = context;
        itemRenderer = new FriendDrawerItemRenderer(context);
        readyRenderer = new FriendDrawerReadyRenderer(context);
        panelBackground = decode(context, R.drawable.friend_list_bg);
        readyBackground = decode(context, R.drawable.friend_ready_bg);
        readyTitle = decode(context, R.drawable.friend_ready_title);
        closeArrow = decode(context, R.drawable.friend_btn_open_1);
        openArrow = decode(context, R.drawable.friend_btn_open_2);
        messagePoint = decode(context, R.drawable.friend_message_point);
        tabListInactive = decode(context, R.drawable.friend_lable_list_1);
        tabListActive = decode(context, R.drawable.friend_lable_list_2);
        tabStartingInactive =
                decode(context, R.drawable.friend_lable_follow_1);
        tabStartingActive =
                decode(context, R.drawable.friend_lable_follow_2);
        tabRecallInactive = decode(context, R.drawable.friend_lable_recall_1);
        tabRecallActive = decode(context, R.drawable.friend_lable_recall_2);
        inviteAllButton = decode(context, R.drawable.friend_invite_all);
        refreshListButton =
                decode(context, R.drawable.friend_btn_refresh_list);
    }

    static Bitmap decode(Context context, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(
                context.getResources(), resourceId, options);
    }

    void draw(
            Canvas canvas,
            FriendDrawerState state,
            FriendDrawerLayout layout,
            float expandProgress,
            long nowMillis) {
        if (expandProgress < 1.0f) {
            // The original keeps the ready body on screen underneath the
            // expanding list panel and fades it out as the panel covers it.
            int readyLayer =
                    canvas.saveLayerAlpha(
                            layout.readyBackgroundRect().left - 24.0f,
                            layout.readyBackgroundRect().top - 24.0f,
                            layout.readyBackgroundRect().right + 24.0f,
                            layout.readyBackgroundRect().bottom + 24.0f,
                            Math.round((1.0f - expandProgress) * 255.0f));
            drawReadyPanel(canvas, state, layout, nowMillis);
            canvas.restoreToCount(readyLayer);
        }
        drawHandle(
                canvas, layout, expandProgress,
                state.unreadApplications());
        if (expandProgress <= 0.0f) {
            return;
        }
        RectF panel = layout.panelRect();
        int save = canvas.save();
        canvas.translate(
                -(1.0f - expandProgress) * layout.panelRight(), 0.0f);
        int alpha = Math.round(expandProgress * 255.0f);
        int layer =
                canvas.saveLayerAlpha(
                        panel.left - 24.0f,
                        panel.top,
                        panel.right + 24.0f,
                        panel.bottom,
                        alpha);
        drawPanel(canvas, state, layout, nowMillis);
        canvas.restoreToCount(layer);
        canvas.restoreToCount(save);
    }

    /** Edge handle, mirroring the original state machine: collapsed
     * shows the small open arrow on the ready body's edge; while the
     * panel moves, only the close arrow slides with it. */
    private void drawHandle(
            Canvas canvas,
            FriendDrawerLayout layout,
            float expandProgress,
            int unreadApplications) {
        if (expandProgress <= 0.0f) {
            canvas.drawBitmap(
                    openArrow, null,
                    layout.collapsedArrowRect(), bitmapPaint);
            if (unreadApplications > 0) {
                RectF ready = layout.readyPanelRect();
                canvas.drawBitmap(
                        messagePoint,
                        null,
                        new RectF(
                                ready.right - 44.0f,
                                ready.top - 6.0f,
                                ready.right - 4.0f,
                                ready.top + 34.0f),
                        bitmapPaint);
            }
            return;
        }
        canvas.drawBitmap(
                closeArrow, null,
                layout.arrowRect(expandProgress), bitmapPaint);
    }

    /** Collapsed body: the original {@code _KW_READY_PANEL} with its
     * nine-sliced background, gold title plate and friend rows. */
    private void drawReadyPanel(
            Canvas canvas,
            FriendDrawerState state,
            FriendDrawerLayout layout,
            long nowMillis) {
        RectF body = layout.readyBackgroundRect();
        drawNineSlice(
                canvas,
                readyBackground,
                body,
                48,
                52,
                layout.readyBackgroundInsetX(),
                layout.readyBackgroundInsetY());
        canvas.drawBitmap(
                readyTitle, null, layout.readyTitleRect(), bitmapPaint);
        readyRenderer.drawList(canvas, state, layout, nowMillis);
    }

    /**
     * Draws a nine-sliced bitmap: corners keep their size, edges stretch
     * along one axis and the centre stretches both ways. The ready
     * background is a 117x122 rounded plate that the original stretches
     * to the full panel body.
     */
    private void drawNineSlice(
            Canvas canvas,
            Bitmap bitmap,
            RectF destination,
            int sourceInsetX,
            int sourceInsetY,
            float destinationInsetX,
            float destinationInsetY) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] sourceX = {
            0, sourceInsetX, width - sourceInsetX, width
        };
        int[] sourceY = {
            0, sourceInsetY, height - sourceInsetY, height
        };
        float[] destinationX = {
            destination.left,
            destination.left + destinationInsetX,
            destination.right - destinationInsetX,
            destination.right
        };
        float[] destinationY = {
            destination.top,
            destination.top + destinationInsetY,
            destination.bottom - destinationInsetY,
            destination.bottom
        };
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(
                                sourceX[column], sourceY[row],
                                sourceX[column + 1], sourceY[row + 1]),
                        new RectF(
                                destinationX[column], destinationY[row],
                                destinationX[column + 1],
                                destinationY[row + 1]),
                        bitmapPaint);
            }
        }
    }

    private void drawPanel(
            Canvas canvas,
            FriendDrawerState state,
            FriendDrawerLayout layout,
            long nowMillis) {
        canvas.drawBitmap(panelBackground, PANEL_BG_SOURCE, layout.panelRect(), bitmapPaint);
        drawTabs(canvas, state, layout);
        switch (state.tab()) {
            case STARTING -> {
                drawEmptyMessage(
                        canvas,
                        layout,
                        context.getString(
                                R.string.friend_starting_empty));
                // The original follow tab anchors a refresh-list button
                // at the bottom (View.lua _btnAction); the follow table
                // feed is not built yet, so the button stays dimmed and
                // has no hit region instead of faking a refresh.
                bitmapPaint.setAlpha(110);
                canvas.drawBitmap(
                        refreshListButton, null,
                        layout.refreshListRect(), bitmapPaint);
                bitmapPaint.setAlpha(255);
            }
            case RECALL -> {
                // The original recall tab carries no bottom action slot.
                itemRenderer.drawRecallList(
                        canvas, state, layout, nowMillis);
            }
            default -> {
                itemRenderer.drawList(canvas, state, layout, nowMillis);
                canvas.drawBitmap(
                        inviteAllButton, null,
                        layout.inviteAllRect(), bitmapPaint);
            }
        }
    }

    private void drawTabs(
            Canvas canvas, FriendDrawerState state,
            FriendDrawerLayout layout) {
        Bitmap[] inactive = {
            tabListInactive, tabStartingInactive, tabRecallInactive
        };
        Bitmap[] active = {
            tabListActive, tabStartingActive, tabRecallActive
        };
        int activeIndex = state.tab().ordinal();
        for (int index = 0; index < 3; index++) {
            if (index == activeIndex) {
                continue;
            }
            canvas.drawBitmap(
                    inactive[index], null, layout.tabRect(index),
                    bitmapPaint);
        }
        canvas.drawBitmap(
                active[activeIndex], null,
                layout.tabRect(activeIndex), bitmapPaint);
    }

    private void drawEmptyMessage(
            Canvas canvas, FriendDrawerLayout layout, String message) {
        RectF list = layout.listRect();
        textPaint.setTextSize(EMPTY_TEXT_SIZE);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(SECONDARY_TEXT);
        textPaint.setTextAlign(Paint.Align.CENTER);
        drawCenteredLines(
                canvas, message, list.centerX(), list.centerY(), textPaint);
        textPaint.setFakeBoldText(false);
    }

    /** Draws a possibly multi-line label centred on the given point;
     * the original empty-state copy is hard-wrapped with newlines. */
    static void drawCenteredLines(
            Canvas canvas, String message, float centerX, float centerY,
            Paint paint) {
        String[] lines = message.split("\n");
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float lineHeight = metrics.descent - metrics.ascent;
        float top =
                centerY
                        - lines.length * lineHeight / 2.0f
                        - metrics.ascent;
        for (int index = 0; index < lines.length; index++) {
            canvas.drawText(
                    lines[index], centerX, top + index * lineHeight, paint);
        }
    }

}
