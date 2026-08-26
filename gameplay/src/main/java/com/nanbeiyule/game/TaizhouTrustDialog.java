package com.nanbeiyule.game;

import android.content.Context;

/** {@code Common/CSB/GameBase/TrustLayer.csb} 托管层的全屏宿主。 */
final class TaizhouTrustDialog extends TaizhouFullscreenDialog {
    TaizhouTrustDialog(Context context, int punishSeconds, TaizhouTrustView.Actions actions) {
        this(context, new TaizhouTrustView(context, punishSeconds, actions));
    }

    private TaizhouTrustDialog(Context context, TaizhouTrustView view) {
        // 原版 _KW_PANEL_ROBOT 自带 alpha=178 遮罩，窗口层不再叠加系统压暗。
        super(context, view, false);
        view.setDismissAction(this::dismiss);
    }
}
