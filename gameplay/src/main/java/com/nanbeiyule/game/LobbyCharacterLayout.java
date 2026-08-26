package com.nanbeiyule.game;

/**
 * 把原版大厅人物骨架落到南北娱乐 3200×1792 设计空间。
 *
 * <p>原版 {@code prime/hall/style/HallStyleBaseLayer.lua:588-597} 以
 * {@code skeletonScale = 1} 加载骨架并 {@code setPosition(0, -40)}，父节点
 * {@code uiroot.ad_panel.hall_icon} 再 {@code setScale(0.9)}，全程等比。
 *
 * <p>该父节点不在仓库持有的 {@code HallStyle1Scene.csb} 版本内（运行时用的是
 * {@code hallstyle/style1} 热更覆盖版），所以绝对坐标无法从 CSB 无损恢复。这里的剪影框
 * 来自实机截图 {@code android/assets/ui/game-home-final/xianyi-device-screen.png}
 * （1600×900）的像素测量，按 3200/1600 与 1792/900 换算，属于截图校准推断，
 * 不是已确认的原版 CSB 几何。
 */
final class LobbyCharacterLayout {

    /** 实测 x=287（1600 宽）→ 574（3200 宽）。 */
    static final float SILHOUETTE_LEFT = 574.0f;

    /** 实测 x=406 → 812。 */
    static final float SILHOUETTE_RIGHT = 812.0f;

    /** 实测 y=259（900 高）→ 515.70（1792 高）。 */
    static final float SILHOUETTE_TOP = 515.70f;

    /** 实测 y=688 → 1369.88，即人物脚底线。 */
    static final float SILHOUETTE_BOTTOM = 1369.88f;

    /**
     * 站立姿势双脚落点的水平中心。
     *
     * <p>{@link #place} 把骨架的绘制包围盒水平居中到剪影框中心 693，但包围盒还包含向左
     * 侧展开的头发与手臂，因此双脚实际落在包围盒中心右侧。底座要看起来被踩在正中，锚点
     * 必须用双脚中心而不是包围盒中心。
     *
     * <p>取值来自实机截图 {@code image copy 5.png}（2464×1462 窗口，页面变换 scaleX
     * 0.77000、scaleY 0.77344）中鞋面像素的测量：双脚横跨设计 x 659.7..770.1，中心
     * 714.9。属于截图校准推断，不是已确认的原版 CSB 几何；换骨架、换静止动画或改
     * {@link #SKELETON_SCALE} 后必须重新测量。
     */
    static final float STANCE_CENTER_X = 715.0f;

    /** 原版 {@code hall_icon:setScale(0.9)}，骨架本身以 {@code skeletonScale = 1} 加载。 */
    static final float ORIGINAL_HALL_ICON_SCALE = 0.9f;

    /** 原版 {@code HallStyle1Scene.csb} 的 Scene 设计宽度。 */
    static final float ORIGINAL_DESIGN_WIDTH = 1334.0f;

    /** 南北娱乐大厅设计宽度。 */
    static final float DESIGN_WIDTH = 3200.0f;

    /**
     * 仅按可读到的原版参数推导的缩放：骨架 {@code skeletonScale = 1}、
     * {@code hall_icon:setScale(0.9)}、设计分辨率 {@code 1334×750 EXACT_FIT}
     * （证据 {@code game/app/AppModel.lua:995}），换算到 3200 宽设计空间。
     */
    static final float PARAMETER_SCALE =
            ORIGINAL_HALL_ICON_SCALE * (DESIGN_WIDTH / ORIGINAL_DESIGN_WIDTH);

    /**
     * 实机校准系数。
     *
     * <p>按 {@link #PARAMETER_SCALE} 渲染后，人物在 1600×900 上的剪影高度为 609px，
     * 而原版实机证据 {@code xianyi-device-screen.png} 同口径测量只有 429px，比值 0.7044。
     * 差异说明 {@code uiroot.ad_panel.hall_icon} 节点自身还带一层 CSB 缩放，而该节点不在
     * 仓库持有的 {@code HallStyle1Scene.csb} 版本内，无法无损读出。因此尺寸以实机证据为准，
     * 该系数是截图校准推断，不得描述成已确认的原版节点参数。
     */
    static final float DEVICE_CALIBRATION = 429.0f / 609.0f;

    /** 骨架单位到设计单位的最终缩放。 */
    static final float SKELETON_SCALE = PARAMETER_SCALE * DEVICE_CALIBRATION;

    private LobbyCharacterLayout() {}

    /**
     * 骨架空间到设计空间的等比变换。
     *
     * <p>Spine 使用 Y 轴向上的坐标系，设计空间 Y 轴向下，因此 {@link #toScreenY} 取负。
     */
    record Placement(float scale, float offsetX, float offsetY) {
        float toScreenX(float skeletonX) {
            return offsetX + skeletonX * scale;
        }

        float toScreenY(float skeletonY) {
            return offsetY - skeletonY * scale;
        }
    }

    /**
     * 按原版缩放落位：尺寸取 {@link #SKELETON_SCALE}，位置把骨架实际绘制包围盒的
     * 底边贴到实测脚底线、水平中心对齐实测剪影框中心。
     *
     * <p>尺寸来自原版参数推导，位置来自实机截图校准，两者边界不同，不得混写。
     *
     * @param boundsX 骨架绘制包围盒左边（Spine 坐标）
     * @param boundsY 骨架绘制包围盒底边（Spine 坐标，Y 向上）
     * @param boundsWidth 骨架绘制包围盒宽
     * @param boundsHeight 骨架绘制包围盒高
     */
    static Placement place(
            float boundsX, float boundsY, float boundsWidth, float boundsHeight) {
        if (boundsWidth <= 0.0f || boundsHeight <= 0.0f) {
            throw new IllegalArgumentException("Skeleton bounds must have positive size");
        }
        float scale = SKELETON_SCALE;

        float centreX = (SILHOUETTE_LEFT + SILHOUETTE_RIGHT) / 2.0f;
        float skeletonCentreX = boundsX + boundsWidth / 2.0f;
        float offsetX = centreX - skeletonCentreX * scale;

        // 脚底对齐：设计空间的 SILHOUETTE_BOTTOM 必须落在骨架包围盒底边。
        float offsetY = SILHOUETTE_BOTTOM + boundsY * scale;

        return new Placement(scale, offsetX, offsetY);
    }
}
