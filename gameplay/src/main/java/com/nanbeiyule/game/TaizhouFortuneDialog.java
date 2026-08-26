package com.nanbeiyule.game;

import android.content.Context;

/** Original CaiYunNewLayer.csb prayer screen. */
final class TaizhouFortuneDialog extends TaizhouFullscreenDialog {
    TaizhouFortuneDialog(
            Context context, FortuneState state, TaizhouFortuneToolView.Actions actions) {
        this(context, new TaizhouFortuneToolView(
                context, TaizhouFortuneToolView.Mode.FORTUNE, state, actions));
    }

    private TaizhouFortuneDialog(Context context, TaizhouFortuneToolView view) {
        super(context, view, false);
        view.setDismissAction(this::dismiss);
    }
}
