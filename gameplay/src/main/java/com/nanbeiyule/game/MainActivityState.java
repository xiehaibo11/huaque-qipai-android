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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
abstract class MainActivityState extends Activity {
    protected static final int REQUEST_CHOOSE_AVATAR = 4103;
    protected static final int REQUEST_RECORD_AUDIO = 4104;
    protected static final int IMMERSIVE_UI_FLAGS =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    protected View loadingView;
    protected LocalResourceVerifier resourceVerifier;
    protected AuthApiClient authApiClient;
    protected RegionApiClient regionApiClient;
    protected GameHomeApiClient gameHomeApiClient;
    protected PersonalCenterApiClient personalCenterApiClient;
    protected MembershipApiClient membershipApiClient;
    protected ShopApiClient shopApiClient;
    protected MainActivityCreateRoomFlow createRoomFlow;
    protected MainActivityJoinRoomFlow joinRoomFlow;
    protected MainActivityGoldChooseRoomFlow goldChooseRoomFlow;
    protected MainActivityMatchArenaFlow matchArenaFlow;
    protected String pendingJoinRoomNumber;
    protected PendingMembershipPaymentStore pendingMembershipPaymentStore;
    protected MembershipPaymentLauncher membershipPaymentLauncher;
    protected RealNameApiClient realNameApiClient;
    protected RealNameStatusStore realNameStatusStore;
    protected FriendApiClient friendApiClient;
    protected FriendDrawerStore friendDrawerStore;
    protected FriendDrawerView friendDrawerView;
    protected FriendAddDialog friendAddDialog;
    protected FriendApplicationsDialog friendApplicationsDialog;
    protected AlipayRealNameGateway alipayRealNameGateway;
    protected AvatarApiClient avatarApiClient;
    protected OriginalLobbyAudioController originalLobbyAudioController;
    protected PersonalCenterSettingsStore personalCenterSettingsStore;
    protected PersonalCenterSystemSettings personalCenterSystemSettings;
    protected AvatarImageLoader avatarImageLoader;
    protected AvatarImageProcessor avatarImageProcessor;
    protected final ExecutorService avatarProcessorExecutor =
            Executors.newSingleThreadExecutor();
    protected RegionSelectionStore regionSelectionStore;
    protected LoginSessionStore loginSessionStore;
    protected FirstLaunchAgreementStore firstLaunchAgreementStore;
    protected AuthSessionCoordinator authSessionCoordinator;
    protected OneTapLoginCoordinator oneTapLoginCoordinator;
    protected LoginRequestLoadingController loginRequestLoadingController;
    protected LoginLoadingStageGate wechatLoadingStageGate;
    protected final ForegroundReturnLoadingPolicy foregroundReturnLoadingPolicy =
            new ForegroundReturnLoadingPolicy();
    protected long foregroundReturnLoadingToken =
            LoginRequestLoadingController.NO_TOKEN;
    protected WechatAuthStateStore wechatAuthStateStore;
    protected WechatLoginManager wechatLoginManager;
    protected boolean wechatLoginPending;
    protected long wechatLoadingToken =
            LoginRequestLoadingController.NO_TOKEN;
    protected long wechatPreLaunchStageToken =
            LoginLoadingStageGate.NO_TOKEN;
    protected long wechatPostAuthStageToken =
            LoginLoadingStageGate.NO_TOKEN;
    protected long oneTapLoadingToken =
            LoginRequestLoadingController.NO_TOKEN;
    protected RegionApiClient.Catalog regionCatalog;
    protected boolean regionCatalogLoading;
    protected boolean openRegionWhenCatalogLoads;
    protected boolean returnToHomeAfterRegionSelection;
    protected FirstLaunchAgreementDialog firstLaunchAgreementDialog;
    protected PhoneLoginDialog phoneLoginDialog;
    protected AvatarEditorDialog avatarEditorDialog;
    protected PersonalCenterDialog personalCenterDialog;
    protected MembershipCenterDialog membershipCenterDialog;
    protected MembershipDailyGiftDialog membershipDailyGiftDialog;
    protected MembershipPurchaseDialog membershipPurchaseDialog;
    protected MembershipPaymentChoiceDialog membershipPaymentChoiceDialog;
    protected MembershipPaymentCancelDialog membershipPaymentCancelDialog;
    protected ShopDialog shopDialog;
    protected MoreMenuDialog moreMenuDialog;
    protected MembershipNoticeDialog membershipNoticeDialog;
    protected ShopInventoryDialog shopInventoryDialog;
    protected RealNameDialog realNameDialog;
    protected LocalResourceVerifier personalCenterRepairVerifier;
    protected boolean personalCenterLoading;
    protected boolean postAgreementStartupStarted;
    protected AvatarImageProcessor.ProcessedAvatar pendingAvatar;
    protected GameHomeView currentHomeView;
    protected GameHomeState currentHomeState;
    protected Bitmap currentAvatarBitmap;
    protected final Runnable showSecondLoadingPage =
            new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) {
                        return;
                    }
                    SecondLoadingView secondLoadingView = createSecondLoadingView();
                    secondLoadingView.setOnProgressCompleteListener(
                            new SecondLoadingView.OnProgressCompleteListener() {
                                @Override
                                public void onProgressComplete() {
                                    if (loadingView == secondLoadingView) {
                                        showAuthenticatedOrLoginPage();
                                    }
                                }
                            });
                    secondLoadingView.setTargetProgress(0.0f);
                    loadingView = secondLoadingView;
                    setContentView(secondLoadingView);
                    startLocalResourceVerification(secondLoadingView);
                }
            };

    protected abstract void configureWindow();

    protected abstract void applyImmersiveMode();

    protected abstract View createStartupView();

    protected abstract SecondLoadingView createSecondLoadingView();

    protected abstract void startPostAgreementStartup();

    protected abstract void showAuthenticatedOrLoginPage();

    protected abstract void showFirstLaunchAgreementDialog();

    protected abstract void showLoginPage();

    protected abstract void openAgreementLink(LoginAgreementLink link);

    protected abstract void showChooseAreaPage();

    protected abstract void preloadRegionCatalog();

    protected abstract void loadRegionCatalog(boolean showLoadingMessage);

    protected abstract void updateLoginRegionNameIfVisible();

    protected abstract void displayChooseAreaPage();

    protected abstract void onRegionSelectedFromMap(RegionApiClient.Lobby lobby);

    protected abstract void syncSelectedRegion(long lobbyId);

    protected abstract void syncSelectedRegion(long lobbyId, Runnable completion);

    protected abstract void syncSelectedRegion(
            long lobbyId,
            Runnable completion,
            Runnable failure);

    protected abstract void runCompletion(Runnable completion);

    protected abstract String selectedAreaName();

    protected abstract void performWechatLogin();

    protected abstract void launchWechatAuthorization(
            String state,
            long scheduledStageToken);

    protected abstract void cancelWechatPreLaunchTransition();

    protected abstract void handleWechatAuthResponse(Intent intent);

    protected abstract void handleRoomInvitationIntent(Intent intent);

    protected abstract void exchangeWechatCode(String code);

    protected abstract void showPhoneLoginDialog();

    protected abstract void startPhoneOneTapLogin();

    protected abstract void onWechatAuthenticated();

    protected abstract void onAuthenticated();

    protected abstract void beginWechatLoadingRequest();

    protected abstract void refreshWechatLoadingRequest();

    protected abstract void beginWechatPostAuthTransition();

    protected abstract void cancelWechatPostAuthTransition();

    protected abstract void finishWechatLoadingRequest();

    protected abstract void finishOneTapLoadingRequest();

    protected abstract void beginForegroundReturnLoadingRequest();

    protected abstract void finishForegroundReturnLoadingRequest();

    protected abstract boolean hasActiveLoginRequest();

    protected abstract void bindLoginRequestLoadingToGameHome();

    protected abstract void bindLoginRequestLoadingToLoginPage();

    protected abstract void showGameHomePage();

    protected abstract void refreshGameHomeAfterForegroundReturn();

    protected abstract void loadGameHomeAfterWechatTransition();

    protected abstract void completeWechatPostAuthTransition(
            GameHomeState state);

    protected abstract void loadGameHome();

    protected abstract void displayGameHome(GameHomeState state);

    protected abstract void checkRealNameAfterHomeLoad();

    protected abstract void loadFriendsAfterHomeLoad();

    protected abstract void loadAvatarForHome(GameHomeState state, GameHomeView homeView);

    protected abstract void showPersonalCenter();

    protected abstract void showPersonalCenterSettings();

    protected abstract void showMembershipCenter();

    protected abstract void showShop();

    protected abstract void showShop(ShopCategory initialCategory);

    protected abstract void showShopInventory();

    protected abstract void showDailyMission();

    /** 打开定时登录有礼全屏页；由 {@code MainActivityTimeLoginActFlow} 实现。 */
    protected abstract void showTimeLoginAct();

    protected abstract void showMail();

    protected abstract void showMoreMenu();

    protected abstract void showMembershipDailyGift();

    protected abstract void handleMembershipPaymentIntent(Intent intent);

    protected abstract void confirmPendingMembershipPaymentAfterForegroundReturn();

    protected abstract void cancelMembershipPaymentConfirmation();

    protected abstract void displayPersonalCenter(PersonalCenterState state);

    protected abstract void dismissPersonalCenter();

    protected abstract void handlePersonalCenterAction(
            PersonalCenterAction action,
            PersonalCenterState state);

    protected abstract void showInformationDialog(
            String title, String message);

    protected abstract void showPersonalCenterUnavailable(String featureName);

    protected abstract void savePrivacySettings(
            PersonalCenterPrivacySettings previous,
            PersonalCenterPrivacySettings updated);

    protected abstract void showFeedbackInput(
            PersonalCenterFeedbackItem.Category category);

    protected abstract void submitPersonalCenterFeedback(
            PersonalCenterFeedbackItem.Category category,
            String content);

    protected abstract void loadFeedbackHistory();

    protected abstract void showFeedbackHistory(
            List<PersonalCenterFeedbackItem> items);

    protected abstract void clearPersonalCenterCache();

    protected abstract void runPersonalCenterNetworkCheck();

    protected abstract void runPersonalCenterResourceRepair();

    protected abstract void openExternalUrl(String url);

    protected abstract void showAvatarEditor();

    protected abstract void chooseAvatarPhoto();

    protected abstract void processSelectedAvatar(Uri uri);

    protected abstract void uploadSelectedAvatar();

    protected abstract void showGameHomeStatus(int messageResource, boolean retryEnabled);

    protected abstract void startLocalResourceVerification(SecondLoadingView secondLoadingView);
}
