package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.nanbeiyule.game.mahjong.MahjongTileSprite;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileGeometry;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouMahjongDiscardProjection;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayInteraction;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Draws only server-confirmed river tiles using UIMahPlayerOutArea geometry. */
final class TaizhouMahjongDiscardRenderer {
    private static final int NORMAL = 0xffffffff;
    private static final int SAME_VALUE = 0xffffc9aa;
    private static final long SHOW_OUT_MAH_MILLIS = 1_000L;
    private static final float SHOW_OUT_MAH_PADDING = 20.0f;

    private final OriginalMahjongTilePainter tilePainter;
    private final TaizhouMahjongDiscardEffectRenderer effectRenderer;
    private final Bitmap showOutMahBackground;
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final TaizhouDiscardAnimationTracker animationTracker = new TaizhouDiscardAnimationTracker();
    private boolean showBigOutMah = true;

    TaizhouMahjongDiscardRenderer(
            OriginalMahjongTilePainter tilePainter,
            TaizhouMahjongDiscardEffectRenderer effectRenderer,
            Bitmap showOutMahBackground) {
        this.tilePainter = Objects.requireNonNull(tilePainter, "tilePainter");
        this.effectRenderer = Objects.requireNonNull(effectRenderer, "effectRenderer");
        this.showOutMahBackground =
                Objects.requireNonNull(showOutMahBackground, "showOutMahBackground");
    }

    void setShowBigOutMah(boolean show) {
        showBigOutMah = show;
    }

    void update(TaizhouMahjongVisibleRound round, long nowElapsed) {
        animationTracker.update(round, nowElapsed);
    }

    void draw(
            Canvas canvas,
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongPlayInteraction interaction,
            long nowElapsed) {
        if (round == null) {
            return;
        }
        Integer selectedValue = selectedValue(interaction);
        TaizhouDiscardAnimationTracker.LastDiscard lastDiscard = animationTracker.lastDiscard();
        TaizhouDiscardAnimationTracker.RunningDiscard runningDiscard =
                animationTracker.running(nowElapsed);
        TaizhouMahjongDiscardProjection.Tile lastDiscardTile = null;
        for (int serverSeat = 1; serverSeat <= round.chairCount(); serverSeat++) {
            int localSeat =
                    TaizhouMahjongSeatMapper.toLocalSeat(
                            serverSeat, round.mySeat(), round.chairCount());
            TaizhouMahjongVisibleRound.SeatRiver river = round.riverAt(serverSeat);
            List<TaizhouMahjongDiscardProjection.Tile> tiles =
                    new ArrayList<>(
                            TaizhouMahjongDiscardProjection.forLocalSeat(
                                    localSeat,
                                    round.chairCount(),
                                    river.tiles(),
                                    river.maxLineCount()));
            tiles.sort(
                    Comparator.comparingInt(TaizhouMahjongDiscardProjection.Tile::localZOrder)
                            .thenComparingInt(TaizhouMahjongDiscardProjection.Tile::tileIndex));
            for (TaizhouMahjongDiscardProjection.Tile tile : tiles) {
                tilePainter.draw(
                        canvas,
                        OriginalMahjongTileDrawPlan.atAnchor(
                                tile.pose(),
                                tile.tileValue(),
                                tile.designX(),
                                tile.cocosY(),
                                tile.effectiveScale(),
                                tile.anchorX(),
                                tile.anchorY(),
                                round.jokerTiles().contains(tile.tileValue())),
                        selectedValue != null && selectedValue == tile.tileValue()
                                ? SAME_VALUE
                                : NORMAL);
                if (matches(lastDiscard, serverSeat, tile)) {
                    lastDiscardTile = tile;
                }
                if (matches(runningDiscard, serverSeat, tile)) {
                    effectRenderer.drawDiscard(
                            canvas,
                            tile,
                            Math.round(
                                    runningDiscard.progress()
                                            * TaizhouDiscardAnimationTracker
                                                    .DISCARD_FLIGHT_MILLIS));
                }
            }
        }
        if (lastDiscardTile != null) {
            effectRenderer.drawCursor(canvas, lastDiscardTile, nowElapsed);
        }
    }

    void drawShowOutMah(
            Canvas canvas,
            TaizhouMahjongVisibleRound round,
            long nowElapsed) {
        LastDiscardProjection projection = lastDiscardProjection(round);
        if (projection != null) {
            drawShowOutMah(
                    canvas,
                    round,
                    projection.localSeat(),
                    projection.tile(),
                    nowElapsed);
        }
    }

    long nextRepaintDelayMillis(long nowElapsed) {
        long animationDelay = animationTracker.nextRepaintDelayMillis(nowElapsed);
        long showOutDelay = nextShowOutMahDelayMillis(nowElapsed);
        if (animationDelay <= 0L) {
            return showOutDelay;
        }
        if (showOutDelay <= 0L) {
            return animationDelay;
        }
        return Math.min(animationDelay, showOutDelay);
    }

    void release() {
        effectRenderer.release();
    }

    private void drawShowOutMah(
            Canvas canvas,
            TaizhouMahjongVisibleRound round,
            int localSeat,
            TaizhouMahjongDiscardProjection.Tile tile,
            long nowElapsed) {
        if (!showBigOutMah
                || localSeat == TaizhouMahjongTableLayout.SEAT_BOTTOM
                || animationTracker.lastDiscardElapsedMillis(nowElapsed) >= SHOW_OUT_MAH_MILLIS) {
            return;
        }
        OriginalMahjongTileGeometry.Composition composition =
                OriginalMahjongTileGeometry.defaultTile(
                        MahjongTileSprite.STAND_FACE_FORWARD, tile.tileValue());
        float scale = TaizhouMahjongTableLayout.showOutMahPanel(localSeat).scale;
        float width = composition.width * scale + SHOW_OUT_MAH_PADDING * 2.0f;
        float height = composition.height * scale + SHOW_OUT_MAH_PADDING * 2.0f;
        TaizhouMahjongTableLayout.Slot slot =
                TaizhouMahjongTableLayout.showOutMahPanel(localSeat);
        drawNineSlice(
                canvas,
                showOutMahBackground,
                new RectF(
                        slot.designX() - width / 2.0f,
                        TaizhouMahjongTableLayout.designY(slot.cocosY() + height),
                        slot.designX() + width / 2.0f,
                        TaizhouMahjongTableLayout.designY(slot.cocosY())));
        tilePainter.draw(
                canvas,
                OriginalMahjongTileDrawPlan.atAnchor(
                        MahjongTileSprite.STAND_FACE_FORWARD,
                        tile.tileValue(),
                        slot.designX(),
                        slot.cocosY()
                                + SHOW_OUT_MAH_PADDING
                                + composition.height * scale / 2.0f,
                        scale,
                        0.5f,
                        0.5f,
                        round.jokerTiles().contains(tile.tileValue())));
    }

    private static LastDiscardProjection lastDiscardProjection(TaizhouMahjongVisibleRound round) {
        if (round == null || round.lastDiscard() == null) {
            return null;
        }
        TaizhouMahjongVisibleRound.LastDiscard marker = round.lastDiscard();
        int localSeat =
                TaizhouMahjongSeatMapper.toLocalSeat(
                        marker.seatNumber(), round.mySeat(), round.chairCount());
        TaizhouMahjongVisibleRound.SeatRiver river = round.riverAt(marker.seatNumber());
        List<TaizhouMahjongDiscardProjection.Tile> tiles =
                TaizhouMahjongDiscardProjection.forLocalSeat(
                        localSeat,
                        round.chairCount(),
                        river.tiles(),
                        river.maxLineCount());
        if (marker.tileIndex() < 0 || marker.tileIndex() >= tiles.size()) {
            return null;
        }
        return new LastDiscardProjection(localSeat, tiles.get(marker.tileIndex()));
    }

    private long nextShowOutMahDelayMillis(long nowElapsed) {
        if (!showBigOutMah || animationTracker.lastDiscard() == null) {
            return 0L;
        }
        long remaining = SHOW_OUT_MAH_MILLIS - animationTracker.lastDiscardElapsedMillis(nowElapsed);
        return remaining > 0L ? remaining : 0L;
    }

    private void drawNineSlice(Canvas canvas, Bitmap bitmap, RectF destination) {
        int capLeft = bitmap.getWidth() / 3;
        int capTop = bitmap.getHeight() / 3;
        int capRight = bitmap.getWidth() - capLeft;
        int capBottom = bitmap.getHeight() - capTop;
        float dstLeft = destination.left + Math.min(capLeft, destination.width() / 2.0f);
        float dstTop = destination.top + Math.min(capTop, destination.height() / 2.0f);
        float dstRight = destination.right - Math.min(bitmap.getWidth() - capRight, destination.width() / 2.0f);
        float dstBottom = destination.bottom - Math.min(bitmap.getHeight() - capBottom, destination.height() / 2.0f);
        int[] sourceX = {0, capLeft, capRight, bitmap.getWidth()};
        int[] sourceY = {0, capTop, capBottom, bitmap.getHeight()};
        float[] destX = {destination.left, dstLeft, dstRight, destination.right};
        float[] destY = {destination.top, dstTop, dstBottom, destination.bottom};
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                if (sourceX[column] == sourceX[column + 1]
                        || sourceY[row] == sourceY[row + 1]
                        || destX[column] == destX[column + 1]
                        || destY[row] == destY[row + 1]) {
                    continue;
                }
                canvas.drawBitmap(
                        bitmap,
                        new Rect(
                                sourceX[column],
                                sourceY[row],
                                sourceX[column + 1],
                                sourceY[row + 1]),
                        new RectF(
                                destX[column],
                                destY[row],
                                destX[column + 1],
                                destY[row + 1]),
                        bitmapPaint);
            }
        }
    }

    private static boolean matches(
            TaizhouDiscardAnimationTracker.LastDiscard marker,
            int serverSeat,
            TaizhouMahjongDiscardProjection.Tile tile) {
        return marker != null
                && marker.serverSeat() == serverSeat
                && marker.tileIndex() == tile.tileIndex()
                && marker.tileValue() == tile.tileValue();
    }

    private static boolean matches(
            TaizhouDiscardAnimationTracker.RunningDiscard marker,
            int serverSeat,
            TaizhouMahjongDiscardProjection.Tile tile) {
        return marker != null
                && marker.serverSeat() == serverSeat
                && marker.tileIndex() == tile.tileIndex()
                && marker.tileValue() == tile.tileValue();
    }

    private static Integer selectedValue(TaizhouMahjongPlayInteraction interaction) {
        if (interaction == null || interaction.visualState().selectedIndex() == null) {
            return null;
        }
        int selectedIndex = interaction.visualState().selectedIndex();
        for (TaizhouMahjongPlayGesture.Tile tile : interaction.tiles()) {
            if (tile.index() == selectedIndex) {
                return tile.value();
            }
        }
        return null;
    }

    private record LastDiscardProjection(
            int localSeat,
            TaizhouMahjongDiscardProjection.Tile tile) {}
}
