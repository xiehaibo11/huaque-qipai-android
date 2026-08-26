package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.nanbeiyule.game.mahjong.TaizhouMahjongGameLayerAtlas;
import com.nanbeiyule.game.mahjong.TaizhouMahjongGameLayerFrame;

/** Extracts upright bitmaps from the original TexturePacker GameLayer sheet. */
final class TaizhouMahjongGameLayerBitmap {
    private TaizhouMahjongGameLayerBitmap() {}

    static Bitmap extract(Bitmap atlas, String frameName) {
        if (atlas == null
                || atlas.isRecycled()
                || atlas.getWidth() != TaizhouMahjongGameLayerAtlas.GAME_LAYER_WIDTH
                || atlas.getHeight() != TaizhouMahjongGameLayerAtlas.GAME_LAYER_HEIGHT) {
            throw new IllegalArgumentException("Invalid original GameLayer atlas");
        }
        TaizhouMahjongGameLayerFrame frame = TaizhouMahjongGameLayerFrame.require(frameName);
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
