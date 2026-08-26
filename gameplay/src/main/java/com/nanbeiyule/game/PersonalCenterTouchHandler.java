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

abstract class PersonalCenterTouchHandler extends PersonalCenterChromeRenderer {
    protected PersonalCenterTouchHandler(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
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
            invalidate();
        }
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        this.buttonClickSound =
                buttonClickSound == null ? () -> {} : buttonClickSound;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP
                || viewport == null) {
            return true;
        }
        float x =
                (event.getX() - viewport.offsetX())
                        / viewport.scaleX();
        float y =
                (event.getY() - viewport.offsetY())
                        / viewport.scaleY();
        if (layout.closeHit().contains(x, y)) {
            performClick();
            listener.onCloseRequested();
            return true;
        }
        List<PersonalCenterLayout.Box> tabs = layout.tabHits();
        for (int index = 0; index < tabs.size(); index++) {
            if (tabs.get(index).contains(x, y)) {
                performClick();
                selectedTab = index;
                invalidate();
                return true;
            }
        }
        switch (selectedTab) {
            case 0 -> handlePersonalInfoTouch(x, y);
            case 1 -> handleSecurityTouch(x, y);
            case 2 -> handlePrivacyTouch(x, y);
            case 3 -> handleSystemTouch(x, y);
            case 4 -> handleHelpTouch(x, y);
            default -> {
                return true;
            }
        }
        return true;
    }

    protected void handlePersonalInfoTouch(float x, float y) {
        if (layout.copyHit().contains(x, y)) {
            performClick();
            listener.onCopyPlayerIdRequested(
                    state.player().publicPlayerId());
        } else if (layout.avatar().contains(x, y)
                || layout.refreshAvatarHit().contains(x, y)) {
            performClick();
            if (state.capabilities().avatarRefresh()) {
                listener.onRefreshAvatarRequested();
            } else {
                listener.onUnavailableRequested("刷新头像");
            }
        } else if (layout.switchRegionHit().contains(x, y)) {
            performClick();
            if (state.capabilities().regionSwitch()) {
                listener.onSwitchRegionRequested();
            } else {
                listener.onUnavailableRequested("切换地区");
            }
        } else if (layout.switchAccountHit().contains(x, y)) {
            performClick();
            if (state.capabilities().accountSwitch()) {
                listener.onSwitchAccountRequested();
            } else {
                listener.onUnavailableRequested("切换账号");
            }
        } else if (layout.deleteAccountHit().contains(x, y)) {
            performClick();
            listener.onUnavailableRequested("账号注销");
        } else if (layout.purchaseRoomCardsHit().contains(x, y)) {
            performClick();
            listener.onUnavailableRequested("购买房卡");
        } else if (layout.purchaseDiamondsHit().contains(x, y)) {
            performClick();
            listener.onUnavailableRequested("购买钻石");
        } else if (layout.boundRoomCardsHelpHit().contains(x, y)) {
            performClick();
            listener.onUnavailableRequested("绑定房卡说明");
        } else {
            dispatchIndexedAction(
                    layout.quickActionHits(), QUICK_ACTIONS, x, y);
        }
    }

    protected void handleSecurityTouch(float x, float y) {
        dispatchIndexedAction(
                layout.securityHits(), SECURITY_ACTIONS, x, y);
    }

    protected void handlePrivacyTouch(float x, float y) {
        List<PersonalCenterLayout.Box> rows = layout.privacyHits();
        for (int index = 0; index < rows.size(); index++) {
            if (rows.get(index).contains(x, y)) {
                performClick();
                PersonalCenterPrivacySettings previous = privacy;
                privacy = privacy.toggled(index);
                invalidate();
                listener.onPrivacyChanged(previous, privacy);
                return;
            }
        }
        if (layout.privacyPolicyHit().contains(x, y)) {
            performClick();
            listener.onActionRequested(
                    PersonalCenterAction.PRIVACY_POLICY);
        }
    }

    protected void handleSystemTouch(float x, float y) {
        PersonalCenterSystemSettings updated = null;
        if (layout.musicSliderHit().contains(x, y)) {
            updated =
                    systemSettings.withMusicVolume(
                            percentageAt(x, layout.musicSliderHit()));
        } else if (layout.musicToggleHit().contains(x, y)) {
            updated =
                    systemSettings.withMusicEnabled(
                            !systemSettings.musicEnabled());
        } else if (layout.soundSliderHit().contains(x, y)) {
            updated =
                    systemSettings.withSoundVolume(
                            percentageAt(x, layout.soundSliderHit()));
        } else if (layout.soundToggleHit().contains(x, y)) {
            updated =
                    systemSettings.withSoundEnabled(
                            !systemSettings.soundEnabled());
        } else if (layout.voiceToggleHit().contains(x, y)) {
            updated =
                    systemSettings.withVoiceEnabled(
                            !systemSettings.voiceEnabled());
        } else if (layout.vibrationToggleHit().contains(x, y)) {
            updated =
                    systemSettings.withVibrationEnabled(
                            !systemSettings.vibrationEnabled());
        } else if (layout.batterySaverToggleHit().contains(x, y)) {
            updated =
                    systemSettings.withBatterySaver(
                            !systemSettings.batterySaver());
        }
        if (updated != null) {
            applySystemSettings(updated);
            return;
        }
        List<PersonalCenterLayout.Box> graphics =
                layout.graphicsQualityHits();
        for (int index = 0; index < graphics.size(); index++) {
            if (graphics.get(index).contains(x, y)) {
                applySystemSettings(
                        systemSettings.withGraphicsQuality(index));
                return;
            }
        }
        List<PersonalCenterLayout.Box> effects =
                layout.effectsQualityHits();
        for (int index = 0; index < effects.size(); index++) {
            if (effects.get(index).contains(x, y)) {
                applySystemSettings(
                        systemSettings.withEffectsQuality(index));
                return;
            }
        }
        List<PersonalCenterLayout.Box> tools =
                layout.systemToolHits();
        for (int index = 0; index < tools.size(); index++) {
            if (!tools.get(index).contains(x, y)) {
                continue;
            }
            performClick();
            listener.onActionRequested(
                    switch (index) {
                        case 0 -> PersonalCenterAction.CLEAR_CACHE;
                        case 1 -> PersonalCenterAction.NETWORK_CHECK;
                        case 2 -> PersonalCenterAction.RESOURCE_REPAIR;
                        default ->
                                throw new IllegalStateException(
                                        "Unknown system tool " + index);
                    });
            return;
        }
    }

    protected void handleHelpTouch(float x, float y) {
        dispatchIndexedAction(
                layout.helpHits(), HELP_ACTIONS, x, y);
    }

    protected void dispatchIndexedAction(
            List<PersonalCenterLayout.Box> boxes,
            PersonalCenterAction[] actions,
            float x,
            float y) {
        for (int index = 0; index < boxes.size(); index++) {
            if (boxes.get(index).contains(x, y)) {
                performClick();
                listener.onActionRequested(actions[index]);
                return;
            }
        }
    }

    protected void applySystemSettings(
            PersonalCenterSystemSettings updated) {
        performClick();
        systemSettings = updated;
        invalidate();
        listener.onSystemSettingsChanged(updated);
    }

    protected static int percentageAt(
            float x, PersonalCenterLayout.Box box) {
        float progress =
                Math.max(
                        0.0f,
                        Math.min(
                                1.0f,
                                (x - box.left()) / box.width()));
        return Math.round(progress * 100.0f);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        buttonClickSound.run();
        return true;
    }
}
