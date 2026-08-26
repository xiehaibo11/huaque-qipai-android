package com.nanbeiyule.game.mahjong;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Projects one server seat's private hand into original 2D positions and overlap order. */
public final class TaizhouMahjongHandProjection {
    private static final int ORIGINAL_MAX_HAND_MAHS_COUNT = 17;

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

    /** Returns tiles in the same back-to-front order that Cocos local z-order produces. */
    public static List<Tile> forSeat(
            TaizhouMahjongVisibleRound round, int serverSeat) {
        Objects.requireNonNull(round, "round");
        return forSeat(round, serverSeat, round.handAt(serverSeat).meldCount());
    }

    /**
     * Projects a hand using the meld count that the meld layer can actually
     * render. This keeps the hand area and the exposed-meld layer synchronized
     * when seat-private {@code meldCount} and public meld payloads arrive on
     * different event boundaries.
     */
    public static List<Tile> forSeat(
            TaizhouMahjongVisibleRound round, int serverSeat, int renderedMeldCount) {
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
                                    localSeat, index, renderedMeldCount, false)));
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
                                    localSeat, handCount, renderedMeldCount)));
        }
        result.sort(Comparator.comparingInt(Tile::localZOrder));
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
