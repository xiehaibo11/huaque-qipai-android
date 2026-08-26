package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public final class MailApiProtocolTest {
    @Test
    public void parsesTheOriginalTenItemPageMetadata() throws Exception {
        MailApiProtocol.MailPage page =
                MailApiProtocol.mailPageFromJson(
                        "{\"mails\":[{\"mailId\":\"7\",\"title\":\"奖励\","
                                + "\"hasAttachment\":true,\"read\":false,"
                                + "\"claimed\":false}],\"page\":3,\"hasMore\":false}");

        assertEquals(3, page.page());
        assertFalse(page.hasMore());
        assertEquals("7", page.mails().get(0).mailId());
    }

    @Test
    public void parsesOnlyTheMailIdsActuallyDeletedByTheServer() throws Exception {
        MailApiProtocol.MailDeletedCount result =
                MailApiProtocol.MailDeletedCount.fromJson(
                        new org.json.JSONObject(
                                "{\"deletedCount\":1,\"deletedMailIds\":[\"8\"]}"));

        assertEquals(java.util.List.of("8"), result.deletedMailIds());
    }

    @Test
    public void showsLobbyAttentionForUnreadOrUnclaimedAwardMail() {
        org.junit.Assert.assertTrue(new MailApiProtocol.MailSummary(1, 0).hasAttention());
        org.junit.Assert.assertTrue(new MailApiProtocol.MailSummary(0, 1).hasAttention());
        org.junit.Assert.assertFalse(new MailApiProtocol.MailSummary(0, 0).hasAttention());
    }
}
