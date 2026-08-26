package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.nanbeiyule.game.lobbyshare.LobbyShareContent;
import com.nanbeiyule.game.lobbyshare.LobbyShareController;
import com.nanbeiyule.game.lobbyshare.LobbyShareRewardOffer;
import com.nanbeiyule.game.lobbyshare.WechatLobbyShareGateway;
import com.nanbeiyule.game.wechat.WechatLoginManager;
import java.util.Objects;

/** Public lobby-share surface backed only by a real WeChat friend request or link copy. */
public final class LobbyShareDialog extends Dialog {
    private final LobbyShareContent content;
    private final LobbyShareController controller;
    private final WechatLoginManager manager;
    private final LobbyShareView shareView;
    private final boolean ownsManager;
    private boolean detached;

    public LobbyShareDialog(Context context, GameHomeState.Player currentPlayer) {
        this(
                context,
                currentPlayer,
                new WechatLoginManager(context),
                LobbyShareRewardOffer.none(),
                true);
    }

    public LobbyShareDialog(
            Context context,
            GameHomeState.Player currentPlayer,
            WechatLoginManager existingManager) {
        this(context, currentPlayer, existingManager, LobbyShareRewardOffer.none(), false);
    }

    /** Shows a reward only when it was supplied by an authenticated server response. */
    public LobbyShareDialog(
            Context context,
            GameHomeState.Player currentPlayer,
            WechatLoginManager existingManager,
            LobbyShareRewardOffer serverReward) {
        this(context, currentPlayer, existingManager, serverReward, false);
    }

    private LobbyShareDialog(
            Context context,
            GameHomeState.Player currentPlayer,
            WechatLoginManager manager,
            LobbyShareRewardOffer serverReward,
            boolean ownsManager) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        this.manager = Objects.requireNonNull(manager, "manager");
        this.ownsManager = ownsManager;
        content =
                LobbyShareContent.create(
                        currentProductName(context),
                        LobbyShareContent.PRODUCTION_DOWNLOAD_URL,
                        currentPlayer);
        controller =
                new LobbyShareController(content, new WechatLobbyShareGateway(manager));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        shareView =
                new LobbyShareView(
                        context,
                        content,
                        Objects.requireNonNull(serverReward, "serverReward"),
                        new LobbyShareView.Actions() {
                            @Override
                            public void onWechatShareRequested() {
                                launchWechat();
                            }

                            @Override
                            public void onCopyLinkRequested() {
                                copyDownloadLink();
                            }

                            @Override
                            public void onCloseRequested() {
                                dismiss();
                            }
                        });
        setContentView(
                shareView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        configureWindow();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            FullscreenWindowPolicy.apply(window);
            window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
            window.getDecorView().requestApplyInsets();
        }
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    @Override
    public void dismiss() {
        if (ownsManager && !detached) {
            detached = true;
            manager.detach();
        }
        super.dismiss();
    }

    private void launchWechat() {
        LobbyShareController.Outcome outcome = controller.shareToWechatFriend();
        if (outcome != LobbyShareController.Outcome.WECHAT_OPENED) {
            shareView.setCopyVisible(true);
        }
        String message = switch (outcome) {
            case WECHAT_OPENED -> "已打开微信，请选择好友完成分享";
            case WECHAT_UNAVAILABLE -> "微信不可用，可复制官方下载链接";
            case WECHAT_REJECTED -> "未能打开微信，可复制官方下载链接";
        };
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void copyDownloadLink() {
        ClipboardManager clipboard =
                (ClipboardManager)
                        getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(getContext(), "无法访问剪贴板", Toast.LENGTH_SHORT).show();
            return;
        }
        clipboard.setPrimaryClip(
                ClipData.newPlainText(
                        content.shareTitle() + "官方下载地址",
                        controller.copyableLink()));
        Toast.makeText(getContext(), "官方下载链接已复制", Toast.LENGTH_SHORT).show();
    }

    private void configureWindow() {
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        FullscreenWindowPolicy.apply(window);
    }

    private static String currentProductName(Context context) {
        CharSequence label =
                context.getApplicationInfo().loadLabel(context.getPackageManager());
        if (label == null || label.toString().isBlank()) {
            throw new IllegalStateException("Application label is required for sharing");
        }
        return label.toString().trim();
    }
}
