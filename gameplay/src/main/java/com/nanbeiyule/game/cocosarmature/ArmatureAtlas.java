package com.nanbeiyule.game.cocosarmature;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
    private final Map<String, int[]> frames;
    private final Map<String, Bitmap> cache = new HashMap<>();

    private ArmatureAtlas(Bitmap sheet, Map<String, int[]> frames) {
        this.sheet = sheet;
        this.frames = frames;
    }

    public static ArmatureAtlas load(
            AssetManager assets, String texturePath, String framesPath) {
        try (InputStream textureStream = assets.open(texturePath);
                InputStream framesStream = assets.open(framesPath)) {
            Bitmap sheet = BitmapFactory.decodeStream(textureStream);
            if (sheet == null) {
                throw new IllegalStateException("Unable to decode armature sheet " + texturePath);
            }
            JSONObject json = new JSONObject(readAll(framesStream));
            Map<String, int[]> frames = new HashMap<>();
            for (Iterator<String> keys = json.keys(); keys.hasNext(); ) {
                String key = keys.next();
                JSONObject frame = json.getJSONObject(key);
                frames.put(
                        ArmatureData.stripSuffix(key),
                        new int[] {
                            frame.getInt("x"),
                            frame.getInt("y"),
                            frame.getInt("width"),
                            frame.getInt("height")
                        });
            }
            return new ArmatureAtlas(sheet, frames);
        } catch (IOException | JSONException exception) {
            throw new IllegalStateException("Unable to load armature atlas " + texturePath,
                    exception);
        }
    }

    /** 显示对象位图；缺帧返回 {@code null}，由调用方跳过绘制（对应原版空 display）。 */
    public Bitmap sprite(String displayName) {
        String key = ArmatureData.stripSuffix(displayName);
        Bitmap cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        int[] frame = frames.get(key);
        if (frame == null || frame[2] <= 0 || frame[3] <= 0) {
            return null;
        }
        Bitmap sprite = Bitmap.createBitmap(sheet, frame[0], frame[1], frame[2], frame[3]);
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
        if (!sheet.isRecycled()) {
            sheet.recycle();
        }
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
