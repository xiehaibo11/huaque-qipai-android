package com.huaque.ui;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.net.Uri;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.window.OnBackInvokedDispatcher;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaque.ui.auth.AuthViewState;
import com.huaque.ui.auth.LuaAuthRuntime;
import com.huaque.ui.friend.FriendApiClient;
import com.huaque.ui.friend.FriendPanelController;
import com.huaque.ui.friend.FriendPanelView;
import com.huaque.ui.wechat.WechatSubscriptionApiClient;
import com.huaque.ui.wechat.WechatSubscriptionController;
import com.huaque.ui.wechat.WechatSubscriptionOfferPolicy;
import com.huaque.ui.wechat.WechatSubscriptionOfferStore;
import com.nanbeiyule.game.auth.SecureStringStorage;
import com.nanbeiyule.game.GameHomeApiClient;
import com.nanbeiyule.game.GameHomeState;
import com.nanbeiyule.game.MailFeatureController;
import com.nanbeiyule.game.OriginalLobbyAudioController;
import com.nanbeiyule.game.OriginalLobbyEffectView;
import com.nanbeiyule.game.OriginalLobbyTapEffectView;
import com.nanbeiyule.game.PersonalCenterSettingsStore;
import com.nanbeiyule.game.PersonalCenterSystemSettings;
import com.nanbeiyule.game.ZhejiangLobbyHeaderView;
import com.nanbeiyule.game.ZhejiangLobbySettingsDialog;
import com.nanbeiyule.game.wechat.WechatAuthResponse;
import com.nanbeiyule.game.wechat.WechatAuthStateStore;
import com.nanbeiyule.game.wechat.WechatCallbackContract;
import com.nanbeiyule.game.wechat.WechatLoginManager;
import com.nanbeiyule.game.wechat.SecureWechatSubscriptionStore;
import com.nanbeiyule.game.wechat.WechatSubscriptionCallback;
import com.nanbeiyule.game.wechat.WechatSubscriptionPending;
import com.nanbeiyule.game.wechat.WechatSubscriptionStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQUEST_GAMEPLAY = 4100;
    private static final String ZIHUN_JINGDIAN_LIHEI_ASSET = "fonts/nanbei_lihei.ttf";
    private static final String ZHEJIANG_CUYUAN_ASSET = "fonts/fangzhengcuyuan.ttf";
    private static final int LOADING_LABEL_CENTER_Y = 736;
    private static final int LOADING_LABEL_MIN_HEIGHT = 56;
    private static final int AGREEMENT_ROW_X = 649;
    private static final int AGREEMENT_ROW_Y = 932;
    private static final int AGREEMENT_ROW_WIDTH = 615;
    private static final int AGREEMENT_ROW_HEIGHT = 36;
    private static final int USER_SERVICE_LINK_X = 865;
    private static final int USER_SERVICE_LINK_WIDTH = 194;
    private static final int PRIVACY_POLICY_LINK_X = 1064;
    private static final int PRIVACY_POLICY_LINK_WIDTH = 200;
    private static final long SESSION_RESTORE_RETRY_MILLIS = 3_000L;
    private static final long ACCOUNT_SWITCH_LOADING_MILLIS = 600L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable loadingTick;
    private int loadingProgress;
    private Typeface huaqueDisplayTypeface;
    private Typeface huaqueHeavyTypeface;
    private Typeface zhejiangLobbyTypeface;
    private LuaAuthRuntime authRuntime;
    private BoxRoot loginRoot;
    private BoxRoot phoneLoginOverlay;
    private BoxRoot registrationResultOverlay;
    private BoxRoot loginLoadingOverlay;
    private Runnable loginLoadingTick;
    private int loginLoadingProgress;
    private EditText phoneInput;
    private EditText codeInput;
    private EditText passwordInput;
    private TextView phoneLoginVerifyTab;
    private TextView phoneLoginPasswordTab;
    private TextView phoneLoginSecondLabel;
    private TextView phoneLoginRegisterLink;
    private TextView phoneLoginForgotLink;
    private View phoneLoginVerifyLine;
    private View phoneLoginPasswordLine;
    private TextView sendCodeButton;
    private ImageView loginButton;
    private TextView authStatusText;
    private boolean passwordLoginMode;
    private boolean registrationMode;
    private String registrationPhone = "";
    private Runnable authTick;
    private Runnable sessionRestoreRetry;
    private boolean restoringSession;
    private boolean authSuccessShown;
    private boolean lobbyUnauthorizedRefreshAttempted;
    private boolean agreementAccepted = LoginAgreementModel.DEFAULT_ACCEPTED;
    private ImageView agreementRowView;
    private LoginAgreementConfig agreementConfig;
    private LoginAgreementConfigClient agreementConfigClient;
    private LoginAgreementConsentStore agreementConsentStore;
    private BoxRoot agreementOverlay;
    private AlertDialog agreementRejectDialog;
    private boolean lobbyMoreMenuVisible = LobbyBottomBarModel.DEFAULT_MORE_MENU_VISIBLE;
    private final List<View> lobbyMorePopupViews = new ArrayList<>();
    private final List<ImageView> lobbyMotionViews = new ArrayList<>();
    private Runnable lobbyMotionStarter;
    private ValueAnimator lobbyMotionAnimator;
    private boolean activityResumed;
    private String friendAccessToken = "";
    private FriendPanelController friendPanelController;
    private GameHomeApiClient lobbyHomeApiClient;
    private OriginalLobbyAudioController lobbyAudioController;
    private PersonalCenterSettingsStore lobbySettingsStore;
    private PersonalCenterSystemSettings lobbySystemSettings;
    private ZhejiangLobbySettingsDialog lobbySettingsDialog;
    private ZhejiangLobbyRecordController lobbyRecordController;
    private MailFeatureController lobbyMailController;
    private ImageView lobbyMailAttentionView;
    private LobbyRoot activeLobbyRoot;
    private View lobbyHeaderView;
    private LobbyAnnouncementMarqueeView lobbyAnnouncementView;
    private String lobbyPublicPlayerId = "";
    private WechatAuthStateStore wechatAuthStateStore;
    private WechatLoginManager wechatLoginManager;
    private WechatSubscriptionController wechatSubscriptionController;
    private WechatSubscriptionOfferStore wechatSubscriptionOfferStore;
    private final WechatSubscriptionOfferPolicy wechatSubscriptionOfferPolicy =
            new WechatSubscriptionOfferPolicy();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideSystemUi();
        lobbyAudioController = new OriginalLobbyAudioController(this);
        lobbySettingsStore = new PersonalCenterSettingsStore(this);
        lobbySystemSettings = lobbySettingsStore.load();
        lobbyAudioController.applySettings(lobbySystemSettings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher()
                    .registerOnBackInvokedCallback(
                            OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                            this::handleBackNavigation);
        }
        agreementConsentStore = new LoginAgreementConsentStore(this);
        agreementConfig = LoginAgreementConfig.defaults(BuildConfig.LEGAL_BASE_URL);
        agreementConfigClient = new LoginAgreementConfigClient(
                BuildConfig.AUTH_BASE_URL,
                BuildConfig.LEGAL_BASE_URL);
        wechatAuthStateStore = new WechatAuthStateStore(this);
        WechatSubscriptionStore subscriptionStore =
                new SecureWechatSubscriptionStore(this);
        wechatLoginManager = new WechatLoginManager(this, subscriptionStore);
        wechatSubscriptionController = new WechatSubscriptionController(
                new WechatSubscriptionApiClient(
                        BuildConfig.AUTH_BASE_URL, this::runOnUiThread),
                wechatLoginManager,
                subscriptionStore,
                this::handleWechatSubscriptionEvent);
        wechatSubscriptionOfferStore = new WechatSubscriptionOfferStore(
                new SecureStringStorage(this));
        agreementConfigClient.load(config -> {
            agreementConfig = config;
            refreshAgreementGate();
        });
        if (getIntent() != null
                && WechatCallbackContract.ACTION_SUBSCRIPTION_RESPONSE.equals(
                        getIntent().getAction())) {
            handleWechatSubscriptionResponse(getIntent());
            showSplashPage();
        } else if (getIntent() != null
                && WechatCallbackContract.ACTION_AUTH_RESPONSE.equals(
                        getIntent().getAction())) {
            showLoginPage();
            handleWechatAuthResponse(getIntent());
        } else {
            showSplashPage();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null
                && WechatCallbackContract.ACTION_SUBSCRIPTION_RESPONSE.equals(
                        intent.getAction())) {
            handleWechatSubscriptionResponse(intent);
        } else {
            handleWechatAuthResponse(intent);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityResumed = true;
        if (lobbyAudioController != null) {
            lobbyAudioController.onStart();
        }
        startLobbyIconAnimations();
        flushWechatSubscription();
        if (activeLobbyRoot != null && authRuntime != null && !restoringSession) {
            loadLobbyAccountSnapshot(activeLobbyRoot);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_GAMEPLAY) {
            return;
        }
        ensureAuthRuntime();
        if (resultCode
                != com.nanbeiyule.game.MainActivity.RESULT_ACCOUNT_SWITCH_REQUESTED) {
            refreshLobbySession();
            return;
        }
        authRuntime.dispatch("logout", "", "", nowSeconds());
        if (wechatAuthStateStore != null) {
            wechatAuthStateStore.clear();
        }
        friendAccessToken = "";
        authSuccessShown = false;
        removePhoneLoginOverlay();
        LobbyRoot lobby = activeLobbyRoot;
        stopLobbyIconAnimations();
        closeLobbyHomeRequest();
        showAccountSwitchLoading(lobby);
    }

    @Override
    protected void onPause() {
        activityResumed = false;
        if (lobbyAudioController != null) {
            lobbyAudioController.onStop();
        }
        stopLobbyIconAnimations();
        super.onPause();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        handleBackNavigation();
    }

    private void handleBackNavigation() {
        if (agreementOverlay != null) {
            showAgreementRejectConfirmation();
            return;
        }
        if (lobbyMailController != null && lobbyMailController.isShowing()) {
            lobbyMailController.dismiss();
            return;
        }
        finishAfterTransition();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        stopLobbyIconAnimations();
        if (authRuntime != null) {
            authRuntime.close(nowSeconds());
        }
        if (agreementConfigClient != null) {
            agreementConfigClient.close();
        }
        if (agreementRejectDialog != null) {
            agreementRejectDialog.dismiss();
        }
        if (friendPanelController != null) {
            friendPanelController.close();
            friendPanelController = null;
        }
        if (wechatLoginManager != null) {
            wechatLoginManager.detach();
            wechatLoginManager = null;
        }
        if (wechatSubscriptionController != null) {
            wechatSubscriptionController.close();
            wechatSubscriptionController = null;
        }
        closeLobbyHomeRequest();
        if (lobbyAudioController != null) {
            lobbyAudioController.destroy();
            lobbyAudioController = null;
        }
        if (lobbySettingsDialog != null) {
            lobbySettingsDialog.dismiss();
            lobbySettingsDialog = null;
        }
        if (lobbyRecordController != null) {
            lobbyRecordController.close();
            lobbyRecordController = null;
        }
        closeLobbyMail();
        super.onDestroy();
    }

    private void hideSystemUi() {
        applyFullscreenWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller =
                    getWindow().getDecorView().getWindowInsetsController();
            if (controller != null) {
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                controller.hide(WindowInsets.Type.systemBars());
            }
            return;
        }
        hideLegacySystemUi();
    }

    private void applyFullscreenWindow() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (FullscreenWindowPolicy.disablesDecorFitting(Build.VERSION.SDK_INT)) {
            window.setDecorFitsSystemWindows(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attributes.layoutInDisplayCutoutMode =
                    FullscreenWindowPolicy.cutoutModeForApi(Build.VERSION.SDK_INT);
            window.setAttributes(attributes);
        }
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }
    }

    @SuppressWarnings("deprecation")
    private void hideLegacySystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void showSplashPage() {
        if (lobbyAudioController != null) {
            lobbyAudioController.setLobbyActive(false);
        }
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.startup_logo);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);

        int logoSize = Math.min(dp(460), Math.round(Math.min(
                getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels) * 0.58f));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(logoSize, logoSize);
        params.gravity = Gravity.CENTER;
        root.addView(logo, params);

        setContentView(root);
        handler.postDelayed(this::showBrandPage, StartupSequenceModel.LOGO_SPLASH_MILLIS);
    }

    private void showBrandPage() {
        BoxRoot root = baseRoot(R.drawable.huaque_bg_split, 0x00000000, ImageView.ScaleType.FIT_XY);
        setContentView(root);
        handler.postDelayed(this::showLoadingPage, StartupSequenceModel.BRAND_PAGE_MILLIS);
    }

    private void showLoadingPage() {
        BoxRoot root = baseRoot(R.drawable.huaque_bg_split, 0x33000000, ImageView.ScaleType.FIT_XY);

        XianyiLoadingView loadingView = new XianyiLoadingView(
                this,
                huaqueHeavyTypeface(),
                huaqueDisplayTypeface()
        );
        root.addBox(loadingView, 790, 386, 340, 340);

        TextView loadingText = label("正在加载资源", 21, 0xFFF8EED8, false);
        loadingText.setIncludeFontPadding(true);
        float shadowRadius = dp(4);
        float shadowDy = dp(2);
        loadingText.setShadowLayer(shadowRadius, 0, shadowDy, 0xCC000000);
        Paint.FontMetricsInt fontMetrics = loadingText.getPaint().getFontMetricsInt();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int loadingLabelHeight = LoadingAnimationModel.requiredVirtualTextBoxHeight(
                fontMetrics.top,
                fontMetrics.bottom,
                shadowRadius,
                shadowDy,
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                LOADING_LABEL_MIN_HEIGHT);
        root.addBox(
                loadingText,
                650,
                LoadingAnimationModel.centeredBoxTop(
                        LOADING_LABEL_CENTER_Y,
                        loadingLabelHeight),
                620,
                loadingLabelHeight);

        setContentView(root);

        loadingProgress = 0;
        loadingTick = new Runnable() {
            @Override
            public void run() {
                loadingProgress = LoadingAnimationModel.nextProgress(loadingProgress);
                loadingView.setPhase(loadingProgress);
                if (loadingProgress >= 100) {
                    restoreSessionOrShowLoginPage();
                } else {
                    handler.postDelayed(this, LoadingAnimationModel.TICK_MILLIS);
                }
            }
        };
        handler.postDelayed(loadingTick, 200);
    }

    private void showLoginPage() {
        removeRegistrationResultOverlay();
        registrationMode = false;
        if (lobbyAudioController != null) {
            lobbyAudioController.setLobbyActive(false);
        }
        if (loadingTick != null) {
            handler.removeCallbacks(loadingTick);
        }
        cancelSessionRestoreRetry();
        restoringSession = false;

        loginRoot = baseRoot(R.drawable.huaque_loading_login, 0x00000000, ImageView.ScaleType.FIT_XY);
        agreementAccepted = !LoginAgreementModel.requiresPrompt(
                agreementConsentStore.acceptedVersion(),
                agreementConfig.version());

        View phoneLoginHitArea = new View(this);
        phoneLoginHitArea.setContentDescription("手机登录");
        phoneLoginHitArea.setClickable(true);
        phoneLoginHitArea.setFocusable(true);
        phoneLoginHitArea.setOnClickListener(ignored -> openPhoneLogin());
        loginRoot.addStretchedBox(phoneLoginHitArea, 990, 795, 300, 115);

        View wechatLoginHitArea = new View(this);
        wechatLoginHitArea.setContentDescription("微信登录");
        wechatLoginHitArea.setClickable(true);
        wechatLoginHitArea.setFocusable(true);
        wechatLoginHitArea.setOnClickListener(ignored -> openWeChatLogin());
        loginRoot.addStretchedBox(wechatLoginHitArea, 620, 795, 300, 115);

        agreementRowView = new ImageView(this);
        agreementRowView.setScaleType(ImageView.ScaleType.FIT_XY);
        agreementRowView.setClickable(true);
        agreementRowView.setFocusable(true);
        agreementRowView.setOnClickListener(ignored -> toggleAgreementAccepted());
        updateAgreementRowView();
        loginRoot.addStretchedBox(
                agreementRowView,
                AGREEMENT_ROW_X,
                AGREEMENT_ROW_Y,
                AGREEMENT_ROW_WIDTH,
                AGREEMENT_ROW_HEIGHT
        );

        View userServiceLink = new View(this);
        userServiceLink.setContentDescription("用户服务协议");
        userServiceLink.setClickable(true);
        userServiceLink.setFocusable(true);
        userServiceLink.setOnClickListener(
                ignored -> openLegalDocument(agreementConfig.userServiceUrl()));
        loginRoot.addStretchedBox(
                userServiceLink,
                USER_SERVICE_LINK_X,
                AGREEMENT_ROW_Y,
                USER_SERVICE_LINK_WIDTH,
                AGREEMENT_ROW_HEIGHT);

        View privacyPolicyLink = new View(this);
        privacyPolicyLink.setContentDescription("隐私保护政策");
        privacyPolicyLink.setClickable(true);
        privacyPolicyLink.setFocusable(true);
        privacyPolicyLink.setOnClickListener(
                ignored -> openLegalDocument(agreementConfig.privacyPolicyUrl()));
        loginRoot.addStretchedBox(
                privacyPolicyLink,
                PRIVACY_POLICY_LINK_X,
                AGREEMENT_ROW_Y,
                PRIVACY_POLICY_LINK_WIDTH,
                AGREEMENT_ROW_HEIGHT);

        setContentView(loginRoot);
        if (!agreementAccepted) {
            showAgreementOverlay();
        }
    }

    private void restoreSessionOrShowLoginPage() {
        agreementAccepted = !LoginAgreementModel.requiresPrompt(
                agreementConsentStore.acceptedVersion(),
                agreementConfig.version());
        if (!agreementAccepted) {
            showLoginPage();
            return;
        }
        ensureAuthRuntime();
        restoringSession = true;
        authSuccessShown = false;
        authRuntime.dispatch("restore", "", "", nowSeconds());
    }

    private void openPhoneLogin() {
        if (!LoginAgreementModel.canContinue(agreementAccepted)) {
            toast("请先阅读并同意《用户服务协议》《隐私保护政策》");
            return;
        }
        ensureAuthRuntime();
        if (authRuntime != null) {
            authSuccessShown = false;
            authRuntime.dispatch("open", "", "", nowSeconds());
        }
    }

    private void openWeChatLogin() {
        if (!LoginAgreementModel.canContinue(agreementAccepted)) {
            toast("请先阅读并同意《用户服务协议》《隐私保护政策》");
            return;
        }
        String state = wechatAuthStateStore == null ? "" : wechatAuthStateStore.begin();
        if (state.isBlank() || wechatLoginManager == null) {
            toast("微信登录启动失败，请稍后重试");
            return;
        }
        WechatLoginManager.StartResult result = wechatLoginManager.start(state);
        if (result == WechatLoginManager.StartResult.STARTED) {
            return;
        }
        wechatAuthStateStore.clear();
        switch (result) {
            case NOT_CONFIGURED -> toast("微信登录尚未配置");
            case NOT_INSTALLED -> toast("请先安装微信客户端");
            case REJECTED, STARTED -> toast("微信登录启动失败，请稍后重试");
        }
    }

    private void handleWechatAuthResponse(Intent intent) {
        if (intent == null
                || !WechatCallbackContract.ACTION_AUTH_RESPONSE.equals(intent.getAction())
                || wechatAuthStateStore == null) {
            return;
        }
        intent.setAction(null);
        String state = intent.getStringExtra(WechatCallbackContract.EXTRA_STATE);
        if (!wechatAuthStateStore.consume(state)) {
            toast("微信登录校验失败，请重试");
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
            wechatSubscriptionOfferPolicy.onWechatAuthenticated();
            ensureAuthRuntime();
            authSuccessShown = false;
            authRuntime.dispatch("wechat", response.code(), "", nowSeconds());
        } else if (response.status() == WechatAuthResponse.Status.DENIED) {
            wechatSubscriptionOfferPolicy.onWechatAuthenticationFailed();
            toast("微信授权被拒绝");
        } else if (response.status() == WechatAuthResponse.Status.FAILED) {
            wechatSubscriptionOfferPolicy.onWechatAuthenticationFailed();
            toast("微信登录失败，请重试");
        }
    }

    private void handleWechatSubscriptionResponse(Intent intent) {
        if (intent == null
                || !WechatCallbackContract.ACTION_SUBSCRIPTION_RESPONSE.equals(
                        intent.getAction())
                || wechatSubscriptionController == null) {
            return;
        }
        intent.setAction(null);
        WechatSubscriptionPending.CaptureResult result =
                wechatSubscriptionController.capture(
                        new WechatSubscriptionCallback(
                                intent.getIntExtra(
                                        WechatCallbackContract.EXTRA_ERROR_CODE,
                                        Integer.MIN_VALUE),
                                intent.getStringExtra(WechatCallbackContract.EXTRA_ACTION),
                                intent.getStringExtra(
                                        WechatCallbackContract.EXTRA_TEMPLATE_ID),
                                intent.getIntExtra(
                                        WechatCallbackContract.EXTRA_SCENE, -1),
                                intent.getStringExtra(
                                        WechatCallbackContract.EXTRA_RESERVED),
                                intent.getStringExtra(
                                        WechatCallbackContract.EXTRA_OPEN_ID),
                                intent.getStringExtra(
                                        WechatCallbackContract.EXTRA_TRANSACTION)),
                        System.currentTimeMillis());
        if (result == WechatSubscriptionPending.CaptureResult.CAPTURED
                || result == WechatSubscriptionPending.CaptureResult.DUPLICATE) {
            flushWechatSubscription();
        } else if (result == WechatSubscriptionPending.CaptureResult.TAMPERED
                || result == WechatSubscriptionPending.CaptureResult.EXPIRED) {
            toast("微信提醒回调校验失败");
        }
    }

    private void flushWechatSubscription() {
        if (wechatSubscriptionController == null || authRuntime == null) {
            return;
        }
        wechatSubscriptionController.flush(authRuntime.accessToken());
    }

    private void handleWechatSubscriptionEvent(
            WechatSubscriptionController.Event event) {
        switch (event) {
            case NOT_CONFIGURED -> toast("微信提醒尚未配置");
            case NOT_INSTALLED -> toast("请先安装微信客户端");
            case UNSUPPORTED -> toast("当前微信版本不支持此提醒");
            case ALREADY_PENDING -> toast("已有微信提醒授权正在处理");
            case START_FAILED -> toast("微信提醒启动失败，请稍后重试");
            case CONFIRMED -> toast("微信提醒已开启");
            case UNAUTHORIZED -> refreshLobbySession();
            case STARTED, TERMINAL, NETWORK_PENDING -> {
            }
        }
    }

    private void toggleAgreementAccepted() {
        agreementAccepted = LoginAgreementModel.toggle(agreementAccepted);
        updateAgreementRowView();
    }

    private void openLegalDocument(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException ignored) {
            toast("未找到可打开协议页面的浏览器");
        }
    }

    private void updateAgreementRowView() {
        if (agreementRowView == null) {
            return;
        }
        agreementRowView.setImageResource(agreementRowDrawable());
        agreementRowView.setSelected(agreementAccepted);
        agreementRowView.setContentDescription(agreementAccepted ? "已同意协议" : "未同意协议");
    }

    private int agreementRowDrawable() {
        return agreementAccepted
                ? R.drawable.agreement_row_checked
                : R.drawable.agreement_row_unchecked;
    }

    private void refreshAgreementGate() {
        if (loginRoot == null) {
            return;
        }
        agreementAccepted = !LoginAgreementModel.requiresPrompt(
                agreementConsentStore.acceptedVersion(),
                agreementConfig.version());
        updateAgreementRowView();
        if (agreementAccepted) {
            removeAgreementOverlay();
        } else {
            showAgreementOverlay();
        }
    }

    private void showAgreementOverlay() {
        if (agreementOverlay != null || loginRoot == null) {
            return;
        }

        final int[] panel = AgreementDialogLayoutModel.dialogBounds(1920, 1080);
        agreementOverlay = new BoxRoot(this);
        agreementOverlay.setClickable(true);
        agreementOverlay.setFocusable(true);

        View dim = new View(this);
        dim.setBackgroundColor(0xB8000000);
        dim.setClickable(true);
        agreementOverlay.addStretchedBox(dim, 0, 0, 1920, 1080);

        addAgreementImage(
                agreementOverlay,
                R.drawable.agreement_dialog_panel,
                panel,
                null,
                null);
        addAgreementImage(
                agreementOverlay,
                R.drawable.agreement_dialog_header,
                AgreementDialogLayoutModel.mapLegacyBox(
                        panel, 12, 13, 1523, 118),
                null,
                null);
        addAgreementImage(
                agreementOverlay,
                R.drawable.agreement_dialog_title,
                AgreementDialogLayoutModel.mapLegacyBox(
                        panel, 525, 36, 580, 69),
                null,
                null);

        TextView welcome = agreementText(
                "欢迎来到南北娱乐，您使用本游戏前应阅读并同意",
                Gravity.START | Gravity.CENTER_VERTICAL,
                AgreementTextLayoutModel.scaledTextRatio(47, 0.95f, 75));
        addAgreementBox(
                agreementOverlay,
                welcome,
                panel,
                130,
                AgreementTextLayoutModel.centeredTop(233, 47, 75),
                1137,
                75);

        TextView userService = agreementText(
                "《南北娱乐游戏用户协议》",
                Gravity.START | Gravity.CENTER_VERTICAL,
                AgreementTextLayoutModel.scaledTextRatio(56, 0.82f, 76));
        userService.setContentDescription("打开南北娱乐游戏用户协议");
        userService.setClickable(true);
        userService.setFocusable(true);
        userService.setOnClickListener(
                ignored -> openLegalDocument(agreementConfig.userServiceUrl()));
        addAgreementBox(
                agreementOverlay,
                userService,
                panel,
                147,
                AgreementTextLayoutModel.centeredTop(371, 56, 76),
                900,
                76);

        TextView privacy = agreementText(
                "《南北娱乐游戏隐私政策》",
                Gravity.START | Gravity.CENTER_VERTICAL,
                AgreementTextLayoutModel.scaledTextRatio(56, 0.82f, 76));
        privacy.setContentDescription("打开南北娱乐游戏隐私政策");
        privacy.setClickable(true);
        privacy.setFocusable(true);
        privacy.setOnClickListener(
                ignored -> openLegalDocument(agreementConfig.privacyPolicyUrl()));
        addAgreementBox(
                agreementOverlay,
                privacy,
                panel,
                147,
                AgreementTextLayoutModel.centeredTop(463, 56, 76),
                900,
                76);

        TextView notice = agreementText(
                "您可点开以上内容查看详细说明，如您拒绝，将无法继续游戏。",
                Gravity.CENTER,
                AgreementTextLayoutModel.scaledTextRatio(54, 0.88f, 80));
        addAgreementBox(
                agreementOverlay,
                notice,
                panel,
                66,
                AgreementTextLayoutModel.centeredTop(608, 54, 80),
                1417,
                80);

        addAgreementImage(
                agreementOverlay,
                R.drawable.agreement_dialog_reject,
                AgreementDialogLayoutModel.mapLegacyBox(
                        panel, 437, 874, 252, 100),
                "拒绝用户协议和隐私政策",
                this::showAgreementRejectConfirmation);
        addAgreementImage(
                agreementOverlay,
                R.drawable.agreement_dialog_accept,
                AgreementDialogLayoutModel.mapLegacyBox(
                        panel, 868, 874, 252, 101),
                "同意用户协议和隐私政策",
                this::acceptAgreement);

        loginRoot.addView(agreementOverlay, new FrameLayout.LayoutParams(-1, -1));
        agreementOverlay.bringToFront();
    }

    private void addAgreementImage(
            BoxRoot root,
            int resource,
            int[] bounds,
            String contentDescription,
            Runnable action
    ) {
        ImageView image = new ImageView(this);
        image.setImageResource(resource);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        if (contentDescription != null) {
            image.setContentDescription(contentDescription);
        }
        if (action != null) {
            image.setClickable(true);
            image.setFocusable(true);
            image.setOnClickListener(ignored -> action.run());
        }
        root.addBox(image, bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    private void addAgreementBox(
            BoxRoot root,
            View view,
            int[] panel,
            int x,
            int y,
            int width,
            int height
    ) {
        int[] bounds = AgreementDialogLayoutModel.mapLegacyBox(
                panel, x, y, width, height);
        root.addBox(view, bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    private TextView agreementText(String text, int gravity, float textHeightRatio) {
        TextView view = new FittedTextView(this, textHeightRatio);
        view.setText(text);
        view.setTextColor(0xFF603824);
        view.setGravity(gravity);
        view.setIncludeFontPadding(true);
        view.setSingleLine(true);
        view.setTypeface(huaqueDisplayTypeface(), Typeface.NORMAL);
        return view;
    }

    private void acceptAgreement() {
        agreementConsentStore.accept(agreementConfig.version());
        agreementAccepted = true;
        updateAgreementRowView();
        removeAgreementOverlay();
    }

    private void showAgreementRejectConfirmation() {
        if (agreementRejectDialog != null && agreementRejectDialog.isShowing()) {
            return;
        }
        agreementRejectDialog = new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage("您确定要拒绝用户协议和隐私政策吗？\n拒绝后将无法进入游戏。")
                .setNegativeButton("继续阅读", null)
                .setPositiveButton("退出游戏", (dialog, which) -> finishAndRemoveTask())
                .create();
        agreementRejectDialog.setOnDismissListener(dialog -> {
            agreementRejectDialog = null;
            hideSystemUi();
        });
        agreementRejectDialog.show();
    }

    private void removeAgreementOverlay() {
        if (agreementOverlay != null && loginRoot != null) {
            loginRoot.removeView(agreementOverlay);
        }
        agreementOverlay = null;
    }

    private void ensureAuthRuntime() {
        if (authRuntime != null) {
            return;
        }
        authRuntime = new LuaAuthRuntime(this, new LuaAuthRuntime.Listener() {
            @Override
            public void onState(AuthViewState state) {
                renderAuthState(state);
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void renderAuthState(AuthViewState state) {
        if (state.authenticated() && !authSuccessShown) {
            authSuccessShown = true;
            restoringSession = false;
            cancelSessionRestoreRetry();
            friendAccessToken = authRuntime.accessToken();
            flushWechatSubscription();
            if (registrationMode) {
                showRegistrationResult("注册成功，请点击确定进入游戏", true);
                return;
            }
            authRuntime.dispatch("close", "", "", nowSeconds());
            showLobbyPage();
            return;
        }
        if ("WECHAT_VERIFYING".equals(state.phase())) {
            showLoginLoadingOverlay();
            return;
        }
        if ("WECHAT_ERROR".equals(state.phase())) {
            wechatSubscriptionOfferPolicy.onWechatAuthenticationFailed();
            removeLoginLoadingOverlay();
            toast(state.message());
            return;
        }
        if (restoringSession) {
            if ("RESTORED".equals(state.phase())) {
                restoringSession = false;
                cancelSessionRestoreRetry();
                friendAccessToken = authRuntime.accessToken();
                flushWechatSubscription();
                if (activeLobbyRoot != null) {
                    showLobbyPage();
                }
            } else if ("RESTORE_RETRY".equals(state.phase())) {
                scheduleSessionRestoreRetry();
            } else if ("RESTORE_REQUIRED".equals(state.phase())) {
                authSuccessShown = false;
                closeAuthenticatedLobby();
                showLoginPage();
            }
            return;
        }
        if (!state.visible()) {
            removePhoneLoginOverlay();
            return;
        }
        ensurePhoneLoginOverlay();
        authStatusText.setText(state.message());
        authStatusText.setTextColor("ERROR".equals(state.phase()) ? 0xFFB72E27 : 0xFF356453);
        if (registrationMode
                && "ERROR".equals(state.phase())
                && state.message().contains("已注册")) {
            showRegistrationResult("该手机号已经注册，请走手机验证码登录流程", false);
        }

        sendCodeButton.setEnabled(!passwordLoginMode && state.sendEnabled());
        sendCodeButton.setAlpha(state.sendEnabled() ? 1f : 0.62f);
        if (state.remainingSeconds() > 0) {
            sendCodeButton.setBackgroundResource(R.drawable.xianyi_login_send_code);
            sendCodeButton.setText(String.format(Locale.CHINA, "%d秒", state.remainingSeconds()));
        } else if ("REQUESTING_CODE".equals(state.phase())) {
            sendCodeButton.setBackgroundResource(R.drawable.xianyi_login_send_code);
            sendCodeButton.setText("发送中…");
        } else {
            sendCodeButton.setBackgroundResource(R.drawable.xianyi_login_send_code_active);
            sendCodeButton.setText("");
        }

        loginButton.setEnabled(state.loginEnabled());
        loginButton.setAlpha(state.loginEnabled() ? 1f : 0.62f);
        if ("VERIFYING".equals(state.phase()) || "PASSWORD_VERIFYING".equals(state.phase())) {
            loginButton.setImageDrawable(null);
            loginButton.setContentDescription("确定中");
        } else {
            loginButton.setImageResource(R.drawable.xianyi_login_confirm_text);
            loginButton.setContentDescription("确定");
        }

        updateLoginLoadingOverlay(LoginLoadingModel.isVisible(
                state.phase(),
                state.authenticated()));

        scheduleAuthTick();
    }

    private void scheduleSessionRestoreRetry() {
        cancelSessionRestoreRetry();
        sessionRestoreRetry = () -> {
            sessionRestoreRetry = null;
            if (!isFinishing() && restoringSession && authRuntime != null) {
                authRuntime.dispatch("restore", "", "", nowSeconds());
            }
        };
        handler.postDelayed(sessionRestoreRetry, SESSION_RESTORE_RETRY_MILLIS);
    }

    private void cancelSessionRestoreRetry() {
        if (sessionRestoreRetry != null) {
            handler.removeCallbacks(sessionRestoreRetry);
            sessionRestoreRetry = null;
        }
    }

    private void showLobbyPage() {
        if (loadingTick != null) {
            handler.removeCallbacks(loadingTick);
            loadingTick = null;
        }
        if (authTick != null) {
            handler.removeCallbacks(authTick);
            authTick = null;
        }
        cancelSessionRestoreRetry();
        restoringSession = false;
        removeRegistrationResultOverlay();
        removePhoneLoginOverlay();
        loginRoot = null;
        stopLobbyIconAnimations();
        lobbyMotionViews.clear();
        if (friendPanelController != null) {
            friendPanelController.close();
            friendPanelController = null;
        }
        closeLobbyHomeRequest();
        closeLobbyMail();
        lobbyPublicPlayerId = "";
        com.nanbeiyule.game.ShopAssetPreloader.preload(this);

        LobbyRoot root = new LobbyRoot(this);
        activeLobbyRoot = root;
        if (lobbyAudioController != null) {
            root.setButtonClickSound(lobbyAudioController::playButtonClick);
        }
        root.setBackgroundColor(Color.BLACK);

        addLobbyBackground(root);
        lobbyAnnouncementView = new LobbyAnnouncementMarqueeView(this);
        root.addStretchedBox(
                lobbyAnnouncementView,
                LobbyLayoutModel.x(544),
                151,
                LobbyLayoutModel.width(1365),
                57);
        addLobbyActivityCards(root);
        addLobbyImage(root, R.drawable.lobby_game_cards_static, 1159, 235, 1183, 613);
        root.addStretchedBox(
                new LobbyRoomColumnView(this),
                LobbyLayoutModel.x(LobbyRoomColumnView.PSD_X),
                LobbyRoomColumnView.PSD_Y,
                LobbyLayoutModel.width(LobbyRoomColumnView.PSD_WIDTH),
                LobbyRoomColumnView.PSD_HEIGHT);

        for (LobbyIconMotionModel.Spec spec : LobbyIconMotionModel.specs()) {
            addLobbyMotionIcon(root, spec);
        }

        addZhejiangLobbyBottomBar(root);

        addLobbyTapAction(
                root,
                "台州麻将",
                1159,
                235,
                390,
                613,
                this::openTaizhouGoldGame);
        addLobbyTap(root, "浙江挖花", 1530, 235, 393, 243, "浙江挖花");
        addLobbyTap(root, "浙江十三水", 1530, 506, 393, 343, "浙江十三水");
        addLobbyTap(
                root,
                "好友圈",
                LobbyRoomColumnView.PSD_X,
                LobbyRoomColumnView.PSD_Y + LobbyRoomColumnView.FRIEND_TOP,
                LobbyRoomColumnView.PSD_WIDTH,
                LobbyRoomColumnView.FRIEND_HEIGHT,
                "好友圈");
        addLobbyTapAction(
                root,
                "创建房间",
                LobbyRoomColumnView.PSD_X,
                LobbyRoomColumnView.PSD_Y + LobbyRoomColumnView.CREATE_TOP,
                LobbyRoomColumnView.PSD_WIDTH,
                LobbyRoomColumnView.CREATE_HEIGHT,
                () -> openGameplay(
                        com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_CREATE_ROOM));
        addLobbyTapAction(
                root,
                "加入房间",
                LobbyRoomColumnView.PSD_X,
                LobbyRoomColumnView.PSD_Y + LobbyRoomColumnView.JOIN_TOP,
                LobbyRoomColumnView.PSD_WIDTH,
                LobbyRoomColumnView.JOIN_HEIGHT,
                () -> openGameplay(
                        com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_JOIN_ROOM));

        addLobbyTapAction(
                root,
                "个人中心",
                24,
                12,
                120,
                106,
                () ->
                        openGameplay(
                                com.nanbeiyule.game.MainActivity
                                        .ACTION_OPEN_ZHEJIANG_PERSONAL_CENTER));
        addLobbyTapAction(
                root,
                "复制玩家序号",
                330,
                55,
                79,
                51,
                this::copyLobbyPlayerId);

        FriendPanelView friendPanel = new FriendPanelView(this);
        FriendApiClient friendApi = new FriendApiClient(
                BuildConfig.AUTH_BASE_URL, friendAccessToken, this::runOnUiThread);
        friendPanelController = new FriendPanelController(
                friendAccessToken,
                friendApi,
                friendPanel,
                this::handleLobbyUnauthorized);
        friendPanel.attach(friendPanelController);
        root.addStretchedBox(friendPanel, 0, 0, 1920, 1080);

        OriginalLobbyTapEffectView tapEffectView =
                new OriginalLobbyTapEffectView(this);
        root.setTapEffectView(tapEffectView);
        root.addView(
                tapEffectView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);
        initializeLobbyMail();
        if (lobbyAudioController != null) {
            lobbyAudioController.setLobbyActive(true);
        }
        loadLobbyAccountSnapshot(root);
        startLobbyIconAnimations();
    }

    private void loadLobbyAccountSnapshot(LobbyRoot root) {
        if (lobbyHomeApiClient != null) {
            lobbyHomeApiClient.shutdown();
        }
        String accessToken = authRuntime == null
                ? ""
                : authRuntime.sessionValue("accessToken");
        lobbyHomeApiClient = new GameHomeApiClient(BuildConfig.AUTH_BASE_URL);
        lobbyHomeApiClient.loadHome(
                accessToken,
                new GameHomeApiClient.Callback() {
                    @Override
                    public void onSuccess(GameHomeState state) {
                        if (activeLobbyRoot != root || isFinishing()) {
                            return;
                        }
                        if (lobbyHeaderView != null) {
                            root.removeView(lobbyHeaderView);
                        }
                        lobbyUnauthorizedRefreshAttempted = false;
                        lobbyPublicPlayerId = Long.toString(state.player().publicPlayerId());
                        maybeOfferWechatSubscription(state.player().userId());
                        if (lobbyAnnouncementView != null) {
                            lobbyAnnouncementView.setAnnouncements(
                                    state.announcements().stream()
                                            .map(GameHomeState.Announcement::content)
                                            .toList());
                        }
                        lobbyHeaderView = new ZhejiangLobbyHeaderView(
                                MainActivity.this,
                                state,
                                R.drawable.lobby_top_controls,
                                BuildConfig.AUTH_BASE_URL,
                                accessToken);
                        root.addStretchedBoxAt(1, lobbyHeaderView, 0, 0, 1920, 130);
                    }

                    @Override
                    public void onUnauthorized() {
                        if (activeLobbyRoot == root && !isFinishing()) {
                            handleLobbyUnauthorized();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (activeLobbyRoot == root && !isFinishing()) {
                            toast(message);
                        }
                    }
                });
    }

    private void maybeOfferWechatSubscription(String userIdentity) {
        if (!wechatSubscriptionOfferPolicy.onHomeLoaded()
                || isFinishing()
                || wechatSubscriptionOfferStore == null
                || !wechatSubscriptionOfferStore.markIfFirst(userIdentity)) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("开启微信提醒")
                .setMessage("一次授权仅用于发送一条提醒")
                .setNegativeButton("暂不开启", null)
                .setPositiveButton(
                        "开启微信提醒",
                        (dialog, which) -> {
                            if (wechatSubscriptionController != null
                                    && authRuntime != null) {
                                wechatSubscriptionController.start(
                                        authRuntime.accessToken());
                            }
                        })
                .show();
    }

    private void closeLobbyHomeRequest() {
        if (lobbyAudioController != null) {
            lobbyAudioController.setLobbyActive(false);
        }
        if (lobbyHomeApiClient != null) {
            lobbyHomeApiClient.shutdown();
            lobbyHomeApiClient = null;
        }
        activeLobbyRoot = null;
        lobbyHeaderView = null;
        lobbyAnnouncementView = null;
    }

    private void openTaizhouGoldGame() {
        openGameplay(TaizhouGoldEntryModel.ACTION);
    }

    private void copyLobbyPlayerId() {
        if (lobbyPublicPlayerId.isEmpty()) {
            return;
        }
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        clipboard.setPrimaryClip(
                ClipData.newPlainText("玩家序号", lobbyPublicPlayerId));
        toast("复制成功!");
    }

    private void openZhejiangShop() {
        openGameplay(com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_SHOP);
    }

    private void openGameplay(String action) {
        if (authRuntime == null) {
            toast("请先完成登录");
            return;
        }
        String refreshToken = authRuntime.sessionValue("refreshToken");
        if (refreshToken.isEmpty()) {
            toast("登录状态已失效，请重新登录");
            return;
        }

        Intent intent = new Intent(this, com.nanbeiyule.game.MainActivity.class);
        intent.setAction(action);
        startActivityForResult(intent, REQUEST_GAMEPLAY);
    }

    private void refreshLobbySession() {
        if (authRuntime == null || restoringSession || isFinishing()) {
            return;
        }
        restoringSession = true;
        authRuntime.dispatch("restore", "", "", nowSeconds());
    }

    private void handleLobbyUnauthorized() {
        if (activeLobbyRoot == null || isFinishing() || restoringSession) {
            return;
        }
        if (!lobbyUnauthorizedRefreshAttempted) {
            lobbyUnauthorizedRefreshAttempted = true;
            refreshLobbySession();
            return;
        }
        authSuccessShown = false;
        if (authRuntime != null) {
            authRuntime.dispatch("logout", "", "", nowSeconds());
        }
        closeAuthenticatedLobby();
        showLoginPage();
    }

    private void closeAuthenticatedLobby() {
        stopLobbyIconAnimations();
        if (friendPanelController != null) {
            friendPanelController.close();
            friendPanelController = null;
        }
        if (lobbyRecordController != null) {
            lobbyRecordController.close();
            lobbyRecordController = null;
        }
        closeLobbyMail();
        closeLobbyHomeRequest();
    }

    private void addZhejiangLobbyBottomBar(LobbyRoot root) {
        lobbyMoreMenuVisible = LobbyBottomBarModel.DEFAULT_MORE_MENU_VISIBLE;
        lobbyMorePopupViews.clear();

        LobbyBottomBarModel.Rect bar = LobbyBottomBarModel.barBounds();
        View background = new View(this);
        background.setBackgroundResource(R.drawable.zhejiang_lobby_bottom_bg);
        root.addStretchedBox(background, bar.x, bar.y, bar.width, bar.height);

        for (int index = 1; index < LobbyBottomBarModel.items().size(); index++) {
            ImageView divider = new ImageView(this);
            divider.setImageResource(R.drawable.zhejiang_lobby_bottom_divider);
            divider.setScaleType(ImageView.ScaleType.FIT_XY);
            root.addStretchedBox(divider, 90 + index * 160, 972, 1, 42);
        }
        int animationLayerIndex = root.getChildCount();

        ImageView store = new ImageView(this);
        store.setImageResource(R.drawable.zhejiang_lobby_store);
        store.setScaleType(ImageView.ScaleType.FIT_CENTER);
        store.setContentDescription("商城");
        store.setClickable(true);
        store.setFocusable(true);
        store.setOnClickListener(ignored -> handleZhejiangLobbyItem(
                LobbyBottomBarModel.items().get(0)));
        root.addStretchedBox(store, 20, 840, 259, 196);

        for (int index = 1; index < LobbyBottomBarModel.items().size(); index++) {
            LobbyBottomBarModel.Item item = LobbyBottomBarModel.items().get(index);
            LobbyBottomBarModel.Rect bounds = LobbyBottomBarModel.itemBounds().get(index);
            FittedTextView title = new FittedTextView(this, 40f / 95f);
            title.setText(item.title);
            title.setTextColor(0xFFE8C8B5);
            title.setGravity(Gravity.CENTER);
            title.setIncludeFontPadding(false);
            title.setSingleLine(true);
            title.setTypeface(zhejiangLobbyTypeface());
            root.addStretchedBox(title, bounds.x, bounds.y, bounds.width, bounds.height);
        }

        ImageView quickStart = addZhejiangLobbyQuickStart(root);

        OriginalLobbyEffectView effects =
                OriginalLobbyEffectView.createZhejiangBottomControls(
                        this,
                        () -> {
                            store.setVisibility(View.INVISIBLE);
                            quickStart.setVisibility(View.INVISIBLE);
                        });
        root.addStretchedBoxAt(
                animationLayerIndex, effects, 0, 0, 1920, 1080);

        addZhejiangLobbyMoreMenu(root);

        List<LobbyBottomBarModel.Rect> itemBounds = LobbyBottomBarModel.itemBounds();
        for (int index = 0; index < LobbyBottomBarModel.items().size(); index++) {
            LobbyBottomBarModel.Item item = LobbyBottomBarModel.items().get(index);
            LobbyBottomBarModel.Rect bounds = itemBounds.get(index);
            View hitArea = new View(this);
            hitArea.setContentDescription(item.title);
            hitArea.setClickable(true);
            hitArea.setFocusable(true);
            hitArea.setOnClickListener(ignored -> handleZhejiangLobbyItem(item));
            root.addStretchedBox(hitArea, bounds.x, bounds.y, bounds.width, bounds.height);
        }

        lobbyMailAttentionView = new ImageView(this);
        lobbyMailAttentionView.setBackgroundResource(
                com.nanbeiyule.game.R.drawable.img_mail_red);
        lobbyMailAttentionView.setVisibility(View.GONE);
        root.addStretchedBox(lobbyMailAttentionView, 1176, 926, 42, 42);

        root.setOutsideTapDismissRegion(
                230,
                784,
                1140,
                256,
                () -> {
                    if (lobbyMoreMenuVisible) {
                        setZhejiangLobbyMoreMenuVisible(false);
                    }
                });
    }

    private ImageView addZhejiangLobbyQuickStart(BoxRoot root) {
        LobbyBottomBarModel.Rect bounds = LobbyBottomBarModel.quickStartBounds();

        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.zhejiang_lobby_quick_start);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        root.addStretchedBox(image, bounds.x, bounds.y, bounds.width, bounds.height);

        FittedTextView subtitle = new FittedTextView(this, 30f / 35f);
        subtitle.setText(LobbyBottomBarModel.quickStartSubtitle());
        subtitle.setTextColor(0xFF7E412B);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setIncludeFontPadding(false);
        subtitle.setSingleLine(true);
        subtitle.setTypeface(zhejiangLobbyTypeface());
        root.addStretchedBox(subtitle, bounds.x, bounds.y + 123, bounds.width, 35);

        View hitArea = new View(this);
        hitArea.setContentDescription("快速开始，" + LobbyBottomBarModel.quickStartSubtitle());
        hitArea.setClickable(true);
        hitArea.setFocusable(true);
        hitArea.setOnClickListener(ignored -> {
            setZhejiangLobbyMoreMenuVisible(false);
            openTaizhouGoldGame();
        });
        root.addStretchedBox(hitArea, bounds.x, bounds.y, bounds.width, bounds.height);
        return image;
    }

    private void addZhejiangLobbyMoreMenu(BoxRoot root) {
        java.util.List<LobbyMoreMenuModel.Item> items = LobbyMoreMenuModel.items();
        int[] icons = {
                R.drawable.zhejiang_lobby_more_score,
                R.drawable.zhejiang_lobby_more_public,
                R.drawable.zhejiang_lobby_more_news,
                R.drawable.zhejiang_lobby_more_bind_phone,
                R.drawable.zhejiang_lobby_more_settings,
                R.drawable.zhejiang_lobby_more_rules,
                R.drawable.zhejiang_lobby_more_health,
                R.drawable.zhejiang_lobby_more_notice
        };

        View popupBackground = new View(this);
        popupBackground.setBackgroundResource(R.drawable.zhejiang_lobby_more_bg);
        addZhejiangLobbyMoreView(root, popupBackground, 230, 784, 1120, 150);

        for (int index = 0; index < items.size(); index++) {
            int x = 230 + index * 140;
            ImageView icon = new ImageView(this);
            icon.setImageResource(icons[index]);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            addZhejiangLobbyMoreView(root, icon, x, 790, 140, 130);

            View hitArea = new View(this);
            LobbyMoreMenuModel.Item item = items.get(index);
            hitArea.setContentDescription(item.title());
            hitArea.setClickable(true);
            hitArea.setFocusable(true);
            hitArea.setOnClickListener(ignored -> {
                setZhejiangLobbyMoreMenuVisible(false);
                handleZhejiangLobbyMoreItem(item.destination());
            });
            addZhejiangLobbyMoreView(root, hitArea, x, 784, 140, 150);
        }
    }

    private void handleZhejiangLobbyMoreItem(LobbyMoreMenuModel.Destination destination) {
        switch (destination) {
            case SETTINGS -> showZhejiangLobbySettings();
            case SCORING_ASSISTANT -> openGameplay(
                    com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_SCORING_ASSISTANT);
            case WECHAT_PUBLIC -> openGameplay(
                    com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_WECHAT_PUBLIC);
            case ZHEJIANG_NEWS -> openGameplay(
                    com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_NEWS);
            case PHONE_BINDING -> openGameplay(
                    com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_PHONE_BINDING);
            case RULES -> openGameplay(
                    com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_RULES);
            case HEALTH_NOTICE -> openGameplay(
                    com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_HEALTH_NOTICE);
            case ANNOUNCEMENTS -> openGameplay(
                    com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_ANNOUNCEMENTS);
        }
    }

    private void addZhejiangLobbyMoreView(
            BoxRoot root,
            View view,
            int x,
            int y,
            int width,
            int height
    ) {
        view.setVisibility(View.GONE);
        lobbyMorePopupViews.add(view);
        root.addStretchedBox(view, x, y, width, height);
    }

    private void handleZhejiangLobbyItem(LobbyBottomBarModel.Item item) {
        LobbyBottomBarModel.Destination destination =
                LobbyBottomBarModel.destination(item);
        if (destination == LobbyBottomBarModel.Destination.SHOP) {
            setZhejiangLobbyMoreMenuVisible(false);
            openZhejiangShop();
            return;
        }
        if (destination == LobbyBottomBarModel.Destination.SHOP_DECORATION) {
            setZhejiangLobbyMoreMenuVisible(false);
            openGameplay(
                    com.nanbeiyule.game.MainActivity
                            .ACTION_OPEN_ZHEJIANG_DECORATION_SHOP);
            return;
        }
        if (destination == LobbyBottomBarModel.Destination.RECORDS) {
            setZhejiangLobbyMoreMenuVisible(false);
            showZhejiangLobbyRecords();
            return;
        }
        if (destination == LobbyBottomBarModel.Destination.ACTIVITIES) {
            setZhejiangLobbyMoreMenuVisible(false);
            openGameplay(com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_ACTIVITIES);
            return;
        }
        if (destination == LobbyBottomBarModel.Destination.SHARE) {
            setZhejiangLobbyMoreMenuVisible(false);
            openGameplay(com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_SHARE);
            return;
        }
        if (destination == LobbyBottomBarModel.Destination.BAG) {
            setZhejiangLobbyMoreMenuVisible(false);
            openGameplay(com.nanbeiyule.game.MainActivity.ACTION_OPEN_ZHEJIANG_BAG);
            return;
        }
        if (destination == LobbyBottomBarModel.Destination.MAIL) {
            setZhejiangLobbyMoreMenuVisible(false);
            if (lobbyMailController != null) {
                lobbyMailController.show();
            }
            return;
        }
        if (destination == LobbyBottomBarModel.Destination.MORE) {
            setZhejiangLobbyMoreMenuVisible(
                    LobbyBottomBarModel.toggleMoreMenu(lobbyMoreMenuVisible));
            return;
        }
        setZhejiangLobbyMoreMenuVisible(false);
        toast(item.title);
    }

    private void initializeLobbyMail() {
        lobbyMailController =
                new MailFeatureController(
                        this,
                        BuildConfig.AUTH_BASE_URL,
                        () -> authRuntime == null ? "" : authRuntime.sessionValue("accessToken"),
                        new MailFeatureController.Listener() {
                            @Override public void onAttentionChanged(boolean visible) {
                                if (lobbyMailAttentionView != null) {
                                    lobbyMailAttentionView.setVisibility(
                                            visible ? View.VISIBLE : View.GONE);
                                }
                            }

                            @Override public void onUnauthorized() {
                                handleLobbyUnauthorized();
                            }

                            @Override public void onMessage(String message) {
                                toast(message);
                            }
                        },
                        lobbyAudioController == null
                                ? null
                                : lobbyAudioController::playButtonClick);
        lobbyMailController.refreshAttention();
    }

    private void closeLobbyMail() {
        if (lobbyMailController != null) {
            lobbyMailController.close();
            lobbyMailController = null;
        }
        lobbyMailAttentionView = null;
    }

    private void showZhejiangLobbyRecords() {
        if (lobbyRecordController == null) {
            lobbyRecordController = new ZhejiangLobbyRecordController(
                    this,
                    () -> authRuntime == null
                            ? "" : authRuntime.sessionValue("accessToken"),
                    () -> openGameplay(
                            com.nanbeiyule.game.MainActivity
                                    .ACTION_OPEN_ZHEJIANG_PERSONAL_CENTER),
                    this::handleLobbyUnauthorized,
                    lobbyAudioController == null
                            ? null : lobbyAudioController::playButtonClick,
                    this::hideSystemUi);
        }
        lobbyRecordController.show();
    }

    private void setZhejiangLobbyMoreMenuVisible(boolean visible) {
        lobbyMoreMenuVisible = visible;
        for (View view : lobbyMorePopupViews) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void showZhejiangLobbySettings() {
        if (isFinishing() || lobbySettingsDialog != null) {
            return;
        }
        ZhejiangLobbySettingsDialog dialog =
                new ZhejiangLobbySettingsDialog(
                        this,
                        lobbySystemSettings,
                        new ZhejiangLobbySettingsDialog.Actions() {
                            @Override
                            public void onSettingsChanged(
                                    PersonalCenterSystemSettings settings) {
                                lobbySystemSettings = settings;
                                lobbySettingsStore.save(settings);
                                if (lobbyAudioController != null) {
                                    lobbyAudioController.applySettings(settings);
                                }
                            }

                            @Override
                            public void onLegalLinkRequested(
                                    ZhejiangLobbySettingsDialog.LegalLink link) {
                                switch (link) {
                                    case USER_SERVICE ->
                                            openLegalDocument(
                                                    agreementConfig.userServiceUrl());
                                    case PRIVACY ->
                                            openLegalDocument(
                                                    agreementConfig.privacyPolicyUrl());
                                    case QUALIFICATION,
                                            PERSONAL_INFORMATION,
                                            THIRD_PARTY_SHARING ->
                                            toast("该合规页面尚未配置");
                                }
                            }
                        });
        lobbySettingsDialog = dialog;
        if (lobbyAudioController != null) {
            dialog.setButtonClickSound(lobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (lobbySettingsDialog == dialog) {
                        lobbySettingsDialog = null;
                    }
                    hideSystemUi();
                });
        dialog.show();
    }

    private void addLobbyImage(BoxRoot root, int resId, int psdX, int psdY, int psdW, int psdH) {
        ImageView image = new ImageView(this);
        image.setImageResource(resId);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        root.addStretchedBox(
                image,
                LobbyLayoutModel.x(psdX),
                psdY,
                LobbyLayoutModel.width(psdW),
                psdH
        );
    }

    private void addLobbyBackground(LobbyRoot root) {
        LobbyBackgroundView background = new LobbyBackgroundView(this);
        root.addView(
                background,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void addLobbyActivityCards(BoxRoot root) {
        Bitmap source = BitmapFactory.decodeResource(
                getResources(), R.drawable.lobby_activity_cards);
        for (LobbyActivityCardsModel.CardSpec spec : LobbyActivityCardsModel.specs()) {
            ImageView image = new ImageView(this);
            image.setImageBitmap(Bitmap.createBitmap(
                    source,
                    spec.sourceX(),
                    spec.sourceY(),
                    spec.sourceWidth(),
                    spec.sourceHeight()));
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            root.addStretchedBox(
                    image,
                    spec.x(),
                    spec.y(),
                    spec.width(),
                    spec.height());
        }
    }

    private void addLobbyMotionIcon(BoxRoot root, LobbyIconMotionModel.Spec spec) {
        ImageView image = new ImageView(this);
        image.setImageResource(spec.drawableResId());
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        image.setClickable(false);
        image.setFocusable(false);
        lobbyMotionViews.add(image);
        root.addStretchedBox(
                image,
                LobbyLayoutModel.x(spec.psdX()),
                spec.psdY(),
                LobbyLayoutModel.width(spec.psdWidth()),
                spec.psdHeight());
    }

    private void startLobbyIconAnimations() {
        stopLobbyIconAnimations();
        if (!activityResumed || lobbyMotionViews.isEmpty()) {
            return;
        }

        LobbyIconMotionModel.Spec[] specs = LobbyIconMotionModel.specs();
        ImageView anchor = lobbyMotionViews.get(0);
        lobbyMotionStarter = new Runnable() {
            @Override
            public void run() {
                if (!activityResumed || lobbyMotionStarter != this) {
                    return;
                }
                for (ImageView view : lobbyMotionViews) {
                    if (view.getWidth() == 0 || view.getHeight() == 0) {
                        anchor.postOnAnimation(this);
                        return;
                    }
                }

                ValueAnimator animator = ValueAnimator.ofInt(
                        0, (int) LobbyIconMotionModel.sequenceDurationMillis());
                animator.setDuration(LobbyIconMotionModel.sequenceDurationMillis());
                animator.setInterpolator(new LinearInterpolator());
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.addUpdateListener(animation -> applyLobbyIconFrames(
                        specs, ((Integer) animation.getAnimatedValue()).longValue()));
                lobbyMotionAnimator = animator;
                applyLobbyIconFrames(specs, 0L);
                animator.start();
            }
        };
        anchor.post(lobbyMotionStarter);
    }

    private void applyLobbyIconFrames(LobbyIconMotionModel.Spec[] specs, long elapsedMillis) {
        for (int i = 0; i < lobbyMotionViews.size(); i++) {
            ImageView view = lobbyMotionViews.get(i);
            LobbyIconMotionModel.Spec spec = specs[i];
            LobbyIconMotionModel.Frame frame = LobbyIconMotionModel.frameAt(spec, elapsedMillis);
            view.setTranslationX(
                    view.getWidth() * frame.translationXPsdPixels() / spec.psdWidth());
            view.setTranslationY(
                    view.getHeight() * frame.translationYPsdPixels() / spec.psdHeight());
            view.setScaleX(frame.scaleX());
            view.setScaleY(frame.scaleY());
        }
    }

    private void stopLobbyIconAnimations() {
        if (lobbyMotionStarter != null && !lobbyMotionViews.isEmpty()) {
            lobbyMotionViews.get(0).removeCallbacks(lobbyMotionStarter);
        }
        lobbyMotionStarter = null;
        if (lobbyMotionAnimator != null) {
            lobbyMotionAnimator.cancel();
            lobbyMotionAnimator = null;
        }
        for (ImageView view : lobbyMotionViews) {
            view.setTranslationX(0f);
            view.setTranslationY(0f);
            view.setScaleX(1f);
            view.setScaleY(1f);
            view.setRotation(0f);
        }
    }

    private void addLobbyTap(
            BoxRoot root,
            String contentDescription,
            int psdX,
            int psdY,
            int psdW,
            int psdH,
            String toastText
    ) {
        addLobbyTapAction(root, contentDescription, psdX, psdY, psdW, psdH, () -> toast(toastText));
    }

    private void addLobbyTapAction(
            BoxRoot root,
            String contentDescription,
            int psdX,
            int psdY,
            int psdW,
            int psdH,
            Runnable action
    ) {
        View hitArea = new View(this);
        hitArea.setContentDescription(contentDescription);
        hitArea.setClickable(true);
        hitArea.setFocusable(true);
        hitArea.setOnClickListener(ignored -> action.run());
        root.addStretchedBox(
                hitArea,
                LobbyLayoutModel.x(psdX),
                psdY,
                LobbyLayoutModel.width(psdW),
                psdH
        );
    }

    private void ensurePhoneLoginOverlay() {
        if (phoneLoginOverlay != null || loginRoot == null) {
            return;
        }
        phoneLoginOverlay = new BoxRoot(this);
        if (registrationMode) {
            buildRegistrationOverlay();
            return;
        }

        View dim = new View(this);
        dim.setBackgroundColor(Color.TRANSPARENT);
        dim.setOnClickListener(ignored -> closePhoneLogin());
        phoneLoginOverlay.addStretchedBox(dim, 0, 0, 1920, 1080);

        View frame = new View(this);
        frame.setBackground(nineSlice(R.drawable.xianyi_login_panel_frame, 22, 22));
        phoneLoginOverlay.addStretchedBox(frame, 500, 180, 920, 720);

        View panel = new View(this);
        panel.setContentDescription("手机登录面板");
        panel.setBackground(nineSlice(R.drawable.xianyi_login_panel_inner, 14, 14));
        panel.setClickable(true);
        phoneLoginOverlay.addStretchedBox(panel, 510, 190, 900, 700);

        View footer = new View(this);
        footer.setBackground(nineSlice(R.drawable.xianyi_login_panel_footer, 8, 8));
        phoneLoginOverlay.addStretchedBox(footer, 522, 650, 876, 228);

        View footerDivider = new View(this);
        footerDivider.setBackgroundResource(R.drawable.xianyi_login_tab_line);
        phoneLoginOverlay.addStretchedBox(footerDivider, 522, 650, 876, 2);

        phoneLoginVerifyTab = fittedLabel("验证码登录", 0.56f, 0xFFA83E10, true);
        phoneLoginVerifyTab.setTypeface(huaqueDisplayTypeface(), Typeface.NORMAL);
        phoneLoginVerifyTab.setClickable(true);
        phoneLoginVerifyTab.setFocusable(true);
        phoneLoginVerifyTab.setOnClickListener(ignored -> showPhoneVerificationMode());
        phoneLoginOverlay.addStretchedBox(phoneLoginVerifyTab, 600, 225, 320, 74);

        phoneLoginPasswordTab = fittedLabel("账号密码登录", 0.56f, 0xFF7A6B65, false);
        phoneLoginPasswordTab.setTypeface(huaqueDisplayTypeface(), Typeface.NORMAL);
        phoneLoginPasswordTab.setClickable(true);
        phoneLoginPasswordTab.setFocusable(true);
        phoneLoginPasswordTab.setOnClickListener(ignored -> showPhonePasswordMode());
        phoneLoginOverlay.addStretchedBox(phoneLoginPasswordTab, 1000, 225, 320, 74);

        phoneLoginVerifyLine = new View(this);
        phoneLoginVerifyLine.setBackgroundResource(R.drawable.xianyi_login_tab_line);
        phoneLoginOverlay.addStretchedBox(phoneLoginVerifyLine, 655, 302, 210, 5);

        phoneLoginPasswordLine = new View(this);
        phoneLoginPasswordLine.setBackgroundResource(R.drawable.xianyi_login_tab_line);
        phoneLoginOverlay.addStretchedBox(phoneLoginPasswordLine, 1055, 302, 210, 5);

        TextView phoneLabel = fittedLabel("手机号：", 0.52f, 0xFF7A4A2B, false);
        phoneLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        phoneLoginOverlay.addStretchedBox(phoneLabel, 590, 365, 170, 78);

        phoneInput = input("请输入...");
        phoneInput.setContentDescription("手机号输入框");
        phoneInput.setTypeface(huaqueDisplayTypeface(), Typeface.NORMAL);
        phoneInput.setTextColor(0xFF7A4A2B);
        phoneInput.setHintTextColor(0xFFB09A70);
        phoneInput.setPadding(dp(18), 0, dp(18), 0);
        phoneInput.setBackground(nineSlice(R.drawable.xianyi_login_input, 12, 12));
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        phoneLoginOverlay.addStretchedBox(phoneInput, 780, 365, 540, 78);

        phoneLoginSecondLabel = fittedLabel("验证码：", 0.52f, 0xFF7A4A2B, false);
        phoneLoginSecondLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        phoneLoginOverlay.addStretchedBox(phoneLoginSecondLabel, 590, 500, 170, 78);

        codeInput = input("请输入...");
        codeInput.setContentDescription("验证码输入框");
        codeInput.setTypeface(huaqueDisplayTypeface(), Typeface.NORMAL);
        codeInput.setTextColor(0xFF7A4A2B);
        codeInput.setHintTextColor(0xFFB09A70);
        codeInput.setPadding(dp(18), 0, dp(18), 0);
        codeInput.setBackground(nineSlice(R.drawable.xianyi_login_input, 12, 12));
        codeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        phoneLoginOverlay.addStretchedBox(codeInput, 780, 500, 330, 78);

        passwordInput = input("请输入...");
        passwordInput.setContentDescription("密码输入框");
        passwordInput.setTypeface(huaqueDisplayTypeface(), Typeface.NORMAL);
        passwordInput.setTextColor(0xFF7A4A2B);
        passwordInput.setHintTextColor(0xFFB09A70);
        passwordInput.setPadding(dp(18), 0, dp(18), 0);
        passwordInput.setBackground(nineSlice(R.drawable.xianyi_login_input, 12, 12));
        passwordInput.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
        phoneLoginOverlay.addStretchedBox(passwordInput, 780, 500, 540, 78);

        sendCodeButton = fittedActionButton(
                "获取验证码", 0.40f, 0xFF49C982, 0xFF159B59, Color.WHITE);
        sendCodeButton.setContentDescription("获取验证码");
        sendCodeButton.setBackgroundResource(R.drawable.xianyi_login_send_code_active);
        sendCodeButton.setElevation(0f);
        sendCodeButton.setText("");
        sendCodeButton.setSingleLine(true);
        sendCodeButton.setOnClickListener(ignored -> authRuntime.dispatch(
                "requestCode", phoneInput.getText().toString(), "", nowSeconds()));
        phoneLoginOverlay.addStretchedBox(sendCodeButton, 1130, 500, 190, 78);

        authStatusText = fittedLabel("", 0.68f, 0xFF356453, false);
        authStatusText.setContentDescription("登录状态提示");
        phoneLoginOverlay.addStretchedBox(authStatusText, 780, 590, 540, 46);

        phoneLoginRegisterLink = fittedLabel("注册账号", 0.52f, 0xFF20A83A, false);
        phoneLoginRegisterLink.setClickable(true);
        phoneLoginRegisterLink.setFocusable(true);
        phoneLoginRegisterLink.setOnClickListener(ignored -> showPhoneRegistrationMode());
        phoneLoginOverlay.addStretchedBox(phoneLoginRegisterLink, 730, 584, 210, 58);

        phoneLoginForgotLink = fittedLabel("忘记密码", 0.52f, 0xFFC84B3D, false);
        phoneLoginForgotLink.setClickable(true);
        phoneLoginForgotLink.setFocusable(true);
        phoneLoginForgotLink.setOnClickListener(ignored -> {
            showPhoneVerificationMode();
            authStatusText.setText("请使用验证码登录后重置密码");
        });
        phoneLoginOverlay.addStretchedBox(phoneLoginForgotLink, 1080, 584, 210, 58);

        loginButton = new ImageView(this);
        loginButton.setContentDescription("确定");
        loginButton.setBackground(nineSlice(R.drawable.xianyi_login_confirm, 30, 24));
        loginButton.setImageResource(R.drawable.xianyi_login_confirm_text);
        loginButton.setScaleType(ImageView.ScaleType.CENTER);
        loginButton.setElevation(0f);
        loginButton.setClickable(true);
        loginButton.setFocusable(true);
        loginButton.setOnClickListener(ignored -> authRuntime.dispatch(
                passwordLoginMode ? "submitPassword" : "submitCode",
                phoneInput.getText().toString(),
                passwordLoginMode
                        ? passwordInput.getText().toString()
                        : codeInput.getText().toString(),
                nowSeconds()));
        phoneLoginOverlay.addStretchedBox(loginButton, 840, 700, 240, 80);

        ImageView closeButton = new ImageView(this);
        closeButton.setContentDescription("关闭手机登录");
        closeButton.setImageResource(R.drawable.xianyi_login_close);
        closeButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        closeButton.setClickable(true);
        closeButton.setFocusable(true);
        closeButton.setOnClickListener(ignored -> closePhoneLogin());
        phoneLoginOverlay.addStretchedBox(closeButton, 1360, 155, 76, 76);

        loginRoot.addView(phoneLoginOverlay, new FrameLayout.LayoutParams(-1, -1));
        showPhoneVerificationMode();
    }

    private void buildRegistrationOverlay() {
        View dim = new View(this);
        dim.setBackgroundColor(Color.TRANSPARENT);
        dim.setOnClickListener(ignored -> closeRegistrationMode());
        phoneLoginOverlay.addStretchedBox(dim, 0, 0, 1920, 1080);

        View frame = new View(this);
        frame.setBackground(nineSlice(R.drawable.xianyi_login_panel_frame, 22, 22));
        phoneLoginOverlay.addStretchedBox(frame, 500, 180, 920, 720);

        View panel = new View(this);
        panel.setContentDescription("注册账号面板");
        panel.setBackground(nineSlice(R.drawable.xianyi_login_panel_inner, 14, 14));
        panel.setClickable(true);
        phoneLoginOverlay.addStretchedBox(panel, 510, 190, 900, 700);

        View footer = new View(this);
        footer.setBackground(nineSlice(R.drawable.xianyi_login_panel_footer, 8, 8));
        phoneLoginOverlay.addStretchedBox(footer, 522, 650, 876, 228);

        View footerDivider = new View(this);
        footerDivider.setBackgroundResource(R.drawable.xianyi_login_tab_line);
        phoneLoginOverlay.addStretchedBox(footerDivider, 522, 650, 876, 2);

        ImageView titleBackground = new ImageView(this);
        titleBackground.setImageResource(R.drawable.xianyi_register_title_bg);
        titleBackground.setScaleType(ImageView.ScaleType.FIT_XY);
        phoneLoginOverlay.addStretchedBox(titleBackground, 780, 215, 360, 60);

        ImageView title = new ImageView(this);
        title.setContentDescription("注册账号标题");
        title.setImageResource(R.drawable.xianyi_register_title);
        title.setScaleType(ImageView.ScaleType.CENTER);
        phoneLoginOverlay.addStretchedBox(title, 840, 222, 240, 48);

        TextView phoneLabel = fittedLabel("手机号：", 0.52f, 0xFF7A4A2B, false);
        phoneLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        phoneLoginOverlay.addStretchedBox(phoneLabel, 590, 365, 170, 78);

        phoneInput = input("请输入...");
        phoneInput.setContentDescription("注册手机号输入框");
        phoneInput.setTextColor(0xFF7A4A2B);
        phoneInput.setHintTextColor(0xFFB09A70);
        phoneInput.setBackground(nineSlice(R.drawable.xianyi_login_input, 12, 12));
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        phoneInput.setText(registrationPhone);
        phoneLoginOverlay.addStretchedBox(phoneInput, 780, 365, 540, 78);

        TextView codeLabel = fittedLabel("验证码：", 0.52f, 0xFF7A4A2B, false);
        codeLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        phoneLoginOverlay.addStretchedBox(codeLabel, 590, 500, 170, 78);

        codeInput = input("请输入...");
        codeInput.setContentDescription("注册验证码输入框");
        codeInput.setTextColor(0xFF7A4A2B);
        codeInput.setHintTextColor(0xFFB09A70);
        codeInput.setBackground(nineSlice(R.drawable.xianyi_login_input, 12, 12));
        codeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        phoneLoginOverlay.addStretchedBox(codeInput, 780, 500, 330, 78);

        sendCodeButton = fittedActionButton(
                "获取验证码", 0.40f, 0xFF49C982, 0xFF159B59, Color.WHITE);
        sendCodeButton.setContentDescription("注册获取验证码");
        sendCodeButton.setBackgroundResource(R.drawable.xianyi_login_send_code_active);
        sendCodeButton.setElevation(0f);
        sendCodeButton.setText("");
        sendCodeButton.setSingleLine(true);
        sendCodeButton.setOnClickListener(ignored -> authRuntime.dispatch(
                "requestCode", phoneInput.getText().toString(), "", nowSeconds()));
        phoneLoginOverlay.addStretchedBox(sendCodeButton, 1130, 500, 190, 78);

        authStatusText = fittedLabel("", 0.68f, 0xFF356453, false);
        authStatusText.setContentDescription("注册状态提示");
        phoneLoginOverlay.addStretchedBox(authStatusText, 780, 590, 540, 46);

        loginButton = new ImageView(this);
        loginButton.setContentDescription("注册确定");
        loginButton.setBackground(nineSlice(R.drawable.xianyi_login_confirm, 30, 24));
        loginButton.setImageResource(R.drawable.xianyi_login_confirm_text);
        loginButton.setScaleType(ImageView.ScaleType.CENTER);
        loginButton.setClickable(true);
        loginButton.setFocusable(true);
        loginButton.setOnClickListener(ignored -> authRuntime.dispatch(
                "submitRegister",
                phoneInput.getText().toString(),
                codeInput.getText().toString(),
                nowSeconds()));
        phoneLoginOverlay.addStretchedBox(loginButton, 840, 700, 240, 80);

        ImageView closeButton = new ImageView(this);
        closeButton.setContentDescription("关闭注册账号");
        closeButton.setImageResource(R.drawable.xianyi_login_close);
        closeButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        closeButton.setClickable(true);
        closeButton.setFocusable(true);
        closeButton.setOnClickListener(ignored -> closeRegistrationMode());
        phoneLoginOverlay.addStretchedBox(closeButton, 1360, 155, 76, 76);

        loginRoot.addView(phoneLoginOverlay, new FrameLayout.LayoutParams(-1, -1));
    }

    private void showPhoneRegistrationMode() {
        registrationPhone = phoneInput == null ? "" : phoneInput.getText().toString();
        removePhoneLoginOverlay();
        registrationMode = true;
        ensurePhoneLoginOverlay();
    }

    private void closeRegistrationMode() {
        registrationPhone = phoneInput == null ? registrationPhone : phoneInput.getText().toString();
        removePhoneLoginOverlay();
        registrationMode = false;
        ensurePhoneLoginOverlay();
        showPhonePasswordMode();
        phoneInput.setText(registrationPhone);
    }

    private void showPhoneVerificationMode() {
        passwordLoginMode = false;
        phoneLoginVerifyTab.setTextColor(0xFFA83E10);
        phoneLoginPasswordTab.setTextColor(0xFF7A6B65);
        phoneLoginVerifyLine.setVisibility(View.VISIBLE);
        phoneLoginPasswordLine.setVisibility(View.GONE);
        phoneLoginSecondLabel.setText("验证码：");
        codeInput.setVisibility(View.VISIBLE);
        sendCodeButton.setVisibility(View.VISIBLE);
        passwordInput.setVisibility(View.GONE);
        phoneLoginRegisterLink.setVisibility(View.GONE);
        phoneLoginForgotLink.setVisibility(View.GONE);
        authStatusText.setVisibility(View.VISIBLE);
    }

    private void showPhonePasswordMode() {
        passwordLoginMode = true;
        phoneLoginVerifyTab.setTextColor(0xFF7A6B65);
        phoneLoginPasswordTab.setTextColor(0xFFA83E10);
        phoneLoginVerifyLine.setVisibility(View.GONE);
        phoneLoginPasswordLine.setVisibility(View.VISIBLE);
        phoneLoginSecondLabel.setText("密码：");
        codeInput.setVisibility(View.GONE);
        sendCodeButton.setVisibility(View.GONE);
        passwordInput.setVisibility(View.VISIBLE);
        phoneLoginRegisterLink.setVisibility(View.VISIBLE);
        phoneLoginForgotLink.setVisibility(View.VISIBLE);
        authStatusText.setVisibility(View.GONE);
    }

    private void closePhoneLogin() {
        if (registrationMode) {
            closeRegistrationMode();
            return;
        }
        if (authRuntime != null) {
            authRuntime.dispatch("close", "", "", nowSeconds());
        } else {
            removePhoneLoginOverlay();
        }
    }

    private void showRegistrationResult(String message, boolean success) {
        if (registrationResultOverlay != null || loginRoot == null) {
            return;
        }
        registrationResultOverlay = new BoxRoot(this);

        View blocker = new View(this);
        blocker.setBackgroundColor(Color.TRANSPARENT);
        blocker.setClickable(true);
        registrationResultOverlay.addStretchedBox(blocker, 0, 0, 1920, 1080);

        View frame = new View(this);
        frame.setContentDescription("注册结果提示");
        frame.setBackground(nineSlice(R.drawable.xianyi_prompt_frame, 24, 24));
        registrationResultOverlay.addStretchedBox(frame, 520, 210, 880, 650);

        View inner = new View(this);
        inner.setBackground(nineSlice(R.drawable.xianyi_prompt_inner, 16, 16));
        registrationResultOverlay.addStretchedBox(inner, 535, 225, 850, 620);

        ImageView titleBackground = new ImageView(this);
        titleBackground.setImageResource(R.drawable.xianyi_prompt_title_bg);
        titleBackground.setScaleType(ImageView.ScaleType.FIT_XY);
        registrationResultOverlay.addStretchedBox(titleBackground, 535, 225, 850, 96);

        ImageView title = new ImageView(this);
        title.setImageResource(R.drawable.xianyi_prompt_title);
        title.setScaleType(ImageView.ScaleType.CENTER);
        registrationResultOverlay.addStretchedBox(title, 835, 246, 250, 54);

        TextView resultText = fittedLabel(message, 0.26f, 0xFF7A4A2B, false);
        resultText.setGravity(Gravity.CENTER);
        resultText.setMaxLines(3);
        registrationResultOverlay.addStretchedBox(resultText, 620, 350, 680, 245);

        View footer = new View(this);
        footer.setBackground(nineSlice(R.drawable.xianyi_login_panel_footer, 8, 8));
        registrationResultOverlay.addStretchedBox(footer, 535, 675, 850, 170);

        View footerDivider = new View(this);
        footerDivider.setBackgroundResource(R.drawable.xianyi_login_tab_line);
        registrationResultOverlay.addStretchedBox(footerDivider, 535, 675, 850, 2);

        ImageView confirm = new ImageView(this);
        confirm.setContentDescription("注册结果确定");
        confirm.setBackground(nineSlice(R.drawable.xianyi_login_confirm, 30, 24));
        confirm.setImageResource(R.drawable.xianyi_login_confirm_text);
        confirm.setScaleType(ImageView.ScaleType.CENTER);
        confirm.setClickable(true);
        confirm.setFocusable(true);
        confirm.setOnClickListener(ignored -> {
            removeRegistrationResultOverlay();
            if (success) {
                registrationMode = false;
                authRuntime.dispatch("close", "", "", nowSeconds());
                showLobbyPage();
                return;
            }
            closeRegistrationMode();
            showPhoneVerificationMode();
        });
        registrationResultOverlay.addStretchedBox(confirm, 840, 720, 240, 80);

        loginRoot.addView(registrationResultOverlay, new FrameLayout.LayoutParams(-1, -1));
    }

    private void removeRegistrationResultOverlay() {
        if (registrationResultOverlay != null && loginRoot != null) {
            loginRoot.removeView(registrationResultOverlay);
        }
        registrationResultOverlay = null;
    }

    private void removePhoneLoginOverlay() {
        removeLoginLoadingOverlay();
        if (authTick != null) {
            handler.removeCallbacks(authTick);
            authTick = null;
        }
        if (phoneLoginOverlay != null && loginRoot != null) {
            loginRoot.removeView(phoneLoginOverlay);
        }
        phoneLoginOverlay = null;
        phoneInput = null;
        codeInput = null;
        passwordInput = null;
        phoneLoginVerifyTab = null;
        phoneLoginPasswordTab = null;
        phoneLoginSecondLabel = null;
        phoneLoginRegisterLink = null;
        phoneLoginForgotLink = null;
        phoneLoginVerifyLine = null;
        phoneLoginPasswordLine = null;
        sendCodeButton = null;
        loginButton = null;
        authStatusText = null;
        passwordLoginMode = false;
    }

    private void updateLoginLoadingOverlay(boolean visible) {
        if (visible) {
            showLoginLoadingOverlay();
        } else {
            removeLoginLoadingOverlay();
        }
    }

    private void showLoginLoadingOverlay() {
        if (loginLoadingOverlay != null || loginRoot == null) {
            return;
        }

        loginLoadingOverlay = new BoxRoot(this);
        View dim = new View(this);
        dim.setBackgroundColor(Color.TRANSPARENT);
        dim.setClickable(true);
        dim.setFocusable(true);
        dim.setContentDescription(LoginLoadingModel.LOGIN_LABEL);
        loginLoadingOverlay.addBox(dim, 0, 0, 1920, 1080);

        XianyiLoadingView loadingView = new XianyiLoadingView(
                this,
                huaqueHeavyTypeface(),
                huaqueDisplayTypeface(),
                LoginLoadingModel.LOGIN_LABEL
        );
        loginLoadingOverlay.addBox(loadingView, 790, 386, 340, 340);
        loginRoot.addView(loginLoadingOverlay, new FrameLayout.LayoutParams(-1, -1));

        loginLoadingProgress = 0;
        loginLoadingTick = new Runnable() {
            @Override
            public void run() {
                if (loginLoadingOverlay == null) {
                    return;
                }
                loginLoadingProgress = LoginLoadingModel.nextProgress(loginLoadingProgress);
                loadingView.setPhase(loginLoadingProgress);
                handler.postDelayed(this, LoadingAnimationModel.TICK_MILLIS);
            }
        };
        handler.post(loginLoadingTick);
    }

    private void showAccountSwitchLoading(LobbyRoot lobby) {
        BoxRoot overlay = new BoxRoot(this);
        View guard = new View(this);
        guard.setBackgroundColor(Color.TRANSPARENT);
        guard.setClickable(true);
        guard.setFocusable(true);
        overlay.addBox(guard, 0, 0, 1920, 1080);

        XianyiLoadingView loadingView = new XianyiLoadingView(
                this,
                huaqueHeavyTypeface(),
                huaqueDisplayTypeface(),
                ""
        );
        loadingView.setContentDescription("切换账号加载圆环");
        overlay.addBox(loadingView, 790, 386, 340, 340);
        lobby.addView(overlay, new FrameLayout.LayoutParams(-1, -1));

        handler.postDelayed(this::showLoginPage, ACCOUNT_SWITCH_LOADING_MILLIS);
    }

    private void removeLoginLoadingOverlay() {
        if (loginLoadingTick != null) {
            handler.removeCallbacks(loginLoadingTick);
            loginLoadingTick = null;
        }
        if (loginLoadingOverlay != null && loginRoot != null) {
            loginRoot.removeView(loginLoadingOverlay);
        }
        loginLoadingOverlay = null;
        loginLoadingProgress = 0;
    }

    private void scheduleAuthTick() {
        if (authTick != null) {
            handler.removeCallbacks(authTick);
        }
        authTick = () -> {
            authTick = null;
            if (authRuntime != null && phoneLoginOverlay != null) {
                authRuntime.dispatch("tick", "", "", nowSeconds());
                scheduleAuthTick();
            }
        };
        handler.postDelayed(authTick, 1000);
    }

    private static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    private BoxRoot baseRoot(int bgResId, int overlayColor, ImageView.ScaleType scaleType) {
        BoxRoot root = new BoxRoot(this);
        root.setBackgroundColor(Color.BLACK);

        ImageView bg = new ImageView(this);
        bg.setImageResource(bgResId);
        bg.setScaleType(scaleType);
        root.addView(bg, new FrameLayout.LayoutParams(-1, -1));

        View overlay = new View(this);
        overlay.setBackgroundColor(overlayColor);
        root.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        return root;
    }

    private TextView fittedLabel(
            String text, float textHeightRatio, int color, boolean bold) {
        TextView view = new FittedTextView(this, textHeightRatio);
        view.setText(text);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        view.setTypeface(
                bold ? huaqueHeavyTypeface() : huaqueDisplayTypeface(),
                Typeface.NORMAL);
        return view;
    }

    private TextView label(String text, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        view.setTypeface(bold ? huaqueHeavyTypeface() : huaqueDisplayTypeface(), Typeface.NORMAL);
        return view;
    }

    private EditText input(String hint) {
        EditText editText = new FittedEditText(this, 0.44f);
        editText.setHint(hint);
        editText.setSingleLine(true);
        editText.setImeOptions(
                editText.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editText.setTextColor(0xFF064A3D);
        editText.setHintTextColor(0x88064A3D);
        editText.setTypeface(huaqueDisplayTypeface(), Typeface.NORMAL);
        editText.setPadding(dp(22), 0, dp(22), 0);
        editText.setBackground(roundedStroke(0xCCFFFFFF, dp(22), 0x5548C7C2, dp(2)));
        return editText;
    }

    private TextView fittedActionButton(
            String text,
            float textHeightRatio,
            int startColor,
            int endColor,
            int textColor) {
        TextView button = fittedLabel(text, textHeightRatio, textColor, true);
        button.setBackground(roundedGradient(startColor, endColor, dp(28), 0));
        button.setElevation(dp(8));
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private TextView actionButton(String text, int startColor, int endColor, int textColor) {
        TextView button = label(text, 26, textColor, true);
        button.setBackground(roundedGradient(startColor, endColor, dp(28), 0));
        button.setElevation(dp(8));
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private View space(int heightDp) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(heightDp)));
        return view;
    }

    private GradientDrawable roundedStroke(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            drawable.setStroke(strokeWidth, strokeColor);
        }
        return drawable;
    }

    private GradientDrawable roundedGradient(int startColor, int endColor, int radius, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{startColor, endColor}
        );
        drawable.setCornerRadius(radius);
        if (strokeColor != 0) {
            drawable.setStroke(dp(2), strokeColor);
        }
        return drawable;
    }

    private Drawable nineSlice(int resId, int sliceX, int sliceY) {
        return new NineSliceDrawable(
                BitmapFactory.decodeResource(getResources(), resId), sliceX, sliceY);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private Typeface huaqueDisplayTypeface() {
        if (huaqueDisplayTypeface == null) {
            huaqueDisplayTypeface = loadTypeface(ZIHUN_JINGDIAN_LIHEI_ASSET, Typeface.DEFAULT);
        }
        return huaqueDisplayTypeface;
    }

    private Typeface huaqueHeavyTypeface() {
        if (huaqueHeavyTypeface == null) {
            huaqueHeavyTypeface = loadTypeface(ZIHUN_JINGDIAN_LIHEI_ASSET, Typeface.DEFAULT_BOLD);
        }
        return huaqueHeavyTypeface;
    }

    private Typeface zhejiangLobbyTypeface() {
        if (zhejiangLobbyTypeface == null) {
            zhejiangLobbyTypeface = loadTypeface(ZHEJIANG_CUYUAN_ASSET, Typeface.DEFAULT_BOLD);
        }
        return zhejiangLobbyTypeface;
    }

    private Typeface loadTypeface(String assetPath, Typeface fallback) {
        try {
            return Typeface.createFromAsset(getAssets(), assetPath);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class NineSliceDrawable extends Drawable {
        private final Bitmap bitmap;
        private final int sliceX;
        private final int sliceY;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Rect src = new Rect();
        private final Rect dst = new Rect();

        NineSliceDrawable(Bitmap bitmap, int sliceX, int sliceY) {
            this.bitmap = bitmap;
            this.sliceX = Math.min(sliceX, bitmap.getWidth() / 2);
            this.sliceY = Math.min(sliceY, bitmap.getHeight() / 2);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            int dstSliceX = Math.min(sliceX, bounds.width() / 2);
            int dstSliceY = Math.min(sliceY, bounds.height() / 2);
            int sourceRight = bitmap.getWidth() - sliceX;
            int sourceBottom = bitmap.getHeight() - sliceY;
            int targetRight = bounds.right - dstSliceX;
            int targetBottom = bounds.bottom - dstSliceY;

            drawPatch(canvas, 0, 0, sliceX, sliceY,
                    bounds.left, bounds.top, bounds.left + dstSliceX, bounds.top + dstSliceY);
            drawPatch(canvas, sliceX, 0, sourceRight, sliceY,
                    bounds.left + dstSliceX, bounds.top, targetRight, bounds.top + dstSliceY);
            drawPatch(canvas, sourceRight, 0, bitmap.getWidth(), sliceY,
                    targetRight, bounds.top, bounds.right, bounds.top + dstSliceY);
            drawPatch(canvas, 0, sliceY, sliceX, sourceBottom,
                    bounds.left, bounds.top + dstSliceY, bounds.left + dstSliceX, targetBottom);
            drawPatch(canvas, sliceX, sliceY, sourceRight, sourceBottom,
                    bounds.left + dstSliceX, bounds.top + dstSliceY, targetRight, targetBottom);
            drawPatch(canvas, sourceRight, sliceY, bitmap.getWidth(), sourceBottom,
                    targetRight, bounds.top + dstSliceY, bounds.right, targetBottom);
            drawPatch(canvas, 0, sourceBottom, sliceX, bitmap.getHeight(),
                    bounds.left, targetBottom, bounds.left + dstSliceX, bounds.bottom);
            drawPatch(canvas, sliceX, sourceBottom, sourceRight, bitmap.getHeight(),
                    bounds.left + dstSliceX, targetBottom, targetRight, bounds.bottom);
            drawPatch(canvas, sourceRight, sourceBottom, bitmap.getWidth(), bitmap.getHeight(),
                    targetRight, targetBottom, bounds.right, bounds.bottom);
        }

        private void drawPatch(
                Canvas canvas,
                int sourceLeft,
                int sourceTop,
                int sourceRight,
                int sourceBottom,
                int targetLeft,
                int targetTop,
                int targetRight,
                int targetBottom) {
            if (sourceRight <= sourceLeft || sourceBottom <= sourceTop
                    || targetRight <= targetLeft || targetBottom <= targetTop) {
                return;
            }
            src.set(sourceLeft, sourceTop, sourceRight, sourceBottom);
            dst.set(targetLeft, targetTop, targetRight, targetBottom);
            canvas.drawBitmap(bitmap, src, dst, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private static final class FittedTextView extends TextView {
        private final float textHeightRatio;

        FittedTextView(Activity activity, float textHeightRatio) {
            super(activity);
            this.textHeightRatio = textHeightRatio;
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            super.onSizeChanged(width, height, oldWidth, oldHeight);
            setTextSize(TypedValue.COMPLEX_UNIT_PX, height * textHeightRatio);
        }
    }

    private static final class FittedEditText extends EditText {
        private final float textHeightRatio;

        FittedEditText(Activity activity, float textHeightRatio) {
            super(activity);
            this.textHeightRatio = textHeightRatio;
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            super.onSizeChanged(width, height, oldWidth, oldHeight);
            setTextSize(TypedValue.COMPLEX_UNIT_PX, height * textHeightRatio);
        }
    }

    private static class LoadingProgressView extends View {
        private final Bitmap background;
        private final Bitmap fill;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Rect src = new Rect();
        private final Rect dst = new Rect();
        private int progress;

        LoadingProgressView(Activity activity) {
            super(activity);
            background = BitmapFactory.decodeResource(activity.getResources(), R.drawable.huaque_progress_bg);
            fill = BitmapFactory.decodeResource(activity.getResources(), R.drawable.huaque_progress_fill);
        }

        void setProgress(int value) {
            progress = Math.max(0, Math.min(100, value));
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }

            dst.set(0, 0, width, height);
            canvas.drawBitmap(background, null, dst, paint);

            int insetX = Math.max(4, Math.round(width * 5f / 344f));
            int insetY = Math.max(4, Math.round(height * 5f / 23f));
            int innerWidth = width - insetX * 2;
            int innerHeight = height - insetY * 2;
            if (innerWidth <= 0 || innerHeight <= 0 || progress <= 0) {
                return;
            }

            int drawWidth = Math.round(innerWidth * progress / 100f);
            int srcWidth = Math.max(1, Math.round(fill.getWidth() * progress / 100f));
            src.set(0, 0, srcWidth, fill.getHeight());
            dst.set(insetX, insetY, insetX + drawWidth, insetY + innerHeight);
            canvas.drawBitmap(fill, src, dst, paint);
        }
    }

    private static class XianyiLoadingView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final RectF bounds = new RectF();
        private final Bitmap[] ringFrames;
        private final Typeface heavyTypeface;
        private final Typeface displayTypeface;
        private final String loadingLabel;
        private final long animationStartedAt;
        private float phase;

        XianyiLoadingView(Activity activity, Typeface heavyTypeface, Typeface displayTypeface) {
            this(activity, heavyTypeface, displayTypeface, "加载中");
        }

        XianyiLoadingView(
                Activity activity,
                Typeface heavyTypeface,
                Typeface displayTypeface,
                String loadingLabel
        ) {
            super(activity);
            this.heavyTypeface = heavyTypeface;
            this.displayTypeface = displayTypeface;
            this.loadingLabel = loadingLabel;
            Bitmap atlas = BitmapFactory.decodeResource(activity.getResources(), R.drawable.xianyi_jiazaiya);
            ringFrames = new Bitmap[]{
                    extract(atlas, 248, 586, 253, 282, 65, 53, false),
                    extract(atlas, 2, 297, 248, 284, 70, 54, false),
                    extract(atlas, 2, 583, 244, 285, 74, 57, false),
                    extract(atlas, 1003, 603, 262, 265, 54, 71, false),
                    extract(atlas, 766, 37, 273, 262, 42, 73, true),
                    extract(atlas, 746, 592, 276, 255, 31, 77, true),
                    extract(atlas, 503, 590, 278, 241, 29, 90, true),
                    extract(atlas, 999, 321, 269, 250, 33, 83, true),
                    extract(atlas, 1474, 6, 259, 258, 35, 73, false),
                    extract(atlas, 252, 303, 242, 281, 40, 48, false),
                    extract(atlas, 496, 304, 242, 280, 40, 48, false),
                    extract(atlas, 1030, 54, 252, 265, 38, 54, false),
                    extract(atlas, 1251, 339, 262, 258, 37, 58, true),
                    extract(atlas, 504, 26, 276, 260, 36, 60, true),
                    extract(atlas, 2, 14, 281, 255, 38, 56, true),
                    extract(atlas, 259, 21, 280, 243, 43, 55, true),
                    extract(atlas, 740, 312, 276, 257, 47, 55, true),
                    extract(atlas, 1267, 606, 252, 262, 61, 72, false)
            };
            animationStartedAt = android.os.SystemClock.uptimeMillis();
        }

        void setPhase(float value) {
            phase = value;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth();
            float height = getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }

            float size = Math.min(width, height);
            float cx = width / 2f;
            float cy = height / 2f;
            long elapsed = android.os.SystemClock.uptimeMillis() - animationStartedAt;
            Bitmap ring = ringFrames[(int) ((elapsed / 100L) % ringFrames.length)];
            float ringWidth = size * 1.02f;
            float ringHeight = ringWidth * 366f / 352f;
            bounds.set(
                    cx - ringWidth / 2f,
                    cy - ringHeight / 2f,
                    cx + ringWidth / 2f,
                    cy + ringHeight / 2f
            );
            paint.setAlpha(255);
            paint.clearShadowLayer();
            canvas.drawBitmap(ring, null, bounds, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(heavyTypeface);
            paint.setTextSize(size * 0.35f);
            paint.setColor(0xFFFFF0B0);
            paint.setShadowLayer(size * 0.034f, 0f, 0f, 0xFFFFA643);
            Paint.FontMetrics glyphMetrics = paint.getFontMetrics();
            float glyphBaseline = LoadingAnimationModel.centeredTextBaseline(
                    cy - size * 0.055f,
                    glyphMetrics.ascent,
                    glyphMetrics.descent
            );
            canvas.drawText(LoadingAnimationModel.centerGlyph(phase), cx, glyphBaseline, paint);

            if (!loadingLabel.isEmpty()) {
                paint.setTypeface(displayTypeface);
                paint.setTextSize(size * 0.102f);
                paint.setColor(0xFFFBE8FF);
                paint.setShadowLayer(size * 0.035f, 0f, 0f, 0xFFB275FF);
                Paint.FontMetrics loadingMetrics = paint.getFontMetrics();
                float loadingBaseline = LoadingAnimationModel.centeredTextBaseline(
                        cy + size * 0.285f,
                        loadingMetrics.ascent,
                        loadingMetrics.descent
                );
                canvas.drawText(loadingLabel, cx, loadingBaseline, paint);
            }

            paint.clearShadowLayer();
            paint.setAlpha(255);
            postInvalidateDelayed(Math.max(1L, 100L - elapsed % 100L));
        }

        private static Bitmap extract(
                Bitmap atlas,
                int x,
                int y,
                int width,
                int height,
                int offsetX,
                int offsetY,
                boolean rotated
        ) {
            int packedWidth = rotated ? height : width;
            int packedHeight = rotated ? width : height;
            Bitmap bitmap = Bitmap.createBitmap(atlas, x, y, packedWidth, packedHeight);
            if (rotated) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90f);
                bitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        matrix,
                        true
                );
            }
            return normalize(bitmap, 352, 366, offsetX, offsetY);
        }

        private static Bitmap normalize(
                Bitmap bitmap,
                int originalWidth,
                int originalHeight,
                int offsetX,
                int offsetY
        ) {
            Bitmap normalized = Bitmap.createBitmap(
                    originalWidth,
                    originalHeight,
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(normalized);
            canvas.drawBitmap(
                    bitmap,
                    offsetX,
                    originalHeight - offsetY - bitmap.getHeight(),
                    null
            );
            return normalized;
        }
    }

    public static class BoxRoot extends FrameLayout {
        private static final float BASE_W = 1920f;
        private static final float BASE_H = 1080f;

        public BoxRoot(Activity activity) {
            super(activity);
        }

        public void addBox(View view, int x, int y, int w, int h) {
            addView(view, new BoxParams(x, y, w, h));
        }

        public void addStretchedBox(View view, int x, int y, int w, int h) {
            addView(view, new StretchedBoxParams(x, y, w, h));
        }

        public void addStretchedBoxAt(int index, View view, int x, int y, int w, int h) {
            addView(view, index, new StretchedBoxParams(x, y, w, h));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            float scale = Math.min(width / BASE_W, height / BASE_H);
            int offsetX = Math.round((width - BASE_W * scale) / 2f);
            int offsetY = Math.round((height - BASE_H * scale) / 2f);

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                if (params instanceof StretchedBoxParams) {
                    StretchedBoxParams box = (StretchedBoxParams) params;
                    int[] mapped = mapStretchedBox(
                            width, height, box.baseX, box.baseY, box.baseW, box.baseH);
                    box.width = mapped[2];
                    box.height = mapped[3];
                    box.leftMargin = mapped[0];
                    box.topMargin = mapped[1];
                } else if (params instanceof BoxParams) {
                    BoxParams box = (BoxParams) params;
                    box.width = Math.round(box.baseW * scale);
                    box.height = Math.round(box.baseH * scale);
                    box.leftMargin = offsetX + Math.round(box.baseX * scale);
                    box.topMargin = offsetY + Math.round(box.baseY * scale);
                }
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        protected int[] mapStretchedBox(
                int screenWidth,
                int screenHeight,
                int x,
                int y,
                int width,
                int height) {
            return StretchedBoxModel.map(
                    screenWidth, screenHeight, x, y, width, height);
        }
    }

    private static final class LobbyRoot extends BoxRoot {
        private RectF outsideTapDismissRegion;
        private Runnable outsideTapAction;
        private Runnable buttonClickSound = () -> {};
        private OriginalLobbyTapEffectView tapEffectView;
        private View pressedClickable;

        LobbyRoot(Activity activity) {
            super(activity);
        }

        void setButtonClickSound(Runnable buttonClickSound) {
            this.buttonClickSound = buttonClickSound == null ? () -> {} : buttonClickSound;
        }

        void setTapEffectView(OriginalLobbyTapEffectView tapEffectView) {
            this.tapEffectView = tapEffectView;
        }

        @Override
        protected int[] mapStretchedBox(
                int screenWidth,
                int screenHeight,
                int x,
                int y,
                int width,
                int height) {
            return LobbyViewportModel.map(
                    screenWidth, screenHeight, x, y, width, height);
        }

        void setOutsideTapDismissRegion(
                int x,
                int y,
                int width,
                int height,
                Runnable action
        ) {
            outsideTapDismissRegion = new RectF(x, y, x + width, y + height);
            outsideTapAction = action;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN -> {
                    pressedClickable = clickableAt(this, event.getX(), event.getY());
                    if (tapEffectView != null) {
                        tapEffectView.playAt(event.getX(), event.getY());
                    }
                    if (outsideTapDismissRegion != null && outsideTapAction != null) {
                        float baseX = LobbyViewportModel.unmapX(
                                getWidth(), getHeight(), event.getX());
                        float baseY = LobbyViewportModel.unmapY(
                                getWidth(), getHeight(), event.getY());
                        if (!outsideTapDismissRegion.contains(baseX, baseY)) {
                            outsideTapAction.run();
                        }
                    }
                }
                case MotionEvent.ACTION_UP -> {
                    View released = clickableAt(this, event.getX(), event.getY());
                    if (pressedClickable != null && pressedClickable == released) {
                        buttonClickSound.run();
                    }
                    pressedClickable = null;
                }
                case MotionEvent.ACTION_CANCEL -> pressedClickable = null;
                default -> {}
            }
            return super.dispatchTouchEvent(event);
        }

        private static View clickableAt(View view, float x, float y) {
            if (view.getVisibility() != View.VISIBLE
                    || view.getAlpha() <= 0.0f
                    || x < 0.0f
                    || y < 0.0f
                    || x >= view.getWidth()
                    || y >= view.getHeight()) {
                return null;
            }
            if (view instanceof ViewGroup group) {
                for (int index = group.getChildCount() - 1; index >= 0; index--) {
                    View child = group.getChildAt(index);
                    float[] point = {x - child.getLeft(), y - child.getTop()};
                    Matrix inverse = new Matrix();
                    if (!child.getMatrix().invert(inverse)) {
                        continue;
                    }
                    inverse.mapPoints(point);
                    View candidate = clickableAt(child, point[0], point[1]);
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
            return view.isClickable() && view.isEnabled() ? view : null;
        }
    }

    private static class BoxParams extends FrameLayout.LayoutParams {
        final int baseX;
        final int baseY;
        final int baseW;
        final int baseH;

        BoxParams(int x, int y, int w, int h) {
            super(w, h);
            baseX = x;
            baseY = y;
            baseW = w;
            baseH = h;
        }
    }

    private static class StretchedBoxParams extends FrameLayout.LayoutParams {
        final int baseX;
        final int baseY;
        final int baseW;
        final int baseH;

        StretchedBoxParams(int x, int y, int w, int h) {
            super(w, h);
            baseX = x;
            baseY = y;
            baseW = w;
            baseH = h;
        }
    }
}
