package com.nanbeiyule.game.spine37;

import java.util.List;
import java.util.Map;

public record Spine37Data(
        String spineVersion,
        List<BoneSetup> bones,
        List<SlotSetup> slots,
        Map<AttachmentKey, Attachment> attachments,
        Map<AttachmentKey, ClippingAttachment> clippingAttachments,
        Map<String, Animation> animations) {
    public Spine37Data {
        bones = List.copyOf(bones);
        slots = List.copyOf(slots);
        attachments = Map.copyOf(attachments);
        clippingAttachments = Map.copyOf(clippingAttachments);
        animations = Map.copyOf(animations);
    }

    public Spine37Data(
            String spineVersion,
            List<BoneSetup> bones,
            List<SlotSetup> slots,
            Map<AttachmentKey, Attachment> attachments,
            Map<String, Animation> animations) {
        this(spineVersion, bones, slots, attachments, Map.of(), animations);
    }

    public int meshAttachmentCount() {
        int count = 0;
        for (Attachment attachment : attachments.values()) {
            if (attachment.mesh()) {
                count++;
            }
        }
        return count;
    }

    public int weightedMeshAttachmentCount() {
        int count = 0;
        for (Attachment attachment : attachments.values()) {
            if (attachment.mesh() && attachment.weighted()) {
                count++;
            }
        }
        return count;
    }

    public int clippingAttachmentCount() {
        return clippingAttachments.size();
    }

    public enum TransformMode {
        NORMAL,
        NO_SCALE
    }

    public record BoneSetup(
            String name,
            int parentIndex,
            float x,
            float y,
            float rotation,
            float scaleX,
            float scaleY,
            float shearX,
            float shearY,
            TransformMode transformMode) {
        public BoneSetup(
                String name,
                int parentIndex,
                float x,
                float y,
                float rotation,
                float scaleX,
                float scaleY) {
            this(
                    name,
                    parentIndex,
                    x,
                    y,
                    rotation,
                    scaleX,
                    scaleY,
                    0.0f,
                    0.0f,
                    TransformMode.NORMAL);
        }
    }

    public record SlotSetup(
            String name,
            int boneIndex,
            String setupAttachment,
            String blend,
            ColorValue color) {}

    public record AttachmentKey(String slotName, String attachmentName) {}

    public record Attachment(
            String slotName,
            String name,
            String path,
            boolean mesh,
            boolean weighted,
            float x,
            float y,
            float rotation,
            float scaleX,
            float scaleY,
            float width,
            float height,
            float[] uvs,
            short[] triangles,
            float[] vertices) {
        public Attachment {
            uvs = uvs == null ? new float[0] : uvs.clone();
            triangles = triangles == null ? new short[0] : triangles.clone();
            vertices = vertices == null ? new float[0] : vertices.clone();
        }

        @Override
        public float[] uvs() {
            return uvs.clone();
        }

        @Override
        public short[] triangles() {
            return triangles.clone();
        }

        @Override
        public float[] vertices() {
            return vertices.clone();
        }
    }

    public record ClippingAttachment(
            String slotName,
            String name,
            String endSlot,
            boolean weighted,
            int vertexCount,
            float[] vertices) {
        public ClippingAttachment {
            vertices = vertices.clone();
        }

        @Override
        public float[] vertices() {
            return vertices.clone();
        }
    }

    public record Animation(
            String name,
            float duration,
            Map<String, BoneTimeline> boneTimelines,
            Map<String, SlotTimeline> slotTimelines,
            Map<DeformKey, List<DeformFrame>> deformTimelines,
            List<EventFrame> events) {
        public Animation {
            boneTimelines = Map.copyOf(boneTimelines);
            slotTimelines = Map.copyOf(slotTimelines);
            deformTimelines = Map.copyOf(deformTimelines);
            events = List.copyOf(events);
        }

        public Animation(
                String name,
                float duration,
                Map<String, BoneTimeline> boneTimelines,
                Map<String, SlotTimeline> slotTimelines,
                Map<DeformKey, List<DeformFrame>> deformTimelines) {
            this(name, duration, boneTimelines, slotTimelines, deformTimelines, List.of());
        }
    }

    public record BoneTimeline(
            List<NumericFrame> rotate,
            List<NumericFrame> translate,
            List<NumericFrame> scale,
            List<NumericFrame> shear) {
        public BoneTimeline {
            rotate = List.copyOf(rotate);
            translate = List.copyOf(translate);
            scale = List.copyOf(scale);
            shear = List.copyOf(shear);
        }

        public BoneTimeline(
                List<NumericFrame> rotate,
                List<NumericFrame> translate,
                List<NumericFrame> scale) {
            this(rotate, translate, scale, List.of());
        }
    }

    public record SlotTimeline(
            List<ColorFrame> color,
            List<AttachmentFrame> attachment) {
        public SlotTimeline {
            color = List.copyOf(color);
            attachment = List.copyOf(attachment);
        }
    }

    public record Curve(Type type, float x1, float y1, float x2, float y2) {
        public static final Curve LINEAR =
                new Curve(Type.LINEAR, 0.0f, 0.0f, 1.0f, 1.0f);
        public static final Curve STEPPED =
                new Curve(Type.STEPPED, 0.0f, 0.0f, 1.0f, 1.0f);

        public enum Type {
            LINEAR,
            STEPPED,
            BEZIER
        }

        public Curve {
            if (type == null
                    || !Float.isFinite(x1)
                    || !Float.isFinite(y1)
                    || !Float.isFinite(x2)
                    || !Float.isFinite(y2)) {
                throw new IllegalArgumentException(
                        "Spine curve values must be finite");
            }
            if (type == Type.BEZIER
                    && (x1 < 0.0f || x1 > 1.0f || x2 < 0.0f || x2 > 1.0f)) {
                throw new IllegalArgumentException(
                        "Spine Bezier time controls must be between zero and one");
            }
        }

        public static Curve bezier(float x1, float y1, float x2, float y2) {
            return new Curve(Type.BEZIER, x1, y1, x2, y2);
        }

        public boolean stepped() {
            return type == Type.STEPPED;
        }

        public float apply(float linearAlpha) {
            float alpha = Math.max(0.0f, Math.min(1.0f, linearAlpha));
            if (type == Type.LINEAR) {
                return alpha;
            }
            if (type == Type.STEPPED) {
                return 0.0f;
            }
            float low = 0.0f;
            float high = 1.0f;
            for (int iteration = 0; iteration < 18; iteration++) {
                float parameter = (low + high) / 2.0f;
                float curveX = cubic(parameter, x1, x2);
                if (curveX < alpha) {
                    low = parameter;
                } else {
                    high = parameter;
                }
            }
            return cubic((low + high) / 2.0f, y1, y2);
        }

        private static float cubic(float parameter, float first, float second) {
            float inverse = 1.0f - parameter;
            return 3.0f * inverse * inverse * parameter * first
                    + 3.0f * inverse * parameter * parameter * second
                    + parameter * parameter * parameter;
        }
    }

    public record NumericFrame(float time, float x, float y, Curve curve) {
        public NumericFrame {
            if (curve == null) {
                throw new IllegalArgumentException("Numeric frame curve must not be null");
            }
        }

        public NumericFrame(float time, float x, float y, boolean stepped) {
            this(time, x, y, stepped ? Curve.STEPPED : Curve.LINEAR);
        }

        public boolean stepped() {
            return curve.stepped();
        }
    }

    public record ColorFrame(float time, ColorValue color, Curve curve) {
        public ColorFrame {
            if (color == null || curve == null) {
                throw new IllegalArgumentException(
                        "Color frame values must not be null");
            }
        }

        public ColorFrame(float time, ColorValue color, boolean stepped) {
            this(time, color, stepped ? Curve.STEPPED : Curve.LINEAR);
        }

        public boolean stepped() {
            return curve.stepped();
        }
    }

    public record AttachmentFrame(float time, String name) {}

    public record EventFrame(float time, String name) {}

    public record DeformKey(String slotName, String attachmentName) {}

    public record DeformFrame(float time, int offset, float[] vertices, Curve curve) {
        public DeformFrame {
            vertices = vertices.clone();
            if (curve == null) {
                throw new IllegalArgumentException("Deform frame curve must not be null");
            }
        }

        public DeformFrame(
                float time,
                int offset,
                float[] vertices,
                boolean stepped) {
            this(
                    time,
                    offset,
                    vertices,
                    stepped ? Curve.STEPPED : Curve.LINEAR);
        }

        @Override
        public float[] vertices() {
            return vertices.clone();
        }

        public boolean stepped() {
            return curve.stepped();
        }
    }

    public record ColorValue(float red, float green, float blue, float alpha) {
        public static final ColorValue WHITE = new ColorValue(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
