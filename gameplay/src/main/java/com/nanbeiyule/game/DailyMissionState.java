package com.nanbeiyule.game;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

record DailyMissionState(
        Instant serverTime,
        Page page,
        List<Page> pages,
        long activityPoints,
        List<Milestone> milestones,
        List<Task> tasks,
        Wallet wallet) {
    enum TaskState { CLAIMABLE, IN_PROGRESS, CLAIMED }
    enum MilestoneState { LOCKED, CLAIMABLE, CLAIMED }

    record Page(
            String pageCode,
            String displayName,
            String cycleType,
            Instant expiresAt,
            boolean redPoint) {}

    record Reward(String code, String displayName, long quantity, String iconKey) {}

    record Task(
            String taskCode,
            String title,
            long progress,
            long target,
            long activityPoints,
            TaskState state,
            String jumpType,
            int displayOrder,
            Instant startsAt,
            Instant endsAt,
            List<Reward> rewards) {
        Task {
            rewards = List.copyOf(rewards);
        }

        /** 原版 KW_PANEL_ITEMS：活跃值格排第一，其后是全部道具奖励，不截断。 */
        int rewardCellCount() {
            return (activityPoints > 0 ? 1 : 0) + rewards.size();
        }
    }

    record Milestone(
            long target,
            MilestoneState state,
            int displayOrder,
            List<Reward> rewards) {
        Milestone {
            rewards = List.copyOf(rewards);
        }
    }

    record Wallet(long roomCards, long coins, long diamonds, long coupons) {
        static Wallet empty() { return new Wallet(0, 0, 0, 0); }
    }

    DailyMissionState {
        milestones = List.copyOf(milestones);
        tasks = List.copyOf(tasks);
        pages = pages == null || pages.isEmpty() ? List.of(page) : List.copyOf(pages);
        wallet = wallet == null ? Wallet.empty() : wallet;
    }

    /** 原版 initTabs 最多摆五个页签，多余的服务端页面不画。 */
    List<Page> tabs() {
        return pages.size() <= DailyMissionLayout.MAX_TABS
                ? pages
                : pages.subList(0, DailyMissionLayout.MAX_TABS);
    }

    /** 原版 onEventFlushAct 按页签各自的红点刷新；目录缺失时退回当前页。 */
    boolean redPointFor(String pageCode) {
        for (Page candidate : pages) {
            if (candidate.pageCode().equals(pageCode)) return candidate.redPoint();
        }
        return false;
    }

    static DailyMissionState fromJson(JSONObject body) throws JSONException {
        Instant serverTime = instant(requiredString(body, "serverTime"), "serverTime");
        JSONObject pageBody = body.getJSONObject("page");
        Page page = parsePage(pageBody);
        List<Page> pages = parsePages(body.optJSONArray("pages"), page);
        List<Milestone> milestones = parseMilestones(body.optJSONArray("milestones"));
        List<Task> tasks = parseTasks(body.optJSONArray("tasks"));
        milestones.sort(Comparator.comparingInt(Milestone::displayOrder));
        tasks.sort(Comparator.comparingInt((Task task) -> stateOrder(task.state()))
                .thenComparingInt(Task::displayOrder));
        return new DailyMissionState(
                serverTime,
                page,
                pages,
                Math.max(0L, body.optLong("activityPoints", 0L)),
                milestones,
                tasks,
                parseWallet(body.optJSONObject("wallet")));
    }

    private static Page parsePage(JSONObject body) throws JSONException {
        return new Page(
                requiredString(body, "pageCode"),
                body.optString("displayName", ""),
                requiredString(body, "cycleType"),
                instant(requiredString(body, "expiresAt"), "expiresAt"),
                body.optBoolean("redPoint", false));
    }

    private static List<Page> parsePages(JSONArray bodies, Page current) throws JSONException {
        List<Page> result = new ArrayList<>();
        if (bodies == null) return List.of(current);
        for (int index = 0; index < bodies.length(); index++) {
            result.add(parsePage(bodies.getJSONObject(index)));
        }
        return result.isEmpty() ? List.of(current) : result;
    }

    /** View.lua calcShowLeft：超过一天先写「N天」，其余固定 HH:MM:SS。 */
    static String formatCountdown(long seconds) {
        long safe = Math.max(0L, seconds);
        long days = safe / 86_400L;
        return (days > 0 ? days + "天" : "")
                + String.format(
                        java.util.Locale.ROOT,
                        "%02d:%02d:%02d",
                        safe % 86_400L / 3_600L,
                        safe % 3_600L / 60L,
                        safe % 60L);
    }

    long secondsRemaining() {
        return Math.max(0L, Duration.between(serverTime, page.expiresAt()).getSeconds());
    }

    static float segmentedProgress(long points, List<Long> targets) {
        if (targets == null || targets.isEmpty() || points <= 0) return 0f;
        float segment = 100f / targets.size();
        long previous = 0L;
        for (int index = 0; index < targets.size(); index++) {
            long target = targets.get(index);
            if (target <= previous) continue;
            if (points < target) {
                float within = (float) (points - previous) / (float) (target - previous);
                return Math.max(0f, Math.min(100f, index * segment + within * segment));
            }
            if (points == target) return Math.min(100f, (index + 1) * segment);
            previous = target;
        }
        return 100f;
    }

    private static List<Task> parseTasks(JSONArray bodies) throws JSONException {
        List<Task> result = new ArrayList<>();
        if (bodies == null) return result;
        for (int index = 0; index < bodies.length(); index++) {
            JSONObject body = bodies.getJSONObject(index);
            long target = Math.max(1L, body.getLong("target"));
            long progress = Math.max(0L, Math.min(target, body.optLong("progress", 0L)));
            result.add(new Task(
                    requiredString(body, "taskCode"),
                    body.optString("title", ""),
                    progress,
                    target,
                    Math.max(0L, body.optLong("activityPoints", 0L)),
                    taskState(requiredString(body, "state")),
                    body.optString("jumpType", ""),
                    body.optInt("displayOrder", index),
                    optionalInstant(body, "startsAt"),
                    optionalInstant(body, "endsAt"),
                    parseRewards(body.optJSONArray("rewards"))));
        }
        return result;
    }

    private static List<Milestone> parseMilestones(JSONArray bodies) throws JSONException {
        List<Milestone> result = new ArrayList<>();
        if (bodies == null) return result;
        for (int index = 0; index < bodies.length(); index++) {
            JSONObject body = bodies.getJSONObject(index);
            result.add(new Milestone(
                    Math.max(1L, body.getLong("target")),
                    milestoneState(requiredString(body, "state")),
                    body.optInt("displayOrder", index),
                    parseRewards(body.optJSONArray("rewards"))));
        }
        return result;
    }

    private static List<Reward> parseRewards(JSONArray bodies) throws JSONException {
        List<Reward> result = new ArrayList<>();
        if (bodies == null) return result;
        for (int index = 0; index < bodies.length(); index++) {
            JSONObject body = bodies.getJSONObject(index);
            result.add(new Reward(
                    requiredString(body, "code"),
                    body.optString("displayName", ""),
                    Math.max(0L, body.optLong("quantity", 0L)),
                    body.optString("iconKey", "")));
        }
        return result;
    }

    private static Wallet parseWallet(JSONObject body) {
        return body == null
                ? Wallet.empty()
                : new Wallet(
                        body.optLong("roomCards", 0L),
                        body.optLong("coins", 0L),
                        body.optLong("diamonds", 0L),
                        body.optLong("coupons", 0L));
    }

    private static TaskState taskState(String value) throws JSONException {
        try {
            return TaskState.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new JSONException("unknown task state: " + value);
        }
    }

    private static MilestoneState milestoneState(String value) throws JSONException {
        try {
            return MilestoneState.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new JSONException("unknown milestone state: " + value);
        }
    }

    private static Instant instant(String value, String field) throws JSONException {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new JSONException(field + " is not an ISO-8601 instant");
        }
    }

    /** 限时窗口字段缺失时返回 null，表示该任务完全跟随页签周期。 */
    private static Instant optionalInstant(JSONObject body, String field)
            throws JSONException {
        String value = body.optString(field, "").trim();
        return value.isEmpty() ? null : instant(value, field);
    }

    private static String requiredString(JSONObject body, String field) throws JSONException {
        String value = body.getString(field).trim();
        if (value.isEmpty()) throw new JSONException(field + " must not be blank");
        return value;
    }

    private static int stateOrder(TaskState state) {
        return switch (state) {
            case CLAIMABLE -> 0;
            case IN_PROGRESS -> 1;
            case CLAIMED -> 2;
        };
    }
}
