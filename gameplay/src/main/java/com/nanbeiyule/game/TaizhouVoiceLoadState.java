package com.nanbeiyule.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStream;

final class TaizhouVoiceLoadState {
    private static final String PREFERENCES_NAME = "taizhou_voice_load_state";

    interface Store {
        String getString(String key);

        void putString(String key, String value);
    }

    interface ResourceDirectory {
        boolean hasRawResource(String resourceName);

        boolean hasAsset(String assetPath);
    }

    private final Store store;

    TaizhouVoiceLoadState(Store store) {
        this.store = store;
    }

    static TaizhouVoiceLoadState create(Context context) {
        SharedPreferences preferences =
                context.getApplicationContext()
                        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return new TaizhouVoiceLoadState(new SharedPreferencesStore(preferences));
    }

    boolean shouldShowProgress(
            TaizhouMahjongVoiceCatalog.VoicePackage voicePackage,
            ResourceDirectory directory) {
        return !isLoaded(voicePackage, directory);
    }

    void markLoaded(
            TaizhouMahjongVoiceCatalog.VoicePackage voicePackage,
            ResourceDirectory directory) {
        if (directoryComplete(voicePackage, directory)) {
            store.putString(cacheKey(voicePackage), voicePackage.cacheToken());
        }
    }

    private boolean isLoaded(
            TaizhouMahjongVoiceCatalog.VoicePackage voicePackage,
            ResourceDirectory directory) {
        return directoryComplete(voicePackage, directory)
                && voicePackage.cacheToken().equals(store.getString(cacheKey(voicePackage)));
    }

    private static boolean directoryComplete(
            TaizhouMahjongVoiceCatalog.VoicePackage voicePackage,
            ResourceDirectory directory) {
        if (voicePackage == null || directory == null) {
            return false;
        }
        for (String resourceName : voicePackage.rawResourceNames()) {
            if (!directory.hasRawResource(resourceName)) {
                return false;
            }
        }
        for (String assetPath : voicePackage.assetPaths()) {
            if (!directory.hasAsset(assetPath)) {
                return false;
            }
        }
        return true;
    }

    private static String cacheKey(TaizhouMahjongVoiceCatalog.VoicePackage voicePackage) {
        return "loaded_game_sound_" + voicePackage.gameId();
    }

    static final class AndroidResourceDirectory implements ResourceDirectory {
        private final TaizhouMahjongSoundPlayer soundPlayer;
        private final AssetManager assets;

        AndroidResourceDirectory(TaizhouMahjongSoundPlayer soundPlayer, AssetManager assets) {
            this.soundPlayer = soundPlayer;
            this.assets = assets;
        }

        @Override
        public boolean hasRawResource(String resourceName) {
            return soundPlayer.hasRawResource(resourceName);
        }

        @Override
        public boolean hasAsset(String assetPath) {
            try (InputStream ignored = assets.open(assetPath)) {
                return true;
            } catch (IOException exception) {
                return false;
            }
        }
    }

    private static final class SharedPreferencesStore implements Store {
        private final SharedPreferences preferences;

        private SharedPreferencesStore(SharedPreferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public String getString(String key) {
            return preferences.getString(key, "");
        }

        @Override
        public void putString(String key, String value) {
            preferences.edit().putString(key, value).apply();
        }
    }
}
