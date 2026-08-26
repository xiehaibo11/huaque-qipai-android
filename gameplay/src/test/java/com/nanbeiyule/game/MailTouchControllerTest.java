package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public final class MailTouchControllerTest {
    @Test
    public void requestsTheNextPageAfterPullingPastTheLoadedListBottom() {
        MailState state = new MailState();
        List<MailApiProtocol.MailEntry> mails = new ArrayList<>();
        for (int index = 0; index < 10; index++) {
            mails.add(mail(Integer.toString(index)));
        }
        state.setPage(new MailApiProtocol.MailPage(mails, 1, true));
        RecordingActions actions = new RecordingActions();
        MailTouchController controller = new MailTouchController(actions, state, 8f);

        controller.onDown(800f, 800f);
        controller.onMove(800f, -400f);
        controller.onUp(800f, -400f);

        assertEquals(1, actions.nextPageRequests);
    }

    private static MailApiProtocol.MailEntry mail(String id) {
        return new MailApiProtocol.MailEntry(
                id, id, "", "系统", false, false, false,
                Instant.parse("2026-08-24T12:00:00Z"), null);
    }

    private static final class RecordingActions implements MailTouchController.Actions {
        int nextPageRequests;
        @Override public void onClose() {}
        @Override public void onMailOpen(MailApiProtocol.MailEntry entry) {}
        @Override public void onReadAll() {}
        @Override public void onDelete(List<String> mailIds) {}
        @Override public void onClaimAll() {}
        @Override public void onLoadNextPage() { nextPageRequests++; }
    }
}
