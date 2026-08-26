package com.nanbeiyule.game;

import android.content.Context;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/** Full-screen native host for the authenticated Zhejiang score assistant. */
final class ScoreAssistantDialog extends TaizhouFullscreenDialog {
    interface Actions extends ScoreAssistantView.Actions {}

    private final ScoreAssistantView content;

    ScoreAssistantDialog(Context context, Actions actions) {
        this(context, actions, new DismissHolder());
    }

    private ScoreAssistantDialog(Context context, Actions actions, DismissHolder holder) {
        this(context, new ScoreAssistantView(context, forwarding(actions, holder)), holder);
    }

    private ScoreAssistantDialog(
            Context context, ScoreAssistantView content, DismissHolder holder) {
        super(context, content, false);
        this.content = content;
        holder.dismiss = this::dismiss;
    }

    void setButtonClickSound(Runnable sound) { content.setButtonClickSound(sound); }
    ScoreAssistantState state() { return content.state(); }
    void beginLoad() { content.beginLoad(); }
    void showError(String message) { content.showError(message); }
    void showActive(List<ScoreAssistantApiProtocol.LedgerSummary> ledgers) { content.showActive(ledgers); }
    void showHistory(ScoreAssistantApiProtocol.HistoryPage page) { content.showHistory(page); }
    void showMonthly(ScoreAssistantApiProtocol.MonthlyStatistics monthly) { content.showMonthly(monthly); }
    void showDetail(ScoreAssistantApiProtocol.LedgerDetail detail) { content.showDetail(detail); }
    void applyRound(ScoreAssistantApiProtocol.RoundResult round) { content.applyRound(round); }
    void applyLedgerState(ScoreAssistantApiProtocol.LedgerState update) { content.applyLedgerState(update); }
    void removeLedger(UUID ledgerId) { content.removeLedger(ledgerId); }

    private static ScoreAssistantView.Actions forwarding(Actions actions, DismissHolder holder) {
        if (actions == null) throw new IllegalArgumentException("actions must not be null");
        return new ScoreAssistantView.Actions() {
            @Override public void onDismissRequested() { holder.dismiss.run(); }
            @Override public void onRetryRequested() { actions.onRetryRequested(); }
            @Override public void onTabRequested(ScoreAssistantState.Tab tab) { actions.onTabRequested(tab); }
            @Override public void onCreateRequested() { actions.onCreateRequested(); }
            @Override public void onLedgerRequested(UUID id) { actions.onLedgerRequested(id); }
            @Override public void onRoundRequested(ScoreAssistantApiProtocol.LedgerDetail detail) {
                actions.onRoundRequested(detail);
            }
            @Override public void onEndRequested(UUID id) { actions.onEndRequested(id); }
            @Override public void onFavoriteRequested(UUID id, boolean favorite) {
                actions.onFavoriteRequested(id, favorite);
            }
            @Override public void onDeleteRequested(UUID id) { actions.onDeleteRequested(id); }
            @Override public void onHistoryPageRequested(int page) { actions.onHistoryPageRequested(page); }
            @Override public void onMonthRequested(YearMonth month) { actions.onMonthRequested(month); }
        };
    }

    private static final class DismissHolder {
        private Runnable dismiss = () -> {};
    }
}
