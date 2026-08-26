package com.nanbeiyule.game.spine37;

import static com.nanbeiyule.game.spine37.Spine37JsonValues.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Spine37JsonParser {
    // 仓库内两个已验证的骨架版本：大厅特效导出为 3.7.94，闲逸斗地主原版大厅人物
    // （renwunv/renwunan）导出为 3.3.07。两者的 bones/slots/skins 结构相同，动画只用到
    // 本运行时已支持的 rotate、translate、scale、attachment 时间轴与 stepped/贝塞尔曲线。
    // 闲逸斗地主点击特效导出为 3.8.75，但仍使用旧版对象 skins，且动画只包含上述
    // 已支持时间轴。仅放行这个实际验证版本；其他 3.8 数据仍拒绝。
    private static final List<String> SUPPORTED_VERSION_PREFIXES =
            List.of("3.3.", "3.7.", "3.7-from-", "3.8.75");

    private Spine37JsonParser() {}

    public static Spine37Data parse(String json) {
        try {
            JSONObject root = new JSONObject(json);
            String version = root.getJSONObject("skeleton").getString("spine");
            if (SUPPORTED_VERSION_PREFIXES.stream().noneMatch(version::startsWith)) {
                throw new IllegalArgumentException(
                        "Expected Spine 3.3, 3.7, or verified 3.8.75 data, found "
                                + version);
            }
            List<Spine37Data.BoneSetup> bones = parseBones(root.getJSONArray("bones"));
            Map<String, Integer> boneIndices = new LinkedHashMap<>();
            for (int index = 0; index < bones.size(); index++) {
                boneIndices.put(bones.get(index).name(), index);
            }
            List<Spine37Data.SlotSetup> slots =
                    parseSlots(root.getJSONArray("slots"), boneIndices);
            ParsedAttachments parsedAttachments =
                    parseAttachments(root.getJSONObject("skins"));
            Map<String, Spine37Data.Animation> animations =
                    Spine37AnimationParser.parseAnimations(root.getJSONObject("animations"));
            return new Spine37Data(
                    version,
                    bones,
                    slots,
                    parsedAttachments.drawable(),
                    parsedAttachments.clipping(),
                    animations);
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid Spine skeleton JSON", exception);
        }
    }

    private static List<Spine37Data.BoneSetup> parseBones(JSONArray body)
            throws JSONException {
        List<Spine37Data.BoneSetup> bones = new ArrayList<>();
        Map<String, Integer> indices = new LinkedHashMap<>();
        for (int index = 0; index < body.length(); index++) {
            JSONObject bone = body.getJSONObject(index);
            String name = bone.getString("name");
            String parent = bone.optString("parent", "");
            int parentIndex = parent.isEmpty() ? -1 : requireIndex(indices, parent, "bone");
            bones.add(
                    new Spine37Data.BoneSetup(
                            name,
                            parentIndex,
                            number(bone, "x", 0.0f),
                            number(bone, "y", 0.0f),
                            number(bone, "rotation", 0.0f),
                            number(bone, "scaleX", 1.0f),
                            number(bone, "scaleY", 1.0f),
                            number(bone, "shearX", 0.0f),
                            number(bone, "shearY", 0.0f),
                            transformMode(bone.optString("transform", "normal"))));
            indices.put(name, index);
        }
        return bones;
    }

    private static Spine37Data.TransformMode transformMode(String value) {
        return "noScale".equals(value)
                ? Spine37Data.TransformMode.NO_SCALE
                : Spine37Data.TransformMode.NORMAL;
    }

    private static List<Spine37Data.SlotSetup> parseSlots(
            JSONArray body, Map<String, Integer> boneIndices)
            throws JSONException {
        List<Spine37Data.SlotSetup> slots = new ArrayList<>();
        for (int index = 0; index < body.length(); index++) {
            JSONObject slot = body.getJSONObject(index);
            slots.add(
                    new Spine37Data.SlotSetup(
                            slot.getString("name"),
                            requireIndex(
                                    boneIndices,
                                    slot.getString("bone"),
                                    "slot bone"),
                            slot.has("attachment") ? slot.optString("attachment", null) : null,
                            slot.optString("blend", "normal"),
                            parseColor(slot.optString("color", "ffffffff"))));
        }
        return slots;
    }

    private static ParsedAttachments parseAttachments(
            JSONObject skins)
            throws JSONException {
        JSONObject defaultSkin = skins.getJSONObject("default");
        Map<Spine37Data.AttachmentKey, Spine37Data.Attachment> result =
                new LinkedHashMap<>();
        Map<Spine37Data.AttachmentKey, Spine37Data.ClippingAttachment> clipping =
                new LinkedHashMap<>();
        Iterator<String> slotNames = defaultSkin.keys();
        while (slotNames.hasNext()) {
            String slotName = slotNames.next();
            JSONObject slotAttachments = defaultSkin.getJSONObject(slotName);
            Iterator<String> attachmentNames = slotAttachments.keys();
            while (attachmentNames.hasNext()) {
                String attachmentName = attachmentNames.next();
                JSONObject body = slotAttachments.getJSONObject(attachmentName);
                String type = body.optString("type", "region");
                if ("clipping".equals(type)) {
                    int vertexCount = body.getInt("vertexCount");
                    float[] vertices = floats(body.getJSONArray("vertices"));
                    clipping.put(
                            new Spine37Data.AttachmentKey(slotName, attachmentName),
                            new Spine37Data.ClippingAttachment(
                                    slotName,
                                    attachmentName,
                                    body.getString("end"),
                                    vertices.length != vertexCount * 2,
                                    vertexCount,
                                    vertices));
                    continue;
                }
                if (!"region".equals(type) && !"mesh".equals(type)) {
                    // Other non-drawable attachment types are not needed by this runtime.
                    continue;
                }
                boolean mesh = "mesh".equals(type);
                float[] uvs = mesh ? floats(body.getJSONArray("uvs")) : new float[0];
                float[] vertices = mesh ? floats(body.getJSONArray("vertices")) : new float[0];
                short[] triangles =
                        mesh ? shorts(body.getJSONArray("triangles")) : new short[0];
                boolean weighted = mesh && vertices.length != uvs.length;
                Spine37Data.Attachment attachment =
                        new Spine37Data.Attachment(
                                slotName,
                                attachmentName,
                                body.optString("path", attachmentName),
                                mesh,
                                weighted,
                                number(body, "x", 0.0f),
                                number(body, "y", 0.0f),
                                number(body, "rotation", 0.0f),
                                number(body, "scaleX", 1.0f),
                                number(body, "scaleY", 1.0f),
                                number(body, "width", 0.0f),
                                number(body, "height", 0.0f),
                                uvs,
                                triangles,
                                vertices);
                result.put(
                        new Spine37Data.AttachmentKey(slotName, attachmentName),
                        attachment);
            }
        }
        return new ParsedAttachments(result, clipping);
    }

    private record ParsedAttachments(
            Map<Spine37Data.AttachmentKey, Spine37Data.Attachment> drawable,
            Map<Spine37Data.AttachmentKey, Spine37Data.ClippingAttachment> clipping) {}

}
