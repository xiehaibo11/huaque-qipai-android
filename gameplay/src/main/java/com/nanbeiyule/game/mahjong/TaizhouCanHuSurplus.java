package com.nanbeiyule.game.mahjong;

import com.nanbeiyule.game.gameplay.GameplayMeld;
import com.nanbeiyule.game.gameplay.GameplaySeatFlowers;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import java.util.List;

/**
 * {@code CanHuMahsUI:getSurplusMahs} 的移植：一张可胡牌在牌墙里还剩几张。
 *
 * <p>原版从 4 张（花牌 1 张）起减：补花区、本家手牌、本家单放、四家副露（杠已按
 * 已知牌值补齐四张）、四家牌河。这里的减项全部来自服务端已下发的公开/自家数据，
 * 没有任何推测；缺失的那一项（原版的开牌区 {@code getOpenData}）台州不发。
 */
public final class TaizhouCanHuSurplus {
    /** {@code mahID >= 97}：花牌整副只有一张。 */
    public static final int FLOWER_FIRST_VALUE = 0x61;

    private static final int NORMAL_TILE_COPIES = 4;

    private static final int FLOWER_TILE_COPIES = 1;

    private TaizhouCanHuSurplus() {}

    /** 返回 {@code tileValue} 的剩余张数，下限 0。 */
    public static int remaining(GameplayTableState state, int tileValue) {
        boolean flower = tileValue >= FLOWER_FIRST_VALUE;
        int count = flower ? FLOWER_TILE_COPIES : NORMAL_TILE_COPIES;
        if (state == null) {
            return count;
        }
        if (flower) {
            for (GameplaySeatFlowers flowers : state.flowers()) {
                count -= occurrences(flowers.tiles(), tileValue);
            }
        }
        count -= inOwnHand(state, tileValue);
        count -= inMelds(state.melds(), tileValue);
        count -= inRivers(state, tileValue);
        return Math.max(0, count);
    }

    private static int inOwnHand(GameplayTableState state, int tileValue) {
        TaizhouMahjongVisibleRound round = state.visibleRound().orElse(null);
        if (round == null) {
            return 0;
        }
        TaizhouMahjongVisibleRound.SeatHand hand = round.handAt(state.mySeat());
        int count = occurrences(hand.concealedTiles(), tileValue);
        if (hand.drawnTile() != null && hand.drawnTile() == tileValue) {
            count++;
        }
        return count;
    }

    /** 杠的四张里只有一张露值，原版先把整组补成同一牌值再统计。 */
    private static int inMelds(List<GameplayMeld> melds, int tileValue) {
        int count = 0;
        for (GameplayMeld meld : melds) {
            List<Integer> tiles = meld.tiles();
            if (tiles.size() == 4) {
                int kongValue = MahjongTile.BACK;
                for (int tile : tiles) {
                    if (tile != MahjongTile.BACK) {
                        kongValue = tile;
                        break;
                    }
                }
                if (kongValue != MahjongTile.BACK) {
                    count += kongValue == tileValue ? tiles.size() : 0;
                    continue;
                }
            }
            count += occurrences(tiles, tileValue);
        }
        return count;
    }

    private static int inRivers(GameplayTableState state, int tileValue) {
        TaizhouMahjongVisibleRound round = state.visibleRound().orElse(null);
        if (round == null) {
            return 0;
        }
        int count = 0;
        for (TaizhouMahjongVisibleRound.SeatRiver river : round.rivers()) {
            count += occurrences(river.tiles(), tileValue);
        }
        return count;
    }

    private static int occurrences(List<Integer> tiles, int tileValue) {
        int count = 0;
        for (int tile : tiles) {
            if (tile == tileValue) {
                count++;
            }
        }
        return count;
    }
}
