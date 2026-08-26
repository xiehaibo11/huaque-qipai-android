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

abstract class MainActivityMaintenanceFlow extends MainActivityPersonalPreferencesFlow {
    protected void clearPersonalCenterCache() {
        File cacheDirectory = getCacheDir();
        avatarProcessorExecutor.execute(
                () -> {
                    boolean cleared = deleteChildren(cacheDirectory);
                    runOnUiThread(
                            () ->
                                    Toast.makeText(
                                                    MainActivityMaintenanceFlow.this,
                                                    cleared
                                                            ? "缓存已清理"
                                                            : "部分缓存清理失败",
                                                    Toast.LENGTH_SHORT)
                                            .show());
                });
    }

    protected static boolean deleteChildren(File directory) {
        File[] children =
                directory == null ? null : directory.listFiles();
        if (children == null) {
            return true;
        }
        boolean success = true;
        for (File child : children) {
            if (child.isDirectory()) {
                success &= deleteChildren(child);
            }
            success &= child.delete();
        }
        return success;
    }

    protected void runPersonalCenterNetworkCheck() {
        long startedAt = SystemClock.elapsedRealtime();
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        personalCenterApiClient.load(
                                accessToken,
                                new PersonalCenterApiClient.Callback() {
                                    @Override
                                    public void onSuccess(
                                            PersonalCenterState state) {
                                        callback.onSuccess(state);
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
                new AuthSessionCoordinator.Callback<
                        PersonalCenterState>() {
                    @Override
                    public void onSuccess(PersonalCenterState result) {
                        long latency =
                                SystemClock.elapsedRealtime() - startedAt;
                        showInformationDialog(
                                "网络检测",
                                "账号服务连接正常\n响应耗时："
                                        + latency
                                        + " ms");
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissPersonalCenter();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        showInformationDialog("网络检测", message);
                    }
                });
    }

    protected void runPersonalCenterResourceRepair() {
        if (personalCenterRepairVerifier != null) {
            Toast.makeText(
                            this,
                            "资源检测正在进行",
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        personalCenterRepairVerifier =
                new LocalResourceVerifier(
                        this,
                        new LocalResourceVerifier.Listener() {
                            @Override
                            public void onProgress(float percent) {
                                if (percent < 100.0f) {
                                    return;
                                }
                                personalCenterRepairVerifier = null;
                                showInformationDialog(
                                        "问题修复",
                                        "本地资源完整性检测通过");
                            }

                            @Override
                            public void onFailure(String message) {
                                personalCenterRepairVerifier = null;
                                showInformationDialog(
                                        "问题修复", message);
                            }
                        });
        personalCenterRepairVerifier.start();
    }

    protected void openExternalUrl(String url) {
        try {
            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)));
        } catch (RuntimeException exception) {
            Toast.makeText(
                            this,
                            R.string.login_agreement_open_failed,
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
