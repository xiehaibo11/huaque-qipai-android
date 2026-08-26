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

abstract class PersonalCenterViewState extends AdaptiveCanvasView {
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

        void onSystemSettingsChanged(
                PersonalCenterSystemSettings settings);
    }

    protected enum IconType {
        PERSON,
        SHIELD,
        PRIVACY,
        SETTINGS,
        LOCK,
        HELP,
        PHONE,
        ID_CARD,
        DEVICE,
        GROWTH,
        MEDAL,
        FAVORITE,
        GIFT,
        MESSAGE,
        MUSIC,
        SOUND,
        MICROPHONE,
        VIBRATION,
        CACHE,
        NETWORK,
        REPAIR,
        FAQ,
        CUSTOMER_SERVICE,
        FEEDBACK,
        ALERT,
        HISTORY
    }

    protected static final String[] TAB_TITLES = {
        "个人信息",
        "账号安全",
        "隐私设置",
        "系统设置",
        "帮助反馈"
    };
    static final int SYSTEM_SETTINGS_TAB = 3;
    protected static final PersonalCenterAction[] QUICK_ACTIONS = {
        PersonalCenterAction.MY_RECORDS,
        PersonalCenterAction.FAVORITES,
        PersonalCenterAction.GIFTS,
        PersonalCenterAction.MESSAGES
    };
    protected static final PersonalCenterAction[] SECURITY_ACTIONS = {
        PersonalCenterAction.PHONE_BINDING,
        PersonalCenterAction.LOGIN_PASSWORD,
        PersonalCenterAction.PAYMENT_PASSWORD,
        PersonalCenterAction.REAL_NAME,
        PersonalCenterAction.DEVICE_MANAGEMENT
    };
    protected static final PersonalCenterAction[] HELP_ACTIONS = {
        PersonalCenterAction.FAQ,
        PersonalCenterAction.CUSTOMER_SERVICE,
        PersonalCenterAction.FEEDBACK,
        PersonalCenterAction.REPORT,
        PersonalCenterAction.FEEDBACK_HISTORY
    };

    protected static final int NAVY_TOP = Color.rgb(27, 41, 55);
    protected static final int NAVY_BOTTOM = Color.rgb(16, 29, 42);
    protected static final int NAVY_LIGHT = Color.rgb(50, 65, 78);
    protected static final int CHAMPAGNE_LIGHT = Color.rgb(255, 233, 181);
    protected static final int CHAMPAGNE = Color.rgb(232, 188, 107);
    protected static final int CHAMPAGNE_DARK = Color.rgb(171, 111, 37);
    protected static final int GOLD = Color.rgb(203, 150, 64);
    protected static final int GOLD_DARK = Color.rgb(139, 88, 26);
    protected static final int CREAM = Color.rgb(255, 249, 239);
    protected static final int CREAM_CARD = Color.rgb(255, 252, 246);
    protected static final int BROWN_TEXT = Color.rgb(101, 63, 32);
    protected static final int MUTED_TEXT = Color.rgb(150, 117, 83);
    protected static final int GREEN = Color.rgb(76, 169, 91);
    protected static final int BLUE = Color.rgb(55, 89, 181);

    protected final PersonalCenterState state;
    protected final Listener listener;
    protected final PersonalCenterLayout layout = new PersonalCenterLayout();
    protected final Bitmap roomCardIcon;
    protected final Bitmap diamondIcon;
    protected final Bitmap addIcon;
    protected final EnumMap<IconType, Bitmap> personalCenterIcons =
            new EnumMap<>(IconType.class);
    protected final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint textPaint =
            new Paint(
                    Paint.ANTI_ALIAS_FLAG
                            | Paint.SUBPIXEL_TEXT_FLAG);
    protected final Paint bitmapPaint =
            new Paint(
                    Paint.ANTI_ALIAS_FLAG
                            | Paint.FILTER_BITMAP_FLAG);
    protected final Path reusablePath = new Path();
    protected final Typeface typeface;

    protected Bitmap avatarBitmap;
    protected int selectedTab;
    protected PersonalCenterPrivacySettings privacy;
    protected PersonalCenterSystemSettings systemSettings;
    protected PersonalCenterLayout.Viewport viewport;
    protected Runnable buttonClickSound = () -> {};

    void selectTab(int index) {
        if (index < 0 || index >= TAB_TITLES.length) {
            throw new IllegalArgumentException("Unknown personal-center tab " + index);
        }
        selectedTab = index;
        invalidate();
    }

    PersonalCenterViewState(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context);
        this.state = state;
        this.listener = listener;
        privacy = state.privacy();
        this.systemSettings =
                systemSettings == null
                        ? PersonalCenterSystemSettings.defaults()
                        : systemSettings;
        this.avatarBitmap =
                avatarBitmap == null || avatarBitmap.isRecycled()
                        ? AvatarFrameRenderer.loadDefaultAvatar(getResources())
                        : avatarBitmap;
        roomCardIcon =
                BitmapFactory.decodeResource(
                        getResources(), R.drawable.home_icon_room_card);
        diamondIcon =
                BitmapFactory.decodeResource(
                        getResources(), R.drawable.home_icon_diamond);
        addIcon =
                BitmapFactory.decodeResource(
                        getResources(), R.drawable.home_icon_add);
        loadPersonalCenterIcons();
        typeface = loadTypeface();
        strokePaint.setStyle(Paint.Style.STROKE);
        setFocusable(true);
        setClickable(true);
        setContentDescription(
                context.getString(R.string.personal_center_accessibility));
    }

    protected void loadPersonalCenterIcons() {
        loadPersonalCenterIcon(
                IconType.PERSON, R.drawable.personal_icon_person);
        loadPersonalCenterIcon(
                IconType.SHIELD, R.drawable.personal_icon_shield);
        loadPersonalCenterIcon(
                IconType.PRIVACY, R.drawable.personal_icon_privacy);
        loadPersonalCenterIcon(
                IconType.SETTINGS, R.drawable.personal_icon_settings);
        loadPersonalCenterIcon(
                IconType.HELP, R.drawable.personal_icon_help);
        loadPersonalCenterIcon(
                IconType.PHONE, R.drawable.personal_icon_phone);
        loadPersonalCenterIcon(
                IconType.LOCK, R.drawable.personal_icon_lock);
        loadPersonalCenterIcon(
                IconType.ID_CARD, R.drawable.personal_icon_id_card);
        loadPersonalCenterIcon(
                IconType.DEVICE, R.drawable.personal_icon_device);
        loadPersonalCenterIcon(
                IconType.GROWTH, R.drawable.personal_icon_growth);
        loadPersonalCenterIcon(
                IconType.MEDAL, R.drawable.personal_icon_medal);
        loadPersonalCenterIcon(
                IconType.FAVORITE, R.drawable.personal_icon_favorite);
        loadPersonalCenterIcon(
                IconType.GIFT, R.drawable.personal_icon_gift);
        loadPersonalCenterIcon(
                IconType.MESSAGE, R.drawable.personal_icon_message);
        loadPersonalCenterIcon(
                IconType.MUSIC, R.drawable.personal_icon_music);
        loadPersonalCenterIcon(
                IconType.SOUND, R.drawable.personal_icon_sound);
        loadPersonalCenterIcon(
                IconType.MICROPHONE, R.drawable.personal_icon_microphone);
        loadPersonalCenterIcon(
                IconType.VIBRATION, R.drawable.personal_icon_vibration);
        loadPersonalCenterIcon(
                IconType.CACHE, R.drawable.personal_icon_cache);
        loadPersonalCenterIcon(
                IconType.NETWORK, R.drawable.personal_icon_network);
        loadPersonalCenterIcon(
                IconType.REPAIR, R.drawable.personal_icon_repair);
        loadPersonalCenterIcon(
                IconType.FAQ, R.drawable.personal_icon_faq);
        loadPersonalCenterIcon(
                IconType.CUSTOMER_SERVICE,
                R.drawable.personal_icon_customer_service);
        loadPersonalCenterIcon(
                IconType.FEEDBACK, R.drawable.personal_icon_feedback);
        loadPersonalCenterIcon(
                IconType.ALERT, R.drawable.personal_icon_alert);
        loadPersonalCenterIcon(
                IconType.HISTORY, R.drawable.personal_icon_history);
    }

    protected void loadPersonalCenterIcon(
            IconType type,
            int drawableResource) {
        Bitmap bitmap =
                BitmapFactory.decodeResource(
                        getResources(), drawableResource);
        if (bitmap != null && !bitmap.isRecycled()) {
            personalCenterIcons.put(type, bitmap);
        }
    }
    protected Typeface loadTypeface() {
        try {
            return Typeface.createFromAsset(
                    getContext().getAssets(),
                    "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException ignored) {
            return Typeface.DEFAULT;
        }
    }

    protected static RectF rect(PersonalCenterLayout.Box box) {
        return new RectF(
                box.left(), box.top(), box.right(), box.bottom());
    }

    protected static String ellipsizeName(String value) {
        if (value.length() <= 12) {
            return value;
        }
        return value.substring(0, 10) + "..";
    }
}
