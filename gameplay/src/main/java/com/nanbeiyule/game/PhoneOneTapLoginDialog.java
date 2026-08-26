package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

final class PhoneOneTapLoginDialog extends Dialog {
    interface OnUseOtherPhoneRequestedListener {
        void onUseOtherPhoneRequested();
    }

    private final Activity activity;
    private final OnUseOtherPhoneRequestedListener onUseOtherPhoneRequestedListener;

    PhoneOneTapLoginDialog(
            Activity activity,
            OnUseOtherPhoneRequestedListener onUseOtherPhoneRequestedListener) {
        super(activity);
        this.activity = activity;
        this.onUseOtherPhoneRequestedListener = onUseOtherPhoneRequestedListener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(createContent());
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void show() {
        super.show();
        PhoneLoginDialogChrome.show(this, activity);
    }

    private PhoneLoginPanel createContent() {
        PhoneLoginPanel panel =
                PhoneLoginDialogChrome.panel(activity);

        TextView title =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_one_tap_title),
                        PhoneLoginDialogChrome.COLOR_BROWN,
                        Gravity.CENTER,
                        true);
        panel.addAt(title, 330, 6, 650, 90, 46.0f);
        panel.addAt(
                PhoneLoginDialogChrome.closeHitTarget(
                        activity, view -> dismiss()),
                1188,
                0,
                116,
                112);

        TextView status =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_one_tap_preparing),
                        PhoneLoginDialogChrome.COLOR_BROWN,
                        Gravity.CENTER,
                        true);
        panel.addAt(status, 250, 185, 820, 80, 34.0f);

        TextView provider =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_one_tap_provider),
                        PhoneLoginDialogChrome.COLOR_BROWN,
                        Gravity.CENTER,
                        false);
        panel.addAt(provider, 430, 285, 440, 50, 25.0f);

        Button useOtherPhone =
                PhoneLoginDialogChrome.linkButton(
                        activity,
                        activity.getString(R.string.phone_one_tap_use_other_phone));
        useOtherPhone.setOnClickListener(
                view -> {
                    dismiss();
                    if (onUseOtherPhoneRequestedListener != null) {
                        onUseOtherPhoneRequestedListener.onUseOtherPhoneRequested();
                    }
                });
        panel.addAt(useOtherPhone, 430, 395, 440, 70, 30.0f);

        TextView agreement =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_login_agreement),
                        PhoneLoginDialogChrome.COLOR_BROWN,
                        Gravity.CENTER,
                        false);
        panel.addAt(agreement, 250, 535, 820, 52, 24.0f);

        TextView footer =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_login_auto_register),
                        PhoneLoginDialogChrome.COLOR_MUTED_BROWN,
                        Gravity.CENTER,
                        false);
        panel.addAt(footer, 320, 596, 660, 42, 22.0f);
        return panel;
    }
}
