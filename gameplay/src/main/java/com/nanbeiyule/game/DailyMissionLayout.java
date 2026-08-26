package com.nanbeiyule.game;

/** 1920x1080 geometry recovered from LuckyMissionView.csb. */
final class DailyMissionLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;
    static final float TASK_WIDTH = 1499f;
    static final float TASK_HEIGHT = 184f;
    static final float TASK_STEP = 184f;

    static final Box BOARD = new Box(108.8163f, 93.1436f, 1926.8163f, 1063.1436f);
    static final Box PAGE_BACKGROUND = new Box(232.93f, 156.3683f, 1792.93f, 1008.3683f);
    static final Box CLOSE = new Box(1809.288f, 108.54f, 1863.288f, 162.54f);
    static final Box TITLE = new Box(188.67f, 17.64f, 709.67f, 202.64f);

    /**
     * 页签命中区是 _KW_PANEL_ITEM_TAB_n 面板本身；选中态画 KW_CLICK_TAB>Image_7，
     * 未选中态画 KW_CLICK_ITTEM，两者在原版里 X 不同，选中的页签会左移贴住底板边缘。
     */
    /** 原版 initTabs 由服务端 pageList 驱动，最多五个页签。 */
    static final int MAX_TABS = 5;
    static final float TAB_WIDTH = 323f;
    static final float TAB_HEIGHT = 137f;

    // _KW_PANEL_ITEM_TAB_n 面板本身，也是原版的命中区。
    private static final float[] TAB_PANEL_LEFT = {-6.54f, 10.46f, 15.46f, 20.46f, 24.46f};
    private static final float[] TAB_TOP = {214.648f, 352.648f, 490.649f, 627.648f, 768.648f};
    // KW_CLICK_ITTEM（未选中）与 KW_CLICK_TAB>Image_7（选中）在页签内的 X 不同：
    // 未选中的页签依次向右错开，选中的那个左移贴住底板边缘。
    private static final float[] TAB_IMAGE_LEFT = {-3.31f, 10.46f, 15.46f, 20.46f, 24.46f};
    private static final float[] TAB_IMAGE_ACTIVE_LEFT = {-3.31f, -2.46f, -0.69f, -2.15f, -1.38f};
    // KW_REDPOINT 33x33，挂在页签面板上，不随选中态左右移动。
    private static final float[] TAB_RED_POINT_CENTER_X =
            {232.48f, 231.715f, 231.87f, 232.025f, 232.795f};
    private static final float TAB_RED_POINT_LOCAL_CENTER_Y = 17.81f;
    private static final float TAB_RED_POINT_SIZE = 33f;

    /** KW_TEXT_TAB 在页签图内的局部中心 (135.66, 137-75.35)。 */
    static final float TAB_TEXT_OFFSET_X = 135.66f;
    static final float TAB_TEXT_OFFSET_Y = 61.65f;
    /** 页签文案换行时的行距：未选中 52、选中 56（CSB 两行文本 104/112）。 */
    static final float TAB_TEXT_LINE_HEIGHT_ACTIVE = 56f;
    static final float TAB_TEXT_LINE_HEIGHT_INACTIVE = 52f;

    static Box tabPanel(int index) {
        return new Box(
                TAB_PANEL_LEFT[index],
                TAB_TOP[index],
                TAB_PANEL_LEFT[index] + TAB_WIDTH,
                TAB_TOP[index] + TAB_HEIGHT);
    }

    static Box tabImage(int index, boolean active) {
        float left = active ? TAB_IMAGE_ACTIVE_LEFT[index] : TAB_IMAGE_LEFT[index];
        return new Box(left, TAB_TOP[index], left + TAB_WIDTH, TAB_TOP[index] + TAB_HEIGHT);
    }

    static Box tabRedPoint(int index) {
        float centerX = TAB_RED_POINT_CENTER_X[index];
        float centerY = TAB_TOP[index] + TAB_RED_POINT_LOCAL_CENTER_Y;
        float half = TAB_RED_POINT_SIZE * 0.5f;
        return new Box(centerX - half, centerY - half, centerX + half, centerY + half);
    }

    static final Box ACTIVITY_BADGE = new Box(298.85f, 175.012f, 547.85f, 322.012f);
    static final Box ACTIVITY_PROGRESS = new Box(470.35f, 295.012f, 1695.35f, 313.012f);
    /**
     * 有阶段奖时用 _KW_PANEL_LIST_TASKS_A（_KW_BG_1 + _KW_SCROLL_LIST_TASK_A），
     * 没有阶段奖时原版整体换成 _KW_PANEL_LIST_TASKS_B，列表更高也更靠上。
     */
    static final Box TASK_BACKGROUND = new Box(268f, 323.3f, 1768f, 968.3f);
    static final Box TASK_VIEWPORT = new Box(268f, 328.3f, 1768f, 963.3f);
    static final Box TASK_BACKGROUND_NO_STAGE = new Box(268f, 201.5f, 1768f, 968.5f);
    static final Box TASK_VIEWPORT_NO_STAGE = new Box(268f, 211.976f, 1768f, 961.976f);
    /** _KW_BG_1/_KW_BG_2 开启了 scale9，cap insets 在 50x50 贴图上四边各 16。 */
    static final float TASK_BACKGROUND_INSET = 16f;
    static final Box TASK_BUTTON_LOCAL_COCOS =
            new Box(1226.5f, 53f, 1473.5f, 147f);

    /** KW_PANEL_ITEMS 横向滚动区 pos(725,99.36) size 475x155 anchor(0,0.5)，格子 150x150。 */
    static final float TASK_REWARD_VIEWPORT_LEFT = 725f;
    static final float TASK_REWARD_VIEWPORT_WIDTH = 475f;
    static final float TASK_REWARD_VIEWPORT_TOP = 7.14f;
    static final float TASK_REWARD_VIEWPORT_HEIGHT = 155f;
    static final float TASK_REWARD_CELL = 150f;
    static final float TASK_REWARD_CELL_TOP = 9.64f;

    /** KW_LEFT_TIME pos(9.5,184) anchor(0,1) size 90x73；KW_LEFT pos(1350,37) anchor(0.5,0.5)。 */
    static final float TASK_LIMIT_BADGE_LEFT = 9.5f;
    static final float TASK_LIMIT_BADGE_TOP = 0f;
    static final float TASK_LIMIT_BADGE_WIDTH = 90f;
    static final float TASK_LIMIT_BADGE_HEIGHT = 73f;
    static final float TASK_LEFT_TEXT_X = 1350f;
    static final float TASK_LEFT_TEXT_CENTER_Y = 147f;

    /** _KW_PANEL_ITEM_AWARD 子节点相对 119x120 条目左上角的 CSB 偏移。 */
    static final float MILESTONE_FRAME_OFFSET_X = 1.19f;
    static final float MILESTONE_PROP_SIZE = 100f;
    static final float MILESTONE_PROP_TOP = 4f;
    static final float MILESTONE_COUNT_RIGHT = 111f;
    static final float MILESTONE_COUNT_CENTER_Y = 94f;
    static final float MILESTONE_STAGE_CENTER_Y = 135f;
    static final float MILESTONE_CLAIMED_WIDTH = 102.4f;
    static final float MILESTONE_CLAIMED_HEIGHT = 80.8f;
    static final float MILESTONE_CLAIMED_CENTER_Y = 54f;

    /** View.lua 播放的六套骨骼动画锚点，全部换算到顶部原点舞台坐标。 */
    static final float EFFECT_TOP_SWEEP_X = -210f;
    static final float EFFECT_TOP_SWEEP_Y = 0f;
    static final float EFFECT_ACTIVITY_STARS_X = 423.35f;
    static final float EFFECT_ACTIVITY_STARS_Y = 248.512f;
    static final float EFFECT_ACTIVITY_ICON_X = 351.9564f;
    static final float EFFECT_ACTIVITY_ICON_Y = 271.933f;
    static final float EFFECT_ACTIVITY_ICON_SCALE = 0.85f;
    static final float EFFECT_PROGRESS_HEAD_Y = 304.012f;
    static final float EFFECT_MILESTONE_OFFSET_Y = -3.6f;
    static final float EFFECT_MILESTONE_SCALE = 0.8f;
    static final float EFFECT_BUTTON_OFFSET_Y = -2.82f;
    static final float EFFECT_BUTTON_SCALE_X = 0.76f;
    static final float EFFECT_BUTTON_SCALE_Y = 1.04f;

    private static final float MILESTONE_FIRST_X = 776.6f;
    private static final float MILESTONE_STEP_X = 306.25f;
    private static final float MILESTONE_CENTER_Y = 229.012f;

    private DailyMissionLayout() {}

    static Box taskViewport(boolean hasStages) {
        return hasStages ? TASK_VIEWPORT : TASK_VIEWPORT_NO_STAGE;
    }

    static Box taskBackground(boolean hasStages) {
        return hasStages ? TASK_BACKGROUND : TASK_BACKGROUND_NO_STAGE;
    }

    static Box taskRect(int index, int taskCount, float scroll) {
        return taskRect(index, taskCount, scroll, true);
    }

    static Box taskRect(int index, int taskCount, float scroll, boolean hasStages) {
        float top = taskViewport(hasStages).top
                + index * TASK_STEP
                - clampScroll(scroll, taskCount, hasStages);
        return new Box(268.5f, top, 1767.5f, top + TASK_HEIGHT);
    }

    static Box taskButtonRect(int index, int taskCount, float scroll) {
        return taskButtonRect(index, taskCount, scroll, true);
    }

    static Box taskButtonRect(int index, int taskCount, float scroll, boolean hasStages) {
        Box task = taskRect(index, taskCount, scroll, hasStages);
        float top = task.top + TASK_HEIGHT - TASK_BUTTON_LOCAL_COCOS.bottom;
        float bottom = task.top + TASK_HEIGHT - TASK_BUTTON_LOCAL_COCOS.top;
        return new Box(
                task.left + TASK_BUTTON_LOCAL_COCOS.left,
                top,
                task.left + TASK_BUTTON_LOCAL_COCOS.right,
                bottom);
    }

    static Box milestoneRect(int index) {
        float centerX = MILESTONE_FIRST_X + index * MILESTONE_STEP_X;
        return new Box(
                centerX - 59.5f,
                MILESTONE_CENTER_Y - 60f,
                centerX + 59.5f,
                MILESTONE_CENTER_Y + 60f);
    }

    /** 单个任务条内的奖励横向滚动区，坐标已换算到舞台。 */
    static Box taskRewardViewport(DailyMissionLayout.Box task) {
        return new Box(
                task.left() + TASK_REWARD_VIEWPORT_LEFT,
                task.top() + TASK_REWARD_VIEWPORT_TOP,
                task.left() + TASK_REWARD_VIEWPORT_LEFT + TASK_REWARD_VIEWPORT_WIDTH,
                task.top() + TASK_REWARD_VIEWPORT_TOP + TASK_REWARD_VIEWPORT_HEIGHT);
    }

    /** 原版 setInnerContainerSize：内容不足视口宽时不产生滚动。 */
    static float maxRewardScroll(int cellCount) {
        return Math.max(0f, cellCount * TASK_REWARD_CELL - TASK_REWARD_VIEWPORT_WIDTH);
    }

    static float clampRewardScroll(float scroll, int cellCount) {
        return Math.max(0f, Math.min(maxRewardScroll(cellCount), scroll));
    }

    static float maxTaskScroll(int taskCount) {
        return maxTaskScroll(taskCount, true);
    }

    static float maxTaskScroll(int taskCount, boolean hasStages) {
        return Math.max(0f, taskCount * TASK_STEP - taskViewport(hasStages).height());
    }

    static float clampScroll(float scroll, int taskCount) {
        return clampScroll(scroll, taskCount, true);
    }

    static float clampScroll(float scroll, int taskCount, boolean hasStages) {
        return Math.max(0f, Math.min(maxTaskScroll(taskCount, hasStages), scroll));
    }

    record Box(float left, float top, float right, float bottom) {
        float width() { return right - left; }
        float height() { return bottom - top; }
        float centerX() { return (left + right) * 0.5f; }
        float centerY() { return (top + bottom) * 0.5f; }
        boolean contains(float x, float y) {
            return x >= left && x < right && y >= top && y < bottom;
        }
    }

    record Transform(float scale, float offsetX, float offsetY) {
        static Transform contain(int width, int height) {
            float scale = Math.min(width / DESIGN_WIDTH, height / DESIGN_HEIGHT);
            return new Transform(
                    scale,
                    (width - DESIGN_WIDTH * scale) * 0.5f,
                    (height - DESIGN_HEIGHT * scale) * 0.5f);
        }

        float designX(float x) { return (x - offsetX) / scale; }
        float designY(float y) { return (y - offsetY) / scale; }
    }
}
