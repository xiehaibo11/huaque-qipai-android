package com.nanbeiyule.game;

import android.content.Context;

/** Original ChatLayer.csb right-side chat panel. */
final class TaizhouChatDialog extends TaizhouFullscreenDialog {
    interface Actions extends TaizhouChatView.Actions {}

    TaizhouChatDialog(
            Context context,
            TaizhouRoomToolsState state,
            int mySeat,
            Actions actions) {
        this(context, new TaizhouChatView(context, state, mySeat, actions));
    }

    private TaizhouChatDialog(Context context, TaizhouChatView view) {
        super(context, view, false);
        view.setDismissAction(this::dismiss);
    }
}
