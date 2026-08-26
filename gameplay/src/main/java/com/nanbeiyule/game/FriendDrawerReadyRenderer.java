package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Renders the rows of the collapsed "ready" friend body.
 *
 * <p>The original {@code _KW_READY_ITEM} ({@code IMListLayer.csb}) is a
 * 200x150 cell that stacks the avatar over a name plate rather than
 * laying them out side by side like the expanded list row: the head node
 * sits at {@code (70, 85)} with scale {@code 0.6}, the state icon at
 * {@code (160, 81)} and the name plate at {@code (70, 22)}. CSB places
 * the origin at the bottom-left, so those become 65 / 69 / 128 units from
 * the top. Sub-positions are kept as fractions of the cell so the row
 * still fills the recovered panel width exactly.
 */
final class FriendDrawerReadyRenderer {
    private static final float AVATAR_CENTER_X_FRACTION = 70.0f / 200.0f;
    private static final float AVATAR_CENTER_Y_FRACTION = 65.0f / 150.0f;
    private static final float AVATAR_RADIUS_FRACTION =
            145.0f * 0.6f / 2.0f / 150.0f;
    private static final float STATE_CENTER_X_FRACTION = 160.0f / 200.0f;
    private static final float STATE_CENTER_Y_FRACTION = 69.0f / 150.0f;
    private static final float STATE_HEIGHT_FRACTION = 92.0f / 150.0f;
    private static final float STATE_ASPECT = 47.0f / 92.0f;
    private static final float NAME_CENTER_Y_FRACTION = 128.0f / 150.0f;

    private final Context context;
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path reusablePath = new Path();
    private final Bitmap defaultHead;
    private final Bitmap headFrame;
    private final Bitmap stateOnline;
    private final Bitmap stateOffline;

    FriendDrawerReadyRenderer(Context context) {
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
    }

    void drawList(
            Canvas canvas,
            FriendDrawerState state,
            FriendDrawerLayout layout,
            long nowMillis) {
        RectF list = layout.readyListRect();
        if (state.friends().isEmpty()) {
            // _KW_UI_TIPS_READY_NO_FRIEND is a 47-unit label inside the
            // 0.84-scaled ready panel; the original renders it bold.
            textPaint.setTextSize(56.0f);
            textPaint.setFakeBoldText(true);
            textPaint.setColor(FriendDrawerRenderer.SECONDARY_TEXT);
            textPaint.setTextAlign(Paint.Align.CENTER);
            String message =
                    state.loadingInitial()
                            ? context.getString(R.string.friend_loading)
                            : context.getString(
                                    R.string.game_home_no_friends);
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
            RectF item = layout.readyItemRect(index);
            if (item.bottom - state.scrollOffset() < list.top
                    || item.top - state.scrollOffset() > list.bottom) {
                continue;
            }
            drawItem(canvas, item, state, state.friends().get(index));
        }
        canvas.restoreToCount(save);
    }

    private void drawItem(
            Canvas canvas,
            RectF item,
            FriendDrawerState state,
            FriendEntry friend) {
        float centerX = item.left + item.width() * AVATAR_CENTER_X_FRACTION;
        float centerY = item.top + item.height() * AVATAR_CENTER_Y_FRACTION;
        float radius = item.height() * AVATAR_RADIUS_FRACTION;

        Bitmap avatar = state.avatar(friend.avatarKey());
        if (avatar == null || avatar.isRecycled()) {
            avatar = defaultHead;
        }
        int save = canvas.save();
        reusablePath.reset();
        reusablePath.addCircle(centerX, centerY, radius, Path.Direction.CW);
        canvas.clipPath(reusablePath);
        canvas.drawBitmap(
                avatar,
                null,
                new RectF(
                        centerX - radius, centerY - radius,
                        centerX + radius, centerY + radius),
                bitmapPaint);
        canvas.restoreToCount(save);

        float frameRadius = radius * 164.0f / 145.0f;
        canvas.drawBitmap(
                headFrame,
                null,
                new RectF(
                        centerX - frameRadius, centerY - frameRadius,
                        centerX + frameRadius, centerY + frameRadius),
                bitmapPaint);

        float stateHeight = item.height() * STATE_HEIGHT_FRACTION;
        float stateWidth = stateHeight * STATE_ASPECT;
        float stateCenterX =
                item.left + item.width() * STATE_CENTER_X_FRACTION;
        float stateCenterY =
                item.top + item.height() * STATE_CENTER_Y_FRACTION;
        canvas.drawBitmap(
                friend.state() == FriendEntry.State.OFFLINE
                        ? stateOffline
                        : stateOnline,
                null,
                new RectF(
                        stateCenterX - stateWidth / 2.0f,
                        stateCenterY - stateHeight / 2.0f,
                        stateCenterX + stateWidth / 2.0f,
                        stateCenterY + stateHeight / 2.0f),
                bitmapPaint);

        textPaint.setTextSize(34.0f);
        textPaint.setColor(FriendDrawerRenderer.PRIMARY_TEXT);
        textPaint.setTextAlign(Paint.Align.CENTER);
        String name = friend.displayName() == null ? "" : friend.displayName();
        while (textPaint.getTextSize() > 18.0f
                && textPaint.measureText(name) > item.width() - 24.0f) {
            textPaint.setTextSize(textPaint.getTextSize() - 1.0f);
        }
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(
                name,
                item.centerX(),
                item.top
                        + item.height() * NAME_CENTER_Y_FRACTION
                        - (metrics.ascent + metrics.descent) / 2.0f,
                textPaint);
    }
}
