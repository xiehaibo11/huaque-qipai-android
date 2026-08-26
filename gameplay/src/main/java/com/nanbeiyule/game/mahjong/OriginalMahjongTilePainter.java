package com.nanbeiyule.game.mahjong;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Executes original Mahjong tile draw plans against the byte-identical atlases. */
public final class OriginalMahjongTilePainter {
    private final Bitmap groundAtlas;
    private final Bitmap faceAtlas;
    private final Bitmap faceType1Atlas;
    private final Bitmap iconAtlas;
    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Map<String, Bitmap> frameCache = new HashMap<>();

    public OriginalMahjongTilePainter(
            Bitmap groundAtlas, Bitmap faceAtlas, Bitmap iconAtlas) {
        this(groundAtlas, faceAtlas, iconAtlas, null);
    }

    /** {@code faceType1Atlas} 是牌花 1 的公共图集，牌花设成 1 时才需要。 */
    public OriginalMahjongTilePainter(
            Bitmap groundAtlas, Bitmap faceAtlas, Bitmap iconAtlas, Bitmap faceType1Atlas) {
        this.faceType1Atlas = faceType1Atlas;
        this.groundAtlas =
                requireAtlas(
                        groundAtlas,
                        TaizhouMahjongGroundAtlas.GROUND_WIDTH,
                        TaizhouMahjongGroundAtlas.GROUND_HEIGHT,
                        "ground");
        this.faceAtlas =
                requireAtlas(
                        faceAtlas,
                        TaizhouMahjongFaceAtlas.ATLAS_WIDTH,
                        TaizhouMahjongFaceAtlas.ATLAS_HEIGHT,
                        "face");
        this.iconAtlas =
                requireAtlas(
                        iconAtlas,
                        TaizhouMahjongTableAtlas.ICON_WIDTH,
                        TaizhouMahjongTableAtlas.ICON_HEIGHT,
                        "icon");
    }

    public void drawHandTile(
            Canvas canvas, TaizhouMahjongHandLayout.TilePosition position, int tileValue) {
        drawHandTile(canvas, position, tileValue, false);
    }

    public void drawHandTile(
            Canvas canvas,
            TaizhouMahjongHandLayout.TilePosition position,
            int tileValue,
            boolean joker) {
        draw(canvas, OriginalMahjongTileDrawPlan.forHandTile(position, tileValue, joker));
    }

    public void drawHandTile(
            Canvas canvas,
            TaizhouMahjongHandLayout.TilePosition position,
            int tileValue,
            int cocosColor) {
        drawHandTile(canvas, position, tileValue, cocosColor, false);
    }

    public void drawHandTile(
            Canvas canvas,
            TaizhouMahjongHandLayout.TilePosition position,
            int tileValue,
            int cocosColor,
            boolean joker) {
        draw(
                canvas,
                OriginalMahjongTileDrawPlan.forHandTile(position, tileValue, joker),
                cocosColor);
    }

    public void draw(Canvas canvas, List<OriginalMahjongTileDrawPlan.Command> commands) {
        draw(canvas, commands, 0xffffffff);
    }

    public void draw(
            Canvas canvas,
            List<OriginalMahjongTileDrawPlan.Command> commands,
            int cocosColor) {
        Objects.requireNonNull(canvas, "canvas");
        Objects.requireNonNull(commands, "commands");
        paint.setColorFilter(new PorterDuffColorFilter(cocosColor, PorterDuff.Mode.MULTIPLY));
        try {
            for (OriginalMahjongTileDrawPlan.Command command : commands) {
                drawCommand(canvas, Objects.requireNonNull(command, "command"));
            }
        } finally {
            paint.setColorFilter(null);
        }
    }

    private void drawCommand(Canvas canvas, OriginalMahjongTileDrawPlan.Command command) {
        Bitmap frame = frame(command);
        float centerX = (command.drawLeft + command.drawRight) / 2.0f;
        float centerY = (command.drawTop + command.drawBottom) / 2.0f;
        int save = canvas.save();
        if (command.rotationDegrees != 0) {
            canvas.rotate(command.rotationDegrees, centerX, centerY);
        }
        if (command.scale9) {
            drawNineSlice(canvas, frame, command);
        } else {
            canvas.drawBitmap(
                    frame,
                    null,
                    new RectF(
                            command.drawLeft,
                            command.drawTop,
                            command.drawRight,
                            command.drawBottom),
                    paint);
        }
        canvas.restoreToCount(save);
    }

    private void drawNineSlice(
            Canvas canvas,
            Bitmap frame,
            OriginalMahjongTileDrawPlan.Command command) {
        for (OriginalMahjongNineSlice.Patch patch :
                OriginalMahjongNineSlice.forCommand(command)) {
            if (patch.sourceLeft == patch.sourceRight
                    || patch.sourceTop == patch.sourceBottom
                    || patch.destinationLeft == patch.destinationRight
                    || patch.destinationTop == patch.destinationBottom) {
                continue;
            }
            canvas.drawBitmap(
                    frame,
                    new Rect(
                            patch.sourceLeft,
                            patch.sourceTop,
                            patch.sourceRight,
                            patch.sourceBottom),
                    new RectF(
                            patch.destinationLeft,
                            patch.destinationTop,
                            patch.destinationRight,
                            patch.destinationBottom),
                    paint);
        }
    }

    private Bitmap frame(OriginalMahjongTileDrawPlan.Command command) {
        String key = command.atlas.name() + ':' + command.frameName;
        Bitmap cached = frameCache.get(key);
        if (cached != null) {
            return cached;
        }
        Bitmap atlas =
                switch (command.atlas) {
                    case GROUND -> groundAtlas;
                    case FACE -> faceAtlas;
                    case FACE_TYPE_1 -> requireAtlas(
                            faceType1Atlas,
                            MahjongFaceType1Atlas.ATLAS_WIDTH,
                            MahjongFaceType1Atlas.ATLAS_HEIGHT,
                            "face type 1");
                    case ICON -> iconAtlas;
                };
        requireInsideAtlas(atlas, command);
        Bitmap stored =
                Bitmap.createBitmap(
                        atlas,
                        command.sourceX,
                        command.sourceY,
                        command.storedWidth,
                        command.storedHeight);
        Bitmap upright = stored;
        if (command.atlasRotated) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-90.0f);
            upright =
                    Bitmap.createBitmap(
                            stored,
                            0,
                            0,
                            command.storedWidth,
                            command.storedHeight,
                            matrix,
                            false);
            if (upright != stored) {
                stored.recycle();
            }
        }
        if (upright.getWidth() != command.uprightWidth
                || upright.getHeight() != command.uprightHeight) {
            upright.recycle();
            throw new IllegalStateException("restored frame size mismatch: " + command.frameName);
        }
        frameCache.put(key, upright);
        return upright;
    }

    private static Bitmap requireAtlas(Bitmap atlas, int width, int height, String name) {
        Objects.requireNonNull(atlas, name + " atlas");
        if (atlas.getWidth() != width || atlas.getHeight() != height) {
            throw new IllegalArgumentException(name + " atlas size mismatch");
        }
        return atlas;
    }

    private static void requireInsideAtlas(
            Bitmap atlas, OriginalMahjongTileDrawPlan.Command command) {
        if (command.sourceX < 0
                || command.sourceY < 0
                || command.storedWidth <= 0
                || command.storedHeight <= 0
                || command.sourceX + command.storedWidth > atlas.getWidth()
                || command.sourceY + command.storedHeight > atlas.getHeight()) {
            throw new IllegalStateException("frame outside atlas: " + command.frameName);
        }
    }
}
