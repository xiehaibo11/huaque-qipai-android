package com.nanbeiyule.game;

import android.content.Context;

/** {@code TaiZhou/EarlyStart.csb} 确认弹层的全屏宿主，与原版 DIALOG 层级一致。 */
final class TaizhouEarlyStartDialog extends TaizhouFullscreenDialog {
    TaizhouEarlyStartDialog(
            Context context, String requesterName, TaizhouEarlyStartView.Actions actions) {
        this(context, new TaizhouEarlyStartView(context, requesterName, actions));
    }

    private TaizhouEarlyStartDialog(Context context, TaizhouEarlyStartView view) {
        super(context, view, true);
        view.setDismissAction(this::dismiss);
    }
}
