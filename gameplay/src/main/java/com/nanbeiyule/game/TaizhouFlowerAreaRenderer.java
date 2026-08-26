package com.nanbeiyule.game;

import android.graphics.Canvas;
import com.nanbeiyule.game.gameplay.GameplaySeatFlowers;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.MahjongTile;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouMahjongFlowerLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongHandLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import java.util.Objects;

/**
 * Draws every seat's flower row (补花区) from the server-projected
 * {@link GameplayTableState#flowers()} using the original
 * {@link TaizhouMahjongFlowerLayout} geometry (the {@code KW_FLOWER} roots at
 * 0.4 scale and the per-seat stacking of {@code UIMahFlowerArea}).
 */
final class TaizhouFlowerAreaRenderer {
    private static final long DAZHONG_TAIZHOU_MAHJONG_GAME_ID = 30109L;

    private final OriginalMahjongTilePainter tilePainter;

    TaizhouFlowerAreaRenderer(OriginalMahjongTilePainter tilePainter) {
        this.tilePainter = Objects.requireNonNull(tilePainter, "tilePainter");
    }

    void draw(Canvas canvas, GameplayTableState state) {
        if (!shouldDraw(state)) {
            return;
        }
        for (GameplaySeatFlowers seatFlowers : state.flowers()) {
            if (seatFlowers.seatNumber() > state.chairCount()) {
                continue;
            }
            int localSeat =
                    TaizhouMahjongSeatMapper.toLocalSeat(
                            seatFlowers.seatNumber(), state.mySeat(), state.chairCount());
            for (int index = 0; index < seatFlowers.tiles().size(); index++) {
                int tile = seatFlowers.tiles().get(index);
                if (!MahjongTile.hasTaizhouFace(tile)) {
                    continue;
                }
                TaizhouMahjongHandLayout.TilePosition position =
                        TaizhouMahjongFlowerLayout.flowerTile(localSeat, index);
                tilePainter.draw(
                        canvas,
                        OriginalMahjongTileDrawPlan.atAnchor(
                                position.pose,
                                tile,
                                position.designX,
                                position.cocosY,
                                position.effectiveScale,
                                position.anchorX,
                                position.anchorY));
            }
        }
    }

    static boolean shouldDraw(GameplayTableState state) {
        return state != null
                && state.gameId() != DAZHONG_TAIZHOU_MAHJONG_GAME_ID
                && !state.flowers().isEmpty();
    }
}
