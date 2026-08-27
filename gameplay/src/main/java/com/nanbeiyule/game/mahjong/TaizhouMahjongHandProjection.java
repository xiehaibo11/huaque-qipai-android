package com.nanbeiyule.game.mahjong;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Projects one server seat's private hand into original 2D positions and overlap order. */
public final class TaizhouMahjongHandProjection {
    private static final int ORIGINAL_MAX_HAND_MAHS_COUNT = 17;
    private static final List<Integer> ORIGINAL_LOCAL_DRAW_ORDER =
            List.of(
                    TaizhouMahjongTableLayout.SEAT_LEFT,
                    TaizhouMahjongTableLayout.SEAT_RIGHT,
                    TaizhouMahjongTableLayout.SEAT_TOP,
                    TaizhouMahjongTableLayout.SEAT_BOTTOM);

    public record Tile(
            int serverSeat,
            int localSeat,
            int tileValue,
            int handIndex,
            boolean drawn,
            int localZOrder,
            TaizhouMahjongHandLayout.TilePosition position) {
        public Tile {
            Objects.requireNonNull(position, "position");
        }
    }

    private TaizhouMahjongHandProjection() {}

    /**
     * Returns tiles in the same back-to-front order that Cocos local z-order
     * produces, starting after the melds the meld layer can actually
     * render ({@code UIMahHandArea:_getHandMahsStartPos}). This keeps the hand
     * area and the exposed-meld layer synchronized when seat-private
     * {@code meldCount} and public meld payloads arrive on different event
     * boundaries.
     */
    public static List<Tile> forSeat(
            TaizhouMahjongVisibleRound round, int serverSeat, float meldStartOffset) {
        Objects.requireNonNull(round, "round");
        TaizhouMahjongVisibleRound.SeatHand hand = round.handAt(serverSeat);
        int localSeat =
                TaizhouMahjongSeatMapper.toLocalSeat(
                        serverSeat, round.mySeat(), round.chairCount());
        int direction = addDirection(localSeat);
        List<Tile> result = new ArrayList<>(hand.concealedTiles().size() + 1);
        for (int index = 0; index < hand.concealedTiles().size(); index++) {
            result.add(
                    new Tile(
                            serverSeat,
                            localSeat,
                            hand.concealedTiles().get(index),
                            index,
                            false,
                            ORIGINAL_MAX_HAND_MAHS_COUNT - index * direction,
                            TaizhouMahjongHandLayout.handTile(
                                    localSeat, index, meldStartOffset, false)));
        }
        if (hand.drawnTile() != null) {
            int handCount = hand.concealedTiles().size();
            result.add(
                    new Tile(
                            serverSeat,
                            localSeat,
                            hand.drawnTile(),
                            -1,
                            true,
                            ORIGINAL_MAX_HAND_MAHS_COUNT - handCount * direction,
                            TaizhouMahjongHandLayout.drawnTile(
                                    localSeat, handCount, meldStartOffset)));
        }
        result.sort(Comparator.comparingInt(Tile::localZOrder));
        return List.copyOf(result);
    }

    public static List<Integer> serverSeatDrawOrder(TaizhouMahjongVisibleRound round) {
        Objects.requireNonNull(round, "round");
        List<Integer> result = new ArrayList<>(round.chairCount());
        for (int localSeat : ORIGINAL_LOCAL_DRAW_ORDER) {
            for (int serverSeat = 1; serverSeat <= round.chairCount(); serverSeat++) {
                if (TaizhouMahjongSeatMapper.toLocalSeat(
                                serverSeat, round.mySeat(), round.chairCount())
                        == localSeat) {
                    result.add(serverSeat);
                    break;
                }
            }
        }
        return List.copyOf(result);
    }

    private static int addDirection(int localSeat) {
        switch (localSeat) {
            case TaizhouMahjongTableLayout.SEAT_BOTTOM:
            case TaizhouMahjongTableLayout.SEAT_RIGHT:
                return 1;
            case TaizhouMahjongTableLayout.SEAT_LEFT:
            case TaizhouMahjongTableLayout.SEAT_TOP:
                return -1;
            default:
                throw new IllegalArgumentException("unknown local seat " + localSeat);
        }
    }
}
