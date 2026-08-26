package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Renders the inferred QuicklyJoin shell with the recovered Xianyi input-control pixels.
 *
 * <p>The archive contains the title lettering, six-slot artwork, keypad states, delete/redo icons,
 * and two number strips. It does not contain the QuicklyJoin CSB, outer-panel texture, or close
 * texture, so only those missing shell pieces remain explicitly inferred Canvas drawings.
 */
final class JoinRoomRenderer {
    static final int ACTION_NONE = -10;
    static final int ACTION_CLEAR = -2;
    static final int ACTION_DELETE = -3;

    private static final int FRAME_TOP = 0x768f9696;
    private static final int FRAME_BOTTOM = 0xb0535d5e;
    private static final int FRAME_EDGE = 0xff52595a;
    private static final int BODY_TOP = 0xfff9f8e4;
    private static final int BODY_BOTTOM = 0xfffdf6d4;
    private static final float CORNER = 26f;
    /** Raw {@code btn_number_n.png} is 178 pixels wide; layout width is inferred from the image. */
    private static final float CONTROL_SCALE = JoinRoomLayout.key(1).width() / 178f;
    /** Raw {@code number_bg.png} is 85 pixels high; its design-space height is locked by layout. */
    private static final float SLOT_SCALE = JoinRoomLayout.DIGIT_SLOT_DIAMETER / 85f;
    /** The title lettering is deliberately a little larger than a keypad glyph in the reference. */
    private static final float TITLE_TEXT_SCALE = 1.4f;

    private final XianyiJoinRoomDrawableSet drawables;
    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Rect source = new Rect();
    private final RectF box = new RectF();

    JoinRoomRenderer(XianyiJoinRoomDrawableSet drawables) {
        if (drawables == null) {
            throw new IllegalArgumentException("drawables must not be null");
        }
        this.drawables = drawables;
    }

    void draw(Canvas canvas, JoinRoomInput input, int pressedAction) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(178, 0, 0, 0));
        canvas.drawRect(0f, 0f, JoinRoomLayout.DESIGN_WIDTH, JoinRoomLayout.DESIGN_HEIGHT, paint);

        drawFrame(canvas);
        drawHeaderGlass(canvas);
        drawBody(canvas);
        drawSlots(canvas, input);
        drawKeypad(canvas, pressedAction);
        drawTitleCrown(canvas);
        drawClose(canvas);
    }

    private void drawFrame(Canvas canvas) {
        fillVertical(canvas, JoinRoomLayout.FRAME, FRAME_TOP, FRAME_BOTTOM, CORNER + 6f);
        stroke(canvas, JoinRoomLayout.FRAME, FRAME_EDGE, 4f, CORNER + 6f);
    }

    /**
     * Screenshot-inferred translucent header. The QuicklyJoin archive has no shell texture, but
     * the reference clearly shows the hall behind a silver glass band rather than a solid bar.
     */
    private void drawHeaderGlass(Canvas canvas) {
        JoinRoomLayout.Rect bounds = JoinRoomLayout.HEADER;
        Path header = roundedTopPath(bounds, CORNER);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setShader(
                new LinearGradient(
                        0f,
                        bounds.top(),
                        0f,
                        bounds.bottom(),
                        new int[] {0x6eedf0ec, 0x70999fa0, 0x2f535f62},
                        new float[] {0f, 0.28f, 1f},
                        Shader.TileMode.CLAMP));
        canvas.drawPath(header, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.2f);
        paint.setColor(0x80f9fbf4);
        canvas.drawPath(header, paint);
        paint.setStrokeWidth(1.4f);
        paint.setColor(0x843e494b);
        canvas.drawLine(bounds.left(), bounds.bottom() - 1f, bounds.right(), bounds.bottom() - 1f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
    }

    private void drawBody(Canvas canvas) {
        fillVertical(canvas, JoinRoomLayout.BODY, BODY_TOP, BODY_BOTTOM, CORNER);
    }

    private void drawSlots(Canvas canvas, JoinRoomInput input) {
        float centerY = JoinRoomLayout.DIGIT_CENTER_Y;
        for (int index = 0; index < 6; index++) {
            float centerX = JoinRoomLayout.DIGIT_CENTERS_X[index];
            drawCentered(canvas, drawables.slot, centerX, centerY, SLOT_SCALE);
            String value = input.digitAt(index);
            if (!value.isEmpty()) {
                drawCenteredSource(
                        canvas,
                        drawables.slotFont,
                        XianyiJoinRoomNumberAtlas.slotDigit(value.charAt(0) - '0'),
                        centerX,
                        centerY,
                        SLOT_SCALE);
            }
        }
    }

    private void drawKeypad(Canvas canvas, int pressedAction) {
        for (int digit = 1; digit <= 9; digit++) {
            drawDigitKey(canvas, digit, JoinRoomLayout.key(digit), pressedAction == digit);
        }
        drawDigitKey(canvas, 0, JoinRoomLayout.key(0), pressedAction == 0);
        drawIconKey(
                canvas,
                JoinRoomLayout.DELETE,
                drawables.delete,
                pressedAction == ACTION_DELETE);
        drawIconKey(
                canvas, JoinRoomLayout.CLEAR, drawables.redo, pressedAction == ACTION_CLEAR);
    }

    private void drawDigitKey(
            Canvas canvas, int digit, JoinRoomLayout.Rect bounds, boolean pressed) {
        drawStretched(canvas, pressed ? drawables.keyPressed : drawables.keyNormal, bounds);
        drawCenteredSource(
                canvas,
                drawables.keypadFont,
                XianyiJoinRoomNumberAtlas.keypadDigit(digit),
                bounds.centerX(),
                bounds.centerY(),
                CONTROL_SCALE);
    }

    private void drawIconKey(
            Canvas canvas, JoinRoomLayout.Rect bounds, Bitmap icon, boolean pressed) {
        drawStretched(canvas, pressed ? drawables.keyPressed : drawables.keyNormal, bounds);
        drawCentered(canvas, icon, bounds.centerX(), bounds.centerY(), CONTROL_SCALE);
    }

    /**
     * Draws the screenshot-inferred title crown. The original content pixels are restricted to
     * {@code joing_title.png}; the broad satin ribbon, its filigree, and the close shell were not
     * present in the recovered QuicklyJoin archive.
     */
    private void drawTitleCrown(Canvas canvas) {
        Path crown = titleCrownPath(0f);
        canvas.save();
        canvas.translate(0f, 4f);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setColor(0x550d1314);
        canvas.drawPath(crown, paint);
        canvas.restore();

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(
                new LinearGradient(
                        0f,
                        JoinRoomLayout.TITLE_CROWN.top(),
                        0f,
                        JoinRoomLayout.TITLE_CROWN.bottom(),
                        new int[] {0xffe8e5df, 0xffe8cba6, 0xfff5e8ca},
                        new float[] {0f, 0.42f, 1f},
                        Shader.TileMode.CLAMP));
        canvas.drawPath(crown, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3.2f);
        paint.setColor(0xffd9dcda);
        canvas.drawPath(crown, paint);

        Path insetCrown = titleCrownPath(9f);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(
                new LinearGradient(
                        0f,
                        JoinRoomLayout.TITLE_CROWN.top() + 9f,
                        0f,
                        JoinRoomLayout.TITLE_CROWN.bottom() - 9f,
                        new int[] {0xfff2e5d2, 0xffe7c59f, 0xfff2dfbd},
                        new float[] {0f, 0.46f, 1f},
                        Shader.TileMode.CLAMP));
        canvas.drawPath(insetCrown, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(0xffd8b88f);
        canvas.drawPath(insetCrown, paint);

        drawCrownScrollwork(canvas);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        drawCentered(
                canvas,
                drawables.title,
                JoinRoomLayout.TITLE_TEXT_CENTER_X,
                JoinRoomLayout.TITLE_TEXT_CENTER_Y,
                TITLE_TEXT_SCALE);
    }

    /** The paired curls are deliberately separate from the title bitmap: they are screenshot inference. */
    private void drawCrownScrollwork(Canvas canvas) {
        drawCrownScrollworkSide(canvas, -1f);
        drawCrownScrollworkSide(canvas, 1f);
    }

    private void drawCrownScrollworkSide(Canvas canvas, float side) {
        JoinRoomLayout.Rect bounds = JoinRoomLayout.TITLE_CROWN;
        float centerX = bounds.centerX();
        float top = bounds.top();
        float bottom = bounds.bottom();
        float outer = centerX + side * 274f;
        float middle = centerX + side * 204f;
        float inner = centerX + side * 116f;

        Path curl = new Path();
        curl.moveTo(outer, top + 25f);
        curl.cubicTo(
                outer - side * 22f,
                top + 41f,
                middle + side * 14f,
                bottom - 20f,
                middle,
                bottom - 31f);
        curl.cubicTo(
                middle - side * 39f,
                bottom - 42f,
                middle - side * 31f,
                top + 43f,
                inner,
                top + 46f);
        curl.cubicTo(
                inner + side * 30f,
                top + 49f,
                inner + side * 36f,
                top + 74f,
                inner + side * 10f,
                top + 78f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5.5f);
        paint.setColor(0x4ffcf3df);
        canvas.drawPath(curl, paint);
        paint.setStrokeWidth(2.2f);
        paint.setColor(0x70bf9b78);
        canvas.drawPath(curl, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
    }

    private Path titleCrownPath(float inset) {
        JoinRoomLayout.Rect bounds = JoinRoomLayout.TITLE_CROWN;
        float left = bounds.left() + inset;
        float top = bounds.top() + inset;
        float right = bounds.right() - inset;
        float bottom = bounds.bottom() - inset;
        float centerX = (left + right) * 0.5f;
        float width = right - left;

        Path crown = new Path();
        crown.moveTo(left, top + 17f);
        crown.cubicTo(
                left + width * 0.12f,
                top + 4f,
                centerX - width * 0.34f,
                top + 17f,
                centerX - width * 0.25f,
                top + 19f);
        crown.cubicTo(
                centerX - width * 0.16f,
                top + 7f,
                centerX - width * 0.08f,
                top + 4f,
                centerX,
                top + 5f);
        crown.cubicTo(
                centerX + width * 0.08f,
                top + 4f,
                centerX + width * 0.16f,
                top + 7f,
                centerX + width * 0.25f,
                top + 19f);
        crown.cubicTo(
                centerX + width * 0.34f,
                top + 17f,
                right - width * 0.12f,
                top + 4f,
                right,
                top + 17f);
        crown.cubicTo(
                right - width * 0.03f,
                top + 47f,
                right - width * 0.08f,
                bottom - 18f,
                right - width * 0.22f,
                bottom - 8f);
        crown.cubicTo(
                right - width * 0.34f,
                bottom + 3f,
                centerX + width * 0.19f,
                bottom + 5f,
                centerX,
                bottom - 1f);
        crown.cubicTo(
                centerX - width * 0.19f,
                bottom + 5f,
                left + width * 0.34f,
                bottom + 3f,
                left + width * 0.22f,
                bottom - 8f);
        crown.cubicTo(
                left + width * 0.08f,
                bottom - 18f,
                left + width * 0.03f,
                top + 47f,
                left,
                top + 17f);
        crown.close();
        return crown;
    }

    private Path roundedTopPath(JoinRoomLayout.Rect bounds, float corner) {
        Path path = new Path();
        float left = bounds.left();
        float top = bounds.top();
        float right = bounds.right();
        float bottom = bounds.bottom();
        path.moveTo(left, bottom);
        path.lineTo(left, top + corner);
        path.quadTo(left, top, left + corner, top);
        path.lineTo(right - corner, top);
        path.quadTo(right, top, right, top + corner);
        path.lineTo(right, bottom);
        path.close();
        return path;
    }

    /** The original close texture is unavailable, so this reference-image approximation remains. */
    private void drawClose(Canvas canvas) {
        JoinRoomLayout.Rect bounds = JoinRoomLayout.CLOSE;
        float centerX = bounds.centerX();
        float width = bounds.width();
        float height = bounds.height();
        float top = bounds.top();
        float lobeRadius = width * 0.29f;
        float lobeY = top + height * 0.30f;

        Path spade = new Path();
        spade.addCircle(centerX - width * 0.21f, lobeY, lobeRadius, Path.Direction.CW);
        spade.addCircle(centerX + width * 0.21f, lobeY, lobeRadius, Path.Direction.CW);
        Path body = new Path();
        body.moveTo(centerX - width * 0.5f, lobeY);
        body.lineTo(centerX + width * 0.5f, lobeY);
        body.lineTo(centerX, top + height * 0.86f);
        body.close();
        spade.op(body, Path.Op.UNION);
        Path stem = new Path();
        stem.moveTo(centerX - width * 0.11f, top + height);
        stem.lineTo(centerX + width * 0.11f, top + height);
        stem.lineTo(centerX, top + height * 0.62f);
        stem.close();
        spade.op(stem, Path.Op.UNION);

        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setShader(
                new LinearGradient(
                        0f, top, 0f, top + height, 0xff9a9ca1, 0xff6f7175, Shader.TileMode.CLAMP));
        canvas.drawPath(spade, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(0xff55575b);
        canvas.drawPath(spade, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(12f);
        paint.setColor(0xfff3e6b8);
        float arm = width * 0.21f;
        float crossY = lobeY + height * 0.04f;
        canvas.drawLine(centerX - arm, crossY - arm, centerX + arm, crossY + arm, paint);
        canvas.drawLine(centerX - arm, crossY + arm, centerX + arm, crossY - arm, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
    }

    private void drawStretched(Canvas canvas, Bitmap bitmap, JoinRoomLayout.Rect bounds) {
        box.set(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        canvas.drawBitmap(bitmap, null, box, bitmapPaint);
    }

    private void drawCentered(
            Canvas canvas, Bitmap bitmap, float centerX, float centerY, float scale) {
        float width = bitmap.getWidth() * scale;
        float height = bitmap.getHeight() * scale;
        box.set(
                centerX - width * 0.5f,
                centerY - height * 0.5f,
                centerX + width * 0.5f,
                centerY + height * 0.5f);
        canvas.drawBitmap(bitmap, null, box, bitmapPaint);
    }

    private void drawCenteredSource(
            Canvas canvas,
            Bitmap bitmap,
            XianyiJoinRoomNumberAtlas.SourceRect sourceRect,
            float centerX,
            float centerY,
            float scale) {
        source.set(sourceRect.left(), sourceRect.top(), sourceRect.right(), sourceRect.bottom());
        float width = sourceRect.width() * scale;
        float height = sourceRect.height() * scale;
        box.set(
                centerX - width * 0.5f,
                centerY - height * 0.5f,
                centerX + width * 0.5f,
                centerY + height * 0.5f);
        canvas.drawBitmap(bitmap, source, box, bitmapPaint);
    }

    private void fillVertical(
            Canvas canvas, JoinRoomLayout.Rect rect, int top, int bottom, float corner) {
        box.set(rect.left(), rect.top(), rect.right(), rect.bottom());
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setShader(
                new LinearGradient(
                        0f, rect.top(), 0f, rect.bottom(), top, bottom, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(box, corner, corner, paint);
        paint.setShader(null);
        paint.setAlpha(255);
    }

    private void stroke(
            Canvas canvas, JoinRoomLayout.Rect rect, int color, float width, float corner) {
        box.set(rect.left(), rect.top(), rect.right(), rect.bottom());
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setColor(color);
        canvas.drawRoundRect(box, corner, corner, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
    }
}
