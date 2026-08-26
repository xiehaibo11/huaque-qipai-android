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

abstract class PersonalCenterInteractionView extends PersonalCenterTouchHandler {
    protected PersonalCenterInteractionView(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        viewport =
                PersonalCenterLayout.fit(
                        getWidth(),
                        getHeight(),
                        adaptiveSafeInsets());
        int save = canvas.save();
        canvas.translate(viewport.offsetX(), viewport.offsetY());
        canvas.scale(viewport.scaleX(), viewport.scaleY());

        drawCreamContent(canvas);
        drawChampagneTopBar(canvas);
        drawDarkNavySidebar(canvas);
        drawFrameBorder(canvas);
        drawTitle(canvas);
        drawTabs(canvas);
        switch (selectedTab) {
            case 0 -> drawPersonalInfoPage(canvas);
            case 1 -> drawAccountSecurityPage(canvas);
            case 2 -> drawPrivacyPage(canvas);
            case 3 -> drawSystemSettingsPage(canvas);
            case 4 -> drawHelpFeedbackPage(canvas);
            default -> throw new IllegalStateException(
                    "Unknown personal-center tab " + selectedTab);
        }
        canvas.restoreToCount(save);
    }
}
