package com.nanbeiyule.game;

import java.util.Collections;
import java.util.List;

/**
 * 一次领取的服务端结果。{@code claimFlag} 是原版 {@code Config.lua:18-26} 的字面量，
 * 客户端按 {@code Module.lua:98-110} 的分支出提示，不自行判定成败。
 */
record TimeLoginClaimResult(
        String claimFlag,
        List<TimeLoginActState.Reward> rewards,
        Integer wheelSliceIndex,
        long walletCoins) {

    static final String FLAG_SUCCESS = "Success";
    static final String FLAG_NOT_IN_TIME = "Not_In_Time";
    static final String FLAG_ALREADY_CLAIM = "Already_Claim";
    static final String FLAG_GOLD_OVER = "Gold_Over";
    static final String FLAG_WHEEL_CNT_LACK = "Wheel_Cnt_Lack";

    TimeLoginClaimResult {
        rewards = rewards == null ? List.of() : Collections.unmodifiableList(rewards);
    }

    boolean succeeded() {
        return FLAG_SUCCESS.equals(claimFlag);
    }

    /** 原版 Module.lua:98-110 的固定提示文案；{@code goldOver} 只在金币超限分支用到。 */
    String toastText(long goldOver) {
        return switch (claimFlag) {
            case FLAG_NOT_IN_TIME -> "当前奖励已过期";
            case FLAG_ALREADY_CLAIM -> "已领取过该奖励";
            case FLAG_GOLD_OVER ->
                    "携带金币超过" + TimeLoginNumberFormat.compact(goldOver) + "不可领奖";
            case FLAG_WHEEL_CNT_LACK -> "转盘次数不足";
            case FLAG_SUCCESS -> "";
            default -> "其他错误";
        };
    }
}
