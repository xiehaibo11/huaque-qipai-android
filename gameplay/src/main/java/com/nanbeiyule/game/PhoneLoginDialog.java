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

final class PhoneLoginDialog extends PhoneLoginDialogView {
    PhoneLoginDialog(
            Activity activity,
            AuthApiClient apiClient,
            LoginSessionStore sessionStore,
            LoginRequestLoadingController loadingController,
            OriginalLobbyAudioController audioController,
            OnLoginSuccessListener onLoginSuccessListener,
            OnOneTapRequestedListener onOneTapRequestedListener) {
        super(
                activity,
                apiClient,
                sessionStore,
                loadingController,
                audioController,
                onLoginSuccessListener,
                onOneTapRequestedListener);
    }
    protected void requestOtp() {
        String phoneNumber = normalizedPhone();
        if (phoneNumber == null) {
            setStatus(activity.getString(R.string.phone_login_invalid_phone));
            return;
        }
        setStatus(activity.getString(R.string.phone_login_sending));
        sendCodeButton.setEnabled(false);
        apiClient.requestOtp(
                phoneNumber,
                new AuthApiClient.Callback<>() {
                    @Override
                    public void onSuccess(AuthApiClient.OtpRequested result) {
                        if (!isShowing()) {
                            return;
                        }
                        setStatus(activity.getString(R.string.phone_login_code_sent));
                        startCountdown();
                        codeInput.requestFocus();
                    }

                    @Override
                    public void onError(String message) {
                        if (!isShowing()) {
                            return;
                        }
                        setStatus(message);
                        sendCodeButton.setEnabled(true);
                        sendCodeButton.setText(R.string.phone_login_send_code);
                    }
                });
    }

    protected void verifyOtp() {
        String phoneNumber = normalizedPhone();
        String code = codeInput.getText().toString().trim();
        if (phoneNumber == null) {
            setStatus(activity.getString(R.string.phone_login_invalid_phone));
            return;
        }
        if (!code.matches("\\d{6}")) {
            setStatus(activity.getString(R.string.phone_login_invalid_code));
            return;
        }
        setStatus("");
        loginButton.setEnabled(false);
        loadingToken = loadingController.begin();
        apiClient.verifyOtp(
                phoneNumber,
                code,
                new AuthApiClient.Callback<>() {
                    @Override
                    public void onSuccess(AuthApiClient.SessionTokens result) {
                        finishLoadingRequest();
                        if (!isShowing()) {
                            return;
                        }
                        sessionStore.save(result);
                        if (onLoginSuccessListener != null) {
                            onLoginSuccessListener.onLoginSuccess();
                        }
                        Toast.makeText(
                                        activity,
                                        R.string.phone_login_success,
                                        Toast.LENGTH_SHORT)
                                .show();
                        dismiss();
                    }

                    @Override
                    public void onError(String message) {
                        finishLoadingRequest();
                        if (!isShowing()) {
                            return;
                        }
                        setStatus(message);
                        loginButton.setEnabled(true);
                    }
                });
    }

    protected void finishLoadingRequest() {
        if (loadingToken == LoginRequestLoadingController.NO_TOKEN) {
            return;
        }
        loadingController.finish(loadingToken);
        loadingToken = LoginRequestLoadingController.NO_TOKEN;
    }

    protected String normalizedPhone() {
        return PhoneNumberValidator.normalize(phoneInput.getText().toString());
    }

    protected void startCountdown() {
        cancelCountdown();
        countDownTimer =
                new CountDownTimer(60_000L, 1_000L) {
                    @Override
                    public void onTick(long millisecondsUntilFinished) {
                        sendCodeButton.setText(
                                activity.getString(
                                        R.string.phone_login_countdown,
                                        Math.max(1L, millisecondsUntilFinished / 1_000L)));
                    }

                    @Override
                    public void onFinish() {
                        sendCodeButton.setEnabled(true);
                        sendCodeButton.setText(R.string.phone_login_resend_code);
                    }
                }.start();
    }

    protected void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    protected void setStatus(String message) {
        statusText.setText(message);
    }
}
