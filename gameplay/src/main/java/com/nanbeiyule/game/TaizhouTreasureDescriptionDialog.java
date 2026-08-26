package com.nanbeiyule.game;

import android.content.Context;

/** Full-screen host for the original treasure-pot activity explanation. */
final class TaizhouTreasureDescriptionDialog extends TaizhouFullscreenDialog {
    TaizhouTreasureDescriptionDialog(Context context) {
        this(context, new TaizhouTreasureDescriptionView(context));
    }

    private TaizhouTreasureDescriptionDialog(
            Context context, TaizhouTreasureDescriptionView view) {
        super(context, view, true);
        view.setCloseAction(this::dismiss);
    }
}
