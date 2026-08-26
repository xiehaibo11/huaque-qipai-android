package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableAtlas;

/** Extracts upright frames from the original Mahjong icon atlas. */
final class TaizhouMahjongIconBitmap {
    private TaizhouMahjongIconBitmap() {}

    static Bitmap extract(Bitmap atlas, String frameName) {
        if (atlas == null
                || atlas.isRecycled()
                || atlas.getWidth() != TaizhouMahjongTableAtlas.ICON_WIDTH
                || atlas.getHeight() != TaizhouMahjongTableAtlas.ICON_HEIGHT) {
            throw new IllegalArgumentException("Invalid original Mahjong icon atlas");
        }
        int index =
                TaizhouMahjongTableAtlas.indexOf(
                        TaizhouMahjongTableAtlas.ICON_NAMES, frameName);
        if (index < 0) {
            throw new IllegalArgumentException("Missing original Mahjong icon " + frameName);
        }
        int[] frame = TaizhouMahjongTableAtlas.ICON_FRAMES[index];
        int storedWidth = frame[4] == 0 ? frame[2] : frame[3];
        int storedHeight = frame[4] == 0 ? frame[3] : frame[2];
        Bitmap stored =
                Bitmap.createBitmap(atlas, frame[0], frame[1], storedWidth, storedHeight);
        if (frame[4] == 0) {
            return stored;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(-90.0f);
        Bitmap upright =
                Bitmap.createBitmap(
                        stored, 0, 0, storedWidth, storedHeight, matrix, true);
        if (upright != stored) {
            stored.recycle();
        }
        return upright;
    }
}
