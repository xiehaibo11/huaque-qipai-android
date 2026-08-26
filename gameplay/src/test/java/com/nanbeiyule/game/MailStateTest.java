package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.List;
import org.junit.Test;

public final class MailStateTest {
    private static final Instant NOW = Instant.parse("2026-08-24T12:00:00Z");

    @Test
    public void sortsWithTheOriginalUnreadAndUnclaimedAttachmentPriority() {
        MailState state = new MailState();

        state.setMails(
                List.of(
                        mail("read-plain-new", false, true, NOW.plusSeconds(40)),
                        mail("unread-plain", false, false, NOW.plusSeconds(20)),
                        mail("read-award", true, true, NOW.plusSeconds(30)),
                        mail("unread-award-old", true, false, NOW.plusSeconds(10)),
                        mail("unread-award-new", true, false, NOW.plusSeconds(50))));

        assertEquals(
                List.of(
                        "unread-award-new",
                        "unread-award-old",
                        "unread-plain",
                        "read-award",
                        "read-plain-new"),
                state.mails().stream().map(MailApiProtocol.MailEntry::mailId).toList());
    }

    private static MailApiProtocol.MailEntry mail(
            String id, boolean hasAttachment, boolean read, Instant sendTime) {
        return new MailApiProtocol.MailEntry(
                id,
                id,
                "",
                "系统",
                hasAttachment,
                read,
                false,
                sendTime,
                NOW.plusSeconds(86_400));
    }
}
