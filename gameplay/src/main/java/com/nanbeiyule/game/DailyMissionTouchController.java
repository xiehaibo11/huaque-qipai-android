package com.nanbeiyule.game;

final class DailyMissionTouchController {
    interface Actions {
        void onClose();
        void onPageSelected(String pageCode);
        void onTaskClaim(String taskCode);
        void onTaskGo(DailyMissionState.Task task);
        void onMilestoneClaim(long target);
    }

    private static final int CLOSE = 1;
    private static final int TAB_BASE = 10;
    private static final int MILESTONE_BASE = 100;
    private static final int TASK_BASE = 1000;

    private final Actions actions;
    private final TapGestureGuard tapGuard;
    private final float touchSlop;
    private DailyMissionState state;
    private float taskScroll;
    private float[] rewardScroll = new float[0];
    private float lastX;
    private float lastY;
    private float downX;
    private float downY;
    private boolean scrollGesture;
    private boolean axisResolved;
    private boolean horizontalGesture;
    private int rewardScrollRow = -1;
    private boolean hasStages = true;
    private boolean contentInteractive;
    private int pressedTarget = TapGestureGuard.NO_TARGET;

    DailyMissionTouchController(Actions actions, float touchSlop) {
        this.actions = actions;
        this.touchSlop = touchSlop;
        tapGuard = new TapGestureGuard(touchSlop);
    }

    void setState(DailyMissionState next) {
        String previousPage = state == null ? "" : state.page().pageCode();
        state = next;
        if (next == null || !previousPage.equals(next.page().pageCode())) taskScroll = 0f;
        taskScroll = DailyMissionLayout.clampScroll(
                taskScroll,
                next == null ? 0 : next.tasks().size(),
                next != null && !next.milestones().isEmpty());
        rewardScroll = new float[next == null ? 0 : next.tasks().size()];
        // 原版没有阶段奖时整体换成 _KW_PANEL_LIST_TASKS_B，列表视口不同。
        hasStages = next != null && !next.milestones().isEmpty();
        contentInteractive = next != null;
    }

    void setContentInteractive(boolean interactive) {
        contentInteractive = interactive;
        if (!interactive) cancel();
    }

    void onDown(float x, float y) {
        pressedTarget = targetAt(x, y);
        lastX = x;
        lastY = y;
        downX = x;
        downY = y;
        scrollGesture = DailyMissionLayout.taskViewport(hasStages).contains(x, y);
        rewardScrollRow = rewardRowAt(x, y);
        axisResolved = false;
        horizontalGesture = false;
        tapGuard.begin(x, y, pressedTarget);
    }

    void onMove(float x, float y) {
        tapGuard.move(x, y);
        if (state != null && scrollGesture) {
            if (!axisResolved
                    && Math.max(Math.abs(x - downX), Math.abs(y - downY)) >= touchSlop) {
                axisResolved = true;
                horizontalGesture = rewardScrollRow >= 0
                        && Math.abs(x - downX) > Math.abs(y - downY);
            }
            if (axisResolved && horizontalGesture) {
                rewardScroll[rewardScrollRow] = DailyMissionLayout.clampRewardScroll(
                        rewardScroll[rewardScrollRow] + lastX - x,
                        state.tasks().get(rewardScrollRow).rewardCellCount());
            } else if (axisResolved) {
                taskScroll = DailyMissionLayout.clampScroll(
                        taskScroll + lastY - y, state.tasks().size(), hasStages);
            }
        }
        lastX = x;
        lastY = y;
        if (targetAt(x, y) != pressedTarget) pressedTarget = TapGestureGuard.NO_TARGET;
    }

    void onUp(float x, float y) {
        int target = targetAt(x, y);
        boolean tap = tapGuard.finish(x, y, target);
        pressedTarget = TapGestureGuard.NO_TARGET;
        resetGesture();
        if (tap) dispatch(target);
    }

    void cancel() {
        tapGuard.reset();
        pressedTarget = TapGestureGuard.NO_TARGET;
        resetGesture();
    }

    private void resetGesture() {
        scrollGesture = false;
        axisResolved = false;
        horizontalGesture = false;
        rewardScrollRow = -1;
    }

    float taskScroll() { return taskScroll; }

    float rewardScroll(int taskIndex) {
        return taskIndex >= 0 && taskIndex < rewardScroll.length
                ? rewardScroll[taskIndex]
                : 0f;
    }

    int pressedTarget() { return pressedTarget; }

    /** 原版只有任务按钮有按下缩放和压暗；返回 -1 表示当前没有按住任何任务按钮。 */
    int pressedTaskIndex() {
        return pressedTarget >= TASK_BASE ? pressedTarget - TASK_BASE : -1;
    }

    private int rewardRowAt(float x, float y) {
        if (state == null || !contentInteractive) return -1;
        if (!DailyMissionLayout.taskViewport(hasStages).contains(x, y)) return -1;
        int count = state.tasks().size();
        for (int index = 0; index < count; index++) {
            if (DailyMissionLayout.maxRewardScroll(
                    state.tasks().get(index).rewardCellCount()) <= 0f) {
                continue;
            }
            DailyMissionLayout.Box viewport = DailyMissionLayout.taskRewardViewport(
                    DailyMissionLayout.taskRect(index, count, taskScroll, hasStages));
            if (viewport.contains(x, y)) return index;
        }
        return -1;
    }

    private int targetAt(float x, float y) {
        if (DailyMissionLayout.CLOSE.contains(x, y)) return CLOSE;
        if (!contentInteractive) return TapGestureGuard.NO_TARGET;
        if (state == null) return TapGestureGuard.NO_TARGET;
        // 命中区是 _KW_PANEL_ITEM_TAB_n 面板本身，不是会左右移动的页签图。
        for (int index = 0; index < state.tabs().size(); index++) {
            if (DailyMissionLayout.tabPanel(index).contains(x, y)) return TAB_BASE + index;
        }
        for (int index = 0; index < state.milestones().size(); index++) {
            if (state.milestones().get(index).state()
                            == DailyMissionState.MilestoneState.CLAIMABLE
                    && DailyMissionLayout.milestoneRect(index).contains(x, y)) {
                return MILESTONE_BASE + index;
            }
        }
        if (!DailyMissionLayout.taskViewport(hasStages).contains(x, y)) {
            return TapGestureGuard.NO_TARGET;
        }
        for (int index = 0; index < state.tasks().size(); index++) {
            DailyMissionState.Task task = state.tasks().get(index);
            DailyMissionLayout.Box button = DailyMissionLayout.taskButtonRect(
                    index, state.tasks().size(), taskScroll, hasStages);
            if (task.state() != DailyMissionState.TaskState.CLAIMED
                    && button.contains(x, y)) {
                return TASK_BASE + index;
            }
        }
        return TapGestureGuard.NO_TARGET;
    }

    private void dispatch(int target) {
        if (target == CLOSE) {
            actions.onClose();
        } else if (target >= TASK_BASE && state != null) {
            DailyMissionState.Task task = state.tasks().get(target - TASK_BASE);
            if (task.state() == DailyMissionState.TaskState.CLAIMABLE) {
                actions.onTaskClaim(task.taskCode());
            } else if (task.state() == DailyMissionState.TaskState.IN_PROGRESS) {
                actions.onTaskGo(task);
            }
        } else if (target >= MILESTONE_BASE && state != null) {
            actions.onMilestoneClaim(
                    state.milestones().get(target - MILESTONE_BASE).target());
        } else if (target >= TAB_BASE && state != null) {
            DailyMissionState.Page page = state.tabs().get(target - TAB_BASE);
            // 原版 flushTabsState 对当前页签关掉触摸，重复点同一页不再请求。
            if (!page.pageCode().equals(state.page().pageCode())) {
                actions.onPageSelected(page.pageCode());
            }
        }
    }
}
