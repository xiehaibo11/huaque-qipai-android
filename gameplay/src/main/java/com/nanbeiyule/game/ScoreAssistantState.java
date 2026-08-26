package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** UI state that only accepts totals and lifecycle changes returned by the server. */
final class ScoreAssistantState {
    enum Tab {
        ACTIVE,
        HISTORY,
        MONTHLY
    }

    enum LoadState {
        IDLE,
        LOADING,
        CONTENT,
        ERROR
    }

    private Tab tab = Tab.ACTIVE;
    private LoadState loadState = LoadState.IDLE;
    private String error = "";
    private List<ScoreAssistantApiProtocol.LedgerSummary> active = List.of();
    private ScoreAssistantApiProtocol.HistoryPage history;
    private ScoreAssistantApiProtocol.MonthlyStatistics monthly;
    private ScoreAssistantApiProtocol.LedgerDetail detail;

    Tab tab() {
        return tab;
    }

    void selectTab(Tab value) {
        tab = value;
        detail = null;
        error = "";
    }

    LoadState loadState() {
        return loadState;
    }

    String error() {
        return error;
    }

    void beginLoad() {
        loadState = LoadState.LOADING;
        error = "";
    }

    void showError(String message) {
        loadState = LoadState.ERROR;
        error = message == null ? "请求失败" : message;
    }

    List<ScoreAssistantApiProtocol.LedgerSummary> active() {
        return active;
    }

    void showActive(List<ScoreAssistantApiProtocol.LedgerSummary> value) {
        active = List.copyOf(value);
        detail = null;
        loadState = LoadState.CONTENT;
        error = "";
    }

    ScoreAssistantApiProtocol.HistoryPage history() {
        return history;
    }

    void showHistory(ScoreAssistantApiProtocol.HistoryPage value) {
        history = value;
        detail = null;
        loadState = LoadState.CONTENT;
        error = "";
    }

    boolean hasNextHistoryPage() {
        return history != null && history.page() < history.totalPages();
    }

    ScoreAssistantApiProtocol.MonthlyStatistics monthly() {
        return monthly;
    }

    void showMonthly(ScoreAssistantApiProtocol.MonthlyStatistics value) {
        monthly = value;
        detail = null;
        loadState = LoadState.CONTENT;
        error = "";
    }

    ScoreAssistantApiProtocol.LedgerDetail detail() {
        return detail;
    }

    void showDetail(ScoreAssistantApiProtocol.LedgerDetail value) {
        detail = value;
        loadState = LoadState.CONTENT;
        error = "";
    }

    void clearDetail() {
        detail = null;
        loadState = LoadState.CONTENT;
        error = "";
    }

    void applyRound(ScoreAssistantApiProtocol.RoundResult round) {
        if (detail == null || round == null) {
            return;
        }
        List<ScoreAssistantApiProtocol.Player> players = new ArrayList<>(detail.players().size());
        for (ScoreAssistantApiProtocol.Player player : detail.players()) {
            long authoritative = player.totalScore();
            for (ScoreAssistantApiProtocol.RoundScore score : round.scores()) {
                if (score.playerId().equals(player.playerId())) {
                    authoritative = score.totalAfter();
                    break;
                }
            }
            players.add(
                    new ScoreAssistantApiProtocol.Player(
                            player.playerId(),
                            player.position(),
                            player.name(),
                            player.ownerPlayer(),
                            authoritative));
        }
        List<ScoreAssistantApiProtocol.RoundResult> rounds = new ArrayList<>(detail.rounds());
        rounds.add(round);
        detail = new ScoreAssistantApiProtocol.LedgerDetail(
                detail.ledgerId(), detail.status(), detail.favorite(), round.roundNumber(),
                detail.startedAt(), detail.endedAt(), players, rounds);
        loadState = LoadState.CONTENT;
        error = "";
    }

    void applyLedgerState(ScoreAssistantApiProtocol.LedgerState update) {
        if (detail == null || update == null || !detail.ledgerId().equals(update.ledgerId())) {
            return;
        }
        detail = new ScoreAssistantApiProtocol.LedgerDetail(
                detail.ledgerId(), update.status(), update.favorite(), update.roundCount(),
                detail.startedAt(), update.endedAt(), detail.players(), detail.rounds());
        loadState = LoadState.CONTENT;
        error = "";
    }

    void removeLedger(UUID ledgerId) {
        if (detail != null && detail.ledgerId().equals(ledgerId)) {
            detail = null;
        }
        active = active.stream().filter(item -> !item.ledgerId().equals(ledgerId)).toList();
        if (history != null) {
            List<ScoreAssistantApiProtocol.LedgerSummary> remaining = history.ledgers().stream()
                    .filter(item -> !item.ledgerId().equals(ledgerId)).toList();
            history = new ScoreAssistantApiProtocol.HistoryPage(
                    history.page(), history.pageSize(), Math.max(0, history.totalCount() - 1),
                    history.totalPages(), remaining);
        }
    }
}
