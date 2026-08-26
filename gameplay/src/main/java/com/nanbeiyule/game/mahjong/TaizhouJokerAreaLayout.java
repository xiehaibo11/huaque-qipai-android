package com.nanbeiyule.game.mahjong;

import java.util.ArrayList;
import java.util.List;

/**
 * 台州牌桌中心的财神指示牌区 {@code KW_JOKER_MAH}。
 *
 * <p>原版把该节点挂在 {@code _KW_ADAPT_MAH_5} 之下
 * （{@code MahLayer/View2D/UIMahLayer.lua:15,114-128}），由
 * {@code UIMahJokerArea:setJokerMahs} 为每张翻出的财神创建一个
 * {@code MAH_TYPE.LIE_FACE_UP_VERTICAL_TOUP} 的 {@code UIMah} 并调用
 * {@code showJokerIcon(true)}（{@code MAH_VALUE.BACK} 除外）。本工程的牌面图集只有
 * 台州牌花，因此没有台州牌面的值（背面值、空值、越界值）直接不进入布局，与
 * {@code TaizhouMeldRenderer}、{@code TaizhouFlowerAreaRenderer} 的坏数据保护一致。位置来自
 * {@code UIMahJokerArea:computeOutMahs} 与 {@code arrageOutMahs}：先按牌数选一套
 * 槽位左下角坐标，再 {@code setPosition(info.x + cardSize.width/2, info.y +
 * cardSize.height/2)}，即牌的中心。牌身尺寸取自
 * {@link OriginalMahjongTileGeometry#defaultTile(int, int)}，不另存第二套牌形。
 *
 * <p>本类只做几何，不碰位图；角标层仍由
 * {@link OriginalMahjongTileDrawPlan} 的 ICON 命令生成，与手牌、牌河、副露和结算
 * 共用同一套姿态公式。
 */
public final class TaizhouJokerAreaLayout {
    /** {@code UIMahJokerArea:computeOutMahs} 的 {@code config.xImgWidth}。 */
    public static final float SLOT_WIDTH = 138.0f;

    /** {@code config.yImgHeight}。 */
    public static final float SLOT_HEIGHT = 192.0f;

    /** {@code config.xDistance}。 */
    public static final float SLOT_GAP_X = 10.0f;

    /** {@code config.yDistance}。 */
    public static final float SLOT_GAP_Y = 6.0f;

    /** 原版只为前四张财神给出位置；第五张起 {@code mahStyleInfo[index]} 为空。 */
    public static final int MAX_TILES = 4;

    /** One indicator tile, already projected into the 1920x1080 design space. */
    public record TilePosition(
            int tileValue,
            int pose,
            float designX,
            float cocosY,
            float scale,
            int zOrder) {}

    private TaizhouJokerAreaLayout() {}

    /**
     * @param jokerTiles 本局翻出的财神物理牌值；未翻得时为空，什么都不画。
     */
    public static List<TilePosition> tiles(List<Integer> jokerTiles) {
        if (jokerTiles == null || jokerTiles.isEmpty()) {
            return List.of();
        }
        List<Integer> drawable = new ArrayList<>(jokerTiles.size());
        for (int tileValue : jokerTiles) {
            if (MahjongTile.hasTaizhouFace(tileValue)) {
                drawable.add(tileValue);
            }
        }
        if (drawable.isEmpty()) {
            return List.of();
        }
        TaizhouMahjongTableLayout.Slot slot = TaizhouMahjongTableLayout.CENTER_JOKER;
        int count = Math.min(drawable.size(), MAX_TILES);
        List<TilePosition> positions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            int tileValue = drawable.get(index);
            OriginalMahjongTileGeometry.Composition composition =
                    OriginalMahjongTileGeometry.defaultTile(
                            MahjongTileSprite.LIE_UP_VERTICAL_UP, tileValue);
            float localX = slotLeft(drawable.size(), index) + composition.width / 2.0f;
            float localY = slotBottom(drawable.size(), index) + composition.height / 2.0f;
            positions.add(
                    new TilePosition(
                            tileValue,
                            MahjongTileSprite.LIE_UP_VERTICAL_UP,
                            slot.designX() + slot.scale * localX,
                            slot.cocosY() + slot.scale * localY,
                            slot.scale,
                            index + 1));
        }
        return List.copyOf(positions);
    }

    /** {@code computeOutMahs} 的 {@code mahInfo[index].x}。 */
    private static float slotLeft(int total, int zeroBasedIndex) {
        if (total == 1) {
            return -SLOT_WIDTH / 2.0f;
        }
        return zeroBasedIndex % 2 == 0 ? -SLOT_WIDTH - SLOT_GAP_X : SLOT_GAP_X;
    }

    /** {@code computeOutMahs} 的 {@code mahInfo[index].y}。 */
    private static float slotBottom(int total, int zeroBasedIndex) {
        if (total == 1) {
            return -SLOT_HEIGHT / 2.0f;
        }
        if (total == 2) {
            return -SLOT_HEIGHT / 2.0f;
        }
        return zeroBasedIndex < 2 ? SLOT_GAP_Y : -SLOT_HEIGHT - SLOT_GAP_Y;
    }
}
