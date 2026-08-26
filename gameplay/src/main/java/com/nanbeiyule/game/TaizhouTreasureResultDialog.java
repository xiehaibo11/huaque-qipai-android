package com.nanbeiyule.game;

import android.content.Context;

/** Full-screen original one/five treasure draw result host. */
final class TaizhouTreasureResultDialog extends TaizhouFullscreenDialog {
    private final TaizhouTreasureResultView view;

    TaizhouTreasureResultDialog(
            Context context,
            FortuneState beforeDraw,
            FortuneTreasureDrawResult result,
            TaizhouTreasureResultView.Actions actions) {
        this(context, new TaizhouTreasureResultView(context, beforeDraw, result, actions));
    }

    private TaizhouTreasureResultDialog(
            Context context, TaizhouTreasureResultView view) {
        super(context, view, false);
        this.view = view;
    }

    void setRepeatEnabled(boolean enabled) {
        view.setRepeatEnabled(enabled);
    }
}
