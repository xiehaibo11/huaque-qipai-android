package com.nanbeiyule.game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** View-local state for the native mail page; server data stays authoritative. */
final class MailState {
    private static final int MAX_CLAIM_BATCH = 10;
    private List<MailApiProtocol.MailEntry> mails = List.of();
    private MailApiProtocol.MailSummary summary = new MailApiProtocol.MailSummary(0, 0);
    private MailApiProtocol.MailDetail detail;
    private boolean selectMode;
    private int page;
    private boolean hasMore;
    private final Set<String> selectedMailIds = new LinkedHashSet<>();

    List<MailApiProtocol.MailEntry> mails() { return mails; }
    MailApiProtocol.MailSummary summary() { return summary; }
    MailApiProtocol.MailDetail detail() { return detail; }
    boolean selectMode() { return selectMode; }
    int page() { return page; }
    boolean hasMore() { return hasMore; }

    void setSummary(MailApiProtocol.MailSummary next) {
        summary = next == null ? new MailApiProtocol.MailSummary(0, 0) : next;
    }

    void setMails(List<MailApiProtocol.MailEntry> next) {
        mails = sorted(next == null ? List.of() : next);
        selectedMailIds.removeIf(id -> find(id) == null);
        if (mails.isEmpty()) {
            selectMode = false;
        }
    }

    void setPage(MailApiProtocol.MailPage next) {
        if (next == null) {
            setMails(List.of());
            page = 0;
            hasMore = false;
            return;
        }
        List<MailApiProtocol.MailEntry> combined = new ArrayList<>();
        if (next.page() > 1) {
            combined.addAll(mails);
        }
        Set<String> ids = new LinkedHashSet<>();
        for (MailApiProtocol.MailEntry entry : combined) {
            ids.add(entry.mailId());
        }
        for (MailApiProtocol.MailEntry entry : next.mails()) {
            if (ids.add(entry.mailId())) {
                combined.add(entry);
            }
        }
        setMails(combined);
        page = next.page();
        hasMore = next.hasMore();
    }

    void setDetail(MailApiProtocol.MailDetail next) {
        detail = next;
    }

    void enterSelectMode() {
        selectMode = true;
        selectedMailIds.clear();
        for (MailApiProtocol.MailEntry entry : mails) {
            selectedMailIds.add(entry.mailId());
        }
    }

    void exitSelectMode() {
        selectMode = false;
        selectedMailIds.clear();
    }

    boolean toggleSelected(String mailId) {
        if (!selectedMailIds.remove(mailId)) {
            selectedMailIds.add(mailId);
            return true;
        }
        return false;
    }

    boolean isSelected(String mailId) {
        return selectedMailIds.contains(mailId);
    }

    List<String> selectedMailIds() {
        return List.copyOf(selectedMailIds);
    }

    /** GoldNew MailView.reqDel：仅已读且没有未领取附件的邮件可提交删除。 */
    List<String> deletableMailIds(List<String> requestedIds) {
        Set<String> requested = Set.copyOf(requestedIds);
        List<String> result = new ArrayList<>();
        for (MailApiProtocol.MailEntry entry : mails) {
            boolean attachmentSettled = !entry.hasAttachment() || entry.claimed();
            if (requested.contains(entry.mailId()) && entry.read() && attachmentSettled) {
                result.add(entry.mailId());
            }
        }
        return result;
    }

    List<String> claimableMailIds() {
        List<String> result = new ArrayList<>();
        for (MailApiProtocol.MailEntry entry : mails) {
            if (entry.hasAttachment() && !entry.claimed()) {
                result.add(entry.mailId());
                if (result.size() == MAX_CLAIM_BATCH) {
                    break;
                }
            }
        }
        return result;
    }

    void markRead(String mailId) {
        MailApiProtocol.MailEntry entry = find(mailId);
        if (entry == null) {
            return;
        }
        List<MailApiProtocol.MailEntry> next = new ArrayList<>(mails);
        next.set(next.indexOf(entry), entry.markRead());
        mails = sorted(next);
    }

    void markAllRead() {
        List<MailApiProtocol.MailEntry> next = new ArrayList<>();
        for (MailApiProtocol.MailEntry entry : mails) {
            next.add(entry.markRead());
        }
        mails = sorted(next);
    }

    void markClaimed(List<String> mailIds) {
        Set<String> claimed = Set.copyOf(mailIds);
        List<MailApiProtocol.MailEntry> next = new ArrayList<>();
        for (MailApiProtocol.MailEntry entry : mails) {
            next.add(claimed.contains(entry.mailId()) ? entry.markClaimed() : entry);
        }
        mails = sorted(next);
        if (detail != null && claimed.contains(detail.entry().mailId())) {
            detail = new MailApiProtocol.MailDetail(
                    detail.entry().markClaimed(), detail.content(), detail.attachments());
        }
    }

    void removeMailIds(List<String> mailIds) {
        Set<String> removed = Set.copyOf(mailIds);
        List<MailApiProtocol.MailEntry> next = new ArrayList<>();
        for (MailApiProtocol.MailEntry entry : mails) {
            if (!removed.contains(entry.mailId())) {
                next.add(entry);
            }
        }
        setMails(next);
        if (detail != null && removed.contains(detail.entry().mailId())) {
            detail = null;
        }
        exitSelectMode();
    }

    /** Lua updateTime：过期隐藏，按天/小时/即将到期显示剩余时长。 */
    static String remainingText(MailApiProtocol.MailEntry entry, Instant now) {
        Instant expireTime = entry.expireTime();
        if (expireTime == null || now == null) {
            return "";
        }
        long seconds = Duration.between(now, expireTime).getSeconds();
        if (seconds <= 0) {
            return "";
        }
        long days = seconds / 86_400L;
        if (days >= 1) {
            return "剩余" + days + "天";
        }
        long hours = seconds / 3_600L;
        if (hours >= 1) {
            return "剩余" + hours + "小时";
        }
        return "即将到期";
    }

    private MailApiProtocol.MailEntry find(String mailId) {
        for (MailApiProtocol.MailEntry entry : mails) {
            if (entry.mailId().equals(mailId)) {
                return entry;
            }
        }
        return null;
    }

    /** GoldNew MailData.sortMailList: state first, then newest publish time. */
    private static List<MailApiProtocol.MailEntry> sorted(
            List<MailApiProtocol.MailEntry> entries) {
        List<MailApiProtocol.MailEntry> result = new ArrayList<>(entries);
        result.sort(
                Comparator.comparingInt(MailState::priority)
                        .thenComparing(
                                MailApiProtocol.MailEntry::sendTime,
                                Comparator.nullsLast(Comparator.reverseOrder())));
        return List.copyOf(result);
    }

    private static int priority(MailApiProtocol.MailEntry entry) {
        boolean awardPending = entry.hasAttachment() && !entry.claimed();
        if (!entry.read()) {
            return awardPending ? 0 : 1;
        }
        return awardPending ? 2 : 3;
    }
}
