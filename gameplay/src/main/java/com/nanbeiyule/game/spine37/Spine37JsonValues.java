package com.nanbeiyule.game.spine37;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class Spine37JsonValues {
    private Spine37JsonValues() {}

    static Spine37Data.ColorValue parseColor(String rgba) {
        if (rgba == null || rgba.length() != 8) {
            throw new IllegalArgumentException("Spine color must contain 8 hexadecimal digits");
        }
        long value = Long.parseLong(rgba, 16);
        return new Spine37Data.ColorValue(
                ((value >> 24) & 0xff) / 255.0f,
                ((value >> 16) & 0xff) / 255.0f,
                ((value >> 8) & 0xff) / 255.0f,
                (value & 0xff) / 255.0f);
    }

    static Spine37Data.Curve curve(JSONObject frame) throws JSONException {
        if (!frame.has("curve")) {
            return Spine37Data.Curve.LINEAR;
        }
        Object curve = frame.get("curve");
        if ("stepped".equals(curve)) {
            return Spine37Data.Curve.STEPPED;
        }
        if (curve instanceof JSONArray values && values.length() == 4) {
            return Spine37Data.Curve.bezier(
                    (float) values.getDouble(0),
                    (float) values.getDouble(1),
                    (float) values.getDouble(2),
                    (float) values.getDouble(3));
        }
        throw new IllegalArgumentException(
                "Spine curve must be linear, stepped, or four Bezier controls");
    }

    static float[] floats(JSONArray body) throws JSONException {
        float[] result = new float[body.length()];
        for (int index = 0; index < body.length(); index++) {
            result[index] = (float) body.getDouble(index);
        }
        return result;
    }

    static short[] shorts(JSONArray body) throws JSONException {
        short[] result = new short[body.length()];
        for (int index = 0; index < body.length(); index++) {
            result[index] = (short) body.getInt(index);
        }
        return result;
    }

    static float number(JSONObject body, String name, float defaultValue) {
        return body.has(name) ? (float) body.optDouble(name, defaultValue) : defaultValue;
    }

    static int requireIndex(
            Map<String, Integer> indices, String name, String kind) {
        Integer index = indices.get(name);
        if (index == null) {
            throw new IllegalArgumentException("Unknown " + kind + ": " + name);
        }
        return index;
    }}
