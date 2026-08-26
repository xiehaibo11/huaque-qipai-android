package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;

/** Immutable geometry for the approved Zhejiang lobby composition. */
final class GameHomeV3Layout {
    record Box(float left, float top, float right, float bottom) {
        Box {
            if (right <= left || bottom <= top) {
                throw new IllegalArgumentException("Box must have positive size");
            }
        }

        float width() {
            return right - left;
        }

        float height() {
            return bottom - top;
        }

        float centerX() {
            return (left + right) / 2.0f;
        }

        float centerY() {
            return (top + bottom) / 2.0f;
        }

        boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }

        boolean contains(Box other) {
            return other.left >= left
                    && other.top >= top
                    && other.right <= right
                    && other.bottom <= bottom;
        }

        boolean intersects(Box other) {
            return left < other.right
                    && right > other.left
                    && top < other.bottom
                    && bottom > other.top;
        }
    }

    record Tile(String key, Box source, Box destination, Box hit) {
        Tile(String key, Box destination) {
            this(key, destination, destination, destination);
        }

        Tile(String key, Box destination, Box hit) {
            this(key, destination, destination, hit);
        }
    }

    private static final Box PAGE = box(0, 0, 3200, 1792);
    private static final Box SHEET = PAGE;
    private static final Box PLAY_CARD_STAGE = box(900, 202, 3200, 1522);
    private static final float PLAY_CARD_WIDTH = 530.0f;
    private static final float PLAY_CARD_HEIGHT = 397.5f;
    private static final float PLAY_CARD_GAP_X = 40.0f;
    private static final float PLAY_CARD_GAP_Y = 130.0f;
    private static final float PRIMARY_ROW_WIDTH =
            PLAY_CARD_WIDTH * 3.0f + PLAY_CARD_GAP_X * 2.0f;
    private static final float GAME_ROW_WIDTH =
            PLAY_CARD_WIDTH * 3.0f + PLAY_CARD_GAP_X * 2.0f;
    private static final float PLAY_CARD_MATRIX_HEIGHT =
            PLAY_CARD_HEIGHT * 2.0f + PLAY_CARD_GAP_Y;
    private static final float PLAY_CARD_MATRIX_TOP =
            PLAY_CARD_STAGE.top()
                    + (PLAY_CARD_STAGE.height() - PLAY_CARD_MATRIX_HEIGHT) / 2.0f;

    private final Box playerPanel = box(10, 8, 735, 226);
    private final Box walletPanel = box(741, 66, 2408, 164);
    private final Box friendPanel = box(30, 295, 440, 1409);
    private final Box competitionPanel = box(900, 230, 2582.5f, 790);
    private final Box casualPanel = box(900, 810, 3150, 1390);
    // 人物保持在左中区域，右侧为四列玩法舞台；删除比赛场侧边入口后，
    // 其余四个左侧入口维持原坐标。
    private final Box character = box(500, 390, 850, 1370);
    // 用户的浙江大厅下方裁图（1766px 宽）只用于控制条横向比例与槽位中心。它的
    // 木地板是背景层，不能进入本布局或透明控制条位图。整个底部组合仍使用共享的
    // 3200 x 1792 页面坐标：暗色条 x≈236..2292，商城车叠在左端，快速开始独立在右端。
    private final Box bottomBar = box(0, 1490, 2300, 1792);

    // 顶栏五个入口改为纯图标（无文字），视觉尺寸对齐闲逸斗地主顶栏单个
    // 图标（42-47 设计点 @1334x750 ≈ 103 设计单位 @3200x1792，证据见
    // artifacts/xianyi-doudizhu/HALL-TOP-BAR-EVIDENCE.md）；垂直中心与
    // 钱包胶囊中心 75 对齐（闲逸实机截图中顶栏图标与 money_bg 同一水平线，
    // 中心约 y=35 @1600x900），命中区保留原 154x190 槽位保证触控面积。
    private final List<Tile> topActions =
            List.of(
                    tile(
                            "MEMBERSHIP",
                            box(2455, 23, 2559, 127),
                            box(2430, 12, 2584, 202)),
                    tile(
                            "WELFARE",
                            box(2610, 23, 2714, 127),
                            box(2584, 12, 2740, 202)),
                    tile(
                            "MAIL",
                            box(2764, 23, 2868, 127),
                            box(2738, 12, 2894, 202)),
                    tile(
                            "CUSTOMER_SERVICE",
                            box(2918, 23, 3022, 127),
                            box(2892, 12, 3048, 202)),
                    tile(
                            "SETTINGS",
                            box(3070, 23, 3174, 127),
                            box(3046, 12, 3198, 202)));

    private final List<Tile> sideActions =
            List.of(
                    tile("SIDE_WELFARE", 42, 500, 300, 704),
                    tile("SIDE_CHEST", 42, 730, 300, 934),
                    tile("SIDE_CHECKIN", 42, 960, 300, 1164));

    // 用户 8/17 提供的玩法切图为 4:3：第一排是创建、加入、比赛，
    // 第二排是台州、十三水、挖花。两排都是 3 张、同宽同列，在人物右侧
    // PLAY_CARD_STAGE 内居中。创建房间只有一态，不再叠加返回房间贴图。
    private final List<Tile> primaryEntries =
            List.of(
                    playCard("CREATE_ROOM", 0, 0),
                    playCard("JOIN_ROOM", 1, 0),
                    playCard("COMPETITION", 2, 0));

    // 返回房间是创建房间格位的第二层，坐标、尺寸、命中区完全复用第一层。
    // 对应原版 _KW_IMG_BOX_ROOM_TITLE 挂在 _KWA_BTND_CREATE_BACK_BOX_ROOM 之下。
    private final Tile primaryBackRoom =
            playCard("CREATE_ROOM", 0, 0);

    private final List<Tile> gameEntries =
            List.of(
                    playCard("TAIZHOU_MAHJONG", 0, 1),
                    playCard("SHI_SAN_SHUI", 1, 1),
                    playCard("WA_HUA", 2, 1));

    // 浙江目标的八个槽位依次是商城、装扮、战绩、活动、分享、背包、邮件、更多。
    // 分界来自用户目标截图的标签中心换算（而不是已移除的闲逸条位图）；命中区与
    // 可见标签都由这个类给出，因此没有第二套触控几何。
    private final Tile store =
            tile("STORE", box(0, 1490, 420, 1792), box(0, 1518, 420, 1792));
    private final Box storeArtwork = box(14, 1472, 354, 1732);
    private final Box storeLabel = box(0, 1650, 420, 1790);

    private final List<Tile> bottomActions =
            List.of(
                    tile("DRESS_UP", 420, 1530, 680, 1792),
                    tile("RECORDS", 680, 1530, 950, 1792),
                    tile("ACTIVITIES", 950, 1530, 1215, 1792),
                    tile("SHARE", 1215, 1530, 1483, 1792),
                    tile("BAG", 1483, 1530, 1750, 1792),
                    tile("MAIL", 1750, 1530, 2022, 1792),
                    tile("MORE", 2022, 1530, 2300, 1792));

    // QuickStartBtn.csb 的原始 Btn_anniu (428x173) 宽高比为 2.474。这里保持同一比率，
    // 右缘与玩法舞台对齐；_txtGameName 的副标题和 Img_KS 标题同样拥有布局中的专属框。
    private final Tile quickStart =
            tile(
                    "QUICK_START",
                    box(2350, 1472, 3095, 1773),
                    box(2338, 1460, 3120, 1790));
    private final Box quickStartLabel = box(2493, 1492, 2952, 1632);
    private final Box quickStartSubtitle = box(2440, 1640, 3010, 1734);

    Box page() {
        return PAGE;
    }

    Box sheet() {
        return SHEET;
    }

    Box playerPanel() {
        return playerPanel;
    }

    Box walletPanel() {
        return walletPanel;
    }

    Box friendPanel() {
        return friendPanel;
    }

    Box competitionPanel() {
        return competitionPanel;
    }

    Box casualPanel() {
        return casualPanel;
    }

    Box bottomBar() {
        return bottomBar;
    }

    Box character() {
        return character;
    }

    List<Tile> topActions() {
        return topActions;
    }

    List<Tile> sideActions() {
        return sideActions;
    }

    List<Tile> primaryEntries() {
        return primaryEntries;
    }

    Tile primaryBackRoom() {
        return primaryBackRoom;
    }

    List<Tile> gameEntries() {
        return gameEntries;
    }

    Tile store() {
        return store;
    }

    Box storeArtwork() {
        return storeArtwork;
    }

    Box bottomLabelBounds(Tile tile) {
        return "STORE".equals(tile.key())
                ? storeLabel
                : box(
                        tile.destination().left(),
                        1580,
                        tile.destination().right(),
                        1738);
    }

    List<Tile> bottomActions() {
        return bottomActions;
    }

    List<Tile> navigationSlots() {
        List<Tile> slots = new ArrayList<>();
        slots.add(store);
        slots.addAll(bottomActions);
        return List.copyOf(slots);
    }

    Tile quickStart() {
        return quickStart;
    }

    Box quickStartLabel() {
        return quickStartLabel;
    }

    Box quickStartSubtitle() {
        return quickStartSubtitle;
    }

    List<Tile> allTiles() {
        List<Tile> tiles = new ArrayList<>();
        tiles.addAll(topActions);
        tiles.addAll(sideActions);
        tiles.addAll(primaryEntries);
        tiles.addAll(gameEntries);
        tiles.add(store);
        tiles.addAll(bottomActions);
        tiles.add(quickStart);
        return List.copyOf(tiles);
    }

    static boolean shouldDrawDynamicLabel(
            String artworkLabel,
            String dynamicLabel) {
        return dynamicLabel != null
                && !dynamicLabel.trim().isEmpty()
                && !dynamicLabel.trim().equals(artworkLabel);
    }

    private static Tile tile(
            String key,
            float left,
            float top,
            float right,
            float bottom) {
        return new Tile(key, box(left, top, right, bottom));
    }

    private static Tile playCard(String key, int column, int row) {
        float rowWidth = row == 0 ? PRIMARY_ROW_WIDTH : GAME_ROW_WIDTH;
        float rowLeft = PLAY_CARD_STAGE.left() + (PLAY_CARD_STAGE.width() - rowWidth) / 2.0f;
        float left = rowLeft + column * (PLAY_CARD_WIDTH + PLAY_CARD_GAP_X);
        float top = PLAY_CARD_MATRIX_TOP + row * (PLAY_CARD_HEIGHT + PLAY_CARD_GAP_Y);
        return tile(key, left, top, left + PLAY_CARD_WIDTH, top + PLAY_CARD_HEIGHT);
    }

    private static Tile tile(String key, Box destination, Box hit) {
        return new Tile(key, destination, hit);
    }

    private static Box box(
            float left,
            float top,
            float right,
            float bottom) {
        return new Box(left, top, right, bottom);
    }
}
