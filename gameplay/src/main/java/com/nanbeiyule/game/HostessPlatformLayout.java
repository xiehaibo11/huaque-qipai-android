package com.nanbeiyule.game;

/**
 * 大厅人物底座在 3200×1792 设计空间中的落位。
 *
 * <p>底座 {@code game_home_final_hostess_platform.png} 是南北娱乐现代美术资源，不是浙江
 * 游戏大厅原版证据，因此落位不存在可对照的原版 CSB 节点，只能按“人物站在顶面正中”这一
 * 视觉约束求解。
 *
 * <p>位图是 1455×491 的透视圆台：顶面是一个椭圆，其下方还有两级台阶和外圈光晕。按位图
 * 边框对齐会把人物踩到台阶前沿上，所以这里用顶面椭圆中心作为锚点。
 *
 * <p>位图由 {@code android/tools/build_hostess_platform_asset.py} 从完整原图按 alpha
 * 包围盒裁切生成，不重采样，因此柔光和宽高比与原图一致；上一版是从带背景图上抠出来的，
 * 外圈柔光被硬边切掉且纵向被拉伸 11.1%。
 *
 * <p>椭圆中心由同一脚本实测并写入 {@code manifest-hostess-platform.json}：以顶面内部色
 * （高蓝、低红的亮青渐变）为判据逐行取最长连续段，最宽行横跨 x 366..1090，中心 x=728；
 * 再在该列上下扫描得 80px 与 97px，中心 y=142.5，竖直半轴 88.5。
 * {@code GameHomeFinalAssetsTest} 用同一判据复测该锚点，位图一旦替换即失败。
 */
final class HostessPlatformLayout {

    /** 位图原始宽，由 {@code GameHomeFinalAssetsTest} 锁定。 */
    static final int SOURCE_WIDTH = 1455;

    /** 位图原始高，由 {@code GameHomeFinalAssetsTest} 锁定。 */
    static final int SOURCE_HEIGHT = 491;

    /** 顶面椭圆中心在位图中的 X，实测最宽行 x 366..1090 的中点。 */
    static final float TOP_SURFACE_CENTER_X = 728.0f;

    /** 顶面椭圆中心在位图中的 Y，最宽行所在列上下扫描 80/97px 的中点。 */
    static final float TOP_SURFACE_CENTER_Y = 142.5f;

    /** 顶面椭圆竖直半轴实测值，脚底余量判定用。 */
    static final float TOP_SURFACE_SEMI_AXIS_Y = 88.5f;

    /** 底座在设计空间中的绘制宽度，按人物身高比例选定。 */
    static final float WIDTH = 860.0f;

    private HostessPlatformLayout() {}

    /** 底座等比缩放后的绘制高度。 */
    static float height() {
        return WIDTH * SOURCE_HEIGHT / SOURCE_WIDTH;
    }

    /**
     * 按“顶面椭圆中心落在人物站立点”求底座矩形。
     *
     * <p>站立点是 {@link LobbyCharacterLayout#STANCE_CENTER_X} 与
     * {@link LobbyCharacterLayout#SILHOUETTE_BOTTOM}，即双脚水平中心与脚底线。
     */
    static GameHomeV3Layout.Box place() {
        float height = height();
        float left =
                LobbyCharacterLayout.STANCE_CENTER_X
                        - WIDTH * (TOP_SURFACE_CENTER_X / SOURCE_WIDTH);
        float top =
                LobbyCharacterLayout.SILHOUETTE_BOTTOM
                        - height * (TOP_SURFACE_CENTER_Y / SOURCE_HEIGHT);
        return new GameHomeV3Layout.Box(left, top, left + WIDTH, top + height);
    }
}
