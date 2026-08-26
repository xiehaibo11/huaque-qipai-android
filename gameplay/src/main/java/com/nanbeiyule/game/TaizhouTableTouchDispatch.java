package com.nanbeiyule.game;

import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongWaitingProjection;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;

/** 把触摸控制器回调转交给牌桌 View，避免渲染类再次承担一整套匿名监听器。 */
final class TaizhouTableTouchDispatch implements TaizhouTableTouchController.Listener {
    private final TaizhouMahjongTableView view;

    TaizhouTableTouchDispatch(TaizhouMahjongTableView view) {
        this.view = view;
    }

    @Override
    public void onPlayResult(TaizhouMahjongPlayGesture.Result result) {
        view.applyPlayResult(result);
    }

    @Override
    public void onVoiceResult(TaizhouMahjongVoiceGesture.Result result) {
        view.dispatchVoiceGesture(result);
    }

    @Override
    public void onWaitingAction(TaizhouMahjongWaitingProjection.Action action) {
        view.dispatchWaitingAction(action);
    }

    @Override
    public void onSettleAction(TaizhouSettleInteraction.Action action) {
        view.dispatchSettleAction(action);
    }

    @Override
    public void onTotalResultAction(TaizhouTotalResultInteraction.Action action) {
        view.dispatchTotalResultAction(action);
    }

    @Override
    public void onEarlyStartRequested() {
        view.dispatchEarlyStart();
    }

    @Override
    public void onMultipleChoice(TaizhouMultipleState.Choice choice) {
        view.dispatchMultipleChoice(choice);
    }

    @Override
    public void onGestureClick() {
        view.performClick();
    }

    @Override
    public void onPlayerHeadTapped(int seatNumber) {
        view.dispatchPlayerHeadTapped(seatNumber);
    }

    @Override
    public void onCanHuDismissed() {
        view.invalidate();
    }
}
