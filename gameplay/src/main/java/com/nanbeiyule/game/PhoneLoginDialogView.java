package com.nanbeiyule.game;

import android.app.Activity;
import android.app.Dialog;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

abstract class PhoneLoginDialogView extends Dialog {
    interface OnLoginSuccessListener {
        void onLoginSuccess();
    }

    interface OnOneTapRequestedListener {
        void onOneTapRequested();
    }

    protected final Activity activity;
    protected final AuthApiClient apiClient;
    protected final LoginSessionStore sessionStore;
    protected final LoginRequestLoadingController loadingController;
    protected final OriginalLobbyAudioController audioController;
    protected final OnLoginSuccessListener onLoginSuccessListener;
    protected final OnOneTapRequestedListener onOneTapRequestedListener;

    protected LoginRequestLoadingView loadingView;
    protected long loadingToken =
            LoginRequestLoadingController.NO_TOKEN;
    protected EditText phoneInput;
    protected EditText codeInput;
    protected Button sendCodeButton;
    protected Button loginButton;
    protected TextView statusText;
    protected CountDownTimer countDownTimer;

    PhoneLoginDialogView(
            Activity activity,
            AuthApiClient apiClient,
            LoginSessionStore sessionStore,
            LoginRequestLoadingController loadingController,
            OriginalLobbyAudioController audioController,
            OnLoginSuccessListener onLoginSuccessListener,
            OnOneTapRequestedListener onOneTapRequestedListener) {
        super(activity);
        this.activity = activity;
        this.apiClient = apiClient;
        this.sessionStore = sessionStore;
        this.loadingController = loadingController;
        this.audioController = audioController;
        this.onLoginSuccessListener = onLoginSuccessListener;
        this.onOneTapRequestedListener = onOneTapRequestedListener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(createContent());
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void show() {
        super.show();
        PhoneLoginDialogChrome.show(this, activity);
        loadingController.bind(loadingView);
    }

    @Override
    public void dismiss() {
        finishLoadingRequest();
        loadingController.unbind(loadingView);
        cancelCountdown();
        super.dismiss();
    }

    protected View createContent() {
        FrameLayout root = new FrameLayout(activity);
        PhoneLoginPanel panel =
                PhoneLoginDialogChrome.panel(activity);

        TextView title =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_login_title),
                        PhoneLoginDialogChrome.COLOR_BROWN,
                        Gravity.CENTER,
                        true);
        panel.addAt(title, 330, 6, 650, 90, 46.0f);
        panel.addAt(
                PhoneLoginDialogChrome.closeHitTarget(
                        activity,
                        view -> playButtonClickAnd(this::dismiss)),
                1188,
                0,
                116,
                112);

        TextView phoneLabel =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_login_phone_label),
                        PhoneLoginDialogChrome.COLOR_BROWN,
                        Gravity.CENTER_VERTICAL,
                        true);
        panel.addAt(phoneLabel, 155, 145, 160, 70, 34.0f);

        phoneInput =
                PhoneLoginDialogChrome.input(
                        activity,
                        activity.getString(R.string.phone_login_phone_hint));
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(18)});
        panel.addAt(phoneInput, 330, 140, 760, 86, 30.0f);

        TextView codeLabel =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_login_code_label),
                        PhoneLoginDialogChrome.COLOR_BROWN,
                        Gravity.CENTER_VERTICAL,
                        true);
        panel.addAt(codeLabel, 155, 262, 160, 70, 34.0f);

        codeInput =
                PhoneLoginDialogChrome.input(
                        activity,
                        activity.getString(R.string.phone_login_code_hint));
        codeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
        panel.addAt(codeInput, 330, 255, 470, 86, 29.0f);

        sendCodeButton =
                PhoneLoginDialogChrome.actionButton(
                        activity,
                        activity.getString(R.string.phone_login_send_code));
        panel.addAt(sendCodeButton, 820, 255, 270, 86, 30.0f);

        statusText =
                PhoneLoginDialogChrome.text(
                        activity,
                        "",
                        PhoneLoginDialogChrome.COLOR_ERROR,
                        Gravity.CENTER,
                        false);
        panel.addAt(statusText, 330, 348, 760, 42, 23.0f);

        loginButton =
                PhoneLoginDialogChrome.actionButton(
                        activity,
                        activity.getString(R.string.phone_login_submit));
        panel.addAt(loginButton, 330, 395, 620, 104, 45.0f);

        Button oneTapSwitch =
                PhoneLoginDialogChrome.linkButton(
                        activity,
                        activity.getString(R.string.phone_login_switch_one_tap));
        oneTapSwitch.setOnClickListener(
                view ->
                        playButtonClickAnd(
                                () -> {
                                    dismiss();
                                    if (onOneTapRequestedListener != null) {
                                        onOneTapRequestedListener
                                                .onOneTapRequested();
                                    }
                                }));
        panel.addAt(oneTapSwitch, 430, 520, 440, 58, 30.0f);

        TextView agreement =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_login_agreement),
                        PhoneLoginDialogChrome.COLOR_BROWN,
                        Gravity.CENTER,
                        false);
        panel.addAt(agreement, 250, 592, 820, 52, 24.0f);

        TextView footer =
                PhoneLoginDialogChrome.text(
                        activity,
                        activity.getString(R.string.phone_login_auto_register),
                        PhoneLoginDialogChrome.COLOR_MUTED_BROWN,
                        Gravity.CENTER,
                        false);
        panel.addAt(footer, 320, 646, 660, 42, 22.0f);

        sendCodeButton.setOnClickListener(
                view -> playButtonClickAnd(this::requestOtp));
        loginButton.setOnClickListener(
                view -> playButtonClickAnd(this::verifyOtp));
        root.addView(
                panel,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        loadingView = new LoginRequestLoadingView(activity);
        root.addView(
                loadingView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        return root;
    }

    protected void playButtonClickAnd(Runnable action) {
        if (audioController != null) {
            audioController.playButtonClick();
        }
        action.run();
    }
    protected abstract void requestOtp();

    protected abstract void verifyOtp();

    protected abstract void finishLoadingRequest();

    protected abstract void cancelCountdown();
}
