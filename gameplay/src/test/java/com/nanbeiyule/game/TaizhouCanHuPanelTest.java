package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.mahjong.TaizhouMahjongExtraAtlas;
import java.util.List;
import org.junit.Test;

/**
 * 听牌可胡面板的原版几何：{@code CanHuMahs.csb} 的 {@code _bg} / {@code _line} 九宫
 * 边距，以及 {@code CanHuMahsUI:initUI} 的网格与 {@code setHuInfo} 的文字排布。
 */
public final class TaizhouCanHuPanelTest {
    /** 九宫边距必须落在图内：{@code 左 + 中 + 右 == 源宽}，上下同理。 */
    @Test
    public void backgroundCapInsetsFitTheSourceFrame() {
        int[] frame = frame(TaizhouCanHuLayout.BG_FRAME);
        assertEquals(100, frame[2]);
        assertEquals(100, frame[3]);
        assertEquals(33.0f, TaizhouCanHuLayout.BG_CAP_X, 0.0f);
        assertEquals(33.0f, TaizhouCanHuLayout.BG_CAP_Y, 0.0f);
        assertEquals(
                frame[2],
                TaizhouCanHuLayout.BG_CAP_X
                        + TaizhouCanHuLayout.BG_CAP_WIDTH
                        + (frame[2] - TaizhouCanHuLayout.BG_CAP_X - TaizhouCanHuLayout.BG_CAP_WIDTH),
                0.001f);
        assertTrue(
                TaizhouCanHuLayout.BG_CAP_X + TaizhouCanHuLayout.BG_CAP_WIDTH < frame[2]);
        assertTrue(
                TaizhouCanHuLayout.BG_CAP_Y + TaizhouCanHuLayout.BG_CAP_HEIGHT < frame[3]);
    }

    @Test
    public void divisionLineCapInsetsFitTheSourceFrame() {
        int[] frame = frame(TaizhouCanHuLayout.LINE_FRAME);
        assertEquals(2, frame[2]);
        assertEquals(192, frame[3]);
        // 左右不留边（宽度就是 2），上下各留 63。
        assertEquals(0.0f, TaizhouCanHuLayout.LINE_CAP_X, 0.0f);
        assertEquals(frame[2], TaizhouCanHuLayout.LINE_CAP_WIDTH, 0.001f);
        assertEquals(63.0f, TaizhouCanHuLayout.LINE_CAP_Y, 0.0f);
        assertEquals(
                63.0f,
                frame[3] - TaizhouCanHuLayout.LINE_CAP_Y - TaizhouCanHuLayout.LINE_CAP_HEIGHT,
                0.001f);
    }

    /** {@code bgWidth = (width+1)*235}、{@code bgHeight = height*200 (+25)}。 */
    @Test
    public void twoTargetsUseOneRowAndThreeColumnUnits() {
        TaizhouCanHuState state = TaizhouCanHuState.shown(List.of(0x11, 0x21), List.of(row(20, 3), row(20, 2)));
        assertEquals(2, state.columns());
        assertEquals(1, state.rows());
        assertEquals(705.0f, state.backgroundWidth(), 0.0f);
        assertEquals(225.0f, state.backgroundHeight(), 0.0f);
        assertEquals(260.0f, state.tileLocalX(0), 0.0f);
        assertEquals(490.0f, state.tileLocalX(1), 0.0f);
        assertEquals(105.0f, state.tileLocalY(0), 0.0f);
    }

    /** 分割线固定在 bg 本地 x=190，即 275 宽的「胡」格右缘之内。 */
    @Test
    public void divisionLineSitsAfterTheHuLogoColumn() {
        TaizhouCanHuState state = TaizhouCanHuState.shown(List.of(0x11), List.of(row(20, 4)));
        assertTrue(TaizhouCanHuLayout.LINE_LOCAL_X < state.tileLocalX(0));
        assertEquals(190.0f, TaizhouCanHuLayout.LINE_LOCAL_X, 0.0f);
    }

    /** 只有「N张」一段时用 {@code huInfoPositionY[1] = {-30}}，字号 46。 */
    @Test
    public void singleInfoSegmentUsesTheOriginalRowOffset() {
        assertEquals(1, TaizhouCanHuLayout.INFO_ROW_OFFSETS[0].length);
        assertEquals(-30.0f, TaizhouCanHuLayout.INFO_ROW_OFFSETS[0][0], 0.0f);
        assertEquals(3, TaizhouCanHuLayout.INFO_ROW_OFFSETS[2].length);
        assertEquals(46.0f, TaizhouCanHuLayout.INFO_FONT_SIZE, 0.0f);
        assertEquals(50.0f, TaizhouCanHuLayout.INFO_TEXT_LOCAL_X, 0.0f);
        // 字模填充色：can_hu_mah_info 红 / can_hu_mah_info_2 棕。
        assertEquals(0xffad1c13, TaizhouCanHuLayout.INFO_NUMBER_COLOR);
        assertEquals(0xff3d2916, TaizhouCanHuLayout.INFO_UNIT_COLOR);
    }

    /** 原版每格是「N胡」+「N张」两段，两段用 {@code huInfoPositionY[2] = {0,-70}}。 */
    @Test
    public void infoRowsTravelWithTheTargets() {
        TaizhouCanHuState state =
                TaizhouCanHuState.shown(List.of(0x11, 0x21), List.of(row(20, 3), row(20, 2)));
        assertEquals(2, state.infoRows().size());
        assertEquals(row(20, 3), state.infoRows().get(0));
        assertEquals(row(20, 2), state.infoRows().get(1));
        assertEquals(2, TaizhouCanHuLayout.INFO_ROW_OFFSETS[1].length);
        assertEquals(0.0f, TaizhouCanHuLayout.INFO_ROW_OFFSETS[1][0], 0.0f);
        assertEquals(-70.0f, TaizhouCanHuLayout.INFO_ROW_OFFSETS[1][1], 0.0f);
        assertTrue(TaizhouCanHuState.hidden().infoRows().isEmpty());
    }

    /** {@code bShowFanNum=false, bShowHuNum=true} 的台州两段：N胡 + N张。 */
    private static List<TaizhouCanHuState.InfoSegment> row(int huPoint, int surplus) {
        return List.of(
                new TaizhouCanHuState.InfoSegment(huPoint, TaizhouCanHuLayout.HU_UNIT),
                new TaizhouCanHuState.InfoSegment(surplus, TaizhouCanHuLayout.SURPLUS_UNIT));
    }

    private static int[] frame(String name) {
        int index =
                TaizhouMahjongExtraAtlas.indexOf(
                        TaizhouMahjongExtraAtlas.TAIZHOU_MAHJONG_CAN_HU_TIP_NAMES, name);
        assertTrue(index >= 0);
        return TaizhouMahjongExtraAtlas.TAIZHOU_MAHJONG_CAN_HU_TIP_FRAMES[index];
    }
}
