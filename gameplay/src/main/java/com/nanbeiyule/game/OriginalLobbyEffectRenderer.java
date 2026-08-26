package com.nanbeiyule.game;

import android.opengl.GLES20;
import com.nanbeiyule.game.spine37.Spine37Data;
import com.nanbeiyule.game.spine37.Spine37Projection;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

final class OriginalLobbyEffectRenderer {
    private static final float ORIGINAL_WIDTH = 3200.0f;
    private static final float ORIGINAL_HEIGHT = 1792.0f;

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

    private final List<OriginalLobbyEffectInstance> instances;
    private final long animationStartedNanos = System.nanoTime();

    private int program;
    private int positionLocation;
    private int texCoordLocation;
    private int colorLocation;
    private FloatBuffer positionBuffer;
    private FloatBuffer texCoordBuffer;
    private ShortBuffer indexBuffer;

    OriginalLobbyEffectRenderer(List<OriginalLobbyEffectInstance> instances) {
        this.instances = instances;
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
            recycleInstances();
            throw new IllegalStateException("Unable to link lobby effect program: " + message);
        }
        positionLocation = GLES20.glGetAttribLocation(program, "aPosition");
        texCoordLocation = GLES20.glGetAttribLocation(program, "aTexCoord");
        colorLocation = GLES20.glGetUniformLocation(program, "uColor");
        int samplerLocation = GLES20.glGetUniformLocation(program, "uTexture");
        GLES20.glUseProgram(program);
        GLES20.glUniform1i(samplerLocation, 0);
        for (OriginalLobbyEffectInstance instance : instances) {
            instance.uploadTextures();
        }
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
    }

    void draw(int viewportWidth, int viewportHeight) {
        if (program == 0 || viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }
        GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);

        float elapsedSeconds =
                (System.nanoTime() - animationStartedNanos) / 1_000_000_000.0f;
        GameHomeViewportLayout layout =
                GameHomeViewportLayout.calculate(viewportWidth, viewportHeight);
        AdaptiveViewport.Transform designTransform =
                layout.pageTransform();
        for (OriginalLobbyEffectInstance instance : instances) {
            OriginalLobbyEffectSpec spec = instance.spec();
            Spine37Projection projection =
                    Spine37Projection.fromCenteredPixelTransform(
                            viewportWidth,
                            viewportHeight,
                            designTransform.scaleX() * spec.scale(),
                            designTransform.scaleY() * spec.scale(),
                            designTransform.mapX(spec.anchorX()),
                            designTransform.mapY(spec.anchorY()));
            List<Spine37Runtime.DrawCommand> commands =
                    instance.runtime()
                            .sample(spec.animationName(), elapsedSeconds);
            for (Spine37Runtime.DrawCommand command : commands) {
                if (!spec.drawsAttachment(command.attachmentName())) {
                    continue;
                }
                drawCommand(instance, projection, command);
            }
        }
    }

    private void drawCommand(
            OriginalLobbyEffectInstance instance,
            Spine37Projection projection,
            Spine37Runtime.DrawCommand command) {
        Integer texture = instance.texture(command.pageName());
        if (texture == null) {
            return;
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
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
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
                positionLocation, 2, GLES20.GL_FLOAT, false, 0, positionBuffer);
        GLES20.glEnableVertexAttribArray(texCoordLocation);
        GLES20.glVertexAttribPointer(
                texCoordLocation, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                triangles.length,
                GLES20.GL_UNSIGNED_SHORT,
                indexBuffer);
    }

    void release() {
        for (OriginalLobbyEffectInstance instance : instances) {
            instance.releaseTextures();
        }
        if (program != 0) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
    }

    private void recycleInstances() {
        for (OriginalLobbyEffectInstance instance : instances) {
            instance.recyclePendingBitmaps();
        }
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
            throw new IllegalStateException("Unable to compile lobby effect shader: " + message);
        }
        return shader;
    }

    private static FloatBuffer floatBuffer(FloatBuffer current, int requiredFloats) {
        if (current == null || current.capacity() < requiredFloats) {
            return ByteBuffer.allocateDirect(requiredFloats * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
        }
        current.clear();
        return current;
    }

    private static ShortBuffer shortBuffer(ShortBuffer current, int requiredShorts) {
        if (current == null || current.capacity() < requiredShorts) {
            return ByteBuffer.allocateDirect(requiredShorts * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
        }
        current.clear();
        return current;
    }
}
