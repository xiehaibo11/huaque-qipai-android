package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AnnouncementApiProtocolTest {
    @Test
    public void parsesTheStableAnnouncementListContractWithoutInventingNullableContent() throws Exception {
        AnnouncementApiProtocol.AnnouncementPage page =
                AnnouncementApiProtocol.pageFromJson(
                        """
                        {
                          "lobbyId": 900023,
                          "announcements": [{
                            "announcementId": 71,
                            "title": "系统公告",
                            "subtitle": "台州大厅",
                            "bodyText": "今日更新完成",
                            "pageUrl": null,
                            "lobbyId": 900023,
                            "sortOrder": 3,
                            "startsAt": "2026-08-24T10:00:00Z",
                            "endsAt": null,
                            "version": 4,
                            "read": false
                          }]
                        }
                        """);

        assertEquals(900023L, page.lobbyId());
        assertEquals(1, page.announcements().size());
        AnnouncementApiProtocol.AnnouncementSummary item = page.announcements().get(0);
        assertEquals(71L, item.announcementId());
        assertEquals("系统公告", item.title());
        assertEquals("今日更新完成", item.bodyText());
        assertEquals("", item.pageUrl());
        assertFalse(item.read());
        assertNull(item.endsAt());
    }

    @Test
    public void parsesDetailAsTheServerAuthoritativeReadReceipt() throws Exception {
        AnnouncementApiProtocol.AnnouncementDetail detail =
                AnnouncementApiProtocol.detailFromJson(
                        """
                        {
                          "announcementId": 71,
                          "title": "系统公告",
                          "subtitle": "台州大厅",
                          "bodyText": null,
                          "pageUrl": "https://zjnews.zjol.com.cn/a/71",
                          "lobbyId": null,
                          "sortOrder": 3,
                          "startsAt": "2026-08-24T10:00:00Z",
                          "endsAt": "2026-09-01T10:00:00Z",
                          "version": 4,
                          "read": true,
                          "readAt": "2026-08-24T10:05:00Z"
                        }
                        """);

        assertEquals(71L, detail.announcementId());
        assertEquals("", detail.bodyText());
        assertEquals("https://zjnews.zjol.com.cn/a/71", detail.pageUrl());
        assertTrue(detail.read());
        assertEquals("2026-08-24T10:05:00Z", detail.readAt().toString());
    }

    @Test
    public void parsesTheExplicitReadEndpointReceipt() throws Exception {
        AnnouncementApiProtocol.ReadReceipt receipt =
                AnnouncementApiProtocol.readReceiptFromJson(
                        """
                        {
                          "announcementId": 71,
                          "version": 4,
                          "read": true,
                          "readAt": "2026-08-24T10:05:00Z"
                        }
                        """);

        assertEquals(71L, receipt.announcementId());
        assertEquals(4L, receipt.version());
        assertTrue(receipt.read());
        assertEquals("2026-08-24T10:05:00Z", receipt.readAt().toString());
    }

    @Test
    public void usesTheExactAuthenticatedBackendRoutes() {
        assertEquals("/api/v1/announcements", AnnouncementApiClient.listPath());
        assertEquals("/api/v1/announcements/71", AnnouncementApiClient.detailPath(71L));
        assertEquals("/api/v1/announcements/71/read", AnnouncementApiClient.readPath(71L));
    }

    @Test
    public void externalAnnouncementPagesMustRemainHttps() {
        assertTrue(AnnouncementPageUrlPolicy.isSafe("https://zjnews.zjol.com.cn/a/71"));
        assertFalse(AnnouncementPageUrlPolicy.isSafe("http://zjnews.zjol.com.cn/a/71"));
        assertFalse(AnnouncementPageUrlPolicy.isSafe("javascript:alert(1)"));
        assertFalse(AnnouncementPageUrlPolicy.isSafe("https://user@example.com/a/71"));
    }
}
