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

abstract class MainActivityLoadingFlow extends MainActivityPhoneFlow {
    protected void beginWechatLoadingRequest() {
        if (loginRequestLoadingController == null) {
            return;
        }
        wechatLoadingToken =
                loginRequestLoadingController.begin();
    }

    protected void refreshWechatLoadingRequest() {
        if (loginRequestLoadingController == null) {
            return;
        }
        wechatLoadingToken =
                loginRequestLoadingController.refresh(
                        wechatLoadingToken);
    }

    protected void beginWechatPostAuthTransition() {
        if (wechatLoadingStageGate == null) {
            finishWechatLoadingRequest();
            return;
        }
        refreshWechatLoadingRequest();
        wechatPostAuthStageToken =
                wechatLoadingStageGate.begin();
    }

    protected void cancelWechatPostAuthTransition() {
        long stageToken = wechatPostAuthStageToken;
        if (stageToken != LoginLoadingStageGate.NO_TOKEN
                && wechatLoadingStageGate != null) {
            wechatLoadingStageGate.cancel(stageToken);
        }
        wechatPostAuthStageToken =
                LoginLoadingStageGate.NO_TOKEN;
        finishWechatLoadingRequest();
    }

    protected void finishWechatLoadingRequest() {
        if (loginRequestLoadingController != null) {
            loginRequestLoadingController.finish(
                    wechatLoadingToken);
        }
        wechatLoadingToken =
                LoginRequestLoadingController.NO_TOKEN;
    }

    protected void finishOneTapLoadingRequest() {
        if (loginRequestLoadingController != null) {
            loginRequestLoadingController.finish(
                    oneTapLoadingToken);
        }
        oneTapLoadingToken =
                LoginRequestLoadingController.NO_TOKEN;
    }

    protected void beginForegroundReturnLoadingRequest() {
        if (loginRequestLoadingController == null) {
            return;
        }
        bindLoginRequestLoadingToGameHome();
        foregroundReturnLoadingToken =
                loginRequestLoadingController.begin();
    }

    protected void finishForegroundReturnLoadingRequest() {
        if (loginRequestLoadingController != null) {
            loginRequestLoadingController.finish(
                    foregroundReturnLoadingToken);
        }
        foregroundReturnLoadingToken =
                LoginRequestLoadingController.NO_TOKEN;
    }

    protected boolean hasActiveLoginRequest() {
        return wechatLoginPending
                || wechatLoadingToken != LoginRequestLoadingController.NO_TOKEN
                || oneTapLoadingToken != LoginRequestLoadingController.NO_TOKEN
                || (phoneLoginDialog != null && phoneLoginDialog.isShowing());
    }

    protected void bindLoginRequestLoadingToGameHome() {
        if (loginRequestLoadingController == null) {
            return;
        }
        if (loadingView instanceof GameHomeSceneLayout scene) {
            loginRequestLoadingController.bind(scene.loadingView());
        } else {
            loginRequestLoadingController.bind(null);
        }
    }

    protected void bindLoginRequestLoadingToLoginPage() {
        if (loginRequestLoadingController == null) {
            return;
        }
        if (authSessionCoordinator != null
                && authSessionCoordinator.hasRecoverableSession()) {
            loginRequestLoadingController.bind(null);
            return;
        }
        if (loadingView instanceof LoginPageView loginPageView) {
            loginRequestLoadingController.bind(
                    loginPageView.loadingView());
        } else {
            loginRequestLoadingController.bind(null);
        }
    }
}
