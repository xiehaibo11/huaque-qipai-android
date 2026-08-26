package com.nanbeiyule.game;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.nanbeiyule.game.spine37.Spine37Atlas;
import com.nanbeiyule.game.spine37.Spine37AtlasParser;
import com.nanbeiyule.game.spine37.Spine37Data;
import com.nanbeiyule.game.spine37.Spine37JsonParser;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

final class OriginalLobbyEffectAssets {
    record Loaded(
            Spine37Runtime runtime,
            Map<String, Bitmap> pages) {
        Loaded {
            pages = Map.copyOf(pages);
        }
    }

    private OriginalLobbyEffectAssets() {}

    static Loaded load(AssetManager assets, OriginalLobbyEffectSpec spec)
            throws IOException {
        return load(assets, spec.assetDirectory(), spec.baseName());
    }

    /** 大厅特效与大厅人物骨架共用同一套 Spine 资源装载语义。 */
    static Loaded load(AssetManager assets, String directory, String baseName)
            throws IOException {
        String atlasText = readUtf8(assets, directory + "/" + baseName + ".atlas");
        String jsonText = readUtf8(assets, directory + "/" + baseName + ".json");
        Spine37Atlas atlas = Spine37AtlasParser.parse(atlasText);
        Spine37Data data = Spine37JsonParser.parse(jsonText);
        Map<String, Bitmap> pages = new LinkedHashMap<>();
        try {
            for (Spine37Atlas.Page page : atlas.pages()) {
                String path = directory + "/" + page.name();
                try (InputStream input = assets.open(path, AssetManager.ACCESS_STREAMING)) {
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    if (bitmap == null) {
                        throw new IOException("Unable to decode effect texture " + page.name());
                    }
                    pages.put(page.name(), bitmap);
                }
            }
        } catch (IOException | RuntimeException exception) {
            for (Bitmap bitmap : pages.values()) {
                bitmap.recycle();
            }
            throw exception;
        }
        return new Loaded(new Spine37Runtime(data, atlas), pages);
    }

    private static String readUtf8(AssetManager assets, String path) throws IOException {
        try (InputStream input = assets.open(path, AssetManager.ACCESS_STREAMING);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }
}
