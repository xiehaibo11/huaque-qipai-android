package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

final class DailyMissionDrawableSet {
    final Bitmap board;
    final Bitmap dailyBackground;
    final Bitmap weeklyBackground;
    final Bitmap dailyTaskBackground;
    final Bitmap weeklyTaskBackground;
    final Bitmap surpriseTaskBackground;
    final Bitmap luckyTaskBackground;
    final Bitmap surpriseBackground;
    final Bitmap luckyBackground;
    final Bitmap dailyTitle;
    final Bitmap weeklyTitle;
    final Bitmap surpriseTitle;
    final Bitmap luckyTitle;
    final Bitmap tabActive;
    final Bitmap tabInactive;
    final Bitmap redPoint;
    final Bitmap close;
    final Bitmap activityLight;
    final Bitmap activityBadge;
    final Bitmap activityIcon;
    final Bitmap activityProgressTrack;
    final Bitmap activityProgressFill;
    final Bitmap milestoneFrame;
    final Bitmap milestoneLocked;
    final Bitmap milestoneReached;
    final Bitmap milestoneClaimed;
    final Bitmap milestoneReachedLabel;
    final Bitmap milestoneClaimedLabel;
    final Bitmap claimedOverlay;
    final Bitmap rewardCell;
    final Bitmap taskComplete;
    final Bitmap taskInProgress;
    final Bitmap taskProgressTrack;
    final Bitmap taskProgressFill;
    final Bitmap timerTip;
    final Bitmap buttonYellow;
    final Bitmap buttonBlue;
    final Bitmap buttonDisabled;
    final Bitmap coin;
    final Bitmap doubleCard;
    final Bitmap roomCard;

    DailyMissionDrawableSet(Resources resources) {
        board = load(resources, R.drawable.daily_mission_original_background);
        dailyBackground = load(resources, R.drawable.daily_mission_original_daily_background);
        weeklyBackground = load(resources, R.drawable.daily_mission_original_weekly_background);
        dailyTaskBackground = load(
                resources, R.drawable.daily_mission_original_daily_task_background);
        weeklyTaskBackground = load(
                resources, R.drawable.daily_mission_original_weekly_task_background);
        surpriseTaskBackground = load(
                resources, R.drawable.daily_mission_original_surprise_task_background);
        luckyTaskBackground = load(
                resources, R.drawable.daily_mission_original_lucky_task_background);
        surpriseBackground = load(
                resources, R.drawable.daily_mission_original_surprise_background);
        luckyBackground = load(resources, R.drawable.daily_mission_original_lucky_background);
        surpriseTitle = load(resources, R.drawable.daily_mission_original_surprise_title);
        luckyTitle = load(resources, R.drawable.daily_mission_original_lucky_title);
        dailyTitle = load(resources, R.drawable.daily_mission_original_daily_title);
        weeklyTitle = load(resources, R.drawable.daily_mission_original_weekly_title);
        tabActive = load(resources, R.drawable.daily_mission_original_tab_active);
        tabInactive = load(resources, R.drawable.daily_mission_original_tab_inactive);
        redPoint = load(resources, R.drawable.daily_mission_original_red_point);
        close = load(resources, R.drawable.daily_mission_original_close);
        activityLight = load(resources, R.drawable.daily_mission_original_activity_light);
        activityBadge = load(resources, R.drawable.daily_mission_original_activity_badge);
        activityIcon = load(resources, R.drawable.daily_mission_original_activity_icon);
        activityProgressTrack = load(
                resources, R.drawable.daily_mission_original_activity_progress_track);
        activityProgressFill = load(
                resources, R.drawable.daily_mission_original_activity_progress_fill);
        milestoneFrame = load(resources, R.drawable.daily_mission_original_milestone_frame);
        milestoneLocked = load(resources, R.drawable.daily_mission_original_milestone_locked);
        milestoneReached = load(resources, R.drawable.daily_mission_original_milestone_reached);
        milestoneClaimed = load(resources, R.drawable.daily_mission_original_milestone_claimed);
        milestoneReachedLabel = load(
                resources, R.drawable.daily_mission_original_milestone_reached_label);
        milestoneClaimedLabel = load(
                resources, R.drawable.daily_mission_original_milestone_claimed_label);
        claimedOverlay = load(resources, R.drawable.daily_mission_original_claimed_overlay);
        rewardCell = load(resources, R.drawable.daily_mission_original_reward_cell);
        taskComplete = load(resources, R.drawable.daily_mission_original_task_complete);
        taskInProgress = load(resources, R.drawable.daily_mission_original_task_in_progress);
        taskProgressTrack = load(
                resources, R.drawable.daily_mission_original_task_progress_track);
        taskProgressFill = load(
                resources, R.drawable.daily_mission_original_task_progress_fill);
        timerTip = load(resources, R.drawable.daily_mission_original_timer_tip);
        buttonYellow = load(resources, R.drawable.daily_mission_original_button_yellow);
        buttonBlue = load(resources, R.drawable.daily_mission_original_button_blue);
        buttonDisabled = load(resources, R.drawable.daily_mission_original_button_disabled);
        coin = load(resources, R.drawable.membership_reward_coin);
        doubleCard = load(resources, R.drawable.membership_reward_game_card);
        roomCard = load(resources, R.drawable.game_home_final_resource_room_card);
    }

    /** Config.lua TAB_INFO 的 BG_IMG。 */
    Bitmap pageBackground(DailyMissionTheme theme) {
        return switch (theme) {
            case DAILY -> dailyBackground;
            case WEEK -> weeklyBackground;
            case TEAM -> surpriseBackground;
            case LUCKY, CUSTOM -> luckyBackground;
        };
    }

    /** Config.lua TAB_INFO 的 BG_IMG_TASK。 */
    Bitmap taskBackground(DailyMissionTheme theme) {
        return switch (theme) {
            case DAILY -> dailyTaskBackground;
            case WEEK -> weeklyTaskBackground;
            case TEAM -> surpriseTaskBackground;
            case LUCKY, CUSTOM -> luckyTaskBackground;
        };
    }

    /** Config.lua TAB_INFO 的 TITLE_IMG。 */
    Bitmap title(DailyMissionTheme theme) {
        return switch (theme) {
            case DAILY -> dailyTitle;
            case WEEK -> weeklyTitle;
            case TEAM -> surpriseTitle;
            case LUCKY, CUSTOM -> luckyTitle;
        };
    }

    Bitmap rewardIcon(DailyMissionState.Reward reward) {
        if ("mission_coin".equals(reward.iconKey()) || "COIN".equals(reward.code())) {
            return coin;
        }
        if ("mission_double_card".equals(reward.iconKey())
                || "DOUBLE_SCORE_CARD".equals(reward.code())) {
            return doubleCard;
        }
        if ("mission_room_card".equals(reward.iconKey())
                || "ROOM_CARD".equals(reward.code())) {
            return roomCard;
        }
        return null;
    }

    private static Bitmap load(Resources resources, int resourceId) {
        Bitmap result = BitmapFactory.decodeResource(resources, resourceId);
        if (result == null) throw new IllegalStateException(
                "Unable to decode daily mission drawable " + resourceId);
        return result;
    }
}
