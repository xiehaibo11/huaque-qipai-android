package com.nanbeiyule.game;

import java.util.List;

/** Header geometry measured from the user-supplied 3200 x 1792 lobby reference. */
final class GameHomeOriginalHeaderLayout {
    private static final float ORIGINAL_SCALE_X = 3200.0f / 1920.0f;
    private static final float ORIGINAL_SCALE_Y = 1792.0f / 1080.0f;

    record ReferenceCrop(
            String key,
            GameHomeV3Layout.Box source,
            GameHomeV3Layout.Box destination) {}

    private final GameHomeV3Layout.Box page = box(0, 0, 3200, 1792);
    private final GameHomeV3Layout.Box header = box(0, 0, 2450, 224);
    private final GameHomeV3Layout.Box springBackgroundSource =
            box(
                    210,
                    0,
                    210 + header.width() / ORIGINAL_SCALE_X,
                    header.height() / ORIGINAL_SCALE_Y);
    private final GameHomeV3Layout.Box headerFrameDestination =
            box(78, 18, 708, 214);

    private final GameHomeV3Layout.Box avatarAssembly = box(13, 10, 197, 194);
    private final GameHomeV3Layout.Box avatarImage = box(27, 24, 183, 180);
    private final GameHomeV3Layout.Box nickname = box(258, 28, 650, 108);
    private final GameHomeV3Layout.Box playerId = box(258, 118, 630, 186);

    // 顶栏几何按 2026-08-07 用户提供的 2434 宽参考截图重校准（宽度比 3200/2434）：
    // 头像贴近左缘，昵称（手机用户号）与 ID 左对齐 x=258；昵称旁的 V3 徽章
    // 应用户要求于 2026-08-07 移除，不再绘制也不保留几何。
    // 货币组布局按闲逸 HallStyle1Scene.csb 的 node_gold/node_dou/node_card 结构
    // 并以 1600x900 实机截图（xianyi-device-screen.png）逐像素校准：
    // 140x35 半透明深色胶囊（money_bg，hall_icon_bg.png）固定宽，白色加号徽章
    // （hall_add_btn.png）中心落在胶囊左缘、略低于中线，胶囊内只绘制数值
    // （无货币名文本），三组等距排列；货币图标按内容高度对齐原版（房卡卡券
    // 76、金币 96、钻石 96），内容右缘统一越过胶囊左缘 8px，保证整行图标
    // 视觉尺寸与压边量一致。
    private final GameHomeV3Layout.Box regionGroup = box(781, 28, 1038, 122);
    private final GameHomeV3Layout.Box locationIcon = box(788, 21, 898, 131);
    private final GameHomeV3Layout.Box regionTrack = box(843, 34, 1038, 116);
    private final GameHomeV3Layout.Box regionValue = box(891, 34, 1033, 116);

    private final GameHomeV3Layout.Box roomCardGroup = box(1196, 12, 1578, 138);
    private final GameHomeV3Layout.Box roomCardIcon = box(1196, 12, 1322, 138);
    private final GameHomeV3Layout.Box roomCardTrack = box(1306, 34, 1578, 116);
    private final GameHomeV3Layout.Box roomCardValue = box(1378, 34, 1578, 116);
    private final GameHomeV3Layout.Box roomCardPlus = box(1281, 70, 1331, 120);
    // 房卡加号命中区：在加号位图基础上向外扩 30 设计单位便于点按，
    // 点击后跳转商城房卡分类。
    private final GameHomeV3Layout.Box roomCardPlusHit = box(1251, 40, 1361, 150);

    private final GameHomeV3Layout.Box coinGroup = box(1666, 20, 1994, 130);
    private final GameHomeV3Layout.Box coinIcon = box(1666, 20, 1775, 129);
    private final GameHomeV3Layout.Box coinTrack = box(1722, 34, 1994, 116);
    private final GameHomeV3Layout.Box coinValue = box(1794, 34, 1994, 116);
    private final GameHomeV3Layout.Box coinPlus = box(1697, 70, 1747, 120);
    // 金币加号命中区：在加号位图基础上向外扩 30 设计单位便于点按，
    // 点击后跳转商城金币分类。
    private final GameHomeV3Layout.Box coinPlusHit = box(1667, 40, 1777, 150);

    private final GameHomeV3Layout.Box diamondGroup = box(2053, 16, 2414, 134);
    private final GameHomeV3Layout.Box diamondIcon = box(2053, 16, 2171, 134);
    private final GameHomeV3Layout.Box diamondTrack = box(2154, 34, 2414, 116);
    private final GameHomeV3Layout.Box diamondValue = box(2226, 34, 2414, 116);
    private final GameHomeV3Layout.Box diamondPlus = box(2129, 70, 2179, 120);
    // 用户点击钻石钱包元素时进入商城钻石充值页；命中区向外扩到截图标记范围。
    private final GameHomeV3Layout.Box diamondPlusHit = box(1995, 0, 2425, 160);

    private final List<ReferenceCrop> referenceCrops =
            List.of(crop("AVATAR_ASSEMBLY", 0, 0, 202, 211));

    private final List<GameHomeV3Layout.Box> dynamicBoxes =
            List.of(
                    avatarAssembly,
                    avatarImage,
                    nickname,
                    playerId,
                    regionGroup,
                    locationIcon,
                    regionTrack,
                    regionValue,
                    roomCardGroup,
                    roomCardIcon,
                    roomCardTrack,
                    roomCardValue,
                    roomCardPlus,
                    roomCardPlusHit,
                    coinGroup,
                    coinIcon,
                    coinTrack,
                    coinValue,
                    coinPlus,
                    coinPlusHit,
                    diamondGroup,
                    diamondIcon,
                    diamondTrack,
                    diamondValue,
                    diamondPlus,
                    diamondPlusHit);

    GameHomeV3Layout.Box page() {
        return page;
    }

    GameHomeV3Layout.Box header() {
        return header;
    }

    GameHomeV3Layout.Box springBackgroundSource() {
        return springBackgroundSource;
    }

    GameHomeV3Layout.Box headerFrameDestination() {
        return headerFrameDestination;
    }

    GameHomeV3Layout.Box avatarAssembly() {
        return avatarAssembly;
    }

    GameHomeV3Layout.Box avatarImage() {
        return avatarImage;
    }

    GameHomeV3Layout.Box nickname() {
        return nickname;
    }

    GameHomeV3Layout.Box playerId() {
        return playerId;
    }

    GameHomeV3Layout.Box regionGroup() {
        return regionGroup;
    }

    GameHomeV3Layout.Box locationIcon() {
        return locationIcon;
    }

    GameHomeV3Layout.Box regionTrack() {
        return regionTrack;
    }

    GameHomeV3Layout.Box regionValue() {
        return regionValue;
    }

    GameHomeV3Layout.Box roomCardGroup() {
        return roomCardGroup;
    }

    GameHomeV3Layout.Box roomCardIcon() {
        return roomCardIcon;
    }

    GameHomeV3Layout.Box roomCardTrack() {
        return roomCardTrack;
    }

    GameHomeV3Layout.Box roomCardValue() {
        return roomCardValue;
    }

    GameHomeV3Layout.Box roomCardPlus() {
        return roomCardPlus;
    }

    GameHomeV3Layout.Box roomCardPlusHit() {
        return roomCardPlusHit;
    }

    GameHomeV3Layout.Box coinGroup() {
        return coinGroup;
    }

    GameHomeV3Layout.Box coinIcon() {
        return coinIcon;
    }

    GameHomeV3Layout.Box coinTrack() {
        return coinTrack;
    }

    GameHomeV3Layout.Box coinValue() {
        return coinValue;
    }

    GameHomeV3Layout.Box coinPlus() {
        return coinPlus;
    }

    GameHomeV3Layout.Box coinPlusHit() {
        return coinPlusHit;
    }

    GameHomeV3Layout.Box diamondGroup() {
        return diamondGroup;
    }

    GameHomeV3Layout.Box diamondIcon() {
        return diamondIcon;
    }

    GameHomeV3Layout.Box diamondTrack() {
        return diamondTrack;
    }

    GameHomeV3Layout.Box diamondValue() {
        return diamondValue;
    }

    GameHomeV3Layout.Box diamondPlus() {
        return diamondPlus;
    }

    GameHomeV3Layout.Box diamondPlusHit() {
        return diamondPlusHit;
    }

    List<ReferenceCrop> referenceCrops() {
        return referenceCrops;
    }

    List<GameHomeV3Layout.Box> dynamicBoxes() {
        return dynamicBoxes;
    }

    private static ReferenceCrop crop(
            String key,
            float left,
            float top,
            float right,
            float bottom) {
        GameHomeV3Layout.Box box = box(left, top, right, bottom);
        return new ReferenceCrop(key, box, box);
    }

    private static GameHomeV3Layout.Box box(
            float left,
            float top,
            float right,
            float bottom) {
        return new GameHomeV3Layout.Box(left, top, right, bottom);
    }
}
