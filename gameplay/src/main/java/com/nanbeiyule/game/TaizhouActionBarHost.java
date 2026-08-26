package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayActionOffer;
import com.nanbeiyule.game.gameplay.GameplayKongType;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.MahjongTile;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Table-side owner of the action bar (吃碰杠胡动作条) state and touch routing.
 *
 * <p>Visibility is driven exclusively by the server {@code ACTION_OFFERED}
 * projection ({@link GameplayTableState#actionOffer()}), matching the original
 * server {@code nPower} dispatch; the client never infers an action from the
 * local hand. Chow/kong candidate data comes from the same server offer
 * ({@code chowCandidates}/{@code kongOptions}); the CHOW command's {@code
 * candidateIndex} indexes the server list verbatim, so the panel displays
 * candidates in server order.
 *
 * <p>Every dispatched action hides the bar and consumes the offer, exactly
 * like the original trailing {@code showAction({}, false)}: while the same
 * same offer identity is still projected (until ACTION_EXPIRED or the meld
 * lands) the bar stays hidden. The backend restarts offerId numbering every
 * round, so identity includes both offerId and actionToken rather than offerId
 * alone.
 *
 * <p>PASS follows the original {@code isSendCancelServer} swallow rule
 * ({@code UIMahLayerBase.luac:651-683}, already ported as
 * {@link TaizhouActionBarInteraction#isCancelSentToServer}): with only
 * PASS+HU on the bar and a self-drawn hand the tap is purely local.
 */
final class TaizhouActionBarHost {
    /** Taizhou mahjong game id, the {@code areaId} argument of the ported rule. */
    private static final int TAIZHOU_GAME_ID = 30109;

    interface Listener {
        void onChowRequested(int tileValue, int candidateIndex, String actionToken);

        void onPungRequested(int tileValue, String actionToken);

        void onKongRequested(int tileValue, GameplayKongType kongType, String actionToken);

        void onHuRequested(String actionToken);

        void onPassRequested(String actionToken);
    }

    private final TaizhouActionBarInteraction interaction = new TaizhouActionBarInteraction();
    private record OfferIdentity(int offerId, String actionToken) {}

    private final Set<OfferIdentity> consumedOffers = new HashSet<>();
    private Listener listener;
    private TaizhouActionBarState barState = TaizhouActionBarState.hidden(0);
    private GameplayActionOffer currentOffer;
    private int concealedTileCount;
    private boolean touchCaptured;

    void setListener(Listener nextListener) {
        listener = nextListener;
    }

    /** Returns the current bar state for the renderer and for tests. */
    TaizhouActionBarState barState() {
        return barState;
    }

    void update(GameplayTableState state) {
        GameplayActionOffer offer = state == null ? null : state.actionOffer().orElse(null);
        concealedTileCount = concealedTileCount(state);
        if (offer == null) {
            currentOffer = null;
            barState = TaizhouActionBarState.hidden(0);
            return;
        }
        if (consumedOffers.contains(identityOf(offer))) {
            currentOffer = offer;
            barState = TaizhouActionBarState.hidden(offer.offerId());
            return;
        }
        if (currentOffer == null
                || !identityOf(currentOffer).equals(identityOf(offer))) {
            currentOffer = offer;
            barState = TaizhouActionBarState.fromPower(offer.powerMask(), offer.offerId());
        }
    }

    /**
     * Captures the touch when it lands on the bar or the comb panel; the hand
     * interaction only runs when this returns false.
     */
    boolean onTouchDown(float designX, float cocosY) {
        touchCaptured =
                barState.combKind() != TaizhouActionBarState.CombKind.NONE
                        ? interaction.hitCombCandidate(barState, designX, cocosY) != null
                                || interaction.hitCombCancel(barState, designX, cocosY)
                        : interaction.hitBarAction(barState, designX, cocosY) != null;
        return touchCaptured;
    }

    /** Dispatches a captured tap; a slide off the target cancels it. */
    boolean onTouchUp(float designX, float cocosY) {
        if (!touchCaptured) {
            return false;
        }
        touchCaptured = false;
        if (barState.combKind() != TaizhouActionBarState.CombKind.NONE) {
            Integer candidate = interaction.hitCombCandidate(barState, designX, cocosY);
            if (candidate != null) {
                dispatchCombCandidate(candidate - 1);
            } else if (interaction.hitCombCancel(barState, designX, cocosY)) {
                barState = barState.withoutCombs().withBarShown();
            }
            return true;
        }
        Integer action = interaction.hitBarAction(barState, designX, cocosY);
        if (action != null) {
            dispatchAction(action);
        }
        return true;
    }

    void onTouchCancel() {
        touchCaptured = false;
    }

    private void dispatchAction(int actionId) {
        GameplayActionOffer offer = currentOffer;
        if (offer == null) {
            return;
        }
        switch (actionId) {
            case TaizhouActionBarState.ACTION_PASS -> {
                List<Integer> visibleActions = barState.visibleActions();
                consume(offer);
                if (TaizhouActionBarInteraction.isCancelSentToServer(
                        visibleActions, TAIZHOU_GAME_ID, concealedTileCount)) {
                    notifyPass(offer.actionToken());
                }
            }
            case TaizhouActionBarState.ACTION_CHOW -> {
                List<int[]> candidates = offer.chowCandidates();
                if (candidates.size() == 1) {
                    consume(offer);
                    if (listener != null) {
                        listener.onChowRequested(offer.contextTile(), 0, offer.actionToken());
                    }
                } else if (candidates.size() > 1) {
                    barState = barState.withBarHidden().withCombs(
                            chowDisplayCombs(offer), TaizhouActionBarState.CombKind.CHOW);
                }
            }
            case TaizhouActionBarState.ACTION_PONG -> {
                consume(offer);
                if (listener != null) {
                    listener.onPungRequested(offer.contextTile(), offer.actionToken());
                }
            }
            case TaizhouActionBarState.ACTION_KONG -> {
                List<GameplayActionOffer.KongOption> options = offer.kongOptions();
                if (options.size() == 1) {
                    consume(offer);
                    GameplayActionOffer.KongOption option = options.get(0);
                    if (listener != null) {
                        listener.onKongRequested(
                                option.tileValue(), option.kongType(), offer.actionToken());
                    }
                } else if (options.size() > 1) {
                    barState = barState.withBarHidden().withCombs(
                            kongDisplayCombs(options), TaizhouActionBarState.CombKind.KONG);
                }
            }
            case TaizhouActionBarState.ACTION_HU -> {
                consume(offer);
                if (listener != null) {
                    listener.onHuRequested(offer.actionToken());
                }
            }
            default -> {
                // FLOWER/TING have no handler branch in the original dispatch.
            }
        }
    }

    private void dispatchCombCandidate(int candidateIndex) {
        GameplayActionOffer offer = currentOffer;
        if (offer == null) {
            return;
        }
        TaizhouActionBarState.CombKind kind = barState.combKind();
        consume(offer);
        if (listener == null) {
            return;
        }
        if (kind == TaizhouActionBarState.CombKind.CHOW) {
            listener.onChowRequested(offer.contextTile(), candidateIndex, offer.actionToken());
        } else if (kind == TaizhouActionBarState.CombKind.KONG) {
            GameplayActionOffer.KongOption option = offer.kongOptions().get(candidateIndex);
            listener.onKongRequested(option.tileValue(), option.kongType(), offer.actionToken());
        }
    }

    /**
     * One display comb per server candidate: the server list already carries the
     * full three-tile comb in run position (the original {@code findChow} shape);
     * sorted ascending so every candidate cell reads as a run.
     */
    private static List<int[]> chowDisplayCombs(GameplayActionOffer offer) {
        List<int[]> combs = new ArrayList<>(offer.chowCandidates().size());
        for (int[] candidate : offer.chowCandidates()) {
            int[] comb = candidate.clone();
            Arrays.sort(comb);
            combs.add(comb);
        }
        return combs;
    }

    /** Concealed kongs keep the original {BACK,BACK,BACK,value} display form. */
    private static List<int[]> kongDisplayCombs(List<GameplayActionOffer.KongOption> options) {
        List<int[]> combs = new ArrayList<>(options.size());
        for (GameplayActionOffer.KongOption option : options) {
            int value = option.tileValue();
            combs.add(
                    option.kongType() == GameplayKongType.CONCEALED
                            ? new int[] {MahjongTile.BACK, MahjongTile.BACK, MahjongTile.BACK, value}
                            : new int[] {value, value, value, value});
        }
        return combs;
    }

    private void consume(GameplayActionOffer offer) {
        consumedOffers.add(identityOf(offer));
        barState = barState.withBarHidden().withoutCombs();
    }

    private static OfferIdentity identityOf(GameplayActionOffer offer) {
        return new OfferIdentity(offer.offerId(), offer.actionToken());
    }

    private static int concealedTileCount(GameplayTableState state) {
        if (state == null || state.visibleRound().isEmpty()) {
            return 0;
        }
        TaizhouMahjongVisibleRound round = state.visibleRound().get();
        TaizhouMahjongVisibleRound.SeatHand hand = round.handAt(round.mySeat());
        return hand.concealedTiles().size() + (hand.drawnTile() == null ? 0 : 1);
    }

    private void notifyPass(String actionToken) {
        if (listener != null) {
            listener.onPassRequested(actionToken);
        }
    }
}
