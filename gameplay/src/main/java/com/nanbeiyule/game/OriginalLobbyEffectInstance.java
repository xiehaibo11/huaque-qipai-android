package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.util.LinkedHashMap;
import java.util.Map;

/** One recovered spine effect instance with its GL texture state. */
final class OriginalLobbyEffectInstance {
    private final OriginalLobbyEffectSpec spec;
    private final Spine37Runtime runtime;
    private final Map<String, Bitmap> pageBitmaps;
    private final Map<String, Integer> textures = new LinkedHashMap<>();

    OriginalLobbyEffectInstance(
            OriginalLobbyEffectAssets.Loaded loaded,
            OriginalLobbyEffectSpec spec) {
        this.spec = spec;
        runtime = loaded.runtime();
        pageBitmaps = new LinkedHashMap<>(loaded.pages());
    }

    OriginalLobbyEffectSpec spec() {
        return spec;
    }

    Spine37Runtime runtime() {
        return runtime;
    }

    Integer texture(String pageName) {
        return textures.get(pageName);
    }

    void uploadTextures() {
        for (Map.Entry<String, Bitmap> entry : pageBitmaps.entrySet()) {
            int[] ids = new int[1];
            GLES20.glGenTextures(1, ids, 0);
            if (ids[0] == 0) {
                recyclePendingBitmaps();
                throw new IllegalStateException(
                        "Unable to allocate lobby effect texture");
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0]);
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, entry.getValue(), 0);
            textures.put(entry.getKey(), ids[0]);
            entry.getValue().recycle();
        }
        pageBitmaps.clear();
    }

    void releaseTextures() {
        if (!textures.isEmpty()) {
            int[] ids = new int[textures.size()];
            int index = 0;
            for (Integer texture : textures.values()) {
                ids[index++] = texture;
            }
            GLES20.glDeleteTextures(ids.length, ids, 0);
            textures.clear();
        }
        recyclePendingBitmaps();
    }

    void recyclePendingBitmaps() {
        for (Bitmap bitmap : pageBitmaps.values()) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        pageBitmaps.clear();
    }
}
