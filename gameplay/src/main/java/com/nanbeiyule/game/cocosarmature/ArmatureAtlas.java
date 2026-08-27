package com.nanbeiyule.game.cocosarmature;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 原版 Armature 图集（{@code *0.png} + {@code *0.plist}）。
 *
 * <p>plist 由 {@code tools/export_taizhou_ingame_assets.py} 原样转成 JSON，键为显示对象名，
 * 值是 {@code x/y/width/height/offsetX/offsetY/originalWidth/originalHeight} 的矩形，
 * 与 Cocos 的 {@code CCSpriteFrame} 字段同名同义。
 */
public final class ArmatureAtlas {
    private final Bitmap sheet;
    private final Map<String, Frame> frames;
    private final Map<String, Bitmap> cache = new HashMap<>();

    private ArmatureAtlas(Bitmap sheet, Map<String, Frame> frames) {
        this.sheet = sheet;
        this.frames = frames;
    }

    private record Frame(
            Bitmap sheet,
            int x,
            int y,
            int width,
            int height,
            int originalWidth,
            int originalHeight,
            float offsetX,
            float offsetY) {}

    public static ArmatureAtlas load(
            AssetManager assets, String texturePath, String framesPath) {
        try (InputStream textureStream = assets.open(texturePath);
                InputStream framesStream = assets.open(framesPath)) {
            Bitmap sheet = BitmapFactory.decodeStream(textureStream);
            if (sheet == null) {
                throw new IllegalStateException("Unable to decode armature sheet " + texturePath);
            }
            JSONObject json = new JSONObject(readAll(framesStream));
            Map<String, Frame> frames = readFrames(sheet, json);
            return new ArmatureAtlas(sheet, frames);
        } catch (IOException | JSONException exception) {
            throw new IllegalStateException("Unable to load armature atlas " + texturePath,
                    exception);
        }
    }

    public static ArmatureAtlas load(
            AssetManager assets, String[] texturePaths, String[] framesPaths) {
        if (texturePaths.length == 0 || texturePaths.length != framesPaths.length) {
            throw new IllegalArgumentException("armature atlas lists must match");
        }
        try {
            Bitmap firstSheet = null;
            Map<String, Frame> frames = new HashMap<>();
            for (int index = 0; index < texturePaths.length; index++) {
                try (InputStream textureStream = assets.open(texturePaths[index]);
                        InputStream framesStream = assets.open(framesPaths[index])) {
                    Bitmap sheet = BitmapFactory.decodeStream(textureStream);
                    if (sheet == null) {
                        throw new IllegalStateException(
                                "Unable to decode armature sheet " + texturePaths[index]);
                    }
                    if (firstSheet == null) {
                        firstSheet = sheet;
                    }
                    frames.putAll(readFrames(sheet, new JSONObject(readAll(framesStream))));
                }
            }
            return new ArmatureAtlas(firstSheet, frames);
        } catch (IOException | JSONException exception) {
            throw new IllegalStateException("Unable to load armature atlases", exception);
        }
    }

    /** 显示对象位图；缺帧返回 {@code null}，由调用方跳过绘制（对应原版空 display）。 */
    public Bitmap sprite(String displayName) {
        String key = ArmatureData.stripSuffix(displayName);
        Bitmap cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Frame frame = frames.get(key);
        if (frame == null || frame.width() <= 0 || frame.height() <= 0) {
            return null;
        }
        Bitmap stored =
                Bitmap.createBitmap(
                        frame.sheet(), frame.x(), frame.y(), frame.width(), frame.height());
        Bitmap sprite = restore(stored, frame);
        cache.put(key, sprite);
        return sprite;
    }

    public void recycle() {
        for (Bitmap sprite : cache.values()) {
            if (sprite != null && !sprite.isRecycled() && sprite != sheet) {
                sprite.recycle();
            }
        }
        cache.clear();
        Set<Bitmap> recycled = new HashSet<>();
        for (Frame frame : frames.values()) {
            if (frame.sheet() != null && recycled.add(frame.sheet()) && !frame.sheet().isRecycled()) {
                frame.sheet().recycle();
            }
        }
    }

    private static Map<String, Frame> readFrames(Bitmap sheet, JSONObject json)
            throws JSONException {
        Map<String, Frame> frames = new HashMap<>();
        for (Iterator<String> keys = json.keys(); keys.hasNext(); ) {
            String key = keys.next();
            JSONObject frame = json.getJSONObject(key);
            frames.put(
                    ArmatureData.stripSuffix(key),
                    new Frame(
                            sheet,
                            frame.getInt("x"),
                            frame.getInt("y"),
                            frame.getInt("width"),
                            frame.getInt("height"),
                            frame.optInt("originalWidth", frame.getInt("width")),
                            frame.optInt("originalHeight", frame.getInt("height")),
                            (float) frame.optDouble("offsetX", 0.0),
                            (float) frame.optDouble("offsetY", 0.0)));
        }
        return frames;
    }

    private static Bitmap restore(Bitmap stored, Frame frame) {
        if (frame.originalWidth() == frame.width()
                && frame.originalHeight() == frame.height()
                && frame.offsetX() == 0.0f
                && frame.offsetY() == 0.0f) {
            return stored;
        }
        Bitmap restored =
                Bitmap.createBitmap(
                        frame.originalWidth(), frame.originalHeight(), Bitmap.Config.ARGB_8888);
        float left = (frame.originalWidth() - frame.width()) * 0.5f + frame.offsetX();
        float top = (frame.originalHeight() - frame.height()) * 0.5f - frame.offsetY();
        new Canvas(restored).drawBitmap(stored, Math.round(left), Math.round(top), null);
        stored.recycle();
        return restored;
    }

    private static String readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }
}
