package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 定时登录有礼的客户端只读状态。全部字段来自受保护的 Time Login API，
 * 客户端不推导领取资格，也不在本地伪造进度、奖励或钱包。
 */
final class TimeLoginActState {
    /** 原版 Config.lua:4-10 的状态字面量。 */
    static final String STATUS_CAN_REWARD = "CanReward";
    static final String STATUS_REWARDED = "Rewarded";
    static final String STATUS_CAN_SUPPLE = "CanSupple";
    static final String STATUS_NOT_IN_TIME = "NotInTime";
    static final String STATUS_OVER_TIME = "OverTime";

    private static final int SECONDS_PER_DAY = 86400;

    /** 一个时段卡。 */
    record Slot(
            String rewardId,
            int startSecond,
            int endSecond,
            String rewardFlag,
            long rewardAmount,
            String rewardName) {

        /** 原版 View.lua:230-233 的跨零点归一。 */
        int normalizedStartSecond() {
            return startSecond >= endSecond ? startSecond - SECONDS_PER_DAY : startSecond;
        }

        /** 原版 View.lua:193-200：{@code %d:%02d} 拼「起-止」，所以是 9:00 而不是 09:00。 */
        String timeRangeText() {
            return formatClock(startSecond) + '-' + formatClock(endSecond);
        }

        boolean claimable() {
            return STATUS_CAN_REWARD.equals(rewardFlag) || STATUS_CAN_SUPPLE.equals(rewardFlag);
        }

        boolean rewarded() {
            return STATUS_REWARDED.equals(rewardFlag);
        }

        boolean notInTime() {
            return STATUS_NOT_IN_TIME.equals(rewardFlag);
        }

        /** 原版 View.lua:221-228 的状态文案；可领取时不画文案而是画按钮。 */
        String stateText() {
            if (rewarded()) {
                return "已领取";
            }
            if (STATUS_OVER_TIME.equals(rewardFlag)) {
                return "明日再来";
            }
            return "未到时间";
        }

        /** 原版 View.lua:234-250 的三档分档，决定卡底图与文字色。 */
        int timeBand() {
            int realStart = normalizedStartSecond();
            if (realStart < TimeLoginActLayout.SLOT_TIME_BAND_MORNING_END) {
                return 0;
            }
            return realStart < TimeLoginActLayout.SLOT_TIME_BAND_NOON_END ? 1 : 2;
        }

        private static String formatClock(int second) {
            return String.format(Locale.US, "%d:%02d", second / 3600, second % 3600 / 60);
        }
    }

    /** 转盘目录；{@code props} 恒为 8 项且按格序，概率不下发。 */
    record Wheel(String rewardId, int currentCount, int requiredCount, List<Reward> props) {
        boolean unlocked() {
            return currentCount >= requiredCount;
        }

        /** 原版 View.lua:299-316：奖池里金币道具的最大值。 */
        long maxCoinReward() {
            long max = 0;
            for (Reward reward : props) {
                if ("COIN".equals(reward.propId()) && reward.propCnt() > max) {
                    max = reward.propCnt();
                }
            }
            return max;
        }
    }

    record Reward(String propId, long propCnt, String name) {}

    private final boolean active;
    private final String activityCode;
    private final List<Slot> slots;
    private final Wheel wheel;
    private final long goldOver;
    private final int daySecond;
    private final long serverTimeSeconds;
    private final long walletCoins;

    TimeLoginActState(
            boolean active,
            String activityCode,
            List<Slot> slots,
            Wheel wheel,
            long goldOver,
            int daySecond,
            long serverTimeSeconds,
            long walletCoins) {
        this.active = active;
        this.activityCode = activityCode == null ? "" : activityCode;
        this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
        this.wheel = wheel;
        this.goldOver = goldOver;
        this.daySecond = daySecond;
        this.serverTimeSeconds = serverTimeSeconds;
        this.walletCoins = walletCoins;
    }

    /**
     * 原版 {@code Module.lua:251-253} 的 {@code isValid()}：{@code return self._aid ~= 0}。
     * 活动关闭时原版服务端仍正常应答，只把活动 ID 置 0，所以判据是实体字段而不是 HTTP 状态码。
     * 南北娱乐后端对应下发 {@code active} 与 {@code activityCode}，缺字段一律按无效处理。
     */
    boolean isValid() {
        return active && !activityCode.isEmpty();
    }

    /** 服务端下发的活动编码，对应原版 {@code _aid}；无效态为空串。 */
    String activityCode() {
        return activityCode;
    }

    /**
     * 原版 {@code Module.lua:285-290} 的 {@code isHaveAward()}：{@code _loginRewards} 非空。
     * AutoPop 用它选 {@code popCfg} 的档位下标（无奖 1 / 有奖 2）。
     */
    boolean isHaveAward() {
        return !slots.isEmpty();
    }

    List<Slot> slots() {
        return slots;
    }

    Wheel wheel() {
        return wheel;
    }

    long goldOver() {
        return goldOver;
    }

    int daySecond() {
        return daySecond;
    }

    long serverTimeSeconds() {
        return serverTimeSeconds;
    }

    long walletCoins() {
        return walletCoins;
    }

    /**
     * 原版 View.lua:213-220：只有顺序上第一个 {@code NotInTime} 的卡显示倒计时，
     * 其余显示静态「未到时间」。返回 -1 表示没有卡显示倒计时。
     */
    int countdownSlotIndex() {
        for (int index = 0; index < slots.size(); index++) {
            if (slots.get(index).notInTime()) {
                return index;
            }
        }
        return -1;
    }

    /**
     * 距离该时段开始还剩多少秒，按服务端 {@code daySecond} 加本地已流逝时间推进，
     * 对应原版 View.lua:461-471。返回 0 表示已到点，调用方应重新拉取状态。
     */
    long remainingSeconds(int slotIndex, long elapsedSeconds) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return 0;
        }
        long left =
                slots.get(slotIndex).normalizedStartSecond() - (daySecond + elapsedSeconds);
        return Math.max(0, left);
    }

    /** 原版 View.lua:472 的 {@code %02d:%02d:%02d}。 */
    static String formatCountdown(long remainingSeconds) {
        long safe = Math.max(0, remainingSeconds);
        return String.format(
                Locale.US,
                "%02d:%02d:%02d",
                safe / 3600,
                safe % 3600 / 60,
                safe % 60);
    }

    /**
     * 大厅入口红点，逐条对应原版 {@code Module.lua:222-238} 的 {@code checkShowRedPoint()}：
     * 任一时段可领或可补领，或转盘已解锁。宝箱分支不在当前实现范围内。
     */
    boolean showsRedPoint() {
        for (Slot slot : slots) {
            if (slot.claimable()) {
                return true;
            }
        }
        return wheel != null && wheel.unlocked();
    }

    /** 原版 View.lua:71 的页脚文案，数值由服务端 goldOver 下发。 */
    String goldOverTipsText() {
        return "注：携带金币超过" + TimeLoginNumberFormat.compact(goldOver) + "不可领奖";
    }

    /** 原版 View.lua:300 的「<最大金币>金币」，用 BMFont 绘制。 */
    String maxRewardText() {
        if (wheel == null) {
            return "";
        }
        return TimeLoginNumberFormat.compact(wheel.maxCoinReward()) + "金币";
    }
}
