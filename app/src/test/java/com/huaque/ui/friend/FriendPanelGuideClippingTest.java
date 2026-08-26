package com.huaque.ui.friend;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public final class FriendPanelGuideClippingTest {
    @Test
    public void guideCanRenderPastBothUpcomingPageAndExpandedPanel() throws IOException {
        Path source = Path.of("src/main/java/com/huaque/ui/friend/FriendPanelView.java");
        if (!Files.exists(source)) {
            source = Path.of("app").resolve(source);
        }
        String friendPanel = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);

        assertTrue(friendPanel.contains("this.setClipChildren(false);"));
        assertTrue(friendPanel.contains("expandedPanel.setClipChildren(false);"));
        assertTrue(friendPanel.contains("page.setClipChildren(false);"));
    }
}
