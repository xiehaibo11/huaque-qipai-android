package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.nanbeiyule.game.spine37.Spine37Data;
import com.nanbeiyule.game.spine37.Spine37Projection;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** OpenGL ES 2.0 renderer for the recovered agreement hand animation. */
final class LoginAgreementHintRenderer {
    static final String ANIMATION_NAME = "ShouDianJi";

    private static final String VERTEX_SHADER =
            "attribute vec2 aPosition;\n"
                    + "attribute vec2 aTexCoord;\n"
                    + "varying vec2 vTexCoord;\n"
                    + "void main() {\n"
                    + "  gl_Position = vec4(aPosition, 0.0, 1.0);\n"
                    + "  vTexCoord = aTexCoord;\n"
                    + "}\n";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "uniform sampler2D uTexture;\n"
                    + "uniform vec4 uColor;\n"
                    + "varying vec2 vTexCoord;\n"
                    + "void main() {\n"
                    + "  gl_FragColor = texture2D(uTexture, vTexCoord) * uColor;\n"
                    + "}\n";

    private final Spine37Runtime runtime;
    private final Map<String, Bitmap> pageBitmaps;
    private final Map<String, Integer> textures = new LinkedHashMap<>();

    private int program;
    private int positionLocation;
    private int texCoordLocation;
    private int colorLocation;
    private FloatBuffer positionBuffer;
    private FloatBuffer texCoordBuffer;
    private ShortBuffer indexBuffer;

    LoginAgreementHintRenderer(LoginAgreementHintAssets.Loaded assets) {
        runtime = assets.runtime();
        pageBitmaps = new LinkedHashMap<>(assets.pages());
    }

    void initialize() {
        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        if (status[0] == 0) {
            String message = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            program = 0;
            recyclePendingBitmaps();
            throw new IllegalStateException(
                    "Unable to link agreement hint program: " + message);
        }
        positionLocation = GLES20.glGetAttribLocation(program, "aPosition");
        texCoordLocation = GLES20.glGetAttribLocation(program, "aTexCoord");
        colorLocation = GLES20.glGetUniformLocation(program, "uColor");
        int samplerLocation = GLES20.glGetUniformLocation(program, "uTexture");
        GLES20.glUseProgram(program);
        GLES20.glUniform1i(samplerLocation, 0);
        uploadTextures();
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
    }

    void draw(int viewportWidth, int viewportHeight, float elapsedSeconds) {
        if (program == 0 || viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }
        clear(viewportWidth, viewportHeight);
        GLES20.glUseProgram(program);
        List<Spine37Runtime.DrawCommand> commands =
                runtime.sample(ANIMATION_NAME, elapsedSeconds);
        Spine37Projection projection =
                LoginAgreementHintPlacement.projection(
                        viewportWidth,
                        viewportHeight);
        for (Spine37Runtime.DrawCommand command : commands) {
            Integer texture = textures.get(command.pageName());
            if (texture == null) {
                continue;
            }
            float[] vertices = projection.toNdc(command.vertices());
            float[] uvs = command.uvs();
            short[] triangles = command.triangles();
            positionBuffer = floatBuffer(positionBuffer, vertices.length);
            texCoordBuffer = floatBuffer(texCoordBuffer, uvs.length);
            indexBuffer = shortBuffer(indexBuffer, triangles.length);
            positionBuffer.put(vertices).position(0);
            texCoordBuffer.put(uvs).position(0);
            indexBuffer.put(triangles).position(0);

            if ("additive".equals(command.blend())) {
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
            } else {
                GLES20.glBlendFunc(
                        GLES20.GL_ONE,
                        GLES20.GL_ONE_MINUS_SRC_ALPHA);
            }
            Spine37Data.ColorValue color = command.color();
            GLES20.glUniform4f(
                    colorLocation,
                    color.red() * color.alpha(),
                    color.green() * color.alpha(),
                    color.blue() * color.alpha(),
                    color.alpha());
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
            GLES20.glEnableVertexAttribArray(positionLocation);
            GLES20.glVertexAttribPointer(
                    positionLocation,
                    2,
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    positionBuffer);
            GLES20.glEnableVertexAttribArray(texCoordLocation);
            GLES20.glVertexAttribPointer(
                    texCoordLocation,
                    2,
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    texCoordBuffer);
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES,
                    triangles.length,
                    GLES20.GL_UNSIGNED_SHORT,
                    indexBuffer);
        }
    }

    void clear(int viewportWidth, int viewportHeight) {
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }
        GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    void release() {
        if (!textures.isEmpty()) {
            int[] ids = new int[textures.size()];
            int index = 0;
            for (Integer texture : textures.values()) {
                ids[index++] = texture;
            }
            GLES20.glDeleteTextures(ids.length, ids, 0);
            textures.clear();
        }
        if (program != 0) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        recyclePendingBitmaps();
    }

    private void uploadTextures() {
        for (Map.Entry<String, Bitmap> entry : pageBitmaps.entrySet()) {
            int[] ids = new int[1];
            GLES20.glGenTextures(1, ids, 0);
            if (ids[0] == 0) {
                recyclePendingBitmaps();
                throw new IllegalStateException(
                        "Unable to allocate agreement hint texture");
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
            GLUtils.texImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    entry.getValue(),
                    0);
            textures.put(entry.getKey(), ids[0]);
            entry.getValue().recycle();
        }
        pageBitmaps.clear();
    }

    private void recyclePendingBitmaps() {
        for (Bitmap bitmap : pageBitmaps.values()) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        pageBitmaps.clear();
    }

    private static int compileShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String message = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new IllegalStateException(
                    "Unable to compile agreement hint shader: " + message);
        }
        return shader;
    }

    private static FloatBuffer floatBuffer(
            FloatBuffer current,
            int requiredFloats) {
        if (current == null || current.capacity() < requiredFloats) {
            return ByteBuffer.allocateDirect(requiredFloats * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
        }
        current.clear();
        return current;
    }

    private static ShortBuffer shortBuffer(
            ShortBuffer current,
            int requiredShorts) {
        if (current == null || current.capacity() < requiredShorts) {
            return ByteBuffer.allocateDirect(requiredShorts * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
        }
        current.clear();
        return current;
    }
}
