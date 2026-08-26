package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.List;

/** Server-authoritative state for the announcement list and selected detail. */
final class AnnouncementCenterState {
    enum PageState {
        LOADING,
        EMPTY,
        CONTENT,
        ERROR
    }

    private PageState pageState = PageState.LOADING;
    private List<AnnouncementApiProtocol.AnnouncementSummary> announcements = List.of();
    private AnnouncementApiProtocol.AnnouncementDetail detail;
    private long selectedAnnouncementId;
    private boolean detailLoading;
    private String message = "";

    PageState pageState() {
        return pageState;
    }

    List<AnnouncementApiProtocol.AnnouncementSummary> announcements() {
        return announcements;
    }

    AnnouncementApiProtocol.AnnouncementDetail detail() {
        return detail;
    }

    long selectedAnnouncementId() {
        return selectedAnnouncementId;
    }

    boolean detailLoading() {
        return detailLoading;
    }

    String message() {
        return message;
    }

    int unreadCount() {
        int count = 0;
        for (AnnouncementApiProtocol.AnnouncementSummary announcement : announcements) {
            if (!announcement.read()) {
                count++;
            }
        }
        return count;
    }

    void beginPageLoad() {
        pageState = PageState.LOADING;
        announcements = List.of();
        detail = null;
        selectedAnnouncementId = 0L;
        detailLoading = false;
        message = "";
    }

    void showPage(AnnouncementApiProtocol.AnnouncementPage page) {
        announcements = page == null ? List.of() : List.copyOf(page.announcements());
        pageState = announcements.isEmpty() ? PageState.EMPTY : PageState.CONTENT;
        detail = null;
        selectedAnnouncementId = 0L;
        detailLoading = false;
        message = "";
    }

    void showPageError(String error) {
        pageState = PageState.ERROR;
        announcements = List.of();
        detail = null;
        selectedAnnouncementId = 0L;
        detailLoading = false;
        message = safeMessage(error, "公告加载失败，请稍后重试");
    }

    boolean beginDetailLoad(long announcementId) {
        if (find(announcementId) == null) {
            return false;
        }
        selectedAnnouncementId = announcementId;
        detail = null;
        detailLoading = true;
        message = "";
        return true;
    }

    void showDetail(AnnouncementApiProtocol.AnnouncementDetail result) {
        if (result == null
                || !detailLoading
                || selectedAnnouncementId != result.announcementId()) {
            return;
        }
        detail = result;
        detailLoading = false;
        message = "";
        AnnouncementApiProtocol.AnnouncementSummary current = find(result.announcementId());
        if (current != null && current.version() == result.version() && result.read()) {
            List<AnnouncementApiProtocol.AnnouncementSummary> next =
                    new ArrayList<>(announcements);
            next.set(next.indexOf(current), current.withRead(true));
            announcements = List.copyOf(next);
        }
    }

    void showDetailError(long announcementId, String error) {
        if (!detailLoading || selectedAnnouncementId != announcementId) {
            return;
        }
        detailLoading = false;
        message = safeMessage(error, "公告详情加载失败");
    }

    private AnnouncementApiProtocol.AnnouncementSummary find(long announcementId) {
        for (AnnouncementApiProtocol.AnnouncementSummary announcement : announcements) {
            if (announcement.announcementId() == announcementId) {
                return announcement;
            }
        }
        return null;
    }

    private static String safeMessage(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
