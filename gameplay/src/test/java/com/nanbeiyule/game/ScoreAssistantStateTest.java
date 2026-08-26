package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class ScoreAssistantStateTest {
    private static final UUID LEDGER = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID PLAYER = UUID.fromString("40000000-0000-0000-0000-000000000004");

    @Test
    public void roundResultReplacesTotalsWithServerReturnedTotalAfter() {
        ScoreAssistantState state = new ScoreAssistantState();
        state.showDetail(detail(ScoreAssistantApiProtocol.Status.IN_PROGRESS, false, 0, 0L));

        state.applyRound(
                new ScoreAssistantApiProtocol.RoundResult(
                        UUID.fromString("60000000-0000-0000-0000-000000000006"),
                        1,
                        Instant.parse("2026-08-24T12:05:00Z"),
                        List.of(new ScoreAssistantApiProtocol.RoundScore(PLAYER, "", 18L, 18L))));

        assertEquals(1, state.detail().roundCount());
        assertEquals(18L, state.detail().players().get(0).totalScore());
        assertEquals(1, state.detail().rounds().size());
    }

    @Test
    public void stateFavoriteEndAndDeleteOnlyApplyToMatchingLedger() {
        ScoreAssistantState state = new ScoreAssistantState();
        state.showDetail(detail(ScoreAssistantApiProtocol.Status.IN_PROGRESS, false, 2, 18L));
        state.applyLedgerState(
                new ScoreAssistantApiProtocol.LedgerState(
                        LEDGER,
                        ScoreAssistantApiProtocol.Status.ENDED,
                        true,
                        2,
                        Instant.parse("2026-08-24T12:10:00Z")));

        assertEquals(ScoreAssistantApiProtocol.Status.ENDED, state.detail().status());
        assertTrue(state.detail().favorite());

        state.removeLedger(LEDGER);
        assertNull(state.detail());
    }

    @Test
    public void historyPreservesOneBasedPaginationAndMonthlySelection() {
        ScoreAssistantState state = new ScoreAssistantState();
        state.showHistory(new ScoreAssistantApiProtocol.HistoryPage(2, 10, 13, 2, List.of()));
        state.showMonthly(
                new ScoreAssistantApiProtocol.MonthlyStatistics(
                        YearMonth.of(2026, 8), 2, 1, 1, 20, 30, -10, "牌友乙", "牌友甲"));

        assertEquals(2, state.history().page());
        assertFalse(state.hasNextHistoryPage());
        assertEquals(YearMonth.of(2026, 8), state.monthly().month());
    }

    private static ScoreAssistantApiProtocol.LedgerDetail detail(
            ScoreAssistantApiProtocol.Status status,
            boolean favorite,
            int roundCount,
            long total) {
        return new ScoreAssistantApiProtocol.LedgerDetail(
                LEDGER,
                status,
                favorite,
                roundCount,
                Instant.parse("2026-08-24T12:00:00Z"),
                null,
                List.of(new ScoreAssistantApiProtocol.Player(PLAYER, 1, "本人", true, total)),
                List.of());
    }
}
