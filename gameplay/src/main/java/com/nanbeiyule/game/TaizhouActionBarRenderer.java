package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.nanbeiyule.game.mahjong.MahjongTile;
import com.nanbeiyule.game.mahjong.MahjongTileSprite;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableAtlas;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import java.util.List;
import java.util.Objects;

/**
 * Canvas renderer for the Taizhou mahjong action bar and its candidate panel.
 * Draws the visible action buttons at their compacted slots and, while a comb
 * candidate panel is open, the stretched {@code action_combs_bg} strip, the
 * candidate melds as original lie tiles, and the cancel button.
 *
 * <p>Disabled/alpha: the original defines no persistent disabled visual for
 * these buttons (no disabled texture in the CSB, no alpha rule in Lua), so
 * every visible button is drawn fully opaque; the one-second PASS debounce is
 * transient and handled by Wave 2. The {@code hu_ani} overlay on the HU button
 * and the slide-in/out animations are likewise not part of this static layer.
 * The comb background has no scale9 evidence in the CSB dump, so it is drawn
 * as a uniform stretch (推断: the dump tool reads no scale9 flag).
 */
final class TaizhouActionBarRenderer {
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap[] buttonFrames = new Bitmap[8];
    private final Bitmap combBackground;
    private final Bitmap cancelFrame;
    private final OriginalMahjongTilePainter tilePainter;

    TaizhouActionBarRenderer(Bitmap actionBtnAtlas, OriginalMahjongTilePainter tilePainter) {
        Objects.requireNonNull(actionBtnAtlas, "actionBtnAtlas");
        if (actionBtnAtlas.getWidth() != TaizhouMahjongTableAtlas.ACTION_BTN_WIDTH
                || actionBtnAtlas.getHeight() != TaizhouMahjongTableAtlas.ACTION_BTN_HEIGHT) {
            throw new IllegalArgumentException("original action button atlas size mismatch");
        }
        this.tilePainter = Objects.requireNonNull(tilePainter, "tilePainter");
        for (int actionId = 1; actionId <= 7; actionId++) {
            String frameName = TaizhouActionBarLayout.frameName(actionId);
            if (frameName != null) {
                buttonFrames[actionId] = extract(actionBtnAtlas, frameName);
            }
        }
        combBackground = extract(actionBtnAtlas, "action_combs_bg.png");
        cancelFrame = extract(actionBtnAtlas, "action_cancle.png");
    }

    void draw(Canvas canvas, TaizhouActionBarState state) {
        if (state == null) {
            return;
        }
        if (state.barVisible()) {
            drawBar(canvas, state);
        }
        if (state.combKind() != TaizhouActionBarState.CombKind.NONE) {
            drawCombPanel(canvas, state);
        }
    }

    private void drawBar(Canvas canvas, TaizhouActionBarState state) {
        List<Integer> actions = state.visibleActions();
        for (int slot = 1; slot <= actions.size(); slot++) {
            Bitmap frame = buttonFrames[actions.get(slot - 1)];
            if (frame == null) {
                continue;
            }
            float[] rect = TaizhouActionBarLayout.slotRectAndroid(slot);
            canvas.drawBitmap(frame, null, new RectF(rect[0], rect[1], rect[2], rect[3]), bitmapPaint);
        }
    }

    private void drawCombPanel(Canvas canvas, TaizhouActionBarState state) {
        List<int[]> candidates = state.combCandidates();
        if (candidates.isEmpty()) {
            return;
        }
        int tilesPerComb = candidates.get(0).length;
        float cellWidth = TaizhouActionBarLayout.combCellWidth();
        float cellHeight = TaizhouActionBarLayout.combCellHeight(tilesPerComb);
        float backLeft = TaizhouActionBarLayout.combsBackLeft(cellWidth, candidates.size());
        float backHeight = TaizhouActionBarLayout.combsBackHeight(cellHeight, candidates.size());

        canvas.drawBitmap(
                combBackground,
                null,
                new RectF(
                        backLeft,
                        TaizhouMahjongTableLayout.designY(
                                TaizhouActionBarLayout.COMBS_BACK_BOTTOM + backHeight),
                        TaizhouActionBarLayout.COMBS_BACK_RIGHT,
                        TaizhouMahjongTableLayout.designY(TaizhouActionBarLayout.COMBS_BACK_BOTTOM)),
                bitmapPaint);

        for (int index = candidates.size(); index >= 1; index--) {
            drawComb(canvas, candidates.get(index - 1), index, cellWidth, cellHeight, backLeft);
        }

        float cancelHalf = TaizhouActionBarLayout.CANCEL_SIZE / 2.0f;
        float cancelCenterX = TaizhouActionBarLayout.CANCEL_RIGHT - cancelHalf;
        canvas.drawBitmap(
                cancelFrame,
                null,
                new RectF(
                        cancelCenterX - cancelHalf,
                        TaizhouMahjongTableLayout.designY(
                                TaizhouActionBarLayout.CANCEL_CENTER_Y + cancelHalf),
                        cancelCenterX + cancelHalf,
                        TaizhouMahjongTableLayout.designY(
                                TaizhouActionBarLayout.CANCEL_CENTER_Y - cancelHalf)),
                bitmapPaint);
    }

    /** Draws one candidate meld: three lie tiles in a row, kong's fourth tile
     * stacked on tile 2 ({@code CombAlignIndexConfig[BOTTOM][0][1] = 2}). */
    private void drawComb(
            Canvas canvas, int[] tiles, int index, float cellWidth, float cellHeight, float backLeft) {
        float cellLeft = TaizhouActionBarLayout.combCellLeft(index, cellWidth, backLeft);
        float cellBottom = TaizhouActionBarLayout.combCellBottom(index, cellHeight);
        float scale = TaizhouActionBarLayout.COMB_SCALE;
        for (int tile = 0; tile < tiles.length; tile++) {
            float localX = (tile % 3) * TaizhouActionBarLayout.COMB_TILE_WIDTH
                    + TaizhouActionBarLayout.COMB_TILE_WIDTH / 2.0f;
            float localY = TaizhouActionBarLayout.COMB_TILE_HEIGHT / 2.0f;
            if (tile >= 3) {
                localX = TaizhouActionBarLayout.COMB_TILE_WIDTH + TaizhouActionBarLayout.COMB_TILE_WIDTH / 2.0f;
                localY += TaizhouActionBarLayout.COMB_TILE_THICK;
            }
            int pose =
                    tiles[tile] == MahjongTile.BACK
                            ? MahjongTileSprite.LIE_DOWN_VERTICAL
                            : MahjongTileSprite.LIE_UP_VERTICAL_UP;
            tilePainter.draw(
                    canvas,
                    OriginalMahjongTileDrawPlan.atAnchor(
                            pose,
                            tiles[tile],
                            cellLeft + localX * scale,
                            cellBottom + localY * scale,
                            scale,
                            0.5f,
                            0.5f));
        }
    }

    private static Bitmap extract(Bitmap atlas, String frameName) {
        int index =
                TaizhouMahjongTableAtlas.indexOf(
                        TaizhouMahjongTableAtlas.ACTION_BTN_NAMES, frameName);
        if (index < 0) {
            throw new IllegalArgumentException("missing action button frame " + frameName);
        }
        int[] frame = TaizhouMahjongTableAtlas.ACTION_BTN_FRAMES[index];
        if (frame[4] != 0) {
            throw new IllegalStateException("unexpected rotated frame " + frameName);
        }
        return Bitmap.createBitmap(atlas, frame[0], frame[1], frame[2], frame[3]);
    }
}
