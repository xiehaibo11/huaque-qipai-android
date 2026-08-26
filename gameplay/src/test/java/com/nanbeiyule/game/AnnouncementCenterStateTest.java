package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.Test;

public class AnnouncementCenterStateTest {
    @Test
    public void emptyAndFailureAreExplicitInsteadOfPlaceholderRows() {
        AnnouncementCenterState state = new AnnouncementCenterState();

        state.showPage(new AnnouncementApiProtocol.AnnouncementPage(900023L, List.of()));
        assertEquals(AnnouncementCenterState.PageState.EMPTY, state.pageState());

        state.beginPageLoad();
        assertEquals(AnnouncementCenterState.PageState.LOADING, state.pageState());

        state.showPageError("公告服务暂时不可用");
        assertEquals(AnnouncementCenterState.PageState.ERROR, state.pageState());
        assertEquals("公告服务暂时不可用", state.message());
        assertTrue(state.announcements().isEmpty());
    }

    @Test
    public void detailResponseMarksOnlyItsCurrentServerVersionRead() {
        AnnouncementApiProtocol.AnnouncementSummary first = summary(71L, 4L, false);
        AnnouncementApiProtocol.AnnouncementSummary second = summary(72L, 2L, false);
        AnnouncementCenterState state = new AnnouncementCenterState();
        state.showPage(new AnnouncementApiProtocol.AnnouncementPage(900023L, List.of(first, second)));

        assertEquals(2, state.unreadCount());
        assertTrue(state.beginDetailLoad(71L));
        assertTrue(state.detailLoading());
        state.showDetail(detail(71L, 4L));

        assertFalse(state.detailLoading());
        assertEquals(1, state.unreadCount());
        assertTrue(state.announcements().get(0).read());
        assertFalse(state.announcements().get(1).read());
        assertEquals(71L, state.detail().announcementId());
    }

    @Test
    public void staleDetailCannotOverwriteASelectionMadeAfterItsRequest() {
        AnnouncementCenterState state = new AnnouncementCenterState();
        state.showPage(
                new AnnouncementApiProtocol.AnnouncementPage(
                        900023L,
                        List.of(summary(71L, 4L, false), summary(72L, 2L, false))));

        assertTrue(state.beginDetailLoad(71L));
        assertTrue(state.beginDetailLoad(72L));
        state.showDetail(detail(71L, 4L));

        assertTrue(state.detailLoading());
        assertEquals(72L, state.selectedAnnouncementId());
        assertEquals(2, state.unreadCount());
    }

    private static AnnouncementApiProtocol.AnnouncementSummary summary(
            long id, long version, boolean read) {
        return new AnnouncementApiProtocol.AnnouncementSummary(
                id,
                "公告" + id,
                "台州大厅",
                "公告内容",
                "",
                900023L,
                (int) id,
                Instant.parse("2026-08-24T10:00:00Z"),
                null,
                version,
                read);
    }

    private static AnnouncementApiProtocol.AnnouncementDetail detail(long id, long version) {
        return new AnnouncementApiProtocol.AnnouncementDetail(
                id,
                "公告" + id,
                "台州大厅",
                "公告内容",
                "",
                900023L,
                (int) id,
                Instant.parse("2026-08-24T10:00:00Z"),
                null,
                version,
                true,
                Instant.parse("2026-08-24T10:05:00Z"));
    }
}
