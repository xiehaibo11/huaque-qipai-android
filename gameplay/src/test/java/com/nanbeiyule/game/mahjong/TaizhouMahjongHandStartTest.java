package com.nanbeiyule.game.mahjong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.gameplay.GameplayMeld;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.List;
import org.junit.Test;

/**
 * 手牌起点必须落在副露之后（原版 {@code UIMahHandArea:_getHandMahsStartPos}）：
 * BOTTOM 走 {@code CombTotalLength = 405}，其余三家走最后一组的包围盒。
 */
public final class TaizhouMahjongHandStartTest {
    private static final int CHAIRS = 4;

    private static final int MY_SEAT = 1;

    @Test
    public void bottomUsesTheFixedCombTotalLength() {
        assertEquals(0.0f, TaizhouMahjongHandLayout.bottomMeldStartOffset(0), 0.0f);
        assertEquals(
                2.0f * TaizhouMahjongHandLayout.BOTTOM_MELD_LENGTH,
                TaizhouMahjongHandLayout.bottomMeldStartOffset(2),
                0.0f);
    }

    @Test
    public void handTilesStepAwayFromTheMeldStart() {
        float offset =
                TaizhouMahjongMeldLayout.handStartOffset(
                        TaizhouMahjongTableLayout.SEAT_LEFT,
                        List.of(pung(4, 1)),
                        MY_SEAT,
                        CHAIRS);
        TaizhouMahjongHandLayout.TilePosition first =
                TaizhouMahjongHandLayout.handTile(
                        TaizhouMahjongTableLayout.SEAT_LEFT, 0, offset, false);
        TaizhouMahjongHandLayout.TilePosition second =
                TaizhouMahjongHandLayout.handTile(
                        TaizhouMahjongTableLayout.SEAT_LEFT, 1, offset, false);
        float root = TaizhouMahjongTableLayout.HAND_LEFT.scale;
        float step =
                MahjongTileSprite.topEdgeWidth(MahjongTileSprite.STAND_FACE_TO_LEFT) * 0.9f;
        assertEquals(
                TaizhouMahjongTableLayout.HAND_LEFT.cocosY() + offset * root,
                first.cocosY,
                0.001f);
        assertEquals(-step * root, second.cocosY - first.cocosY, 0.001f);
    }

    /** 副露把手牌推走之后，第一张手牌不再压在最后一张副露牌上。 */
    @Test
    public void meldsAndHandNoLongerShareTheSameOrigin() {
        List<GameplayMeld> melds = List.of(pung(4, 1));
        float offset =
                TaizhouMahjongMeldLayout.handStartOffset(
                        TaizhouMahjongTableLayout.SEAT_LEFT, melds, MY_SEAT, CHAIRS);
        TaizhouMahjongHandLayout.TilePosition first =
                TaizhouMahjongHandLayout.handTile(
                        TaizhouMahjongTableLayout.SEAT_LEFT, 0, offset, false);
        float lowestMeldY = Float.MAX_VALUE;
        for (TaizhouMahjongMeldLayout.TilePlacement placement :
                TaizhouMahjongMeldLayout.seatMelds(
                        TaizhouMahjongTableLayout.SEAT_LEFT, melds, MY_SEAT, CHAIRS)) {
            lowestMeldY = Math.min(lowestMeldY, placement.cocosY);
        }
        assertTrue(first.cocosY < lowestMeldY);
    }

    @Test
    public void topSeatHandGrowsLeftFromTheMeldStart() {
        float offset =
                TaizhouMahjongMeldLayout.handStartOffset(
                        TaizhouMahjongTableLayout.SEAT_TOP,
                        List.of(pung(3, 2)),
                        MY_SEAT,
                        CHAIRS);
        TaizhouMahjongHandLayout.TilePosition first =
                TaizhouMahjongHandLayout.handTile(
                        TaizhouMahjongTableLayout.SEAT_TOP, 0, offset, false);
        TaizhouMahjongHandLayout.TilePosition second =
                TaizhouMahjongHandLayout.handTile(
                        TaizhouMahjongTableLayout.SEAT_TOP, 1, offset, false);
        float root = TaizhouMahjongTableLayout.HAND_TOP.scale;
        float step =
                MahjongTileSprite.topEdgeWidth(MahjongTileSprite.STAND_FACE_BACKWARD) * 0.9f;
        assertEquals(
                TaizhouMahjongTableLayout.HAND_TOP.designX() + offset * root,
                first.designX,
                0.001f);
        assertEquals(-step * root, second.designX - first.designX, 0.001f);
    }

    @Test
    public void bottomHandUsesTheOriginalTopMostHandStripOrder() {
        TaizhouMahjongVisibleRound round =
                new TaizhouMahjongVisibleRound(
                        4,
                        1,
                        List.of(
                                new TaizhouMahjongVisibleRound.SeatHand(1, List.of(17), null, 0),
                                TaizhouMahjongVisibleRound.SeatHand.opponent(2, 1, false, 0),
                                TaizhouMahjongVisibleRound.SeatHand.opponent(3, 1, false, 0),
                                TaizhouMahjongVisibleRound.SeatHand.opponent(4, 1, false, 0)),
                        List.of(),
                        List.of());

        assertEquals(List.of(4, 2, 3, 1), TaizhouMahjongHandProjection.serverSeatDrawOrder(round));
    }

    private static GameplayMeld pung(int seat, int fromSeat) {
        return new GameplayMeld(seat, MahjongCombType.PONG, List.of(0x11, 0x11, 0x11), fromSeat);
    }
}
