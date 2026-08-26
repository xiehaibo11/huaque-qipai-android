package com.nanbeiyule.game;

import android.graphics.Canvas;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouMahjongDiscardProjection;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayInteraction;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Draws only server-confirmed river tiles using UIMahPlayerOutArea geometry. */
final class TaizhouMahjongDiscardRenderer {
    private static final int NORMAL = 0xffffffff;
    private static final int SAME_VALUE = 0xffffc9aa;

    private final OriginalMahjongTilePainter tilePainter;

    TaizhouMahjongDiscardRenderer(OriginalMahjongTilePainter tilePainter) {
        this.tilePainter = Objects.requireNonNull(tilePainter, "tilePainter");
    }

    void draw(
            Canvas canvas,
            TaizhouMahjongVisibleRound round,
            TaizhouMahjongPlayInteraction interaction) {
        if (round == null) {
            return;
        }
        Integer selectedValue = selectedValue(interaction);
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
            }
        }
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
}
