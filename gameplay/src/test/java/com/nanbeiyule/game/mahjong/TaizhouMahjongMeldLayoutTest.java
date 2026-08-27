package com.nanbeiyule.game.mahjong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.gameplay.GameplayMeld;
import com.nanbeiyule.game.mahjong.round.MahjongCombType;
import java.util.List;
import org.junit.After;
import org.junit.Test;

/**
 * 副露排布与手牌起点，对照原版 {@code UIMahComb:_updateHorizontal/VerticalLayoutAndSize}、
 * {@code UIMahHandArea:_updateCombsPosition / _getHandMahsStartPos} 与
 * {@code UIMahConfig2D.HandAreaLayout}。
 *
 * <p>四人桌 mySeat=1 时：座位 1=BOTTOM、2=RIGHT、3=TOP、4=LEFT。
 */
public final class TaizhouMahjongMeldLayoutTest {
    private static final int CHAIRS = 4;

    private static final int MY_SEAT = 1;

    @After
    public void restoreAppearance() {
        MahjongSettingData.setAppearance(MahjongTileAppearance.area7109Defaults());
    }

    /**
     * 组内步进按 topEdgeWidth，且绘制次序是
     * {@code SingleLayerMahCount - index}：组里的第一张（屏幕最下方那张）最后画、压住
     * 上一张的下缘，画反了整组就会露出一圈厚边，看着像叠在一起。
     */
    @Test
    public void sideSeatCombStepsByTopEdgeWidthAndDrawsNearestLast() {
        List<TaizhouMahjongMeldLayout.TilePlacement> tiles =
                TaizhouMahjongMeldLayout.seatMelds(
                        TaizhouMahjongTableLayout.SEAT_LEFT,
                        List.of(pung(4, 1)),
                        MY_SEAT,
                        CHAIRS);
        assertEquals(3, tiles.size());
        float rootScale = TaizhouMahjongTableLayout.HAND_LEFT.scale;
        float combScale = 0.9f;
        float step = MahjongTileSprite.topEdgeWidth(MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT);
        // 最后画的是喂牌方向的竖躺牌（组里的第一张），前两张是本座位常态的横躺牌。
        assertEquals(MahjongTileSprite.LIE_UP_VERTICAL_DOWN, tiles.get(2).pose);
        assertEquals(MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT, tiles.get(0).pose);
        assertTrue(tiles.get(0).cocosY > tiles.get(1).cocosY);
        assertTrue(tiles.get(1).cocosY > tiles.get(2).cocosY);
        assertEquals(
                step * combScale * rootScale,
                tiles.get(0).cocosY - tiles.get(1).cocosY,
                0.001f);
        assertEquals(tiles.get(0).designX, tiles.get(1).designX, 0.001f);
    }

    /** BOTTOM/TOP 是竖向布局：{@code mah:setLocalZOrder(index)}，后一张压前一张。 */
    @Test
    public void frontSeatCombDrawsInAddOrder() {
        List<TaizhouMahjongMeldLayout.TilePlacement> tiles =
                TaizhouMahjongMeldLayout.seatMelds(
                        TaizhouMahjongTableLayout.SEAT_TOP,
                        List.of(chow(3, 2)),
                        MY_SEAT,
                        CHAIRS);
        assertEquals(3, tiles.size());
        // 喂牌方向的横躺牌是第三张，仍然最后画。
        assertEquals(MahjongTileSprite.LIE_UP_HORIZONTAL_RIGHT, tiles.get(2).pose);
        assertTrue(tiles.get(0).designX < tiles.get(1).designX);
        assertTrue(tiles.get(1).designX < tiles.get(2).designX);
    }

    /** 组间距：LEFT 是 {@code CombDistance = 2}、{@code AddDirection = -1}。 */
    @Test
    public void leftSeatHandStartsAfterTheLastComb() {
        float offset =
                TaizhouMahjongMeldLayout.handStartOffset(
                        TaizhouMahjongTableLayout.SEAT_LEFT,
                        List.of(pung(4, 1)),
                        MY_SEAT,
                        CHAIRS);
        assertEquals(-372.8f, offset, 0.01f);
    }

    /** RIGHT 与 LEFT 同尺寸但 {@code AddDirection = 1}。 */
    @Test
    public void rightSeatHandStartsOnTheOppositeDirection() {
        float offset =
                TaizhouMahjongMeldLayout.handStartOffset(
                        TaizhouMahjongTableLayout.SEAT_RIGHT,
                        List.of(pung(2, 1)),
                        MY_SEAT,
                        CHAIRS);
        assertEquals(372.8f, offset, 0.01f);
    }

    /** TOP 是水平增长：{@code CombDistance = 20}、{@code AddDirection = -1}。 */
    @Test
    public void topSeatHandStartsAfterTheCombWidth() {
        float offset =
                TaizhouMahjongMeldLayout.handStartOffset(
                        TaizhouMahjongTableLayout.SEAT_TOP,
                        List.of(chow(3, 2)),
                        MY_SEAT,
                        CHAIRS);
        assertEquals(-420.5f, offset, 0.01f);
    }

    /** 两组之间必须真的分开：第二组的起点越过第一组的包围盒。 */
    @Test
    public void twoCombsDoNotOverlap() {
        float one =
                TaizhouMahjongMeldLayout.handStartOffset(
                        TaizhouMahjongTableLayout.SEAT_LEFT,
                        List.of(pung(4, 1)),
                        MY_SEAT,
                        CHAIRS);
        float two =
                TaizhouMahjongMeldLayout.handStartOffset(
                        TaizhouMahjongTableLayout.SEAT_LEFT,
                        List.of(pung(4, 1), pung(4, 1)),
                        MY_SEAT,
                        CHAIRS);
        assertEquals(2.0f * one, two, 0.01f);
        assertTrue(two < one);
    }

    /** 无副露时手牌起点为 0（原版 {@code HandMahsStartPos[FOURTEEN] = (0,0)}）。 */
    @Test
    public void noMeldsKeepsTheHandAtTheAreaOrigin() {
        assertEquals(
                0.0f,
                TaizhouMahjongMeldLayout.handStartOffset(
                        TaizhouMahjongTableLayout.SEAT_TOP, List.of(), MY_SEAT, CHAIRS),
                0.0f);
    }

    /** 杠的第四张按 {@code UIMah:getThick} 叠高，厚度随外观设置变化。 */
    @Test
    public void kongFourthTileStacksByTheConfiguredThickness() {
        MahjongSettingData.setAppearance(
                new MahjongTileAppearance(1, 1, 3, 2, 0.0f, 1.0f, 1.0f));
        List<TaizhouMahjongMeldLayout.TilePlacement> tiles =
                TaizhouMahjongMeldLayout.seatMelds(
                        TaizhouMahjongTableLayout.SEAT_TOP,
                        List.of(
                                new GameplayMeld(
                                        3,
                                        MahjongCombType.EXPOSED_KONG,
                                        List.of(0x11, 0x11, 0x11, 0x11),
                                        2)),
                        MY_SEAT,
                        CHAIRS);
        assertEquals(4, tiles.size());
        float rootScale = TaizhouMahjongTableLayout.HAND_TOP.scale;
        float expected =
                OriginalMahjongTileGeometry.thickness(tiles.get(3).pose) * 0.9f * rootScale;
        assertEquals(
                MahjongTileSprite.defaultThickness(tiles.get(3).pose) + 15.0f,
                OriginalMahjongTileGeometry.thickness(tiles.get(3).pose),
                0.001f);
        int alignIndex = 1;
        assertEquals(tiles.get(alignIndex).designX, tiles.get(3).designX, 0.001f);
        assertEquals(expected, tiles.get(3).cocosY - tiles.get(alignIndex).cocosY, 0.001f);
    }

    private static GameplayMeld pung(int seat, int fromSeat) {
        return new GameplayMeld(seat, MahjongCombType.PONG, List.of(0x11, 0x11, 0x11), fromSeat);
    }

    private static GameplayMeld chow(int seat, int fromSeat) {
        return new GameplayMeld(seat, MahjongCombType.CHOW, List.of(0x11, 0x12, 0x13), fromSeat);
    }
}
