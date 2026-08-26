package com.nanbeiyule.game.mahjong.round;

import com.nanbeiyule.game.mahjong.MahjongTile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 1:1 port of the original {@code GameData:getSurplusMahs} remaining-tile
 * counter, recovered from
 * {@code src/game/Mahjong/BasicMahjong/Modules/GameLayer/GameData.luac:1063-1164}.
 *
 * <p>Every deduction block carries the Lua line range it mirrors. The Lua
 * loops run over {@code 0..getMaxPlayer()-1}; here they run over
 * {@link TaizhouRoundState#seats()}, which holds exactly one entry per chair.
 *
 * <p>This is only a display-side counter (听牌提示剩余张数,
 * {@code ConfigData:needGetSurplusMahCount = true}, Basic ConfigData.luac:52-54);
 * it decides nothing about walls, dealing or winning.
 */
public final class MahjongSurplusCounter {
    /** :1065 — 普通牌从 4 张起计。 */
    static final int NORMAL_COPIES = 4;
    /** :1069 — 花牌从 1 张起计。 */
    static final int FLOWER_COPIES = 1;
    /** :1068 — {@code mahID >= 97} 判为花牌（97 = 0x61 = HUA_MEI）。 */
    static final int FLOWER_MIN_VALUE = 97;

    private MahjongSurplusCounter() {}

    /**
     * Returns the remaining unseen count of {@code mahID}.
     *
     * @param state current round state (areas are read, never mutated — the
     *     Lua {@code clone} at :1112 is mirrored by copying kong melds)
     * @param mahID tile value in the original encoding (see {@link MahjongTile})
     * @param outMahsPutType client discard-area setting, read by the Lua from
     *     {@code settingData:getOutTableCardStyle()} at :1137
     */
    public static int getSurplusMahs(
            TaizhouRoundState state, int mahID, MahjongOutMahsPutType outMahsPutType) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(outMahsPutType, "outMahsPutType");
        int count = NORMAL_COPIES; // :1065
        // :1066 — 本方座位 localToSeat(LOCAL_SEAT.BOTTOM)，即 mySeat。
        TaizhouRoundSeatState ownSeat = state.seatAt(state.mySeat());
        if (mahID >= FLOWER_MIN_VALUE) { // :1068
            count = FLOWER_COPIES; // :1069
            // :1070-1081 — 补花区（只在花牌分支内扣除）。
            for (TaizhouRoundSeatState seat : state.seats()) {
                for (int replaceMah : seat.flowerTiles()) {
                    if (replaceMah == mahID) {
                        count--;
                    }
                }
            }
        }
        // :1082-1090 — 开牌。
        for (int openMah : state.openTiles()) {
            if (openMah == mahID) {
                count--;
            }
        }
        // :1092-1100 — 手牌（仅本方座位）。
        for (int handMah : ownSeat.handTiles()) {
            if (handMah == mahID) {
                count--;
            }
        }
        // :1102-1108 — 单放（Lua 判 dfMah~=0；本模型用 null 表示无单放）。
        if (ownSeat.drawnTile() != null && ownSeat.drawnTile() == mahID) {
            count--;
        }
        // :1110-1134 — 全部座位的副露。
        for (TaizhouRoundSeatState seat : state.seats()) {
            for (TaizhouRoundSeatState.Meld meld : seat.melds()) {
                List<Integer> mahs = meld.tiles();
                if (mahs.size() == 4) { // :1114
                    int kongMah = MahjongTile.BACK; // :1115
                    for (int mah : mahs) { // :1116-1121 — 首个非牌背值
                        if (mah != MahjongTile.BACK) {
                            kongMah = mah;
                            break;
                        }
                    }
                    if (kongMah != MahjongTile.BACK) { // :1122-1127 — 全副填成该值
                        List<Integer> filled = new ArrayList<>(4);
                        for (int index = 0; index < mahs.size(); index++) {
                            filled.add(kongMah);
                        }
                        mahs = filled;
                    }
                }
                for (int mah : mahs) { // :1128-1132
                    if (mah == mahID) {
                        count--;
                    }
                }
            }
        }
        // :1136-1158 — 出牌区。
        if (outMahsPutType == MahjongOutMahsPutType.FOUR_DIRECTION) { // :1138
            for (TaizhouRoundSeatState seat : state.seats()) { // :1139-1148
                for (int outMah : seat.outTiles()) {
                    if (outMah == mahID) {
                        count--;
                    }
                }
            }
        } else {
            for (int outMah : state.sharedOutTiles()) { // :1149-1158
                if (outMah == mahID) {
                    count--;
                }
            }
        }
        if (count < 0) { // :1160-1162 — 负数归零
            count = 0;
        }
        return count; // :1163
    }
}
