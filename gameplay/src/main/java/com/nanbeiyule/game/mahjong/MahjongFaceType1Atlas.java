package com.nanbeiyule.game.mahjong;

/**
 * {@code cocosStudio/MahjongNew/MahLayer/Image/mahlayer_mah_face_1.plist} 的帧表：牌花 1。
 *
 * <p>与区包的牌花 2（{@link TaizhouMahjongFaceAtlas}）不同，这张图集里的帧没有旋转，
 * 直接按 {@code 140x158} 读。
 */
public final class MahjongFaceType1Atlas {
    public static final int ATLAS_WIDTH = 1990;

    public static final int ATLAS_HEIGHT = 482;

    /** {@code {tileId, atlasX, atlasY}}，按牌值排序。 */
    public static final int[][] FRAMES = {
        {17, 1706, 2}, // 0x11 万1
        {18, 1564, 322}, // 0x12 万2
        {19, 1564, 162}, // 0x13 万3
        {20, 1564, 2}, // 0x14 万4
        {21, 1422, 322}, // 0x15 万5
        {22, 1422, 162}, // 0x16 万6
        {23, 1422, 2}, // 0x17 万7
        {24, 1280, 322}, // 0x18 万8
        {25, 1280, 162}, // 0x19 万9
        {33, 1280, 2}, // 0x21 条1
        {34, 1138, 322}, // 0x22 条2
        {35, 1138, 162}, // 0x23 条3
        {36, 1138, 2}, // 0x24 条4
        {37, 996, 322}, // 0x25 条5
        {38, 996, 162}, // 0x26 条6
        {39, 996, 2}, // 0x27 条7
        {40, 854, 322}, // 0x28 条8
        {41, 854, 162}, // 0x29 条9
        {49, 854, 2}, // 0x31 筒1
        {50, 712, 322}, // 0x32 筒2
        {51, 712, 162}, // 0x33 筒3
        {52, 712, 2}, // 0x34 筒4
        {53, 570, 322}, // 0x35 筒5
        {54, 570, 162}, // 0x36 筒6
        {55, 570, 2}, // 0x37 筒7
        {56, 428, 322}, // 0x38 筒8
        {57, 428, 162}, // 0x39 筒9
        {65, 428, 2}, // 0x41 风1
        {66, 286, 322}, // 0x42 风2
        {67, 286, 162}, // 0x43 风3
        {68, 286, 2}, // 0x44 风4
        {81, 144, 322}, // 0x51 箭1
        {82, 144, 162}, // 0x52 箭2
        {83, 144, 2}, // 0x53 箭3
        {97, 2, 322}, // 0x61 花1
        {98, 2, 162}, // 0x62 花2
        {99, 2, 2}, // 0x63 花3
        {100, 1848, 322}, // 0x64 花4
        {101, 1706, 322}, // 0x65 花5
        {102, 1848, 162}, // 0x66 花6
        {103, 1706, 162}, // 0x67 花7
        {104, 1848, 2}, // 0x68 花8
    };

    private MahjongFaceType1Atlas() {}

    /** 返回 {@code tileId} 在图集里的原点，没有则返回 null。 */
    public static int[] frameOf(int tileId) {
        for (int[] frame : FRAMES) {
            if (frame[0] == tileId) {
                return new int[] {frame[1], frame[2]};
            }
        }
        return null;
    }
}
