package com.nanbeiyule.game.dragonbones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 原版 DragonBones 5.5 的 {@code *_ske.json} 只读模型。
 *
 * <p>字段名沿用导出文件的短键：{@code skX/skY} 旋转角、{@code scX/scY} 缩放、
 * {@code aM} 透明度百分比、{@code tweenEasing} 缓动（0 线性、缺省为不缓动）、
 * {@code curve} 是 (0,0)→(1,1) 的三次贝塞尔控制点。
 */
public final class DragonBonesData {
    public record Transform(float x, float y, float rotation, float scaleX, float scaleY) {
        static Transform of(JSONObject json) {
            if (json == null) {
                return new Transform(0.0f, 0.0f, 0.0f, 1.0f, 1.0f);
            }
            return new Transform(
                    (float) json.optDouble("x", 0.0),
                    (float) json.optDouble("y", 0.0),
                    (float) json.optDouble("skX", 0.0),
                    (float) json.optDouble("scX", 1.0),
                    (float) json.optDouble("scY", 1.0));
        }
    }

    public record Bone(String name, String parent, Transform transform) {}

    public record Slot(String name, String parent, int displayIndex) {}

    public record Display(String texture, Transform transform) {}

    /** 一个关键帧：{@code duration} 是持续帧数，{@code tween} 为 null 表示不缓动。 */
    public record Frame(int duration, float[] values, float[] tween) {}

    public record Timeline(String bone, List<Frame> translate, List<Frame> rotate) {}

    public record SlotTimeline(String slot, List<Frame> alpha) {}

    public record Animation(
            String name,
            int duration,
            List<Timeline> bones,
            List<SlotTimeline> slots) {}

    private final float frameRate;
    private final List<Bone> bones;
    private final List<Slot> slots;
    private final Map<String, Display> displays;
    private final Animation animation;

    private DragonBonesData(
            float frameRate,
            List<Bone> bones,
            List<Slot> slots,
            Map<String, Display> displays,
            Animation animation) {
        this.frameRate = frameRate;
        this.bones = bones;
        this.slots = slots;
        this.displays = displays;
        this.animation = animation;
    }

    public static DragonBonesData parse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONObject armature = root.getJSONArray("armature").getJSONObject(0);
        float frameRate = (float) armature.optDouble("frameRate", root.optDouble("frameRate", 24.0));

        List<Bone> bones = new ArrayList<>();
        JSONArray boneArray = armature.optJSONArray("bone");
        for (int index = 0; boneArray != null && index < boneArray.length(); index++) {
            JSONObject bone = boneArray.getJSONObject(index);
            bones.add(new Bone(
                    bone.getString("name"),
                    bone.optString("parent", null),
                    Transform.of(bone.optJSONObject("transform"))));
        }

        List<Slot> slots = new ArrayList<>();
        JSONArray slotArray = armature.optJSONArray("slot");
        for (int index = 0; slotArray != null && index < slotArray.length(); index++) {
            JSONObject slot = slotArray.getJSONObject(index);
            slots.add(new Slot(
                    slot.getString("name"),
                    slot.optString("parent", null),
                    slot.optInt("displayIndex", 0)));
        }

        Map<String, Display> displays = new HashMap<>();
        JSONArray skins = armature.optJSONArray("skin");
        if (skins != null && skins.length() > 0) {
            JSONArray skinSlots = skins.getJSONObject(0).optJSONArray("slot");
            for (int index = 0; skinSlots != null && index < skinSlots.length(); index++) {
                JSONObject slot = skinSlots.getJSONObject(index);
                JSONArray display = slot.optJSONArray("display");
                if (display == null || display.length() == 0) {
                    continue;
                }
                JSONObject first = display.getJSONObject(0);
                displays.put(slot.getString("name"), new Display(
                        first.getString("name"),
                        Transform.of(first.optJSONObject("transform"))));
            }
        }

        JSONObject animation = armature.getJSONArray("animation").getJSONObject(0);
        return new DragonBonesData(
                frameRate, bones, slots, displays, parseAnimation(animation));
    }

    private static Animation parseAnimation(JSONObject json) throws JSONException {
        List<Timeline> bones = new ArrayList<>();
        JSONArray boneArray = json.optJSONArray("bone");
        for (int index = 0; boneArray != null && index < boneArray.length(); index++) {
            JSONObject timeline = boneArray.getJSONObject(index);
            bones.add(new Timeline(
                    timeline.getString("name"),
                    frames(timeline.optJSONArray("translateFrame"), "x", "y"),
                    frames(timeline.optJSONArray("rotateFrame"), "rotate", null)));
        }
        List<SlotTimeline> slots = new ArrayList<>();
        JSONArray slotArray = json.optJSONArray("slot");
        for (int index = 0; slotArray != null && index < slotArray.length(); index++) {
            JSONObject timeline = slotArray.getJSONObject(index);
            slots.add(new SlotTimeline(
                    timeline.getString("name"), alphaFrames(timeline.optJSONArray("colorFrame"))));
        }
        return new Animation(
                json.getString("name"), json.optInt("duration", 0), bones, slots);
    }

    private static List<Frame> frames(JSONArray array, String firstKey, String secondKey)
            throws JSONException {
        if (array == null) {
            return List.of();
        }
        List<Frame> frames = new ArrayList<>(array.length());
        for (int index = 0; index < array.length(); index++) {
            JSONObject frame = array.getJSONObject(index);
            float[] values = secondKey == null
                    ? new float[] {(float) frame.optDouble(firstKey, 0.0)}
                    : new float[] {
                        (float) frame.optDouble(firstKey, 0.0),
                        (float) frame.optDouble(secondKey, 0.0)
                    };
            frames.add(new Frame(frame.optInt("duration", 0), values, tween(frame)));
        }
        return frames;
    }

    private static List<Frame> alphaFrames(JSONArray array) throws JSONException {
        if (array == null) {
            return List.of();
        }
        List<Frame> frames = new ArrayList<>(array.length());
        for (int index = 0; index < array.length(); index++) {
            JSONObject frame = array.getJSONObject(index);
            JSONObject value = frame.optJSONObject("value");
            float alpha = value == null ? 100.0f : (float) value.optDouble("aM", 100.0);
            frames.add(new Frame(frame.optInt("duration", 0), new float[] {alpha}, tween(frame)));
        }
        return frames;
    }

    /** 返回 null 表示该帧不缓动（保持值）；长度 0 表示线性；长度 4 是贝塞尔控制点。 */
    private static float[] tween(JSONObject frame) throws JSONException {
        JSONArray curve = frame.optJSONArray("curve");
        if (curve != null) {
            float[] control = new float[curve.length()];
            for (int index = 0; index < control.length; index++) {
                control[index] = (float) curve.getDouble(index);
            }
            return control;
        }
        return frame.has("tweenEasing") ? new float[0] : null;
    }

    public float frameRate() {
        return frameRate;
    }

    public List<Bone> bones() {
        return bones;
    }

    public List<Slot> slots() {
        return slots;
    }

    public Display display(String slot) {
        return displays.get(slot);
    }

    public Animation animation() {
        return animation;
    }
}
