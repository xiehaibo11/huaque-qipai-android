package com.nanbeiyule.game.spine37;

import static com.nanbeiyule.game.spine37.Spine37TimelineSampler.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Spine37Runtime {
    public record DrawCommand(
            String attachmentName,
            String pageName,
            String blend,
            Spine37Data.ColorValue color,
            float[] vertices,
            float[] uvs,
            short[] triangles) {
        public DrawCommand {
            vertices = vertices.clone();
            uvs = uvs.clone();
            triangles = triangles.clone();
        }

        @Override
        public float[] vertices() {
            return vertices.clone();
        }

        @Override
        public float[] uvs() {
            return uvs.clone();
        }

        @Override
        public short[] triangles() {
            return triangles.clone();
        }
    }

    private final Spine37Data data;
    private final Spine37Atlas atlas;
    private final Map<String, Integer> boneIndices = new LinkedHashMap<>();
    private final Map<String, Spine37Atlas.Page> pages = new LinkedHashMap<>();

    public Spine37Runtime(Spine37Data data, Spine37Atlas atlas) {
        this.data = data;
        this.atlas = atlas;
        for (int index = 0; index < data.bones().size(); index++) {
            boneIndices.put(data.bones().get(index).name(), index);
        }
        for (Spine37Atlas.Page page : atlas.pages()) {
            pages.put(page.name(), page);
        }
    }

    public float animationDuration(String animationName) {
        return animation(animationName).duration();
    }

    public float eventTime(String animationName, String eventName) {
        for (Spine37Data.EventFrame event : animation(animationName).events()) {
            if (event.name().equals(eventName)) return event.time();
        }
        return Float.NaN;
    }

    public List<DrawCommand> sample(String animationName, float elapsedSeconds) {
        Spine37Data.Animation animation = animation(animationName);
        float time = Spine37Animator.wrapTime(elapsedSeconds, animation.duration());
        Spine37MeshBuilder.BoneMatrix[] bones = sampleBones(animation, time);
        List<DrawCommand> commands = new ArrayList<>();
        float[] activeClipPolygon = null;
        String activeClipEndSlot = null;
        for (Spine37Data.SlotSetup slot : data.slots()) {
            String clipEndingAfterThisSlot = activeClipEndSlot;
            Spine37Data.SlotTimeline timeline = animation.slotTimelines().get(slot.name());
            String attachmentName = attachmentAt(slot.setupAttachment(), timeline, time);
            if (attachmentName != null) {
                Spine37Data.AttachmentKey attachmentKey =
                        new Spine37Data.AttachmentKey(slot.name(), attachmentName);
                Spine37Data.ClippingAttachment clippingAttachment =
                        data.clippingAttachments().get(attachmentKey);
                if (clippingAttachment != null) {
                    float[] clippingDeform =
                            sampleDeform(
                                    animation.deformTimelines()
                                            .get(
                                                    new Spine37Data.DeformKey(
                                                            slot.name(),
                                                            attachmentName)),
                                    clippingAttachment,
                                    time);
                    activeClipPolygon =
                            Spine37MeshBuilder.worldVertices(
                                    clippingAttachment,
                                    bones,
                                    slot.boneIndex(),
                                    clippingDeform);
                    activeClipEndSlot = clippingAttachment.endSlot();
                } else {
                    Spine37Data.Attachment attachment = data.attachments().get(attachmentKey);
                    if (attachment != null) {
                        Spine37Atlas.Region region = atlas.regions().get(attachment.path());
                        if (region != null) {
                            Spine37Atlas.Page page = pages.get(region.pageName());
                            if (page == null) {
                                throw new IllegalArgumentException(
                                        "Missing atlas page " + region.pageName());
                            }
                            float[] deform =
                                    sampleDeform(
                                            animation.deformTimelines()
                                                    .get(
                                                            new Spine37Data.DeformKey(
                                                                    slot.name(),
                                                                    attachmentName)),
                                            attachment,
                                            time);
                            float[] vertices =
                                    Spine37MeshBuilder.worldVertices(
                                            attachment,
                                            region,
                                            bones,
                                            slot.boneIndex(),
                                            deform);
                            float[] uvs =
                                    Spine37MeshBuilder.atlasUvs(attachment, region, page);
                            short[] triangles = Spine37MeshBuilder.triangles(attachment);
                            Spine37Data.ColorValue color =
                                    multiply(slot.color(), colorAt(timeline, time));
                            if (color.alpha() > 0.001f) {
                                if (activeClipPolygon != null) {
                                    Spine37PolygonClipper.Result clipped =
                                            Spine37PolygonClipper.clip(
                                                    vertices,
                                                    uvs,
                                                    triangles,
                                                    activeClipPolygon);
                                    vertices = clipped.vertices();
                                    uvs = clipped.uvs();
                                    triangles = clipped.triangles();
                                }
                                if (triangles.length > 0) {
                                    commands.add(
                                            new DrawCommand(
                                                    attachmentName,
                                                    region.pageName(),
                                                    slot.blend(),
                                                    color,
                                                    vertices,
                                                    uvs,
                                                    triangles));
                                }
                            }
                        }
                        // Missing atlas regions intentionally render nothing, matching Spine.
                    }
                }
            }
            if (clipEndingAfterThisSlot != null
                    && clipEndingAfterThisSlot.equals(slot.name())) {
                activeClipPolygon = null;
                activeClipEndSlot = null;
            }
        }
        return List.copyOf(commands);
    }

    private Spine37Data.Animation animation(String animationName) {
        Spine37Data.Animation animation = data.animations().get(animationName);
        if (animation == null) throw new IllegalArgumentException("Unknown animation " + animationName);
        return animation;
    }

    private Spine37MeshBuilder.BoneMatrix[] sampleBones(
            Spine37Data.Animation animation, float time) {
        Spine37MeshBuilder.BoneMatrix[] result =
                new Spine37MeshBuilder.BoneMatrix[data.bones().size()];
        for (int index = 0; index < data.bones().size(); index++) {
            Spine37Data.BoneSetup setup = data.bones().get(index);
            Spine37Data.BoneTimeline timeline =
                    animation.boneTimelines().get(setup.name());
            float rotation =
                    setup.rotation()
                            + (timeline == null
                                    ? 0.0f
                                    : Spine37Animator.sampleX(
                                            timeline.rotate(),
                                            time,
                                            0.0f));
            float x =
                    setup.x()
                            + (timeline == null
                                    ? 0.0f
                                    : Spine37Animator.sampleX(
                                            timeline.translate(),
                                            time,
                                            0.0f));
            float y =
                    setup.y()
                            + (timeline == null
                                    ? 0.0f
                                    : Spine37Animator.sampleY(
                                            timeline.translate(),
                                            time,
                                            0.0f));
            float scaleX =
                    setup.scaleX()
                            * (timeline == null
                                    ? 1.0f
                                    : Spine37Animator.sampleX(
                                            timeline.scale(),
                                            time,
                                            1.0f));
            float scaleY =
                    setup.scaleY()
                            * (timeline == null
                                    ? 1.0f
                                    : Spine37Animator.sampleY(
                                            timeline.scale(),
                                            time,
                                            1.0f));
            float shearX =
                    setup.shearX()
                            + (timeline == null
                                    ? 0.0f
                                    : Spine37Animator.sampleX(
                                            timeline.shear(),
                                            time,
                                            0.0f));
            float shearY =
                    setup.shearY()
                            + (timeline == null
                                    ? 0.0f
                                    : Spine37Animator.sampleY(
                                            timeline.shear(),
                                            time,
                                            0.0f));
            double radiansX = Math.toRadians(rotation + shearX);
            double radiansY = Math.toRadians(rotation + 90.0f + shearY);
            float localA = (float) Math.cos(radiansX) * scaleX;
            float localB = (float) Math.cos(radiansY) * scaleY;
            float localC = (float) Math.sin(radiansX) * scaleX;
            float localD = (float) Math.sin(radiansY) * scaleY;
            if (setup.parentIndex() < 0) {
                result[index] =
                        new Spine37MeshBuilder.BoneMatrix(
                                localA,
                                localB,
                                localC,
                                localD,
                                x,
                                y);
            } else {
                Spine37MeshBuilder.BoneMatrix parent = result[setup.parentIndex()];
                float parentA = parent.a();
                float parentB = parent.b();
                float parentC = parent.c();
                float parentD = parent.d();
                if (setup.transformMode() == Spine37Data.TransformMode.NO_SCALE) {
                    float parentXLength = (float) Math.hypot(parentA, parentC);
                    float parentYLength = (float) Math.hypot(parentB, parentD);
                    if (parentXLength > 0.000001f) {
                        parentA /= parentXLength;
                        parentC /= parentXLength;
                    }
                    if (parentYLength > 0.000001f) {
                        parentB /= parentYLength;
                        parentD /= parentYLength;
                    }
                }
                result[index] =
                        new Spine37MeshBuilder.BoneMatrix(
                                parentA * localA + parentB * localC,
                                parentA * localB + parentB * localD,
                                parentC * localA + parentD * localC,
                                parentC * localB + parentD * localD,
                                x * parent.a() + y * parent.b() + parent.worldX(),
                                x * parent.c() + y * parent.d() + parent.worldY());
            }
        }
        return result;
    }

}
