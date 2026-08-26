package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.MahjongMeldAlgorithm;
import com.nanbeiyule.game.mahjong.MahjongTile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hit testing and click dispatch for the Taizhou mahjong action bar, ported
 * from {@code BasicMahjong/Modules/GameLayer/View2D/UIMahLayerAction.luac}
 * (showAction/showActionCombs/onTouchEventActionComb),
 * {@code ViewBase/UIMahLayerBase.luac} (onTouchEventActionButton,
 * isSendCancelServer) and {@code BasicMahjong/Modules/GameLayer/Module.luac:1317-1487}
 * (doActionChow/doActionPong/doActionKong).
 *
 * <p>Taizhou rule constants: {@code canChowPungKongJoker() == false} and
 * {@code isGuoGangBuGang() == false} ({@code BasicMahjong/Data/ConfigData.luac:28,37};
 * TaiZhou has no override), so jokers are stripped before candidate search and
 * the hand is kept when collecting fill kongs. The original {@code curPower}
 * gate on concealed/fill kongs never filters anything in this build because
 * {@code setCurPower} is only ever called with {@code POWER.NONE}
 * ({@code Module.luac:224,270}); this port keeps that behaviour.
 *
 * <p>The class performs no network calls; outcomes are intents plus the next
 * immutable state. The original one-second PASS-button debounce and the
 * slide animations are Wave 2 concerns.
 */
public final class TaizhouActionBarInteraction {
    /** Areas where PASS always notifies the server (UIMahLayerBase.luac:651). */
    private static final int[] SUPPORT_SEND_PASS_AREA = {7111, 7115, 7102};

    /**
     * Minimal hand snapshot needed to resolve a tapped action, mirroring the
     * original call sites: the concealed hand, the incoming discard
     * ({@code lastPlayMah}), the joker and its stand-in, the just-drawn tile
     * ({@code danFang}) and the player's exposed melds. Taizhou has a single
     * joker, matching {@link MahjongMeldAlgorithm}.
     */
    public record HandContext(
            int[] handTiles,
            int inTile,
            int joker,
            int instead,
            int drawnTile,
            int[][] exposedMelds) {
        public HandContext {
            handTiles = handTiles == null ? new int[0] : handTiles.clone();
        }
    }

    /** Result of one tap: an optional intent plus the next bar state. */
    public record TapOutcome(TaizhouActionIntent intent, TaizhouActionBarState nextState) {}

    /** Returns the visible action id at the point, or null. Cocos bottom-up Y. */
    public Integer hitBarAction(TaizhouActionBarState state, float designX, float cocosY) {
        if (state == null || !state.barVisible()) {
            return null;
        }
        List<Integer> actions = state.visibleActions();
        for (int slot = 1; slot <= actions.size(); slot++) {
            float centerX = TaizhouActionBarLayout.slotCenterX(slot);
            float half = TaizhouActionBarLayout.BUTTON_SIZE / 2.0f;
            if (designX >= centerX - half
                    && designX <= centerX + half
                    && cocosY >= TaizhouActionBarLayout.SLOT_CENTER_Y - half
                    && cocosY <= TaizhouActionBarLayout.SLOT_CENTER_Y + half) {
                return actions.get(slot - 1);
            }
        }
        return null;
    }

    /** Returns the 1-based comb candidate index at the point, or null. */
    public Integer hitCombCandidate(TaizhouActionBarState state, float designX, float cocosY) {
        if (state == null || state.combKind() == TaizhouActionBarState.CombKind.NONE) {
            return null;
        }
        List<int[]> candidates = state.combCandidates();
        float cellWidth = TaizhouActionBarLayout.combCellWidth();
        float cellHeight = TaizhouActionBarLayout.combCellHeight(candidates.get(0).length);
        float backLeft = TaizhouActionBarLayout.combsBackLeft(cellWidth, candidates.size());
        for (int index = 1; index <= candidates.size(); index++) {
            float left = TaizhouActionBarLayout.combCellLeft(index, cellWidth, backLeft);
            float bottom = TaizhouActionBarLayout.combCellBottom(index, cellHeight);
            if (designX >= left
                    && designX <= left + cellWidth
                    && cocosY >= bottom
                    && cocosY <= bottom + cellHeight) {
                return index;
            }
        }
        return null;
    }

    /** Whether the point is inside the candidate panel's cancel button. */
    public boolean hitCombCancel(TaizhouActionBarState state, float designX, float cocosY) {
        if (state == null || state.combKind() == TaizhouActionBarState.CombKind.NONE) {
            return false;
        }
        float half = TaizhouActionBarLayout.CANCEL_SIZE / 2.0f;
        float centerX = TaizhouActionBarLayout.CANCEL_RIGHT - half;
        return designX >= centerX - half
                && designX <= centerX + half
                && cocosY >= TaizhouActionBarLayout.CANCEL_CENTER_Y - half
                && cocosY <= TaizhouActionBarLayout.CANCEL_CENTER_Y + half;
    }

    /** Routes a design-space tap through the current mode. */
    public TapOutcome tap(
            TaizhouActionBarState state, float designX, float cocosY, HandContext context) {
        if (state.combKind() != TaizhouActionBarState.CombKind.NONE) {
            Integer candidate = hitCombCandidate(state, designX, cocosY);
            if (candidate != null) {
                return tapCombCandidate(state, candidate - 1);
            }
            if (hitCombCancel(state, designX, cocosY)) {
                return tapCombCancel(state);
            }
            return new TapOutcome(null, state);
        }
        Integer action = hitBarAction(state, designX, cocosY);
        if (action != null) {
            return tapActionButton(state, action, context);
        }
        return new TapOutcome(null, state);
    }

    /**
     * Ports {@code onTouchEventActionButton} plus the matching
     * {@code doAction*} branch. Every branch hides the bar, exactly like the
     * original trailing {@code showAction({}, false)}.
     */
    public TapOutcome tapActionButton(
            TaizhouActionBarState state, int actionId, HandContext context) {
        TaizhouActionBarState barHidden = state.withBarHidden();
        switch (actionId) {
            case TaizhouActionBarState.ACTION_PASS:
                return new TapOutcome(TaizhouActionIntent.pass(), barHidden);
            case TaizhouActionBarState.ACTION_CHOW:
                return chowOutcome(barHidden, context);
            case TaizhouActionBarState.ACTION_PONG:
                return pongOutcome(barHidden, context);
            case TaizhouActionBarState.ACTION_KONG:
                return kongOutcome(barHidden, context);
            case TaizhouActionBarState.ACTION_HU:
                return new TapOutcome(TaizhouActionIntent.hu(), barHidden);
            case TaizhouActionBarState.ACTION_TING:
                return new TapOutcome(TaizhouActionIntent.ting(), barHidden);
            default:
                // ACTION_FLOWER (button 6) has no handler branch in the original.
                return new TapOutcome(null, barHidden);
        }
    }

    /** Ports {@code onTouchEventActionComb}: resolves a candidate cell tap. */
    public TapOutcome tapCombCandidate(TaizhouActionBarState state, int candidateIndex) {
        if (state.combKind() == TaizhouActionBarState.CombKind.NONE) {
            throw new IllegalStateException("no comb candidate panel is shown");
        }
        int[] tiles = state.combCandidates().get(candidateIndex).clone();
        TaizhouActionBarState next = state.withoutCombs();
        if (tiles.length == 3) {
            return new TapOutcome(TaizhouActionIntent.chow(candidateIndex, tiles), next);
        }
        if (tiles[0] == MahjongTile.BACK) {
            // Concealed kong: the send form replaces the three backs.
            int value = tiles[tiles.length - 1];
            return new TapOutcome(
                    TaizhouActionIntent.kong(
                            candidateIndex,
                            TaizhouActionBarState.COMB_FLAG_CKONG,
                            new int[] {value, value, value, value}),
                    next);
        }
        return new TapOutcome(
                TaizhouActionIntent.kong(
                        candidateIndex, TaizhouActionBarState.COMB_FLAG_TKONG, tiles),
                next);
    }

    /** Ports {@code onTouchEventActionCancel}: close the panel, restore the bar. */
    public TapOutcome tapCombCancel(TaizhouActionBarState state) {
        return new TapOutcome(
                TaizhouActionIntent.cancel(), state.withoutCombs().withBarShown());
    }

    /** Ports {@code doActionChow}: direct send, or the reversed candidate list. */
    private TapOutcome chowOutcome(TaizhouActionBarState barHidden, HandContext context) {
        int[] tempHands = withoutJokers(context.handTiles(), context.joker());
        List<int[]> combs =
                MahjongMeldAlgorithm.findChow(
                        tempHands, context.inTile(), context.joker(), context.instead());
        if (combs.size() == 1) {
            return new TapOutcome(TaizhouActionIntent.chow(0, combs.get(0)), barHidden);
        }
        if (combs.size() > 1) {
            Collections.reverse(combs);
            return new TapOutcome(
                    null, barHidden.withCombs(combs, TaizhouActionBarState.CombKind.CHOW));
        }
        return new TapOutcome(null, barHidden);
    }

    /** Ports {@code doActionPong}: always a direct three-of-a-kind send. */
    private TapOutcome pongOutcome(TaizhouActionBarState barHidden, HandContext context) {
        int[] tempHands = withoutJokers(context.handTiles(), context.joker());
        int[] combs = MahjongMeldAlgorithm.findPong(tempHands, context.inTile());
        if (combs.length == 0) {
            return new TapOutcome(null, barHidden);
        }
        return new TapOutcome(TaizhouActionIntent.pong(combs), barHidden);
    }

    /** Ports {@code doActionKong}: exposed kong sends; otherwise concealed and
     * fill kongs are collected, sent directly when unique, or shown. */
    private TapOutcome kongOutcome(TaizhouActionBarState barHidden, HandContext context) {
        int[] tempHands = withoutJokers(context.handTiles(), context.joker());
        List<int[]> exposed = MahjongMeldAlgorithm.findExposedKong(tempHands, context.inTile());
        if (exposed.size() == 1) {
            return new TapOutcome(
                    TaizhouActionIntent.kong(
                            0, TaizhouActionBarState.COMB_FLAG_MKONG, exposed.get(0)),
                    barHidden);
        }
        List<int[]> combs = new ArrayList<>();
        combs.addAll(MahjongMeldAlgorithm.findConcealedKong(tempHands, context.drawnTile()));
        combs.addAll(
                MahjongMeldAlgorithm.findFillKong(
                        tempHands, context.drawnTile(), context.exposedMelds()));
        if (combs.size() == 1) {
            int[] display = combs.get(0);
            if (display[0] == MahjongTile.BACK) {
                int value = display[display.length - 1];
                return new TapOutcome(
                        TaizhouActionIntent.kong(
                                0,
                                TaizhouActionBarState.COMB_FLAG_CKONG,
                                new int[] {value, value, value, value}),
                        barHidden);
            }
            return new TapOutcome(
                    TaizhouActionIntent.kong(0, TaizhouActionBarState.COMB_FLAG_TKONG, display),
                    barHidden);
        }
        if (combs.size() > 1) {
            return new TapOutcome(
                    null, barHidden.withCombs(combs, TaizhouActionBarState.CombKind.KONG));
        }
        return new TapOutcome(null, barHidden);
    }

    /**
     * Ports {@code isSendCancelServer} (UIMahLayerBase.luac:651-683): whether a
     * PASS tap notifies the server. In the self-drawn hu scene (only pass+hu
     * buttons, outside areas 7111/7115/7102, concealed tile count % 3 == 2)
     * the cancel is swallowed and only an operate-pass is forwarded.
     */
    public static boolean isCancelSentToServer(
            List<Integer> actionTypes, int areaId, int concealedTileCount) {
        if (actionTypes.size() > 2) {
            return true;
        }
        boolean supportedArea = false;
        for (int area : SUPPORT_SEND_PASS_AREA) {
            if (area == areaId) {
                supportedArea = true;
                break;
            }
        }
        if (!actionTypes.contains(TaizhouActionBarState.ACTION_HU) || supportedArea) {
            return true;
        }
        return concealedTileCount % 3 != 2;
    }

    /** Removes every joker occurrence, per the original joker-strip loop. */
    private static int[] withoutJokers(int[] handTiles, int joker) {
        if (joker == MahjongMeldAlgorithm.NO_TILE) {
            return handTiles.clone();
        }
        int[] remaining = new int[handTiles.length];
        int count = 0;
        for (int tile : handTiles) {
            if (tile != joker) {
                remaining[count++] = tile;
            }
        }
        int[] result = new int[count];
        System.arraycopy(remaining, 0, result, 0, count);
        return result;
    }
}
