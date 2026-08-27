package com.nanbeiyule.game;

import android.content.Context;

/** {@code CF.TipTool.showTip({type = TIP_LAYER_TYPE.OK, funcOK = ...}, message)}。 */
final class TaizhouTipDialog extends TaizhouFullscreenDialog {
    TaizhouTipDialog(Context context, String message, Runnable confirmed) {
        this(context, new TaizhouTipView(context, message, confirmed));
    }

    private TaizhouTipDialog(Context context, TaizhouTipView view) {
        super(context, view, true);
        view.setDismissAction(this::dismiss);
    }
}
