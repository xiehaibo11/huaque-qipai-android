package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

/** Runtime slices of the original activityTitle TexturePacker atlas. */
final class OriginalActivityCenterArtwork {
    final Bitmap background;
    final Bitmap titleFrame;
    final Bitmap titleOuter;
    final Bitmap titleInner;
    final Bitmap tabSelected;
    final Bitmap tabUnselected;
    final Bitmap activitySelectedText;
    final Bitmap activityUnselectedText;
    final Bitmap announcementSelectedText;
    final Bitmap announcementUnselectedText;
    final Bitmap close;
    final Bitmap awardCenter;
    final Bitmap rowSelected;
    final Bitmap rowUnselected;
    final Bitmap disclaimer;

    OriginalActivityCenterArtwork(Context context) {
        background = load(context, R.drawable.original_activity_shop_background);
        Bitmap atlas = load(context, R.drawable.original_activity_title_atlas);
        titleFrame = frame(atlas, 2, 2, 500, 90, true, 500, 90, 0, 0);
        titleOuter = frame(atlas, 83, 1291, 94, 90, true, 94, 90, 0, 0);
        titleInner = frame(atlas, 26, 1143, 41, 48, true, 41, 48, 0, 0);
        tabSelected = frame(atlas, 75, 856, 242, 81, true, 248, 81, 2, 0);
        tabUnselected = frame(atlas, 2, 1191, 238, 79, true, 248, 81, -5, 0);
        activitySelectedText = frame(atlas, 128, 721, 122, 56, true, 122, 56, 0, 0);
        activityUnselectedText = frame(atlas, 74, 1842, 128, 63, true, 128, 63, 0, 0);
        announcementSelectedText = frame(atlas, 139, 1866, 123, 62, true, 123, 62, 0, 0);
        announcementUnselectedText = frame(atlas, 2, 1839, 129, 70, true, 129, 70, 0, 0);
        close = frame(atlas, 2, 1431, 185, 218, false, 185, 218, 0, 0);
        awardCenter = frame(atlas, 2, 1651, 186, 106, true, 186, 106, 0, 0);
        rowSelected = frame(atlas, 94, 2, 351, 124, true, 351, 124, 0, 0);
        rowUnselected = frame(atlas, 2, 504, 350, 124, true, 350, 124, 0, 0);
        disclaimer = frame(atlas, 2, 856, 333, 22, true, 367, 46, 0, 4);
    }

    private static Bitmap load(Context context, int resource) {
        return BitmapFactory.decodeResource(context.getResources(), resource);
    }

    private static Bitmap frame(
            Bitmap atlas,
            int x,
            int y,
            int width,
            int height,
            boolean rotated,
            int sourceWidth,
            int sourceHeight,
            int offsetX,
            int offsetY) {
        int storedWidth = rotated ? height : width;
        int storedHeight = rotated ? width : height;
        Bitmap cropped = Bitmap.createBitmap(atlas, x, y, storedWidth, storedHeight);
        Bitmap trimmed = cropped;
        if (rotated) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-90f);
            trimmed = Bitmap.createBitmap(cropped, 0, 0, storedWidth, storedHeight, matrix, true);
        }
        if (trimmed.getWidth() == sourceWidth
                && trimmed.getHeight() == sourceHeight
                && offsetX == 0
                && offsetY == 0) {
            return trimmed;
        }
        Bitmap restored = Bitmap.createBitmap(sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);
        restored.eraseColor(Color.TRANSPARENT);
        float left = (sourceWidth - trimmed.getWidth()) * 0.5f + offsetX;
        float top = (sourceHeight - trimmed.getHeight()) * 0.5f - offsetY;
        new Canvas(restored).drawBitmap(trimmed, left, top, new Paint(Paint.FILTER_BITMAP_FLAG));
        return restored;
    }
}
