package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableAtlas;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;

/**
 * Draws the TableInfo 生牌信息层 ({@code TableInfo.csb}): the
 * {@code tz_shengPai.png} banner, the {@code mah_img_surplus.png} 剩余 label and
 * the {@code mah_number-export.fnt} count, shown only while the state projection
 * reports a positive sheng-pai count (see {@link TaizhouTableInfoState}).
 */
final class TaizhouTableInfoRenderer {
    private final Bitmap shengPaiBanner;
    private final Bitmap surplusLabel;
    private final SxvipBitmapFont countFont;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    TaizhouTableInfoRenderer(Context context, Bitmap tableInfoAtlas) {
        shengPaiBanner = extract(tableInfoAtlas, TaizhouTableInfoLayout.SP_BG_FRAME);
        surplusLabel = extract(tableInfoAtlas, TaizhouTableInfoLayout.SURPLUS_BG_FRAME);
        countFont =
                SxvipBitmapFont.loadRawResource(
                        context.getResources(),
                        R.raw.taizhou_mahjong_table_info_mah_number,
                        R.drawable.taizhou_mahjong_table_info_mah_number);
    }

    void draw(Canvas canvas, GameplayTableState state) {
        TaizhouTableInfoState info = TaizhouTableInfoState.from(state);
        if (!info.shengPaiVisible()) {
            return;
        }
        drawCentered(
                canvas,
                shengPaiBanner,
                TaizhouTableInfoLayout.SP_BG_CENTER_X,
                TaizhouTableInfoLayout.SP_BG_CENTER_COCOS_Y,
                TaizhouTableInfoLayout.SP_BG_WIDTH,
                TaizhouTableInfoLayout.SP_BG_HEIGHT);
        drawCentered(
                canvas,
                surplusLabel,
                TaizhouTableInfoLayout.SURPLUS_BG_LEFT
                        + TaizhouTableInfoLayout.SURPLUS_BG_WIDTH / 2.0f,
                TaizhouTableInfoLayout.SURPLUS_BG_CENTER_COCOS_Y,
                TaizhouTableInfoLayout.SURPLUS_BG_WIDTH,
                TaizhouTableInfoLayout.SURPLUS_BG_HEIGHT);
        countFont.drawLeft(
                canvas,
                String.valueOf(info.shengPaiCount()),
                TaizhouTableInfoLayout.COUNT_LEFT,
                TaizhouMahjongTableLayout.designY(TaizhouTableInfoLayout.COUNT_CENTER_COCOS_Y),
                TaizhouTableInfoLayout.COUNT_SCALE);
    }

    private void drawCentered(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerCocosY,
            float width,
            float height) {
        float centerY = TaizhouMahjongTableLayout.designY(centerCocosY);
        canvas.drawBitmap(
                bitmap,
                null,
                new RectF(
                        centerX - width / 2.0f,
                        centerY - height / 2.0f,
                        centerX + width / 2.0f,
                        centerY + height / 2.0f),
                bitmapPaint);
    }

    /** Extracts one upright frame from the recovered {@code tableInfo} atlas. */
    static Bitmap extract(Bitmap atlas, String frameName) {
        if (atlas == null
                || atlas.isRecycled()
                || atlas.getWidth() != TaizhouMahjongTableAtlas.TABLE_INFO_WIDTH
                || atlas.getHeight() != TaizhouMahjongTableAtlas.TABLE_INFO_HEIGHT) {
            throw new IllegalArgumentException("Invalid original tableInfo atlas");
        }
        int index =
                TaizhouMahjongTableAtlas.indexOf(
                        TaizhouMahjongTableAtlas.TABLE_INFO_NAMES, frameName);
        if (index < 0) {
            throw new IllegalArgumentException("Missing original tableInfo frame " + frameName);
        }
        int[] frame = TaizhouMahjongTableAtlas.TABLE_INFO_FRAMES[index];
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
                Bitmap.createBitmap(stored, 0, 0, storedWidth, storedHeight, matrix, true);
        if (upright != stored) {
            stored.recycle();
        }
        return upright;
    }
}
