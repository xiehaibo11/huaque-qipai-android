package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.List;
import org.junit.Test;

public final class MailDetailTouchControllerTest {
    @Test
    public void routesTheOriginalDeleteAndClaimButtonsForPendingAwards() {
        RecordingActions actions = new RecordingActions();
        MailDetailTouchController controller = new MailDetailTouchController(actions, 8f);
        controller.setDetail(detail(true, false));

        tap(controller, MailLayout.DETAIL_DELETE.centerX(), MailLayout.DETAIL_DELETE.centerY());
        tap(controller, MailLayout.DETAIL_CLAIM.centerX(), MailLayout.DETAIL_CLAIM.centerY());

        assertEquals(List.of("blocked", "claim:7"), actions.events);
    }

    @Test
    public void disablesClaimAfterTheAwardWasAlreadyClaimed() {
        RecordingActions actions = new RecordingActions();
        MailDetailTouchController controller = new MailDetailTouchController(actions, 8f);
        controller.setDetail(detail(true, true));

        tap(controller, MailLayout.DETAIL_CLAIM.centerX(), MailLayout.DETAIL_CLAIM.centerY());

        assertEquals(List.of(), actions.events);
    }

    private static void tap(MailDetailTouchController controller, float x, float y) {
        controller.onDown(x, y);
        controller.onUp(x, y);
    }

    private static MailApiProtocol.MailDetail detail(boolean attachment, boolean claimed) {
        MailApiProtocol.MailEntry entry = new MailApiProtocol.MailEntry(
                "7", "标题", "", "系统", attachment, true, claimed,
                Instant.parse("2026-08-24T12:00:00Z"), null);
        List<MailApiProtocol.MailAttachment> attachments = attachment
                ? List.of(new MailApiProtocol.MailAttachment("", "COIN", 100, "金币"))
                : List.of();
        return new MailApiProtocol.MailDetail(entry, "正文", attachments);
    }

    private static final class RecordingActions implements MailDetailTouchController.Actions {
        final java.util.ArrayList<String> events = new java.util.ArrayList<>();
        @Override public void onClose() { events.add("close"); }
        @Override public void onDelete(String mailId) { events.add("delete:" + mailId); }
        @Override public void onClaim(String mailId) { events.add("claim:" + mailId); }
        @Override public void onDeleteBlocked() { events.add("blocked"); }
    }
}
