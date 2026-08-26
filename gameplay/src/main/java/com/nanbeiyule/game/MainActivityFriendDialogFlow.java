package com.nanbeiyule.game;

import android.app.AlertDialog;

/**
 * Friend dialog flow: owns the add-friend dialog, the incoming
 * applications dialog and the unread invite/reserve notification
 * popup. The drawer itself lives in {@link MainActivityFriendFlow}.
 */
abstract class MainActivityFriendDialogFlow
        extends MainActivityFriendActionsFlow {

    @Override
    protected void loadUnreadFriendNotifications() {
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.notifications(
                                accessToken, true, forwardingFriend(callback)),
                new FriendResult<FriendNotificationsPage>() {
                    @Override
                    public void onSuccess(FriendNotificationsPage page) {
                        if (page.total() > 0
                                && !page.notifications().isEmpty()) {
                            showFriendNotifications(page);
                            markFriendNotificationsRead();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        // Unread notifications stay silent on failure.
                    }
                });
    }

    private void showFriendNotifications(FriendNotificationsPage page) {
        if (isFinishing()) {
            return;
        }
        StringBuilder message = new StringBuilder();
        int limit = Math.min(5, page.notifications().size());
        for (int index = 0; index < limit; index++) {
            FriendNotificationItem item = page.notifications().get(index);
            if (message.length() > 0) {
                message.append('\n');
            }
            message.append(
                    getString(
                            FriendNotificationItem.TYPE_RESERVE.equals(
                                            item.type())
                                    ? R.string.friend_notification_reserve
                                    : FriendNotificationItem.TYPE_RECALL
                                                    .equals(item.type())
                                            ? R.string
                                                    .friend_notification_recall
                                            : R.string
                                                    .friend_notification_invite,
                            item.actorDisplayName()));
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.friend_notification_title)
                .setMessage(message.toString())
                .setPositiveButton(R.string.friend_confirm, null)
                .setOnDismissListener(dialog -> applyImmersiveMode())
                .show();
    }

    private void markFriendNotificationsRead() {
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.markAllRead(
                                accessToken, forwardingFriend(callback)),
                new FriendResult<Void>() {
                    @Override
                    public void onSuccess(Void result) {}

                    @Override
                    public void onError(String message) {}
                });
    }

    @Override
    protected void showFriendAddDialog() {
        if (isFinishing() || friendAddDialog != null) {
            return;
        }
        FriendAddDialog dialog =
                new FriendAddDialog(
                        this,
                        new FriendAddDialog.Listener() {
                            @Override
                            public void onSearchRequested(
                                    FriendAddDialog source,
                                    String query) {
                                searchFriend(source, query);
                            }

                            @Override
                            public void onApplyRequested(
                                    FriendAddDialog source,
                                    FriendSearchResult result) {
                                applyFriend(source, result);
                            }
                        });
        friendAddDialog = dialog;
        dialog.setOnDismissListener(
                dismissed -> {
                    friendAddDialog = null;
                    applyImmersiveMode();
                });
        dialog.show();
    }

    @Override
    protected void showFriendApplicationsDialog() {
        if (isFinishing() || friendApplicationsDialog != null) {
            return;
        }
        FriendApplicationsDialog dialog =
                new FriendApplicationsDialog(
                        this,
                        new FriendApplicationsDialog.Listener() {
                            @Override
                            public void onAccept(
                                    FriendApplicationItem item) {
                                respondFriendApplication(item, true);
                            }

                            @Override
                            public void onReject(
                                    FriendApplicationItem item) {
                                respondFriendApplication(item, false);
                            }
                        });
        friendApplicationsDialog = dialog;
        dialog.setOnDismissListener(
                dismissed -> {
                    friendApplicationsDialog = null;
                    applyImmersiveMode();
                });
        dialog.show();
        refreshFriendApplications();
    }

    @Override
    protected void onFriendApplicationsLoaded(
            FriendApplicationsPage page) {
        if (friendApplicationsDialog != null) {
            friendApplicationsDialog.setApplications(
                    page.applications());
        }
    }

    @Override
    protected void dismissFriendDialogs() {
        if (friendAddDialog != null) {
            FriendAddDialog dialog = friendAddDialog;
            friendAddDialog = null;
            dialog.setOnDismissListener(null);
            dialog.dismiss();
        }
        if (friendApplicationsDialog != null) {
            FriendApplicationsDialog dialog = friendApplicationsDialog;
            friendApplicationsDialog = null;
            dialog.setOnDismissListener(null);
            dialog.dismiss();
        }
    }

    private void searchFriend(FriendAddDialog dialog, String query) {
        if (dialog != friendAddDialog) {
            return;
        }
        dialog.setSearching(true);
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.search(
                                accessToken, query, forwardingFriend(callback)),
                new FriendResult<FriendSearchResult>() {
                    @Override
                    public void onSuccess(FriendSearchResult result) {
                        if (dialog == friendAddDialog) {
                            dialog.setSearching(false);
                            dialog.showResult(result);
                            loadSearchAvatar(dialog, result);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (dialog == friendAddDialog) {
                            dialog.setSearching(false);
                            dialog.showError(message);
                        }
                    }
                });
    }

    private void applyFriend(
            FriendAddDialog dialog, FriendSearchResult result) {
        if (dialog != friendAddDialog) {
            return;
        }
        dialog.setSearching(true);
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.apply(
                                accessToken,
                                result.publicPlayerId(),
                                forwardingFriend(callback)),
                new FriendResult<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {
                        if (dialog == friendAddDialog) {
                            dialog.setSearching(false);
                            dialog.markApplied();
                        }
                        toast(R.string.friend_application_sent);
                    }

                    @Override
                    public void onError(String message) {
                        if (dialog == friendAddDialog) {
                            dialog.setSearching(false);
                            dialog.showError(message);
                        }
                    }
                });
    }

    private void respondFriendApplication(
            FriendApplicationItem item, boolean accept) {
        executeFriend(
                (accessToken, callback) -> {
                    if (accept) {
                        friendApiClient.accept(
                                accessToken,
                                item.publicPlayerId(),
                                forwardingFriend(callback));
                    } else {
                        friendApiClient.reject(
                                accessToken,
                                item.publicPlayerId(),
                                forwardingFriend(callback));
                    }
                },
                new FriendResult<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {
                        refreshFriendApplications();
                        if (accept) {
                            reloadFriendPages();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                        refreshFriendApplications();
                    }
                });
    }

    private void loadSearchAvatar(
            FriendAddDialog dialog, FriendSearchResult result) {
        String avatarKey = result.avatarKey();
        if (avatarKey == null || avatarKey.isBlank()) {
            return;
        }
        loadAvatarBitmap(
                avatarKey,
                bitmap -> {
                    if (dialog == friendAddDialog) {
                        dialog.setResultAvatar(bitmap);
                    }
                });
    }
}
