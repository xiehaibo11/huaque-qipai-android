package com.nanbeiyule.game.cocosarmature;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采样并绘制一段原版 Armature 动作。
 *
 * <p>骨骼矩阵按 {@code cocos2d::TransformHelp::nodeToMatrix} 还原：
 * {@code a=cX·cos(kY)}、{@code b=cX·sin(kY)}、{@code c=-cY·sin(kX)}、{@code d=cY·cos(kX)}，
 * 平移取骨骼当前 {@code x/y}。原版 Y 轴向上，这里在最外层乘一次翻转矩阵换算到 Android
 * 画布坐标，位图自身再用 {@code A} 矩阵把像素空间映射回骨骼空间，避免整体镜像。
 */
public final class ArmaturePlayer {
    /** 原版 Armature 的基准帧率（{@code ArmatureDataManager} 默认 60）。 */
    private static final float BASE_FRAME_RATE = 60.0f;

    private final ArmatureData data;
    private final ArmatureAtlas atlas;
    private final ArmatureData.Movement movement;
    private final Map<String, ArmatureData.BoneTrack> tracks = new HashMap<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix world = new Matrix();
    private final Matrix local = new Matrix();
    private final float[] values = new float[9];

    public ArmaturePlayer(ArmatureData data, ArmatureAtlas atlas) {
        this(data, atlas, data.onlyMovement());
    }

    public ArmaturePlayer(
            ArmatureData data, ArmatureAtlas atlas, ArmatureData.Movement movement) {
        this.data = data;
        this.atlas = atlas;
        this.movement = movement;
        for (ArmatureData.BoneTrack track : movement.tracks()) {
            tracks.put(track.name(), track);
        }
    }

    /** 一次循环的秒长：{@code dr} 帧除以基准帧率再除以 movement 的播放缩放 {@code sc}。 */
    public float durationSeconds() {
        float scale = movement.scale() > 0.0f ? movement.scale() : 1.0f;
        return movement.durationFrames() / (BASE_FRAME_RATE * scale);
    }

    /**
     * 把动画绘制到画布。
     *
     * @param originX 骨架原点的设计坐标 X
     * @param originY 骨架原点的设计坐标 Y（顶部原点）
     * @param scale 整体缩放，1 表示按原版像素
     */
    public void draw(
            Canvas canvas, float elapsedSeconds, float originX, float originY, float scale) {
        float duration = durationSeconds();
        float time = duration <= 0.0f ? 0.0f : elapsedSeconds % duration;
        if (!movement.loop() && elapsedSeconds >= duration) {
            time = duration;
        }
        float frame = time * BASE_FRAME_RATE
                * (movement.scale() > 0.0f ? movement.scale() : 1.0f);
        for (ArmatureData.Bone bone : data.bones()) {
            drawBone(canvas, bone, frame, originX, originY, scale);
        }
    }

    private void drawBone(
            Canvas canvas,
            ArmatureData.Bone bone,
            float frame,
            float originX,
            float originY,
            float scale) {
        ArmatureData.Keyframe sample = sample(bone, frame);
        List<String> displays = bone.displays();
        if (displays.isEmpty()) {
            return;
        }
        int displayIndex = Math.max(0, Math.min(sample.displayIndex(), displays.size() - 1));
        Bitmap sprite = atlas.sprite(displays.get(displayIndex));
        if (sprite == null || sprite.isRecycled()) {
            return;
        }
        // 骨骼在 Cocos 空间的绝对变换：静态 bone_data 位移叠加当前帧位移。
        float x = bone.x() + sample.x();
        float y = bone.y() + sample.y();
        float scaleX = bone.scaleX() * sample.scaleX();
        float scaleY = bone.scaleY() * sample.scaleY();
        float skewX = bone.skewX() + sample.skewX();
        float skewY = bone.skewY() + sample.skewY();
        float a = (float) (scaleX * Math.cos(skewY));
        float b = (float) (scaleX * Math.sin(skewY));
        float c = (float) (-scaleY * Math.sin(skewX));
        float d = (float) (scaleY * Math.cos(skewX));

        ArmatureData.Texture texture = data.texture(displays.get(displayIndex));
        float width = sprite.getWidth();
        float height = sprite.getHeight();

        // A：位图像素空间（Y 向下）→ 骨骼空间（Y 向上），锚点落在骨骼原点。
        values[0] = 1.0f; values[1] = 0.0f; values[2] = -texture.anchorX() * width;
        values[3] = 0.0f; values[4] = -1.0f; values[5] = height * (1.0f - texture.anchorY());
        values[6] = 0.0f; values[7] = 0.0f; values[8] = 1.0f;
        local.setValues(values);

        // M：骨骼变换（Cocos 仿射）。
        values[0] = a; values[1] = c; values[2] = x;
        values[3] = b; values[4] = d; values[5] = y;
        values[6] = 0.0f; values[7] = 0.0f; values[8] = 1.0f;
        world.setValues(values);
        world.preConcat(local);

        // F：Cocos（Y 向上）→ 设计坐标（Y 向下）并整体缩放。
        values[0] = scale; values[1] = 0.0f; values[2] = originX;
        values[3] = 0.0f; values[4] = -scale; values[5] = originY;
        values[6] = 0.0f; values[7] = 0.0f; values[8] = 1.0f;
        local.setValues(values);
        local.preConcat(world);
        canvas.drawBitmap(sprite, local, paint);
    }

    /** 在骨骼轨道上取样；缺轨道时回落到静态姿态。 */
    private ArmatureData.Keyframe sample(ArmatureData.Bone bone, float frame) {
        ArmatureData.BoneTrack track = tracks.get(bone.name());
        if (track == null || track.keyframes().isEmpty()) {
            return ArmatureKeyframes.identity();
        }
        return ArmatureKeyframes.sample(track.keyframes(), frame - track.delay());
    }

    public void recycle() {
        atlas.recycle();
    }
}
