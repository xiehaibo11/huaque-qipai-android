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

abstract class MainActivityPersonalPreferencesFlow extends MainActivityPersonalCenterFlow {
    protected void showPersonalCenterUnavailable(String featureName) {
        Toast.makeText(
                        this,
                        getString(
                                R.string.personal_center_feature_unavailable,
                                featureName),
                        Toast.LENGTH_SHORT)
                .show();
    }

    protected void savePrivacySettings(
            PersonalCenterPrivacySettings previous,
            PersonalCenterPrivacySettings updated) {
        if (personalCenterApiClient == null
                || authSessionCoordinator == null) {
            if (personalCenterDialog != null) {
                personalCenterDialog.setPrivacySettings(previous);
            }
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        personalCenterApiClient.updatePrivacy(
                                accessToken,
                                updated,
                                new PersonalCenterApiClient.PrivacyCallback() {
                                    @Override
                                    public void onSuccess(
                                            PersonalCenterPrivacySettings result) {
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
                        PersonalCenterPrivacySettings>() {
                    @Override
                    public void onSuccess(
                            PersonalCenterPrivacySettings result) {
                        if (personalCenterDialog != null) {
                            personalCenterDialog.setPrivacySettings(result);
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissPersonalCenter();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        if (personalCenterDialog != null) {
                            personalCenterDialog.setPrivacySettings(previous);
                        }
                        Toast.makeText(
                                        MainActivityPersonalPreferencesFlow.this,
                                        message,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    protected void showFeedbackInput(
            PersonalCenterFeedbackItem.Category category) {
        if (isFinishing()) {
            return;
        }
        EditText input = new EditText(this);
        input.setHint(
                category == PersonalCenterFeedbackItem.Category.REPORT
                        ? "请说明需要举报的行为"
                        : "请输入问题或建议");
        input.setMinLines(4);
        input.setMaxLines(7);
        input.setFilters(
                new InputFilter[] {new InputFilter.LengthFilter(500)});
        new AlertDialog.Builder(this)
                .setTitle(
                        category
                                        == PersonalCenterFeedbackItem.Category
                                                .REPORT
                                ? "举报反馈"
                                : "意见反馈")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton(
                        "提交",
                        (dialog, which) ->
                                submitPersonalCenterFeedback(
                                        category,
                                        input.getText().toString()))
                .setOnDismissListener(dialog -> applyImmersiveMode())
                .show();
    }

    protected void submitPersonalCenterFeedback(
            PersonalCenterFeedbackItem.Category category,
            String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            Toast.makeText(
                            this,
                            "请输入反馈内容",
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        personalCenterApiClient.submitFeedback(
                                accessToken,
                                category,
                                normalized,
                                new PersonalCenterApiClient.FeedbackCallback() {
                                    @Override
                                    public void onSuccess(
                                            PersonalCenterFeedbackItem item) {
                                        callback.onSuccess(item);
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
                        PersonalCenterFeedbackItem>() {
                    @Override
                    public void onSuccess(
                            PersonalCenterFeedbackItem result) {
                        Toast.makeText(
                                        MainActivityPersonalPreferencesFlow.this,
                                        "提交成功",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissPersonalCenter();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                        MainActivityPersonalPreferencesFlow.this,
                                        message,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    protected void loadFeedbackHistory() {
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        personalCenterApiClient.loadFeedbackHistory(
                                accessToken,
                                new PersonalCenterApiClient
                                        .FeedbackHistoryCallback() {
                                    @Override
                                    public void onSuccess(
                                            List<PersonalCenterFeedbackItem>
                                                    items) {
                                        callback.onSuccess(items);
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
                        List<PersonalCenterFeedbackItem>>() {
                    @Override
                    public void onSuccess(
                            List<PersonalCenterFeedbackItem> items) {
                        showFeedbackHistory(items);
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissPersonalCenter();
                        showLoginPage();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                        MainActivityPersonalPreferencesFlow.this,
                                        message,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    protected void showFeedbackHistory(
            List<PersonalCenterFeedbackItem> items) {
        if (items.isEmpty()) {
            showInformationDialog("反馈记录", "暂无反馈记录");
            return;
        }
        StringBuilder message = new StringBuilder();
        for (PersonalCenterFeedbackItem item : items) {
            if (message.length() > 0) {
                message.append("\n\n");
            }
            message.append(
                            item.category()
                                            == PersonalCenterFeedbackItem
                                                    .Category.REPORT
                                    ? "举报"
                                    : "反馈")
                    .append(" · ")
                    .append(item.status())
                    .append('\n')
                    .append(item.content())
                    .append('\n')
                    .append(item.createdAt());
        }
        showInformationDialog("反馈记录", message.toString());
    }
}
