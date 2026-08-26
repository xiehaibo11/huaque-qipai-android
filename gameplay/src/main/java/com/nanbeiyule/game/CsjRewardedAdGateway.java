package com.nanbeiyule.game;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import java.util.Map;
import org.json.JSONObject;

final class CsjRewardedAdGateway implements RewardedAdGateway {
    private TTAdNative adNative;
    private TTRewardVideoAd rewardAd;
    private Listener listener;
    private Activity activity;
    private FreeDrawSession session;
    private boolean showRequested;
    private boolean rewardDelivered;
    private String[] slotIds;
    private int slotIndex;
    private String activeSlotId;

    @Override
    public void loadAndShow(Activity activity, FreeDrawSession session, Listener listener) {
        this.activity = activity;
        this.session = session;
        this.listener = listener;
        rewardAd = null;
        showRequested = false;
        rewardDelivered = false;
        slotIds = BuildConfig.CSJ_REWARD_SLOT_IDS.split(",");
        slotIndex = -1;
        activeSlotId = "";
        if (TTAdSdk.isInitSuccess()) {
            load();
            return;
        }
        TTAdConfig config =
                new TTAdConfig.Builder()
                        .appId(BuildConfig.CSJ_APP_ID)
                        .appName(applicationName(activity))
                        .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                        .supportMultiProcess(false)
                        .build();
        TTAdSdk.init(activity.getApplicationContext(), config);
        TTAdSdk.start(
                new TTAdSdk.Callback() {
                    @Override
                    public void success() {
                        activity.runOnUiThread(CsjRewardedAdGateway.this::load);
                    }

                    @Override
                    public void fail(int code, String message) {
                        error("广告初始化失败（" + code + "）：" + safe(message));
                    }
                });
    }

    private void load() {
        Activity current = activity;
        if (current == null || current.isFinishing() || session == null) return;
        adNative = TTAdSdk.getAdManager().createAdNative(current);
        loadNext();
    }

    private void loadNext() {
        Activity current = activity;
        if (current == null || current.isFinishing() || session == null || adNative == null) return;
        slotIndex++;
        if (slotIds == null || slotIndex >= slotIds.length) return;
        activeSlotId = slotIds[slotIndex].trim();
        JSONObject media = new JSONObject();
        try {
            media.put("freeDrawSessionId", session.sessionId());
            media.put("topOnPlacementId", session.adPlacementId());
        } catch (Exception ignored) {
            // Both values are validated server strings.
        }
        AdSlot slot =
                new AdSlot.Builder()
                        .setCodeId(activeSlotId)
                        .setUserID(session.userCustomData())
                        .setMediaExtra(media.toString())
                        .setOrientation(TTAdConstant.HORIZONTAL)
                        .setRewardName("免费抽奖")
                        .setRewardAmount(1)
                        .build();
        adNative.loadRewardVideoAd(slot, new LoadListener(activeSlotId));
    }

    private void show(TTRewardVideoAd ad) {
        if (showRequested || ad == null || activity == null || activity.isFinishing()) return;
        showRequested = true;
        rewardAd = ad;
        ad.setRewardAdInteractionListener(new InteractionListener());
        ad.showRewardVideoAd(activity);
    }

    private final class LoadListener implements TTAdNative.RewardVideoAdListener {
        private final String sourceId;

        private LoadListener(String sourceId) {
            this.sourceId = sourceId;
        }

        @Override
        public void onError(int code, String message) {
            if (slotIds != null && slotIndex + 1 < slotIds.length) {
                loadNext();
                return;
            }
            error("原版广告位均未填充（" + code + "）：" + safe(message));
        }

        @Override
        public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
            activeSlotId = sourceId;
            rewardAd = ad;
        }

        @Override
        public void onRewardVideoCached() {
            show(rewardAd);
        }

        @Override
        public void onRewardVideoCached(TTRewardVideoAd ad) {
            show(ad);
        }
    }

    private final class InteractionListener
            implements TTRewardVideoAd.RewardAdInteractionListener {
        @Override
        public void onAdShow() {
            if (listener != null) listener.onShown();
        }

        @Override public void onAdVideoBarClick() {}

        @Override
        public void onAdClose() {
            if (listener != null) listener.onClosed();
        }

        @Override public void onVideoComplete() {}

        @Override
        public void onVideoError() {
            error("广告播放失败，请稍后重试");
        }

        @Override public void onRewardVerify(
                boolean rewardVerify, int rewardAmount, String rewardName,
                int errorCode, String errorMessage) {}

        @Override
        public void onRewardArrived(boolean verified, int rewardType, Bundle extraInfo) {
            if (!verified || rewardDelivered || listener == null) return;
            rewardDelivered = true;
            listener.onRewardVerified(evidence());
        }

        @Override public void onSkippedVideo() {}
    }

    private Evidence evidence() {
        Map<String, Object> values = rewardAd == null ? Map.of() : rewardAd.getMediaExtraInfo();
        String requestId = value(values, "request_id");
        if (requestId.isBlank()) requestId = value(values, "requestId");
        return new Evidence("CSJ:" + activeSlotId, requestId);
    }

    private void error(String message) {
        Activity current = activity;
        if (current == null) return;
        current.runOnUiThread(() -> {
            if (listener != null) listener.onError(message);
        });
    }

    @Override
    public void release() {
        activity = null;
        session = null;
        listener = null;
        rewardAd = null;
        adNative = null;
        slotIds = null;
        activeSlotId = "";
    }

    private static String value(Map<String, Object> values, String key) {
        Object value = values == null ? null : values.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static String safe(String message) {
        return message == null || message.isBlank() ? "请稍后重试" : message;
    }

    private static String applicationName(Context context) {
        try {
            return String.valueOf(
                    context.getPackageManager().getApplicationLabel(context.getApplicationInfo()));
        } catch (Exception ignored) {
            return "浙江游戏大厅";
        }
    }
}
