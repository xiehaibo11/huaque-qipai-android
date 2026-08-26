package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Draws the 南北娱乐 login page with controls recovered from the original Zhejiang client.
 *
 * <p>The background is the selected 南北娱乐 artwork. Buttons, service, region, agreement, and
 * age-rating graphics are unmodified sprite frames recovered from {@code LoginScene.csb} and its
 * original Cocos atlases.
 */

abstract class LoginViewState extends AdaptiveCanvasView {
    public interface OnPhoneOneTapLoginRequestedListener {
        void onPhoneOneTapLoginRequested();
    }

    public interface OnPhoneLoginRequestedListener {
        void onPhoneLoginRequested();
    }

    public interface OnWechatLoginRequestedListener {
        void onWechatLoginRequested();
    }

    public interface OnRegionSelectionRequestedListener {
        void onRegionSelectionRequested();
    }

    public interface OnAgreementCheckedChangeListener {
        void onAgreementCheckedChanged(boolean checked);
    }

    public interface OnAgreementLinkRequestedListener {
        void onAgreementLinkRequested(LoginAgreementLink link);
    }

    protected static final float PAGE_WIDTH = LoginViewportLayout.PAGE_WIDTH;
    protected static final float PAGE_HEIGHT = LoginViewportLayout.PAGE_HEIGHT;
    protected static final float CSB_WIDTH = 1920.0f;
    protected static final float CSB_HEIGHT = 1080.0f;
    protected static final float CSB_SCALE_X = PAGE_WIDTH / CSB_WIDTH;
    protected static final float CSB_SCALE_Y = PAGE_HEIGHT / CSB_HEIGHT;

    protected static final float BUTTON_Y = 544.968f - 170.0f;
    protected static final float AGREEMENT_PANEL_Y = 544.968f - 347.0f;
    protected static final float CHECKBOX_CENTER_X = 960.0f - 487.0f;
    protected static final float CHECKBOX_CENTER_Y = AGREEMENT_PANEL_Y + 20.0f;

    protected final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    protected final Paint lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    protected final Paint regionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint noticePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected final Bitmap background;
    protected final Bitmap oneTapPlate;
    protected final Bitmap oneTapLabel;
    protected final Bitmap phoneButton;
    protected final Bitmap wechatButton;
    protected final Bitmap customerService;
    protected final Bitmap location;
    protected final Bitmap chooseArea;
    protected final Bitmap gpsBackground;
    protected final Bitmap gpsLine;
    protected final Bitmap checkboxFrame;
    protected final Bitmap checkboxCheck;
    protected final Bitmap loginFrameLight;
    protected final Bitmap agreementRead;
    protected final Bitmap agreementService;
    protected final Bitmap agreementAnd;
    protected final Bitmap agreementParent;
    protected final Bitmap agreementPrivacy;
    protected final Bitmap ageRating;

    protected final RectF oneTapHit = csbRect(497.088f, BUTTON_Y, 428.0f, 163.0f);
    protected final RectF phoneHit = csbRect(972.096f, BUTTON_Y, 428.0f, 163.0f);
    protected final RectF wechatHit = csbRect(1448.064f, BUTTON_Y, 428.0f, 163.0f);
    protected final RectF customerServiceHit = csbRect(1800.0f, 972.0f, 120.0f, 135.0f);
    protected final RectF regionHit = csbRect(960.0f, 523.0f, 840.0f, 90.0f);
    protected final RectF checkboxHit =
            csbRect(CHECKBOX_CENTER_X, CHECKBOX_CENTER_Y, 125.0f, 125.0f);
    protected final RectF serviceAgreementHit = csbRect(868.0f, 222.968f, 203.0f, 55.0f);
    protected final RectF parentAgreementHit = csbRect(1158.0f, 222.968f, 280.0f, 55.0f);
    protected final RectF privacyAgreementHit = csbRect(1405.5f, 222.968f, 217.0f, 55.0f);
    protected final RectF ageRatingHit = csbRect(1737.7896f, 143.9973f, 175.0f, 220.0f);

    protected boolean agreementChecked = false;
    protected long agreementLightStartedMillis = SystemClock.uptimeMillis();
    protected String selectedRegionName;
    protected OnPhoneOneTapLoginRequestedListener phoneOneTapLoginRequestedListener;
    protected OnPhoneLoginRequestedListener phoneLoginRequestedListener;
    protected OnWechatLoginRequestedListener wechatLoginRequestedListener;
    protected OnRegionSelectionRequestedListener regionSelectionRequestedListener;
    protected OnAgreementCheckedChangeListener agreementCheckedChangeListener;
    protected OnAgreementLinkRequestedListener agreementLinkRequestedListener;
    protected Runnable buttonClickSound = () -> {};

    public LoginViewState(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(224, 247, 244));

        background = loadBitmap(R.drawable.second_loading_background);
        oneTapPlate = loadBitmap(R.drawable.login_login_btn);
        oneTapLabel = loadBitmap(R.drawable.login_login_tip);
        phoneButton = loadBitmap(R.drawable.login_btn_phone);
        wechatButton = loadBitmap(R.drawable.login_btn_weixin);
        customerService = loadBitmap(R.drawable.lobby_serviec_btn);
        location = loadBitmap(R.drawable.choose_area_logo);
        chooseArea = loadBitmap(R.drawable.login_choose_area);
        gpsBackground = loadBitmap(R.drawable.login_gps_bg);
        gpsLine = loadBitmap(R.drawable.login_gps_line);
        checkboxFrame = loadBitmap(R.drawable.login_frame);
        checkboxCheck = loadBitmap(R.drawable.login_check);
        loginFrameLight = loadBitmap(R.drawable.login_frame_light);
        agreementRead = loadBitmap(R.drawable.login_text_read);
        agreementService = loadBitmap(R.drawable.login_protocol);
        agreementAnd = loadBitmap(R.drawable.login_text_1);
        agreementParent = loadBitmap(R.drawable.login_parent);
        agreementPrivacy = loadBitmap(R.drawable.login_privacy_protocol);
        ageRating = loadBitmap(R.drawable.login_age_btn);

        Typeface typeface;
        try {
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException ignored) {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        }

        regionPaint.setTypeface(typeface);
        regionPaint.setTextSize(40.0f * CSB_SCALE_Y);
        regionPaint.setColor(Color.WHITE);
        regionPaint.setShadowLayer(1.0f, 0.0f, 1.0f, Color.rgb(82, 121, 111));

        noticePaint.setTypeface(typeface);
        noticePaint.setTextAlign(Paint.Align.CENTER);
        noticePaint.setColor(Color.rgb(67, 120, 115));
        noticePaint.setShadowLayer(1.0f, 0.0f, 1.0f, Color.rgb(229, 245, 233));

        selectedRegionName =
                getResources().getString(R.string.login_region_default_name);
        setContentDescription(getResources().getString(R.string.login_page_title));
        setFocusable(true);
    }

    public void setOnPhoneOneTapLoginRequestedListener(
            OnPhoneOneTapLoginRequestedListener listener) {
        phoneOneTapLoginRequestedListener = listener;
    }

    public void setOnPhoneLoginRequestedListener(
            OnPhoneLoginRequestedListener listener) {
        phoneLoginRequestedListener = listener;
    }

    public void setOnWechatLoginRequestedListener(
            OnWechatLoginRequestedListener listener) {
        wechatLoginRequestedListener = listener;
    }

    public void setOnRegionSelectionRequestedListener(
            OnRegionSelectionRequestedListener listener) {
        regionSelectionRequestedListener = listener;
    }

    public void setOnAgreementCheckedChangeListener(
            OnAgreementCheckedChangeListener listener) {
        agreementCheckedChangeListener = listener;
    }

    public void setOnAgreementLinkRequestedListener(
            OnAgreementLinkRequestedListener listener) {
        agreementLinkRequestedListener = listener;
    }

    public void setButtonClickSound(Runnable buttonClickSound) {
        this.buttonClickSound =
                buttonClickSound == null ? () -> {} : buttonClickSound;
    }

    boolean isAgreementChecked() {
        return agreementChecked;
    }

    public void setSelectedRegionName(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            return;
        }
        selectedRegionName = regionName;
        invalidate();
    }

    protected abstract void drawRegion(Canvas canvas);

    protected abstract void drawAgreement(Canvas canvas);

    protected abstract void drawAgreementFrameLight(Canvas canvas);

    protected abstract void drawNotices(Canvas canvas);

    protected abstract void drawNotice(Canvas canvas, int stringId, float size, float csbY);

    protected abstract void drawCsbBitmapFromLeft(
            Canvas canvas, Bitmap bitmap, float leftX, float centerY);

    protected abstract void drawCsbBitmap(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerYFromBottom,
            float width,
            float height);

    protected abstract void drawCsbBitmap(
            Canvas canvas,
            Bitmap bitmap,
            float centerX,
            float centerYFromBottom,
            float width,
            float height,
            Paint paint);

    protected abstract void drawSafeEdgeBitmap(
            Canvas canvas,
            AdaptiveViewport viewport,
            Bitmap bitmap,
            float centerX,
            float centerYFromBottom,
            float width,
            float height);

    protected abstract void showPlaceholder(int labelStringId);

    protected abstract Bitmap loadBitmap(int resourceId);

    protected static RectF csbRect(
            float centerX,
            float centerYFromBottom,
            float width,
            float height) {
        float pageCenterX = csbX(centerX);
        float pageCenterY = csbTop(centerYFromBottom);
        float pageWidth = width * CSB_SCALE_X;
        float pageHeight = height * CSB_SCALE_Y;
        return new RectF(
                pageCenterX - pageWidth / 2.0f,
                pageCenterY - pageHeight / 2.0f,
                pageCenterX + pageWidth / 2.0f,
                pageCenterY + pageHeight / 2.0f);
    }

    protected static float csbX(float value) {
        return value * CSB_SCALE_X;
    }

    protected static float csbTop(float valueFromBottom) {
        return (CSB_HEIGHT - valueFromBottom) * CSB_SCALE_Y;
    }
}
