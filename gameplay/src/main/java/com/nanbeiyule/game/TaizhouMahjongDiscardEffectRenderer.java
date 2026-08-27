package com.nanbeiyule.game;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import com.nanbeiyule.game.cocosarmature.ArmatureAtlas;
import com.nanbeiyule.game.cocosarmature.ArmatureData;
import com.nanbeiyule.game.cocosarmature.ArmatureExportJson;
import com.nanbeiyule.game.cocosarmature.ArmaturePlayer;
import com.nanbeiyule.game.mahjong.MahjongTileSprite;
import com.nanbeiyule.game.mahjong.TaizhouMahjongDiscardProjection;
import com.nanbeiyule.game.mahjong.TaizhouMahjongTableLayout;

final class TaizhouMahjongDiscardEffectRenderer {
    private static final String CURSOR_DIR = "taizhou_mahjong_cursor_effects/gb_ani";
    private static final String DISCARD_DIR = "taizhou_mahjong_discard_effects/chupai";

    private final ArmaturePlayer cursorPlayer;
    private final TaizhouMahjongFrameSequence discardSequence;

    TaizhouMahjongDiscardEffectRenderer(Context context) {
        AssetManager assets = context.getAssets();
        ArmatureData data = ArmatureExportJson.load(assets, CURSOR_DIR + "/gb_ani.ExportJson");
        ArmatureAtlas atlas =
                ArmatureAtlas.load(assets, CURSOR_DIR + "/gb_ani0.png", CURSOR_DIR + "/gb_ani0.json");
        cursorPlayer = new ArmaturePlayer(data, atlas);
        discardSequence = TaizhouMahjongFrameSequence.load(assets, DISCARD_DIR, "chupai", 50L);
    }

    void drawCursor(Canvas canvas, TaizhouMahjongDiscardProjection.Tile tile, long nowElapsed) {
        Point point = cursorPoint(tile);
        cursorPlayer.draw(canvas, nowElapsed / 1000.0f, point.x(), point.y(), tile.effectiveScale());
    }

    void drawDiscard(
            Canvas canvas, TaizhouMahjongDiscardProjection.Tile tile, long elapsedMillis) {
        Point point = tileCenter(tile);
        discardSequence.draw(canvas, elapsedMillis, point.x(), point.y(), 0.6f);
    }

    long discardDurationMillis() {
        return discardSequence.durationMillis();
    }

    void release() {
        cursorPlayer.recycle();
        discardSequence.recycle();
    }

    private static Point cursorPoint(TaizhouMahjongDiscardProjection.Tile tile) {
        float height = MahjongTileSprite.leftRightEdgeWidth(tile.pose());
        float x =
                tile.designX()
                        + (0.5f - tile.anchorX())
                                * MahjongTileSprite.topEdgeWidth(tile.pose())
                                * tile.effectiveScale();
        float yCocos =
                tile.cocosY()
                        + (height + 20.0f - tile.anchorY() * height) * tile.effectiveScale();
        return new Point(x, TaizhouMahjongTableLayout.designY(yCocos));
    }

    private static Point tileCenter(TaizhouMahjongDiscardProjection.Tile tile) {
        float width = MahjongTileSprite.topEdgeWidth(tile.pose());
        float height = MahjongTileSprite.leftRightEdgeWidth(tile.pose());
        float x = tile.designX() + (0.5f - tile.anchorX()) * width * tile.effectiveScale();
        float yCocos = tile.cocosY() + (0.5f - tile.anchorY()) * height * tile.effectiveScale();
        return new Point(x, TaizhouMahjongTableLayout.designY(yCocos));
    }

    private record Point(float x, float y) {}
}
