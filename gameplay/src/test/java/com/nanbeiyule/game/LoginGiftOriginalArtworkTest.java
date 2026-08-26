package com.nanbeiyule.game;

import static org.junit.Assert.assertTrue;

import com.nanbeiyule.game.spine37.Spine37AtlasParser;
import com.nanbeiyule.game.spine37.Spine37JsonParser;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class LoginGiftOriginalArtworkTest {
    @Test
    public void packagedOriginalLoginGiftSpineRendersTheDailySignShell() throws IOException {
        Path atlas = asset("zzb_hdzx_dlyl.atlas");
        Path json = asset("zzb_hdzx_dlyl.json");
        Path texture = asset("zzb_hdzx_dlyl.png");
        Path dayCards = drawable("login_gift_day_sign_atlas.png");

        assertTrue(Files.isRegularFile(atlas));
        assertTrue(Files.isRegularFile(json));
        assertTrue(Files.isRegularFile(texture));
        assertTrue(Files.isRegularFile(dayCards));

        Spine37Runtime runtime =
                new Spine37Runtime(
                        Spine37JsonParser.parse(readUtf8(json)),
                        Spine37AtlasParser.parse(readUtf8(atlas)));
        Set<String> attachments =
                runtime.sample("loop", 0.5f).stream()
                        .map(Spine37Runtime.DrawCommand::attachmentName)
                        .collect(Collectors.toSet());

        assertTrue(attachments.containsAll(Set.of("di", "zi", "di2", "di3", "di4", "di5", "d77")));
    }

    private static Path asset(String name) {
        Path fromRoot = Path.of("gameplay", "src", "main", "assets", "login_gift", name);
        if (Files.isRegularFile(fromRoot)) return fromRoot;
        return Path.of("src", "main", "assets", "login_gift", name);
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static Path drawable(String name) {
        Path fromRoot = Path.of("gameplay", "src", "main", "res", "drawable-nodpi", name);
        if (Files.isRegularFile(fromRoot)) return fromRoot;
        return Path.of("src", "main", "res", "drawable-nodpi", name);
    }
}
