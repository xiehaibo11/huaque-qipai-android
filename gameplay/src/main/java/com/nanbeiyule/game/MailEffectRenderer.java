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

final class MailEffectRenderer {
    private static final String VERTEX_SHADER =
            "attribute vec2 aPosition;\nattribute vec2 aTexCoord;\n"
                    + "varying vec2 vTexCoord;\nvoid main(){gl_Position=vec4(aPosition,0.0,1.0);"
                    + "vTexCoord=aTexCoord;}\n";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\nuniform sampler2D uTexture;\nuniform vec4 uColor;\n"
                    + "varying vec2 vTexCoord;\nvoid main(){gl_FragColor="
                    + "texture2D(uTexture,vTexCoord)*uColor;}\n";

    private final MailEffectInstance instance;
    private final long startedNanos = System.nanoTime();
    private int program;
    private int positionLocation;
    private int texCoordLocation;
    private int colorLocation;
    private FloatBuffer positionBuffer;
    private FloatBuffer texCoordBuffer;
    private ShortBuffer indexBuffer;

    MailEffectRenderer(MailEffectInstance instance) {
        this.instance = instance;
    }

    void initialize() {
        int vertex = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertex);
        GLES20.glAttachShader(program, fragment);
        GLES20.glLinkProgram(program);
        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        GLES20.glDeleteShader(vertex);
        GLES20.glDeleteShader(fragment);
        if (status[0] == 0) {
            String message = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            program = 0;
            instance.recyclePendingBitmaps();
            throw new IllegalStateException("Unable to link mail effect program: " + message);
        }
        positionLocation = GLES20.glGetAttribLocation(program, "aPosition");
        texCoordLocation = GLES20.glGetAttribLocation(program, "aTexCoord");
        colorLocation = GLES20.glGetUniformLocation(program, "uColor");
        GLES20.glUseProgram(program);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0);
        instance.uploadTextures();
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
    }

    void draw(int width, int height) {
        if (program == 0 || width <= 0 || height <= 0) {
            return;
        }
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);
        float scale = Math.min(width / MailLayout.DESIGN_WIDTH, height / MailLayout.DESIGN_HEIGHT);
        float offsetX = (width - MailLayout.DESIGN_WIDTH * scale) * 0.5f;
        float offsetY = (height - MailLayout.DESIGN_HEIGHT * scale) * 0.5f;
        MailEffectSpec spec = instance.spec();
        Spine37Projection projection =
                Spine37Projection.fromCenteredPixelTransform(
                        width,
                        height,
                        scale * spec.scale(),
                        offsetX + spec.anchorX() * scale,
                        offsetY + spec.anchorY() * scale);
        float elapsed = (System.nanoTime() - startedNanos) / 1_000_000_000f;
        float entranceDuration = instance.runtime().animationDuration(spec.entranceAnimation());
        String animation = elapsed < entranceDuration
                ? spec.entranceAnimation() : spec.loopAnimation();
        float animationTime = elapsed < entranceDuration ? elapsed : elapsed - entranceDuration;
        for (Spine37Runtime.DrawCommand command
                : instance.runtime().sample(animation, animationTime)) {
            drawCommand(projection, command);
        }
    }

    private void drawCommand(
            Spine37Projection projection, Spine37Runtime.DrawCommand command) {
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
        GLES20.glBlendFunc(
                "additive".equals(command.blend()) ? GLES20.GL_ONE : GLES20.GL_ONE,
                "additive".equals(command.blend())
                        ? GLES20.GL_ONE : GLES20.GL_ONE_MINUS_SRC_ALPHA);
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
        GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, positionBuffer);
        GLES20.glEnableVertexAttribArray(texCoordLocation);
        GLES20.glVertexAttribPointer(texCoordLocation, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, triangles.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    }

    void release() {
        instance.releaseTextures();
        if (program != 0) {
            GLES20.glDeleteProgram(program);
            program = 0;
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
            throw new IllegalStateException("Unable to compile mail effect shader: " + message);
        }
        return shader;
    }

    private static FloatBuffer floatBuffer(FloatBuffer current, int count) {
        if (current == null || current.capacity() < count) {
            return ByteBuffer.allocateDirect(count * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        current.clear();
        return current;
    }

    private static ShortBuffer shortBuffer(ShortBuffer current, int count) {
        if (current == null || current.capacity() < count) {
            return ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        }
        current.clear();
        return current;
    }
}
