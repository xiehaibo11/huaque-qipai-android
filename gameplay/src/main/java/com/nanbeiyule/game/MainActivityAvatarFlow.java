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

abstract class MainActivityAvatarFlow extends MainActivityMaintenanceFlow {
    protected void showAvatarEditor() {
        if (isFinishing()
                || currentHomeState == null
                || currentAvatarBitmap == null
                || avatarEditorDialog != null) {
            return;
        }
        pendingAvatar = null;
        avatarEditorDialog =
                new AvatarEditorDialog(
                        this,
                        currentAvatarBitmap,
                        currentHomeState.player().membershipLevel(),
                        new AvatarEditorDialog.Listener() {
                            @Override
                            public void onChooseRequested() {
                                chooseAvatarPhoto();
                            }

                            @Override
                            public void onSaveRequested() {
                                uploadSelectedAvatar();
                            }
                        });
        avatarEditorDialog.setOnDismissListener(
                dialog -> {
                    avatarEditorDialog = null;
                    pendingAvatar = null;
                    applyImmersiveMode();
                });
        avatarEditorDialog.show();
    }

    protected void chooseAvatarPhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CHOOSE_AVATAR);
    }

    protected void processSelectedAvatar(Uri uri) {
        if (avatarEditorDialog == null || avatarImageProcessor == null) {
            return;
        }
        avatarEditorDialog.setUploading(true);
        Toast.makeText(this, R.string.avatar_editor_processing, Toast.LENGTH_SHORT).show();
        avatarProcessorExecutor.execute(
                () -> {
                    try {
                        AvatarImageProcessor.ProcessedAvatar processed =
                                avatarImageProcessor.process(uri);
                        runOnUiThread(
                                () -> {
                                    if (isFinishing() || avatarEditorDialog == null) {
                                        processed.bitmap().recycle();
                                        return;
                                    }
                                    pendingAvatar = processed;
                                    avatarEditorDialog.setSelectedAvatar(processed.bitmap());
                                    avatarEditorDialog.setUploading(false);
                                });
                    } catch (Exception exception) {
                        String detail =
                                exception.getMessage() == null
                                        ? getString(R.string.avatar_editor_process_failed, "")
                                        : getString(
                                                R.string.avatar_editor_process_failed,
                                                exception.getMessage());
                        runOnUiThread(
                                () -> {
                                    if (avatarEditorDialog != null) {
                                        avatarEditorDialog.setUploading(false);
                                    }
                                    if (!isFinishing()) {
                                        Toast.makeText(
                                                        MainActivityAvatarFlow.this,
                                                        detail,
                                                        Toast.LENGTH_LONG)
                                                .show();
                                    }
                                });
                    }
                });
    }

    protected void uploadSelectedAvatar() {
        if (pendingAvatar == null
                || avatarEditorDialog == null
                || avatarApiClient == null
                || authSessionCoordinator == null) {
            return;
        }
        AvatarImageProcessor.ProcessedAvatar upload = pendingAvatar;
        avatarEditorDialog.setUploading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        avatarApiClient.upload(
                                upload.jpeg(),
                                accessToken,
                                new AvatarApiClient.Callback<>() {
                                    @Override
                                    public void onSuccess(
                                            AvatarApiClient.UploadResult result) {
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
                                }),
                new AuthSessionCoordinator.Callback<
                        AvatarApiClient.UploadResult>() {
                    @Override
                    public void onSuccess(AvatarApiClient.UploadResult result) {
                        if (isFinishing()) {
                            return;
                        }
                        currentAvatarBitmap = upload.bitmap();
                        if (avatarImageLoader != null) {
                            avatarImageLoader.putMemory(
                                    result.avatarKey(),
                                    upload.bitmap());
                        }
                        if (currentHomeView != null) {
                            currentHomeView.setAvatarBitmap(upload.bitmap());
                        }
                        if (avatarEditorDialog != null) {
                            avatarEditorDialog.dismiss();
                        }
                        Toast.makeText(
                                        MainActivityAvatarFlow.this,
                                        R.string.avatar_editor_saved,
                                        Toast.LENGTH_SHORT)
                                .show();
                        loadGameHome();
                    }

                    @Override
                    public void onLoginRequired() {
                        if (!isFinishing()) {
                            if (avatarEditorDialog != null) {
                                avatarEditorDialog.dismiss();
                            }
                            showLoginPage();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (isFinishing()) {
                            return;
                        }
                        if (avatarEditorDialog != null) {
                            avatarEditorDialog.setUploading(false);
                        }
                        Toast.makeText(
                                        MainActivityAvatarFlow.this,
                                        message,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    protected void showGameHomeStatus(int messageResource, boolean retryEnabled) {
        finishForegroundReturnLoadingRequest();
        if (loginRequestLoadingController != null) {
            loginRequestLoadingController.bind(null);
        }
        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(Color.rgb(10, 24, 48));

        TextView messageView = new TextView(this);
        messageView.setText(messageResource);
        messageView.setTextColor(Color.WHITE);
        messageView.setTextSize(24.0f);
        messageView.setGravity(Gravity.CENTER);
        messageView.setBackgroundColor(Color.argb(145, 25, 61, 112));
        if (retryEnabled) {
            messageView.setText(
                    getString(
                            R.string.game_home_retry_format,
                            getString(messageResource)));
            messageView.setOnClickListener(view -> loadGameHome());
        }
        FrameLayout.LayoutParams messageParams =
                new FrameLayout.LayoutParams(
                        440,
                        150,
                        Gravity.CENTER);
        container.addView(messageView, messageParams);
        loadingView = container;
        setContentView(container);
    }

    protected void startLocalResourceVerification(SecondLoadingView secondLoadingView) {
        if (resourceVerifier != null) {
            resourceVerifier.cancel();
        }
        resourceVerifier =
                new LocalResourceVerifier(
                        this,
                        new LocalResourceVerifier.Listener() {
                            @Override
                            public void onProgress(float percent) {
                                if (loadingView == secondLoadingView) {
                                    secondLoadingView.setTargetProgress(percent);
                                }
                            }

                            @Override
                            public void onFailure(String message) {
                                if (loadingView == secondLoadingView) {
                                    secondLoadingView.setStatusText(message);
                                }
                            }
                        });
        resourceVerifier.start();
    }
}
