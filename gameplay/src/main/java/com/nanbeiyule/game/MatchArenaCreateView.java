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
import android.view.MotionEvent;
import android.view.View;

/** Old TeaHouseSetupView.csb initial-create page; not the newer edit layout. */
final class MatchArenaCreateView extends View {
    interface Actions {
        void onCloseRequested();
        void onSubmitRequested();
        void onModeChanged();
        void onConfigurationChanged();
        void onCustomInputRequested(boolean autoTransfer);
        void onModalChanged(boolean visible);
    }

    private final MatchArenaCreateState state;
    private final long purchasedRoomCards;
    private final Actions actions;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Typeface typeface;
    private final Bitmap panel;
    private final Bitmap baseTab;
    private final Bitmap close;
    private final Bitmap input;
    private final Bitmap inputDisabled;
    private final Bitmap arrowDown;
    private final Bitmap arrowUp;
    private final Bitmap costSelect;
    private final Bitmap sure;
    private final Bitmap cancel;
    private final Bitmap separator;
    private final Bitmap dropdownBackground;
    private final Bitmap dropdownLine;
    private final Bitmap popupBackground;
    private final Bitmap popupTitle;
    private final Bitmap checkbox;
    private final Bitmap checkboxSelected;
    private final Bitmap popupSure;
    private final MatchArenaCreateRemodelRenderer remodelRenderer;
    private boolean modeDropdown;
    private boolean costPopup;
    private boolean autoHelp;
    private boolean reminderHelp;
    private boolean buyCardHelp;
    private boolean chargeCardHelp;
    private boolean remodelErrorAuto;
    private String errorMessage = "";
    private Runnable buttonClickSound = () -> {};
    private final Runnable hideAutoHelp = () -> { autoHelp = false; invalidate(); };
    private final Runnable hideReminderHelp = () -> { reminderHelp = false; invalidate(); };
    private final Runnable hideRemodelError = () -> { errorMessage = ""; invalidate(); };

    MatchArenaCreateView(
            Context context,
            MatchArenaCreateState state,
            long purchasedRoomCards,
            Actions actions) {
        super(context);
        this.state = state;
        this.purchasedRoomCards = purchasedRoomCards;
        this.actions = actions;
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        panel = bitmap(R.drawable.tea_house_setup_bg);
        baseTab = bitmap(R.drawable.teahouse_setup_btn_base_on);
        close = bitmap(R.drawable.teahouse_setup_btn_off);
        input = bitmap(R.drawable.teahouse_setup_input_bg1);
        inputDisabled = bitmap(R.drawable.teahouse_setup_input_bg2);
        arrowDown = bitmap(R.drawable.teahouse_setup_btn_arrow_down);
        arrowUp = bitmap(R.drawable.teahouse_setup_btn_arrow_up);
        costSelect = bitmap(R.drawable.teahouse_setup_costtype_btn_select);
        sure = bitmap(R.drawable.teahouse_setup_btn_sure);
        cancel = bitmap(R.drawable.teahouse_setup_btn_cancel);
        separator = bitmap(R.drawable.teahouse_setup_line_3);
        dropdownBackground = bitmap(R.drawable.teahouse_setup_img_bg1);
        dropdownLine = bitmap(R.drawable.teahouse_setup_line_1);
        popupBackground = bitmap(R.drawable.tea_house_tipslayer_bg);
        popupTitle = bitmap(R.drawable.teahouse_setup_costtype_title);
        checkbox = bitmap(R.drawable.teahouse_setup_checkbox);
        checkboxSelected = bitmap(R.drawable.teahouse_setup_checkbox_select);
        popupSure = bitmap(R.drawable.teahouse_setup_costtype_btn_sure);
        remodelRenderer = new MatchArenaCreateRemodelRenderer(context);
        setFocusable(true);
    }

    void showError(String message) {
        errorMessage = message == null ? "" : message;
        remodelErrorAuto = state.autoTransferValidationError() != null;
        invalidate();
    }

    void validateAutoTransferSelection() {
        rejectInvalidAutoTransfer();
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0x99000000);
        canvas.save();
        float scale = viewportScale();
        canvas.translate((getWidth() - 1920 * scale) / 2f, (getHeight() - 1080 * scale) / 2f);
        canvas.scale(scale, scale);
        draw(canvas, panel, new RectF(206, 141, 1714, 1049));
        draw(canvas, baseTab, new RectF(230, 47, 624, 145));
        draw(canvas, close, new RectF(1645.5f, 119, 1744.5f, 221));
        draw(canvas, separator, new RectF(227, 283, 1693, 286));
        drawBaseForm(canvas);
        remodelRenderer.draw(
                canvas,
                state,
                errorMessage,
                autoHelp,
                reminderHelp,
                buyCardHelp,
                chargeCardHelp,
                remodelErrorAuto);
        if (modeDropdown) {
            drawModeDropdown(canvas);
        }
        if (costPopup) {
            drawCostPopup(canvas);
        }
        canvas.restore();
    }

    private void drawBaseForm(Canvas canvas) {
        if (state.mode() != MatchArenaCreateState.Mode.LOBBY_CARD) {
            text(canvas, "购买房卡总库存：" + purchasedRoomCards, 330, 270, 34, 0xffa36f48, Paint.Align.LEFT);
        }

        label(canvas, "比赛场备注：", 330, 350);
        drawInput(canvas, 820, 740, 340, 72);
        text(canvas, "仅允许填写数字", 820, 405, 30, 0xffa27654, Paint.Align.CENTER);

        label(canvas, "每日消耗上限：", 330, 475);
        drawInput(canvas, 820, 615, 340, 72, state.mode() != MatchArenaCreateState.Mode.LEADER);
        if (state.mode() != MatchArenaCreateState.Mode.LEADER) {
            text(canvas, "仅领队模式可用", 820, 477, 40, Color.WHITE, Paint.Align.CENTER);
        }

        label(canvas, "比赛场充值：", 330, 600);
        drawInput(canvas, 820, 490, 340, 72, state.mode() == MatchArenaCreateState.Mode.LOBBY_CARD);
        if (state.mode() == MatchArenaCreateState.Mode.LOBBY_CARD) {
            text(canvas, "当前模式不可用", 820, 602, 40, Color.WHITE, Paint.Align.CENTER);
        } else {
            text(canvas, "充值房卡到此比赛场", 820, 654, 27, 0xffa27654, Paint.Align.CENTER);
        }

        label(canvas, "比赛场模式：", 1015, 350);
        drawInput(canvas, 1460, 740, 340, 72);
        text(canvas, state.mode().label, 1440, 362, 40, 0xfffffaeb, Paint.Align.CENTER);
        draw(canvas, modeDropdown ? arrowUp : arrowDown, cocosRect(1591, 738, 74, 72));

        draw(canvas, costSelect, cocosRect(1150, 625, 274, 102));
        text(
                canvas,
                state.costType() == null ? "未选择消耗模式" : "已选择消耗模式",
                1150,
                536,
                30,
                0xffa05b38,
                Paint.Align.CENTER);

        draw(canvas, cancel, cocosRect(695, 140, 239, 103));
        draw(canvas, sure, cocosRect(1240, 140, 239, 103));
        if (!errorMessage.isBlank() && !errorMessage.startsWith("!")) {
            text(canvas, errorMessage, 960, 870, 31, 0xffd33c2f, Paint.Align.CENTER);
        }
        if (state.isSubmitting()) {
            paint.setColor(0x66000000);
            canvas.drawRect(206, 141, 1714, 1049, paint);
            text(canvas, "正在创建比赛场…", 960, 650, 38, Color.WHITE, Paint.Align.CENTER);
        }
    }

    private void drawModeDropdown(Canvas canvas) {
        float top = 375;
        drawNine(canvas, dropdownBackground, new RectF(1305, top, 1615, top + 270), 100, 65, 100, 65);
        for (int index = 0; index < MatchArenaCreateState.MODES_900023.size(); index++) {
            MatchArenaCreateState.Mode mode = MatchArenaCreateState.MODES_900023.get(index);
            float rowTop = top + index * 65;
            text(canvas, mode.label, 1460, rowTop + 44, 30, 0xff8d5738, Paint.Align.CENTER);
            draw(canvas, dropdownLine, new RectF(1315, rowTop + 64, 1605, rowTop + 67));
        }
    }

    private void drawCostPopup(Canvas canvas) {
        paint.setColor(0x99000000);
        canvas.drawRect(0, 0, 1920, 1080, paint);
        draw(canvas, popupBackground, new RectF(416.5f, 210, 1503.5f, 870));
        draw(canvas, popupTitle, new RectF(859.5f, 207.5f, 1060.5f, 270.5f));
        draw(canvas, close, new RectF(1417.5f, 186, 1516.5f, 288));
        if (state.mode() != MatchArenaCreateState.Mode.CIRCULATION) {
            costOption(canvas, MatchArenaCreateState.CostType.CHAMPION, 590, 660);
            costOption(canvas, MatchArenaCreateState.CostType.AA, 1040, 660);
        } else {
            costOption(canvas, MatchArenaCreateState.CostType.AA, 590, 660);
        }
        draw(canvas, popupSure, cocosRect(958.41f, 320.25f, 301, 131));
    }

    private void costOption(Canvas canvas, MatchArenaCreateState.CostType value, float x, float y) {
        RectF box = new RectF(x, 1080 - y - 40, x + 76, 1080 - y + 40);
        draw(canvas, checkbox, box);
        if (state.pendingCostType() == value) {
            draw(canvas, checkboxSelected, new RectF(x, 1080 - y - 33, x + 76, 1080 - y + 33));
        }
        String label = value == MatchArenaCreateState.CostType.CHAMPION ? "冠军消耗" : "平摊消耗";
        text(canvas, label, x + 110, 1080 - y + 11, 34, 0xff8d5738, Paint.Align.LEFT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP || state.isSubmitting()) {
            return true;
        }
        float scale = viewportScale();
        float x = (event.getX() - (getWidth() - 1920 * scale) / 2f) / scale;
        float y = (event.getY() - (getHeight() - 1080 * scale) / 2f) / scale;
        if (costPopup) {
            return handleCostTap(x, y);
        }
        if (modeDropdown) {
            int index = dropdownIndex(x, y);
            if (index >= 0) {
                state.setMode(MatchArenaCreateState.MODES_900023.get(index));
                modeDropdown = false;
                errorMessage = "";
                buttonClickSound.run();
                actions.onModeChanged();
                invalidate();
                return true;
            }
            modeDropdown = false;
            invalidate();
            return true;
        }
        if (new RectF(1645.5f, 119, 1744.5f, 221).contains(x, y)
                || cocosRect(695, 140, 239, 103).contains(x, y)) {
            buttonClickSound.run();
            actions.onCloseRequested();
        } else if (cocosRect(1460, 740, 340, 72).contains(x, y)) {
            buttonClickSound.run();
            modeDropdown = true;
            invalidate();
        } else if (cocosRect(1150, 625, 274, 102).contains(x, y)) {
            buttonClickSound.run();
            state.openCostEditor();
            costPopup = true;
            actions.onModalChanged(true);
            invalidate();
        } else if (cocosRect(1240, 140, 239, 103).contains(x, y)) {
            buttonClickSound.run();
            actions.onSubmitRequested();
        } else if (handleRemodelTap(x, y)) {
            return true;
        }
        return true;
    }

    private int dropdownIndex(float x, float y) {
        if (x < 1320 || x > 1600) return -1;
        for (int index = 0; index < MatchArenaCreateState.MODES_900023.size(); index++) {
            float centerY = 407.5f + index * 65;
            if (y >= centerY - 25 && y <= centerY + 25) return index;
        }
        return -1;
    }

    private boolean handleRemodelTap(float x, float y) {
        if (state.mode() == MatchArenaCreateState.Mode.LOBBY_CARD) return false;
        if (cocosRect(780, 829, 73, 74).contains(x, y)) {
            buyCardHelp = !buyCardHelp;
            buttonClickSound.run();
            invalidate();
            return true;
        } else if (cocosRect(1029.5233f, 490, 73, 74).contains(x, y)) {
            chargeCardHelp = !chargeCardHelp;
            buttonClickSound.run();
            invalidate();
            return true;
        } else if (cocosRect(368, 365, 76, 80).contains(x, y)) {
            boolean enable = !state.autoTransferEnabled();
            state.setAutoTransferEnabled(enable);
            if (enable && rejectInvalidAutoTransfer()) return true;
        } else if (cocosRect(368, 255, 76, 80).contains(x, y)) {
            state.setLowCardReminderEnabled(!state.lowCardReminderEnabled());
        } else if (cocosRect(640, 365, 73, 74).contains(x, y)) {
            showHelp(true);
            return true;
        } else if (cocosRect(640, 255, 73, 74).contains(x, y)) {
            showHelp(false);
            return true;
        } else if (state.autoTransferEnabled() && selectAmount(x, y, true, 365)) {
            return true;
        } else if (state.lowCardReminderEnabled() && selectAmount(x, y, false, 255)) {
            return true;
        } else {
            return false;
        }
        changed();
        return true;
    }

    private boolean selectAmount(float x, float y, boolean auto, float centerY) {
        long[] values = {100, 500, 1000, 2000};
        float[] centers = {740, 920, 1100, 1280};
        for (int index = 0; index < centers.length; index++) {
            if (cocosRect(centers[index], centerY, 80, 80).contains(x, y)) {
                if (auto) state.selectAutoTransferPreset(values[index]);
                else state.selectLowCardReminderPreset(values[index]);
                if (auto && rejectInvalidAutoTransfer()) return true;
                changed();
                return true;
            }
        }
        if (new RectF(1420, 1080 - centerY - 40, 1649, 1080 - centerY + 40).contains(x, y)) {
            if (auto) state.selectAutoTransferCustom();
            else state.selectLowCardReminderCustom();
            changed();
            actions.onCustomInputRequested(auto);
            return true;
        }
        return false;
    }

    private void changed() {
        errorMessage = "";
        buttonClickSound.run();
        actions.onConfigurationChanged();
        invalidate();
    }

    private void showHelp(boolean auto) {
        Runnable timeout = auto ? hideAutoHelp : hideReminderHelp;
        removeCallbacks(timeout);
        boolean showing = auto ? autoHelp : reminderHelp;
        if (auto) autoHelp = !showing;
        else reminderHelp = !showing;
        buttonClickSound.run();
        invalidate();
        if (!showing) postDelayed(timeout, auto ? 5000 : 3000);
    }

    private boolean rejectInvalidAutoTransfer() {
        String error = state.autoTransferValidationError();
        if (error == null) return false;
        state.setAutoTransferEnabled(false);
        errorMessage = error;
        remodelErrorAuto = true;
        removeCallbacks(hideRemodelError);
        postDelayed(hideRemodelError, 5000);
        buttonClickSound.run();
        actions.onConfigurationChanged();
        invalidate();
        return true;
    }

    private boolean handleCostTap(float x, float y) {
        if (new RectF(1417.5f, 186, 1516.5f, 288).contains(x, y)) {
            state.cancelCostEditor();
            costPopup = false;
            actions.onModalChanged(false);
        } else if (new RectF(590, 380, 850, 460).contains(x, y)
                && state.mode() != MatchArenaCreateState.Mode.CIRCULATION) {
            state.selectPendingCostType(MatchArenaCreateState.CostType.CHAMPION);
        } else if (new RectF(1040, 380, 1300, 460).contains(x, y)
                && state.mode() != MatchArenaCreateState.Mode.CIRCULATION) {
            state.selectPendingCostType(MatchArenaCreateState.CostType.AA);
        } else if (cocosRect(958.41f, 320.25f, 301, 131).contains(x, y)) {
            state.confirmCostEditor();
            costPopup = false;
            actions.onModalChanged(false);
            errorMessage = "";
        }
        buttonClickSound.run();
        invalidate();
        return true;
    }

    private void label(Canvas canvas, String value, float x, float baseline) {
        text(canvas, value, x, baseline, 50, 0xff8d5738, Paint.Align.LEFT);
    }

    private void drawInput(Canvas canvas, float centerX, float centerY, float width, float height) {
        drawInput(canvas, centerX, centerY, width, height, false);
    }

    private void drawInput(
            Canvas canvas,
            float centerX,
            float centerY,
            float width,
            float height,
            boolean disabled) {
        drawNine(
                canvas,
                disabled ? inputDisabled : input,
                cocosRect(centerX, centerY, width, height),
                16,
                16,
                16,
                16);
    }

    private void text(Canvas canvas, String value, float x, float baseline, float size, int color, Paint.Align align) {
        paint.setTypeface(typeface);
        paint.setTextSize(size);
        paint.setTextAlign(align);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(value, x, baseline, paint);
    }

    private Bitmap bitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    private void draw(Canvas canvas, Bitmap bitmap, RectF destination) {
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, null, destination, paint);
    }

    private void drawNine(
            Canvas canvas,
            Bitmap bitmap,
            RectF destination,
            int left,
            int top,
            int right,
            int bottom) {
        int[] sx = {0, left, bitmap.getWidth() - right, bitmap.getWidth()};
        int[] sy = {0, top, bitmap.getHeight() - bottom, bitmap.getHeight()};
        float[] dx = {destination.left, destination.left + left, destination.right - right, destination.right};
        float[] dy = {destination.top, destination.top + top, destination.bottom - bottom, destination.bottom};
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                canvas.drawBitmap(
                        bitmap,
                        new Rect(sx[column], sy[row], sx[column + 1], sy[row + 1]),
                        new RectF(dx[column], dy[row], dx[column + 1], dy[row + 1]),
                        paint);
            }
        }
    }

    private static RectF cocosRect(float centerX, float centerY, float width, float height) {
        return new RectF(
                centerX - width / 2,
                1080 - centerY - height / 2,
                centerX + width / 2,
                1080 - centerY + height / 2);
    }

    private float viewportScale() {
        return Math.max(0.0001f, Math.min(getWidth() / 1920f, getHeight() / 1080f));
    }
}
