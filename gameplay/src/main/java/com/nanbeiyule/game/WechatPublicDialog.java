package com.nanbeiyule.game;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/** Public native entry point for the recovered Taizhou official-account panel. */
public final class WechatPublicDialog extends TaizhouFullscreenDialog {
    private static final String WECHAT_PACKAGE = "com.tencent.mm";

    private final WechatPublicModel model;
    private final WechatPublicView publicView;
    private boolean copiedOnOpen;

    public WechatPublicDialog(Context context) {
        this(context, WechatPublicModel.taizhou());
    }

    private WechatPublicDialog(Context context, WechatPublicModel model) {
        this(context, model, new WechatPublicView(context, model));
    }

    private WechatPublicDialog(
            Context context, WechatPublicModel model, WechatPublicView publicView) {
        super(context, publicView, false);
        this.model = model;
        this.publicView = publicView;
        publicView.setActions(
                new WechatPublicView.Actions() {
                    @Override
                    public void onDismissRequested() {
                        dismiss();
                    }

                    @Override
                    public void onCopyRequested() {
                        copyPublicName(true);
                    }

                    @Override
                    public void onOpenWechatRequested() {
                        openWechat();
                    }
                });
    }

    public void setButtonClickSound(Runnable sound) {
        publicView.setButtonClickSound(sound);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!copiedOnOpen) {
            copiedOnOpen = copyPublicName(true);
        }
    }

    private boolean copyPublicName(boolean showConfirmation) {
        ClipboardManager clipboard =
                (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            if (showConfirmation) {
                Toast.makeText(getContext(), "无法访问剪贴板", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        clipboard.setPrimaryClip(
                ClipData.newPlainText("微信公众号", model.clipboardText()));
        if (showConfirmation) {
            Toast.makeText(getContext(), "已为您复制公众号", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void openWechat() {
        Intent launch =
                getContext().getPackageManager().getLaunchIntentForPackage(WECHAT_PACKAGE);
        WechatPublicModel.OpenAction action =
                WechatPublicModel.openAction(launch != null);
        if (action == WechatPublicModel.OpenAction.SHOW_NOT_INSTALLED) {
            copyPublicName(false);
            Toast.makeText(
                            getContext(),
                            "未检测到微信，公众号名称已复制",
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        try {
            getContext().startActivity(launch);
        } catch (RuntimeException exception) {
            copyPublicName(false);
            Toast.makeText(
                            getContext(),
                            "微信当前无法打开，公众号名称已复制",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
