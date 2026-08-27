package com.nanbeiyule.game;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.util.function.Consumer;

abstract class MainActivityGameHomeDisplayFlow extends MainActivityGameHomeLoadFlow {
    private final MainActivityTaizhouMahjongFlow taizhouMahjongFlow = new MainActivityTaizhouMahjongFlow(this);
    private final MainActivityWuLongFlow wuLongFlow = new MainActivityWuLongFlow(this);
    private final MainActivityGameRecordFlow gameRecordFlow =
            new MainActivityGameRecordFlow(this);

    protected void displayGameHome(GameHomeState state) {
        taizhouMahjongFlow.close();
        wuLongFlow.close();
        returnToHomeAfterRegionSelection = false;
        GameHomeView homeView = new GameHomeView(this, state, false);
        if (originalLobbyAudioController != null) {
            homeView.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        currentHomeState = state;
        currentHomeView = homeView;
        currentAvatarBitmap =
                avatarImageLoader == null
                        ? AvatarFrameRenderer.loadDefaultAvatar(getResources())
                        : avatarImageLoader.defaultAvatar();
        homeView.setAvatarBitmap(currentAvatarBitmap);
        homeView.setOnHomeActionListener(
                new GameHomeView.OnHomeActionListener() {
                    @Override
                    public void onPersonalCenterRequested() {
                        showPersonalCenter();
                    }

                    @Override
                    public void onMembershipCenterRequested() {
                        showMembershipCenter();
                    }

                    @Override
                    public void onShopRequested() {
                        showShop();
                    }

                    @Override public void onShopRequested(ShopCategory initialCategory) { showShop(initialCategory); }
                    @Override public void onBagRequested() { showShopInventory(); }
                    @Override public void onActivityCenterRequested() {
                        showLobbyFeature(MainActivityDestination.ACTIVITY_CENTER);
                    }
                    @Override public void onShareRequested() {
                        showLobbyFeature(MainActivityDestination.SHARE);
                    }
                    @Override
                    public void onDailyMissionRequested() {
                        showDailyMission();
                    }

                    @Override
                    public void onMailRequested() {
                        showMail();
                    }

                    @Override
                    public void onGameRecordsRequested() {
                        gameRecordFlow.show();
                    }

                    @Override
                    public void onMoreRequested() {
                        showMoreMenu();
                    }

                    @Override public void onSettingsRequested() { showPersonalCenterSettings(); }
                    @Override
                    public void onChangeRegionRequested() {
                        returnToHomeAfterRegionSelection = true;
                        showChooseAreaPage();
                    }

                    @Override
                    public void onLogoutRequested() {
                        authSessionCoordinator.clearSession();
                        returnToHomeAfterRegionSelection = false;
                        showLoginPage();
                    }

                    @Override
                    public void onEntryRequested(GameHomeState.Entry entry) {
                        onGameHomeEntryRequested(entry);
                    }

                    @Override
                    public void onUnavailableFeatureRequested(
                            String featureName) {
                        Toast.makeText(
                                        MainActivityGameHomeDisplayFlow.this,
                                        getString(
                                                R.string.game_home_feature_unavailable,
                                                featureName),
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override public void onLobbyStatusRequested(String status) { showLobbyStatus(status); }

                    @Override
                    public void onRetryRequested() {
                        loadGameHome();
                    }
                });
        homeView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        GameHomeSceneLayout scene = new GameHomeSceneLayout(this, homeView);
        scene.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        loadingView = scene;
        setContentView(scene);
        if (originalLobbyAudioController != null) {
            originalLobbyAudioController.setLobbyActive(true);
        }
        if (loginRequestLoadingController != null) {
            loginRequestLoadingController.bind(scene.loadingView());
        }
        loadAvatarForHome(state, homeView);
        joinPendingRoomAfterHomeDisplay();
    }

    @Override
    protected void handleRoomInvitationIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String roomNumber = RoomInvitationLink.parse(intent.getDataString());
        if (roomNumber == null) {
            return;
        }
        pendingJoinRoomNumber = roomNumber;
        if (currentHomeView != null && currentHomeView.isAttachedToWindow()) {
            joinPendingRoomAfterHomeDisplay();
        }
    }

    protected void joinPendingRoomAfterHomeDisplay() {}

    protected void onGameHomeEntryRequested(GameHomeState.Entry entry) {
        Toast.makeText(
                        MainActivityGameHomeDisplayFlow.this,
                        getString(
                                R.string.game_home_feature_unavailable,
                                entry.displayName()),
                        Toast.LENGTH_SHORT)
                .show();
    }

    protected void openTaizhouMahjongSession(String roomNumber) { taizhouMahjongFlow.open(roomNumber); }

    /** 返场（原版断线重连）进桌，进桌后要按 onReqPlayerPlace 判断这一局是否已经结束。 */
    protected void reenterTaizhouMahjongSession(String roomNumber) {
        taizhouMahjongFlow.open(roomNumber, true);
    }

    /** 结算后「重新匹配队友」，由接了金币场流程的子类覆写。 */
    void rematchGoldRoom(long gameId) {}
    protected void openWuLongSession(String roomNumber) { wuLongFlow.open(roomNumber); }

    @Override
    protected void onRecordAudioPermissionResult(boolean granted) {
        taizhouMahjongFlow.onRecordAudioPermissionResult(granted);
    }

    @Override
    protected void onStart() {
        super.onStart();
        wuLongFlow.onHostResumed();
    }

    @Override
    protected void onStop() {
        wuLongFlow.onHostPaused();
        super.onStop();
    }

    /** Overridden by the friend flow once its authenticated drawer is available. */
    protected void showTableFriendDrawer(ViewGroup parent) {}
    protected void hideTableFriendDrawer() {}

    @Override
    protected boolean handleBackRequest() {
        if (goldChooseRoomFlow != null && goldChooseRoomFlow.handleBack()) {
            return true;
        }
        return taizhouMahjongFlow.handleBack() || wuLongFlow.handleBack();
    }

    @Override
    protected void onDestroy() {
        taizhouMahjongFlow.close();
        wuLongFlow.close();
        gameRecordFlow.close();
        super.onDestroy();
    }

    @Override
    protected void showMoreMenu() {
        if (isFinishing() || currentHomeView == null || moreMenuDialog != null) {
            return;
        }
        MoreMenuDialog dialog =
                new MoreMenuDialog(
                        this,
                        item -> {
                            MoreMenuDialog showing = moreMenuDialog;
                            if (showing != null) {
                                showing.dismiss();
                            }
                            onMoreMenuItemSelected(item);
                        });
        moreMenuDialog = dialog;
        if (originalLobbyAudioController != null) {
            dialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        dialog.setOnDismissListener(
                ignored -> {
                    if (moreMenuDialog == dialog) {
                        moreMenuDialog = null;
                    }
                    applyImmersiveMode();
                });
        dialog.show();
    }

    private void onMoreMenuItemSelected(MoreMenuItem item) {
        if (item == null) return;
        ZhejiangLobbyAction.Route route = ZhejiangLobbyAction.more(item);
        if (route.destination() == ZhejiangLobbyAction.Destination.SETTINGS) {
            showPersonalCenterSettings();
        } else if (route.destination() == ZhejiangLobbyAction.Destination.RULES) {
            showLobbyFeature(MainActivityDestination.RULES);
        } else if (route.destination() == ZhejiangLobbyAction.Destination.SCORING_ASSISTANT) {
            showLobbyFeature(MainActivityDestination.SCORING_ASSISTANT);
        } else if (route.destination() == ZhejiangLobbyAction.Destination.ANNOUNCEMENTS) {
            showLobbyFeature(MainActivityDestination.ANNOUNCEMENTS);
        } else {
            showLobbyStatus(route.unavailableMessage());
        }
    }

    private void showLobbyStatus(String status) {
        if (!isFinishing() && status != null && !status.isBlank()) {
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        }
    }

    protected void loadAvatarForHome(GameHomeState state, GameHomeView homeView) {
        if (avatarImageLoader == null || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        avatarImageLoader.load(
                                state.player().avatarKey(),
                                accessToken,
                                new AvatarImageLoader.Callback() {
                                    @Override
                                    public void onBitmap(Bitmap bitmap) {
                                        callback.onSuccess(bitmap);
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
                GameHomeAvatarBinding.callback(this, homeView));
    }

    protected abstract void loadAvatarBitmap(String avatarKey, Consumer<Bitmap> onBitmap);
    protected abstract void showLobbyFeature(MainActivityDestination destination);
}
