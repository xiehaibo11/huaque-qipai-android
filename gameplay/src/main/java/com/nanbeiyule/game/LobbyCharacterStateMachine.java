package com.nanbeiyule.game;

/**
 * 闲逸斗地主原版大厅人物的动画状态机。
 *
 * <p>行为逐条对应原版 {@code prime/hall/style/HallStyleBaseLayer.lua}：
 *
 * <ul>
 *   <li>573-577 {@code refreshPeopleAtlas}：{@code bkWidth = width / height * 750}，
 *       小于 1330 直接 return，不加载人物。
 *   <li>580-600：性别 1 男加载 {@code renwunan}，2 女加载 {@code renwunv}；两者都以
 *       {@code setAnimation(0, "animation", true)} 进入站立状态，循环播放。
 *   <li>601-645 {@code update(dt)}：按累计时间切换状态，男 20 → 4.3 → 9，女 20 → 4.5，
 *       每次切换把累计时间置零（不扣除溢出量），且每次 update 最多切换一次。
 * </ul>
 *
 * <p>阈值取原版 Lua 字面量而不是骨架动画的真实时长（男 animation2 实际 4.367 秒、
 * animation3 实际 8.0 秒），因为原版就是用这组字面量驱动的，改成动画时长会偏离原版节奏。
 */
final class LobbyCharacterStateMachine {

    enum Gender {
        MALE("renwunan"),
        FEMALE("renwunv");

        private final String skeletonName;

        Gender(String skeletonName) {
            this.skeletonName = skeletonName;
        }

        String skeletonName() {
            return skeletonName;
        }

        /** 原版 {@code m_peopleGender}：1 男 2 女。 */
        static Gender fromOriginalCode(int code) {
            return switch (code) {
                case 1 -> MALE;
                case 2 -> FEMALE;
                default -> throw new IllegalArgumentException(
                        "Original people gender must be 1 or 2, found " + code);
            };
        }
    }

    /** 原版 {@code stateEnumeration}。 */
    private enum State {
        STAND,
        ACTION_1,
        ACTION_2
    }

    /** 原版显示门槛：设计宽度低于该值不加载人物。 */
    static final float MINIMUM_DESIGN_WIDTH = 1330.0f;

    /** 原版设计高度基准，{@code bkWidth} 由它换算。 */
    private static final float DESIGN_HEIGHT = 750.0f;

    private static final float STAND_SECONDS = 20.0f;
    private static final float MALE_ACTION_1_SECONDS = 4.3f;
    private static final float MALE_ACTION_2_SECONDS = 9.0f;
    private static final float FEMALE_ACTION_1_SECONDS = 4.5f;

    private final Gender gender;
    private State state = State.STAND;
    private float elapsedSeconds;

    LobbyCharacterStateMachine(Gender gender) {
        this.gender = gender;
    }

    /**
     * 原版 {@code refreshPeopleAtlas} 的显示门槛。
     *
     * @param viewportWidthPixels 实际窗口宽度像素
     * @param viewportHeightPixels 实际窗口高度像素
     */
    static boolean shouldRender(int viewportWidthPixels, int viewportHeightPixels) {
        if (viewportWidthPixels <= 0 || viewportHeightPixels <= 0) {
            return false;
        }
        float designWidth =
                (float) viewportWidthPixels / (float) viewportHeightPixels * DESIGN_HEIGHT;
        return designWidth >= MINIMUM_DESIGN_WIDTH;
    }

    Gender gender() {
        return gender;
    }

    String skeletonName() {
        return gender.skeletonName();
    }

    /** 当前应该播放的骨架动画名。 */
    String animationName() {
        return switch (state) {
            case STAND -> "animation";
            case ACTION_1 -> gender == Gender.MALE ? "animation2" : "animation1";
            case ACTION_2 -> "animation3";
        };
    }

    /** 自本次状态切换以来的累计秒数，等价于原版 {@code m_dt}，可直接喂给骨架采样。 */
    float animationTime() {
        return elapsedSeconds;
    }

    /** 原版三个状态都以 {@code setAnimation(track, name, true)} 循环播放。 */
    boolean looping() {
        return true;
    }

    /** 对应原版 {@code update(dt)}，每次调用最多切换一次状态。 */
    void advance(float deltaSeconds) {
        if (!(deltaSeconds > 0.0f)) {
            return;
        }
        elapsedSeconds += deltaSeconds;
        switch (state) {
            case STAND -> {
                if (elapsedSeconds > STAND_SECONDS) {
                    enter(State.ACTION_1);
                }
            }
            case ACTION_1 -> {
                float threshold =
                        gender == Gender.MALE
                                ? MALE_ACTION_1_SECONDS
                                : FEMALE_ACTION_1_SECONDS;
                if (elapsedSeconds > threshold) {
                    // 女性没有第二个动作状态，直接回到站立。
                    enter(gender == Gender.MALE ? State.ACTION_2 : State.STAND);
                }
            }
            case ACTION_2 -> {
                if (elapsedSeconds > MALE_ACTION_2_SECONDS) {
                    enter(State.STAND);
                }
            }
        }
    }

    private void enter(State next) {
        state = next;
        elapsedSeconds = 0.0f;
    }
}
