package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import java.util.EnumMap;
import java.util.List;

/** Dark-navy, champagne-gold personal center backed by authenticated first-party state. */

abstract class PersonalCenterProfilePageRenderer extends PersonalCenterActionRenderer {
    protected PersonalCenterProfilePageRenderer(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected void drawPersonalInfoPage(Canvas canvas) {
        drawCircularAvatar(canvas, rect(layout.avatar()));

        setText(35.0f, BROWN_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas,
                ellipsizeName(state.player().displayName()) + "  ♂",
                750.0f,
                398.0f);
        setText(22.0f, MUTED_TEXT, false, Paint.Align.RIGHT);
        drawBaselineCenteredText(
                canvas,
                "ID：" + state.player().publicPlayerId(),
                825.0f,
                449.0f);
        drawSmallGoldButton(canvas, rect(layout.copyHit()), "复制");
        setText(22.0f, MUTED_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas,
                "快乐游戏，随心体验！  ✎",
                750.0f,
                510.0f);

        drawRoomCardResourceRow(
                canvas,
                rect(layout.purchasedRoomCards()),
                "购买房卡",
                state.wallet().purchasedRoomCards(),
                false,
                false);
        drawRoomCardResourceRow(
                canvas,
                rect(layout.boundRoomCards()),
                "绑定房卡",
                state.wallet().boundRoomCards(),
                true,
                true);
        drawDiamondResourceRow(
                canvas,
                rect(layout.diamonds()),
                state.wallet().diamonds());

        RectF quickCard = new RectF(450.0f, 550.0f, 1830.0f, 710.0f);
        drawCard(canvas, quickCard);
        float quickWidth = quickCard.width() / 4.0f;
        drawQuickAction(
                canvas,
                quickCard.left,
                quickWidth,
                IconType.MEDAL,
                "我的战绩");
        drawQuickAction(
                canvas,
                quickCard.left + quickWidth,
                quickWidth,
                IconType.FAVORITE,
                "我的收藏");
        drawQuickAction(
                canvas,
                quickCard.left + quickWidth * 2.0f,
                quickWidth,
                IconType.GIFT,
                "我的礼包");
        drawQuickAction(
                canvas,
                quickCard.left + quickWidth * 3.0f,
                quickWidth,
                IconType.MESSAGE,
                "我的消息");

        drawGoldButton(canvas, layout.switchAccountHit(), "切换账号");
        drawBlueButton(canvas, layout.switchRegionHit(), "切换地区");
        drawTextLink(canvas, layout.deleteAccountHit(), "账号注销");
        drawTextLink(canvas, layout.refreshAvatarHit(), "刷新头像");
    }

    protected void drawAccountSecurityPage(Canvas canvas) {
        String phone =
                state.account().phoneBound()
                        ? state.account().maskedPhone()
                        : "未绑定";
        float top = 145.0f;
        float rowHeight = 105.0f;
        drawSecurityRow(
                canvas,
                top,
                IconType.PHONE,
                "绑定手机",
                phone,
                state.capabilities().phoneRebind() ? "更换" : "查看");
        drawSecurityRow(
                canvas,
                top + rowHeight,
                IconType.LOCK,
                "登录密码",
                "未开放",
                "修改");
        drawSecurityRow(
                canvas,
                top + rowHeight * 2.0f,
                IconType.LOCK,
                "支付密码",
                "未开放",
                "修改");
        drawSecurityRow(
                canvas,
                top + rowHeight * 3.0f,
                IconType.ID_CARD,
                "实名认证",
                state.capabilities().healthCertification()
                        ? "可认证"
                        : "未开放",
                "查看");
        drawSecurityRow(
                canvas,
                top + rowHeight * 4.0f,
                IconType.DEVICE,
                "设备管理",
                "未开放",
                "管理");

        RectF safetyCard = new RectF(450.0f, 700.0f, 1830.0f, 910.0f);
        drawCard(canvas, safetyCard);
        int score =
                Math.min(
                        90,
                        40
                                + (state.account().phoneBound() ? 30 : 0)
                                + state.account()
                                                .identityProviders()
                                                .size()
                                        * 10);
        setText(27.0f, BROWN_TEXT, false, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas,
                "安全等级：",
                safetyCard.left + 55.0f,
                safetyCard.top + 64.0f);
        setText(27.0f, GREEN, true, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas,
                score >= 70 ? "高" : "中",
                safetyCard.left + 185.0f,
                safetyCard.top + 64.0f);
        drawSlider(
                canvas,
                new RectF(
                        safetyCard.left + 55.0f,
                        safetyCard.top + 118.0f,
                        safetyCard.right - 275.0f,
                        safetyCard.top + 142.0f),
                score / 100.0f,
                GREEN);
        setText(24.0f, BROWN_TEXT, false, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas,
                score + "分",
                safetyCard.right - 235.0f,
                safetyCard.top + 130.0f);
        drawIcon(
                canvas,
                IconType.SHIELD,
                safetyCard.right - 105.0f,
                safetyCard.centerY(),
                72.0f,
                GOLD);
    }
}
