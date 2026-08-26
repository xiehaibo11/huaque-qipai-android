package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Draws the recovered SettingLayer.csd with its original Zhejiang atlas frames. */
final class ZhejiangLobbySettingsRenderer {
    private static final int TEXT_COLOR = Color.rgb(205, 133, 81);

    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap panelPatch;
    private final Bitmap titleBackground;
    private final Bitmap corner;
    private final Bitmap title;
    private final Bitmap close;
    private final Bitmap sliderTrack;
    private final Bitmap sliderFill;
    private final Bitmap sliderThumb;
    private final Bitmap music;
    private final Bitmap sound;
    private final Bitmap voice;
    private final Bitmap radioBackground;
    private final Bitmap radioSelected;

    ZhejiangLobbySettingsRenderer(Context context) {
        panelPatch = load(context, R.drawable.zhejiang_settings_panel_patch);
        titleBackground = load(context, R.drawable.zhejiang_settings_title_bg);
        corner = load(context, R.drawable.zhejiang_settings_corner);
        title = load(context, R.drawable.zhejiang_settings_title);
        close = load(context, R.drawable.zhejiang_settings_close);
        sliderTrack = load(context, R.drawable.zhejiang_settings_slider_track);
        sliderFill = load(context, R.drawable.zhejiang_settings_slider_fill);
        sliderThumb = load(context, R.drawable.zhejiang_settings_slider_thumb);
        music = load(context, R.drawable.zhejiang_settings_music);
        sound = load(context, R.drawable.zhejiang_settings_sound);
        voice = load(context, R.drawable.zhejiang_settings_voice);
        radioBackground = load(context, R.drawable.com_btn_check_box1_bg);
        radioSelected = load(context, R.drawable.com_btn_check_box1);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTypeface(
                Typeface.createFromAsset(
                        context.getAssets(), "fonts/zihun_jingdian_lihei.ttf"));
    }

    void draw(Canvas canvas, PersonalCenterSystemSettings settings) {
        float left = ZhejiangLobbySettingsLayout.PANEL_LEFT;
        float top = ZhejiangLobbySettingsLayout.PANEL_TOP;
        drawNineSlice(
                canvas,
                panelPatch,
                new RectF(left + 1.2f, top + 79f, left + 1086.5f, top + 660f),
                33);

        RectF titleLeft = new RectF(left + 1f, top, left + 544.5f, top + 81f);
        RectF titleRight = new RectF(left + 543.5f, top, left + 1087f, top + 81f);
        drawBitmap(canvas, titleBackground, titleLeft);
        drawFlipped(canvas, titleBackground, titleRight, true);
        drawBitmap(canvas, title, new RectF(left + 488f, top + 10f, left + 599f, top + 71f));
        drawFlipped(
                canvas,
                corner,
                new RectF(left + 20f, top + 558f, left + 109f, top + 640f),
                true);
        drawBitmap(
                canvas,
                corner,
                new RectF(left + 978f, top + 558f, left + 1067f, top + 640f));
        drawBitmap(
                canvas,
                close,
                new RectF(left + 1018f, top - 22.1f, left + 1117f, top + 79.9f));

        drawBitmap(canvas, sound, new RectF(left + 134.8f, top + 138f, left + 219.8f, top + 182f));
        drawBitmap(canvas, music, new RectF(left + 135.1f, top + 279.5f, left + 220.1f, top + 322.5f));
        drawBitmap(canvas, voice, new RectF(left + 134.8f, top + 405f, left + 219.8f, top + 449f));

        drawSlider(canvas, top + 128f, effectiveSound(settings));
        drawSlider(canvas, top + 269f, effectiveMusic(settings));
        drawSlider(canvas, top + 395f, effectiveVoice(settings));
        drawVoiceType(canvas, settings.maleVoice());
        drawLegalLinks(canvas);
    }

    private void drawSlider(Canvas canvas, float top, int percent) {
        float left = ZhejiangLobbySettingsLayout.PANEL_LEFT
                + ZhejiangLobbySettingsLayout.SLIDER_LEFT;
        float right = left + ZhejiangLobbySettingsLayout.SLIDER_WIDTH;
        RectF destination = new RectF(left, top, right, top + 64f);
        drawBitmap(canvas, sliderTrack, destination);

        float progressRight = left + ZhejiangLobbySettingsLayout.SLIDER_WIDTH * percent / 100f;
        int save = canvas.save();
        canvas.clipRect(left, top, progressRight, top + 64f);
        drawBitmap(canvas, sliderFill, destination);
        canvas.restoreToCount(save);

        RectF thumb =
                new RectF(
                        progressRight - 47.5f,
                        top - 17.5f,
                        progressRight + 47.5f,
                        top + 81.5f);
        drawBitmap(canvas, sliderThumb, thumb);
    }

    private void drawVoiceType(Canvas canvas, boolean maleVoice) {
        float left = ZhejiangLobbySettingsLayout.PANEL_LEFT;
        float top = ZhejiangLobbySettingsLayout.PANEL_TOP;
        drawCenteredText(canvas, "出牌语音", new RectF(left + 93f, top + 486f, left + 292f, top + 552f), 52f);
        drawRadio(canvas, new RectF(left + 355.5f, top + 477.6f, left + 435.5f, top + 557.6f), maleVoice);
        drawCenteredText(canvas, "男声", new RectF(left + 446f, top + 486f, left + 570f, top + 552f), 49f);
        drawRadio(canvas, new RectF(left + 681f, top + 475.1f, left + 761f, top + 555.1f), !maleVoice);
        drawCenteredText(canvas, "女声", new RectF(left + 774f, top + 484f, left + 898f, top + 550f), 49f);
    }

    private void drawRadio(Canvas canvas, RectF bounds, boolean selected) {
        drawBitmap(canvas, radioBackground, bounds);
        if (selected) {
            drawBitmap(
                    canvas,
                    radioSelected,
                    new RectF(
                            bounds.centerX() - 37f,
                            bounds.centerY() - 36f,
                            bounds.centerX() + 37f,
                            bounds.centerY() + 36f));
        }
    }

    private void drawLegalLinks(Canvas canvas) {
        float left = ZhejiangLobbySettingsLayout.PANEL_LEFT;
        float top = ZhejiangLobbySettingsLayout.PANEL_TOP;
        drawCenteredText(canvas, "《资质登载》", new RectF(left + 130f, top + 548f, left + 370f, top + 610f), 34f);
        drawCenteredText(canvas, "《用户协议》", new RectF(left + 420f, top + 548f, left + 667f, top + 610f), 34f);
        drawCenteredText(canvas, "《隐私协议》", new RectF(left + 690f, top + 548f, left + 940f, top + 610f), 34f);
        drawCenteredText(canvas, "《个人信息收集清单》", new RectF(left + 90f, top + 596f, left + 495f, top + 654f), 31f);
        drawCenteredText(canvas, "《第三方共享个人信息清单》", new RectF(left + 480f, top + 596f, left + 1030f, top + 654f), 31f);
    }

    private void drawCenteredText(Canvas canvas, String text, RectF bounds, float size) {
        textPaint.setTextSize(size);
        textPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = bounds.centerY() - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(text, bounds.centerX(), baseline, textPaint);
    }

    private void drawNineSlice(Canvas canvas, Bitmap bitmap, RectF destination, int edge) {
        int[] source = {0, edge, bitmap.getWidth() - edge, bitmap.getWidth()};
        float[] target = {destination.left, destination.left + edge,
                destination.right - edge, destination.right};
        int[] sourceY = {0, edge, bitmap.getHeight() - edge, bitmap.getHeight()};
        float[] targetY = {destination.top, destination.top + edge,
                destination.bottom - edge, destination.bottom};
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(source[column], sourceY[row], source[column + 1], sourceY[row + 1]),
                        new RectF(target[column], targetY[row], target[column + 1], targetY[row + 1]),
                        bitmapPaint);
            }
        }
    }

    private void drawFlipped(Canvas canvas, Bitmap bitmap, RectF destination, boolean horizontal) {
        int save = canvas.save();
        canvas.scale(horizontal ? -1f : 1f, horizontal ? 1f : -1f,
                destination.centerX(), destination.centerY());
        drawBitmap(canvas, bitmap, destination);
        canvas.restoreToCount(save);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
    }

    private static int effectiveMusic(PersonalCenterSystemSettings settings) {
        return settings.musicEnabled() ? settings.musicVolume() : 0;
    }

    private static int effectiveSound(PersonalCenterSystemSettings settings) {
        return settings.soundEnabled() ? settings.soundVolume() : 0;
    }

    private static int effectiveVoice(PersonalCenterSystemSettings settings) {
        return settings.voiceEnabled() ? settings.voiceVolume() : 0;
    }

    private static Bitmap load(Context context, int resourceId) {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }
}
