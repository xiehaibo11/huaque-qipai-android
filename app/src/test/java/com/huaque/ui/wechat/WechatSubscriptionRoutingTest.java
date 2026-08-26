package com.huaque.ui.wechat;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public final class WechatSubscriptionRoutingTest {
    @Test
    public void bothWxEntryActivitiesRouteEverySubscriptionResponseField() throws Exception {
        assertRoute(source("app/src/main/java/com/huaque/ui/wxapi/WXEntryActivity.java"));
        assertRoute(source(
                "gameplay/src/main/java/com/nanbeiyule/game/wxapi/WXEntryActivity.java"));
        String manager = source(
                "gameplay/src/main/java/com/nanbeiyule/game/wechat/WechatLoginManager.java");
        assertTrue(manager.contains("new SubscribeMessage.Req()"));
        assertTrue(manager.contains("request.transaction = intent.intentId()"));
        assertTrue(manager.contains("request.templateID = intent.templateId()"));
        assertTrue(manager.contains("request.scene = intent.scene()"));
        assertTrue(manager.contains("request.reserved = intent.reserved()"));
    }

    private static void assertRoute(String source) {
        assertTrue(source.contains("COMMAND_SUBSCRIBE_MESSAGE"));
        assertTrue(source.contains("SubscribeMessage.Resp"));
        assertTrue(source.contains("ACTION_SUBSCRIPTION_RESPONSE"));
        assertTrue(source.contains("EXTRA_ACTION"));
        assertTrue(source.contains("EXTRA_TEMPLATE_ID"));
        assertTrue(source.contains("EXTRA_SCENE"));
        assertTrue(source.contains("EXTRA_RESERVED"));
        assertTrue(source.contains("EXTRA_TRANSACTION"));
        assertTrue(source.contains("EXTRA_OPEN_ID"));
    }

    private static String source(String relative) throws Exception {
        Path path = Path.of(relative);
        if (!Files.exists(path)) path = Path.of("..").resolve(path);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
