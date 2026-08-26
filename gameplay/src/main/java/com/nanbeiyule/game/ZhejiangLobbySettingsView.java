package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;

/** Full-screen drawing and touch surface for the recovered lobby settings. */
@SuppressLint("ViewConstructor")
final class ZhejiangLobbySettingsView extends AdaptiveCanvasView {
    interface Actions {
        void onSettingsChanged(PersonalCenterSystemSettings settings);

        void onLegalLinkRequested(ZhejiangLobbySettingsDialog.LegalLink link);

        void onDismissRequested();
    }

    private final ZhejiangLobbySettingsRenderer renderer;
    private final Actions actions;
    private PersonalCenterSystemSettings settings;
    private int activeSlider = -1;
    private boolean outsidePressed;
    private Runnable buttonClickSound = () -> {};

    ZhejiangLobbySettingsView(
            Context context,
            PersonalCenterSystemSettings settings,
            Actions actions) {
        super(context);
        this.settings = settings == null ? PersonalCenterSystemSettings.defaults() : settings;
        this.actions = actions;
        renderer = new ZhejiangLobbySettingsRenderer(context);
        setClickable(true);
        setFocusable(true);
        setContentDescription("设置，音效、音乐、语音和出牌语音");
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(178, 0, 0, 0));
        if (getWidth() <= 0 || getHeight() <= 0) return;
        AdaptiveViewport viewport = adaptiveViewport(
                ZhejiangLobbySettingsLayout.DESIGN_WIDTH,
                ZhejiangLobbySettingsLayout.DESIGN_HEIGHT);
        int save = AdaptiveCanvasDrawing.apply(canvas, viewport.designTransform());
        renderer.draw(canvas, settings);
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) return false;
        AdaptiveViewport.Transform transform =
                adaptiveViewport(
                                ZhejiangLobbySettingsLayout.DESIGN_WIDTH,
                                ZhejiangLobbySettingsLayout.DESIGN_HEIGHT)
                        .designTransform();
        float x = transform.unmapX(event.getX());
        float y = transform.unmapY(event.getY());
        float panelX = x - ZhejiangLobbySettingsLayout.PANEL_LEFT;
        float panelY = y - ZhejiangLobbySettingsLayout.PANEL_TOP;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                activeSlider = sliderAt(panelX, panelY);
                outsidePressed = !panelContains(panelX, panelY);
                if (activeSlider >= 0) updateSlider(activeSlider, panelX);
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                if (activeSlider >= 0) updateSlider(activeSlider, panelX);
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                activeSlider = -1;
                outsidePressed = false;
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                int slider = activeSlider;
                activeSlider = -1;
                if (slider >= 0) {
                    updateSlider(slider, panelX);
                    performClick();
                    return true;
                }
                if (outsidePressed && !panelContains(panelX, panelY)) {
                    outsidePressed = false;
                    performClick();
                    actions.onDismissRequested();
                    return true;
                }
                outsidePressed = false;
                handleTap(panelX, panelY);
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    private void handleTap(float x, float y) {
        if (contains(x, y, 1000f, -45f, 1135f, 100f)) {
            clicked();
            actions.onDismissRequested();
        } else if (contains(x, y, 330f, 465f, 610f, 570f)) {
            change(settings.withMaleVoice(true));
            clicked();
        } else if (contains(x, y, 650f, 465f, 930f, 570f)) {
            change(settings.withMaleVoice(false));
            clicked();
        } else if (contains(x, y, 120f, 548f, 385f, 610f)) {
            legal(ZhejiangLobbySettingsDialog.LegalLink.QUALIFICATION);
        } else if (contains(x, y, 405f, 548f, 675f, 610f)) {
            legal(ZhejiangLobbySettingsDialog.LegalLink.USER_SERVICE);
        } else if (contains(x, y, 680f, 548f, 955f, 610f)) {
            legal(ZhejiangLobbySettingsDialog.LegalLink.PRIVACY);
        } else if (contains(x, y, 80f, 595f, 500f, 660f)) {
            legal(ZhejiangLobbySettingsDialog.LegalLink.PERSONAL_INFORMATION);
        } else if (contains(x, y, 480f, 595f, 1045f, 660f)) {
            legal(ZhejiangLobbySettingsDialog.LegalLink.THIRD_PARTY_SHARING);
        }
    }

    private void legal(ZhejiangLobbySettingsDialog.LegalLink link) {
        clicked();
        actions.onLegalLinkRequested(link);
    }

    private void clicked() {
        performClick();
        buttonClickSound.run();
    }

    private void updateSlider(int slider, float panelX) {
        int percent = ZhejiangLobbySettingsLayout.percentForSliderX(panelX);
        PersonalCenterSystemSettings updated = switch (slider) {
            case 0 -> settings.withSoundVolume(percent).withSoundEnabled(percent > 0);
            case 1 -> settings.withMusicVolume(percent).withMusicEnabled(percent > 0);
            case 2 -> settings.withVoiceVolume(percent).withVoiceEnabled(percent > 0);
            default -> settings;
        };
        change(updated);
    }

    private void change(PersonalCenterSystemSettings updated) {
        if (updated.equals(settings)) return;
        settings = updated;
        actions.onSettingsChanged(updated);
        invalidate();
    }

    private static int sliderAt(float x, float y) {
        if (x < 270f || x > 975f) return -1;
        if (y >= 105f && y <= 215f) return 0;
        if (y >= 246f && y <= 356f) return 1;
        if (y >= 372f && y <= 482f) return 2;
        return -1;
    }

    private static boolean panelContains(float x, float y) {
        return x >= 0f && x <= ZhejiangLobbySettingsLayout.PANEL_WIDTH
                && y >= 0f && y <= ZhejiangLobbySettingsLayout.PANEL_HEIGHT;
    }

    private static boolean contains(
            float x, float y, float left, float top, float right, float bottom) {
        return new RectF(left, top, right, bottom).contains(x, y);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
