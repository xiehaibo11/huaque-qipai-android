package com.nanbeiyule.game.mahjong;

import static org.junit.Assert.assertEquals;

import com.nanbeiyule.game.gameplay.GameplayMeld;
import com.nanbeiyule.game.gameplay.GameplayPhase;
import com.nanbeiyule.game.gameplay.GameplaySeatFlowers;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

/** {@code CanHuMahsUI:getSurplusMahs} 的移植：可胡牌的剩余张数。 */
public final class TaizhouCanHuSurplusTest {
    private static final int TILE = 0x11;

    @Test
    public void anUntouchedTileHasFourCopies() {
        assertEquals(4, TaizhouCanHuSurplus.remaining(state(List.of(), List.of(), List.of()), TILE));
    }

    @Test
    public void ownHandAndRiversAndMeldsAreSubtracted() {
        GameplayTableState state =
                state(
                        List.of(TILE),
                        List.of(TILE),
                        List.of(
                                new GameplayMeld(
                                        2, MahjongCombType.PONG, List.of(0x12, 0x12, 0x12), 1)));
        assertEquals(2, TaizhouCanHuSurplus.remaining(state, TILE));
    }

    /** 杠只露一张牌值，原版先把整组补齐再统计。 */
    @Test
    public void concealedKongCountsFourCopies() {
        GameplayTableState state =
                state(
                        List.of(),
                        List.of(),
                        List.of(
                                new GameplayMeld(
                                        2,
                                        MahjongCombType.CONCEALED_KONG,
                                        List.of(TILE, MahjongTile.BACK,
                                                MahjongTile.BACK, MahjongTile.BACK),
                                        2)));
        assertEquals(0, TaizhouCanHuSurplus.remaining(state, TILE));
    }

    @Test
    public void neverGoesNegative() {
        GameplayTableState state =
                state(List.of(TILE, TILE, TILE), List.of(TILE, TILE), List.of());
        assertEquals(0, TaizhouCanHuSurplus.remaining(state, TILE));
    }

    /** 花牌整副只有一张，补花区拿走后就没了。 */
    @Test
    public void flowerTilesStartAtOneCopy() {
        int flower = TaizhouCanHuSurplus.FLOWER_FIRST_VALUE;
        assertEquals(1, TaizhouCanHuSurplus.remaining(state(List.of(), List.of(), List.of()), flower));
        GameplayTableState taken =
                state(List.of(), List.of(), List.of(), List.of(new GameplaySeatFlowers(3, List.of(flower))));
        assertEquals(0, TaizhouCanHuSurplus.remaining(taken, flower));
    }

    private static GameplayTableState state(
            List<Integer> ownHand, List<Integer> river, List<GameplayMeld> melds) {
        return state(ownHand, river, melds, List.of());
    }

    private static GameplayTableState state(
            List<Integer> ownHand,
            List<Integer> river,
            List<GameplayMeld> melds,
            List<GameplaySeatFlowers> flowers) {
        List<TaizhouMahjongVisibleRound.SeatHand> hands =
                List.of(
                        new TaizhouMahjongVisibleRound.SeatHand(1, ownHand, null, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(2, 0, false, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(3, 0, false, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(4, 0, false, 0));
        List<TaizhouMahjongVisibleRound.SeatRiver> rivers =
                List.of(
                        new TaizhouMahjongVisibleRound.SeatRiver(1, List.of(), 3),
                        new TaizhouMahjongVisibleRound.SeatRiver(2, river, 3),
                        new TaizhouMahjongVisibleRound.SeatRiver(3, List.of(), 3),
                        new TaizhouMahjongVisibleRound.SeatRiver(4, List.of(), 3));
        TaizhouMahjongVisibleRound round =
                new TaizhouMahjongVisibleRound(
                        4, 1, hands, List.of(), List.of(), rivers, null);
        return new GameplayTableState(
                "session",
                "147514",
                30109L,
                GameplayPhase.PLAYING,
                1,
                1L,
                1,
                4,
                8,
                "台州麻将",
                false,
                1,
                List.of(),
                Optional.of(round),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                1,
                null,
                100,
                "2026-08-26T00:00:00Z",
                Optional.empty(),
                melds,
                flowers,
                Optional.empty(),
                Optional.empty(),
                null,
                null,
                Optional.empty(),
                Optional.empty());
    }
}
