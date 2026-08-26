package com.nanbeiyule.game;

import android.opengl.GLES20;
import com.nanbeiyule.game.spine37.Spine37Data;
import com.nanbeiyule.game.spine37.Spine37Projection;
import com.nanbeiyule.game.spine37.Spine37Runtime;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

final class MembershipPrivilegeEffectRenderer {
    private static final float DESIGN_WIDTH = 1920.0f;
    private static final float DESIGN_HEIGHT = 1080.0f;
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
    private final long animationStartedNanos;
    private int program;
    private int positionLocation;
    private int texCoordLocation;
    private int colorLocation;
    private FloatBuffer positionBuffer;
    private FloatBuffer texCoordBuffer;
    private ShortBuffer indexBuffer;

    MembershipPrivilegeEffectRenderer(OriginalLobbyEffectInstance instance) {
        this(List.of(instance), System.nanoTime());
    }

    MembershipPrivilegeEffectRenderer(List<OriginalLobbyEffectInstance> instances) {
        this(instances, System.nanoTime());
    }

    MembershipPrivilegeEffectRenderer(
            List<OriginalLobbyEffectInstance> instances, long animationStartedNanos) {
        this.instances = List.copyOf(instances);
        this.animationStartedNanos = animationStartedNanos;
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
            throw new IllegalStateException("Unable to link membership effect program: " + message);
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
        draw(viewportWidth, viewportHeight, 0.0f);
    }

    void draw(int viewportWidth, int viewportHeight, float contentOffsetX) {
        draw(viewportWidth, viewportHeight, contentOffsetX, 0.0f);
    }

    void draw(
            int viewportWidth,
            int viewportHeight,
            float contentOffsetX,
            float contentOffsetY) {
        if (program == 0 || viewportWidth <= 0 || viewportHeight <= 0) {
            return;
        }
        GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);
        AdaptiveViewport viewport =
                AdaptiveViewport.create(
                        viewportWidth,
                        viewportHeight,
                        DESIGN_WIDTH,
                        DESIGN_HEIGHT,
                        AdaptiveViewport.Insets.NONE);
        AdaptiveViewport.Transform transform = viewport.designTransform();
        float elapsedSeconds =
                (System.nanoTime() - animationStartedNanos) / 1_000_000_000.0f;
        for (OriginalLobbyEffectInstance instance : instances) {
            OriginalLobbyEffectSpec spec = instance.spec();
            Spine37Projection projection =
                    Spine37Projection.fromCenteredPixelTransform(
                            viewportWidth,
                            viewportHeight,
                            transform.scaleX() * spec.scale(),
                            transform.mapX(spec.anchorX() - contentOffsetX),
                            transform.mapY(spec.anchorY() - contentOffsetY));
            List<Spine37Runtime.DrawCommand> commands =
                    instance.runtime().sample(spec.animationName(), elapsedSeconds);
            if (spec.maskContentRects().isEmpty()) {
                applyClip(viewportHeight, transform, spec.clipDesignRect());
                drawCommands(instance, projection, commands);
            } else {
                List<AdaptiveViewport.Rect> masks =
                        new ArrayList<>(spec.maskContentRects().size());
                for (AdaptiveViewport.Rect mask : spec.maskContentRects()) {
                    masks.add(
                            new AdaptiveViewport.Rect(
                                    mask.left() - contentOffsetX,
                                    mask.top() - contentOffsetY,
                                    mask.right() - contentOffsetX,
                                    mask.bottom() - contentOffsetY));
                }
                for (AdaptiveViewport.Rect band :
                        complementBands(spec.clipDesignRect(), masks)) {
                    applyClip(viewportHeight, transform, band);
                    drawCommands(instance, projection, commands);
                }
            }
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        }
    }

    /**
     * Carves every content-space mask out of the clip rectangle, yielding the bands where
     * the effect stays visible. In the original scene graph the effect node sits below
     * every card panel, so each opaque card bitmap occludes it; the surviving bands are
     * the strips above and below the card row plus the gaps between cards.
     */
    static List<AdaptiveViewport.Rect> complementBands(
            AdaptiveViewport.Rect clip, List<AdaptiveViewport.Rect> masks) {
        List<AdaptiveViewport.Rect> bands = new ArrayList<>();
        bands.add(clip);
        for (AdaptiveViewport.Rect mask : masks) {
            List<AdaptiveViewport.Rect> next = new ArrayList<>();
            for (AdaptiveViewport.Rect band : bands) {
                next.addAll(complementBands(band, mask));
            }
            bands = next;
        }
        return bands;
    }

    /** Subtracts a single mask from the clip, returning up to four surviving bands. */
    static List<AdaptiveViewport.Rect> complementBands(
            AdaptiveViewport.Rect clip, AdaptiveViewport.Rect mask) {
        if (clip == null) {
            clip = new AdaptiveViewport.Rect(0.0f, 0.0f, DESIGN_WIDTH, DESIGN_HEIGHT);
        }
        List<AdaptiveViewport.Rect> bands = new ArrayList<>(4);
        float left = Math.max(clip.left(), mask.left());
        float top = Math.max(clip.top(), mask.top());
        float right = Math.min(clip.right(), mask.right());
        float bottom = Math.min(clip.bottom(), mask.bottom());
        if (left >= right || top >= bottom) {
            bands.add(clip);
            return bands;
        }
        addBand(bands, clip.left(), clip.top(), clip.right(), top);
        addBand(bands, clip.left(), bottom, clip.right(), clip.bottom());
        addBand(bands, clip.left(), top, left, bottom);
        addBand(bands, right, top, clip.right(), bottom);
        return bands;
    }

    private static void addBand(
            List<AdaptiveViewport.Rect> bands,
            float left,
            float top,
            float right,
            float bottom) {
        if (right - left > 0.0f && bottom - top > 0.0f) {
            bands.add(new AdaptiveViewport.Rect(left, top, right, bottom));
        }
    }

    private void drawCommands(
            OriginalLobbyEffectInstance instance,
            Spine37Projection projection,
            List<Spine37Runtime.DrawCommand> commands) {
        for (Spine37Runtime.DrawCommand command : commands) {
            if (instance.spec().drawsAttachment(command.attachmentName())) {
                drawCommand(instance, projection, command);
            }
        }
    }

    private static void applyClip(
            int viewportHeight,
            AdaptiveViewport.Transform transform,
            AdaptiveViewport.Rect clipDesignRect) {
        if (clipDesignRect == null) {
            GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
            return;
        }
        int left = Math.round(transform.mapX(clipDesignRect.left()));
        int top = Math.round(transform.mapY(clipDesignRect.top()));
        int right = Math.round(transform.mapX(clipDesignRect.right()));
        int bottom = Math.round(transform.mapY(clipDesignRect.bottom()));
        int width = Math.max(0, right - left);
        int height = Math.max(0, bottom - top);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(left, viewportHeight - bottom, width, height);
    }

    private void drawCommand(
            OriginalLobbyEffectInstance instance,
            Spine37Projection projection,
            Spine37Runtime.DrawCommand command) {
        Integer texture = instance.texture(command.pageName());
        if (texture == null) {
            return;
        }
        positionBuffer = floatBuffer(positionBuffer, command.vertices().length);
        texCoordBuffer = floatBuffer(texCoordBuffer, command.uvs().length);
        indexBuffer = shortBuffer(indexBuffer, command.triangles().length);
        positionBuffer.put(projection.toNdc(command.vertices())).position(0);
        texCoordBuffer.put(command.uvs()).position(0);
        indexBuffer.put(command.triangles()).position(0);

        if ("additive".equals(command.blend())) {
            // The recovered Cocos animation originally drew into one opaque framebuffer.
            // This renderer draws into a transparent TextureView instead, so accumulating
            // particle alpha would make transparent black texels occlude the card when the
            // Android compositor combines the surface. Add RGB exactly as the Spine slot
            // requests, but preserve the destination alpha of the transparent surface.
            GLES20.glBlendFuncSeparate(
                    GLES20.GL_ONE, GLES20.GL_ONE, GLES20.GL_ZERO, GLES20.GL_ONE);
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
        GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, positionBuffer);
        GLES20.glEnableVertexAttribArray(texCoordLocation);
        GLES20.glVertexAttribPointer(texCoordLocation, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, command.triangles().length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
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

    private static int compileShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String message = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new IllegalStateException("Unable to compile membership shader: " + message);
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
