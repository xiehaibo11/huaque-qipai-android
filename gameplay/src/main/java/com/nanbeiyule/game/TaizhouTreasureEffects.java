package com.nanbeiyule.game;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
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

/** Canvas renderer for the eight recovered Spine sets used by the JuBaoPen flow. */
final class TaizhouTreasureEffects {
    static final String MAIN = "zzb_jbp_zjm";
    static final String RESULT = "zzb_jbp_gchd";
    static final String QUALITY = "zzb_jbp_jlgx";
    static final String ENTRY_LIGHT = "zzb_ty_tcbk4";
    static final String BUTTON_SWEEP = "zzb_ty_ansg2";
    static final String RESULT_ITEM = "zzb_ty_bhxz";
    static final String ACQUIRED_FLASH = "zzb_ty_xgaung2";
    static final String FORTUNE_FLOW = "zzb_ty_liuhuo";

    private static final String ASSET_ROOT = "taizhou_treasure_effects";
    private static final String[] SKELETONS = {
        MAIN, RESULT, QUALITY, ENTRY_LIGHT, BUTTON_SWEEP, RESULT_ITEM,
        ACQUIRED_FLASH, FORTUNE_FLOW
    };

    private final Map<String, OriginalLobbyEffectAssets.Loaded> effects =
            new LinkedHashMap<>();
    private final Map<Bitmap, BitmapShader> shaders = new LinkedHashMap<>();
    private final Map<Integer, LightingColorFilter> colorFilters = new LinkedHashMap<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final PorterDuffXfermode additive = new PorterDuffXfermode(PorterDuff.Mode.ADD);

    TaizhouTreasureEffects(AssetManager assets) {
        for (String name : SKELETONS) {
            try {
                effects.put(
                        name,
                        OriginalLobbyEffectAssets.load(
                                assets, ASSET_ROOT + "/" + name, name));
            } catch (IOException | RuntimeException ignored) {
                // Static original bitmaps remain usable if an animation asset is corrupt.
            }
        }
    }

    void draw(
            Canvas canvas,
            String baseName,
            String animation,
            float elapsedSeconds,
            float anchorX,
            float anchorY,
            float scaleX,
            float scaleY) {
        OriginalLobbyEffectAssets.Loaded effect = effects.get(baseName);
        if (effect == null) return;
        List<Spine37Runtime.DrawCommand> commands;
        try {
            commands = effect.runtime().sample(animation, Math.max(0.0f, elapsedSeconds));
        } catch (RuntimeException ignored) {
            return;
        }
        for (Spine37Runtime.DrawCommand command : commands) {
            Bitmap page = effect.pages().get(command.pageName());
            if (page == null || page.isRecycled()) continue;
            drawCommand(canvas, command, page, anchorX, anchorY, scaleX, scaleY);
        }
    }

    void drawOnce(
            Canvas canvas,
            String baseName,
            String animation,
            float elapsedSeconds,
            float anchorX,
            float anchorY,
            float scaleX,
            float scaleY) {
        float duration = animationDuration(baseName, animation);
        float sample = duration > 0.0f
                ? Math.min(Math.max(0.0f, elapsedSeconds), Math.max(0.0f, duration - 0.0001f))
                : Math.max(0.0f, elapsedSeconds);
        draw(canvas, baseName, animation, sample, anchorX, anchorY, scaleX, scaleY);
    }

    float animationDuration(String baseName, String animation) {
        OriginalLobbyEffectAssets.Loaded effect = effects.get(baseName);
        if (effect == null) return 0.0f;
        try {
            return effect.runtime().animationDuration(animation);
        } catch (RuntimeException ignored) {
            return 0.0f;
        }
    }

    float eventTime(String baseName, String animation, String eventName) {
        OriginalLobbyEffectAssets.Loaded effect = effects.get(baseName);
        if (effect == null) return Float.NaN;
        try {
            return effect.runtime().eventTime(animation, eventName);
        } catch (RuntimeException ignored) {
            return Float.NaN;
        }
    }

    void release() {
        for (OriginalLobbyEffectAssets.Loaded effect : effects.values()) {
            for (Bitmap bitmap : effect.pages().values()) {
                if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            }
        }
        effects.clear();
        shaders.clear();
        colorFilters.clear();
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
            positions[index] = anchorX + world[index] * scaleX;
            positions[index + 1] = anchorY - world[index + 1] * scaleY;
            texels[index] = uvs[index] * page.getWidth();
            texels[index + 1] = uvs[index + 1] * page.getHeight();
        }
        Spine37Data.ColorValue color = command.color();
        int rgb =
                Color.rgb(
                        Math.round(clamp(color.red()) * 255.0f),
                        Math.round(clamp(color.green()) * 255.0f),
                        Math.round(clamp(color.blue()) * 255.0f));
        paint.setShader(shaderFor(page));
        paint.setColorFilter(colorFilters.computeIfAbsent(rgb, value -> new LightingColorFilter(value, 0)));
        paint.setAlpha(Math.round(clamp(color.alpha()) * 255.0f));
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
        paint.setColorFilter(null);
        paint.setShader(null);
        paint.setAlpha(255);
    }

    private BitmapShader shaderFor(Bitmap page) {
        return shaders.computeIfAbsent(
                page,
                bitmap -> new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
