package com.nanbeiyule.game;

import android.content.Context;

/** Original TipLayerCommon confirmation for next-round change-card/shuffle reservations. */
final class TaizhouPropReservationDialog extends TaizhouFullscreenDialog {
    interface Actions {
        void onReservationChanged(boolean active);
    }

    TaizhouPropReservationDialog(
            Context context,
            TaizhouRoomToolsState.Tool tool,
            boolean reserved,
            Actions actions) {
        this(context, new TaizhouPropReservationView(context, tool, reserved, actions));
    }

    private TaizhouPropReservationDialog(
            Context context, TaizhouPropReservationView view) {
        super(context, view, true);
        view.setDismissAction(this::dismiss);
    }
}
