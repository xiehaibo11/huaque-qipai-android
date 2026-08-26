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

/** Loads the hash-verified recovered agreement hand Spine assets. */
final class LoginAgreementHintAssets {
    static final String ASSET_DIRECTORY = "login_effects/shoudianji";
    static final String BASE_NAME = "ShouDianJi";

    record Loaded(
            Spine37Runtime runtime,
            Map<String, Bitmap> pages) {
        Loaded {
            if (runtime == null || pages == null || pages.isEmpty()) {
                throw new IllegalArgumentException(
                        "Agreement hint assets must contain a runtime and texture pages");
            }
            pages = Map.copyOf(pages);
        }
    }

    private LoginAgreementHintAssets() {}

    static Loaded load(AssetManager assets) throws IOException {
        String atlasText =
                readUtf8(assets, ASSET_DIRECTORY + "/" + BASE_NAME + ".atlas");
        String jsonText =
                readUtf8(assets, ASSET_DIRECTORY + "/" + BASE_NAME + ".json");
        Spine37Atlas atlas = Spine37AtlasParser.parse(atlasText);
        Spine37Data data = Spine37JsonParser.parse(jsonText);
        Map<String, Bitmap> pages = new LinkedHashMap<>();
        try {
            for (Spine37Atlas.Page page : atlas.pages()) {
                String path = ASSET_DIRECTORY + "/" + page.name();
                try (InputStream input =
                        assets.open(path, AssetManager.ACCESS_STREAMING)) {
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    if (bitmap == null) {
                        throw new IOException(
                                "Unable to decode agreement hint texture " + page.name());
                    }
                    pages.put(page.name(), bitmap);
                }
            }
        } catch (IOException | RuntimeException exception) {
            recycle(pages);
            throw exception;
        }
        return new Loaded(new Spine37Runtime(data, atlas), pages);
    }

    static void recycle(Loaded assets) {
        if (assets != null) {
            recycle(assets.pages());
        }
    }

    private static void recycle(Map<String, Bitmap> pages) {
        for (Bitmap bitmap : pages.values()) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
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
