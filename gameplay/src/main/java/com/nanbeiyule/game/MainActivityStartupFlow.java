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

abstract class MainActivityStartupFlow extends MainActivityLifecycle {
    private static final long STARTUP_PAGE_DELAY_MS = 1_200L;

    protected View createStartupView() {
        ImageView startupView = new ImageView(this);
        startupView.setBackgroundColor(Color.rgb(250, 248, 243));
        startupView.setImageResource(R.drawable.startup_page);
        startupView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        startupView.setAdjustViewBounds(false);
        startupView.setContentDescription(getString(R.string.startup_title));
        startupView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return startupView;
    }

    protected SecondLoadingView createSecondLoadingView() {
        SecondLoadingView secondLoadingView = new SecondLoadingView(this);
        secondLoadingView.setContentDescription(getString(R.string.second_loading_title));
        secondLoadingView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return secondLoadingView;
    }

    protected void startPostAgreementStartup() {
        if (postAgreementStartupStarted || isFinishing()) {
            return;
        }
        postAgreementStartupStarted = true;
        preloadRegionCatalog();
        loadingView.postDelayed(
                showSecondLoadingPage,
                STARTUP_PAGE_DELAY_MS);
        handleWechatAuthResponse(getIntent());
    }

    protected void showAuthenticatedOrLoginPage() {
        if (!firstLaunchAgreementStore.isAccepted()) {
            showFirstLaunchAgreementDialog();
            return;
        }
        if (authSessionCoordinator != null
                && authSessionCoordinator.hasRecoverableSession()) {
            showGameHomePage();
        } else {
            showLoginPage();
        }
    }

    protected void showFirstLaunchAgreementDialog() {
        if (isFinishing()
                || (firstLaunchAgreementDialog != null
                        && firstLaunchAgreementDialog.isShowing())) {
            return;
        }
        FirstLaunchAgreementDialog agreementDialog =
                new FirstLaunchAgreementDialog(this);
        firstLaunchAgreementDialog = agreementDialog;
        agreementDialog.setOnAgreementLinkRequestedListener(
                new FirstLaunchAgreementDialog
                        .OnAgreementLinkRequestedListener() {
                    @Override
                    public void onAgreementLinkRequested(
                            LoginAgreementLink link) {
                        openAgreementLink(link);
                    }
                });
        agreementDialog.setOnRejectRequestedListener(
                new FirstLaunchAgreementDialog
                        .OnRejectRequestedListener() {
                    @Override
                    public void onRejectRequested() {
                        agreementDialog.dismiss();
                        firstLaunchAgreementDialog = null;
                        finishAndRemoveTask();
                    }
                });
        agreementDialog.setOnAcceptRequestedListener(
                new FirstLaunchAgreementDialog
                        .OnAcceptRequestedListener() {
                    @Override
                    public void onAcceptRequested() {
                        firstLaunchAgreementStore.accept();
                        agreementDialog.dismiss();
                        firstLaunchAgreementDialog = null;
                        startPostAgreementStartup();
                    }
                });
        agreementDialog.show();
    }

    protected void showLoginPage() {
        if (isFinishing()) {
            return;
        }
        if (originalLobbyAudioController != null) {
            originalLobbyAudioController.setLobbyActive(false);
        }
        if (resourceVerifier != null) {
            resourceVerifier.cancel();
            resourceVerifier = null;
        }
        currentHomeView = null;
        currentHomeState = null;
        currentAvatarBitmap = null;
        pendingAvatar = null;
        dismissPersonalCenter();
        personalCenterLoading = false;
        if (avatarEditorDialog != null) {
            avatarEditorDialog.dismiss();
            avatarEditorDialog = null;
        }
        finishForegroundReturnLoadingRequest();
        LoginPageView loginPageView = new LoginPageView(this);
        LoginView loginView = loginPageView.loginView();
        if (originalLobbyAudioController != null) {
            loginView.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        loginView.setOnPhoneOneTapLoginRequestedListener(
                new LoginView.OnPhoneOneTapLoginRequestedListener() {
                    @Override
                    public void onPhoneOneTapLoginRequested() {
                        startPhoneOneTapLogin();
                    }
                });
        loginView.setOnPhoneLoginRequestedListener(
                new LoginView.OnPhoneLoginRequestedListener() {
                    @Override
                    public void onPhoneLoginRequested() {
                        showPhoneLoginDialog();
                    }
                });
        loginView.setOnWechatLoginRequestedListener(
                new LoginView.OnWechatLoginRequestedListener() {
                    @Override
                    public void onWechatLoginRequested() {
                        performWechatLogin();
                    }
                });
        loginView.setOnRegionSelectionRequestedListener(
                new LoginView.OnRegionSelectionRequestedListener() {
                    @Override
                    public void onRegionSelectionRequested() {
                        returnToHomeAfterRegionSelection = false;
                        showChooseAreaPage();
                    }
                });
        loginView.setOnAgreementLinkRequestedListener(
                new LoginView.OnAgreementLinkRequestedListener() {
                    @Override
                    public void onAgreementLinkRequested(LoginAgreementLink link) {
                        openAgreementLink(link);
                    }
                });
        String selectedAreaName = selectedAreaName();
        if (selectedAreaName != null) {
            loginView.setSelectedRegionName(selectedAreaName);
        }
        loginView.setContentDescription(getString(R.string.login_page_title));
        loginPageView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loadingView = loginPageView;
        setContentView(loginPageView);
        loginRequestLoadingController.bind(
                loginPageView.loadingView());
    }

    protected void openAgreementLink(LoginAgreementLink link) {
        try {
            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(link.url())));
        } catch (RuntimeException exception) {
            Toast.makeText(
                            this,
                            R.string.login_agreement_open_failed,
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
