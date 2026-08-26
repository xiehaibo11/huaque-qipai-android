package com.nanbeiyule.game;

import java.util.List;

final class MailMutationCoordinator {
    private final MainActivityMailFlow owner;
    private boolean inFlight;

    MailMutationCoordinator(MainActivityMailFlow owner) {
        this.owner = owner;
    }

    void cancel() {
        inFlight = false;
    }

    void readAll() {
        mutate(
                (token, callback) -> owner.activeMailClient().readAll(
                        token, new MailResponseCallbackAdapter<>(callback)),
                new AuthSessionCoordinator.Callback<MailApiProtocol.MailMarkedCount>() {
                    @Override public void onSuccess(MailApiProtocol.MailMarkedCount result) {
                        finish();
                        if (owner.activeMailDialog() != null) owner.activeMailDialog().markAllRead();
                        owner.refreshHomeAfterMailMutation();
                    }
                    @Override public void onLoginRequired() { loginRequired(); }
                    @Override public void onError(String message) { fail(message); }
                });
    }

    void delete(List<String> mailIds) {
        mutate(
                (token, callback) -> owner.activeMailClient().delete(
                        token, mailIds, new MailResponseCallbackAdapter<>(callback)),
                new AuthSessionCoordinator.Callback<MailApiProtocol.MailDeletedCount>() {
                    @Override public void onSuccess(MailApiProtocol.MailDeletedCount result) {
                        finish();
                        if (owner.activeMailDialog() != null) {
                            owner.activeMailDialog().removeMailIds(result.deletedMailIds());
                        }
                        owner.showMailText(owner.getString(R.string.mail_delete_success));
                    }
                    @Override public void onLoginRequired() { loginRequired(); }
                    @Override public void onError(String message) { fail(message); }
                });
    }

    void claim(List<String> mailIds) {
        mutate(
                (token, callback) -> owner.activeMailClient().claim(
                        token, mailIds, new MailResponseCallbackAdapter<>(callback)),
                new AuthSessionCoordinator.Callback<MailApiProtocol.MailClaimResult>() {
                    @Override public void onSuccess(MailApiProtocol.MailClaimResult result) {
                        finish();
                        if (owner.activeMailDialog() != null) {
                            owner.activeMailDialog().markClaimed(result.claimedMailIds());
                        }
                        owner.refreshHomeAfterMailMutation();
                        owner.showMailText(owner.getString(R.string.mail_claim_success));
                    }
                    @Override public void onLoginRequired() { loginRequired(); }
                    @Override public void onError(String message) { fail(message); }
                });
    }

    private <T> void mutate(
            AuthSessionCoordinator.AuthenticatedCall<T> request,
            AuthSessionCoordinator.Callback<T> callback) {
        MailDialog dialog = owner.activeMailDialog();
        if (inFlight || dialog == null || owner.activeMailClient() == null
                || owner.activeMailAuth() == null) {
            return;
        }
        inFlight = true;
        owner.activeMailAuth().execute(
                request,
                new AuthSessionCoordinator.Callback<>() {
                    @Override public void onSuccess(T result) {
                        if (owner.activeMailDialog() == dialog) callback.onSuccess(result);
                    }
                    @Override public void onLoginRequired() {
                        if (owner.activeMailDialog() == dialog) callback.onLoginRequired();
                    }
                    @Override public void onError(String message) {
                        if (owner.activeMailDialog() == dialog) callback.onError(message);
                    }
                });
    }

    private void finish() {
        inFlight = false;
    }

    private void loginRequired() {
        finish();
        owner.showLoginAfterMailExpired();
    }

    private void fail(String message) {
        finish();
        owner.showMailText(message);
    }
}
