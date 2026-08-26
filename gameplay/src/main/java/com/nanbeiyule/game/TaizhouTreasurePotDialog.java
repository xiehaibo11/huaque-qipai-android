package com.nanbeiyule.game;

import android.content.Context;

/** Original JuBaoPenMainView.csb full-screen treasure pot. */
final class TaizhouTreasurePotDialog extends TaizhouFullscreenDialog {
    private final TaizhouTreasurePotView view;

    TaizhouTreasurePotDialog(
            Context context, FortuneState state, TaizhouTreasurePotView.Actions actions) {
        this(context, new TaizhouTreasurePotView(context, state, actions));
    }

    private TaizhouTreasurePotDialog(Context context, TaizhouTreasurePotView view) {
        super(context, view, false);
        this.view = view;
    }

    TaizhouTreasurePotView view() {
        return view;
    }

    void onDrawResult(FortuneTreasureDrawResult result) {
        view.onDrawResult(result);
    }

    void onDrawError() {
        view.onDrawError();
    }

    void replaceState(FortuneState state) {
        view.replaceState(state);
    }

    void resumeAfterResult() {
        view.resumeAfterResult();
    }

    boolean startRepeatDraw(int count) {
        return view.startRepeatDraw(count);
    }
}
