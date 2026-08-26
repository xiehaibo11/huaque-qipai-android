package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;

/** {@code Common/CSB/GameBase/PlayerInfoLayer.csb} 玩家信息面板的全屏宿主。 */
final class TaizhouPlayerInfoDialog extends TaizhouFullscreenDialog {
    private final TaizhouPlayerInfoView view;

    TaizhouPlayerInfoDialog(
            Context context,
            TaizhouPlayerInfoState state,
            Bitmap avatar,
            TaizhouPlayerInfoView.Actions actions) {
        this(context, new TaizhouPlayerInfoView(context, state, avatar, actions));
    }

    private TaizhouPlayerInfoDialog(Context context, TaizhouPlayerInfoView view) {
        super(context, view, true);
        this.view = view;
        view.setDismissAction(this::dismiss);
        setCanceledOnTouchOutside(true);
    }

    void update(TaizhouPlayerInfoState state) {
        view.update(state);
    }
}
