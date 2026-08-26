package com.nanbeiyule.game;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** 从原版 {@code total_result.png} TexturePacker 图集中还原正向帧。 */
final class TaizhouTotalResultBitmap {
    private static final int ATLAS_WIDTH = 2002;
    private static final int ATLAS_HEIGHT = 540;
    private static final String ASSET = "cocos-lua/mahjong/assets/total_result.png";

    /** x/y 是图集坐标；width/height 是 plist sourceSize，旋转帧落盘宽高需交换。 */
    private record Frame(int x, int y, int width, int height, boolean rotated) {}

    private static final Map<String, Frame> FRAMES = Map.ofEntries(
            Map.entry("result_checkbill_btn.png", new Frame(1731, 2, 346, 136, true)),
            Map.entry("result_total_back.png", new Frame(1194, 462, 188, 69, false)),
            Map.entry("result_total_back_lobby.png", new Frame(589, 121, 387, 132, true)),
            Map.entry("result_total_banker.png", new Frame(417, 450, 71, 71, false)),
            Map.entry("result_total_big_win.png", new Frame(2, 217, 442, 138, false)),
            Map.entry("result_total_host.png", new Frame(1384, 462, 68, 70, true)),
            Map.entry("result_total_img.png", new Frame(2, 450, 413, 88, false)),
            Map.entry("result_total_img2.png", new Frame(723, 121, 413, 88, true)),
            Map.entry("result_total_item_bg.png", new Frame(862, 2, 458, 706, true)),
            Map.entry("result_total_nick_name_bg.png", new Frame(533, 217, 261, 50, true)),
            Map.entry("result_total_roominfo.png", new Frame(2, 357, 529, 91, false)),
            Map.entry("result_total_share.png", new Frame(1869, 2, 387, 131, true)),
            Map.entry("result_total_title.png", new Frame(2, 2, 858, 117, false)));

    private TaizhouTotalResultBitmap() {}

    static Bitmap load(AssetManager assets) {
        try (InputStream stream = assets.open(ASSET)) {
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            requireAtlas(bitmap);
            return bitmap;
        } catch (IOException exception) {
            throw new IllegalStateException("Missing original total-result atlas", exception);
        }
    }

    static Bitmap extract(Bitmap atlas, String frameName) {
        requireAtlas(atlas);
        Frame frame = FRAMES.get(frameName);
        if (frame == null) {
            throw new IllegalArgumentException("Missing original total-result frame " + frameName);
        }
        int storedWidth = frame.rotated() ? frame.height() : frame.width();
        int storedHeight = frame.rotated() ? frame.width() : frame.height();
        Bitmap stored = Bitmap.createBitmap(
                atlas, frame.x(), frame.y(), storedWidth, storedHeight);
        if (!frame.rotated()) {
            return stored;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(-90.0f);
        Bitmap upright = Bitmap.createBitmap(
                stored, 0, 0, storedWidth, storedHeight, matrix, true);
        if (upright != stored) {
            stored.recycle();
        }
        return upright;
    }

    private static void requireAtlas(Bitmap atlas) {
        if (atlas == null
                || atlas.isRecycled()
                || atlas.getWidth() != ATLAS_WIDTH
                || atlas.getHeight() != ATLAS_HEIGHT) {
            throw new IllegalArgumentException("Invalid original total-result atlas");
        }
    }
}
