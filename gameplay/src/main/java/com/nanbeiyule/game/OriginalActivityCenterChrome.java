package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Shared layered chrome for the original activity and announcement screens. */
final class OriginalActivityCenterChrome {
    enum SelectedTab {
        ACTIVITY,
        ANNOUNCEMENT
    }

    private static final RectF TITLE_FRAME = new RectF(294.912f, 6.984f, 794.912f, 96.984f);
    private static final RectF CLOSE_ART = new RectF(1717.14f, 81.68f, 1902.14f, 299.68f);
    private final OriginalActivityCenterArtwork artwork;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    OriginalActivityCenterChrome(Context context) {
        artwork = new OriginalActivityCenterArtwork(context);
    }

    void draw(Canvas canvas, SelectedTab selectedTab, boolean showAwardCenter) {
        draw(canvas, artwork.background, 0f, 0f, 1920f, 1080f, false);
        draw(canvas, artwork.titleFrame, TITLE_FRAME, false);
        boolean activity = selectedTab == SelectedTab.ACTIVITY;
        draw(
                canvas,
                activity ? artwork.tabSelected : artwork.tabUnselected,
                rect(LobbyActivityCenterLayout.ACTIVITY_TAB),
                true);
        draw(
                canvas,
                activity ? artwork.tabUnselected : artwork.tabSelected,
                rect(LobbyActivityCenterLayout.ANNOUNCEMENT_TAB),
                false);
        drawTabText(canvas, activity);
        draw(canvas, artwork.titleOuter, rect(LobbyActivityCenterLayout.TITLE_RIGHT_OUTER), false);
        draw(canvas, artwork.titleInner, rect(LobbyActivityCenterLayout.TITLE_RIGHT_INNER), false);
        draw(canvas, artwork.titleOuter, rect(LobbyActivityCenterLayout.TITLE_LEFT_OUTER), true);
        draw(canvas, artwork.titleInner, rect(LobbyActivityCenterLayout.TITLE_LEFT_INNER), true);
        if (showAwardCenter) {
            draw(
                    canvas,
                    artwork.awardCenter,
                    rect(LobbyActivityCenterLayout.AWARD_CENTER),
                    false);
        }
        draw(canvas, artwork.disclaimer, rect(LobbyActivityCenterLayout.DISCLAIMER), false);
        draw(canvas, artwork.close, CLOSE_ART, false);
    }

    void drawRow(Canvas canvas, AdaptiveViewport.Rect bounds, String title, boolean selected, Paint textPaint) {
        draw(canvas, selected ? artwork.rowSelected : artwork.rowUnselected, rect(bounds), false);
        textPaint.setColor(selected ? 0xffffffff : 0xffbf6c1d);
        textPaint.setTextSize(46f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float centerY = bounds.top() + 66f;
        float baseline = centerY - (metrics.ascent + metrics.descent) * 0.5f;
        canvas.drawText(title, bounds.centerX(), baseline, textPaint);
    }

    private void drawTabText(Canvas canvas, boolean activitySelected) {
        Bitmap activityText =
                activitySelected ? artwork.activitySelectedText : artwork.activityUnselectedText;
        Bitmap announcementText =
                activitySelected
                        ? artwork.announcementUnselectedText
                        : artwork.announcementSelectedText;
        drawCentered(canvas, activityText, LobbyActivityCenterLayout.ACTIVITY_TAB, false);
        drawCentered(canvas, announcementText, LobbyActivityCenterLayout.ANNOUNCEMENT_TAB, false);
    }

    private void drawCentered(
            Canvas canvas, Bitmap bitmap, AdaptiveViewport.Rect bounds, boolean flipX) {
        float left = bounds.centerX() - bitmap.getWidth() * 0.5f;
        float top = bounds.centerY() - bitmap.getHeight() * 0.5f;
        draw(canvas, bitmap, left, top, left + bitmap.getWidth(), top + bitmap.getHeight(), flipX);
    }

    private void draw(Canvas canvas, Bitmap bitmap, RectF destination, boolean flipX) {
        draw(
                canvas,
                bitmap,
                destination.left,
                destination.top,
                destination.right,
                destination.bottom,
                flipX);
    }

    private void draw(
            Canvas canvas,
            Bitmap bitmap,
            float left,
            float top,
            float right,
            float bottom,
            boolean flipX) {
        int save = canvas.save();
        if (flipX) canvas.scale(-1f, 1f, (left + right) * 0.5f, (top + bottom) * 0.5f);
        canvas.drawBitmap(bitmap, null, new RectF(left, top, right, bottom), paint);
        canvas.restoreToCount(save);
    }

    private static RectF rect(AdaptiveViewport.Rect value) {
        return new RectF(value.left(), value.top(), value.right(), value.bottom());
    }
}
