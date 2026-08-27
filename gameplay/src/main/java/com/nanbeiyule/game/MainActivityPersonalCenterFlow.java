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
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.Settings;
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

abstract class MainActivityPersonalCenterFlow
        extends MainActivityMailFlow {
    private CountDownTimer personalCenterPhoneTimer;
    private ZhejiangLobbySettingsDialog lobbySettingsDialog;
    protected void showPersonalCenter() {
        showPersonalCenter(-1);
    }

    protected final void showPhoneBinding() {
        showPersonalCenter(PersonalCenterView.PHONE_BINDING_TAB);
    }

    @Override
    protected void promptPhoneBindingAfterWechatLogin() {
        if (isFinishing()
                || currentHomeView == null
                || currentHomeState == null
                || currentAvatarBitmap == null
                || personalCenterDialog != null
                || personalCenterLoading
                || personalCenterApiClient == null
                || authSessionCoordinator == null) {
            return;
        }
        personalCenterLoading = true;
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        personalCenterApiClient.load(
                                accessToken,
                                new PersonalCenterApiClient.Callback() {
                                    @Override
                                    public void onSuccess(
                                            PersonalCenterState state) {
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
                new AuthSessionCoordinator.Callback<
                        PersonalCenterState>() {
                    @Override
                    public void onSuccess(PersonalCenterState state) {
                        personalCenterLoading = false;
                        if (isFinishing()
                                || currentHomeView == null
                                || currentHomeState == null
                                || personalCenterDialog != null
                                || state.account().phoneBound()
                                || !currentHomeState
                                        .player()
                                        .userId()
                                        .equals(state.player().userId())) {
                            return;
                        }
                        displayPersonalCenter(
                                state,
                                PersonalCenterView.PHONE_BINDING_TAB);
                    }

                    @Override
                    public void onLoginRequired() {
                        personalCenterLoading = false;
                        if (!isFinishing()) {
                            showLoginPage();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        personalCenterLoading = false;
                        if (!isFinishing()) {
                            Toast.makeText(
                                            MainActivityPersonalCenterFlow.this,
                                            message,
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    /** Opens the recovered Zhejiang lobby settings directly from the More menu. */
    @Override
    protected void showPersonalCenterSettings() {
        if (isFinishing() || lobbySettingsDialog != null) {
            return;
        }
        ZhejiangLobbySettingsDialog dialog =
                new ZhejiangLobbySettingsDialog(
                        this,
                        personalCenterSystemSettings,
                        new ZhejiangLobbySettingsDialog.Actions() {
                            @Override
                            public void onSettingsChanged(
                                    PersonalCenterSystemSettings settings) {
                                personalCenterSystemSettings = settings;
                                personalCenterSettingsStore.save(settings);
                                if (originalLobbyAudioController != null) {
                                    originalLobbyAudioController.applySettings(settings);
                                }
                            }

                            @Override
                            public void onLegalLinkRequested(
                                    ZhejiangLobbySettingsDialog.LegalLink link) {
                                switch (link) {
                                    case USER_SERVICE ->
                                            openAgreementLink(LoginAgreementLink.SERVICE);
                                    case PRIVACY ->
                                            openAgreementLink(LoginAgreementLink.PRIVACY);
                                    case QUALIFICATION,
                                            PERSONAL_INFORMATION,
                                            THIRD_PARTY_SHARING ->
                                            Toast.makeText(
                                                            MainActivityPersonalCenterFlow.this,
                                                            "该合规页面尚未配置",
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                }
                            }
                        });
        lobbySettingsDialog = dialog;
        if (originalLobbyAudioController != null) {
            dialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (lobbySettingsDialog == dialog) {
                        lobbySettingsDialog = null;
                    }
                    applyImmersiveMode();
                });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (lobbySettingsDialog != null) {
            lobbySettingsDialog.dismiss();
            lobbySettingsDialog = null;
        }
        super.onDestroy();
    }

    private void showPersonalCenter(int selectedTab) {
        if (isFinishing()
                || currentHomeView == null
                || currentHomeState == null
                || currentAvatarBitmap == null
                || personalCenterDialog != null
                || personalCenterLoading
                || personalCenterApiClient == null
                || authSessionCoordinator == null) {
            return;
        }
        personalCenterLoading = true;
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        personalCenterApiClient.load(
                                accessToken,
                                new PersonalCenterApiClient.Callback() {
                                    @Override
                                    public void onSuccess(
                                            PersonalCenterState state) {
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
                new AuthSessionCoordinator.Callback<
                        PersonalCenterState>() {
                    @Override
                    public void onSuccess(PersonalCenterState state) {
                        personalCenterLoading = false;
                        if (isFinishing()
                                || currentHomeView == null
                                || currentHomeState == null
                                || !currentHomeState
                                        .player()
                                        .userId()
                                        .equals(state.player().userId())) {
                            return;
                        }
                        displayPersonalCenter(state, selectedTab);
                    }

                    @Override
                    public void onLoginRequired() {
                        personalCenterLoading = false;
                        if (!isFinishing()) {
                            showLoginPage();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        personalCenterLoading = false;
                        if (!isFinishing()) {
                            Toast.makeText(
                                            MainActivityPersonalCenterFlow.this,
                                            message,
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    protected void displayPersonalCenter(PersonalCenterState state) {
        displayPersonalCenter(state, -1);
    }

    private void displayPersonalCenter(
            PersonalCenterState state,
            int selectedTab) {
        if (isFinishing()
                || personalCenterDialog != null
                || currentAvatarBitmap == null) {
            return;
        }
        personalCenterDialog =
                new PersonalCenterDialog(
                        this,
                        state,
                        personalCenterSystemSettings,
                        currentAvatarBitmap,
                        new PersonalCenterView.Listener() {
                            @Override
                            public void onCloseRequested() {
                                applyImmersiveMode();
                            }

                            @Override
                            public void onCopyPlayerIdRequested(
                                    long publicPlayerId) {
                                ClipboardManager clipboard =
                                        (ClipboardManager)
                                                getSystemService(
                                                        CLIPBOARD_SERVICE);
                                if (clipboard != null) {
                                    clipboard.setPrimaryClip(
                                            ClipData.newPlainText(
                                                    "玩家序号",
                                                    Long.toString(
                                                            publicPlayerId)));
                                }
                                Toast.makeText(
                                                MainActivityPersonalCenterFlow.this,
                                                R.string
                                                        .personal_center_copied,
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void onRefreshAvatarRequested() {
                                dismissPersonalCenter();
                                showAvatarEditor();
                            }

                            @Override
                            public void onSwitchRegionRequested() {
                                dismissPersonalCenter();
                                returnToHomeAfterRegionSelection = true;
                                showChooseAreaPage();
                            }

                            @Override
                            public void onSwitchAccountRequested() {
                                switchAccount();
                            }

                            @Override
                            public void onUnavailableRequested(
                                    String featureName) {
                                Toast.makeText(
                                                MainActivityPersonalCenterFlow.this,
                                                getString(
                                                        R.string
                                                                .personal_center_feature_unavailable,
                                                        featureName),
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void onActionRequested(
                                    PersonalCenterAction action) {
                                handlePersonalCenterAction(
                                        action, state);
                            }

                            @Override
                            public void onPrivacyChanged(
                                    PersonalCenterPrivacySettings previous,
                                    PersonalCenterPrivacySettings updated) {
                                savePrivacySettings(previous, updated);
                            }

                            @Override
                            public void onSystemSettingsChanged(
                                    PersonalCenterSystemSettings settings) {
                                personalCenterSystemSettings = settings;
                                personalCenterSettingsStore.save(settings);
                                if (originalLobbyAudioController != null) {
                                    originalLobbyAudioController.applySettings(
                                            settings);
                                }
                            }
                        });
        if (originalLobbyAudioController != null) {
            personalCenterDialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        personalCenterDialog.setOnDismissListener(
                dialog -> {
                    personalCenterDialog = null;
                    cancelPersonalCenterPhoneTimer();
                    applyImmersiveMode();
                    onPersonalCenterDismissed();
                });
        personalCenterDialog.show();
        if (selectedTab == PersonalCenterView.SYSTEM_SETTINGS_TAB) {
            personalCenterDialog.selectSystemSettings();
        } else if (selectedTab == PersonalCenterView.PHONE_BINDING_TAB) {
            personalCenterDialog.selectPhoneBinding();
        }
    }

    protected void switchAccount() {
        dismissPersonalCenter();
        authSessionCoordinator.clearSession();
        returnToHomeAfterRegionSelection = false;
        showLoginPage();
    }

    protected void dismissPersonalCenter() {
        cancelPersonalCenterPhoneTimer();
        if (personalCenterDialog != null) {
            PersonalCenterDialog dialog = personalCenterDialog;
            personalCenterDialog = null;
            dialog.setOnDismissListener(null);
            dialog.dismiss();
            applyImmersiveMode();
        }
    }

    protected void onPersonalCenterDismissed() {}

    protected void handlePersonalCenterAction(
            PersonalCenterAction action,
            PersonalCenterState state) {
        switch (action) {
            case MY_RECORDS, FAVORITES, GIFTS, MESSAGES ->
                    showPersonalCenterUnavailable(action.displayName());
            case SHOP_ROOM_CARDS -> openPersonalCenterShop(ShopCategory.ROOM_CARD);
            case SHOP_DIAMONDS -> openPersonalCenterShop(ShopCategory.DIAMOND_RECHARGE);
            case BOUND_ROOM_CARD_HELP ->
                    showInformationDialog(
                            "绑定房卡说明",
                            "绑定房卡由活动、会员礼包等方式获得，仅限当前账号使用，不能转赠或兑换现金。");
            case ACCOUNT_DELETION -> confirmAccountDeletion();
            case OPEN_APP_PERMISSION_SETTINGS -> openAppPermissionSettings();
            case TOGGLE_CLIPBOARD_PERMISSION -> { }
            case PHONE_BINDING, PHONE_SUBMIT -> bindPersonalCenterPhone();
            case PHONE_SEND_CODE -> requestPersonalCenterPhoneCode();
            case MEMBERSHIP_CENTER -> {
                dismissPersonalCenter();
                showMembershipCenter();
            }
            case MEMBERSHIP_GIFT -> {
                dismissPersonalCenter();
                showMembershipDailyGift();
            }
            case MEMBERSHIP_PREVIOUS, MEMBERSHIP_NEXT -> { }
            case LOGIN_PASSWORD ->
                    showInformationDialog(
                            "登录密码",
                            "当前登录方式："
                                    + identityProviderText(state)
                                    + "\n当前账号体系尚未开放密码登录。");
            case PAYMENT_PASSWORD ->
                    showPersonalCenterUnavailable("支付密码");
            case REAL_NAME -> showRealNameCenter();
            case DEVICE_MANAGEMENT ->
                    showInformationDialog(
                            "设备管理",
                            "当前会话已由南北娱乐令牌保护。"
                                    + "\n多设备列表与远程下线服务尚未开放。");
            case PRIVACY_POLICY ->
                    openAgreementLink(LoginAgreementLink.PRIVACY);
            case CLEAR_CACHE -> clearPersonalCenterCache();
            case NETWORK_CHECK -> runPersonalCenterNetworkCheck();
            case RESOURCE_REPAIR -> runPersonalCenterResourceRepair();
            case FAQ ->
                    showInformationDialog(
                            "常见问题",
                            "1. 切换地区会返回地区选择页。\n"
                                    + "2. 切换账号会安全退出当前会话。\n"
                                    + "3. 隐私设置保存在当前账号。\n"
                                    + "4. 系统设置保存在本机。");
            case CUSTOMER_SERVICE ->
                    openExternalUrl("https://www.nanbeiyule.com");
            case FEEDBACK ->
                    showFeedbackInput(
                            PersonalCenterFeedbackItem.Category.FEEDBACK);
            case REPORT ->
                    showFeedbackInput(
                            PersonalCenterFeedbackItem.Category.REPORT);
            case FEEDBACK_HISTORY -> loadFeedbackHistory();
        }
    }

    protected static String identityProviderText(
            PersonalCenterState state) {
        List<String> providers =
                state.account().identityProviders();
        return providers.isEmpty()
                ? "未识别"
                : String.join("、", providers);
    }

    private void openPersonalCenterShop(ShopCategory category) {
        dismissPersonalCenter();
        showShop(category);
    }

    private void openAppPermissionSettings() {
        Intent intent =
                new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void requestPersonalCenterPhoneCode() {
        PersonalCenterDialog dialog = personalCenterDialog;
        if (dialog == null
                || personalCenterApiClient == null
                || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        personalCenterApiClient.requestPhoneCode(
                                accessToken,
                                dialog.phoneNumber(),
                                resultCallback(callback)),
                new AuthSessionCoordinator.Callback<
                        PersonalCenterApiClient.PhoneCodeResult>() {
                    @Override
                    public void onSuccess(
                            PersonalCenterApiClient.PhoneCodeResult result) {
                        Toast.makeText(
                                        MainActivityPersonalCenterFlow.this,
                                        "验证码已发送",
                                        Toast.LENGTH_SHORT)
                                .show();
                        startPersonalCenterPhoneCountdown(
                                (int) Math.min(60L, result.expiresInSeconds()));
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissPersonalCenter();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                        MainActivityPersonalCenterFlow.this,
                                        message,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void bindPersonalCenterPhone() {
        PersonalCenterDialog dialog = personalCenterDialog;
        if (dialog == null
                || personalCenterApiClient == null
                || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        personalCenterApiClient.bindPhone(
                                accessToken,
                                dialog.phoneNumber(),
                                dialog.verificationCode(),
                                resultCallback(callback)),
                new AuthSessionCoordinator.Callback<
                        PersonalCenterApiClient.PhoneBindingResult>() {
                    @Override
                    public void onSuccess(
                            PersonalCenterApiClient.PhoneBindingResult result) {
                        if (result.reloginRequired()) {
                            dismissPersonalCenter();
                            authSessionCoordinator.clearSession();
                            Toast.makeText(
                                            MainActivityPersonalCenterFlow.this,
                                            "账号已安全合并，请重新登录",
                                            Toast.LENGTH_LONG)
                                    .show();
                            showLoginPage();
                            return;
                        }
                        Toast.makeText(
                                        MainActivityPersonalCenterFlow.this,
                                        "手机号绑定成功：" + result.maskedPhone(),
                                        Toast.LENGTH_SHORT)
                                .show();
                        dismissPersonalCenter();
                        showPersonalCenter();
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissPersonalCenter();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                        MainActivityPersonalCenterFlow.this,
                                        message,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void confirmAccountDeletion() {
        if (isFinishing()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("账号注销")
                .setMessage("注销后当前账号将停用，所有设备需要重新登录。此操作不可撤销，是否继续？")
                .setNegativeButton("取消", null)
                .setPositiveButton(
                        "确认注销",
                        (dialog, which) -> deactivatePersonalCenterAccount())
                .setOnDismissListener(dialog -> applyImmersiveMode())
                .show();
    }

    private void deactivatePersonalCenterAccount() {
        if (personalCenterApiClient == null || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        personalCenterApiClient.deactivateAccount(
                                accessToken, resultCallback(callback)),
                new AuthSessionCoordinator.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean ignored) {
                        dismissPersonalCenter();
                        authSessionCoordinator.clearSession();
                        Toast.makeText(
                                        MainActivityPersonalCenterFlow.this,
                                        "账号已注销",
                                        Toast.LENGTH_SHORT)
                                .show();
                        showLoginPage();
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissPersonalCenter();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                        MainActivityPersonalCenterFlow.this,
                                        message,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void startPersonalCenterPhoneCountdown(int seconds) {
        cancelPersonalCenterPhoneTimer();
        PersonalCenterDialog dialog = personalCenterDialog;
        if (dialog == null || seconds <= 0) {
            return;
        }
        dialog.setPhoneCodeSeconds(seconds);
        personalCenterPhoneTimer =
                new CountDownTimer(seconds * 1000L, 1000L) {
                    @Override
                    public void onTick(long remainingMillis) {
                        if (personalCenterDialog != null) {
                            personalCenterDialog.setPhoneCodeSeconds(
                                    (int) Math.ceil(remainingMillis / 1000.0));
                        }
                    }

                    @Override
                    public void onFinish() {
                        personalCenterPhoneTimer = null;
                        if (personalCenterDialog != null) {
                            personalCenterDialog.setPhoneCodeSeconds(0);
                        }
                    }
                }.start();
    }

    private void cancelPersonalCenterPhoneTimer() {
        if (personalCenterPhoneTimer != null) {
            personalCenterPhoneTimer.cancel();
            personalCenterPhoneTimer = null;
        }
    }

    private static <T> PersonalCenterApiClient.ResultCallback<T> resultCallback(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new PersonalCenterApiClient.ResultCallback<>() {
            @Override
            public void onSuccess(T result) {
                callback.onSuccess(result);
            }

            @Override
            public void onUnauthorized() {
                callback.onUnauthorized();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
    }

    protected void showInformationDialog(
            String title, String message) {
        if (isFinishing()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .setOnDismissListener(dialog -> applyImmersiveMode())
                .show();
    }
}
