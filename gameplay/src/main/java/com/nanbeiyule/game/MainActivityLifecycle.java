package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import com.nanbeiyule.game.wechat.WechatAuthResponse;
import com.nanbeiyule.game.wechat.WechatAuthStateStore;
import com.nanbeiyule.game.wechat.WechatCallbackContract;
import com.nanbeiyule.game.wechat.WechatLoginManager;
import java.io.File;
import java.util.List;

abstract class MainActivityLifecycle extends MainActivityState {
    private OnBackInvokedCallback backInvokedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backInvokedCallback = this::dispatchBackRequest;
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, backInvokedCallback);
        }
        configureWindow();
        originalLobbyAudioController =
                new OriginalLobbyAudioController(this);
        personalCenterSettingsStore =
                new PersonalCenterSettingsStore(this);
        personalCenterSystemSettings =
                personalCenterSettingsStore.load();
        originalLobbyAudioController.applySettings(
                personalCenterSystemSettings);
        authApiClient = new AuthApiClient();
        regionApiClient = new RegionApiClient();
        gameHomeApiClient = new GameHomeApiClient();
        personalCenterApiClient = new PersonalCenterApiClient();
        membershipApiClient = new MembershipApiClient();
        shopApiClient = new ShopApiClient();
        pendingMembershipPaymentStore =
                new PendingMembershipPaymentStore(this);
        membershipPaymentLauncher =
                new MembershipPaymentLauncher(
                        this, pendingMembershipPaymentStore);
        realNameApiClient = new RealNameApiClient();
        realNameStatusStore = new RealNameStatusStore(this);
        friendApiClient =
                new FriendApiClient(resourceId -> getString(resourceId));
        friendDrawerStore = new FriendDrawerStore(this);
        alipayRealNameGateway = AlipayRealNameGatewayFactory.create(this);
        avatarApiClient = new AvatarApiClient();
        avatarImageLoader = new AvatarImageLoader(this, avatarApiClient);
        avatarImageProcessor = new AvatarImageProcessor(this);
        regionSelectionStore = new RegionSelectionStore(this);
        loginSessionStore = new LoginSessionStore(this);
        firstLaunchAgreementStore =
                new FirstLaunchAgreementStore(this);
        authSessionCoordinator =
                new AuthSessionCoordinator(loginSessionStore, authApiClient);
        loginRequestLoadingController =
                LoginRequestLoadingController.createForMainThread();
        wechatLoadingStageGate =
                LoginLoadingStageGate.createForMainThread();
        wechatAuthStateStore = new WechatAuthStateStore(this);
        wechatLoginManager = new WechatLoginManager(this);
        loadingView = createStartupView();
        setContentView(loadingView);
        handleRoomInvitationIntent(getIntent());
        if (!firstLaunchAgreementStore.isAccepted()) {
            showFirstLaunchAgreementDialog();
            return;
        }
        handleMembershipPaymentIntent(getIntent());
        startPostAgreementStartup();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (originalLobbyAudioController != null) {
            originalLobbyAudioController.onStart();
        }
        refreshGameHomeAfterForegroundReturn();
        if (firstLaunchAgreementStore != null
                && firstLaunchAgreementStore.isAccepted()) {
            confirmPendingMembershipPaymentAfterForegroundReturn();
        }
    }

    @Override
    protected void onStop() {
        foregroundReturnLoadingPolicy.onStoppedAt(SystemClock.elapsedRealtime());
        if (originalLobbyAudioController != null) {
            originalLobbyAudioController.onStop();
        }
        cancelWechatPreLaunchTransition();
        cancelWechatPostAuthTransition();
        if (oneTapLoginCoordinator != null) {
            oneTapLoginCoordinator.cancel();
        }
        finishOneTapLoadingRequest();
        finishForegroundReturnLoadingRequest();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && backInvokedCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backInvokedCallback);
            backInvokedCallback = null;
        }
        if (loadingView != null) {
            loadingView.removeCallbacks(showSecondLoadingPage);
            loadingView = null;
        }
        if (resourceVerifier != null) {
            resourceVerifier.cancel();
            resourceVerifier = null;
        }
        if (phoneLoginDialog != null) {
            phoneLoginDialog.dismiss();
            phoneLoginDialog = null;
        }
        if (firstLaunchAgreementDialog != null) {
            firstLaunchAgreementDialog.dismiss();
            firstLaunchAgreementDialog = null;
        }
        if (oneTapLoginCoordinator != null) {
            oneTapLoginCoordinator.destroy();
            oneTapLoginCoordinator = null;
        }
        cancelWechatPreLaunchTransition();
        cancelWechatPostAuthTransition();
        finishWechatLoadingRequest();
        finishOneTapLoadingRequest();
        finishForegroundReturnLoadingRequest();
        cancelMembershipPaymentConfirmation();
        if (wechatLoadingStageGate != null) {
            wechatLoadingStageGate.destroy();
            wechatLoadingStageGate = null;
        }
        if (loginRequestLoadingController != null) {
            loginRequestLoadingController.destroy();
            loginRequestLoadingController = null;
        }
        if (avatarEditorDialog != null) {
            avatarEditorDialog.dismiss();
            avatarEditorDialog = null;
        }
        if (personalCenterDialog != null) {
            personalCenterDialog.dismiss();
            personalCenterDialog = null;
        }
        if (membershipCenterDialog != null) {
            membershipCenterDialog.dismiss();
            membershipCenterDialog = null;
        }
        if (membershipDailyGiftDialog != null) {
            membershipDailyGiftDialog.dismiss();
            membershipDailyGiftDialog = null;
        }
        if (membershipPurchaseDialog != null) {
            membershipPurchaseDialog.dismiss();
            membershipPurchaseDialog = null;
        }
        if (membershipPaymentChoiceDialog != null) {
            membershipPaymentChoiceDialog.dismiss();
            membershipPaymentChoiceDialog = null;
        }
        if (membershipPaymentCancelDialog != null) {
            membershipPaymentCancelDialog.dismiss();
            membershipPaymentCancelDialog = null;
        }
        if (shopDialog != null) {
            shopDialog.dismiss();
            shopDialog = null;
        }
        if (moreMenuDialog != null) {
            moreMenuDialog.dismiss();
            moreMenuDialog = null;
        }
        if (membershipNoticeDialog != null) {
            membershipNoticeDialog.dismiss();
            membershipNoticeDialog = null;
        }
        if (shopInventoryDialog != null) {
            shopInventoryDialog.dismiss();
            shopInventoryDialog = null;
        }
        if (createRoomFlow != null) {
            createRoomFlow.destroy();
            createRoomFlow = null;
        }
        if (joinRoomFlow != null) {
            joinRoomFlow.destroy();
            joinRoomFlow = null;
        }
        if (goldChooseRoomFlow != null) {
            goldChooseRoomFlow.dismiss();
            goldChooseRoomFlow.shutdown();
            goldChooseRoomFlow = null;
        }
        if (matchArenaFlow != null) {
            matchArenaFlow.destroy();
            matchArenaFlow = null;
        }
        if (realNameDialog != null) {
            realNameDialog.dismiss();
            realNameDialog = null;
        }
        if (friendAddDialog != null) {
            friendAddDialog.dismiss();
            friendAddDialog = null;
        }
        if (friendApplicationsDialog != null) {
            friendApplicationsDialog.dismiss();
            friendApplicationsDialog = null;
        }
        friendDrawerView = null;
        friendDrawerStore = null;
        if (friendApiClient != null) {
            friendApiClient.shutdown();
            friendApiClient = null;
        }
        alipayRealNameGateway = null;
        if (personalCenterRepairVerifier != null) {
            personalCenterRepairVerifier.cancel();
            personalCenterRepairVerifier = null;
        }
        personalCenterLoading = false;
        avatarProcessorExecutor.shutdownNow();
        if (avatarImageLoader != null) {
            avatarImageLoader.shutdown();
            avatarImageLoader = null;
        }
        if (avatarApiClient != null) {
            avatarApiClient.shutdown();
            avatarApiClient = null;
        }
        if (authApiClient != null) {
            authApiClient.shutdown();
            authApiClient = null;
        }
        if (wechatLoginManager != null) {
            wechatLoginManager.detach();
            wechatLoginManager = null;
        }
        if (regionApiClient != null) {
            regionApiClient.shutdown();
            regionApiClient = null;
        }
        if (gameHomeApiClient != null) {
            gameHomeApiClient.shutdown();
            gameHomeApiClient = null;
        }
        if (personalCenterApiClient != null) {
            personalCenterApiClient.shutdown();
            personalCenterApiClient = null;
        }
        if (membershipApiClient != null) {
            membershipApiClient.shutdown();
            membershipApiClient = null;
        }
        if (shopApiClient != null) {
            shopApiClient.shutdown();
            shopApiClient = null;
        }
        membershipPaymentLauncher = null;
        pendingMembershipPaymentStore = null;
        if (realNameApiClient != null) {
            realNameApiClient.shutdown();
            realNameApiClient = null;
        }
        if (originalLobbyAudioController != null) {
            originalLobbyAudioController.destroy();
            originalLobbyAudioController = null;
        }
        super.onDestroy();
    }

    @SuppressLint("GestureBackNavigation")
    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        dispatchBackRequest();
    }

    @SuppressWarnings("deprecation")
    private void dispatchBackRequest() {
        if (!handleBackRequest()) {
            super.onBackPressed();
        }
    }

    protected boolean handleBackRequest() {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            boolean granted =
                    grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            onRecordAudioPermissionResult(granted);
        }
    }

    protected void onRecordAudioPermissionResult(boolean granted) {}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleRoomInvitationIntent(intent);
        if (firstLaunchAgreementStore != null
                && !firstLaunchAgreementStore.isAccepted()) {
            return;
        }
        handleMembershipPaymentIntent(intent);
        handleWechatAuthResponse(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CHOOSE_AVATAR
                || resultCode != RESULT_OK
                || data == null) {
            return;
        }
        Uri uri = data.getData();
        if (uri != null) {
            processSelectedAvatar(uri);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveMode();
        }
    }

    protected void configureWindow() {
        FullscreenWindowPolicy.apply(getWindow());
        applyImmersiveMode();
    }

    protected void applyImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(IMMERSIVE_UI_FLAGS);
    }
}
