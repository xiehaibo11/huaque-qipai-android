package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Renders the friend list portion of the drawer with the original
 * Zhejiang lobby friends artwork: circular avatars under the golden
 * head frame (default silhouette when the friend has no avatarKey),
 * vertical state badges, the round invite button and the shield ribbon.
 */
final class FriendDrawerItemRenderer {
    private final Context context;
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path reusablePath = new Path();
    private final Bitmap defaultHead;
    private final Bitmap headFrame;
    private final Bitmap stateOnline;
    private final Bitmap stateOffline;
    private final Bitmap inviteButton;
    private final Bitmap shieldIcon;
    private final Bitmap recallButton;

    FriendDrawerItemRenderer(Context context) {
        this.context = context;
        defaultHead =
                FriendDrawerRenderer.decode(
                        context, R.drawable.friend_defult_head_img);
        headFrame =
                FriendDrawerRenderer.decode(
                        context, R.drawable.friend_head_frame);
        stateOnline =
                FriendDrawerRenderer.decode(
                        context, R.drawable.friend_state_on_line);
        stateOffline =
                FriendDrawerRenderer.decode(
                        context, R.drawable.friend_state_off_line);
        inviteButton =
                FriendDrawerRenderer.decode(
                        context, R.drawable.friend_action_online_invite);
        shieldIcon =
                FriendDrawerRenderer.decode(
                        context, R.drawable.friend_shield_icon);
        // Original action art for an offline friend (View.lua state 1):
        // the round wechat-invite button, reused for the recall action.
        recallButton =
                FriendDrawerRenderer.decode(
                        context, R.drawable.friend_action_wechat_invite);
    }

    void drawList(
            Canvas canvas,
            FriendDrawerState state,
            FriendDrawerLayout layout,
            long nowMillis) {
        RectF list = layout.listRect();
        if (state.friends().isEmpty()) {
            textPaint.setTextSize(FriendDrawerRenderer.EMPTY_TEXT_SIZE);
            textPaint.setFakeBoldText(true);
            textPaint.setColor(FriendDrawerRenderer.SECONDARY_TEXT);
            textPaint.setTextAlign(Paint.Align.CENTER);
            String message =
                    state.loadingInitial()
                            ? context.getString(R.string.friend_loading)
                            : context.getString(
                                    R.string.friend_list_empty);
            FriendDrawerRenderer.drawCenteredLines(
                    canvas, message, list.centerX(), list.centerY(),
                    textPaint);
            textPaint.setFakeBoldText(false);
            return;
        }
        int save = canvas.save();
        canvas.clipRect(list);
        canvas.translate(0.0f, -state.scrollOffset());
        for (int index = 0; index < state.friends().size(); index++) {
            RectF item = layout.itemRect(index);
            if (item.bottom - state.scrollOffset() < list.top
                    || item.top - state.scrollOffset() > list.bottom) {
                continue;
            }
            drawItem(
                    canvas, layout, item, state.friends().get(index),
                    state, nowMillis);
        }
        if (state.loadingMore()) {
            textPaint.setTextSize(
                    26.0f * FriendDrawerLayout.ITEM_CONTENT_SCALE);
            textPaint.setColor(FriendDrawerRenderer.SECONDARY_TEXT);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(
                    context.getString(R.string.friend_loading),
                    list.centerX(),
                    baseline(
                            layout.itemRect(state.friends().size()).top
                                    + 40.0f),
                    textPaint);
        }
        canvas.restoreToCount(save);
    }

    /** Recall tab: offline friends with the round recall button. */
    void drawRecallList(
            Canvas canvas,
            FriendDrawerState state,
            FriendDrawerLayout layout,
            long nowMillis) {
        RectF list = layout.listRect();
        java.util.List<FriendEntry> candidates =
                state.recallCandidates(nowMillis);
        if (candidates.isEmpty()) {
            textPaint.setTextSize(FriendDrawerRenderer.EMPTY_TEXT_SIZE);
            textPaint.setFakeBoldText(true);
            textPaint.setColor(FriendDrawerRenderer.SECONDARY_TEXT);
            textPaint.setTextAlign(Paint.Align.CENTER);
            FriendDrawerRenderer.drawCenteredLines(
                    canvas,
                    context.getString(R.string.friend_recall_empty),
                    list.centerX(), list.centerY(), textPaint);
            textPaint.setFakeBoldText(false);
            return;
        }
        int save = canvas.save();
        canvas.clipRect(list);
        for (int index = 0; index < candidates.size(); index++) {
            RectF item = layout.itemRect(index);
            FriendEntry friend = candidates.get(index);
            drawAvatar(canvas, layout, item, state, friend);
            if (friend.shielded()) {
                canvas.drawBitmap(
                        shieldIcon, null, layout.shieldRect(item),
                        bitmapPaint);
            }
            RectF button = layout.inviteButtonRect(item);
            float scale = FriendDrawerLayout.ITEM_CONTENT_SCALE;
            drawFittedText(
                    canvas,
                    friend.displayName(),
                    layout.textLeft(),
                    baseline(item.top + 62.0f * scale),
                    button.left - 12.0f * scale - layout.textLeft(),
                    36.0f * scale,
                    FriendDrawerRenderer.PRIMARY_TEXT);
            canvas.drawBitmap(
                    stateOffline, null, layout.stateBadgeRect(item),
                    bitmapPaint);
            RectF badge = layout.stateBadgeRect(item);
            drawFittedText(
                    canvas,
                    presenceText(friend, nowMillis),
                    badge.right + 12.0f * scale,
                    baseline(item.top + 116.0f * scale),
                    button.left - badge.right - 24.0f * scale,
                    26.0f * scale,
                    FriendDrawerRenderer.SECONDARY_TEXT);
            // The recall tab lists friends offline past the seven-day
            // threshold; the rewarded-recall label sits above the
            // recall button like the original panel.
            textPaint.setTextSize(24.0f * scale);
            textPaint.setColor(
                    context.getResources()
                            .getColor(R.color.friend_invite_text));
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(
                    context.getString(R.string.friend_recall_reward),
                    button.centerX(),
                    baseline(item.top + 30.0f * scale),
                    textPaint);
            canvas.drawBitmap(recallButton, null, button, bitmapPaint);
        }
        canvas.restoreToCount(save);
    }

    private void drawItem(
            Canvas canvas,
            FriendDrawerLayout layout,
            RectF item,
            FriendEntry friend,
            FriendDrawerState state,
            long nowMillis) {
        drawAvatar(canvas, layout, item, state, friend);
        if (friend.shielded()) {
            canvas.drawBitmap(
                    shieldIcon, null, layout.shieldRect(item),
                    bitmapPaint);
        }

        boolean offline = friend.state() == FriendEntry.State.OFFLINE;
        float scale = FriendDrawerLayout.ITEM_CONTENT_SCALE;
        // Both states anchor an action button at the invite slot: the
        // online invite art or the offline wechat-invite recall art.
        float nameRight = layout.inviteButtonRect(item).left - 12.0f * scale;
        drawFittedText(
                canvas,
                friend.displayName(),
                layout.textLeft(),
                baseline(item.top + 62.0f * scale),
                nameRight - layout.textLeft(),
                36.0f * scale,
                FriendDrawerRenderer.PRIMARY_TEXT);

        canvas.drawBitmap(
                offline ? stateOffline : stateOnline,
                null,
                layout.stateBadgeRect(item),
                bitmapPaint);
        if (offline) {
            RectF badge = layout.stateBadgeRect(item);
            drawFittedText(
                    canvas,
                    presenceText(friend, nowMillis),
                    badge.right + 12.0f * scale,
                    baseline(item.top + 116.0f * scale),
                    nameRight - badge.right - 12.0f * scale,
                    26.0f * scale,
                    FriendDrawerRenderer.SECONDARY_TEXT);
        }

        if (!offline) {
            // Friends inside the per-friend invite cooldown draw the
            // invite button dimmed; the click still dispatches so the
            // existing cooldown toast explains the state.
            boolean coolingDown =
                    state.inviteCoolingDown(friend.publicPlayerId());
            if (coolingDown) {
                bitmapPaint.setAlpha(110);
            }
            canvas.drawBitmap(
                    inviteButton, null,
                    layout.inviteButtonRect(item), bitmapPaint);
            if (coolingDown) {
                bitmapPaint.setAlpha(255);
            }
        } else {
            // Offline friends show the original wechat-invite action
            // art; the tap dispatches the in-app recall notification.
            canvas.drawBitmap(
                    recallButton, null,
                    layout.inviteButtonRect(item), bitmapPaint);
        }
    }

    private void drawAvatar(
            Canvas canvas,
            FriendDrawerLayout layout,
            RectF item,
            FriendDrawerState state,
            FriendEntry friend) {
        Bitmap bitmap = state.avatar(friend.avatarKey());
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = defaultHead;
        }
        float avatarX = layout.avatarCenterX();
        float avatarY = item.centerY();
        float radius = layout.avatarRadius();
        int save = canvas.save();
        reusablePath.reset();
        reusablePath.addCircle(
                avatarX, avatarY, radius, Path.Direction.CW);
        canvas.clipPath(reusablePath);
        canvas.drawBitmap(
                bitmap,
                null,
                new RectF(
                        avatarX - radius, avatarY - radius,
                        avatarX + radius, avatarY + radius),
                bitmapPaint);
        canvas.restoreToCount(save);
        canvas.drawBitmap(
                headFrame, null, layout.avatarFrameRect(item),
                bitmapPaint);
    }

    /** Mirrors the original getTimeLen: ceiled hours within a day,
     * ceiled days within a week, then a fixed "7天前在线". */
    private String presenceText(FriendEntry friend, long nowMillis) {
        Long at =
                FriendEntry.lastActiveAtMillis(friend.lastActiveAt());
        if (at == null || at >= nowMillis) {
            return context.getString(R.string.friend_last_active_long);
        }
        long elapsed = nowMillis - at;
        if (elapsed <= 86_400_000L) {
            long hours = (elapsed + 3_599_999L) / 3_600_000L;
            return context.getString(
                    R.string.friend_last_active_hours,
                    Math.max(1L, hours));
        }
        if (elapsed <= 7L * 86_400_000L) {
            long days = (elapsed + 86_399_999L) / 86_400_000L;
            return context.getString(
                    R.string.friend_last_active_days, days);
        }
        return context.getString(R.string.friend_last_active_long);
    }

    private void drawFittedText(
            Canvas canvas,
            String value,
            float x,
            float baseline,
            float maximumWidth,
            float size,
            int color) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        textPaint.setTextAlign(Paint.Align.LEFT);
        String text = value == null ? "" : value;
        while (textPaint.getTextSize() > 18.0f
                && textPaint.measureText(text) > maximumWidth) {
            textPaint.setTextSize(textPaint.getTextSize() - 1.0f);
        }
        canvas.drawText(text, x, baseline, textPaint);
    }

    private float baseline(float centerY) {
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        return centerY - (metrics.ascent + metrics.descent) / 2.0f;
    }
}
