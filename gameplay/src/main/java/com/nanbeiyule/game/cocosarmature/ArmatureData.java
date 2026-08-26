package com.nanbeiyule.game.cocosarmature;

import java.util.List;
import java.util.Map;

/**
 * 原版 CocosStudio Armature（{@code *.ExportJson}）的只读模型。
 *
 * <p>字段名沿用导出文件的短键：{@code cX/cY} 缩放、{@code kX/kY} 斜切弧度、
 * {@code dI} 显示索引、{@code fi} 关键帧号、{@code dr} movement 帧长、
 * {@code sc} 播放缩放、{@code twE} 缓动类型。
 */
public final class ArmatureData {
    /** {@code armature_data[].bone_data[]} 的一根骨骼。 */
    public record Bone(
            String name,
            String parent,
            float x,
            float y,
            float scaleX,
            float scaleY,
            float skewX,
            float skewY,
            int zOrder,
            List<String> displays) {}

    /** {@code mov_bone_data[].frame_data[]} 的一个关键帧。 */
    public record Keyframe(
            int frameIndex,
            float x,
            float y,
            float scaleX,
            float scaleY,
            float skewX,
            float skewY,
            int displayIndex,
            boolean tween,
            int easing) {}

    /** {@code mov_data[].mov_bone_data[]} 的一条骨骼轨道。 */
    public record BoneTrack(String name, float delay, List<Keyframe> keyframes) {}

    /** {@code animation_data[].mov_data[]} 的一段动作。 */
    public record Movement(
            String name,
            int durationFrames,
            boolean loop,
            float scale,
            List<BoneTrack> tracks) {}

    /** {@code texture_data[]} 的一个显示对象锚点。 */
    public record Texture(String name, float anchorX, float anchorY, float width, float height) {}

    private final String name;
    private final List<Bone> bones;
    private final Map<String, Movement> movements;
    private final Map<String, Texture> textures;

    ArmatureData(
            String name,
            List<Bone> bones,
            Map<String, Movement> movements,
            Map<String, Texture> textures) {
        this.name = name;
        this.bones = List.copyOf(bones);
        this.movements = Map.copyOf(movements);
        this.textures = Map.copyOf(textures);
    }

    public String name() {
        return name;
    }

    /** 骨骼按 {@code z} 升序，绘制顺序即列表顺序。 */
    public List<Bone> bones() {
        return bones;
    }

    public Movement movement(String movementName) {
        Movement movement = movements.get(movementName);
        if (movement == null) {
            throw new IllegalArgumentException("Missing armature movement " + movementName);
        }
        return movement;
    }

    /** 只有一段动作时的默认动作，对应原版 {@code playByIndex(0)}。 */
    public Movement onlyMovement() {
        if (movements.size() != 1) {
            throw new IllegalStateException(
                    "Armature " + name + " has " + movements.size() + " movements");
        }
        return movements.values().iterator().next();
    }

    /** 显示对象的锚点；{@code texture_data} 缺项时回落到 Cocos 默认中心锚点。 */
    public Texture texture(String textureName) {
        Texture texture = textures.get(stripSuffix(textureName));
        return texture != null ? texture : new Texture(textureName, 0.5f, 0.5f, 0.0f, 0.0f);
    }

    static String stripSuffix(String value) {
        return value.endsWith(".png") ? value.substring(0, value.length() - 4) : value;
    }
}
