package com.nanbeiyule.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.nanbeiyule.game.wechat.WechatAuthResponse;
import com.nanbeiyule.game.wechat.WechatAuthStateStore;
import com.nanbeiyule.game.wechat.WechatCallbackContract;
import com.nanbeiyule.game.wechat.WechatLoginManager;
import java.io.File;
import java.util.List;

abstract class MainActivityPhoneFlow extends MainActivityWechatFlow {
    protected void showPhoneLoginDialog() {
        if (isFinishing()
                || authApiClient == null
                || (phoneLoginDialog != null && phoneLoginDialog.isShowing())) {
            return;
        }
        PhoneLoginDialog dialogInstance =
                new PhoneLoginDialog(
                        this,
                        authApiClient,
                        loginSessionStore,
                        loginRequestLoadingController,
                        originalLobbyAudioController,
                        new PhoneLoginDialog.OnLoginSuccessListener() {
                            @Override
                            public void onLoginSuccess() {
                                onAuthenticated();
                            }
                        },
                        new PhoneLoginDialog.OnOneTapRequestedListener() {
                            @Override
                            public void onOneTapRequested() {
                                startPhoneOneTapLogin();
                            }
                        });
        phoneLoginDialog = dialogInstance;
        dialogInstance.setOnDismissListener(
                dialog -> {
                    if (phoneLoginDialog == dialogInstance) {
                        phoneLoginDialog = null;
                    }
                    bindLoginRequestLoadingToLoginPage();
                });
        dialogInstance.show();
    }

    protected void startPhoneOneTapLogin() {
        if (isFinishing()
                || authApiClient == null
                || loginSessionStore == null) {
            return;
        }
        if (phoneLoginDialog != null && phoneLoginDialog.isShowing()) {
            phoneLoginDialog.dismiss();
        }
        if (oneTapLoginCoordinator == null) {
            OneTapLoginGateway gateway =
                    OneTapGatewayFactory.create(this);
            oneTapLoginCoordinator =
                    new OneTapLoginCoordinator(
                            this,
                            gateway,
                            (provider, credential, callback) ->
                                    authApiClient.loginWithProvider(
                                            provider,
                                            credential,
                                            new AuthApiClient.Callback<>() {
                                                @Override
                                                public void onSuccess(
                                                        AuthApiClient.SessionTokens result) {
                                                    callback.onSuccess(result);
                                                }

                                                @Override
                                                public void onError(String message) {
                                                    callback.onError(message);
                                                }
                                            }),
                            loginSessionStore::save,
                            new OneTapLoginCoordinator.View() {
                                @Override
                                public void onOneTapLoginStarted() {
                                    oneTapLoadingToken =
                                            loginRequestLoadingController.begin();
                                }

                                @Override
                                public void onOneTapLoginSucceeded() {
                                    finishOneTapLoadingRequest();
                                    if (isFinishing()) {
                                        return;
                                    }
                                    Toast.makeText(
                                                    MainActivityPhoneFlow.this,
                                                    R.string.phone_login_success,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    onAuthenticated();
                                }

                                @Override
                                public void onSmsFallback(String errorMessage) {
                                    finishOneTapLoadingRequest();
                                    if (isFinishing()) {
                                        return;
                                    }
                                    if (errorMessage != null
                                            && !errorMessage.isBlank()) {
                                        Toast.makeText(
                                                        MainActivityPhoneFlow.this,
                                                        errorMessage,
                                                        Toast.LENGTH_LONG)
                                                .show();
                                    }
                                    showPhoneLoginDialog();
                                }
                            });
        }
        oneTapLoginCoordinator.preload();
        oneTapLoginCoordinator.start();
    }

    protected void onWechatAuthenticated() {
        finishOneTapLoadingRequest();
        if (isFinishing()) {
            cancelWechatPostAuthTransition();
            return;
        }
        if (loadingView != null) {
            loadingView.removeCallbacks(showSecondLoadingPage);
        }
        long lobbyId =
                regionSelectionStore == null
                        ? 0L
                        : regionSelectionStore.getSelectedLobbyId();
        syncSelectedRegion(
                lobbyId,
                this::loadGameHomeAfterWechatTransition,
                this::cancelWechatPostAuthTransition);
    }

    protected void onAuthenticated() {
        finishWechatLoadingRequest();
        finishOneTapLoadingRequest();
        if (loginRequestLoadingController != null) {
            loginRequestLoadingController.finishAll();
            loginRequestLoadingController.bind(null);
        }
        if (isFinishing()) {
            return;
        }
        if (loadingView != null) {
            loadingView.removeCallbacks(showSecondLoadingPage);
        }
        long lobbyId =
                regionSelectionStore == null
                        ? 0L
                        : regionSelectionStore.getSelectedLobbyId();
        syncSelectedRegion(lobbyId, this::loadGameHome);
    }
}
