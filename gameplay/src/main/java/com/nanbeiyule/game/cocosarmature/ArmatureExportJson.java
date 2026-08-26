package com.nanbeiyule.game.cocosarmature;

import android.content.res.AssetManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** 解析原版 {@code *.ExportJson}（CocosStudio Armature 1.x 导出格式）。 */
public final class ArmatureExportJson {
    private ArmatureExportJson() {}

    public static ArmatureData load(AssetManager assets, String assetPath) {
        try (InputStream stream = assets.open(assetPath)) {
            return parse(readAll(stream));
        } catch (IOException | JSONException exception) {
            throw new IllegalStateException("Unable to load armature " + assetPath, exception);
        }
    }

    static ArmatureData parse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONObject armature = root.getJSONArray("armature_data").getJSONObject(0);
        List<ArmatureData.Bone> bones = new ArrayList<>();
        JSONArray boneArray = armature.getJSONArray("bone_data");
        for (int i = 0; i < boneArray.length(); i++) {
            bones.add(readBone(boneArray.getJSONObject(i)));
        }
        // 原版按 bone_data 的 z 升序绘制（CCBone::setZOrder → CCArmature 的 _boneDag）。
        bones.sort(Comparator.comparingInt(ArmatureData.Bone::zOrder));

        Map<String, ArmatureData.Movement> movements = new LinkedHashMap<>();
        JSONArray animationArray = root.getJSONArray("animation_data");
        for (int i = 0; i < animationArray.length(); i++) {
            JSONArray movementArray =
                    animationArray.getJSONObject(i).getJSONArray("mov_data");
            for (int j = 0; j < movementArray.length(); j++) {
                ArmatureData.Movement movement = readMovement(movementArray.getJSONObject(j));
                movements.put(movement.name(), movement);
            }
        }

        Map<String, ArmatureData.Texture> textures = new LinkedHashMap<>();
        JSONArray textureArray = root.optJSONArray("texture_data");
        for (int i = 0; textureArray != null && i < textureArray.length(); i++) {
            JSONObject texture = textureArray.getJSONObject(i);
            String name = ArmatureData.stripSuffix(texture.getString("name"));
            textures.put(
                    name,
                    new ArmatureData.Texture(
                            name,
                            (float) texture.optDouble("pX", 0.5),
                            (float) texture.optDouble("pY", 0.5),
                            (float) texture.optDouble("width", 0.0),
                            (float) texture.optDouble("height", 0.0)));
        }
        return new ArmatureData(armature.getString("name"), bones, movements, textures);
    }

    private static ArmatureData.Bone readBone(JSONObject bone) throws JSONException {
        List<String> displays = new ArrayList<>();
        JSONArray displayArray = bone.optJSONArray("display_data");
        for (int i = 0; displayArray != null && i < displayArray.length(); i++) {
            displays.add(displayArray.getJSONObject(i).getString("name"));
        }
        return new ArmatureData.Bone(
                bone.getString("name"),
                bone.optString("parent", ""),
                (float) bone.optDouble("x", 0.0),
                (float) bone.optDouble("y", 0.0),
                (float) bone.optDouble("cX", 1.0),
                (float) bone.optDouble("cY", 1.0),
                (float) bone.optDouble("kX", 0.0),
                (float) bone.optDouble("kY", 0.0),
                bone.optInt("z", 0),
                displays);
    }

    private static ArmatureData.Movement readMovement(JSONObject movement) throws JSONException {
        List<ArmatureData.BoneTrack> tracks = new ArrayList<>();
        JSONArray trackArray = movement.getJSONArray("mov_bone_data");
        for (int i = 0; i < trackArray.length(); i++) {
            JSONObject track = trackArray.getJSONObject(i);
            List<ArmatureData.Keyframe> keyframes = new ArrayList<>();
            JSONArray frameArray = track.getJSONArray("frame_data");
            for (int j = 0; j < frameArray.length(); j++) {
                keyframes.add(readKeyframe(frameArray.getJSONObject(j)));
            }
            tracks.add(
                    new ArmatureData.BoneTrack(
                            track.getString("name"),
                            (float) track.optDouble("dl", 0.0),
                            keyframes));
        }
        return new ArmatureData.Movement(
                movement.getString("name"),
                movement.optInt("dr", 1),
                movement.optBoolean("lp", true),
                (float) movement.optDouble("sc", 1.0),
                tracks);
    }

    private static ArmatureData.Keyframe readKeyframe(JSONObject frame) throws JSONException {
        return new ArmatureData.Keyframe(
                frame.optInt("fi", 0),
                (float) frame.optDouble("x", 0.0),
                (float) frame.optDouble("y", 0.0),
                (float) frame.optDouble("cX", 1.0),
                (float) frame.optDouble("cY", 1.0),
                (float) frame.optDouble("kX", 0.0),
                (float) frame.optDouble("kY", 0.0),
                frame.optInt("dI", 0),
                frame.optBoolean("tweenFrame", true),
                frame.optInt("twE", 0));
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
