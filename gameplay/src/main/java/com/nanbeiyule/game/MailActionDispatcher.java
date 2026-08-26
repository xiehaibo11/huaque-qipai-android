package com.nanbeiyule.game;

import java.util.List;

final class MailActionDispatcher implements MailDialog.Actions {
    private final MainActivityMailFlow owner;

    MailActionDispatcher(MainActivityMailFlow owner) {
        this.owner = owner;
    }

    @Override public void onClose() { owner.closeMail(); }
    @Override public void onMailOpen(MailApiProtocol.MailEntry entry) { owner.openMail(entry); }
    @Override public void onReadAll() { owner.readAllMails(); }
    @Override public void onDelete(List<String> mailIds) { owner.deleteMails(mailIds); }
    @Override public void onDeleteBlocked() { owner.showMailDeleteBlocked(); }
    @Override public void onClaimAll() { owner.claimAllMails(); }
    @Override public void onLoadNextPage() { owner.loadNextMailPage(); }
    @Override public void onDetailDelete(String mailId) { owner.deleteMails(List.of(mailId)); }
    @Override public void onDetailClaim(String mailId) { owner.claimMails(List.of(mailId)); }
}
