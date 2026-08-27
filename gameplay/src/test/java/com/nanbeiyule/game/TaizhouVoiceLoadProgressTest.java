package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public final class TaizhouVoiceLoadProgressTest {
    @Test
    public void firstLoadCatalogCoversTableVoiceAndQuickPhrases() {
        List<String> resources = TaizhouMahjongVoiceCatalog.firstLoadResourceNames();

        assertTrue(resources.contains("taizhou_mahjong_sound_out"));
        assertTrue(resources.contains("taizhou_mahjong_voice_dialect_man_17"));
        assertTrue(resources.contains("taizhou_mahjong_voice_dialect_women_83"));
        assertTrue(resources.contains("taizhou_mahjong_action_dialect_man_1"));
        assertTrue(resources.contains("taizhou_mahjong_action_dialect_women_hu_2"));
        assertTrue(
                TaizhouMahjongVoiceCatalog.firstLoadPackage()
                        .assetPaths()
                        .contains("audio/Speak/30109/dialect/Man/M_Speak1.mp3"));
    }

    @Test
    public void bundledVoicePackageContainsFirstLoadDirectories() {
        TaizhouMahjongVoiceCatalog.VoicePackage voicePackage =
                TaizhouMahjongVoiceCatalog.firstLoadPackage();
        for (String resourceName : voicePackage.rawResourceNames()) {
            assertTrue(
                    resourceName,
                    Files.isRegularFile(Path.of("src/main/res/raw", resourceName + ".mp3")));
        }
        for (String assetPath : voicePackage.assetPaths()) {
            assertTrue(assetPath, Files.isRegularFile(Path.of("src/main/assets", assetPath)));
        }
    }

    @Test
    public void progressPercentAndVisibilityFollowLoadState() {
        TaizhouVoiceLoadProgress progress = new TaizhouVoiceLoadProgress();

        progress.start();
        progress.onProgress(1, 4);
        assertEquals(25, progress.percent());
        assertTrue(progress.visible());

        progress.onProgress(4, 4);
        assertEquals(100, progress.percent());
        assertFalse(progress.visible());
    }

    @Test
    public void completedVoicePackageMarkSuppressesNextSameTableLoadProgress() {
        MemoryVoiceLoadStore store = new MemoryVoiceLoadStore();
        TaizhouVoiceLoadState state = new TaizhouVoiceLoadState(store);
        TaizhouMahjongVoiceCatalog.VoicePackage voicePackage = testVoicePackage("1.0.0.2");
        TestVoiceDirectory directory = TestVoiceDirectory.containing(voicePackage);

        assertTrue(state.shouldShowProgress(voicePackage, directory));

        state.markLoaded(voicePackage, directory);

        assertFalse(state.shouldShowProgress(voicePackage, directory));
        assertTrue(state.shouldShowProgress(testVoicePackage("1.0.0.3"), directory));
    }

    @Test
    public void loadedMarkDoesNotHideProgressWhenVoiceDirectoryIsIncomplete() {
        MemoryVoiceLoadStore store = new MemoryVoiceLoadStore();
        TaizhouVoiceLoadState state = new TaizhouVoiceLoadState(store);
        TaizhouMahjongVoiceCatalog.VoicePackage voicePackage = testVoicePackage("1.0.0.2");

        state.markLoaded(voicePackage, TestVoiceDirectory.containing(voicePackage));

        assertTrue(
                state.shouldShowProgress(
                        voicePackage,
                        new TestVoiceDirectory(
                                Set.of("taizhou_mahjong_sound_out"), Set.of())));
    }

    private static TaizhouMahjongVoiceCatalog.VoicePackage testVoicePackage(String version) {
        return new TaizhouMahjongVoiceCatalog.VoicePackage(
                30109,
                version,
                List.of("taizhou_mahjong_sound_out"),
                List.of("audio/Speak/30109/dialect/Man/M_Speak1.mp3"));
    }

    private static final class MemoryVoiceLoadStore implements TaizhouVoiceLoadState.Store {
        private final Map<String, String> values = new HashMap<>();

        @Override
        public String getString(String key) {
            return values.get(key);
        }

        @Override
        public void putString(String key, String value) {
            values.put(key, value);
        }
    }

    private static final class TestVoiceDirectory implements TaizhouVoiceLoadState.ResourceDirectory {
        private final Set<String> rawResources;
        private final Set<String> assetPaths;

        private TestVoiceDirectory(Set<String> rawResources, Set<String> assetPaths) {
            this.rawResources = rawResources;
            this.assetPaths = assetPaths;
        }

        static TestVoiceDirectory containing(TaizhouMahjongVoiceCatalog.VoicePackage voicePackage) {
            return new TestVoiceDirectory(
                    new HashSet<>(voicePackage.rawResourceNames()),
                    new HashSet<>(voicePackage.assetPaths()));
        }

        @Override
        public boolean hasRawResource(String resourceName) {
            return rawResources.contains(resourceName);
        }

        @Override
        public boolean hasAsset(String assetPath) {
            return assetPaths.contains(assetPath);
        }
    }
}
