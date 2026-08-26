package com.nanbeiyule.game;

import android.graphics.Bitmap;

/**
 * 大厅头像加载回调。
 *
 * <p>从 {@code MainActivityGameHomeDisplayFlow} 抽出：该类是大厅显示编排入口，
 * 契约测试 {@code TaizhouMahjongEntryContractTest} 要求它保持在 300 行警告线以内，
 * 避免编排入口继续膨胀。这里只是搬运，不改变任何行为。
 */
final class GameHomeAvatarBinding {
    private GameHomeAvatarBinding() {}

    /** 头像加载三分支：成功换图、会话失效回登录、失败退回默认头像。 */
    static AuthSessionCoordinator.Callback<Bitmap> callback(
            MainActivityGameHomeDisplayFlow owner, GameHomeView homeView) {
        return new AuthSessionCoordinator.Callback<>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (!owner.isFinishing() && owner.currentHomeView == homeView) {
                    owner.currentAvatarBitmap = bitmap;
                    homeView.setAvatarBitmap(bitmap);
                    if (owner.personalCenterDialog != null) {
                        owner.personalCenterDialog.setAvatarBitmap(bitmap);
                    }
                }
            }

            @Override
            public void onLoginRequired() {
                if (!owner.isFinishing()) {
                    owner.showLoginPage();
                }
            }

            @Override
            public void onError(String message) {
                if (!owner.isFinishing()
                        && owner.currentHomeView == homeView
                        && owner.avatarImageLoader != null) {
                    homeView.setAvatarBitmap(owner.avatarImageLoader.defaultAvatar());
                }
            }
        };
    }
}
