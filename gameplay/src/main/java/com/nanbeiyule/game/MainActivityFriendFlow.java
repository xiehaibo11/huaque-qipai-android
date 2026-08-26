package com.nanbeiyule.game;

import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Friend drawer flow: assembles the drawer into the game-home scene and
 * preloads friend pages and the application badge after a fresh home
 * load. Avatars live in {@link MainActivityFriendActionsFlow}, the
 * notification popup and friend dialogs in
 * {@link MainActivityFriendDialogFlow}.
 */
abstract class MainActivityFriendFlow
        extends MainActivityGameHomeDisplayFlow {
    @FunctionalInterface
    interface FriendCall<T> {
        void execute(
                String accessToken,
                AuthSessionCoordinator.CallCallback<T> callback);
    }

    /** Callback adapter with shared login-required and error handling. */
    abstract class FriendResult<T>
            implements AuthSessionCoordinator.Callback<T> {
        @Override
        public void onLoginRequired() {
            requireFriendLogin();
        }

        @Override
        public void onError(String message) {
            toast(message);
        }
    }

    @Override
    protected void displayGameHome(GameHomeState state) {
        super.displayGameHome(state);
        attachFriendDrawer();
    }

    @Override
    protected void showTableFriendDrawer(ViewGroup parent) {
        attachFriendDrawer(parent);
        if (friendDrawerView != null) {
            friendDrawerView.expand();
        }
    }

    @Override
    protected void hideTableFriendDrawer() {
        if (friendDrawerView != null) {
            friendDrawerView.hideDrawer();
        }
    }

    @Override
    protected void loadFriendsAfterHomeLoad() {
        if (isFinishing()
                || friendApiClient == null
                || friendDrawerView == null) {
            return;
        }
        if (friendDrawerStore != null
                && friendDrawerStore.shouldAutoExpandToday(
                        friendUserId(), System.currentTimeMillis())) {
            friendDrawerView.expand();
        } else {
            reloadFriendPages();
        }
        refreshFriendApplications();
        loadUnreadFriendNotifications();
    }

    @Override
    protected void showLoginPage() {
        dismissFriendDialogs();
        if (friendDrawerView != null) {
            friendDrawerView.hideDrawer();
        }
        super.showLoginPage();
    }

    protected void reloadFriendPages() {
        if (friendDrawerView == null) {
            return;
        }
        int loaded = friendDrawerView.loadedCount();
        int pages =
                Math.max(
                        1,
                        (loaded + FriendDrawerState.PAGE_SIZE - 1)
                                / FriendDrawerState.PAGE_SIZE);
        friendDrawerView.beginInitialLoad();
        loadFriendPage(0, pages * FriendDrawerState.PAGE_SIZE);
    }

    protected void loadFriendPage(int page, int size) {
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.listFriends(
                                accessToken, page, size,
                                forwardingFriend(callback)),
                new FriendResult<FriendListPage>() {
                    @Override
                    public void onSuccess(FriendListPage result) {
                        if (friendDrawerView != null) {
                            friendDrawerView.applyPage(result);
                            loadFriendAvatars();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (friendDrawerView != null) {
                            friendDrawerView.loadFailed();
                        }
                    }
                });
    }

    protected void refreshFriendApplications() {
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.applications(
                                accessToken, forwardingFriend(callback)),
                new FriendResult<FriendApplicationsPage>() {
                    @Override
                    public void onSuccess(FriendApplicationsPage page) {
                        if (friendDrawerView != null) {
                            friendDrawerView.setUnreadApplications(
                                    page.total());
                        }
                        onFriendApplicationsLoaded(page);
                    }

                    @Override
                    public void onError(String message) {
                        // The badge preload stays silent on failure.
                    }
                });
    }

    /** Hook for the dialog layer to mirror fresh application data. */
    protected void onFriendApplicationsLoaded(
            FriendApplicationsPage page) {}

    private void attachFriendDrawer() {
        if (!(loadingView instanceof GameHomeSceneLayout scene)) {
            return;
        }
        attachFriendDrawer(scene);
    }

    private void attachFriendDrawer(ViewGroup scene) {
        if (friendDrawerView == null) {
            friendDrawerView = new FriendDrawerView(this);
            friendDrawerView.setListener(new DrawerListener());
        }
        friendDrawerView.setInviteCooldownChecker(
                publicPlayerId ->
                        friendDrawerStore != null
                                && friendDrawerStore.inviteCoolingDown(
                                        friendUserId(),
                                        publicPlayerId,
                                        System.currentTimeMillis()));
        if (friendDrawerView.getParent() instanceof ViewGroup parent) {
            parent.removeView(friendDrawerView);
        }
        friendDrawerView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        scene.addView(friendDrawerView);
        // 牌友抽屉是大厅既有功能，保持原版收起态挂载；每日自动展开逻辑
        // 仍由 loadFriendsAfterHomeLoad() 按原有持久化规则处理。
        friendDrawerView.showDrawer();
    }

    protected <T> void executeFriend(
            FriendCall<T> call,
            AuthSessionCoordinator.Callback<T> callback) {
        if (friendApiClient == null || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(call::execute, callback);
    }

    protected static <T> FriendApiTransport.ResultCallback<T> forwardingFriend(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new FriendApiTransport.ResultCallback<>() {
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

    protected String friendUserId() {
        return currentHomeState == null
                ? null
                : currentHomeState.player().userId();
    }

    protected void toast(int messageResource, Object... arguments) {
        if (!isFinishing()) {
            Toast.makeText(
                            this,
                            getString(messageResource, arguments),
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    protected void toast(String message) {
        if (!isFinishing() && message != null && !message.isBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    protected void requireFriendLogin() {
        if (!isFinishing()) {
            showLoginPage();
        }
    }

    protected void dismissFriendDialogs() {}

    protected abstract void showFriendApplicationsDialog();

    protected abstract void showFriendAddDialog();

    protected abstract void loadUnreadFriendNotifications();

    protected abstract void loadFriendAvatars();

    protected abstract void inviteFriend(FriendEntry friend);

    protected abstract void recallFriend(FriendEntry friend);

    protected abstract void inviteAllOnlineFriends();

    protected abstract void showFriendActions(FriendEntry friend);

    private class DrawerListener implements FriendDrawerView.Listener {
        @Override
        public void onExpandedChanged(boolean expanded) {
            if (expanded) {
                if (friendDrawerStore != null) {
                    friendDrawerStore.markExpandedToday(
                            friendUserId(), System.currentTimeMillis());
                }
                reloadFriendPages();
            }
        }

        @Override
        public void onLoadMoreRequested() {
            loadFriendPage(
                    friendDrawerView == null
                            ? 0
                            : friendDrawerView.nextPage(),
                    FriendDrawerState.PAGE_SIZE);
        }

        @Override
        public void onInviteRequested(FriendEntry friend) {
            inviteFriend(friend);
        }

        @Override
        public void onRecallRequested(FriendEntry friend) {
            recallFriend(friend);
        }

        @Override
        public void onInviteAllRequested() {
            inviteAllOnlineFriends();
        }

        @Override
        public void onFriendAvatarRequested(FriendEntry friend) {
            showFriendActions(friend);
        }
    }
}
