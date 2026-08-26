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

final class OriginalLobbyTapEffectAssets {
    record RegionTexture(Bitmap bitmap, boolean rotated) {}

    record Variant(
            OriginalLobbyTapEffectSpec spec,
            Spine37Runtime runtime,
            float durationSeconds,
            Map<String, RegionTexture> textures) {
        Variant {
            textures = Map.copyOf(textures);
        }

        void recycle() {
            for (RegionTexture texture : textures.values()) {
                if (!texture.bitmap().isRecycled()) {
                    texture.bitmap().recycle();
                }
            }
        }
    }

    private OriginalLobbyTapEffectAssets() {}

    static Variant load(AssetManager assets, OriginalLobbyTapEffectSpec spec)
            throws IOException {
        String directory = spec.assetDirectory();
        Spine37Atlas atlas = Spine37AtlasParser.parse(
                readUtf8(assets, directory + "/" + spec.baseName() + ".atlas"));
        Spine37Data data = Spine37JsonParser.parse(
                readUtf8(assets, directory + "/" + spec.baseName() + ".json"));
        Spine37Runtime runtime = new Spine37Runtime(data, atlas);
        Map<String, Bitmap> pages = loadPages(assets, directory, atlas);
        Map<String, RegionTexture> textures = new LinkedHashMap<>();
        try {
            for (Spine37Data.Attachment attachment : data.attachments().values()) {
                if (textures.containsKey(attachment.name())) {
                    continue;
                }
                Spine37Atlas.Region region = atlas.regions().get(attachment.path());
                Bitmap page = region == null ? null : pages.get(region.pageName());
                if (region == null || page == null) {
                    throw new IOException("Missing tap-effect atlas region " + attachment.path());
                }
                Bitmap packed = Bitmap.createBitmap(
                        page,
                        region.x(),
                        region.y(),
                        region.packedWidth(),
                        region.packedHeight());
                Bitmap owned = packed.copy(Bitmap.Config.ARGB_8888, false);
                if (packed != page) {
                    packed.recycle();
                }
                textures.put(
                        attachment.name(),
                        new RegionTexture(owned, region.rotated()));
            }
            return new Variant(
                    spec,
                    runtime,
                    runtime.animationDuration(spec.animationName()),
                    textures);
        } catch (IOException | RuntimeException exception) {
            recycleTextures(textures);
            throw exception;
        } finally {
            for (Bitmap page : pages.values()) {
                if (!page.isRecycled()) {
                    page.recycle();
                }
            }
        }
    }

    private static Map<String, Bitmap> loadPages(
            AssetManager assets,
            String directory,
            Spine37Atlas atlas) throws IOException {
        Map<String, Bitmap> pages = new LinkedHashMap<>();
        try {
            for (Spine37Atlas.Page page : atlas.pages()) {
                try (InputStream input = assets.open(
                        directory + "/" + page.name(),
                        AssetManager.ACCESS_STREAMING)) {
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    if (bitmap == null) {
                        throw new IOException("Unable to decode " + page.name());
                    }
                    pages.put(page.name(), bitmap);
                }
            }
            return pages;
        } catch (IOException | RuntimeException exception) {
            for (Bitmap page : pages.values()) {
                page.recycle();
            }
            throw exception;
        }
    }

    private static void recycleTextures(Map<String, RegionTexture> textures) {
        for (RegionTexture texture : textures.values()) {
            texture.bitmap().recycle();
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
