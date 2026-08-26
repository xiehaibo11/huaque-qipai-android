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

abstract class LoginViewInteraction extends LoginViewRenderer {
    protected LoginViewInteraction(Context context) {
        super(context);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return super.onTouchEvent(event);
        }

        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        LoginViewportLayout layout =
                LoginViewportLayout.calculate(
                        getWidth(),
                        getHeight(),
                        adaptiveSafeInsets());
        AdaptiveViewport viewport = layout.adaptiveViewport();
        float pageX = layout.toPageX(event.getX());
        float pageY = layout.toPageY(event.getY());
        float customerServiceX =
                pageX
                        - viewport.safeEdgeOffsetX(
                                customerServiceHit.centerX());
        float ageRatingX =
                pageX
                        - viewport.safeEdgeOffsetX(
                                ageRatingHit.centerX());

        if (checkboxHit.contains(pageX, pageY)) {
            agreementChecked = !agreementChecked;
            if (!agreementChecked) {
                agreementLightStartedMillis = SystemClock.uptimeMillis();
            }
            if (agreementCheckedChangeListener != null) {
                agreementCheckedChangeListener.onAgreementCheckedChanged(agreementChecked);
            }
            invalidate();
            performClick();
            return true;
        }
        if (oneTapHit.contains(pageX, pageY)) {
            if (!agreementChecked) {
                Toast.makeText(
                                getContext(),
                                R.string.login_agreement_required,
                                Toast.LENGTH_SHORT)
                        .show();
            } else if (phoneOneTapLoginRequestedListener != null) {
                phoneOneTapLoginRequestedListener.onPhoneOneTapLoginRequested();
            } else {
                showPlaceholder(R.string.login_phone_one_tap);
            }
        } else if (phoneHit.contains(pageX, pageY)) {
            if (!agreementChecked) {
                Toast.makeText(
                                getContext(),
                                R.string.login_agreement_required,
                                Toast.LENGTH_SHORT)
                        .show();
            } else if (phoneLoginRequestedListener != null) {
                phoneLoginRequestedListener.onPhoneLoginRequested();
            } else {
                showPlaceholder(R.string.login_phone);
            }
        } else if (wechatHit.contains(pageX, pageY)) {
            if (!agreementChecked) {
                Toast.makeText(
                                getContext(),
                                R.string.login_agreement_required,
                                Toast.LENGTH_SHORT)
                        .show();
            } else if (wechatLoginRequestedListener != null) {
                wechatLoginRequestedListener.onWechatLoginRequested();
            } else {
                showPlaceholder(R.string.login_wechat);
            }
        } else if (customerServiceHit.contains(customerServiceX, pageY)) {
            showPlaceholder(R.string.login_customer_service);
        } else if (regionHit.contains(pageX, pageY)) {
            if (regionSelectionRequestedListener != null) {
                regionSelectionRequestedListener.onRegionSelectionRequested();
            } else {
                showPlaceholder(R.string.login_region_taizhou);
            }
        } else if (serviceAgreementHit.contains(pageX, pageY)) {
            if (agreementLinkRequestedListener != null) {
                agreementLinkRequestedListener.onAgreementLinkRequested(LoginAgreementLink.SERVICE);
            } else {
                showPlaceholder(R.string.login_service_agreement);
            }
        } else if (parentAgreementHit.contains(pageX, pageY)) {
            if (agreementLinkRequestedListener != null) {
                agreementLinkRequestedListener.onAgreementLinkRequested(LoginAgreementLink.GUARDIANSHIP);
            } else {
                showPlaceholder(R.string.login_parent_guardianship);
            }
        } else if (privacyAgreementHit.contains(pageX, pageY)) {
            if (agreementLinkRequestedListener != null) {
                agreementLinkRequestedListener.onAgreementLinkRequested(LoginAgreementLink.PRIVACY);
            } else {
                showPlaceholder(R.string.login_privacy_policy);
            }
        } else if (ageRatingHit.contains(ageRatingX, pageY)) {
            showPlaceholder(R.string.login_age_notice);
        } else {
            return false;
        }
        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        buttonClickSound.run();
        return true;
    }
}
