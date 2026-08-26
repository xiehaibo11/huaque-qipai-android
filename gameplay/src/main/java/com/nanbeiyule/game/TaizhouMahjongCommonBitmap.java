package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.nanbeiyule.game.mahjong.TaizhouMahjongCommonFrame;

/** Extracts upright frames from the original 2048 x 2048 common game-layer atlas. */
final class TaizhouMahjongCommonBitmap {
    private static final int ATLAS_SIZE = 2048;

    private TaizhouMahjongCommonBitmap() {}

    static Bitmap extract(Bitmap atlas, String frameName) {
        if (atlas == null
                || atlas.isRecycled()
                || atlas.getWidth() != ATLAS_SIZE
                || atlas.getHeight() != ATLAS_SIZE) {
            throw new IllegalArgumentException("Invalid original common game-layer atlas");
        }
        TaizhouMahjongCommonFrame frame = TaizhouMahjongCommonFrame.require(frameName);
        Bitmap stored =
                Bitmap.createBitmap(
                        atlas,
                        frame.atlasX(),
                        frame.atlasY(),
                        frame.storedWidth(),
                        frame.storedHeight());
        if (!frame.rotated()) {
            return stored;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(-90.0f);
        Bitmap upright =
                Bitmap.createBitmap(
                        stored,
                        0,
                        0,
                        frame.storedWidth(),
                        frame.storedHeight(),
                        matrix,
                        true);
        if (upright != stored) {
            stored.recycle();
        }
        return upright;
    }
}
