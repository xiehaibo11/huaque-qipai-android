package com.nanbeiyule.game;

import android.app.Activity;
import java.util.List;
import java.util.Objects;

/** Public host used by the launcher lobby without duplicating the recovered mail module. */
public final class MailFeatureController implements AutoCloseable {
    public interface TokenProvider {
        String accessToken();
    }

    public interface Listener {
        void onAttentionChanged(boolean visible);

        void onUnauthorized();

        void onMessage(String message);
    }

    private final Activity activity;
    private final TokenProvider tokenProvider;
    private final Listener listener;
    private final Runnable buttonClickSound;
    private final MailApiClient client;
    private MailDialog dialog;
    private boolean mutationInFlight;
    private boolean pageLoadInFlight;
    private boolean closed;

    public MailFeatureController(
            Activity activity,
            String apiBaseUrl,
            TokenProvider tokenProvider,
            Listener listener,
            Runnable buttonClickSound) {
        this.activity = Objects.requireNonNull(activity, "activity");
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.buttonClickSound = buttonClickSound == null ? () -> {} : buttonClickSound;
        client = new MailApiClient(apiBaseUrl);
    }

    public void show() {
        if (closed || activity.isFinishing() || dialog != null) {
            return;
        }
        MailDialog opened = new MailDialog(activity, new Actions());
        dialog = opened;
        opened.setButtonClickSound(buttonClickSound);
        opened.setOnDismissListener(
                ignored -> {
                    if (dialog == opened) {
                        dialog = null;
                        mutationInFlight = false;
                        pageLoadInFlight = false;
                    }
                });
        opened.show();
        loadPage(1);
        loadSummary(opened);
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void refreshAttention() {
        if (closed) return;
        client.loadSummary(
                token(),
                new Callback<>(null) {
                    @Override public void onSuccess(MailApiProtocol.MailSummary result) {
                        if (active()) listener.onAttentionChanged(result.hasAttention());
                    }
                });
    }

    @Override
    public void close() {
        closed = true;
        dismiss();
        client.shutdown();
    }

    private void loadSummary(MailDialog expected) {
        client.loadSummary(
                token(),
                new Callback<>(expected) {
                    @Override public void onSuccess(MailApiProtocol.MailSummary result) {
                        if (!active()) return;
                        expected.setSummary(result);
                        listener.onAttentionChanged(result.hasAttention());
                    }
                });
    }

    private void loadPage(int page) {
        MailDialog expected = dialog;
        if (expected == null || pageLoadInFlight) return;
        pageLoadInFlight = true;
        expected.setLoading(true);
        client.loadMails(
                token(),
                page,
                new Callback<>(expected) {
                    @Override public void onSuccess(MailApiProtocol.MailPage result) {
                        if (!active()) return;
                        pageLoadInFlight = false;
                        expected.setPage(result);
                        expected.setLoading(false);
                    }

                    @Override protected void requestFailed(String message) {
                        pageLoadInFlight = false;
                        expected.setLoading(false);
                        expected.setError(message);
                    }
                });
    }

    private void openDetail(MailApiProtocol.MailEntry entry) {
        MailDialog expected = dialog;
        if (expected == null) return;
        client.loadDetail(
                token(),
                entry.mailId(),
                new Callback<>(expected) {
                    @Override public void onSuccess(MailApiProtocol.MailDetail result) {
                        if (!active()) return;
                        expected.markRead(result.entry().mailId());
                        expected.setDetail(result);
                        refreshAttention();
                    }
                });
    }

    private void readAll() {
        MailDialog expected = beginMutation();
        if (expected == null) return;
        client.readAll(
                token(),
                new MutationCallback<>(expected) {
                    @Override public void onSuccess(MailApiProtocol.MailMarkedCount result) {
                        if (!finish()) return;
                        expected.markAllRead();
                        refreshAttention();
                    }
                });
    }

    private void delete(List<String> mailIds) {
        MailDialog expected = beginMutation();
        if (expected == null) return;
        client.delete(
                token(),
                mailIds,
                new MutationCallback<>(expected) {
                    @Override public void onSuccess(MailApiProtocol.MailDeletedCount result) {
                        if (!finish()) return;
                        expected.removeMailIds(result.deletedMailIds());
                        listener.onMessage(activity.getString(R.string.mail_delete_success));
                        refreshAttention();
                    }
                });
    }

    private void claim(List<String> mailIds) {
        MailDialog expected = beginMutation();
        if (expected == null) return;
        client.claim(
                token(),
                mailIds,
                new MutationCallback<>(expected) {
                    @Override public void onSuccess(MailApiProtocol.MailClaimResult result) {
                        if (!finish()) return;
                        expected.markClaimed(result.claimedMailIds());
                        listener.onMessage(activity.getString(R.string.mail_claim_success));
                        refreshAttention();
                    }
                });
    }

    private MailDialog beginMutation() {
        if (mutationInFlight || dialog == null) return null;
        mutationInFlight = true;
        return dialog;
    }

    private String token() {
        String value = tokenProvider.accessToken();
        return value == null ? "" : value;
    }

    private class Actions implements MailDialog.Actions {
        @Override public void onClose() { dismiss(); }
        @Override public void onMailOpen(MailApiProtocol.MailEntry entry) { openDetail(entry); }
        @Override public void onReadAll() { readAll(); }
        @Override public void onDelete(List<String> ids) { delete(ids); }
        @Override public void onDeleteBlocked() {
            listener.onMessage(activity.getString(R.string.mail_delete_blocked));
        }
        @Override public void onClaimAll() {
            if (dialog == null) return;
            List<String> ids = dialog.state().claimableMailIds();
            if (ids.isEmpty()) {
                listener.onMessage(activity.getString(R.string.mail_no_claimable));
            } else {
                claim(ids);
            }
        }
        @Override public void onLoadNextPage() {
            if (dialog != null && dialog.state().hasMore()) {
                loadPage(dialog.state().page() + 1);
            }
        }
        @Override public void onDetailDelete(String id) { delete(List.of(id)); }
        @Override public void onDetailClaim(String id) { claim(List.of(id)); }
    }

    private abstract class Callback<T> implements MailApiClient.ResponseCallback<T> {
        private final MailDialog expected;

        Callback(MailDialog expected) {
            this.expected = expected;
        }

        final boolean active() {
            return !closed && (expected == null || dialog == expected);
        }

        @Override public void onUnauthorized() {
            if (!active()) return;
            mutationInFlight = false;
            pageLoadInFlight = false;
            dismiss();
            listener.onUnauthorized();
        }

        @Override public void onError(String message) {
            if (!active()) return;
            requestFailed(message);
        }

        protected void requestFailed(String message) {
            listener.onMessage(message);
        }
    }

    private abstract class MutationCallback<T> extends Callback<T> {
        MutationCallback(MailDialog expected) {
            super(expected);
        }

        final boolean finish() {
            if (!active()) return false;
            mutationInFlight = false;
            return true;
        }

        @Override protected void requestFailed(String message) {
            mutationInFlight = false;
            super.requestFailed(message);
        }
    }
}
