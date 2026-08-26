package com.nanbeiyule.game.mahjong;

/** Relative-seat mapping from GameBase/Data/RoomData.lua lines 350-375. */
public final class TaizhouMahjongSeatMapper {
    private TaizhouMahjongSeatMapper() {}

    /** Maps one-based server seats to the original LEFT/BOTTOM/RIGHT/TOP enum. */
    public static int toLocalSeat(int seatNumber, int mySeat, int chairCount) {
        if (chairCount != 2 && chairCount != 4) {
            throw new IllegalArgumentException("chairCount must be 2 or 4");
        }
        if (seatNumber < 1 || seatNumber > chairCount || mySeat < 1 || mySeat > chairCount) {
            throw new IllegalArgumentException("seat is outside chairCount");
        }

        int serverSeat = seatNumber - 1;
        int selfSeat = mySeat - 1;
        int localSeat = ((serverSeat - selfSeat + chairCount) % chairCount + 1) % chairCount + 1;
        if (chairCount == 2 && localSeat == TaizhouMahjongTableLayout.SEAT_LEFT) {
            return TaizhouMahjongTableLayout.SEAT_TOP;
        }
        return localSeat;
    }
}
