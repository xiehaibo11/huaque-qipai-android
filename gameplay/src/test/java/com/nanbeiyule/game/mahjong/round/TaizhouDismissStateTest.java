package com.nanbeiyule.game.mahjong.round;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.mahjong.TaizhouDismissStatus;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** {@code Dismiss/Module.luac} 的投票状态机。 */
public final class TaizhouDismissStateTest {
    @Test
    public void countdownRefreshesEverySeatFromAgreements() {
        TaizhouDismissState state = new TaizhouDismissState();

        // iAgrees: 0=拒绝, 1=同意, 其余=未选择；发起座位恒为同意。
        state.applyCountdown(1, 30, Arrays.asList(2, 1, 0, null));

        assertEquals(TaizhouDismissStatus.DEFAULT, state.statusOf(0));
        assertEquals(TaizhouDismissStatus.AGREE, state.statusOf(1));
        assertEquals(TaizhouDismissStatus.REFUSE, state.statusOf(2));
        assertEquals(TaizhouDismissStatus.DEFAULT, state.statusOf(3));
        assertEquals(30, state.remainingSeconds());
    }

    @Test
    public void requestingSeatCountsAsAgreeEvenWithoutItsOwnAgreement() {
        TaizhouDismissState state = new TaizhouDismissState();

        state.applyCountdown(2, 15, Arrays.asList(null, null, null, null));

        assertEquals(TaizhouDismissStatus.AGREE, state.statusOf(2));
    }

    @Test
    public void hidesOnAnyRefusal() {
        TaizhouDismissState state = new TaizhouDismissState();

        state.applyCountdown(0, 20, Arrays.asList(1, 0, null, null));

        assertTrue(state.shouldHide(4));
    }

    @Test
    public void hidesWhenEveryoneAgrees() {
        TaizhouDismissState state = new TaizhouDismissState();

        state.applyCountdown(0, 20, Arrays.asList(1, 1, 1, 1));

        assertTrue(state.shouldHide(4));
    }

    @Test
    public void keepsShowingWhileSomeoneIsStillChoosing() {
        TaizhouDismissState state = new TaizhouDismissState();

        state.applyCountdown(0, 20, Arrays.asList(1, 1, null, null));

        assertFalse(state.shouldHide(4));
    }

    @Test
    public void twoSeatRoomsNeedBothAgreements() {
        TaizhouDismissState state = new TaizhouDismissState();

        state.applyCountdown(0, 20, List.of(1, 2));
        assertFalse(state.shouldHide(2));

        state.applyCountdown(0, 20, List.of(1, 1));
        assertTrue(state.shouldHide(2));
    }

    @Test
    public void singleResponseOverwritesOneSeatOnly() {
        TaizhouDismissState state = new TaizhouDismissState();
        state.applyCountdown(0, 20, Arrays.asList(1, null, null, null));

        state.applyResponse(2, false);

        assertEquals(TaizhouDismissStatus.AGREE, state.statusOf(0));
        assertEquals(TaizhouDismissStatus.REFUSE, state.statusOf(2));
    }

    @Test
    public void clockStopsAtZero() {
        TaizhouDismissState state = new TaizhouDismissState();
        state.applyRequest(1, "阿三", 2);

        state.tick();
        state.tick();
        state.tick();

        assertEquals(0, state.remainingSeconds());
        assertEquals("阿三", state.requestNickname());
        assertEquals(1, state.requestSeat());
    }
}
