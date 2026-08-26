package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import com.nanbeiyule.game.spine37.Spine37Data;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Original weekly login-gift shell; reward content remains server-owned. */
final class LoginGiftPanelRenderer {
    private static final float STAGE_WIDTH = 1700f;
    private static final float STAGE_HEIGHT = 1000f;
    private static final float SPINE_X = 789.990112f;
    private static final float SPINE_Y = 503f;
    private static final float[] DAY_X = {311.9406f, 603.9406f, 895.9406f};
    private static final float[] DAY_Y = {375.0691f, 684.1843f};

    private final OriginalLobbyEffectAssets.Loaded effect;
    private final Bitmap dayCard;
    private final Map<Bitmap, BitmapShader> shaders = new LinkedHashMap<>();
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PorterDuffXfermode additive = new PorterDuffXfermode(PorterDuff.Mode.ADD);

    LoginGiftPanelRenderer(Context context) {
        OriginalLobbyEffectAssets.Loaded loaded = null;
        Bitmap card = null;
        try {
            loaded = OriginalLobbyEffectAssets.load(
                    context.getAssets(), "login_gift", "zzb_hdzx_dlyl");
        } catch (IOException | RuntimeException ignored) {
        }
        try {
            Bitmap atlas = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.login_gift_day_sign_atlas);
            Bitmap stored = Bitmap.createBitmap(atlas, 1, 369, 306, 259);
            Matrix rotation = new Matrix();
            rotation.postRotate(-90f);
            card = Bitmap.createBitmap(stored, 0, 0, 306, 259, rotation, true);
        } catch (RuntimeException ignored) {
        }
        effect = loaded;
        dayCard = card;
        textPaint.setTypeface(
                Typeface.createFromAsset(context.getAssets(), "fonts/zihun_jingdian_lihei.ttf"));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    boolean draw(Canvas canvas, AdaptiveViewport.Rect bounds, float elapsedSeconds) {
        float scale = Math.min(bounds.width() / STAGE_WIDTH, bounds.height() / STAGE_HEIGHT);
        float left = bounds.centerX() - STAGE_WIDTH * scale * 0.5f;
        float top = bounds.centerY() - STAGE_HEIGHT * scale * 0.5f;
        int save = canvas.save();
        canvas.clipRect(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        drawSpine(canvas, left + SPINE_X * scale, top + SPINE_Y * scale, scale,
                elapsedSeconds);
        drawDayCards(canvas, left, top, scale);
        drawUnavailableState(canvas, left, top, scale);
        canvas.restoreToCount(save);
        return effect != null;
    }

    private void drawSpine(
            Canvas canvas, float anchorX, float anchorY, float scale, float elapsedSeconds) {
        if (effect == null) return;
        List<Spine37Runtime.DrawCommand> commands;
        try {
            commands = effect.runtime().sample("loop", elapsedSeconds);
        } catch (RuntimeException exception) {
            return;
        }
        for (Spine37Runtime.DrawCommand command : commands) {
            Bitmap page = effect.pages().get(command.pageName());
            if (page == null || page.isRecycled()) continue;
            float[] world = command.vertices();
            float[] uvs = command.uvs();
            short[] triangles = command.triangles();
            if (triangles.length == 0 || world.length < 6) continue;
            float[] positions = new float[world.length];
            float[] texels = new float[world.length];
            for (int index = 0; index < world.length; index += 2) {
                positions[index] = anchorX + world[index] * scale;
                positions[index + 1] = anchorY - world[index + 1] * scale;
                texels[index] = uvs[index] * page.getWidth();
                texels[index + 1] = uvs[index + 1] * page.getHeight();
            }
            Spine37Data.ColorValue color = command.color();
            bitmapPaint.setShader(shaderFor(page));
            bitmapPaint.setAlpha(Math.round(Math.max(0f, Math.min(1f, color.alpha())) * 255f));
            boolean add = "additive".equals(command.blend()) || "screen".equals(command.blend());
            bitmapPaint.setXfermode(add ? additive : null);
            canvas.drawVertices(Canvas.VertexMode.TRIANGLES, positions.length, positions, 0,
                    texels, 0, null, 0, triangles, 0, triangles.length, bitmapPaint);
            bitmapPaint.setXfermode(null);
            bitmapPaint.setShader(null);
            bitmapPaint.setAlpha(255);
        }
    }

    private void drawDayCards(Canvas canvas, float left, float top, float scale) {
        textPaint.setColor(Color.rgb(157, 70, 0));
        textPaint.setTextSize(36f * scale);
        textPaint.setShadowLayer(scale, 2f * scale, 2f * scale, Color.rgb(110, 110, 110));
        for (int index = 0; index < 6; index++) {
            float x = left + DAY_X[index % 3] * scale;
            float y = top + DAY_Y[index / 3] * scale;
            if (dayCard != null) {
                canvas.drawBitmap(dayCard, null,
                        new RectF(x - 129.5f * scale, y - 153f * scale,
                                x + 129.5f * scale, y + 153f * scale), bitmapPaint);
            }
            float labelY = y - 112.5441f * scale - (textPaint.ascent() + textPaint.descent()) * 0.5f;
            canvas.drawText("第" + (index + 1) + "天", x, labelY, textPaint);
        }
        textPaint.clearShadowLayer();
    }

    private void drawUnavailableState(Canvas canvas, float left, float top, float scale) {
        textPaint.setColor(Color.rgb(116, 76, 54));
        textPaint.setTextSize(31f * scale);
        textPaint.setShadowLayer(scale, scale, scale, Color.WHITE);
        canvas.drawText("登录有礼服务暂未开放", left + 1289f * scale,
                top + 877f * scale, textPaint);
        textPaint.clearShadowLayer();
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
