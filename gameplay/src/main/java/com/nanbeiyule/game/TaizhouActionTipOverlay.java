package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import com.nanbeiyule.game.gameplay.GameplayActionTip;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableAtlas;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Draws the table-centre action tip frames (吃碰杠补花胡) from the original
 * {@code taizhou_mahjong_action_tip} atlas ({@code onBtnAction.plist}).
 *
 * <p>Evidence boundary: the original shows these frames after each operation
 * in {@code UIMahLayerAction} (and {@code MahjongLayer.csb} keeps a leftover
 * {@code act_buhua} sprite), but the recovered archive contains neither the
 * {@code hu_ani} animation nor the tip show/hide timing code. This overlay
 * therefore draws the static frame centred on the table for a fixed window —
 * the centre placement matches the leftover CSB sprite's centre anchor.
 */
final class TaizhouActionTipOverlay {
    private static final Map<GameplayActionTip.Kind, String> FRAMES =
            new EnumMap<>(GameplayActionTip.Kind.class);

    static {
        FRAMES.put(GameplayActionTip.Kind.CHOW, "act_chi.png");
        FRAMES.put(GameplayActionTip.Kind.PONG, "act_peng.png");
        FRAMES.put(GameplayActionTip.Kind.KONG, "act_gang.png");
        FRAMES.put(GameplayActionTip.Kind.CONCEALED_KONG, "act_gang.png");
        FRAMES.put(GameplayActionTip.Kind.FLOWER, "act_buhua.png");
        FRAMES.put(GameplayActionTip.Kind.HU, "eff_hupai.png");
    }

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Map<GameplayActionTip.Kind, Bitmap> frames =
            new EnumMap<>(GameplayActionTip.Kind.class);

    TaizhouActionTipOverlay(Bitmap actionTipAtlas) {
        Objects.requireNonNull(actionTipAtlas, "actionTipAtlas");
        if (actionTipAtlas.getWidth() != TaizhouMahjongTableAtlas.ACTION_TIP_WIDTH
                || actionTipAtlas.getHeight() != TaizhouMahjongTableAtlas.ACTION_TIP_HEIGHT) {
            throw new IllegalArgumentException("original action tip atlas size mismatch");
        }
        for (Map.Entry<GameplayActionTip.Kind, String> entry : FRAMES.entrySet()) {
            frames.put(entry.getKey(), extract(actionTipAtlas, entry.getValue()));
        }
    }

    void draw(Canvas canvas, GameplayActionTip.Kind kind) {
        Bitmap frame = frames.get(kind);
        if (frame == null) {
            return;
        }
        float centerX = TaizhouMahjongTableLayout.DESIGN_WIDTH / 2.0f;
        float centerY = TaizhouMahjongTableLayout.DESIGN_HEIGHT / 2.0f;
        float halfWidth = frame.getWidth() / 2.0f;
        float halfHeight = frame.getHeight() / 2.0f;
        canvas.drawBitmap(
                frame,
                null,
                new RectF(
                        centerX - halfWidth,
                        TaizhouMahjongTableLayout.designY(centerY + halfHeight),
                        centerX + halfWidth,
                        TaizhouMahjongTableLayout.designY(centerY - halfHeight)),
                bitmapPaint);
    }

    private static Bitmap extract(Bitmap atlas, String frameName) {
        int index =
                TaizhouMahjongTableAtlas.indexOf(
                        TaizhouMahjongTableAtlas.ACTION_TIP_NAMES, frameName);
        if (index < 0) {
            throw new IllegalArgumentException("missing action tip frame " + frameName);
        }
        int[] frame = TaizhouMahjongTableAtlas.ACTION_TIP_FRAMES[index];
        int storedWidth = frame[4] != 0 ? frame[3] : frame[2];
        int storedHeight = frame[4] != 0 ? frame[2] : frame[3];
        Bitmap stored = Bitmap.createBitmap(atlas, frame[0], frame[1], storedWidth, storedHeight);
        if (frame[4] == 0) {
            return stored;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(-90.0f);
        Bitmap upright =
                Bitmap.createBitmap(stored, 0, 0, storedWidth, storedHeight, matrix, true);
        if (upright != stored) {
            stored.recycle();
        }
        return upright;
    }
}
