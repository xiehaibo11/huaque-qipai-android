package com.nanbeiyule.game.spine37;

public final class Spine37MeshBuilder {
    public record BoneMatrix(float a, float b, float c, float d, float worldX, float worldY) {
        float transformX(float x, float y) {
            return x * a + y * b + worldX;
        }

        float transformY(float x, float y) {
            return x * c + y * d + worldY;
        }
    }

    private Spine37MeshBuilder() {}

    public static float[] worldVertices(
            Spine37Data.Attachment attachment,
            BoneMatrix[] bones,
            int slotBoneIndex,
            float[] deform) {
        return worldVertices(attachment, null, bones, slotBoneIndex, deform);
    }

    public static float[] worldVertices(
            Spine37Data.Attachment attachment,
            Spine37Atlas.Region region,
            BoneMatrix[] bones,
            int slotBoneIndex,
            float[] deform) {
        if (attachment.weighted()) {
            return weightedWorldVertices(attachment.vertices(), bones);
        }
        float[] local =
                attachment.mesh()
                        ? attachment.vertices()
                        : regionLocalVertices(attachment, region);
        if (local.length % 2 != 0) {
            throw new IllegalArgumentException("Unweighted vertices must contain x/y pairs");
        }
        BoneMatrix bone = requireBone(bones, slotBoneIndex);
        float[] result = new float[local.length];
        for (int index = 0; index < local.length; index += 2) {
            float x = local[index] + value(deform, index);
            float y = local[index + 1] + value(deform, index + 1);
            result[index] = bone.transformX(x, y);
            result[index + 1] = bone.transformY(x, y);
        }
        return result;
    }

    public static short[] triangles(Spine37Data.Attachment attachment) {
        return attachment.mesh()
                ? attachment.triangles()
                : new short[] {0, 1, 2, 2, 3, 0};
    }

    public static float[] worldVertices(
            Spine37Data.ClippingAttachment attachment,
            BoneMatrix[] bones,
            int slotBoneIndex) {
        return worldVertices(attachment, bones, slotBoneIndex, new float[0]);
    }

    public static float[] worldVertices(
            Spine37Data.ClippingAttachment attachment,
            BoneMatrix[] bones,
            int slotBoneIndex,
            float[] deform) {
        if (attachment.weighted()) {
            return weightedWorldVertices(attachment.vertices(), bones);
        }
        float[] local = attachment.vertices();
        if (local.length != attachment.vertexCount() * 2) {
            throw new IllegalArgumentException(
                    "Unweighted clipping vertices must contain x/y pairs");
        }
        BoneMatrix bone = requireBone(bones, slotBoneIndex);
        float[] result = new float[local.length];
        for (int index = 0; index < local.length; index += 2) {
            float x = local[index] + value(deform, index);
            float y = local[index + 1] + value(deform, index + 1);
            result[index] = bone.transformX(x, y);
            result[index + 1] = bone.transformY(x, y);
        }
        return result;
    }

    public static float[] atlasUvs(
            Spine37Data.Attachment attachment,
            Spine37Atlas.Region region,
            Spine37Atlas.Page page) {
        float[] source =
                attachment.mesh()
                        ? attachment.uvs()
                        : new float[] {0, 1, 0, 0, 1, 0, 1, 1};
        float[] result = new float[source.length];
        float pageWidth = page.width();
        float pageHeight = page.height();
        for (int index = 0; index < source.length; index += 2) {
            float sourceU = source[index];
            float sourceV = source[index + 1];
            if (region.rotated()) {
                result[index] =
                        (region.x() + sourceV * region.packedWidth()) / pageWidth;
                result[index + 1] =
                        (region.y() + (1.0f - sourceU) * region.packedHeight())
                                / pageHeight;
            } else {
                result[index] =
                        (region.x() + sourceU * region.packedWidth()) / pageWidth;
                result[index + 1] =
                        (region.y() + sourceV * region.packedHeight()) / pageHeight;
            }
        }
        return result;
    }

    private static float[] regionLocalVertices(
            Spine37Data.Attachment attachment, Spine37Atlas.Region region) {
        float localLeft;
        float localBottom;
        float localRight;
        float localTop;
        if (region == null || region.originalWidth() <= 0 || region.originalHeight() <= 0) {
            float halfWidth = attachment.width() * attachment.scaleX() / 2.0f;
            float halfHeight = attachment.height() * attachment.scaleY() / 2.0f;
            localLeft = -halfWidth;
            localBottom = -halfHeight;
            localRight = halfWidth;
            localTop = halfHeight;
        } else {
            float regionScaleX =
                    attachment.width() / region.originalWidth() * attachment.scaleX();
            float regionScaleY =
                    attachment.height() / region.originalHeight() * attachment.scaleY();
            localLeft =
                    -attachment.width() * attachment.scaleX() / 2.0f
                            + region.offsetX() * regionScaleX;
            localBottom =
                    -attachment.height() * attachment.scaleY() / 2.0f
                            + region.offsetY() * regionScaleY;
            localRight = localLeft + region.width() * regionScaleX;
            localTop = localBottom + region.height() * regionScaleY;
        }
        float radians = (float) Math.toRadians(attachment.rotation());
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float[] corners =
                new float[] {
                    localLeft, localBottom,
                    localLeft, localTop,
                    localRight, localTop,
                    localRight, localBottom
                };
        for (int index = 0; index < corners.length; index += 2) {
            float x = corners[index];
            float y = corners[index + 1];
            corners[index] = x * cos - y * sin + attachment.x();
            corners[index + 1] = x * sin + y * cos + attachment.y();
        }
        return corners;
    }

    private static float[] weightedWorldVertices(
            float[] encoded, BoneMatrix[] bones) {
        int cursor = 0;
        int vertexCount = 0;
        while (cursor < encoded.length) {
            int influences = Math.round(encoded[cursor++]);
            if (influences <= 0) {
                throw new IllegalArgumentException("Weighted vertex has no influences");
            }
            cursor += influences * 4;
            if (cursor > encoded.length) {
                throw new IllegalArgumentException("Malformed weighted vertex data");
            }
            vertexCount++;
        }
        float[] result = new float[vertexCount * 2];
        cursor = 0;
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            int influences = Math.round(encoded[cursor++]);
            float worldX = 0.0f;
            float worldY = 0.0f;
            for (int influence = 0; influence < influences; influence++) {
                int boneIndex = Math.round(encoded[cursor++]);
                float x = encoded[cursor++];
                float y = encoded[cursor++];
                float weight = encoded[cursor++];
                BoneMatrix bone = requireBone(bones, boneIndex);
                worldX += bone.transformX(x, y) * weight;
                worldY += bone.transformY(x, y) * weight;
            }
            result[vertex * 2] = worldX;
            result[vertex * 2 + 1] = worldY;
        }
        return result;
    }

    private static BoneMatrix requireBone(BoneMatrix[] bones, int index) {
        if (index < 0 || index >= bones.length || bones[index] == null) {
            throw new IllegalArgumentException("Invalid bone index " + index);
        }
        return bones[index];
    }

    private static float value(float[] values, int index) {
        return values != null && index < values.length ? values[index] : 0.0f;
    }
}
