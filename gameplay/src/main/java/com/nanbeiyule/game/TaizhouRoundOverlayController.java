package com.nanbeiyule.game;

import android.graphics.Canvas;
import com.nanbeiyule.game.gameplay.GameplayTableState;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.Objects;

/**
 * Assembly of the Wave 2-B round-phase overlays: the meld areas, the flower
 * rows, the action bar and the table-centre action tip. The table view only
 * plugs this controller in; per-component duties stay in their own classes.
 *
 * <p>Draw order follows the original scene stack: the centre joker indicator,
 * melds and flowers above the
 * hands, the action bar above the tiles, the transient tip on top of the table
 * centre, all below the room-message bubble. Touch is routed to the action
 * bar first, exactly like the original action layer's priority over the hand
 * touch area.
 */
final class TaizhouRoundOverlayController {
    private final TaizhouJokerAreaRenderer jokerAreaRenderer;
    private final TaizhouMeldRenderer meldRenderer;
    private final TaizhouFlowerAreaRenderer flowerRenderer;
    private final TaizhouDiceRenderer diceRenderer;
    private final TaizhouActionTipOverlay tipOverlay;
    private final TaizhouActionTipTracker tipTracker = new TaizhouActionTipTracker();
    private final TaizhouMahjongActionEffectTracker actionEffectTracker =
            new TaizhouMahjongActionEffectTracker();
    private final TaizhouActionBarHost actionBarHost;
    private final TaizhouActionBarRenderer actionBarRenderer;
    private final TaizhouMahjongActionEffectRenderer actionEffectRenderer;

    TaizhouRoundOverlayController(
            TaizhouJokerAreaRenderer jokerAreaRenderer,
            TaizhouMeldRenderer meldRenderer,
            TaizhouFlowerAreaRenderer flowerRenderer,
            TaizhouDiceRenderer diceRenderer,
            TaizhouActionTipOverlay tipOverlay,
            TaizhouActionBarHost actionBarHost,
            TaizhouActionBarRenderer actionBarRenderer,
            TaizhouMahjongActionEffectRenderer actionEffectRenderer) {
        this.jokerAreaRenderer = Objects.requireNonNull(jokerAreaRenderer, "jokerAreaRenderer");
        this.meldRenderer = Objects.requireNonNull(meldRenderer, "meldRenderer");
        this.flowerRenderer = Objects.requireNonNull(flowerRenderer, "flowerRenderer");
        this.diceRenderer = Objects.requireNonNull(diceRenderer, "diceRenderer");
        this.tipOverlay = Objects.requireNonNull(tipOverlay, "tipOverlay");
        this.actionBarHost = Objects.requireNonNull(actionBarHost, "actionBarHost");
        this.actionBarRenderer = Objects.requireNonNull(actionBarRenderer, "actionBarRenderer");
        this.actionEffectRenderer =
                Objects.requireNonNull(actionEffectRenderer, "actionEffectRenderer");
    }

    void setActionListener(TaizhouActionBarHost.Listener listener) {
        actionBarHost.setListener(listener);
    }

    /** Feeds the newest projection into the bar and the tip tracker. */
    void update(GameplayTableState state, long nowElapsed) {
        actionBarHost.update(state);
        tipTracker.update(state == null ? null : state.actionTip().orElse(null), nowElapsed);
        actionEffectTracker.update(state, nowElapsed);
    }

    void draw(
            Canvas canvas,
            GameplayTableState state,
            TaizhouMahjongVisibleRound visibleRound,
            long nowElapsed) {
        // 财神指示牌与副露、补花同属牌面层：都是本局持续存在的牌，画在手牌之上、
        // 提示与操作条之下（原版把 KW_JOKER_MAH 放在中心容器 _KW_ADAPT_MAH_5 里）。
        jokerAreaRenderer.draw(canvas, visibleRound);
        meldRenderer.draw(canvas, state, visibleRound);
        flowerRenderer.draw(canvas, state);
        diceRenderer.draw(canvas, state, nowElapsed);
        if (state != null) {
            actionEffectRenderer.draw(
                    canvas,
                    actionEffectTracker.running(nowElapsed),
                    state.mySeat(),
                    state.chairCount(),
                    nowElapsed);
        }
        actionBarRenderer.draw(canvas, actionBarHost.barState());
        tipTracker
                .visibleKind(nowElapsed)
                .ifPresent(kind -> tipOverlay.draw(canvas, kind));
    }

    /** Milliseconds until the running tip hides; 0 when no repaint is needed. */
    long nextRepaintDelayMillis(GameplayTableState state, long nowElapsed) {
        return Math.max(tipTracker.remainingMillis(nowElapsed),
                Math.max(
                        diceRenderer.nextRepaintDelayMillis(state, nowElapsed),
                        actionEffectTracker.nextRepaintDelayMillis(nowElapsed)));
    }

    void release() {
        diceRenderer.release();
        actionEffectRenderer.release();
    }

    /** Returns true when the action bar captured the touch (bar has priority). */
    boolean onTouchDown(float designX, float cocosY) {
        return actionBarHost.onTouchDown(designX, cocosY);
    }

    boolean onTouchUp(float designX, float cocosY) {
        return actionBarHost.onTouchUp(designX, cocosY);
    }

    void onTouchCancel() {
        actionBarHost.onTouchCancel();
    }
}
