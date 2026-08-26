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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 等待桌底部三个图标的原版骨骼动画，绘制在牌桌那张 1920x1080 画布上。
 *
 * <p>骨架与动画名的对应关系见 {@link TaizhouIconAnimationSelection}；本类只负责 Canvas 合成，
 * 装载目录与逐帧播放拆给 {@link TaizhouWaitingIconSpineCatalog} 与
 * {@link TaizhouWaitingIconSpinePlayer}。三个骨架任一不可动画时 {@link #available()} 为 false，
 * 调用方整体回退静态位图（回退输出与此前的合成管线逐像素一致），不因为动效失败而丢按钮；
 * 具体哪些骨架被降级可经 {@link #degradedSkeletons()} 查询。
 */
final class TaizhouWaitingIconEffects {
    /** 请财神 {@code Guide/GamePropView.lua}，`CaiShenIcon.csb` 的挂点动画。 */
    static final String CAISHEN = "zzb_qcs_icon";
    /** 聚宝盆 {@code JuBaoPen/JuBaoPenIconView.lua:10}。 */
    static final String TREASURE_POT = "zzb_jbp_icon";
    /** 福利任务 {@code LuckyMission/IconView.lua:11-15}。 */
    static final String LUCKY_MISSION = "zzb_flrw_icon";

    /** 请财神的循环动画名，原版 `playAni(..., "loop", true)`。 */
    static final String CAISHEN_ANIMATION = "loop";
    /** 聚宝盆的循环动画名，原版 `playAni(..., "animation", true)`。 */
    static final String TREASURE_POT_ANIMATION = "animation";

    private static final String ASSET_ROOT = "taizhou_waiting_icons";
    private static final List<String> SKELETONS = List.of(CAISHEN, TREASURE_POT, LUCKY_MISSION);

    private final TaizhouWaitingIconSpineCatalog catalog;
    private final TaizhouWaitingIconSpinePlayer player;
    private final Map<Bitmap, BitmapShader> shaders = new LinkedHashMap<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final PorterDuffXfermode additive = new PorterDuffXfermode(PorterDuff.Mode.ADD);

    TaizhouWaitingIconEffects(AssetManager assets) {
        catalog = TaizhouWaitingIconSpineCatalog.load(assets, ASSET_ROOT, SKELETONS);
        player = new TaizhouWaitingIconSpinePlayer(catalog);
    }

    /** 三个骨架全部可动画才进入动画分支，保证回退时三图标整体回到静态位图。 */
    boolean available() {
        return catalog.allAvailable(SKELETONS);
    }

    /** 被运行时拒绝或资源缺失的骨架清单（按声明顺序），全部健康时为空。 */
    List<String> degradedSkeletons() {
        return catalog.degradedSkeletons();
    }

    /** 视图不可见时冻结动画时钟；可见性接线由牌桌视图所有者负责。 */
    void pause() {
        player.pause();
    }

    void resume() {
        player.resume();
    }

    /**
     * 按图标中心绘制一帧。
     *
     * <p>{@code anchorX/anchorY} 是图标在 1920x1080 设计坐标里的中心；Spine 世界坐标 Y 向上，
     * 舞台坐标 Y 向下，这里与 {@code DailyMissionEffects} 使用同一套换算。{@code elapsedSeconds}
     * 是调用方墙钟，播放器按增量积分成动画时间轴，暂停时冻结。
     */
    void draw(
            Canvas canvas,
            String skeleton,
            String animation,
            float elapsedSeconds,
            float anchorX,
            float anchorY,
            float scale) {
        player.advanceTo(elapsedSeconds);
        OriginalLobbyEffectAssets.Loaded effect = catalog.loaded(skeleton);
        if (effect == null) {
            return;
        }
        List<Spine37Runtime.DrawCommand> commands;
        try {
            commands = player.sample(skeleton, animation);
        } catch (RuntimeException exception) {
            return;
        }
        for (Spine37Runtime.DrawCommand command : commands) {
            Bitmap page = effect.pages().get(command.pageName());
            if (page == null || page.isRecycled()) {
                continue;
            }
            drawCommand(canvas, command, page, anchorX, anchorY, scale);
        }
    }

    private void drawCommand(
            Canvas canvas,
            Spine37Runtime.DrawCommand command,
            Bitmap page,
            float anchorX,
            float anchorY,
            float scale) {
        float[] world = command.vertices();
        float[] uvs = command.uvs();
        short[] triangles = command.triangles();
        if (triangles.length == 0 || world.length < 6) {
            return;
        }
        float[] positions = new float[world.length];
        float[] texels = new float[world.length];
        for (int index = 0; index < world.length; index += 2) {
            positions[index] = anchorX + world[index] * scale;
            positions[index + 1] = anchorY - world[index + 1] * scale;
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
    }

    private BitmapShader shaderFor(Bitmap page) {
        return shaders.computeIfAbsent(
                page, bitmap -> new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
    }

    void release() {
        shaders.clear();
        catalog.clear();
    }
}
