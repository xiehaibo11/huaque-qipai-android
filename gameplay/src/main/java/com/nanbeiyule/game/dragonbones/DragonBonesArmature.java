package com.nanbeiyule.game.dragonbones;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 一套 DragonBones 骨骼 + 图集，按时间求值后画进给定矩形。
 *
 * <p>原版是 {@code display.playDargonBonesAnimByTimes(params, 0)}，循环播 {@code newAnimation}。
 * 这里只实现该动画用到的能力：骨骼平移/旋转、插槽透明度、线性与三次贝塞尔缓动。
 */
public final class DragonBonesArmature {
    private final DragonBonesData data;
    private final Bitmap sheet;
    private final Map<String, Rect> textures;
    private final Map<String, DragonBonesData.Bone> boneByName = new HashMap<>();
    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    private final Matrix matrix = new Matrix();

    /** 骨架自身的设计尺寸，取 {@code aabb}，用于把动画缩放到目标矩形。 */
    private final float width;
    private final float height;
    private final float originX;
    private final float originY;

    private DragonBonesArmature(
            DragonBonesData data,
            Bitmap sheet,
            Map<String, Rect> textures,
            float width,
            float height,
            float originX,
            float originY) {
        this.data = data;
        this.sheet = sheet;
        this.textures = textures;
        this.width = width;
        this.height = height;
        this.originX = originX;
        this.originY = originY;
        for (DragonBonesData.Bone bone : data.bones()) {
            boneByName.put(bone.name(), bone);
        }
    }

    public static DragonBonesArmature load(AssetManager assets, String directory, String name) {
        try {
            String skeleton = read(assets, directory + "/" + name + "_ske.json");
            DragonBonesData data = DragonBonesData.parse(skeleton);
            JSONObject aabb = new JSONObject(skeleton)
                    .getJSONArray("armature")
                    .getJSONObject(0)
                    .getJSONObject("aabb");
            JSONObject atlas = new JSONObject(read(assets, directory + "/" + name + "_tex.json"));
            Map<String, Rect> textures = new HashMap<>();
            JSONArray sub = atlas.getJSONArray("SubTexture");
            for (int index = 0; index < sub.length(); index++) {
                JSONObject frame = sub.getJSONObject(index);
                int x = frame.getInt("x");
                int y = frame.getInt("y");
                textures.put(
                        frame.getString("name"),
                        new Rect(x, y, x + frame.getInt("width"), y + frame.getInt("height")));
            }
            Bitmap sheet;
            try (InputStream stream = assets.open(directory + "/" + name + "_tex.png")) {
                sheet = BitmapFactory.decodeStream(stream);
            }
            return new DragonBonesArmature(
                    data,
                    sheet,
                    textures,
                    (float) aabb.getDouble("width"),
                    (float) aabb.getDouble("height"),
                    (float) -aabb.getDouble("x"),
                    (float) -aabb.getDouble("y"));
        } catch (IOException | JSONException error) {
            throw new IllegalStateException("unable to load armature " + name, error);
        }
    }

    /** 骨架在 {@code destination} 内等比铺满地绘制，{@code seconds} 是循环内的播放时间。 */
    public void draw(Canvas canvas, android.graphics.RectF destination, float seconds) {
        float scaleX = destination.width() / width;
        float scaleY = destination.height() / height;
        int save = canvas.save();
        canvas.translate(destination.left, destination.top);
        canvas.scale(scaleX, scaleY);
        float frame = frameOf(seconds);
        for (DragonBonesData.Slot slot : data.slots()) {
            if (slot.displayIndex() < 0) {
                continue;
            }
            DragonBonesData.Display display = data.display(slot.name());
            if (display == null) {
                continue;
            }
            Rect source = textures.get(display.texture());
            if (source == null) {
                continue;
            }
            float alpha = alpha(slot.name(), frame);
            if (alpha <= 0.0f) {
                continue;
            }
            matrix.reset();
            matrix.postTranslate(-source.width() / 2.0f, -source.height() / 2.0f);
            apply(matrix, display.transform(), 0.0f, 0.0f, 0.0f);
            applyBoneChain(matrix, slot.parent(), frame);
            matrix.postTranslate(originX, originY);
            paint.setAlpha(Math.round(alpha * 255.0f));
            canvas.drawBitmap(bitmap(display.texture(), source), matrix, paint);
        }
        canvas.restoreToCount(save);
    }

    private void applyBoneChain(Matrix target, String boneName, float frame) {
        DragonBonesData.Bone bone = boneByName.get(boneName);
        while (bone != null) {
            float[] translate = value(bone.name(), true, frame, 2);
            float[] rotate = value(bone.name(), false, frame, 1);
            apply(target, bone.transform(), translate[0], translate[1], rotate[0]);
            bone = bone.parent() == null ? null : boneByName.get(bone.parent());
        }
    }

    private static void apply(
            Matrix target,
            DragonBonesData.Transform transform,
            float offsetX,
            float offsetY,
            float offsetRotation) {
        target.postScale(transform.scaleX(), transform.scaleY());
        target.postRotate(transform.rotation() + offsetRotation);
        target.postTranslate(transform.x() + offsetX, transform.y() + offsetY);
    }

    private float alpha(String slot, float frame) {
        for (DragonBonesData.SlotTimeline timeline : data.animation().slots()) {
            if (timeline.slot().equals(slot) && !timeline.alpha().isEmpty()) {
                return sample(timeline.alpha(), frame, 1)[0] / 100.0f;
            }
        }
        return 1.0f;
    }

    private float[] value(String bone, boolean translate, float frame, int size) {
        for (DragonBonesData.Timeline timeline : data.animation().bones()) {
            if (timeline.bone().equals(bone)) {
                return sample(translate ? timeline.translate() : timeline.rotate(), frame, size);
            }
        }
        return new float[size];
    }

    private float[] sample(List<DragonBonesData.Frame> frames, float frame, int size) {
        if (frames.isEmpty()) {
            return new float[size];
        }
        float cursor = 0.0f;
        for (int index = 0; index < frames.size(); index++) {
            DragonBonesData.Frame current = frames.get(index);
            float end = cursor + current.duration();
            if (frame < end || index == frames.size() - 1) {
                if (current.tween() == null || current.duration() <= 0) {
                    return current.values();
                }
                DragonBonesData.Frame next = frames.get((index + 1) % frames.size());
                float progress = ease(current.tween(), (frame - cursor) / current.duration());
                float[] result = new float[size];
                for (int slot = 0; slot < size; slot++) {
                    float from = slot < current.values().length ? current.values()[slot] : 0.0f;
                    float to = slot < next.values().length ? next.values()[slot] : 0.0f;
                    result[slot] = from + (to - from) * progress;
                }
                return result;
            }
            cursor = end;
        }
        return frames.get(frames.size() - 1).values();
    }

    /** {@code tween} 长度 0 是线性，长度 4 是 (0,0)→(1,1) 的三次贝塞尔。 */
    private static float ease(float[] tween, float progress) {
        float clamped = Math.max(0.0f, Math.min(1.0f, progress));
        if (tween.length < 4) {
            return clamped;
        }
        float low = 0.0f;
        float high = 1.0f;
        float parameter = clamped;
        for (int step = 0; step < 12; step++) {
            float x = bezier(tween[0], tween[2], parameter);
            if (Math.abs(x - clamped) < 0.0005f) {
                break;
            }
            if (x < clamped) {
                low = parameter;
            } else {
                high = parameter;
            }
            parameter = (low + high) / 2.0f;
        }
        return bezier(tween[1], tween[3], parameter);
    }

    private static float bezier(float control1, float control2, float t) {
        float inverse = 1.0f - t;
        return 3.0f * inverse * inverse * t * control1
                + 3.0f * inverse * t * t * control2
                + t * t * t;
    }

    private float frameOf(float seconds) {
        int duration = Math.max(1, data.animation().duration());
        float frames = seconds * data.frameRate();
        return frames % duration;
    }

    /** 动画一个循环的秒数。 */
    public float durationSeconds() {
        return data.animation().duration() / data.frameRate();
    }

    private final Map<String, Bitmap> cache = new HashMap<>();

    private Bitmap bitmap(String name, Rect source) {
        Bitmap cached = cache.get(name);
        if (cached == null) {
            cached = Bitmap.createBitmap(
                    sheet, source.left, source.top, source.width(), source.height());
            cache.put(name, cached);
        }
        return cached;
    }

    private static String read(AssetManager assets, String path) throws IOException {
        try (InputStream stream = assets.open(path)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = stream.read(chunk)) > 0) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toString(StandardCharsets.UTF_8.name());
        }
    }
}
