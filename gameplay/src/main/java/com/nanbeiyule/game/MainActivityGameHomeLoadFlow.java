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

abstract class MainActivityGameHomeLoadFlow extends MainActivityLoadingFlow {
    protected void showGameHomePage() {
        loadGameHome();
    }

    protected void refreshGameHomeAfterForegroundReturn() {
        if (isFinishing()
                || gameHomeApiClient == null
                || authSessionCoordinator == null
                || !foregroundReturnLoadingPolicy.shouldShowLoadingOnForeground(
                        SystemClock.elapsedRealtime(),
                        authSessionCoordinator.hasRecoverableSession(),
                        currentHomeView != null
                                && currentHomeState != null
                                && loadingView instanceof GameHomeSceneLayout,
                        hasActiveLoginRequest())) {
            return;
        }
        beginForegroundReturnLoadingRequest();
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        gameHomeApiClient.loadHome(
                                accessToken,
                                new GameHomeApiClient.Callback() {
                                    @Override
                                    public void onSuccess(GameHomeState state) {
                                        callback.onSuccess(state);
                                    }

                                    @Override
                                    public void onUnauthorized() {
                                        callback.onUnauthorized();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                }),
                new AuthSessionCoordinator.Callback<GameHomeState>() {
                    @Override
                    public void onSuccess(GameHomeState state) {
                        if (!isFinishing()) {
                            finishForegroundReturnLoadingRequest();
                            displayGameHome(state);
                        } else {
                            finishForegroundReturnLoadingRequest();
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        finishForegroundReturnLoadingRequest();
                        if (!isFinishing()) {
                            showLoginPage();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        finishForegroundReturnLoadingRequest();
                        if (!isFinishing()) {
                            Toast.makeText(
                                            MainActivityGameHomeLoadFlow.this,
                                            message,
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    protected void loadGameHomeAfterWechatTransition() {
        if (isFinishing()
                || gameHomeApiClient == null
                || authSessionCoordinator == null) {
            cancelWechatPostAuthTransition();
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        gameHomeApiClient.loadHome(
                                accessToken,
                                new GameHomeApiClient.Callback() {
                                    @Override
                                    public void onSuccess(GameHomeState state) {
                                        callback.onSuccess(state);
                                    }

                                    @Override
                                    public void onUnauthorized() {
                                        callback.onUnauthorized();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                }),
                new AuthSessionCoordinator.Callback<GameHomeState>() {
                    @Override
                    public void onSuccess(GameHomeState state) {
                        if (isFinishing()) {
                            cancelWechatPostAuthTransition();
                            return;
                        }
                        completeWechatPostAuthTransition(state);
                    }

                    @Override
                    public void onLoginRequired() {
                        cancelWechatPostAuthTransition();
                        if (!isFinishing()) {
                            showLoginPage();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        cancelWechatPostAuthTransition();
                        if (isFinishing()) {
                            return;
                        }
                        Toast.makeText(
                                        MainActivityGameHomeLoadFlow.this,
                                        message,
                                        Toast.LENGTH_LONG)
                                .show();
                        showGameHomeStatus(
                                R.string.game_home_load_failed,
                                true);
                    }
                });
    }

    protected void completeWechatPostAuthTransition(
            GameHomeState state) {
        if (state == null
                || wechatLoadingStageGate == null
                || wechatPostAuthStageToken
                        == LoginLoadingStageGate.NO_TOKEN) {
            cancelWechatPostAuthTransition();
            return;
        }
        long scheduledStageToken =
                wechatPostAuthStageToken;
        if (!wechatLoadingStageGate.runAfterMinimum(
                scheduledStageToken,
                () -> {
                    if (scheduledStageToken
                                    != wechatPostAuthStageToken
                            || isFinishing()) {
                        return;
                    }
                    wechatPostAuthStageToken =
                            LoginLoadingStageGate.NO_TOKEN;
                    displayGameHome(state);
                    finishWechatLoadingRequest();
                    promptPhoneBindingAfterWechatLogin();
                })) {
            cancelWechatPostAuthTransition();
        }
    }

    protected void promptPhoneBindingAfterWechatLogin() {}

    protected void loadGameHome() {
        if (isFinishing()
                || gameHomeApiClient == null
                || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        gameHomeApiClient.loadHome(
                                accessToken,
                                new GameHomeApiClient.Callback() {
                                    @Override
                                    public void onSuccess(GameHomeState state) {
                                        callback.onSuccess(state);
                                    }

                                    @Override
                                    public void onUnauthorized() {
                                        callback.onUnauthorized();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                }),
                new AuthSessionCoordinator.Callback<GameHomeState>() {
                    @Override
                    public void onSuccess(GameHomeState state) {
                        if (!isFinishing()) {
                            displayGameHome(state);
                            checkRealNameAfterHomeLoad();
                            loadFriendsAfterHomeLoad();
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        if (!isFinishing()) {
                            showLoginPage();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (isFinishing()) {
                            return;
                        }
                        Toast.makeText(
                                        MainActivityGameHomeLoadFlow.this,
                                        message,
                                        Toast.LENGTH_LONG)
                                .show();
                        showGameHomeStatus(
                                R.string.game_home_load_failed,
                                true);
                    }
                });
    }
}
