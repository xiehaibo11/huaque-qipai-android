package com.nanbeiyule.game.lobbyshare;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LobbyShareControllerTest {
    private static final LobbyShareContent CONTENT =
            LobbyShareContent.create(
                    "南北娱乐", LobbyShareContent.PRODUCTION_DOWNLOAD_URL, null);

    @Test
    public void sendsOnlyARealWechatFriendWebpageRequest() {
        CapturingGateway gateway = new CapturingGateway(LobbyShareGateway.Result.STARTED);
        LobbyShareController controller = new LobbyShareController(CONTENT, gateway);

        assertEquals(
                LobbyShareController.Outcome.WECHAT_OPENED,
                controller.shareToWechatFriend());
        assertEquals("南北娱乐", gateway.title);
        assertEquals("", gateway.description);
        assertEquals(CONTENT.webpageUrl(), gateway.url);
        assertEquals(1, gateway.callCount);
    }

    @Test
    public void unavailableWechatOffersRealLinkFallbackWithoutClaimingSuccess() {
        CapturingGateway gateway =
                new CapturingGateway(LobbyShareGateway.Result.NOT_INSTALLED);
        LobbyShareController controller = new LobbyShareController(CONTENT, gateway);

        assertEquals(
                LobbyShareController.Outcome.WECHAT_UNAVAILABLE,
                controller.shareToWechatFriend());
        assertEquals(CONTENT.webpageUrl(), controller.copyableLink());
    }

    @Test
    public void rejectedWechatRequestDoesNotBecomeAClientSideSuccess() {
        CapturingGateway gateway = new CapturingGateway(LobbyShareGateway.Result.REJECTED);
        LobbyShareController controller = new LobbyShareController(CONTENT, gateway);

        assertEquals(
                LobbyShareController.Outcome.WECHAT_REJECTED,
                controller.shareToWechatFriend());
    }

    private static final class CapturingGateway implements LobbyShareGateway {
        private final Result result;
        private String title;
        private String description;
        private String url;
        private int callCount;

        private CapturingGateway(Result result) {
            this.result = result;
        }

        @Override
        public Result shareToFriend(String title, String description, String webpageUrl) {
            this.title = title;
            this.description = description;
            this.url = webpageUrl;
            callCount++;
            return result;
        }
    }
}
