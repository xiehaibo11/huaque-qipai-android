package com.nanbeiyule.game;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import java.net.URI;
import java.util.Locale;

final class MembershipPaymentLauncher {
    private final Activity activity;
    private final PendingMembershipPaymentStore pendingPaymentStore;

    MembershipPaymentLauncher(
            Activity activity,
            PendingMembershipPaymentStore pendingPaymentStore) {
        this.activity = activity;
        this.pendingPaymentStore = pendingPaymentStore;
    }

    interface Callback {
        void onPaymentMessage(String message);
    }

    interface RelaunchCallback {
        void onRelaunched(String orderId);
    }

    void launch(MembershipOrderState order, Callback callback) {
        if (callback == null) {
            return;
        }
        if (order == null) {
            callback.onPaymentMessage("会员订单创建失败，请稍后重试");
            return;
        }
        String provider = order.provider() == null ? "" : order.provider().toUpperCase(Locale.ROOT);
        switch (provider) {
            case "MOCK" -> startMockPayment(order, callback);
            case "YISHOUMI" -> startYishoumiPayment(order, callback);
            default -> callback.onPaymentMessage("暂不支持的支付渠道：" + order.provider());
        }
    }

    boolean relaunchPending(
            Callback callback, RelaunchCallback relaunchCallback) {
        PendingMembershipPaymentStore.PendingPayment pending = pendingPaymentStore.load();
        if (pending == null) {
            return false;
        }
        if (!isAlipayAvailable()) {
            callback.onPaymentMessage("无法打开支付宝，请安装支付宝后重试");
            return true;
        }
        URI paymentUrl = MembershipPaymentUrl.parse(pending.paymentUrl());
        if (paymentUrl == null) {
            callback.onPaymentMessage("上一笔支付结果仍在确认中，请稍后重试");
            return true;
        }
        try {
            launchAlipayAppPayment(paymentUrl);
            pendingPaymentStore.resetCancellationNotice(pending.orderId());
            relaunchCallback.onRelaunched(pending.orderId());
            callback.onPaymentMessage("正在重新打开上一笔支付宝订单");
        } catch (ActivityNotFoundException | IllegalArgumentException exception) {
            callback.onPaymentMessage("无法打开支付宝，请安装支付宝后重试");
        }
        return true;
    }

    boolean isAlipayAvailable() {
        Intent intent = alipayIntent(
                URI.create("https://www.nanbeiyule.com/payment/result"));
        return !activity.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
    }

    private void startMockPayment(MembershipOrderState order, Callback callback) {
        order.paymentParameters();
        callback.onPaymentMessage("调试支付订单已创建，等待 Mock 回调完成会员开通");
    }

    private void startYishoumiPayment(
            MembershipOrderState order, Callback callback) {
        URI paymentUrl =
                MembershipPaymentUrl.parse(
                        order.paymentParameter("paymentUrl"));
        if (paymentUrl == null) {
            callback.onPaymentMessage("支付地址无效，请稍后重试");
            return;
        }
        if (!isAlipayAvailable()) {
            callback.onPaymentMessage("无法打开支付宝，请安装支付宝后重试");
            return;
        }
        try {
            pendingPaymentStore.save(order.id(), paymentUrl.toString());
            launchAlipayAppPayment(paymentUrl);
            callback.onPaymentMessage("正在打开支付宝，请完成支付后返回南北娱乐");
        } catch (ActivityNotFoundException | IllegalArgumentException exception) {
            callback.onPaymentMessage("无法打开支付宝，请安装支付宝后重试");
        }
    }

    private void launchAlipayAppPayment(URI paymentUrl) {
        activity.startActivity(alipayIntent(paymentUrl));
    }

    private static Intent alipayIntent(URI paymentUrl) {
        URI alipayDeepLink = MembershipPaymentUrl.alipayH5DeepLink(paymentUrl);
        Intent intent =
                new Intent(Intent.ACTION_VIEW, Uri.parse(alipayDeepLink.toString()));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setPackage("com.eg.android.AlipayGphone");
        return intent;
    }
}
