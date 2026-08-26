package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

/** Frame host that keeps native text input aligned to the old CSB fields. */
final class MatchArenaCreateContent extends FrameLayout {
    private final MatchArenaCreateState state = MatchArenaCreateState.original900023();
    private final MatchArenaCreateView artView;
    private final EditText remark;
    private final EditText dailyLimit;
    private final EditText initialCards;
    private final EditText autoTransferCustom;
    private final EditText reminderCustom;
    private MatchArenaCreateState.Mode lastMode = MatchArenaCreateState.Mode.LEADER;
    private boolean modalVisible;
    private Runnable buttonClickSound = () -> {};
    private OriginalMessageBoxDialog messageDialog;

    MatchArenaCreateContent(
            Context context,
            long purchasedRoomCards,
            Runnable dismiss,
            MatchArenaCreateDialog.Listener listener) {
        super(context);
        setClipChildren(false);
        state.setPurchasedRoomCards(purchasedRoomCards);
        remark = field(state.remark(), true, 40);
        dailyLimit = field(state.dailyRoomCardLimit(), false, 40);
        initialCards = field(state.initialRoomCards(), false, 40);
        autoTransferCustom = field(state.autoTransferCustomValue(), false, 39);
        reminderCustom = field(state.lowCardReminderCustomValue(), false, 39);
        artView =
                new MatchArenaCreateView(
                        context,
                        state,
                        purchasedRoomCards,
                        new MatchArenaCreateView.Actions() {
                            @Override public void onCloseRequested() { dismiss.run(); }

                            @Override public void onSubmitRequested() {
                                commitInputs();
                                String error = state.validate();
                                if (error != null) {
                                    showError(error);
                                    return;
                                }
                                hideKeyboardFocus();
                                listener.onCreateRequested(state);
                            }

                            @Override public void onModeChanged() { updateEnabledFields(); }

                            @Override public void onConfigurationChanged() {
                                updateEnabledFields();
                            }

                            @Override public void onCustomInputRequested(boolean autoTransfer) {
                                updateEnabledFields();
                                EditText target = autoTransfer ? autoTransferCustom : reminderCustom;
                                target.post(() -> focusCustomField(target));
                            }

                            @Override public void onModalChanged(boolean visible) {
                                modalVisible = visible;
                                updateEnabledFields();
                            }
                        });
        addView(artView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(remark);
        addView(dailyLimit);
        addView(initialCards);
        addView(autoTransferCustom);
        addView(reminderCustom);
        watchAutoTransferCustomValue();
        updateEnabledFields();
    }

    void setSubmitting(boolean submitting) {
        state.setSubmitting(submitting);
        updateEnabledFields();
        artView.invalidate();
    }

    void showError(String message) {
        state.setSubmitting(false);
        updateEnabledFields();
        if (message != null && message.startsWith("!")) {
            artView.showError(message);
        } else {
            showMessageBox(message);
        }
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
        artView.setButtonClickSound(buttonClickSound);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        artView.layout(0, 0, getWidth(), getHeight());
        layoutField(remark, 820, 740, 318, 55, 40);
        layoutField(dailyLimit, 820, 615, 318, 55, 40);
        layoutField(initialCards, 820, 490, 318, 55, 40);
        layoutField(autoTransferCustom, 1574.5f, 365, 125, 55, 39);
        layoutField(reminderCustom, 1574.5f, 255, 125, 55, 39);
    }

    private EditText field(String value, boolean decimal, float textSize) {
        EditText field = new EditText(getContext());
        field.setText(value);
        field.setTextColor(0xfffffaeb);
        field.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        field.setTypeface(
                android.graphics.Typeface.createFromAsset(
                        getContext().getAssets(), "fonts/fangzhengcuyuan.ttf"));
        field.setSingleLine(true);
        field.setIncludeFontPadding(false);
        field.setGravity(Gravity.CENTER);
        field.setPadding(4, 0, 4, 0);
        field.setBackgroundColor(Color.TRANSPARENT);
        field.setSelectAllOnFocus(false);
        field.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | (decimal ? InputType.TYPE_NUMBER_FLAG_DECIMAL : 0));
        return field;
    }

    private void layoutField(
            EditText field,
            float centerX,
            float centerY,
            float width,
            float height,
            float textSize) {
        float scale = Math.min(getWidth() / 1920f, getHeight() / 1080f);
        float offsetX = (getWidth() - 1920 * scale) / 2f;
        float offsetY = (getHeight() - 1080 * scale) / 2f;
        int x = Math.round(offsetX + (centerX - width / 2) * scale);
        int y = Math.round(offsetY + (1080 - centerY - height / 2) * scale);
        field.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * scale);
        field.layout(x, y, x + Math.round(width * scale), y + Math.round(height * scale));
    }

    private void commitInputs() {
        state.setRemark(remark.getText().toString());
        state.setDailyRoomCardLimit(dailyLimit.getText().toString());
        state.setInitialRoomCards(initialCards.getText().toString());
        state.setAutoTransferCustomValue(autoTransferCustom.getText().toString());
        state.setLowCardReminderCustomValue(reminderCustom.getText().toString());
    }

    private void updateEnabledFields() {
        boolean editable = !state.isSubmitting();
        boolean fieldsVisible = editable && !modalVisible;
        if (lastMode != state.mode()) {
            initialCards.setText(state.initialRoomCards());
            dailyLimit.setText(state.dailyRoomCardLimit());
            lastMode = state.mode();
        }
        if (state.mode() != MatchArenaCreateState.Mode.LEADER) {
            dailyLimit.setText("888888");
        }
        if (state.mode() == MatchArenaCreateState.Mode.LOBBY_CARD) {
            initialCards.setText("0");
        }
        remark.setEnabled(fieldsVisible);
        remark.setVisibility(fieldsVisible ? VISIBLE : INVISIBLE);
        dailyLimit.setEnabled(fieldsVisible && state.mode() == MatchArenaCreateState.Mode.LEADER);
        initialCards.setEnabled(fieldsVisible && state.mode() != MatchArenaCreateState.Mode.LOBBY_CARD);
        dailyLimit.setVisibility(dailyLimit.isEnabled() ? VISIBLE : INVISIBLE);
        initialCards.setVisibility(initialCards.isEnabled() ? VISIBLE : INVISIBLE);
        boolean remodelVisible = state.mode() != MatchArenaCreateState.Mode.LOBBY_CARD;
        autoTransferCustom.setEnabled(
                fieldsVisible
                        && remodelVisible
                        && state.autoTransferEnabled()
                        && state.autoTransferUsesCustomValue());
        reminderCustom.setEnabled(
                fieldsVisible
                        && remodelVisible
                        && state.lowCardReminderEnabled()
                        && state.lowCardReminderUsesCustomValue());
        autoTransferCustom.setVisibility(autoTransferCustom.isEnabled() ? VISIBLE : INVISIBLE);
        reminderCustom.setVisibility(reminderCustom.isEnabled() ? VISIBLE : INVISIBLE);
        artView.invalidate();
    }

    private void hideKeyboardFocus() {
        remark.clearFocus();
        dailyLimit.clearFocus();
        initialCards.clearFocus();
        autoTransferCustom.clearFocus();
        reminderCustom.clearFocus();
    }

    private void focusCustomField(EditText field) {
        if (!field.isEnabled()) return;
        field.requestFocus();
        InputMethodManager keyboard =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.showSoftInput(field, InputMethodManager.SHOW_IMPLICIT);
    }

    private void watchAutoTransferCustomValue() {
        autoTransferCustom.addTextChangedListener(
                new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable value) {
                        state.setAutoTransferCustomValue(value.toString());
                        if (state.autoTransferEnabled()
                                && state.autoTransferUsesCustomValue()
                                && value.length() > 0) {
                            artView.validateAutoTransferSelection();
                        }
                    }
                });
    }

    private void showMessageBox(String message) {
        if (messageDialog != null && messageDialog.isShowing()) return;
        messageDialog =
                new OriginalMessageBoxDialog(
                        getContext(),
                        message == null || message.isBlank() ? "操作失败" : message,
                        () -> messageDialog = null);
        messageDialog.setButtonClickSound(buttonClickSound);
        messageDialog.show();
    }
}
