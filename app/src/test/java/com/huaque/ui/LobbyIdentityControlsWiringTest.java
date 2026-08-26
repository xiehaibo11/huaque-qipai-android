package com.huaque.ui;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public final class LobbyIdentityControlsWiringTest {
    @Test
    public void separatesAvatarAndCopyHitTargets() throws IOException {
        String source = normalizedMainActivity();

        assertTrue(source.contains(
                "root,\"个人中心\",24,12,120,106,"));
        assertTrue(source.contains(
                "root,\"复制玩家序号\",330,55,79,51,this::copyLobbyPlayerId"));
    }

    @Test
    public void copiesTheAuthenticatedNumericPlayerId() throws IOException {
        String source = normalizedMainActivity();

        assertTrue(source.contains("privateStringlobbyPublicPlayerId=\"\";"));
        assertTrue(source.contains(
                "lobbyPublicPlayerId=Long.toString(state.player().publicPlayerId());"));
        assertTrue(source.contains(
                "ClipData.newPlainText(\"玩家序号\",lobbyPublicPlayerId)"));
        assertTrue(source.contains("toast(\"复制成功!\");"));
    }

    private static String normalizedMainActivity() throws IOException {
        Path source = Path.of("src/main/java/com/huaque/ui/MainActivity.java");
        if (!Files.exists(source)) {
            source = Path.of("app").resolve(source);
        }
        return new String(Files.readAllBytes(source), StandardCharsets.UTF_8)
                .replaceAll("\\s+", "");
    }
}
