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

/** Public login view facade backed by focused rendering and interaction layers. */
public final class LoginView extends LoginViewInteraction {
    public LoginView(Context context) {
        super(context);
    }
}
