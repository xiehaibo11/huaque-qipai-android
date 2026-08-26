package com.nanbeiyule.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

/** Hosts the recovered Taizhou gold-room flow inside the South-North client. */
public final class MainActivity extends MainActivityLobbyFeatureFlow {
    public static final String ACTION_OPEN_TAIZHOU_GOLD = "OPEN_TAIZHOU_GOLD";
    public static final String ACTION_OPEN_ZHEJIANG_CREATE_ROOM = "OPEN_ZHEJIANG_CREATE_ROOM";
    public static final String ACTION_OPEN_ZHEJIANG_JOIN_ROOM = "OPEN_ZHEJIANG_JOIN_ROOM";
    public static final String ACTION_OPEN_ZHEJIANG_SHOP = "OPEN_ZHEJIANG_SHOP";
    public static final String ACTION_OPEN_ZHEJIANG_DECORATION_SHOP =
            "OPEN_ZHEJIANG_DECORATION_SHOP";
    public static final String ACTION_OPEN_ZHEJIANG_PERSONAL_CENTER =
            "OPEN_ZHEJIANG_PERSONAL_CENTER";
    public static final String ACTION_OPEN_ZHEJIANG_ACTIVITIES = "OPEN_ZHEJIANG_ACTIVITIES";
    public static final String ACTION_OPEN_ZHEJIANG_SHARE = "OPEN_ZHEJIANG_SHARE";
    public static final String ACTION_OPEN_ZHEJIANG_BAG = "OPEN_ZHEJIANG_BAG";
    public static final String ACTION_OPEN_ZHEJIANG_SCORING_ASSISTANT =
            "OPEN_ZHEJIANG_SCORING_ASSISTANT";
    public static final String ACTION_OPEN_ZHEJIANG_WECHAT_PUBLIC =
            "OPEN_ZHEJIANG_WECHAT_PUBLIC";
    public static final String ACTION_OPEN_ZHEJIANG_NEWS = "OPEN_ZHEJIANG_NEWS";
    public static final String ACTION_OPEN_ZHEJIANG_PHONE_BINDING =
            "OPEN_ZHEJIANG_PHONE_BINDING";
    public static final String ACTION_OPEN_ZHEJIANG_RULES = "OPEN_ZHEJIANG_RULES";
    public static final String ACTION_OPEN_ZHEJIANG_HEALTH_NOTICE =
            "OPEN_ZHEJIANG_HEALTH_NOTICE";
    public static final String ACTION_OPEN_ZHEJIANG_ANNOUNCEMENTS =
            "OPEN_ZHEJIANG_ANNOUNCEMENTS";
    public static final int RESULT_ACCOUNT_SWITCH_REQUESTED = Activity.RESULT_FIRST_USER;

    private MainActivityEntryMode entryMode = MainActivityEntryMode.STANDARD;
    private boolean directEntryOpened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        entryMode = MainActivityEntryMode.fromAction(intent.getAction());
        if (entryMode.isDirect()) {
            new FirstLaunchAgreementStore(this).accept();
            new RegionSelectionStore(this).setSelectedLobbyId(900023L);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void startPostAgreementStartup() {
        if (!entryMode.isDirect()) {
            super.startPostAgreementStartup();
            return;
        }
        if (postAgreementStartupStarted || isFinishing()) {
            return;
        }
        postAgreementStartupStarted = true;
        if (entryMode.loadsGameHomeBeforeDestination()) {
            showGameHomePage();
            return;
        }
        directEntryOpened = true;
        getWindow().getDecorView().post(
                () -> {
                    if (!isFinishing()) {
                            showShop(entryMode.initialShopCategory());
                    }
                });
    }

    @Override
    protected View createStartupView() {
        if (!entryMode.isDirect()) {
            return super.createStartupView();
        }
        FrameLayout transparentHost = new FrameLayout(this);
        transparentHost.setBackgroundColor(Color.TRANSPARENT);
        transparentHost.setContentDescription(
                entryMode.opensShop()
                        ? "正在进入商城"
                        : entryMode.opensPersonalCenter()
                                ? "正在打开个人中心"
                                : entryMode.destination() == MainActivityDestination.JOIN_ROOM
                                        ? "正在打开加入房间"
                                        : "正在进入台州麻将");
        transparentHost.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        return transparentHost;
    }

    @Override
    protected void displayGameHome(GameHomeState state) {
        if (entryMode.isDirect() && directEntryOpened) {
            return;
        }
        if (entryMode.rendersGameHomeBeforeDestination()) {
            super.displayGameHome(state);
            return;
        }
        if (isFinishing()) {
            return;
        }
        MainActivityDestination destination = entryMode.destination();
        if (destination == MainActivityDestination.CREATE_ROOM
                || destination == MainActivityDestination.JOIN_ROOM) {
            directEntryOpened = true;
            currentHomeState = state;
            getWindow().getDecorView().post(
                    () -> {
                        if (!isFinishing()) {
                            if (destination == MainActivityDestination.CREATE_ROOM) {
                                openDirectCreateRoom();
                            } else {
                                openDirectJoinRoom();
                            }
                        }
                    });
            return;
        }
        if (destination == MainActivityDestination.PERSONAL_CENTER
                || destination == MainActivityDestination.PHONE_BINDING) {
            directEntryOpened = true;
            currentHomeState = state;
            currentHomeView = new GameHomeView(this, state, false);
            currentAvatarBitmap =
                    avatarImageLoader == null
                            ? AvatarFrameRenderer.loadDefaultAvatar(getResources())
                            : avatarImageLoader.defaultAvatar();
            getWindow().getDecorView().post(
                    () -> {
                        if (!isFinishing()) {
                            if (destination == MainActivityDestination.PHONE_BINDING) {
                                showPhoneBinding();
                            } else {
                                showPersonalCenter();
                            }
                            loadAvatarBitmap(
                                    state.player().avatarKey(),
                                    bitmap -> {
                                        currentAvatarBitmap = bitmap;
                                        if (personalCenterDialog != null) {
                                            personalCenterDialog.setAvatarBitmap(bitmap);
                                        }
                                    });
                        }
                    });
            return;
        }
        directEntryOpened = true;
        currentHomeState = state;
        getWindow().getDecorView().post(
                () -> {
                    if (isFinishing() || currentHomeState == null) {
                        return;
                    }
                    if (destination == MainActivityDestination.GOLD_ROOM) {
                        if (goldChooseRoomFlow == null) {
                            goldChooseRoomFlow = new MainActivityGoldChooseRoomFlow(this);
                        }
                        goldChooseRoomFlow.show(30400L);
                    } else {
                        showLobbyFeature(destination);
                    }
                });
    }

    @Override
    protected void showLoginPage() {
        if (!entryMode.isDirect()) {
            super.showLoginPage();
            return;
        }
        Toast.makeText(this, "登录状态已失效，请重新登录", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void switchAccount() {
        if (!entryMode.returnsAccountSwitchToLauncher()) {
            super.switchAccount();
            return;
        }
        dismissPersonalCenter();
        returnToHomeAfterRegionSelection = false;
        setResult(RESULT_ACCOUNT_SWITCH_REQUESTED);
        finish();
    }

    @Override
    protected void showGameHomeStatus(int messageResource, boolean retryEnabled) {
        if (!entryMode.isDirect()) {
            super.showGameHomeStatus(messageResource, retryEnabled);
            return;
        }
        finish();
    }

    @Override
    protected boolean handleBackRequest() {
        if (entryMode.destination() == MainActivityDestination.CREATE_ROOM && createRoomFlow != null) {
            createRoomFlow.dismiss();
            finish();
            return true;
        }
        if (entryMode.destination() == MainActivityDestination.JOIN_ROOM && joinRoomFlow != null) {
            joinRoomFlow.dismiss();
            finish();
            return true;
        }
        if (entryMode.opensGoldRoom()
                && goldChooseRoomFlow != null
                && goldChooseRoomFlow.handleBack()) {
            finish();
            return true;
        }
        return super.handleBackRequest();
    }

    boolean isDirectGoldEntry() {
        return entryMode.opensGoldRoom();
    }

    private void openDirectCreateRoom() {
        withVerifiedRealName(
                () -> {
                    if (createRoomFlow == null) {
                        createRoomFlow = new MainActivityCreateRoomFlow(this);
                    }
                    createRoomFlow.routeByPlacement(
                            () -> createRoomFlow.show(
                                    currentHomeState.region().lobbyId(), 0L, this::finish));
                });
    }

    private void openDirectJoinRoom() {
        withVerifiedRealName(() -> openJoinRoomEntry(this::finish));
    }

    @Override
    protected void onShopDismissed() {
        if (entryMode.opensShop() && directEntryOpened && !isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onShopInventoryDismissed() {
        if (entryMode.destination() == MainActivityDestination.BAG
                && directEntryOpened
                && !isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onPersonalCenterDismissed() {
        if ((entryMode.opensPersonalCenter()
                        || entryMode.destination() == MainActivityDestination.PHONE_BINDING)
                && directEntryOpened
                && !isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onActivityCenterDismissed() {
        if (entryMode.destination() == MainActivityDestination.ACTIVITY_CENTER
                && directEntryOpened
                && !isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onLobbyFeatureDismissed(MainActivityDestination destination) {
        if (entryMode.destination() == destination && directEntryOpened && !isFinishing()) {
            finish();
        }
    }

    @Override
    protected void checkRealNameAfterHomeLoad() {
        if (!entryMode.isDirect()) {
            super.checkRealNameAfterHomeLoad();
        }
    }

    @Override
    protected void loadFriendsAfterHomeLoad() {
        if (!entryMode.isDirect()) {
            super.loadFriendsAfterHomeLoad();
        }
    }

}
