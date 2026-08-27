package com.nanbeiyule.game.mahjong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public final class TaizhouMahjongDiscardProjectionTest {
    @Test
    public void lastDiscardCursorSitsAboveTheMarkedRiverTile() {
        TaizhouMahjongVisibleRound round = round(List.of(17, 18));

        TaizhouMahjongDiscardProjection.Cursor cursor =
                TaizhouMahjongDiscardProjection.lastDiscardCursor(round).orElseThrow();
        TaizhouMahjongDiscardProjection.Tile tile =
                TaizhouMahjongDiscardProjection
                        .forLocalSeat(
                                TaizhouMahjongTableLayout.SEAT_BOTTOM,
                                4,
                                List.of(17, 18),
                                3)
                        .get(1);

        float width = MahjongTileSprite.topEdgeWidth(tile.pose()) * tile.effectiveScale();
        float height = MahjongTileSprite.leftRightEdgeWidth(tile.pose()) * tile.effectiveScale();
        float left = tile.designX() - tile.anchorX() * width;
        float bottom = tile.cocosY() - tile.anchorY() * height;

        assertEquals(TaizhouMahjongTableLayout.SEAT_BOTTOM, cursor.localSeat());
        assertEquals(left + width / 2.0f, cursor.designX(), 0.001f);
        assertEquals(TaizhouMahjongTableLayout.designY(bottom + height + 20.0f), cursor.androidY(), 0.001f);
    }

    @Test
    public void lastDiscardCursorIsEmptyWithoutMarker() {
        assertTrue(TaizhouMahjongDiscardProjection.lastDiscardCursor(round(List.of())).isEmpty());
    }

    private static TaizhouMahjongVisibleRound round(List<Integer> river) {
        return new TaizhouMahjongVisibleRound(
                4,
                1,
                List.of(
                        new TaizhouMahjongVisibleRound.SeatHand(1, List.of(17, 18), null, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(2, 2, false, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(3, 2, false, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(4, 2, false, 0)),
                List.of(),
                List.of(),
                List.of(
                        new TaizhouMahjongVisibleRound.SeatRiver(1, river, 3),
                        new TaizhouMahjongVisibleRound.SeatRiver(2, List.of(), 3),
                        new TaizhouMahjongVisibleRound.SeatRiver(3, List.of(), 3),
                        new TaizhouMahjongVisibleRound.SeatRiver(4, List.of(), 3)),
                river.isEmpty()
                        ? null
                        : new TaizhouMahjongVisibleRound.LastDiscard(1, river.size() - 1));
    }
}
