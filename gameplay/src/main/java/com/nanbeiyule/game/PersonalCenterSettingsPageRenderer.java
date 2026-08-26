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

abstract class PersonalCenterSettingsPageRenderer extends PersonalCenterProfilePageRenderer {
    protected PersonalCenterSettingsPageRenderer(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected void drawPrivacyPage(Canvas canvas) {
        float top = 145.0f;
        float rowHeight = 122.0f;
        drawPrivacyRow(
                canvas,
                top,
                "允许添加我为好友",
                "关闭后，其他玩家无法添加你为好友",
                privacy.allowFriendRequests());
        drawPrivacyRow(
                canvas,
                top + rowHeight,
                "允许查看我的战绩",
                "关闭后，其他玩家无法查看你的战绩",
                privacy.showGameRecord());
        drawPrivacyRow(
                canvas,
                top + rowHeight * 2.0f,
                "允许查看我的在线状态",
                "关闭后，其他玩家无法看到你的在线状态",
                privacy.showOnlineStatus());
        drawPrivacyRow(
                canvas,
                top + rowHeight * 3.0f,
                "允许聊天消息提醒",
                "关闭后，将不再接收聊天消息提醒",
                privacy.chatNotifications());
        drawPrivacyRow(
                canvas,
                top + rowHeight * 4.0f,
                "个性化推荐",
                "关闭后，将不会使用你的信息进行个性化推荐",
                privacy.personalizedRecommendations());
        setText(23.0f, BROWN_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(canvas, "隐私政策", 1112.0f, 875.0f);
        strokePaint.setStrokeWidth(1.0f);
        strokePaint.setColor(CHAMPAGNE_DARK);
        canvas.drawLine(1064.0f, 893.0f, 1160.0f, 893.0f, strokePaint);
    }

    protected void drawSystemSettingsPage(Canvas canvas) {
        RectF settingsCard = new RectF(450.0f, 145.0f, 1830.0f, 755.0f);
        drawCard(canvas, settingsCard);
        float labelX = 520.0f;
        float controlLeft = 740.0f;
        float toggleX = 1735.0f;

        drawSettingLabel(canvas, IconType.MUSIC, "音乐", labelX, 215.0f);
        drawSlider(
                canvas,
                new RectF(controlLeft, 202.0f, 1450.0f, 224.0f),
                systemSettings.musicVolume() / 100.0f,
                GOLD);
        drawToggle(
                canvas,
                toggleX,
                213.0f,
                systemSettings.musicEnabled());

        drawSettingLabel(canvas, IconType.SOUND, "音效", labelX, 292.0f);
        drawSlider(
                canvas,
                new RectF(controlLeft, 279.0f, 1450.0f, 301.0f),
                systemSettings.soundVolume() / 100.0f,
                GOLD);
        drawToggle(
                canvas,
                toggleX,
                290.0f,
                systemSettings.soundEnabled());

        drawSettingLabel(
                canvas, IconType.MICROPHONE, "语音播放", labelX, 369.0f);
        drawToggle(
                canvas,
                toggleX,
                367.0f,
                systemSettings.voiceEnabled());
        drawSettingLabel(
                canvas, IconType.VIBRATION, "震动提醒", labelX, 446.0f);
        drawToggle(
                canvas,
                toggleX,
                444.0f,
                systemSettings.vibrationEnabled());

        drawSettingLabel(canvas, IconType.DEVICE, "画面质量", labelX, 525.0f);
        drawChoiceChips(
                canvas,
                new String[] {"流畅", "标准", "高清", "极致"},
                820.0f,
                501.0f,
                144.0f,
                systemSettings.graphicsQuality());

        drawSettingLabel(
                canvas, IconType.SETTINGS, "局内特效", labelX, 602.0f);
        drawChoiceChips(
                canvas,
                new String[] {"低", "中", "高"},
                960.0f,
                578.0f,
                150.0f,
                systemSettings.effectsQuality());

        drawSettingLabel(canvas, IconType.NETWORK, "省电模式", labelX, 679.0f);
        setText(22.0f, MUTED_TEXT, false, Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas,
                "开启后降低帧率，关闭部分特效",
                740.0f,
                679.0f);
        drawToggle(
                canvas,
                toggleX,
                677.0f,
                systemSettings.batterySaver());

        float actionY = 845.0f;
        drawBottomTool(
                canvas, 830.0f, actionY, IconType.CACHE, "清理缓存");
        drawBottomTool(
                canvas, 1110.0f, actionY, IconType.NETWORK, "网络检测");
        drawBottomTool(
                canvas, 1390.0f, actionY, IconType.REPAIR, "问题修复");
    }

    protected void drawHelpFeedbackPage(Canvas canvas) {
        float top = 145.0f;
        float rowHeight = 122.0f;
        drawHelpRow(
                canvas,
                top,
                IconType.FAQ,
                "常见问题",
                "查看游戏常见问题解答");
        drawHelpRow(
                canvas,
                top + rowHeight,
                IconType.CUSTOMER_SERVICE,
                "联系客服",
                "在线客服与账号问题");
        drawHelpRow(
                canvas,
                top + rowHeight * 2.0f,
                IconType.FEEDBACK,
                "意见反馈",
                "反馈问题与建议，帮助我们做得更好");
        drawHelpRow(
                canvas,
                top + rowHeight * 3.0f,
                IconType.ALERT,
                "举报反馈",
                "举报不良行为，维护游戏环境");
        drawHelpRow(
                canvas,
                top + rowHeight * 4.0f,
                IconType.HISTORY,
                "反馈记录",
                "查看历史反馈记录");

        setText(25.0f, BROWN_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas,
                "客服信息以官网公示为准",
                1112.0f,
                860.0f);
        setText(21.0f, MUTED_TEXT, false, Paint.Align.CENTER);
        drawBaselineCenteredText(
                canvas,
                "服务入口：www.nanbeiyule.com",
                1112.0f,
                906.0f);
    }
}
