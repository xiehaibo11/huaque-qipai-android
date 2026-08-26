package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/** Public native host used by both Android lobby entry points. */
public final class ZhejiangLobbySettingsDialog extends Dialog {
    public enum LegalLink {
        QUALIFICATION,
        USER_SERVICE,
        PRIVACY,
        PERSONAL_INFORMATION,
        THIRD_PARTY_SHARING
    }

    public interface Actions {
        void onSettingsChanged(PersonalCenterSystemSettings settings);

        void onLegalLinkRequested(LegalLink link);
    }

    private final ZhejiangLobbySettingsView settingsView;

    public ZhejiangLobbySettingsDialog(
            Context context,
            PersonalCenterSystemSettings settings,
            Actions actions) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        settingsView =
                new ZhejiangLobbySettingsView(
                        context,
                        settings,
                        new ZhejiangLobbySettingsView.Actions() {
                            @Override
                            public void onSettingsChanged(
                                    PersonalCenterSystemSettings updated) {
                                actions.onSettingsChanged(updated);
                            }

                            @Override
                            public void onLegalLinkRequested(LegalLink link) {
                                actions.onLegalLinkRequested(link);
                            }

                            @Override
                            public void onDismissRequested() {
                                dismiss();
                            }
                        });
        setContentView(
                settingsView,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            applyFullscreen(window);
        }
    }

    public void setButtonClickSound(Runnable sound) {
        settingsView.setButtonClickSound(sound);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window == null) return;
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        applyFullscreen(window);
        window.getDecorView().setSystemUiVisibility(MainActivityState.IMMERSIVE_UI_FLAGS);
    }

    private static void applyFullscreen(Window window) {
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.gravity = Gravity.TOP | Gravity.START;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(attributes);
    }
}
