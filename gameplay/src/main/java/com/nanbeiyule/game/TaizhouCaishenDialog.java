package com.nanbeiyule.game;

import android.content.Context;

/** Original CaiYunPropLayer.csb Caishen activation screen. */
final class TaizhouCaishenDialog extends TaizhouFullscreenDialog {
    TaizhouCaishenDialog(
            Context context, FortuneState state, TaizhouFortuneToolView.Actions actions) {
        this(context, new TaizhouFortuneToolView(
                context, TaizhouFortuneToolView.Mode.CAISHEN, state, actions));
    }

    private TaizhouCaishenDialog(Context context, TaizhouFortuneToolView view) {
        super(context, view, true);
        view.setDismissAction(this::dismiss);
    }
}
