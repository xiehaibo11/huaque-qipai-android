package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.util.List;

/**
 * 把原版大厅人物骨架画进大厅主 Canvas。
 *
 * <p>大厅场景层级已固定为“背景 → 独立控件 → 加载遮罩”（见根 AGENTS.md 第 17 章），
 * 所以人物不再引入 {@code TextureView} 图层，而是把 {@link Spine37Runtime} 的三角形
 * 直接交给 {@link Canvas#drawVertices}，与其余大厅控件共用同一个页面变换。
 */
final class LobbyCharacterRenderer {

    private final Spine37Runtime runtime;
    private final Bitmap page;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final LobbyCharacterLayout.Placement placement;

    private float[] screenVertices = new float[0];
    private float[] textureCoordinates = new float[0];

    LobbyCharacterRenderer(Spine37Runtime runtime, Bitmap page, String restAnimation) {
        this.runtime = runtime;
        this.page = page;
        this.placement = placeFrom(runtime, restAnimation);
        BitmapShader shader =
                new BitmapShader(page, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        shader.setLocalMatrix(new Matrix());
        paint.setShader(shader);
    }

    /**
     * 按静止姿势的实际绘制包围盒求落位，而不是用骨架文件里的 setup 包围盒。
     *
     * <p>setup 包围盒包含未挂载或不可见的附件，会让人物整体偏小。
     */
    private static LobbyCharacterLayout.Placement placeFrom(
            Spine37Runtime runtime, String restAnimation) {
        float left = Float.MAX_VALUE;
        float bottom = Float.MAX_VALUE;
        float right = -Float.MAX_VALUE;
        float top = -Float.MAX_VALUE;
        for (Spine37Runtime.DrawCommand command : runtime.sample(restAnimation, 0.0f)) {
            float[] vertices = command.vertices();
            for (int index = 0; index < vertices.length; index += 2) {
                left = Math.min(left, vertices[index]);
                right = Math.max(right, vertices[index]);
                bottom = Math.min(bottom, vertices[index + 1]);
                top = Math.max(top, vertices[index + 1]);
            }
        }
        if (left > right || bottom > top) {
            throw new IllegalStateException("Lobby character produced no drawable geometry");
        }
        return LobbyCharacterLayout.place(left, bottom, right - left, top - bottom);
    }

    /**
     * @param animationName 当前状态对应的原版动画名
     * @param animationTime 该状态的累计秒数，超出动画时长由运行时自行循环
     */
    void draw(Canvas canvas, String animationName, float animationTime) {
        if (page.isRecycled()) {
            return;
        }
        List<Spine37Runtime.DrawCommand> commands =
                runtime.sample(animationName, animationTime);
        float pageWidth = page.getWidth();
        float pageHeight = page.getHeight();
        for (Spine37Runtime.DrawCommand command : commands) {
            float[] vertices = command.vertices();
            float[] uvs = command.uvs();
            short[] triangles = command.triangles();
            if (vertices.length < 6 || triangles.length < 3) {
                continue;
            }
            ensureCapacity(vertices.length);
            for (int index = 0; index < vertices.length; index += 2) {
                screenVertices[index] = placement.toScreenX(vertices[index]);
                screenVertices[index + 1] = placement.toScreenY(vertices[index + 1]);
                // Canvas 的纹理坐标是位图像素空间，Spine 输出的是归一化图集坐标。
                textureCoordinates[index] = uvs[index] * pageWidth;
                textureCoordinates[index + 1] = uvs[index + 1] * pageHeight;
            }
            paint.setAlpha(Math.round(clamp(command.color().alpha()) * 255.0f));
            canvas.drawVertices(
                    Canvas.VertexMode.TRIANGLES,
                    vertices.length,
                    screenVertices,
                    0,
                    textureCoordinates,
                    0,
                    null,
                    0,
                    triangles,
                    0,
                    triangles.length,
                    paint);
        }
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private void ensureCapacity(int length) {
        if (screenVertices.length < length) {
            screenVertices = new float[length];
            textureCoordinates = new float[length];
        }
    }

    void recycle() {
        paint.setShader(null);
        if (!page.isRecycled()) {
            page.recycle();
        }
    }
}
