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

abstract class MainActivityWechatFlow extends MainActivityRegionFlow {
    protected void performWechatLogin() {
        if (isFinishing()
                || wechatLoginPending
                || wechatAuthStateStore == null
                || wechatLoginManager == null
                || wechatLoadingStageGate == null) {
            return;
        }
        String state = wechatAuthStateStore.begin();
        if (state.isBlank()) {
            Toast.makeText(
                            this,
                            R.string.wechat_login_start_failed,
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        wechatLoginPending = true;
        beginWechatLoadingRequest();
        wechatPreLaunchStageToken =
                wechatLoadingStageGate.begin();
        long scheduledStageToken =
                wechatPreLaunchStageToken;
        if (!wechatLoadingStageGate.runAfterMinimum(
                scheduledStageToken,
                () ->
                        launchWechatAuthorization(
                                state,
                                scheduledStageToken))) {
            cancelWechatPreLaunchTransition();
        }
    }

    protected void launchWechatAuthorization(
            String state,
            long scheduledStageToken) {
        if (scheduledStageToken
                        != wechatPreLaunchStageToken
                || isFinishing()
                || wechatLoginManager == null) {
            return;
        }
        wechatPreLaunchStageToken =
                LoginLoadingStageGate.NO_TOKEN;
        WechatLoginManager.StartResult result =
                wechatLoginManager.start(state);
        if (result == WechatLoginManager.StartResult.STARTED) {
            return;
        }

        wechatLoginPending = false;
        wechatAuthStateStore.clear();
        finishWechatLoadingRequest();
        int messageId =
                switch (result) {
                    case NOT_CONFIGURED -> R.string.wechat_login_not_configured;
                    case NOT_INSTALLED -> R.string.wechat_login_not_installed;
                    case REJECTED, STARTED -> R.string.wechat_login_start_failed;
                };
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
    }

    protected void cancelWechatPreLaunchTransition() {
        long stageToken = wechatPreLaunchStageToken;
        if (stageToken == LoginLoadingStageGate.NO_TOKEN) {
            return;
        }
        wechatPreLaunchStageToken =
                LoginLoadingStageGate.NO_TOKEN;
        if (wechatLoadingStageGate != null) {
            wechatLoadingStageGate.cancel(stageToken);
        }
        wechatLoginPending = false;
        if (wechatAuthStateStore != null) {
            wechatAuthStateStore.clear();
        }
        finishWechatLoadingRequest();
    }

    protected void handleWechatAuthResponse(Intent intent) {
        if (intent == null
                || !WechatCallbackContract.ACTION_AUTH_RESPONSE.equals(
                        intent.getAction())
                || wechatAuthStateStore == null
                || authApiClient == null
                || loginSessionStore == null) {
            return;
        }
        intent.setAction(null);
        wechatLoginPending = false;

        String state =
                intent.getStringExtra(WechatCallbackContract.EXTRA_STATE);
        if (!wechatAuthStateStore.consume(state)) {
            finishWechatLoadingRequest();
            return;
        }

        WechatAuthResponse response =
                WechatAuthResponse.from(
                        intent.getIntExtra(
                                WechatCallbackContract.EXTRA_ERROR_CODE,
                                Integer.MIN_VALUE),
                        intent.getStringExtra(WechatCallbackContract.EXTRA_CODE),
                        state);
        if (response.status() == WechatAuthResponse.Status.SUCCESS) {
            beginWechatPostAuthTransition();
            exchangeWechatCode(response.code());
        } else {
            cancelWechatPostAuthTransition();
            finishWechatLoadingRequest();
        }
    }

    protected void exchangeWechatCode(String code) {
        wechatLoginPending = true;
        refreshWechatLoadingRequest();
        authApiClient.loginWithProvider(
                "wechat",
                code,
                new AuthApiClient.Callback<>() {
                    @Override
                    public void onSuccess(AuthApiClient.SessionTokens result) {
                        wechatLoginPending = false;
                        if (isFinishing()) {
                            cancelWechatPostAuthTransition();
                            return;
                        }
                        loginSessionStore.save(result);
                        onWechatAuthenticated();
                    }

                    @Override
                    public void onError(String message) {
                        wechatLoginPending = false;
                        cancelWechatPostAuthTransition();
                    }
                });
    }
}
