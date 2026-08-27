package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayGesture;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayInteraction;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayPermission;
import com.nanbeiyule.game.mahjong.TaizhouMahjongPlayProjection;
import com.nanbeiyule.game.mahjong.TaizhouMahjongVisibleRound;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/**
 * 原版 {@code UIMahTouchHandArea:_createMah} 对每张立牌无条件 {@code setTouchEnabled(true)}：
 * 没轮到自己也能选中、抬牌、拖动，只是 {@code UIMahLayer:_onPlayMah} 的 {@code getPlayPower()}
 * 为假时不发出牌。触摸挂在手牌上，不挂在出牌权上。
 */
public final class TaizhouHandTouchWithoutPermissionTest {
    private static TaizhouMahjongVisibleRound round() {
        return new TaizhouMahjongVisibleRound(
                2,
                1,
                List.of(
                        new TaizhouMahjongVisibleRound.SeatHand(1, List.of(0x11, 0x12), 0x19, 0),
                        TaizhouMahjongVisibleRound.SeatHand.opponent(2, 2, false, 0)),
                List.of(),
                List.of());
    }

    private static TaizhouMahjongPlayPermission permission() {
        return new TaizhouMahjongPlayPermission(
                "play-1",
                TaizhouMahjongPlayGesture.Mode.DOUBLE_CLICK,
                Set.of(0, 1, 2),
                Set.of(),
                Set.of(),
                Set.of());
    }

    @Test
    public void handTilesStayTouchableWithoutAPlayPermission() {
        List<TaizhouMahjongPlayGesture.Tile> tiles =
                TaizhouMahjongPlayProjection.localHand(round(), null, 0);

        assertEquals(3, tiles.size());
        for (TaizhouMahjongPlayGesture.Tile tile : tiles) {
            assertTrue("每张立牌都应可触摸", tile.touchEnabled());
            assertFalse(tile.ting());
            assertFalse(tile.actionMask());
            assertFalse(tile.preBao());
        }
    }

    @Test
    public void tappingWithoutPermissionSelectsButNeverDiscards() {
        TaizhouMahjongPlayInteraction interaction = new TaizhouMahjongPlayInteraction();
        interaction.replace(round(), null, 0, TaizhouMahjongPlayGesture.Mode.DOUBLE_CLICK);

        assertFalse(interaction.hasPlayPermission());
        TaizhouMahjongPlayGesture.Tile tile = interaction.tiles().get(0);
        float x = (tile.left() + tile.right()) / 2.0f;
        float y = (tile.bottom() + tile.top()) / 2.0f;

        TaizhouMahjongPlayGesture.Result down = interaction.onDown(x, y, false);
        assertNotNull(down);
        assertTrue("没有出牌权也要吃下这次触摸", down.handled);
        assertEquals(Integer.valueOf(tile.index()), interaction.visualState().selectedIndex());
        assertNull(down.playIntent);

        // 双击模式下的第二次点击：原版会走 _dispatchPlayMahEvent，但 getPlayPower() 为假不发牌。
        assertNotNull(interaction.onEnd(x, y));
        TaizhouMahjongPlayGesture.Result second =
                interaction.onDown(x, y + com.nanbeiyule.game.mahjong.TaizhouMahjongHandLayout.SELECTED_RAISE, false);
        assertNotNull(second);
        assertTrue(second.handled);
        assertNull("没有出牌权不能产生出牌意图", second.playIntent);
    }

    @Test
    public void permissionArrivingLaterEnablesTheDiscardWithoutRebuildingTouch() {
        TaizhouMahjongPlayInteraction interaction = new TaizhouMahjongPlayInteraction();
        interaction.replace(round(), null, 0, TaizhouMahjongPlayGesture.Mode.DOUBLE_CLICK);
        assertFalse(interaction.tiles().isEmpty());

        interaction.replace(round(), permission(), 0, TaizhouMahjongPlayGesture.Mode.DOUBLE_CLICK);
        assertTrue(interaction.hasPlayPermission());

        TaizhouMahjongPlayGesture.Tile tile = interaction.tiles().get(0);
        float x = (tile.left() + tile.right()) / 2.0f;
        float y = (tile.bottom() + tile.top()) / 2.0f;
        assertNull(interaction.onDown(x, y, false).playIntent);
        interaction.onEnd(x, y);
        TaizhouMahjongPlayGesture.Result second =
                interaction.onDown(x, y + com.nanbeiyule.game.mahjong.TaizhouMahjongHandLayout.SELECTED_RAISE, false);
        assertNotNull(second.playIntent);
        assertEquals("play-1", second.playIntent.actionToken);
    }

    @Test
    public void singleClickDiscardKeepsThePressedTileHitAreaUntilTouchEnds() {
        TaizhouMahjongPlayInteraction interaction = new TaizhouMahjongPlayInteraction();
        interaction.replace(
                round(),
                new TaizhouMahjongPlayPermission(
                        "play-single",
                        TaizhouMahjongPlayGesture.Mode.SINGLE_CLICK,
                        Set.of(0, 1, 2),
                        Set.of(),
                        Set.of(),
                        Set.of()),
                0,
                TaizhouMahjongPlayGesture.Mode.SINGLE_CLICK);
        TaizhouMahjongPlayGesture.Tile tile = interaction.tiles().get(0);
        float x = (tile.left() + tile.right()) / 2.0f;
        float y = tile.bottom() + 10.0f;

        TaizhouMahjongPlayGesture.Result down = interaction.onDown(x, y, false);
        assertNotNull(down);
        assertNull(down.playIntent);

        TaizhouMahjongPlayGesture.Result up = interaction.onEnd(x, y);

        assertNotNull(up.playIntent);
        assertEquals(tile.index(), up.playIntent.tileIndex);
        assertEquals(tile.value(), up.playIntent.tileValue);
        assertEquals("play-single", up.playIntent.actionToken);
    }

    @Test
    public void doubleClickKeepsThePressedTileHitAreaUntilTheConfirmingTap() {
        TaizhouMahjongPlayInteraction interaction = new TaizhouMahjongPlayInteraction();
        interaction.replace(round(), permission(), 0, TaizhouMahjongPlayGesture.Mode.DOUBLE_CLICK);
        TaizhouMahjongPlayGesture.Tile tile = interaction.tiles().get(0);
        float x = (tile.left() + tile.right()) / 2.0f;
        float y = tile.bottom() + 10.0f;

        assertNull(interaction.onDown(x, y, false).playIntent);
        assertNull(interaction.onEnd(x, y).playIntent);
        assertEquals(Integer.valueOf(tile.index()), interaction.visualState().selectedIndex());

        TaizhouMahjongPlayGesture.Result confirm = interaction.onDown(x, y, false);

        assertNotNull(confirm.playIntent);
        assertEquals(tile.index(), confirm.playIntent.tileIndex);
        assertEquals("play-1", confirm.playIntent.actionToken);
    }

    /** 出牌权用掉之后（原版 setPlayPower(false)）手牌仍可触摸，只是不再发牌。 */
    @Test
    public void handStaysTouchableAfterTheDiscardConsumesThePermission() {
        TaizhouMahjongPlayInteraction interaction = new TaizhouMahjongPlayInteraction();
        interaction.replace(round(), permission(), 0, TaizhouMahjongPlayGesture.Mode.DOUBLE_CLICK);
        TaizhouMahjongPlayGesture.Tile tile = interaction.tiles().get(0);
        float x = (tile.left() + tile.right()) / 2.0f;
        float y = (tile.bottom() + tile.top()) / 2.0f;
        interaction.onDown(x, y, false);
        interaction.onEnd(x, y);
        assertNotNull(
                interaction.onDown(
                                x,
                                y + com.nanbeiyule.game.mahjong.TaizhouMahjongHandLayout.SELECTED_RAISE,
                                false)
                        .playIntent);
        interaction.onEnd(x, y);

        // 服务端下一帧收回出牌权。
        interaction.replace(round(), null, 0, TaizhouMahjongPlayGesture.Mode.DOUBLE_CLICK);
        assertFalse(interaction.tiles().isEmpty());
        TaizhouMahjongPlayGesture.Tile after = interaction.tiles().get(0);
        TaizhouMahjongPlayGesture.Result down =
                interaction.onDown(
                        (after.left() + after.right()) / 2.0f,
                        (after.bottom() + after.top()) / 2.0f,
                        false);
        assertTrue("收回出牌权后手牌仍要响应触摸", down.handled);
        assertNull(down.playIntent);
    }
}
