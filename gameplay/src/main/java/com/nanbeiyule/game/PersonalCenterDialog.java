package com.nanbeiyule.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

/** Full-screen owner for the original-style personal-center view. */
final class PersonalCenterDialog extends Dialog {
    private final PersonalCenterView personalCenterView;
    private final EditText phoneInput;
    private final EditText codeInput;

    PersonalCenterDialog(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            PersonalCenterView.Listener listener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        personalCenterView =
                new PersonalCenterView(
                        context,
                        state,
                        systemSettings,
                        avatarBitmap,
                        new PersonalCenterView.Listener() {
                            @Override
                            public void onCloseRequested() {
                                dismiss();
                                listener.onCloseRequested();
                            }

                            @Override
                            public void onCopyPlayerIdRequested(
                                    long publicPlayerId) {
                                listener.onCopyPlayerIdRequested(
                                        publicPlayerId);
                            }

                            @Override
                            public void onRefreshAvatarRequested() {
                                listener.onRefreshAvatarRequested();
                            }

                            @Override
                            public void onSwitchRegionRequested() {
                                listener.onSwitchRegionRequested();
                            }

                            @Override
                            public void onSwitchAccountRequested() {
                                listener.onSwitchAccountRequested();
                            }

                            @Override
                            public void onUnavailableRequested(
                                    String featureName) {
                                listener.onUnavailableRequested(
                                        featureName);
                            }

                            @Override
                            public void onActionRequested(
                                    PersonalCenterAction action) {
                                listener.onActionRequested(action);
                            }

                            @Override
                            public void onPrivacyChanged(
                                    PersonalCenterPrivacySettings previous,
                                    PersonalCenterPrivacySettings updated) {
                                listener.onPrivacyChanged(
                                        previous, updated);
                            }

                            @Override
                            public void onSystemSettingsChanged(
                                    PersonalCenterSystemSettings settings) {
                                listener.onSystemSettingsChanged(settings);
                            }
                        });
        phoneInput = phoneEditor(context, "请输入......", 11);
        codeInput = phoneEditor(context, "", 6);
        phoneInput.setContentDescription("请输入新的手机号");
        codeInput.setContentDescription("请输入短信验证码");
        PhoneOverlayLayout content =
                new PhoneOverlayLayout(
                        context, personalCenterView, phoneInput, codeInput);
        personalCenterView.setPhoneEditorsAttached(true);
        personalCenterView.setTabObserver(content::showPhoneEditors);
        setContentView(
                content,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            WindowManager.LayoutParams attributes =
                    window.getAttributes();
            attributes.dimAmount = 0.62f;
            window.setAttributes(attributes);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        window.getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    void setAvatarBitmap(Bitmap bitmap) {
        personalCenterView.setAvatarBitmap(bitmap);
    }

    void setButtonClickSound(Runnable buttonClickSound) {
        personalCenterView.setButtonClickSound(buttonClickSound);
    }

    void selectSystemSettings() {
        personalCenterView.selectTab(PersonalCenterView.SYSTEM_SETTINGS_TAB);
    }

    void selectPhoneBinding() {
        personalCenterView.selectTab(PersonalCenterView.PHONE_BINDING_TAB);
    }

    void setPrivacySettings(
            PersonalCenterPrivacySettings settings) {
        personalCenterView.setPrivacySettings(settings);
    }

    void setSystemSettings(
            PersonalCenterSystemSettings settings) {
        personalCenterView.setSystemSettings(settings);
    }

    String phoneNumber() {
        return phoneInput.getText().toString();
    }

    String verificationCode() {
        return codeInput.getText().toString();
    }

    void setPhoneCodeSeconds(int seconds) {
        personalCenterView.setPhoneCodeSeconds(seconds);
    }

    void clearPhoneCode() {
        codeInput.setText("");
    }

    private static EditText phoneEditor(
            Context context, String hint, int maxLength) {
        EditText editor = new EditText(context);
        editor.setSingleLine(true);
        editor.setHint(hint);
        editor.setHintTextColor(0xFFAE895D);
        editor.setTextColor(0xFFFDF9F5);
        editor.setInputType(InputType.TYPE_CLASS_NUMBER);
        editor.setFilters(
                new InputFilter[] {
                    new InputFilter.LengthFilter(maxLength)
                });
        editor.setBackgroundColor(Color.TRANSPARENT);
        editor.setPadding(0, 0, 0, 0);
        editor.setTextSize(TypedValue.COMPLEX_UNIT_PX, 42f);
        editor.setTypeface(loadTypeface(context));
        editor.setContentDescription(hint);
        return editor;
    }

    private static Typeface loadTypeface(Context context) {
        try {
            return Typeface.createFromAsset(
                    context.getAssets(), "fonts/zihun_jingdian_lihei.ttf");
        } catch (RuntimeException ignored) {
            return Typeface.DEFAULT;
        }
    }

    private static final class PhoneOverlayLayout extends FrameLayout {
        private static final float DESIGN_WIDTH = 1920f;
        private static final float DESIGN_HEIGHT = 1080f;
        private final EditText phone;
        private final EditText code;

        PhoneOverlayLayout(
                Context context,
                PersonalCenterView view,
                EditText phone,
                EditText code) {
            super(context);
            this.phone = phone;
            this.code = code;
            addView(view, matchParent());
            addView(phone);
            addView(code);
        }

        void showPhoneEditors(int tab) {
            int visibility = tab == 3 ? View.VISIBLE : View.GONE;
            phone.setVisibility(visibility);
            code.setVisibility(visibility);
            if (visibility == View.GONE) {
                phone.clearFocus();
                code.clearFocus();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            float scale = scale();
            measureEditor(phone, PersonalCenterPhoneLayout.PHONE_INPUT, scale);
            measureEditor(code, PersonalCenterPhoneLayout.CODE_INPUT, scale);
        }

        @Override
        protected void onLayout(
                boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            float scale = scale();
            float offsetX = (getWidth() - DESIGN_WIDTH * scale) / 2f;
            float offsetY = (getHeight() - DESIGN_HEIGHT * scale) / 2f;
            layoutEditor(
                    phone,
                    PersonalCenterPhoneLayout.PHONE_INPUT,
                    scale,
                    offsetX,
                    offsetY);
            layoutEditor(
                    code,
                    PersonalCenterPhoneLayout.CODE_INPUT,
                    scale,
                    offsetX,
                    offsetY);
        }

        private float scale() {
            return Math.min(
                    getMeasuredWidth() / DESIGN_WIDTH,
                    getMeasuredHeight() / DESIGN_HEIGHT);
        }

        private static void measureEditor(
                EditText editor, ShopLayout.Rect rect, float scale) {
            editor.setTextSize(TypedValue.COMPLEX_UNIT_PX, 42f * scale);
            editor.setPadding(Math.round(30f * scale), 0, 0, 0);
            editor.measure(
                    MeasureSpec.makeMeasureSpec(
                            Math.round(rect.width() * scale),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(
                            Math.round(rect.height() * scale),
                            MeasureSpec.EXACTLY));
        }

        private static void layoutEditor(
                EditText editor,
                ShopLayout.Rect rect,
                float scale,
                float offsetX,
                float offsetY) {
            int left = Math.round(offsetX + rect.left() * scale);
            int top = Math.round(offsetY + rect.top() * scale);
            editor.layout(
                    left,
                    top,
                    left + editor.getMeasuredWidth(),
                    top + editor.getMeasuredHeight());
        }

        private static LayoutParams matchParent() {
            return new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    }
}
