package com.nanbeiyule.game;

import android.graphics.Canvas;
import com.nanbeiyule.game.gameplay.GameplayMeld;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.MahjongTile;
import com.nanbeiyule.game.mahjong.OriginalMahjongTileDrawPlan;
import com.nanbeiyule.game.mahjong.OriginalMahjongTilePainter;
import com.nanbeiyule.game.mahjong.TaizhouMahjongMeldLayout;
import com.nanbeiyule.game.mahjong.TaizhouMahjongSeatMapper;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Draws every seat's meld area (副露) from the server-projected
 * {@link GameplayTableState#melds()} using the original
 * {@link TaizhouMahjongMeldLayout} geometry. The renderer only consumes
 * authoritative meld events; it never fabricates a combination locally.
 */
final class TaizhouMeldRenderer {
    private final OriginalMahjongTilePainter tilePainter;

    TaizhouMeldRenderer(OriginalMahjongTilePainter tilePainter) {
        this.tilePainter = Objects.requireNonNull(tilePainter, "tilePainter");
    }

    void draw(
            Canvas canvas,
            GameplayTableState state,
            TaizhouMahjongVisibleRound visibleRound) {
        if (state == null || state.melds().isEmpty()) {
            return;
        }
        for (int serverSeat = 1; serverSeat <= state.chairCount(); serverSeat++) {
            List<GameplayMeld> seatMelds = meldsOf(state.melds(), serverSeat);
            if (seatMelds.isEmpty()) {
                continue;
            }
            int localSeat =
                    TaizhouMahjongSeatMapper.toLocalSeat(
                            serverSeat, state.mySeat(), state.chairCount());
            int renderableMeldCount =
                    visibleRound == null
                            ? seatMelds.size()
                            : renderableMeldCount(
                                    seatMelds.size(),
                                    visibleRound.handAt(serverSeat).meldCount());
            for (TaizhouMahjongMeldLayout.TilePlacement placement :
                    TaizhouMahjongMeldLayout.seatMelds(
                            localSeat,
                            seatMelds.subList(0, renderableMeldCount),
                            state.mySeat(),
                            state.chairCount())) {
                if (placement.tileValue != MahjongTile.BACK
                        && !MahjongTile.hasTaizhouFace(placement.tileValue)) {
                    continue;
                }
                tilePainter.draw(
                        canvas,
                        OriginalMahjongTileDrawPlan.atAnchor(
                                placement.pose,
                                placement.tileValue,
                                placement.designX,
                                placement.cocosY,
                                placement.scale,
                                0.5f,
                                0.5f,
                                visibleRound != null
                                        && visibleRound.jokerTiles().contains(placement.tileValue)));
            }
        }
    }

    /**
     * Public melds arrive before the private hand update on some event boundaries.
     * Keep the two layers atomic so a new meld cannot be painted over the old hand.
     */
    static int renderableMeldCount(int publicMeldCount, int visibleMeldCount) {
        if (publicMeldCount < 0 || visibleMeldCount < 0) {
            throw new IllegalArgumentException("meld counts must be non-negative");
        }
        return Math.min(publicMeldCount, visibleMeldCount);
    }

    private static List<GameplayMeld> meldsOf(List<GameplayMeld> melds, int serverSeat) {
        List<GameplayMeld> result = new ArrayList<>();
        for (GameplayMeld meld : melds) {
            if (meld.seat() == serverSeat) {
                result.add(meld);
            }
        }
        return result;
    }
}
