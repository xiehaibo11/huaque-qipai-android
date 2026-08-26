package com.nanbeiyule.game;

import android.app.Activity;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Public lobby boundary for the complete authenticated Mahjong scoring workflow. */
public final class ScoreAssistantController implements AutoCloseable {
    public interface TokenProvider {
        String accessToken();
    }

    public interface Listener {
        void onUnauthorized();
        void onMessage(String message);
        void onDismissed();
    }

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private final Activity activity;
    private final TokenProvider tokenProvider;
    private final Listener listener;
    private final Runnable buttonClickSound;
    private final ScoreAssistantApiClient client;
    private final ScoreAssistantRequestGate requestGate = new ScoreAssistantRequestGate();
    private ScoreAssistantDialog dialog;
    private boolean closed;

    public ScoreAssistantController(
            Activity activity,
            String apiBaseUrl,
            TokenProvider tokenProvider,
            Listener listener,
            Runnable buttonClickSound) {
        this.activity = Objects.requireNonNull(activity, "activity");
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.buttonClickSound = buttonClickSound == null ? () -> {} : buttonClickSound;
        client = new ScoreAssistantApiClient(apiBaseUrl);
    }

    public void show() {
        if (closed || activity.isFinishing() || activity.isDestroyed() || dialog != null) return;
        ScoreAssistantDialog opened = new ScoreAssistantDialog(activity, new DialogActions());
        dialog = opened;
        opened.setButtonClickSound(buttonClickSound);
        opened.setOnDismissListener(ignored -> {
            if (dialog == opened) {
                dialog = null;
                listener.onDismissed();
            }
        });
        opened.show();
        loadActive(opened);
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    @Override
    public void close() {
        closed = true;
        requestGate.invalidate();
        dismiss();
        client.shutdown();
    }

    private void loadActive(ScoreAssistantDialog expected) {
        expected.beginLoad();
        client.loadInProgress(token(), new Callback<>(expected) {
            @Override public void onSuccess(List<ScoreAssistantApiProtocol.LedgerSummary> result) {
                if (active()) expected.showActive(result);
            }
        });
    }

    private void loadHistory(ScoreAssistantDialog expected, int page) {
        expected.beginLoad();
        client.loadHistory(token(), page, new Callback<>(expected) {
            @Override public void onSuccess(ScoreAssistantApiProtocol.HistoryPage result) {
                if (active()) expected.showHistory(result);
            }
        });
    }

    private void loadMonthly(ScoreAssistantDialog expected, YearMonth month) {
        expected.beginLoad();
        client.loadMonthly(token(), month, new Callback<>(expected) {
            @Override public void onSuccess(ScoreAssistantApiProtocol.MonthlyStatistics result) {
                if (active()) expected.showMonthly(result);
            }
        });
    }

    private void loadDetail(ScoreAssistantDialog expected, UUID ledgerId) {
        expected.beginLoad();
        client.loadDetail(token(), ledgerId, new Callback<>(expected) {
            @Override public void onSuccess(ScoreAssistantApiProtocol.LedgerDetail result) {
                if (active()) expected.showDetail(result);
            }
        });
    }

    private void createLedger(List<ScoreAssistantInputValidator.PlayerDraft> players) {
        ScoreAssistantDialog expected = dialog;
        if (expected == null) return;
        expected.beginLoad();
        client.create(token(), players, new Callback<>(expected) {
            @Override public void onSuccess(ScoreAssistantApiProtocol.LedgerDetail result) {
                if (active()) expected.showDetail(result);
            }
        });
    }

    private void addRound(
            ScoreAssistantApiProtocol.LedgerDetail detail,
            List<ScoreAssistantInputValidator.ScoreDelta> scores) {
        ScoreAssistantDialog expected = dialog;
        if (expected == null) return;
        expected.beginLoad();
        client.addRound(token(), detail.ledgerId(), scores, new Callback<>(expected) {
            @Override public void onSuccess(ScoreAssistantApiProtocol.RoundResult result) {
                if (active()) expected.applyRound(result);
            }
        });
    }

    private void endLedger(UUID ledgerId) {
        ScoreAssistantDialog expected = dialog;
        if (expected == null) return;
        expected.beginLoad();
        client.end(token(), ledgerId, new Callback<>(expected) {
            @Override public void onSuccess(ScoreAssistantApiProtocol.LedgerState result) {
                if (active()) expected.applyLedgerState(result);
            }
        });
    }

    private void setFavorite(UUID ledgerId, boolean favorite) {
        ScoreAssistantDialog expected = dialog;
        if (expected == null) return;
        expected.beginLoad();
        client.setFavorite(token(), ledgerId, favorite, new Callback<>(expected) {
            @Override public void onSuccess(ScoreAssistantApiProtocol.LedgerState result) {
                if (active()) expected.applyLedgerState(result);
            }
        });
    }

    private void deleteLedger(UUID ledgerId) {
        ScoreAssistantDialog expected = dialog;
        if (expected == null) return;
        expected.beginLoad();
        client.delete(token(), ledgerId, new Callback<>(expected) {
            @Override public void onSuccess(ScoreAssistantApiProtocol.DeleteReceipt result) {
                if (!active()) return;
                expected.removeLedger(result.ledgerId());
                reloadCurrent(expected);
            }
        });
    }

    private void reloadCurrent(ScoreAssistantDialog expected) {
        if (expected.state().tab() == ScoreAssistantState.Tab.ACTIVE) {
            loadActive(expected);
        } else if (expected.state().tab() == ScoreAssistantState.Tab.HISTORY) {
            int page = expected.state().history() == null ? 1 : expected.state().history().page();
            loadHistory(expected, page);
        } else {
            YearMonth month = expected.state().monthly() == null
                    ? YearMonth.now(SHANGHAI) : expected.state().monthly().month();
            loadMonthly(expected, month);
        }
    }

    private String token() {
        String value = tokenProvider.accessToken();
        return value == null ? "" : value;
    }

    private final class DialogActions implements ScoreAssistantDialog.Actions {
        @Override public void onDismissRequested() { dismiss(); }
        @Override public void onRetryRequested() {
            if (dialog != null) reloadCurrent(dialog);
        }
        @Override public void onTabRequested(ScoreAssistantState.Tab tab) {
            if (dialog == null) return;
            if (tab == ScoreAssistantState.Tab.ACTIVE) loadActive(dialog);
            else if (tab == ScoreAssistantState.Tab.HISTORY) loadHistory(dialog, 1);
            else loadMonthly(dialog, YearMonth.now(SHANGHAI));
        }
        @Override public void onCreateRequested() {
            ScoreAssistantCreateDialog.show(activity, ScoreAssistantController.this::createLedger);
        }
        @Override public void onLedgerRequested(UUID ledgerId) {
            if (dialog != null) loadDetail(dialog, ledgerId);
        }
        @Override public void onRoundRequested(ScoreAssistantApiProtocol.LedgerDetail detail) {
            ScoreAssistantRoundDialog.show(activity, detail.players(), detail.roundCount() + 1,
                    scores -> addRound(detail, scores));
        }
        @Override public void onEndRequested(UUID ledgerId) {
            ScoreAssistantConfirmDialog.show(activity, "结束计分",
                    "结束后将不能继续录入新局，是否确认？", () -> endLedger(ledgerId));
        }
        @Override public void onFavoriteRequested(UUID ledgerId, boolean favorite) {
            setFavorite(ledgerId, favorite);
        }
        @Override public void onDeleteRequested(UUID ledgerId) {
            ScoreAssistantConfirmDialog.show(activity, "删除记录",
                    "删除后该账本及局记录将不再显示，是否确认？", () -> deleteLedger(ledgerId));
        }
        @Override public void onHistoryPageRequested(int page) {
            if (dialog != null) loadHistory(dialog, page);
        }
        @Override public void onMonthRequested(YearMonth month) {
            if (dialog != null) loadMonthly(dialog, month);
        }
    }

    private abstract class Callback<T> implements ScoreAssistantApiClient.Callback<T> {
        private final ScoreAssistantDialog expected;
        private final long generation;
        Callback(ScoreAssistantDialog expected) {
            this.expected = expected;
            generation = requestGate.issue();
        }
        final boolean active() {
            return !closed && dialog == expected && requestGate.isCurrent(generation);
        }
        @Override public void onUnauthorized() {
            if (!active()) return;
            dismiss();
            listener.onUnauthorized();
        }
        @Override public void onError(String message) {
            if (active()) expected.showError(message);
        }
    }
}
