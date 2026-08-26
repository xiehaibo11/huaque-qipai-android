package com.nanbeiyule.game.mahjong;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Player-private round projection matching the original player/front and player/back messages. */
public record TaizhouMahjongVisibleRound(
        int chairCount,
        int mySeat,
        List<SeatHand> hands,
        List<Integer> jokerTiles,
        List<Integer> insteadTiles,
        List<SeatRiver> rivers,
        LastDiscard lastDiscard,
        Integer dealerSeat) {
    public record SeatHand(
            int seatNumber,
            List<Integer> concealedTiles,
            Integer drawnTile,
            int meldCount) {
        public SeatHand {
            if (seatNumber <= 0) {
                throw new IllegalArgumentException("seatNumber must be positive");
            }
            concealedTiles = List.copyOf(Objects.requireNonNull(concealedTiles, "concealedTiles"));
            if (concealedTiles.size() > TaizhouMahjongHandLayout.GENERIC_DRAWABLE_CAPACITY) {
                throw new IllegalArgumentException("too many concealed tiles");
            }
            for (Integer tile : concealedTiles) {
                requireDefinedTile(tile);
            }
            if (drawnTile != null) {
                requireDefinedTile(drawnTile);
            }
            if (meldCount < 0 || meldCount > MahjongSeatAreaLayout.MAX_COMBS_COUNT) {
                throw new IllegalArgumentException("invalid meldCount");
            }
        }

        public static SeatHand opponent(
                int seatNumber, int concealedCount, boolean hasDrawnTile, int meldCount) {
            if (concealedCount < 0
                    || concealedCount > TaizhouMahjongHandLayout.GENERIC_DRAWABLE_CAPACITY) {
                throw new IllegalArgumentException("invalid opponent concealed count");
            }
            List<Integer> backs = new ArrayList<>(concealedCount);
            for (int index = 0; index < concealedCount; index++) {
                backs.add(MahjongTile.BACK);
            }
            return new SeatHand(
                    seatNumber,
                    backs,
                    hasDrawnTile ? MahjongTile.BACK : null,
                    meldCount);
        }
    }

    public record SeatRiver(int seatNumber, List<Integer> tiles, int maxLineCount) {
        public SeatRiver {
            if (seatNumber <= 0 || (maxLineCount != 2 && maxLineCount != 3)) {
                throw new IllegalArgumentException("invalid river metadata");
            }
            tiles = List.copyOf(Objects.requireNonNull(tiles, "tiles"));
            for (Integer tile : tiles) {
                if (tile == null || !MahjongTile.hasTaizhouFace(tile)) {
                    throw new IllegalArgumentException("river contains an unrenderable tile");
                }
            }
        }
    }

    public record LastDiscard(int seatNumber, int tileIndex) {
        public LastDiscard {
            if (seatNumber <= 0 || tileIndex < 0) {
                throw new IllegalArgumentException("invalid last discard");
            }
        }
    }

    public TaizhouMahjongVisibleRound(
            int chairCount,
            int mySeat,
            List<SeatHand> hands,
            List<Integer> jokerTiles,
            List<Integer> insteadTiles) {
        this(
                chairCount,
                mySeat,
                hands,
                jokerTiles,
                insteadTiles,
                emptyRivers(chairCount),
                null,
                null);
    }

    /**
     * 旧七参形态：庄家座位未知（{@code dealerSeat = null}），中心转向盘不旋转。
     */
    public TaizhouMahjongVisibleRound(
            int chairCount,
            int mySeat,
            List<SeatHand> hands,
            List<Integer> jokerTiles,
            List<Integer> insteadTiles,
            List<SeatRiver> rivers,
            LastDiscard lastDiscard) {
        this(chairCount, mySeat, hands, jokerTiles, insteadTiles, rivers, lastDiscard, null);
    }

    /**
     * 庄家的本地座位（LEFT/BOTTOM/RIGHT/TOP），供中心转向盘按原版整块旋转；未知时返回 0。
     *
     * <p>原版 {@code GameModule:rotateWindPos}（{@code BasicMahjong/.../GameLayer/Module.luac:870-875}）
     * 用 {@code CF.roomData:seatToLocal(data.nBanker)} 取本地座位。
     */
    public TaizhouMahjongVisibleRound withDealerSeat(Integer nextDealerSeat) {
        return Objects.equals(dealerSeat, nextDealerSeat)
                ? this
                : new TaizhouMahjongVisibleRound(
                        chairCount,
                        mySeat,
                        hands,
                        jokerTiles,
                        insteadTiles,
                        rivers,
                        lastDiscard,
                        nextDealerSeat);
    }

    public int bankerLocalSeat() {
        return dealerSeat == null
                ? 0
                : TaizhouMahjongSeatMapper.toLocalSeat(dealerSeat, mySeat, chairCount);
    }

    public TaizhouMahjongVisibleRound {
        if ((chairCount != 2 && chairCount != 4) || mySeat <= 0 || mySeat > chairCount) {
            throw new IllegalArgumentException("invalid round seats");
        }
        jokerTiles = copyFaceTiles(jokerTiles, "jokerTiles");
        insteadTiles = copyFaceTiles(insteadTiles, "insteadTiles");
        hands = normalizeHands(chairCount, mySeat, hands, jokerTiles, insteadTiles);
        rivers = normalizeRivers(chairCount, rivers);
        validateLastDiscard(chairCount, rivers, lastDiscard);
        if (dealerSeat != null && (dealerSeat < 1 || dealerSeat > chairCount)) {
            throw new IllegalArgumentException("dealerSeat is outside chairCount");
        }
    }

    public SeatHand handAt(int seatNumber) {
        if (seatNumber <= 0 || seatNumber > chairCount) {
            throw new IllegalArgumentException("seat is outside chairCount");
        }
        return hands.get(seatNumber - 1);
    }

    public SeatRiver riverAt(int seatNumber) {
        if (seatNumber <= 0 || seatNumber > chairCount) {
            throw new IllegalArgumentException("seat is outside chairCount");
        }
        return rivers.get(seatNumber - 1);
    }

    private static List<SeatRiver> emptyRivers(int chairCount) {
        if (chairCount != 2 && chairCount != 4) {
            throw new IllegalArgumentException("invalid round seats");
        }
        List<SeatRiver> result = new ArrayList<>(chairCount);
        int maxLineCount = chairCount == 2 ? 2 : 3;
        for (int seat = 1; seat <= chairCount; seat++) {
            result.add(new SeatRiver(seat, List.of(), maxLineCount));
        }
        return result;
    }

    private static List<SeatRiver> normalizeRivers(
            int chairCount, List<SeatRiver> source) {
        Objects.requireNonNull(source, "rivers");
        if (source.size() != chairCount) {
            throw new IllegalArgumentException("one visible river is required per chair");
        }
        SeatRiver[] ordered = new SeatRiver[chairCount];
        Set<Integer> seen = new HashSet<>();
        for (SeatRiver river : source) {
            Objects.requireNonNull(river, "river");
            int seat = river.seatNumber();
            if (seat > chairCount || !seen.add(seat)) {
                throw new IllegalArgumentException("duplicate or invalid river seat");
            }
            if (chairCount == 2 && river.maxLineCount() != 2) {
                throw new IllegalArgumentException("two-player rivers use two lines");
            }
            ordered[seat - 1] = river;
        }
        return List.of(ordered);
    }

    private static void validateLastDiscard(
            int chairCount, List<SeatRiver> rivers, LastDiscard lastDiscard) {
        if (lastDiscard == null) {
            return;
        }
        if (lastDiscard.seatNumber() > chairCount) {
            throw new IllegalArgumentException("last discard seat is outside chairCount");
        }
        List<Integer> tiles = rivers.get(lastDiscard.seatNumber() - 1).tiles();
        if (tiles.isEmpty() || lastDiscard.tileIndex() != tiles.size() - 1) {
            throw new IllegalArgumentException("last discard is not the last visible river tile");
        }
    }

    private static List<SeatHand> normalizeHands(
            int chairCount,
            int mySeat,
            List<SeatHand> source,
            List<Integer> jokerTiles,
            List<Integer> insteadTiles) {
        Objects.requireNonNull(source, "hands");
        if (source.size() != chairCount) {
            throw new IllegalArgumentException("one visible hand is required per chair");
        }
        SeatHand[] ordered = new SeatHand[chairCount];
        Set<Integer> seen = new HashSet<>();
        for (SeatHand hand : source) {
            Objects.requireNonNull(hand, "hand");
            int seat = hand.seatNumber();
            if (seat > chairCount || !seen.add(seat)) {
                throw new IllegalArgumentException("duplicate or invalid visible seat");
            }
            boolean ownHand = seat == mySeat;
            validatePrivacy(hand, ownHand);
            List<Integer> tiles = new ArrayList<>(hand.concealedTiles());
            if (ownHand) {
                tiles.sort(originalComparator(jokerTiles, insteadTiles));
            }
            ordered[seat - 1] =
                    new SeatHand(seat, tiles, hand.drawnTile(), hand.meldCount());
        }
        return List.of(ordered);
    }

    private static void validatePrivacy(SeatHand hand, boolean ownHand) {
        for (int tile : hand.concealedTiles()) {
            validateVisibleTile(tile, ownHand);
        }
        if (hand.drawnTile() != null) {
            validateVisibleTile(hand.drawnTile(), ownHand);
        }
    }

    private static void validateVisibleTile(int tile, boolean ownHand) {
        if (ownHand ? !MahjongTile.hasTaizhouFace(tile) : tile != MahjongTile.BACK) {
            throw new IllegalArgumentException(
                    ownHand ? "local hand contains an unrenderable tile" : "opponent face leaked");
        }
    }

    private static Comparator<Integer> originalComparator(
            List<Integer> jokerTiles, List<Integer> insteadTiles) {
        return (first, second) -> {
            boolean firstJoker = jokerTiles.contains(first);
            boolean secondJoker = jokerTiles.contains(second);
            if (firstJoker != secondJoker) {
                return firstJoker ? -1 : 1;
            }
            return Integer.compare(
                    realTile(first, jokerTiles, insteadTiles),
                    realTile(second, jokerTiles, insteadTiles));
        };
    }

    private static int realTile(
            int tile, List<Integer> jokerTiles, List<Integer> insteadTiles) {
        return !jokerTiles.isEmpty() && insteadTiles.contains(tile) ? jokerTiles.get(0) : tile;
    }

    private static List<Integer> copyFaceTiles(List<Integer> source, String name) {
        List<Integer> copy = List.copyOf(Objects.requireNonNull(source, name));
        for (Integer tile : copy) {
            if (tile == null || !MahjongTile.hasTaizhouFace(tile)) {
                throw new IllegalArgumentException(name + " contains an unrenderable tile");
            }
        }
        return copy;
    }

    private static void requireDefinedTile(Integer tile) {
        if (tile == null || !MahjongTile.isValid(tile)) {
            throw new IllegalArgumentException("undefined mahjong tile");
        }
    }

    /**
     * 本局财神物理牌值的空安全读取。
     *
     * <p>牌桌在未开局、旧快照缺 {@code jokerTiles} 字段时都会拿到空列表，渲染层因此
     * 不画任何「财」角标（规格「错误与兼容处理」）。手牌、牌河、副露、胡牌提示和结算
     * 共用这一个入口，不各自写一份判空三元表达式。
     */
    public static List<Integer> jokerTilesOf(TaizhouMahjongVisibleRound round) {
        return round == null ? List.of() : round.jokerTiles();
    }
}
