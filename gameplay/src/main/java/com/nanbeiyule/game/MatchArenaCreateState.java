package com.nanbeiyule.game;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Editable values and original 900023 TeaHouseCreateView validation. */
final class MatchArenaCreateState {
    enum Mode {
        LEADER("领队模式"),
        PREPAID("预付模式"),
        CIRCULATION("比赛场流通"),
        LOBBY_CARD("扣玩家卡模式");

        final String label;

        Mode(String label) {
            this.label = label;
        }
    }

    enum CostType {
        CHAMPION("冠军付"),
        AA("平摊");

        final String label;

        CostType(String label) {
            this.label = label;
        }
    }

    static final List<Mode> MODES_900023 =
            List.of(Mode.LEADER, Mode.PREPAID, Mode.CIRCULATION, Mode.LOBBY_CARD);

    private String remark;
    private String dailyRoomCardLimit;
    private String initialRoomCards;
    private Mode mode;
    private CostType costType;
    private CostType pendingCostType;
    private boolean visibleToStrangers;
    private boolean autoTransferEnabled;
    private long autoTransferThreshold;
    private int autoTransferSelection;
    private String autoTransferCustomValue;
    private boolean lowCardReminderEnabled;
    private int lowCardReminderSelection;
    private String lowCardReminderCustomValue;
    private boolean submitting;
    private long purchasedRoomCards = Long.MAX_VALUE;

    static MatchArenaCreateState original900023() {
        MatchArenaCreateState state = new MatchArenaCreateState();
        state.remark = "";
        state.dailyRoomCardLimit = "888888";
        state.initialRoomCards = "0";
        state.mode = Mode.LEADER;
        state.costType = null;
        state.visibleToStrangers = true;
        state.autoTransferEnabled = false;
        state.autoTransferThreshold = 50;
        state.autoTransferSelection = 0;
        state.autoTransferCustomValue = "";
        state.lowCardReminderEnabled = false;
        state.lowCardReminderSelection = 0;
        state.lowCardReminderCustomValue = "";
        return state;
    }

    String validate() {
        String value = normalized(remark);
        if (value.getBytes(StandardCharsets.UTF_8).length > 4) {
            return "比赛场备注不能超过4个字符";
        }
        Long daily = positiveOrZero(dailyRoomCardLimit);
        if (dailyRoomCardLimit == null || dailyRoomCardLimit.isBlank()) {
            return "每日最大消耗不能为空";
        }
        Long initial = positiveOrZero(initialRoomCards);
        if (initial == null) {
            return "划卡数量不正确";
        }
        if (initial > purchasedRoomCards) {
            return "房卡库存不足";
        }
        if (!value.isEmpty() && !value.matches("(?:\\d+(?:\\.\\d*)?|\\.\\d+)")) {
            return "比赛场备注只允许数字和小数点";
        }
        if (daily == null || daily == 0) {
            return "每日最大消耗输入不正确";
        }
        if (costType == null) {
            return "请先为您的比赛场选择消耗模式";
        }
        String autoError = autoTransferValidationError();
        if (autoError != null) return autoError;
        String reminderError = lowCardReminderValidationError();
        if (reminderError != null) return reminderError;
        return null;
    }

    private static Long positiveOrZero(String value) {
        try {
            if (value == null || value.isBlank() || !value.matches("\\d+")) {
                return null;
            }
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim();
    }

    String remark() { return remark; }
    String dailyRoomCardLimit() { return dailyRoomCardLimit; }
    String initialRoomCards() { return initialRoomCards; }
    Mode mode() { return mode; }
    CostType costType() { return costType; }
    CostType pendingCostType() { return pendingCostType; }
    boolean visibleToStrangers() { return visibleToStrangers; }
    boolean autoTransferEnabled() {
        return mode != Mode.LOBBY_CARD && autoTransferEnabled;
    }
    long autoTransferThreshold() { return autoTransferThreshold; }
    long autoTransferAmount() {
        return autoTransferEnabled() ? selectedAutoTransferAmount() : 0;
    }
    Long lowCardReminderThreshold() {
        return lowCardReminderEnabled() ? selectedLowCardReminderThreshold() : null;
    }
    boolean lowCardReminderEnabled() {
        return mode != Mode.LOBBY_CARD && lowCardReminderEnabled;
    }
    long selectedAutoTransferAmount() {
        return selectedAmount(autoTransferSelection, autoTransferCustomValue);
    }
    long selectedLowCardReminderThreshold() {
        return selectedAmount(lowCardReminderSelection, lowCardReminderCustomValue);
    }
    boolean autoTransferUsesCustomValue() { return autoTransferSelection == 4; }
    boolean lowCardReminderUsesCustomValue() { return lowCardReminderSelection == 4; }
    String autoTransferCustomValue() { return autoTransferCustomValue; }
    String lowCardReminderCustomValue() { return lowCardReminderCustomValue; }
    boolean isSubmitting() { return submitting; }
    long initialRoomCardsValue() { return Long.parseLong(initialRoomCards); }
    long dailyRoomCardLimitValue() { return Long.parseLong(dailyRoomCardLimit); }

    void setRemark(String value) { remark = value; }
    void setDailyRoomCardLimit(String value) { dailyRoomCardLimit = value; }
    void setInitialRoomCards(String value) { initialRoomCards = value; }
    void setMode(Mode value) {
        if (mode == value) {
            return;
        }
        mode = value;
        costType = null;
        pendingCostType = null;
        dailyRoomCardLimit = value == Mode.LEADER ? dailyRoomCardLimit : "888888";
        initialRoomCards = "0";
        if (value == Mode.LOBBY_CARD) {
            autoTransferEnabled = false;
            lowCardReminderEnabled = false;
        }
    }
    void openCostEditor() {
        pendingCostType = mode == Mode.CIRCULATION ? CostType.AA : CostType.CHAMPION;
    }
    void selectPendingCostType(CostType value) {
        if (mode == Mode.CIRCULATION) {
            pendingCostType = CostType.AA;
        } else {
            pendingCostType = value;
        }
    }
    void confirmCostEditor() { costType = pendingCostType; }
    void cancelCostEditor() { pendingCostType = null; }
    void setPurchasedRoomCards(long value) { purchasedRoomCards = Math.max(0, value); }
    void setSubmitting(boolean value) { submitting = value; }

    void setAutoTransferEnabled(boolean value) {
        autoTransferEnabled = value && mode != Mode.LOBBY_CARD;
    }

    void selectAutoTransferPreset(long value) {
        autoTransferSelection = presetIndex(value);
    }

    void selectAutoTransferCustom() { autoTransferSelection = 4; }
    void setAutoTransferCustomValue(String value) {
        autoTransferCustomValue = value == null ? "" : value.trim();
    }

    void setLowCardReminderEnabled(boolean value) {
        lowCardReminderEnabled = value && mode != Mode.LOBBY_CARD;
    }

    void selectLowCardReminderPreset(long value) {
        lowCardReminderSelection = presetIndex(value);
    }

    void selectLowCardReminderCustom() { lowCardReminderSelection = 4; }
    void setLowCardReminderCustomValue(String value) {
        lowCardReminderCustomValue = value == null ? "" : value.trim();
    }

    String autoTransferValidationError() {
        if (!autoTransferEnabled()) return null;
        long amount = selectedAutoTransferAmount();
        if (amount <= 0) return "!请填写正确数值";
        if (amount > purchasedRoomCards) {
            return autoTransferUsesCustomValue()
                    ? "!不可超过个人账户数值"
                    : "!个人账户余额不足";
        }
        return null;
    }

    String lowCardReminderValidationError() {
        if (!lowCardReminderEnabled()) return null;
        return selectedLowCardReminderThreshold() > 0 ? null : "!请填写正确数值";
    }

    private static int presetIndex(long value) {
        if (value == 100) return 0;
        if (value == 500) return 1;
        if (value == 1000) return 2;
        if (value == 2000) return 3;
        throw new IllegalArgumentException("Unsupported original room-card preset");
    }

    private static long selectedAmount(int selection, String customValue) {
        if (selection >= 0 && selection < 4) {
            return new long[] {100, 500, 1000, 2000}[selection];
        }
        Long custom = positiveOrZero(customValue);
        return custom == null ? -1 : custom;
    }
}
