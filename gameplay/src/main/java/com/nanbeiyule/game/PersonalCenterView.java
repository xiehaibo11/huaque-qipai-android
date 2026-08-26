package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.IntConsumer;

/** Zhejiang lobby personal center rebuilt on the original 1920x1080 CSD coordinates. */
@SuppressLint("ViewConstructor")
final class PersonalCenterView extends View {
    interface Listener {
        void onCloseRequested();
        void onCopyPlayerIdRequested(long publicPlayerId);
        void onRefreshAvatarRequested();
        void onSwitchRegionRequested();
        void onSwitchAccountRequested();
        void onUnavailableRequested(String featureName);
        void onActionRequested(PersonalCenterAction action);
        void onPrivacyChanged(
                PersonalCenterPrivacySettings previous,
                PersonalCenterPrivacySettings updated);
        void onSystemSettingsChanged(PersonalCenterSystemSettings settings);
    }

    static final int SYSTEM_SETTINGS_TAB = 2;
    static final int PHONE_BINDING_TAB = 3;
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;
    private static final RectF PANEL = new RectF(211.65f, 46.6f, 1710.65f, 968.6f);
    private static final String[] TABS = {
        "个人资料", "健康系统认证", "隐私权限", "手机换绑", "会员中心"
    };
    private static final int BROWN = Color.rgb(113, 65, 36);
    private static final int MUTED = Color.rgb(154, 117, 82);
    private static final int ORANGE = Color.rgb(213, 92, 48);
    private static final int TEAL = Color.rgb(37, 150, 150);
    private static final int PHONE_TEXT = Color.rgb(205, 133, 81);
    private static final int PHONE_HINT = Color.rgb(174, 137, 93);
    private static final int PHONE_TIP = Color.rgb(192, 156, 130);
    private static final int PHONE_BOUND_LABEL = Color.rgb(185, 115, 69);
    private static final int PHONE_BOUND_VALUE = Color.rgb(194, 108, 70);
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneId.systemDefault());

    private final PersonalCenterState state;
    private final Listener listener;
    private final Bitmap panelBitmap;
    private final Bitmap titleBitmap;
    private final Bitmap closeBitmap;
    private final Bitmap selectedTabBitmap;
    private final Bitmap normalTabBitmap;
    private final Bitmap avatarFrameBitmap;
    private final Bitmap roomCardBitmap;
    private final Bitmap boundRoomCardBitmap;
    private final Bitmap diamondBitmap;
    private final Bitmap addBitmap;
    private final Bitmap copyBitmap;
    private final Bitmap helpBitmap;
    private final Bitmap switchAccountBitmap;
    private final Bitmap switchRegionBitmap;
    private final Bitmap verifiedBitmap;
    private final Bitmap alipayBitmap;
    private final Bitmap completeBitmap;
    private final Bitmap healthInfoBitmap;
    private final Bitmap healthCheckBitmap;
    private final Bitmap healthCheckBgBitmap;
    private final Bitmap healthInputBitmap;
    private final Bitmap privacySettingsBitmap;
    private final Bitmap privacyToggleBitmap;
    private final Bitmap privacyToggleOffBitmap;
    private final Bitmap phoneInputBitmap;
    private final Bitmap phoneSendCodeBitmap;
    private final Bitmap vipCardBitmap;
    private final Bitmap vipLevelNormalBitmap;
    private final Bitmap vipLevelOneBitmap;
    private final Bitmap vipLevelTwoBitmap;
    private final Bitmap vipLevelThreeBitmap;
    private final Bitmap vipArrowBitmap;
    private final Bitmap vipInfoBitmap;
    private final Bitmap vipCurrentBitmap;
    private final Bitmap vipRechargeBitmap;
    private final Bitmap vipGetAwardBitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Typeface typeface;
    private Bitmap avatarBitmap;
    private PersonalCenterPrivacySettings privacy;
    private PersonalCenterSystemSettings systemSettings;
    private Runnable buttonClickSound = () -> {};
    private int selectedTab;
    private int shownMembershipLevel;
    private int phoneCodeSeconds;
    private boolean phoneEditorsAttached;
    private IntConsumer tabObserver = ignored -> {};
    private float scale = 1.0f;
    private float offsetX;
    private float offsetY;

    PersonalCenterView(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context);
        this.state = state;
        this.listener = listener;
        this.systemSettings = systemSettings;
        this.privacy = state.privacy();
        this.shownMembershipLevel = Math.max(0, Math.min(3, state.membership().level()));
        this.avatarBitmap = avatarBitmap;
        panelBitmap = BitmapFactory.decodeResource(
                getResources(), R.drawable.zhejiang_personal_center_bg);
        titleBitmap = load(R.drawable.zj_pc_title);
        closeBitmap = load(R.drawable.zj_pc_close);
        selectedTabBitmap = load(R.drawable.zj_pc_tab_selected);
        normalTabBitmap = load(R.drawable.zj_pc_tab_normal);
        avatarFrameBitmap = load(R.drawable.zj_pc_avatar_frame);
        roomCardBitmap = load(R.drawable.zj_pc_room_card);
        boundRoomCardBitmap = load(R.drawable.zj_pc_bound_room_card);
        diamondBitmap = load(R.drawable.zj_pc_diamond);
        addBitmap = load(R.drawable.zj_pc_add);
        copyBitmap = load(R.drawable.zj_pc_copy);
        helpBitmap = load(R.drawable.zj_pc_help);
        switchAccountBitmap = load(R.drawable.zj_pc_switch_account);
        switchRegionBitmap = load(R.drawable.zj_pc_switch_region);
        verifiedBitmap = load(R.drawable.zj_pc_health_verified);
        alipayBitmap = load(R.drawable.zj_pc_health_alipay);
        completeBitmap = load(R.drawable.zj_pc_complete);
        healthInfoBitmap = load(R.drawable.zj_pc_health_info);
        healthCheckBitmap = load(R.drawable.zj_pc_health_check);
        healthCheckBgBitmap = load(R.drawable.zj_pc_health_check_bg);
        healthInputBitmap = load(R.drawable.zj_pc_health_input);
        privacySettingsBitmap = load(R.drawable.zj_pc_privacy_settings);
        privacyToggleBitmap = load(R.drawable.zj_pc_privacy_toggle);
        privacyToggleOffBitmap = load(R.drawable.zj_pc_privacy_toggle_off);
        phoneInputBitmap = load(R.drawable.zj_pc_phone_input);
        phoneSendCodeBitmap = load(R.drawable.zj_pc_phone_send_code);
        vipCardBitmap = load(R.drawable.zj_pc_vip_card_bg);
        vipLevelNormalBitmap = load(R.drawable.zj_pc_vip_level_normal);
        vipLevelOneBitmap = load(R.drawable.zj_pc_vip_level_1);
        vipLevelTwoBitmap = load(R.drawable.zj_pc_vip_level_2);
        vipLevelThreeBitmap = load(R.drawable.zj_pc_vip_level_3);
        vipArrowBitmap = load(R.drawable.zj_pc_vip_arrow);
        vipInfoBitmap = load(R.drawable.zj_pc_vip_info_bg);
        vipCurrentBitmap = load(R.drawable.zj_pc_vip_current);
        vipRechargeBitmap = load(R.drawable.zj_pc_vip_recharge);
        vipGetAwardBitmap = load(R.drawable.zj_pc_vip_get_award);
        typeface = loadTypeface(context);
        text.setTypeface(typeface);
        setClickable(true);
        setFocusable(true);
        setContentDescription("个人中心");
    }

    void selectTab(int index) {
        if (index >= 0 && index < TABS.length) {
            selectedTab = index;
            tabObserver.accept(index);
            invalidate();
        }
    }

    void setTabObserver(IntConsumer observer) {
        tabObserver = observer == null ? ignored -> {} : observer;
        tabObserver.accept(selectedTab);
    }

    void setPhoneEditorsAttached(boolean attached) {
        phoneEditorsAttached = attached;
        invalidate();
    }

    void setPhoneCodeSeconds(int seconds) {
        phoneCodeSeconds = Math.max(0, seconds);
        invalidate();
    }

    void setAvatarBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            avatarBitmap = bitmap;
            invalidate();
        }
    }

    void setPrivacySettings(PersonalCenterPrivacySettings settings) {
        if (settings != null) {
            privacy = settings;
            invalidate();
        }
    }

    void setSystemSettings(PersonalCenterSystemSettings settings) {
        if (settings != null) {
            systemSettings = settings;
        }
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        scale = Math.min(getWidth() / DESIGN_WIDTH, getHeight() / DESIGN_HEIGHT);
        offsetX = (getWidth() - DESIGN_WIDTH * scale) / 2.0f;
        offsetY = (getHeight() - DESIGN_HEIGHT * scale) / 2.0f;
        int save = canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);
        canvas.drawBitmap(panelBitmap, null, PANEL, paint);
        drawTitleAndClose(canvas);
        drawTabs(canvas);
        switch (selectedTab) {
            case 0 -> drawProfile(canvas);
            case 1 -> drawHealth(canvas);
            case 2 -> drawPrivacy(canvas);
            case 3 -> drawPhone(canvas);
            case 4 -> drawMembership(canvas);
            default -> throw new IllegalStateException("Unknown personal center tab");
        }
        canvas.restoreToCount(save);
    }

    private void drawTitleAndClose(Canvas canvas) {
        bitmap(canvas, titleBitmap, new RectF(843, 96, 1079, 167));
        bitmap(canvas, closeBitmap, new RectF(1584, 84, 1683, 186));
    }

    private void drawTabs(Canvas canvas) {
        for (int i = 0; i < TABS.length; i++) {
            float top = 207 + i * 112;
            RectF tab = new RectF(237, top, 552, top + 96);
            bitmap(canvas, selectedTab == i ? selectedTabBitmap : normalTabBitmap, tab);
            label(canvas, TABS[i], tab.centerX(), tab.centerY() + 2,
                    i == 1 ? 31 : 35,
                    selectedTab == i ? Color.WHITE : 0xFF403124,
                    Paint.Align.CENTER, true);
        }
    }

    private void drawProfile(Canvas canvas) {
        RectF avatar = new RectF(695, 228, 859, 392);
        if (avatarBitmap != null && !avatarBitmap.isRecycled()) {
            canvas.save();
            Path clip = new Path();
            clip.addRect(new RectF(704, 237, 850, 383), Path.Direction.CW);
            canvas.clipPath(clip);
            canvas.drawBitmap(avatarBitmap, null, new RectF(704, 237, 850, 383), paint);
            canvas.restore();
        }
        bitmap(canvas, avatarFrameBitmap, avatar);
        label(canvas, ellipsize(state.player().displayName()), 777, 424, 31, BROWN,
                Paint.Align.CENTER, true);
        label(canvas, "序号:" + state.player().publicPlayerId(), 763, 471, 27, BROWN,
                Paint.Align.CENTER, false);
        bitmap(canvas, copyBitmap, new RectF(890, 431, 1011, 509));
        walletRow(canvas, 263, "购买房卡:", state.wallet().purchasedRoomCards(), false);
        walletRow(canvas, 377, "绑定房卡:", state.wallet().boundRoomCards(), true);
        diamondRow(canvas, 491, state.wallet().diamonds());
        bitmap(canvas, switchAccountBitmap, new RectF(703, 708, 969, 808));
        bitmap(canvas, switchRegionBitmap, new RectF(1115, 708, 1381, 808));
        label(canvas, "账号注销", 817, 875, 29, MUTED, Paint.Align.CENTER, false);
        label(canvas, "刷新头像", 1262, 875, 29, MUTED, Paint.Align.CENTER, false);
    }

    private void walletRow(
            Canvas canvas, float y, String title, long value, boolean bound) {
        bitmap(canvas, roomCardBitmap, new RectF(1055, y - 36, 1183, y + 36));
        if (bound) {
            bitmap(canvas, boundRoomCardBitmap, new RectF(1132, y - 1, 1182, y + 42));
        }
        label(canvas, title, 1190, y, 31, BROWN, Paint.Align.LEFT, true);
        label(canvas, Long.toString(value), 1410, y, 31, BROWN, Paint.Align.LEFT, true);
        if (bound) {
            bitmap(canvas, helpBitmap, new RectF(1529, y - 36, 1602, y + 36));
        } else {
            bitmap(canvas, addBitmap, new RectF(1529, y - 37, 1601, y + 37));
        }
    }

    private void diamondRow(Canvas canvas, float y, long value) {
        bitmap(canvas, diamondBitmap, new RectF(1075, y - 41, 1163, y + 41));
        label(canvas, "钻石:", 1190, y, 31, BROWN, Paint.Align.LEFT, true);
        label(canvas, Long.toString(value), 1410, y, 31, BROWN, Paint.Align.LEFT, true);
        bitmap(canvas, addBitmap, new RectF(1529, y - 37, 1601, y + 37));
    }

    private void drawHealth(Canvas canvas) {
        PersonalCenterState.HealthCertification health = state.healthCertification();
        boolean verified = "VERIFIED".equals(health.status());
        label(canvas, "如认证有问题，请联系客服解决", 1085, 283, 28, 0xFFC9A77D,
                Paint.Align.CENTER, false);
        inputLine(canvas, 628, 368, "真实姓名",
                verified ? health.realNameMasked() : "请输入您的真实姓名");
        inputLine(canvas, 628, 470, "身份证号",
                verified ? health.idCardMasked() : "请输入您的身份证号");
        bitmap(canvas, healthCheckBgBitmap, new RectF(742, 575, 803, 635));
        bitmap(canvas, healthCheckBitmap, new RectF(743, 579, 803, 632));
        label(canvas, "手机号一键绑定", 833, 598, 30, 0xFFB99362,
                Paint.Align.LEFT, true);
        label(canvas, "(为持续向您提供服务)", 833, 647, 26, 0xFFC5A57B,
                Paint.Align.LEFT, false);
        bitmap(canvas, healthInfoBitmap, new RectF(1312, 300, 1661, 824));
        bitmap(canvas, alipayBitmap, new RectF(1336, 690, 1637, 821));
        bitmap(canvas, completeBitmap, new RectF(967, 732, 1202, 837));
        if (verified) {
            bitmap(canvas, verifiedBitmap, new RectF(967, 447, 1320, 730));
        }
        label(canvas, "根据《关于防止未成年人沉迷网络游戏的通知》，网络游戏用户需使用有效身份证件",
                1110, 887, 21, 0xFFD4A675, Paint.Align.CENTER, false);
        label(canvas, "进行认证。为保证流畅游戏体验，享受健康游戏生活，请广大玩家尽快完成认证。",
                1110, 920, 21, 0xFFD4A675, Paint.Align.CENTER, false);
    }

    private void drawPrivacy(Canvas canvas) {
        privacyRow(canvas, 263, "设备麦克风权限", true, true);
        privacyRow(canvas, 393, "设备剪切板权限", false, privacy.clipboardAccessEnabled());
        privacyRow(canvas, 523, "设备定位权限", true, false);
        privacyRow(canvas, 653, "设备信息权限", true, false);
        privacyRow(canvas, 783, "设备存储权限", true, false);
    }

    private void privacyRow(
            Canvas canvas, float y, String title, boolean settingsButton, boolean enabled) {
        RectF row = new RectF(640, y - 50, 1600, y + 50);
        paint.setColor(0xFFFFF8E2);
        canvas.drawRoundRect(row, 28, 28, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(0xFFF0E3BD);
        canvas.drawRoundRect(row, 28, 28, paint);
        paint.setStyle(Paint.Style.FILL);
        label(canvas, title, 683, y, 34, 0xFFA76D46, Paint.Align.LEFT, true);
        label(canvas, "使用说明", 1096, y, 34, 0xFF987D5D, Paint.Align.LEFT, true);
        if (settingsButton) {
            bitmap(canvas, privacySettingsBitmap, new RectF(1403, y - 36, 1555, y + 36));
        } else {
            if (enabled) {
                bitmap(canvas, privacyToggleBitmap, new RectF(1394, y - 40, 1563, y + 39));
            } else {
                bitmap(canvas, privacyToggleOffBitmap, new RectF(1394, y - 40, 1563, y + 39));
            }
        }
    }

    private void drawPhone(Canvas canvas) {
        if (state.account().phoneBound()) {
            label(canvas, "您已绑定手机号，可进行换绑",
                    PersonalCenterPhoneLayout.REBIND_TIP_X,
                    PersonalCenterPhoneLayout.REBIND_TIP_Y,
                    36, PHONE_TIP, Paint.Align.CENTER, false);
        } else {
            label(canvas, "为了确保房卡/话费等奖品发送成功，也为了您的账号安全，",
                    PersonalCenterPhoneLayout.REBIND_TIP_X, 292f,
                    36, PHONE_TIP, Paint.Align.CENTER, false);
            label(canvas, "请先绑定您的手机号",
                    PersonalCenterPhoneLayout.REBIND_TIP_X, 334f,
                    36, PHONE_TIP, Paint.Align.CENTER, false);
        }
        phoneInput(canvas,
                PersonalCenterPhoneLayout.PHONE_INPUT,
                PersonalCenterPhoneLayout.PHONE_LABEL_Y,
                "手机号", phoneEditorsAttached ? "" : "请输入......");
        phoneInput(canvas,
                PersonalCenterPhoneLayout.CODE_INPUT,
                PersonalCenterPhoneLayout.CODE_LABEL_Y,
                "验证码", "");
        bitmap(canvas, phoneSendCodeBitmap, rect(PersonalCenterPhoneLayout.SEND_CODE));
        if (phoneCodeSeconds > 0) {
            RectF sendCode = rect(PersonalCenterPhoneLayout.SEND_CODE);
            paint.setColor(0xFF55BDB5);
            canvas.drawRoundRect(sendCode, 42, 42, paint);
            label(canvas, phoneCodeSeconds + "秒", sendCode.centerX(), sendCode.centerY(),
                    42, Color.WHITE, Paint.Align.CENTER, true);
        }
        bitmap(canvas, completeBitmap, rect(PersonalCenterPhoneLayout.COMPLETE));
        if (state.account().phoneBound()) {
            label(canvas, "已绑手机：",
                    PersonalCenterPhoneLayout.BOUND_LABEL_X,
                    PersonalCenterPhoneLayout.BOUND_PHONE_Y,
                    40, PHONE_BOUND_LABEL, Paint.Align.CENTER, false);
            label(canvas, state.account().maskedPhone(),
                    PersonalCenterPhoneLayout.BOUND_VALUE_X,
                    PersonalCenterPhoneLayout.BOUND_PHONE_Y,
                    40, PHONE_BOUND_VALUE, Paint.Align.LEFT, false);
        }
    }

    private void drawMembership(Canvas canvas) {
        PersonalCenterState.Membership membership = state.membership();
        RectF card = new RectF(748, 275, 1529, 635);
        bitmap(canvas, vipCardBitmap, card);
        bitmap(canvas, membershipLevelBitmap(), new RectF(790, 325, 1053, 556));
        bitmapMirrored(canvas, vipArrowBitmap, new RectF(635, 493, 700, 636));
        bitmap(canvas, vipArrowBitmap, new RectF(1560, 493, 1625, 636));
        if (shownMembershipLevel == membership.level()) {
            bitmap(canvas, vipCurrentBitmap, new RectF(725, 250, 888, 408));
        }
        label(canvas, membershipName(), 1250, 372, 42,
                0xFF3B9E73, Paint.Align.CENTER, true);
        label(canvas, membershipCondition(), 1250, 445, 30, BROWN, Paint.Align.CENTER, true);
        bitmap(canvas, vipInfoBitmap, new RectF(748, 650, 1519, 850));
        label(canvas, "【等级特权】：", 770, 690, 29, 0xFFBD4A2D, Paint.Align.LEFT, true);
        label(canvas, membershipPrivilege(), 770, 750, 27, BROWN, Paint.Align.LEFT, false);
        if (membership.active() && shownMembershipLevel == membership.level()) {
            label(canvas, "有效期至 " + formatDate(membership.expiresAt()),
                    930, 835, 25, MUTED, Paint.Align.CENTER, false);
            bitmap(canvas, vipGetAwardBitmap, new RectF(1040, 795, 1307, 895));
            bitmap(canvas, vipRechargeBitmap, new RectF(1350, 802, 1513, 889));
        }
    }

    private Bitmap membershipLevelBitmap() {
        return switch (shownMembershipLevel) {
            case 1 -> vipLevelOneBitmap;
            case 2 -> vipLevelTwoBitmap;
            case 3 -> vipLevelThreeBitmap;
            default -> vipLevelNormalBitmap;
        };
    }

    private String membershipName() {
        return switch (shownMembershipLevel) {
            case 1 -> "尊享会员";
            case 2 -> "豪华会员";
            case 3 -> "至尊会员";
            default -> "普通会员";
        };
    }

    private String membershipCondition() {
        return switch (shownMembershipLevel) {
            case 1 -> "开通会员即可升级";
            case 2 -> "累计会员成长值达到二级";
            case 3 -> "累计会员成长值达到三级";
            default -> "注册即可成为普通会员";
        };
    }

    private String membershipPrivilege() {
        return switch (shownMembershipLevel) {
            case 1 -> "每日会员礼包、专属头像框";
            case 2 -> "进阶每日礼包、房卡福利、专属头像框";
            case 3 -> "至尊每日礼包、房卡福利、专属头像框";
            default -> "无";
        };
    }

    private void inputLine(Canvas canvas, float x, float y, String title, String hint) {
        label(canvas, title, x, y, 38, 0xFFC39158, Paint.Align.LEFT, true);
        RectF box = new RectF(x + 205, y - 39, x + 626, y + 39);
        bitmap(canvas, healthInputBitmap, box);
        label(canvas, hint, box.left + 30, box.centerY(), 27, 0xFFC7AA7B,
                Paint.Align.LEFT, false);
    }

    private void phoneInput(
            Canvas canvas, ShopLayout.Rect bounds, float labelY, String title, String hint) {
        label(canvas, title, PersonalCenterPhoneLayout.LABEL_X, labelY,
                46, PHONE_TEXT, Paint.Align.CENTER, false);
        RectF box = rect(bounds);
        bitmapHorizontalNineSlice(canvas, phoneInputBitmap, box, 86);
        if (!hint.isEmpty()) {
            label(canvas, hint, box.left + 30, box.centerY(),
                    42, PHONE_HINT, Paint.Align.LEFT, false);
        }
    }

    private static RectF rect(ShopLayout.Rect source) {
        return new RectF(source.left(), source.top(), source.right(), source.bottom());
    }

    private void bitmapHorizontalNineSlice(
            Canvas canvas, Bitmap bitmap, RectF destination, int capWidth) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        int sourceWidth = bitmap.getWidth();
        int sourceHeight = bitmap.getHeight();
        float right = destination.right - capWidth;
        canvas.drawBitmap(bitmap,
                new Rect(0, 0, capWidth, sourceHeight),
                new RectF(destination.left, destination.top,
                        destination.left + capWidth, destination.bottom), paint);
        canvas.drawBitmap(bitmap,
                new Rect(capWidth, 0, sourceWidth - capWidth, sourceHeight),
                new RectF(destination.left + capWidth, destination.top, right, destination.bottom),
                paint);
        canvas.drawBitmap(bitmap,
                new Rect(sourceWidth - capWidth, 0, sourceWidth, sourceHeight),
                new RectF(right, destination.top, destination.right, destination.bottom), paint);
    }

    private Bitmap load(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    private void bitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, null, destination, paint);
        }
    }

    private void bitmapMirrored(Canvas canvas, Bitmap bitmap, RectF destination) {
        int save = canvas.save();
        canvas.scale(-1.0f, 1.0f, destination.centerX(), destination.centerY());
        bitmap(canvas, bitmap, destination);
        canvas.restoreToCount(save);
    }

    private void label(
            Canvas canvas, String value, float x, float centerY, float size,
            int color, Paint.Align align, boolean bold) {
        text.setTextSize(size);
        text.setColor(color);
        text.setTextAlign(align);
        text.setTypeface(bold ? Typeface.create(typeface, Typeface.BOLD) : typeface);
        Paint.FontMetrics metrics = text.getFontMetrics();
        canvas.drawText(value, x, centerY - (metrics.ascent + metrics.descent) / 2.0f, text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP || scale <= 0) {
            return true;
        }
        float x = (event.getX() - offsetX) / scale;
        float y = (event.getY() - offsetY) / scale;
        if (inside(x, y, 1570, 70, 1680, 180)) {
            clicked();
            listener.onCloseRequested();
            return true;
        }
        for (int i = 0; i < TABS.length; i++) {
            if (inside(x, y, 225, 198 + i * 112, 565, 311 + i * 112)) {
                clicked();
                selectedTab = i;
                tabObserver.accept(i);
                invalidate();
                return true;
            }
        }
        if (selectedTab == 0 && touchProfile(x, y)) {
            return true;
        }
        PersonalCenterAction action =
                PersonalCenterActionMap.actionFor(selectedTab, x, y);
        if (action != null) {
            clicked();
            handleMappedAction(action);
        }
        return true;
    }

    private boolean touchProfile(float x, float y) {
        if (inside(x, y, 885, 425, 1025, 510)) {
            clicked();
            listener.onCopyPlayerIdRequested(state.player().publicPlayerId());
        } else if (inside(x, y, 675, 205, 880, 410)
                || inside(x, y, 1150, 830, 1375, 920)) {
            clicked();
            listener.onRefreshAvatarRequested();
        } else if (inside(x, y, 670, 690, 1000, 825)) {
            clicked();
            listener.onSwitchAccountRequested();
        } else if (inside(x, y, 1080, 690, 1420, 825)) {
            clicked();
            listener.onSwitchRegionRequested();
        } else {
            return false;
        }
        return true;
    }

    private void handleMappedAction(PersonalCenterAction action) {
        if (action == PersonalCenterAction.PHONE_SEND_CODE
                && phoneCodeSeconds > 0) {
            return;
        }
        if (action == PersonalCenterAction.TOGGLE_CLIPBOARD_PERMISSION) {
            PersonalCenterPrivacySettings previous = privacy;
            privacy = privacy.withClipboardAccessEnabled(!privacy.clipboardAccessEnabled());
            invalidate();
            listener.onPrivacyChanged(previous, privacy);
            return;
        }
        if (action == PersonalCenterAction.MEMBERSHIP_PREVIOUS) {
            shownMembershipLevel = Math.max(0, shownMembershipLevel - 1);
            invalidate();
            return;
        }
        if (action == PersonalCenterAction.MEMBERSHIP_NEXT) {
            shownMembershipLevel = Math.min(3, shownMembershipLevel + 1);
            invalidate();
            return;
        }
        listener.onActionRequested(action);
    }

    private void clicked() {
        performClick();
        buttonClickSound.run();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private static boolean inside(
            float x, float y, float left, float top, float right, float bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    private static String ellipsize(String value) {
        return value.length() <= 10 ? value : value.substring(0, 8) + "…";
    }

    private static String formatDate(String instant) {
        try {
            return DATE.format(Instant.parse(instant));
        } catch (RuntimeException ignored) {
            return "--";
        }
    }

    private static Typeface loadTypeface(Context context) {
        try {
            return Typeface.createFromAsset(
                    context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        } catch (RuntimeException ignored) {
            return Typeface.DEFAULT;
        }
    }
}
