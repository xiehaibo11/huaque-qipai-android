package com.nanbeiyule.game;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import java.util.function.Consumer;

/**
 * Friend action flow: invites with the 30-second per-friend cooldown,
 * shield/unshield, delete with a confirmation step, and friend avatar
 * loading through the shared avatar pipeline.
 */
abstract class MainActivityFriendActionsFlow
        extends MainActivityFriendFlow {

    @Override
    protected void loadFriendAvatars() {
        if (friendDrawerView == null) {
            return;
        }
        for (String avatarKey : friendDrawerView.missingAvatarKeys()) {
            loadAvatarBitmap(
                    avatarKey,
                    bitmap -> {
                        if (friendDrawerView != null) {
                            friendDrawerView.setAvatarBitmap(
                                    avatarKey, bitmap);
                        }
                    });
        }
    }

    @Override
    protected void loadAvatarBitmap(
            String avatarKey, Consumer<Bitmap> onBitmap) {
        if (avatarImageLoader == null || authSessionCoordinator == null) {
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        avatarImageLoader.load(
                                avatarKey, accessToken,
                                forwardingAvatar(callback)),
                new AuthSessionCoordinator.Callback<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        if (!isFinishing()) {
                            onBitmap.accept(bitmap);
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        requireFriendLogin();
                    }

                    @Override
                    public void onError(String message) {
                        // Missing avatars fall back to the default.
                    }
                });
    }

    private static AvatarImageLoader.Callback forwardingAvatar(
            AuthSessionCoordinator.CallCallback<Bitmap> callback) {
        return new AvatarImageLoader.Callback() {
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
        };
    }

    @Override
    protected void inviteFriend(FriendEntry friend) {
        String userId = friendUserId();
        if (friendDrawerStore != null
                && friendDrawerStore.inviteCoolingDown(
                        userId,
                        friend.publicPlayerId(),
                        System.currentTimeMillis())) {
            toast(R.string.friend_error_invite_too_frequent);
            return;
        }
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.invite(
                                accessToken,
                                friend.publicPlayerId(),
                                FriendNotificationItem.TYPE_INVITE,
                                forwardingFriend(callback)),
                new FriendResult<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        recordFriendInvite(userId, friend);
                        toast(R.string.friend_invite_sent);
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                        // The server-side 429 also arms the local
                        // cooldown as a fallback.
                        if (message.equals(
                                getString(
                                        R.string
                                                .friend_error_invite_too_frequent))) {
                            recordFriendInvite(userId, friend);
                        }
                    }
                });
    }

    @Override
    protected void showFriendActions(FriendEntry friend) {
        if (isFinishing()) {
            return;
        }
        String[] actions = {
            getString(
                    friend.shielded()
                            ? R.string.friend_action_unshield
                            : R.string.friend_action_shield),
            getString(R.string.friend_action_delete)
        };
        new AlertDialog.Builder(this)
                .setItems(
                        actions,
                        (dialog, which) -> {
                            if (which == 0) {
                                shieldFriend(friend, !friend.shielded());
                            } else {
                                confirmDeleteFriend(friend);
                            }
                        })
                .setOnDismissListener(dialog -> applyImmersiveMode())
                .show();
    }

    private void confirmDeleteFriend(FriendEntry friend) {
        if (isFinishing()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.friend_delete_confirm_title)
                .setMessage(
                        getString(
                                R.string.friend_delete_confirm_message,
                                friend.displayName()))
                .setPositiveButton(
                        R.string.friend_confirm,
                        (dialog, which) -> deleteFriend(friend))
                .setNegativeButton(R.string.friend_cancel, null)
                .setOnDismissListener(dialog -> applyImmersiveMode())
                .show();
    }

    private void deleteFriend(FriendEntry friend) {
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.deleteFriend(
                                accessToken,
                                friend.publicPlayerId(),
                                forwardingFriend(callback)),
                new FriendResult<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        toast(
                                R.string.friend_deleted_toast,
                                friend.displayName());
                        reloadFriendPages();
                    }
                });
    }

    private void shieldFriend(FriendEntry friend, boolean shielded) {
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.shield(
                                accessToken,
                                friend.publicPlayerId(),
                                shielded,
                                forwardingFriend(callback)),
                new FriendResult<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        toast(
                                shielded
                                        ? R.string.friend_shielded_toast
                                        : R.string
                                                .friend_unshielded_toast,
                                friend.displayName());
                        reloadFriendPages();
                    }
                });
    }

    @Override
    protected void recallFriend(FriendEntry friend) {
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.invite(
                                accessToken,
                                friend.publicPlayerId(),
                                FriendNotificationItem.TYPE_RECALL,
                                forwardingFriend(callback)),
                new FriendResult<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        toast(
                                R.string.friend_recall_sent,
                                friend.displayName());
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
    }

    @Override
    protected void inviteAllOnlineFriends() {
        if (friendDrawerView == null) {
            return;
        }
        String userId = friendUserId();
        executeFriend(
                (accessToken, callback) ->
                        friendApiClient.inviteAll(
                                accessToken,
                                forwardingFriend(callback)),
                new FriendResult<FriendInviteAllResult>() {
                    @Override
                    public void onSuccess(
                            FriendInviteAllResult result) {
                        // Mirror the server-side cooldown locally so the
                        // invite buttons dim right away.
                        if (friendDrawerStore != null
                                && friendDrawerView != null) {
                            long now = System.currentTimeMillis();
                            for (FriendEntry friend :
                                    friendDrawerView.onlineFriends()) {
                                friendDrawerStore.recordInvite(
                                        userId,
                                        friend.publicPlayerId(),
                                        now);
                            }
                        }
                        if (result.invitedCount() <= 0) {
                            toast(R.string.friend_invite_all_none);
                        } else if (result.cooldownSkippedCount() > 0) {
                            toast(
                                    R.string
                                            .friend_invite_all_sent_skipped,
                                    result.invitedCount(),
                                    result.cooldownSkippedCount());
                        } else {
                            toast(
                                    R.string.friend_invite_all_sent,
                                    result.invitedCount());
                        }
                    }
                });
    }

    private void recordFriendInvite(String userId, FriendEntry friend) {
        if (friendDrawerStore != null) {
            friendDrawerStore.recordInvite(
                    userId,
                    friend.publicPlayerId(),
                    System.currentTimeMillis());
        }
    }
}
