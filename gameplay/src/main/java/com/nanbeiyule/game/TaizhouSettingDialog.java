package com.nanbeiyule.game;

import android.content.Context;

/** Original Mahjong SettingNew.csb right-side menu and full detail flow. */
final class TaizhouSettingDialog extends TaizhouFullscreenDialog {
    interface Actions extends TaizhouSettingView.Actions {}

    TaizhouSettingDialog(
            Context context,
            PersonalCenterSystemSettings settings,
            TaizhouMahjongPreferences preferences,
            TaizhouSettingStyle style,
            boolean goldRoom,
            Actions actions) {
        this(
                context,
                new TaizhouSettingView(
                        context, settings, preferences, style, goldRoom, actions));
    }

    private TaizhouSettingDialog(Context context, TaizhouSettingView view) {
        super(context, view, false);
        view.setDismissAction(this::dismiss);
    }
}
