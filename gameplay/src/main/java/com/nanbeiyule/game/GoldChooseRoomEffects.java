package com.nanbeiyule.game;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import com.nanbeiyule.game.goldroom.GoldHallActEntry;
import com.nanbeiyule.game.spine37.Spine37Data;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 选场页的原版骨骼动画，绘制在 1920x1080 设计画布上。
 *
 * <p>卡面骨架取自 {@code ChooseRoom.lua:452-456}：每张卡挂 {@code zzb_jbdt_xfsg} 常驻动画，
 * 选中卡另挂 {@code zzb_jbdt_tjxf}。右上「福利任务」入口的图标本身就是骨骼
 * {@code zzb_flrw_rk}（{@code LuckyMissionBtn.lua:17}），CSB 里的静态帧是隐藏的。
 * 三套骨架各自独立装载，任何一套缺失都不影响其它，动画名都是 {@code animation}。
 * 卡面两套缺失时 {@link #available()} 为 false，调用方退回描边高亮。
 */
final class GoldChooseRoomEffects {
    /** {@code _ani} 常驻挂点。 */
    static final String AMBIENT = "zzb_jbdt_xfsg";

    /** {@code _aniSelect} 选中挂点。 */
    static final String SELECTED = "zzb_jbdt_tjxf";

    /** 三个骨架的动画名，原版 {@code Utils:addSpine(..., "animation")}。 */
    static final String ANIMATION = "animation";

    private static final String ASSET_ROOT = "gold_choose_room_effects";
    private static final String[] CARD_SKELETONS = {AMBIENT, SELECTED};

    private final Map<String, OriginalLobbyEffectAssets.Loaded> skeletons =
            new LinkedHashMap<>();
    private final Map<Bitmap, BitmapShader> shaders = new LinkedHashMap<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final PorterDuffXfermode additive = new PorterDuffXfermode(PorterDuff.Mode.ADD);

    GoldChooseRoomEffects(AssetManager assets) {
        load(assets, AMBIENT);
        load(assets, SELECTED);
        for (GoldHallActEntry entry : GoldHallActEntry.values()) {
            if (entry.spineSkeleton() != null) {
                load(assets, entry.spineSkeleton());
            }
        }
    }

    private void load(AssetManager assets, String name) {
        try {
            skeletons.put(
                    name, OriginalLobbyEffectAssets.load(assets, ASSET_ROOT + "/" + name, name));
        } catch (IOException | RuntimeException exception) {
            skeletons.remove(name);
        }
    }

    /** 卡面两套骨架都在，选中反馈才走骨骼。 */
    boolean available() {
        for (String name : CARD_SKELETONS) {
            if (!skeletons.containsKey(name)) {
                return false;
            }
        }
        return true;
    }

    /** 单套骨架是否可用；活动入口按自己那套判断，不受卡面骨架影响。 */
    boolean has(String skeleton) {
        return skeleton != null && skeletons.containsKey(skeleton);
    }

    /**
     * 按挂点中心绘制一帧。Spine 世界坐标 Y 向上、舞台坐标 Y 向下，换算与其它原版特效一致。
     * {@code scaleY} 单列是因为 {@code _aniSelect} 在 CSB 里被压成 0.82。
     */
    void draw(
            Canvas canvas,
            String skeleton,
            float elapsedSeconds,
            float anchorX,
            float anchorY,
            float scaleX,
            float scaleY) {
        OriginalLobbyEffectAssets.Loaded effect = skeletons.get(skeleton);
        if (effect == null) {
            return;
        }
        List<Spine37Runtime.DrawCommand> commands;
        try {
            commands = effect.runtime().sample(ANIMATION, elapsedSeconds);
        } catch (RuntimeException exception) {
            return;
        }
        for (Spine37Runtime.DrawCommand command : commands) {
            Bitmap page = effect.pages().get(command.pageName());
            if (page == null || page.isRecycled()) {
                continue;
            }
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
        if (triangles.length == 0 || world.length < 6) {
            return;
        }
        float[] positions = new float[world.length];
        float[] texels = new float[world.length];
        for (int index = 0; index < world.length; index += 2) {
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
    }

    private BitmapShader shaderFor(Bitmap page) {
        return shaders.computeIfAbsent(
                page,
                bitmap ->
                        new BitmapShader(
                                bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
    }

    void release() {
        shaders.clear();
        for (Entry<String, OriginalLobbyEffectAssets.Loaded> entry : skeletons.entrySet()) {
            for (Bitmap page : entry.getValue().pages().values()) {
                if (page != null && !page.isRecycled()) {
                    page.recycle();
                }
            }
        }
        skeletons.clear();
    }
}
