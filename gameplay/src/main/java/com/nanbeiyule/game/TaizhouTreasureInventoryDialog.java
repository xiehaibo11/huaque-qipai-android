package com.nanbeiyule.game;

import android.content.Context;

/** Full-screen host for the original four-column treasure inventory. */
final class TaizhouTreasureInventoryDialog extends TaizhouFullscreenDialog {
    TaizhouTreasureInventoryDialog(
            Context context,
            String userId,
            FortuneState state,
            TaizhouTreasurePlacementStore store) {
        this(context, new TaizhouTreasureInventoryView(context, userId, state, store));
    }

    private TaizhouTreasureInventoryDialog(
            Context context, TaizhouTreasureInventoryView view) {
        super(context, view, true);
        view.setCloseAction(this::dismiss);
    }
}
