package com.huaque.ui;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public final class LobbyFriendPanelLayeringTest {
    @Test
    public void personalCenterHitAreaIsAddedBelowFriendPanel() throws IOException {
        Path source = Path.of("src/main/java/com/huaque/ui/MainActivity.java");
        if (!Files.exists(source)) {
            source = Path.of("app").resolve(source);
        }
        String mainActivity = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);

        int personalCenter = mainActivity.indexOf("\"\u4e2a\u4eba\u4e2d\u5fc3\"");
        int friendPanel = mainActivity.indexOf("FriendPanelView friendPanel =");

        assertTrue("personal-center hit area must exist", personalCenter >= 0);
        assertTrue("friend panel must exist", friendPanel >= 0);
        assertTrue(
                "expanded friend panel must be above the overlapping personal-center hit area",
                personalCenter < friendPanel);
    }
}
