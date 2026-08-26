package com.huaque.ui.friend;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public final class FriendPanelTabLayeringTest {
    @Test
    public void tabsAreRaisedAboveBothContentPages() throws IOException {
        Path source = Path.of("src/main/java/com/huaque/ui/friend/FriendPanelView.java");
        if (!Files.exists(source)) {
            source = Path.of("app").resolve(source);
        }
        String friendPanel = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);

        int upcomingPageAdded = friendPanel.indexOf(
                "expandedPanel.addBox(upcomingPage, 0, 0, PANEL_WIDTH, 1080);");
        int friendsTabRaised = friendPanel.indexOf("friendsTab.bringToFront();");
        int upcomingTabRaised = friendPanel.indexOf("upcomingTab.bringToFront();");

        assertTrue(upcomingPageAdded >= 0);
        assertTrue(friendsTabRaised > upcomingPageAdded);
        assertTrue(upcomingTabRaised > upcomingPageAdded);
    }
}
