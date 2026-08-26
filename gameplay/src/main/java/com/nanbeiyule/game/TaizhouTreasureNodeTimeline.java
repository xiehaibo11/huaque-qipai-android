package com.nanbeiyule.game;

import android.content.res.Resources;
import com.nanbeiyule.game.spine37.Spine37Data;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Samples the original CompSpineAction bone-scale and slot-alpha node timeline. */
final class TaizhouTreasureNodeTimeline {
    static final String MAIN_ASSET =
            "taizhou_treasure_effects/zzb_jbp_zjm/zzb_jbp_zjm_animation.json";

    private final Map<String, List<VectorFrame>> scales;
    private final Map<String, List<ScalarFrame>> alphas;

    private TaizhouTreasureNodeTimeline(
            Map<String, List<VectorFrame>> scales,
            Map<String, List<ScalarFrame>> alphas) {
        this.scales = Map.copyOf(scales);
        this.alphas = Map.copyOf(alphas);
    }

    static TaizhouTreasureNodeTimeline loadMain(Resources resources) {
        return load(resources, MAIN_ASSET, "cx");
    }

    static TaizhouTreasureNodeTimeline load(
            Resources resources, String assetPath, String animationName) {
        try (InputStream stream = resources.getAssets().open(assetPath)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) output.write(buffer, 0, read);
            return parse(output.toString(StandardCharsets.UTF_8.name()), animationName);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load treasure node timeline", exception);
        }
    }

    static TaizhouTreasureNodeTimeline parse(String json, String animationName) {
        try {
            JSONObject animation =
                    new JSONObject(json)
                            .getJSONObject("animations")
                            .getJSONObject(animationName);
            return new TaizhouTreasureNodeTimeline(
                    parseScales(animation.optJSONObject("bones")),
                    parseAlphas(animation.optJSONObject("slots")));
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid treasure node timeline", exception);
        }
    }

    NodePose pose(String nodeName, float seconds) {
        return pose(nodeName, nodeName, seconds);
    }

    NodePose pose(String boneName, String slotName, float seconds) {
        float[] scale = sampleVector(scales.get(boneName), seconds, 1.0f, 1.0f);
        float alpha = sampleScalar(alphas.get(slotName), seconds, 1.0f);
        return new NodePose(scale[0], scale[1], alpha);
    }

    private static Map<String, List<VectorFrame>> parseScales(JSONObject bones)
            throws JSONException {
        Map<String, List<VectorFrame>> result = new LinkedHashMap<>();
        if (bones == null) return result;
        Iterator<String> names = bones.keys();
        while (names.hasNext()) {
            String name = names.next();
            JSONArray frames = bones.getJSONObject(name).optJSONArray("scale");
            if (frames == null) continue;
            List<VectorFrame> values = new ArrayList<>();
            for (int index = 0; index < frames.length(); index++) {
                JSONObject frame = frames.getJSONObject(index);
                values.add(
                        new VectorFrame(
                                number(frame, "time", 0.0f),
                                number(frame, "x", 1.0f),
                                number(frame, "y", 1.0f),
                                curve(frame)));
            }
            result.put(name, List.copyOf(values));
        }
        return result;
    }

    private static Map<String, List<ScalarFrame>> parseAlphas(JSONObject slots)
            throws JSONException {
        Map<String, List<ScalarFrame>> result = new LinkedHashMap<>();
        if (slots == null) return result;
        Iterator<String> names = slots.keys();
        while (names.hasNext()) {
            String name = names.next();
            JSONArray frames = slots.getJSONObject(name).optJSONArray("color");
            if (frames == null) continue;
            List<ScalarFrame> values = new ArrayList<>();
            for (int index = 0; index < frames.length(); index++) {
                JSONObject frame = frames.getJSONObject(index);
                values.add(
                        new ScalarFrame(
                                number(frame, "time", 0.0f),
                                alpha(frame.getString("color")),
                                curve(frame)));
            }
            result.put(name, List.copyOf(values));
        }
        return result;
    }

    private static float[] sampleVector(
            List<VectorFrame> frames, float time, float defaultX, float defaultY) {
        if (frames == null || frames.isEmpty() || time < frames.get(0).time()) {
            return new float[] {defaultX, defaultY};
        }
        int previousIndex = previousVector(frames, time);
        VectorFrame previous = frames.get(previousIndex);
        if (previousIndex == frames.size() - 1) return new float[] {previous.x(), previous.y()};
        VectorFrame next = frames.get(previousIndex + 1);
        float progress = progress(previous.time(), next.time(), time, previous.curve());
        return new float[] {
            lerp(previous.x(), next.x(), progress),
            lerp(previous.y(), next.y(), progress)
        };
    }

    private static float sampleScalar(List<ScalarFrame> frames, float time, float defaultValue) {
        if (frames == null || frames.isEmpty() || time < frames.get(0).time()) return defaultValue;
        int previousIndex = previousScalar(frames, time);
        ScalarFrame previous = frames.get(previousIndex);
        if (previousIndex == frames.size() - 1) return previous.value();
        ScalarFrame next = frames.get(previousIndex + 1);
        return lerp(
                previous.value(),
                next.value(),
                progress(previous.time(), next.time(), time, previous.curve()));
    }

    private static float progress(
            float previousTime, float nextTime, float time, Spine37Data.Curve curve) {
        if (nextTime <= previousTime) return 1.0f;
        return curve.apply((time - previousTime) / (nextTime - previousTime));
    }

    private static int previousVector(List<VectorFrame> frames, float time) {
        int index = 0;
        while (index + 1 < frames.size() && frames.get(index + 1).time() <= time) index++;
        return index;
    }

    private static int previousScalar(List<ScalarFrame> frames, float time) {
        int index = 0;
        while (index + 1 < frames.size() && frames.get(index + 1).time() <= time) index++;
        return index;
    }

    private static Spine37Data.Curve curve(JSONObject frame) throws JSONException {
        Object value = frame.opt("curve");
        if (value == null) return Spine37Data.Curve.LINEAR;
        if (value instanceof String) {
            if ("stepped".equals(value)) return Spine37Data.Curve.STEPPED;
            throw new JSONException("Unsupported curve " + value);
        }
        if (value instanceof JSONArray) {
            JSONArray values = (JSONArray) value;
            if (values.length() != 4) throw new JSONException("Bezier curve needs four values");
            return Spine37Data.Curve.bezier(
                    (float) values.getDouble(0),
                    (float) values.getDouble(1),
                    (float) values.getDouble(2),
                    (float) values.getDouble(3));
        }
        throw new JSONException("Unsupported curve value");
    }

    private static float number(JSONObject body, String name, float fallback) {
        return body.has(name) ? (float) body.optDouble(name, fallback) : fallback;
    }

    private static float alpha(String rgba) throws JSONException {
        if (rgba == null || rgba.length() != 8) throw new JSONException("Expected RRGGBBAA color");
        try {
            return Integer.parseInt(rgba.substring(6, 8), 16) / 255.0f;
        } catch (NumberFormatException exception) {
            throw new JSONException("Invalid RRGGBBAA color");
        }
    }

    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    record NodePose(float scaleX, float scaleY, float alpha) {}

    private record VectorFrame(float time, float x, float y, Spine37Data.Curve curve) {}

    private record ScalarFrame(float time, float value, Spine37Data.Curve curve) {}
}
