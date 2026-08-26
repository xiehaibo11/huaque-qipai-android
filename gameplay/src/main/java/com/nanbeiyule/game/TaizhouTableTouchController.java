package com.nanbeiyule.game;

import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayInteraction;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingProjection;
import com.nanbeiyule.game.mahjong.TaizhouPlayerInfoProjection;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;

/**
 * Touch orchestration of the Taizhou mahjong table surface, extracted from the
 * view so the view stays below the 500-line hard limit (AGENTS.md 第 25/28 章).
 *
 * <p>Priority order mirrors the original scene stack: the modal settle page
 * (ROUND_RESULT) first, then hold-to-talk voice chrome, the gold-room multiple
 * layer, the Wave 2-B action bar (which outranks the hand touch area), the
 * early-start button, the hand play gesture, then the waiting-chrome buttons.
 * The controller performs no drawing and no network work; user-visible outcomes
 * leave through {@link Listener}.
 */
final class TaizhouTableTouchController {
    interface Listener {
        void onPlayResult(TaizhouMahjongPlayGesture.Result result);

        void onVoiceResult(TaizhouMahjongVoiceGesture.Result result);

        void onWaitingAction(TaizhouMahjongWaitingProjection.Action action);

        /** 结算页三按钮之一被点击（查看牌桌/洗牌/下一局）。 */
        void onSettleAction(TaizhouSettleInteraction.Action action);

        /** 末局大结算的返回大厅/分享按钮。 */
        void onTotalResultAction(TaizhouTotalResultInteraction.Action action);

        /** 等待态提前开局按钮被点击。 */
        void onEarlyStartRequested();

        /** 加倍层 NONE/ADD/SUPER 之一被点击。 */
        void onMultipleChoice(TaizhouMultipleState.Choice choice);

        /** A completed tap that should register as a view click. */
        void onGestureClick();

        /** CanHuMahsUI consumed a background tap and closed its WINDOW-level layer. */
        void onCanHuDismissed();

        /** 点中某个座位的头像框，打开 PlayerInfoLayer.csb 玩家信息面板。 */
        void onPlayerHeadTapped(int seatNumber);
    }

    private final TaizhouMahjongPlayInteraction playInteraction;
    private final TaizhouMahjongVoiceGesture voiceGesture;
    private final TaizhouRoundOverlayController roundOverlays;
    private final TaizhouCanHuTracker canHuTracker;
    private final TaizhouSettleInteraction settleInteraction = new TaizhouSettleInteraction();
    private final TaizhouTotalResultInteraction totalResultInteraction =
            new TaizhouTotalResultInteraction();
    private final Listener listener;
    private boolean activePlayTouch;
    private boolean activeVoiceTouch;
    private boolean activeOverlayTouch;
    private boolean activeSettleTouch;
    private boolean activeTotalResultTouch;
    private boolean activeSettleReview;
    private boolean activeEarlyStartTouch;
    private boolean activeCanHuTouch;
    private TaizhouSettleInteraction.Action activeSettleAction;
    private TaizhouTotalResultInteraction.Action activeTotalResultAction;
    private TaizhouMultipleState.Choice activeMultipleChoice;
    private TaizhouMahjongWaitingProjection.Action activeWaitingAction =
            TaizhouMahjongWaitingProjection.Action.NONE;

    TaizhouTableTouchController(
            TaizhouMahjongPlayInteraction playInteraction,
            TaizhouMahjongVoiceGesture voiceGesture,
            TaizhouRoundOverlayController roundOverlays,
            TaizhouCanHuTracker canHuTracker,
            Listener listener) {
        this.playInteraction = playInteraction;
        this.voiceGesture = voiceGesture;
        this.roundOverlays = roundOverlays;
        this.canHuTracker = canHuTracker;
        this.listener = listener;
    }

    /** 结算页交互状态（查看牌桌回看态）的唯一持有者。 */
    TaizhouSettleInteraction settleInteraction() {
        return settleInteraction;
    }

    TaizhouTotalResultInteraction totalResultInteraction() {
        return totalResultInteraction;
    }

    /** Drops any in-progress gesture when a fresh projection arrives. */
    void reset(GameplayTableState state) {
        activePlayTouch = false;
        activeOverlayTouch = false;
        activeSettleTouch = false;
        activeTotalResultTouch = false;
        activeSettleReview = false;
        activeEarlyStartTouch = false;
        activeCanHuTouch = false;
        activeSettleAction = null;
        activeTotalResultAction = null;
        activeMultipleChoice = null;
        settleInteraction.reset();
        totalResultInteraction.onSnapshot(state);
    }

    boolean onDown(GameplayTableState tableState, float designX, float designY, float cocosY) {
        if (totalResultInteraction.showing(tableState)) {
            activeTotalResultTouch = true;
            activeTotalResultAction =
                    totalResultInteraction.actionAt(tableState, designX, designY);
            return true;
        }
        // 结算页是模态整页：命中记录按钮，未命中也吞掉（Wave 3 batch-2 放行三按钮）。
        if (settleInteraction.isBlocking(tableState)) {
            activeSettleTouch = true;
            activeSettleAction =
                    TaizhouSettleInteraction.actionAt(tableState, designX, designY);
            return true;
        }
        // 查看牌桌回看态：任意点击恢复结算层。
        if (TaizhouSettleInteraction.showing(tableState) && settleInteraction.reviewingTable()) {
            activeSettleReview = true;
            return true;
        }
        if (tableState.actionOffer().isPresent()
                && roundOverlays != null
                && roundOverlays.onTouchDown(designX, cocosY)) {
            activeOverlayTouch = true;
            activeWaitingAction = TaizhouMahjongWaitingProjection.Action.NONE;
            return true;
        }
        // CanHuMahsUI is a WINDOW-level layer. Its background closes the window, while
        // the selected ting tile must still receive the confirming second tap.
        if (canHuTracker.current().visible()) {
            TaizhouMahjongPlayGesture.Result playResult =
                    tableState.actionOffer().isEmpty()
                            ? playInteraction.onSelectedTileDown(designX, cocosY)
                            : null;
            if (playResult != null && playResult.handled) {
                activePlayTouch = true;
                listener.onPlayResult(playResult);
                canHuTracker.onHandGesture(playInteraction);
                return true;
            }
            activeCanHuTouch = true;
            canHuTracker.onBackgroundClicked();
            listener.onCanHuDismissed();
            return true;
        }
        int headSeat = TaizhouPlayerInfoProjection.seatAt(tableState, designX, designY);
        if (headSeat > 0) {
            activeWaitingAction = TaizhouMahjongWaitingProjection.Action.NONE;
            listener.onPlayerHeadTapped(headSeat);
            return true;
        }
        TaizhouMahjongWaitingProjection.Action chromeAction =
                TaizhouMahjongWaitingProjection.actionAt(
                        tableState, designX, designY, canHuTracker.tingButtonVisible());
        if (chromeAction == TaizhouMahjongWaitingProjection.Action.VOICE) {
            activeVoiceTouch = true;
            activeWaitingAction = TaizhouMahjongWaitingProjection.Action.NONE;
            listener.onVoiceResult(voiceGesture.onDown(designX, designY, true));
            return true;
        }
        // 加倍层可见时优先于动作条、手牌与等待按钮（Wave 3 batch-2）。
        TaizhouMultipleState.Choice choice =
                TaizhouMultipleInteraction.choiceAt(tableState, designX, designY);
        if (choice != null) {
            activeMultipleChoice = choice;
            activeWaitingAction = TaizhouMahjongWaitingProjection.Action.NONE;
            return true;
        }
        // The action bar outranks the hand touch area (Wave 2-B).
        if (roundOverlays != null && roundOverlays.onTouchDown(designX, cocosY)) {
            activeOverlayTouch = true;
            activeWaitingAction = TaizhouMahjongWaitingProjection.Action.NONE;
            return true;
        }
        // 提前开局按钮在等待桌中部，不与手牌区重叠（TableInfo.csb）。
        if (TaizhouEarlyStartProjection.hitButton(tableState, designX, designY)) {
            activeEarlyStartTouch = true;
            activeWaitingAction = TaizhouMahjongWaitingProjection.Action.NONE;
            return true;
        }
        TaizhouMahjongPlayGesture.Result playResult =
                playInteraction.onDown(designX, cocosY, false);
        if (playResult != null && playResult.handled) {
            activePlayTouch = true;
            activeWaitingAction = TaizhouMahjongWaitingProjection.Action.NONE;
            listener.onPlayResult(playResult);
            // 透传手牌选中态给听牌提示 Tracker（Wave 3 CanHuMahs）。
            canHuTracker.onHandGesture(playInteraction);
            return true;
        }
        activeWaitingAction = chromeAction;
        return activeWaitingAction != TaizhouMahjongWaitingProjection.Action.NONE;
    }

    boolean onMove(float designX, float designY, float cocosY) {
        if (activeTotalResultTouch
                || activeSettleTouch
                || activeSettleReview
                || activeCanHuTouch) {
            return true;
        }
        if (activeOverlayTouch) {
            return true;
        }
        if (activeVoiceTouch) {
            listener.onVoiceResult(voiceGesture.onMove(designX, designY));
            return true;
        }
        if (activeMultipleChoice != null || activeEarlyStartTouch) {
            return true;
        }
        if (activePlayTouch) {
            listener.onPlayResult(playInteraction.onMove(designX, cocosY));
            canHuTracker.onHandGesture(playInteraction);
            return true;
        }
        return activeWaitingAction != TaizhouMahjongWaitingProjection.Action.NONE;
    }

    boolean onUp(GameplayTableState tableState, float designX, float designY, float cocosY) {
        if (activeTotalResultTouch) {
            activeTotalResultTouch = false;
            TaizhouTotalResultInteraction.Action action = activeTotalResultAction;
            activeTotalResultAction = null;
            listener.onGestureClick();
            if (action != null
                    && action == totalResultInteraction.actionAt(
                            tableState, designX, designY)) {
                listener.onTotalResultAction(action);
            }
            return true;
        }
        if (activeSettleTouch) {
            activeSettleTouch = false;
            TaizhouSettleInteraction.Action action = activeSettleAction;
            activeSettleAction = null;
            listener.onGestureClick();
            if (action != null
                    && action
                            == TaizhouSettleInteraction.actionAt(tableState, designX, designY)) {
                listener.onSettleAction(action);
            }
            return true;
        }
        if (activeSettleReview) {
            activeSettleReview = false;
            settleInteraction.exitReview();
            listener.onGestureClick();
            return true;
        }
        if (activeCanHuTouch) {
            activeCanHuTouch = false;
            listener.onGestureClick();
            return true;
        }
        if (activeOverlayTouch) {
            activeOverlayTouch = false;
            roundOverlays.onTouchUp(designX, cocosY);
            listener.onGestureClick();
            return true;
        }
        if (activeVoiceTouch) {
            activeVoiceTouch = false;
            listener.onVoiceResult(voiceGesture.onUp(designX, designY));
            listener.onGestureClick();
            return true;
        }
        if (activeMultipleChoice != null) {
            TaizhouMultipleState.Choice armed = activeMultipleChoice;
            activeMultipleChoice = null;
            listener.onGestureClick();
            if (armed == TaizhouMultipleInteraction.choiceAt(tableState, designX, designY)) {
                listener.onMultipleChoice(armed);
            }
            return true;
        }
        if (activeEarlyStartTouch) {
            activeEarlyStartTouch = false;
            listener.onGestureClick();
            if (TaizhouEarlyStartProjection.hitButton(tableState, designX, designY)) {
                listener.onEarlyStartRequested();
            }
            return true;
        }
        if (activePlayTouch) {
            activePlayTouch = false;
            listener.onPlayResult(playInteraction.onEnd(designX, cocosY));
            canHuTracker.onHandGesture(playInteraction);
            listener.onGestureClick();
            return true;
        }
        TaizhouMahjongWaitingProjection.Action action =
                TaizhouMahjongWaitingProjection.actionAt(
                        tableState, designX, designY, canHuTracker.tingButtonVisible());
        boolean shouldRequest =
                activeWaitingAction != TaizhouMahjongWaitingProjection.Action.NONE
                        && activeWaitingAction == action;
        TaizhouMahjongWaitingProjection.Action requestedAction = activeWaitingAction;
        activeWaitingAction = TaizhouMahjongWaitingProjection.Action.NONE;
        if (shouldRequest) {
            listener.onGestureClick();
            listener.onWaitingAction(requestedAction);
            return true;
        }
        return false;
    }

    boolean onCancel(float designX, float designY, float cocosY) {
        if (activeTotalResultTouch) {
            activeTotalResultTouch = false;
            activeTotalResultAction = null;
            return true;
        }
        if (activeSettleTouch) {
            activeSettleTouch = false;
            activeSettleAction = null;
            return true;
        }
        if (activeSettleReview) {
            activeSettleReview = false;
            return true;
        }
        if (activeCanHuTouch) {
            activeCanHuTouch = false;
            return true;
        }
        if (activeOverlayTouch) {
            activeOverlayTouch = false;
            roundOverlays.onTouchCancel();
            return true;
        }
        if (activeVoiceTouch) {
            activeVoiceTouch = false;
            listener.onVoiceResult(voiceGesture.onCancel());
            return true;
        }
        if (activeMultipleChoice != null) {
            activeMultipleChoice = null;
            return true;
        }
        if (activeEarlyStartTouch) {
            activeEarlyStartTouch = false;
            return true;
        }
        if (activePlayTouch) {
            activePlayTouch = false;
            listener.onPlayResult(playInteraction.onCancel(designX, cocosY));
            canHuTracker.onHandGesture(playInteraction);
            return true;
        }
        activeWaitingAction = TaizhouMahjongWaitingProjection.Action.NONE;
        return false;
    }

    boolean onOther() {
        return activeCanHuTouch
                || activeWaitingAction != TaizhouMahjongWaitingProjection.Action.NONE;
    }
}
