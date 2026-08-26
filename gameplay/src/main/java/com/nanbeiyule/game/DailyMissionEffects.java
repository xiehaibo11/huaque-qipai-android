package com.nanbeiyule.game;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import com.nanbeiyule.game.spine37.Spine37Data;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 原版 LuckyMissionView 的六套骨骼动画，绘制在与面板同一张 1920x1080 画布上。
 *
 * <p>骨架坐标是 Y 轴向上的 Spine 世界坐标，这里按 CSB 节点位置换算成顶部原点的舞台坐标，
 * 与面板位图共用同一个 contain 变换和同一次裁剪，因此列表里的特效会跟着任务条滚动和裁切。
 */
final class DailyMissionEffects {
    /** _KW_ANI_BGTOP：顶部扫光。 */
    static final String TOP_SWEEP = "zzb_ty_shanguang";
    /** _KW_ANI_ICONPANEL：活跃值牌星光。 */
    static final String ACTIVITY_STARS = "zzb_ty_xingguang";
    /** _KW_ANI_ICON：活跃值图标流光。 */
    static final String ACTIVITY_ICON_FLOW = "zzb_ty_jlliuguang";
    /** _KW_ANI_PRO：活跃度进度条光点。 */
    static final String PROGRESS_HEAD = "zzb_ty_jdtgd";
    /** 里程碑 KW_ANI：可领取闪光。 */
    static final String MILESTONE_READY = "zzb_ty_xxgx";
    /** 领取按钮 KW_ANI：按钮扫光。 */
    static final String BUTTON_SWEEP = "zzb_ty_ansg";

    private static final String ASSET_ROOT = "daily_mission_effects";
    private static final String ANIMATION = "animation";
    private static final String[] SKELETONS = {
        TOP_SWEEP, ACTIVITY_STARS, ACTIVITY_ICON_FLOW,
        PROGRESS_HEAD, MILESTONE_READY, BUTTON_SWEEP
    };

    private final Map<String, OriginalLobbyEffectAssets.Loaded> skeletons =
            new LinkedHashMap<>();
    private final Map<Bitmap, BitmapShader> shaders = new LinkedHashMap<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final PorterDuffXfermode additive = new PorterDuffXfermode(PorterDuff.Mode.ADD);
    private final boolean available;

    DailyMissionEffects(AssetManager assets) {
        boolean loaded = true;
        for (String name : SKELETONS) {
            try {
                skeletons.put(
                        name,
                        OriginalLobbyEffectAssets.load(assets, ASSET_ROOT + "/" + name, name));
            } catch (IOException | RuntimeException exception) {
                loaded = false;
                break;
            }
        }
        if (!loaded) skeletons.clear();
        available = loaded;
    }

    /** 资源缺失时整面板仍按静态位图渲染，不因为动效失败而黑屏。 */
    boolean available() {
        return available;
    }

    void draw(
            Canvas canvas,
            String skeleton,
            float elapsedSeconds,
            float anchorX,
            float anchorY,
            float scaleX,
            float scaleY) {
        OriginalLobbyEffectAssets.Loaded effect = skeletons.get(skeleton);
        if (effect == null) return;
        List<Spine37Runtime.DrawCommand> commands;
        try {
            commands = effect.runtime().sample(ANIMATION, elapsedSeconds);
        } catch (RuntimeException exception) {
            return;
        }
        for (Spine37Runtime.DrawCommand command : commands) {
            Bitmap page = effect.pages().get(command.pageName());
            if (page == null || page.isRecycled()) continue;
            drawCommand(canvas, command, page, anchorX, anchorY, scaleX, scaleY);
        }
    }

    private void drawCommand(
            Canvas canvas,
            Spine37Runtime.DrawCommand command,
            Bitmap page,
            float anchorX,
            float anchorY,
            float scaleX,
            float scaleY) {
        float[] world = command.vertices();
        float[] uvs = command.uvs();
        short[] triangles = command.triangles();
        if (triangles.length == 0 || world.length < 6) return;
        float[] positions = new float[world.length];
        float[] texels = new float[world.length];
        for (int index = 0; index < world.length; index += 2) {
            // Spine 世界坐标 Y 向上，舞台坐标 Y 向下。
            positions[index] = anchorX + world[index] * scaleX;
            positions[index + 1] = anchorY - world[index + 1] * scaleY;
            texels[index] = uvs[index] * page.getWidth();
            texels[index + 1] = uvs[index + 1] * page.getHeight();
        }
        Spine37Data.ColorValue color = command.color();
        paint.setShader(shaderFor(page));
        paint.setAlpha(Math.round(Math.max(0f, Math.min(1f, color.alpha())) * 255f));
        boolean add = "additive".equals(command.blend()) || "screen".equals(command.blend());
        paint.setXfermode(add ? additive : null);
        canvas.drawVertices(
                Canvas.VertexMode.TRIANGLES,
                positions.length,
                positions,
                0,
                texels,
                0,
                null,
                0,
                triangles,
                0,
                triangles.length,
                paint);
        paint.setXfermode(null);
        paint.setShader(null);
        paint.setAlpha(255);
    }

    private BitmapShader shaderFor(Bitmap page) {
        BitmapShader shader = shaders.get(page);
        if (shader == null) {
            shader = new BitmapShader(page, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            shaders.put(page, shader);
        }
        return shader;
    }
}
