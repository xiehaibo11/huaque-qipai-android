package com.nanbeiyule.game;

import android.widget.Toast;
import java.util.List;

/** Owns the authenticated native mail window. */
abstract class MainActivityMailFlow extends MainActivityShopFlow {
    private MailApiClient mailApiClient;
    private MailDialog mailDialog;
    private boolean pageLoadInFlight;
    private long requestGeneration;
    private final MailMutationCoordinator mailMutations = new MailMutationCoordinator(this);

    @Override
    protected void displayGameHome(GameHomeState state) {
        super.displayGameHome(state);
        loadMailAttention();
    }

    @Override
    protected void showMail() {
        if (isFinishing()
                || mailDialog != null
                || authSessionCoordinator == null
                || currentHomeView == null
                || currentHomeState == null) {
            return;
        }
        if (mailApiClient == null) {
            mailApiClient = new MailApiClient();
        }
        mailDialog = new MailDialog(this, new MailActionDispatcher(this));
        if (originalLobbyAudioController != null) {
            mailDialog.setButtonClickSound(originalLobbyAudioController::playButtonClick);
        }
        mailDialog.setOnDismissListener(
                ignored -> {
                    mailDialog = null;
                    mailMutations.cancel();
                    pageLoadInFlight = false;
                    requestGeneration++;
                    applyImmersiveMode();
                });
        mailDialog.show();
        loadMails();
        loadSummary();
    }

    private void loadSummary() {
        MailDialog dialog = mailDialog;
        if (dialog == null || mailApiClient == null || authSessionCoordinator == null) {
            return;
        }
        long generation = requestGeneration;
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        mailApiClient.loadSummary(accessToken, mailCallback(callback)),
                new AuthSessionCoordinator.Callback<MailApiProtocol.MailSummary>() {
                    @Override
                    public void onSuccess(MailApiProtocol.MailSummary result) {
                        if (generation == requestGeneration
                                && mailDialog == dialog
                                && result != null) {
                            dialog.setSummary(result);
                        }
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissMailForLogin(generation);
                    }

                    @Override
                    public void onError(String message) {
                        // 汇总红点只是附加信息，失败时保留列表内容。
                    }
                });
    }

    private void loadMailAttention() {
        GameHomeView homeView = currentHomeView;
        if (homeView == null || authSessionCoordinator == null) return;
        if (mailApiClient == null) mailApiClient = new MailApiClient();
        authSessionCoordinator.execute(
                (token, callback) -> mailApiClient.loadSummary(
                        token, new MailResponseCallbackAdapter<>(callback)),
                new AuthSessionCoordinator.Callback<MailApiProtocol.MailSummary>() {
                    @Override public void onSuccess(MailApiProtocol.MailSummary result) {
                        if (currentHomeView == homeView && result != null) {
                            homeView.setMailAttention(result.hasAttention());
                        }
                    }
                    @Override public void onLoginRequired() {}
                    @Override public void onError(String message) {}
                });
    }

    private void loadMails() {
        loadMails(1);
    }

    private void loadMails(int page) {
        MailDialog dialog = mailDialog;
        if (dialog == null || mailApiClient == null || authSessionCoordinator == null) {
            return;
        }
        long generation = ++requestGeneration;
        pageLoadInFlight = true;
        dialog.setLoading(true);
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        mailApiClient.loadMails(accessToken, page, mailCallback(callback)),
                new AuthSessionCoordinator.Callback<MailApiProtocol.MailPage>() {
                    @Override
                    public void onSuccess(MailApiProtocol.MailPage result) {
                        if (generation != requestGeneration || mailDialog == null) {
                            return;
                        }
                        pageLoadInFlight = false;
                        mailDialog.setPage(result);
                        mailDialog.setLoading(false);
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissMailForLogin(generation);
                    }

                    @Override
                    public void onError(String message) {
                        if (generation != requestGeneration || mailDialog == null) {
                            return;
                        }
                        pageLoadInFlight = false;
                        mailDialog.setLoading(false);
                        mailDialog.setError(message);
                    }
                });
    }

    void loadNextMailPage() {
        MailDialog dialog = mailDialog;
        if (dialog == null || pageLoadInFlight || !dialog.state().hasMore()) {
            return;
        }
        loadMails(dialog.state().page() + 1);
    }

    void openMail(MailApiProtocol.MailEntry entry) {
        MailDialog dialog = mailDialog;
        if (dialog == null || mailApiClient == null || authSessionCoordinator == null) {
            return;
        }
        long generation = ++requestGeneration;
        authSessionCoordinator.execute(
                (accessToken, callback) ->
                        mailApiClient.loadDetail(
                                accessToken, entry.mailId(), mailCallback(callback)),
                new AuthSessionCoordinator.Callback<MailApiProtocol.MailDetail>() {
                    @Override
                    public void onSuccess(MailApiProtocol.MailDetail result) {
                        if (generation != requestGeneration
                                || mailDialog == null
                                || result == null) {
                            return;
                        }
                        mailDialog.markRead(result.entry().mailId());
                        mailDialog.setDetail(result);
                    }

                    @Override
                    public void onLoginRequired() {
                        dismissMailForLogin(generation);
                    }

                    @Override
                    public void onError(String message) {
                        if (generation == requestGeneration) {
                            showMailText(message);
                        }
                    }
                });
    }

    void readAllMails() {
        mailMutations.readAll();
    }

    void deleteMails(List<String> mailIds) {
        mailMutations.delete(mailIds);
    }

    void claimAllMails() {
        MailDialog dialog = mailDialog;
        if (dialog == null) {
            return;
        }
        List<String> mailIds = dialog.state().claimableMailIds();
        if (mailIds.isEmpty()) {
            showMailText(getString(R.string.mail_no_claimable));
            return;
        }
        claimMails(mailIds);
    }

    void claimMails(List<String> mailIds) {
        mailMutations.claim(mailIds);
    }

    private void dismissMailForLogin(long generation) {
        if (generation != requestGeneration) {
            return;
        }
        if (mailDialog != null) {
            mailDialog.dismiss();
        }
        showLoginPage();
    }

    void showMailText(String message) {
        if (!isFinishing() && message != null && !message.isBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private <T> MailApiClient.ResponseCallback<T> mailCallback(
            AuthSessionCoordinator.CallCallback<T> callback) {
        return new MailResponseCallbackAdapter<>(callback);
    }

    void closeMail() {
        if (mailDialog != null) {
            mailDialog.dismiss();
        }
    }

    void showMailDeleteBlocked() {
        showMailText(getString(R.string.mail_delete_blocked));
    }

    MailDialog activeMailDialog() { return mailDialog; }
    MailApiClient activeMailClient() { return mailApiClient; }
    AuthSessionCoordinator activeMailAuth() { return authSessionCoordinator; }
    void refreshHomeAfterMailMutation() { loadGameHome(); }
    void showLoginAfterMailExpired() {
        closeMail();
        showLoginPage();
    }

    @Override
    protected void onDestroy() {
        if (mailDialog != null) {
            mailDialog.dismiss();
            mailDialog = null;
        }
        if (mailApiClient != null) {
            mailApiClient.shutdown();
            mailApiClient = null;
        }
        super.onDestroy();
    }

}
