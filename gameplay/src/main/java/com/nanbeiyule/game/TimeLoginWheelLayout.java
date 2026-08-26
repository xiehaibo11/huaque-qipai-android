package com.nanbeiyule.game;

import com.nanbeiyule.game.TimeLoginActLayout.Box;
import com.nanbeiyule.game.TimeLoginActLayout.CocosNode;

/**
 * 原版 TimeLoginActWheelLayer.csb 的 1920x1080 几何与开奖时间线。
 * 换算方式与 {@link TimeLoginActLayout} 相同，证据见台账第 5 节与第 6.6 节。
 */
final class TimeLoginWheelLayout {
    /** 八格，与 _KW_ITEM_1.._KW_ITEM_8 一一对应。 */
    static final int SLICE_COUNT = 8;
    static final float SLICE_SIZE = 200f;

    /** WheelView.lua:138：指针 EaseQuadraticActionOut，5.7 秒转到 2520 + index*45 度。 */
    static final float ROLL_DURATION_SECONDS = 5.7f;
    /** WheelView.lua:144：外圈同缓动但 5.8 秒。 */
    static final float RING_ROLL_DURATION_SECONDS = 5.8f;
    static final float ROLL_BASE_DEGREES = 2520f;
    static final float DEGREES_PER_SLICE = 45f;

    private TimeLoginWheelLayout() {}

    private static final CocosNode ROOT =
            new CocosNode(0f, 0f, TimeLoginActLayout.DESIGN_WIDTH, TimeLoginActLayout.DESIGN_HEIGHT);

    static final Box RING = ROOT.child(993.68f, 552f, 970f, 970f, 0.5f, 0.5f).box();
    static final Box BOARD = ROOT.child(993.67f, 540f, 1132f, 1078f, 0.5f, 0.5f).box();
    static final Box CLOSE = ROOT.child(1528.67f, 815.93f, 46f, 46f, 0.5f, 0.5f).box();

    /** 指针 anchor(0.5,0.43)，旋转支点就是该锚点。 */
    static final Box POINTER = ROOT.child(993.675f, 552f, 252f, 301f, 0.5f, 0.43f).box();
    static final Box POINTER_LOCKED = ROOT.child(994f, 552f, 252f, 301f, 0.5f, 0.43f).box();
    static final float POINTER_PIVOT_X = 993.675f;
    static final float POINTER_PIVOT_Y = TimeLoginActLayout.DESIGN_HEIGHT - 552f;

    static final Box ROLL_TEXT = ROOT.child(994.16f, 579.33f, 156f, 88f, 0.5f, 0.5f).box();
    /** _KW_TEXT_ROLL_TIPS pos(78.236,-21.5422) 挂在 156x88 的「抽奖」字图下。 */
    static final float ROLL_TIPS_CENTER_X = ROLL_TEXT.left() + 78.236f;
    static final float ROLL_TIPS_CENTER_Y = ROLL_TEXT.top() + (88f + 21.5422f);
    static final float ROLL_TIPS_FONT_SIZE = 36f;

    /** 八格面板左上角，CSB 里前两格 anchor(0.5,0.5)、后六格 anchor(0,0)。 */
    private static final CocosNode[] SLICES = {
        ROOT.child(997.675f, 867f, SLICE_SIZE, SLICE_SIZE, 0.5f, 0.5f),
        ROOT.child(1224.67f, 776f, SLICE_SIZE, SLICE_SIZE, 0.5f, 0.5f),
        ROOT.child(1200.67f, 464f, SLICE_SIZE, SLICE_SIZE, 0f, 0f),
        ROOT.child(1117.67f, 246f, SLICE_SIZE, SLICE_SIZE, 0f, 0f),
        ROOT.child(897.674f, 132f, SLICE_SIZE, SLICE_SIZE, 0f, 0f),
        ROOT.child(667.675f, 246f, SLICE_SIZE, SLICE_SIZE, 0f, 0f),
        ROOT.child(574.675f, 464f, SLICE_SIZE, SLICE_SIZE, 0f, 0f),
        ROOT.child(650.675f, 676f, SLICE_SIZE, SLICE_SIZE, 0f, 0f),
    };

    /** _KW_IMG_SELECT 386x492，八格位置各不相同（格子局部坐标，Cocos）。 */
    private static final float[][] SELECT_POS = {
        {102.46f, 16f}, {35f, 39f}, {25f, 90f}, {41f, 147f},
        {101f, 196f}, {171f, 150f}, {198f, 94f}, {189f, 42f},
    };

    /** 第 5 格的图标与文字整体上移，是 CSB 里的既有例外。 */
    private static final int RAISED_SLICE_INDEX = 4;

    static Box slice(int index) {
        return SLICES[index].box();
    }

    static Box selectGlow(int index) {
        return SLICES[index].child(SELECT_POS[index][0], SELECT_POS[index][1], 386f, 492f, 0.5f, 0.5f)
                .box();
    }

    /** _KW_ITEM_IMAGE pos(101,44)（第 5 格为 (101,134)） size 184x139。 */
    static Box rewardIcon(int index) {
        float posY = index == RAISED_SLICE_INDEX ? 134f : 44f;
        return SLICES[index].child(101f, posY, 184f, 139f, 0.5f, 0.5f).box();
    }

    /**
     * 奖励文字的垂直中心。第 5 格用 CSB 的 22.5058，其余用 143.507，
     * 都是格子局部 Cocos Y。
     */
    static float rewardTextCenterY(int index) {
        float posY = index == RAISED_SLICE_INDEX ? 22.5058f : 143.507f;
        return slice(index).top() + (SLICE_SIZE - posY);
    }

    /**
     * WheelView.lua:100-102：数量与名称两段文字整体以格子局部 x=101 为中心排布，
     * 不用 CSB 里的静态 X。
     */
    static float rewardTextLeft(int index, float totalWidth) {
        return slice(index).left() + 101f - totalWidth * 0.5f;
    }

    static final float REWARD_COUNT_FONT_SIZE = 50f;
    static final float REWARD_NAME_FONT_SIZE = 36f;
    static final int REWARD_TEXT_COLOR = 0xFF292D56;

    /** WheelView.lua:138：目标角度。 */
    static float targetDegrees(int sliceIndex) {
        return ROLL_BASE_DEGREES + (sliceIndex % SLICE_COUNT) * DEGREES_PER_SLICE;
    }

    /** cc.EaseQuadraticActionOut：1 - (1-t)^2。 */
    static float easeQuadraticOut(float progress) {
        float clamped = Math.max(0f, Math.min(1f, progress));
        float inverse = 1f - clamped;
        return 1f - inverse * inverse;
    }

    /** 旋转到 {@code sliceIndex} 的当前角度。 */
    static float rollDegrees(int sliceIndex, float elapsedSeconds, float durationSeconds) {
        float progress = durationSeconds <= 0f ? 1f : elapsedSeconds / durationSeconds;
        return targetDegrees(sliceIndex) * easeQuadraticOut(progress);
    }

    /** WheelView.lua:189-196：旋转态按当前角度点亮 floor(rotation/45) 那一格。 */
    static int highlightedSlice(float degrees) {
        float normalized = degrees % 360f;
        if (normalized < 0f) {
            normalized += 360f;
        }
        return (int) (normalized / DEGREES_PER_SLICE);
    }
}
