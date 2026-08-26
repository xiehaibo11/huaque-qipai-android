package com.nanbeiyule.game;

import java.util.Arrays;

/**
 * One user decision on the Taizhou mahjong action bar, ported from the original
 * {@code onTouchEventActionButton}/{@code onTouchEventActionComb} dispatch in
 * {@code BasicMahjong/Modules/GameLayer/View2D/UIMahLayerAction.luac} and
 * {@code ViewBase/UIMahLayerBase.luac}.
 *
 * <p>The record only carries what the tap decided; the Wave 2 table integration
 * turns it into the matching server message (sendAction/sendCancel/sendHu/
 * sendTing). {@link Action#CANCEL} is the comb-panel cancel button, which the
 * original handles purely client-side (no server traffic).
 *
 * @param action the tapped action
 * @param candidateIndex index into the state's comb candidate list, or -1 when
 *     no candidate list was involved (PASS/HU/TING/CANCEL, direct PONG)
 * @param combFlag {@code GameDefine.COMB_FLAG} of the chosen meld
 *     (0 when not applicable)
 * @param tiles the meld tiles in server send form; concealed kongs are already
 *     resolved from their {BACK,BACK,BACK,value} display form to four values
 */
public record TaizhouActionIntent(Action action, int candidateIndex, int combFlag, int[] tiles) {
    /** Actions with a button or candidate-cell on the original action bar. */
    public enum Action {
        /** {@code _KW_ACTION_BTN_1} 过: hides the bar and cancels the power. */
        PASS,
        /** {@code _KW_ACTION_BTN_2} 吃. */
        CHOW,
        /** {@code _KW_ACTION_BTN_3} 碰. */
        PONG,
        /** {@code _KW_ACTION_BTN_4} 杠. */
        KONG,
        /** {@code _KW_ACTION_BTN_5} 胡. */
        HU,
        /** {@code _KW_ACTION_BTN_7} 听. */
        TING,
        /** {@code _KW_ACTION_CANCEL_BTN} on the candidate panel; client-side only. */
        CANCEL
    }

    public TaizhouActionIntent {
        if (action == null) {
            throw new IllegalArgumentException("action is required");
        }
        tiles = tiles == null ? new int[0] : Arrays.copyOf(tiles, tiles.length);
    }

    public static TaizhouActionIntent pass() {
        return simple(Action.PASS);
    }

    public static TaizhouActionIntent hu() {
        return simple(Action.HU);
    }

    public static TaizhouActionIntent ting() {
        return simple(Action.TING);
    }

    public static TaizhouActionIntent cancel() {
        return simple(Action.CANCEL);
    }

    public static TaizhouActionIntent chow(int candidateIndex, int[] tiles) {
        return new TaizhouActionIntent(
                Action.CHOW, candidateIndex, TaizhouActionBarState.COMB_FLAG_CHOW, tiles);
    }

    public static TaizhouActionIntent pong(int[] tiles) {
        return new TaizhouActionIntent(Action.PONG, -1, TaizhouActionBarState.COMB_FLAG_PUNG, tiles);
    }

    public static TaizhouActionIntent kong(int candidateIndex, int combFlag, int[] tiles) {
        return new TaizhouActionIntent(Action.KONG, candidateIndex, combFlag, tiles);
    }

    private static TaizhouActionIntent simple(Action action) {
        return new TaizhouActionIntent(action, -1, 0, new int[0]);
    }

    @Override
    public int[] tiles() {
        return Arrays.copyOf(tiles, tiles.length);
    }
}
