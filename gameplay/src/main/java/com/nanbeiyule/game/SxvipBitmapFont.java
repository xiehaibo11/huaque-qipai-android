package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Minimal BMFont renderer for the original SxvipShopLayer TextBMFont nodes. */
final class SxvipBitmapFont {
    private static final Pattern ATTRIBUTE = Pattern.compile("([A-Za-z]+)=(\"[^\"]*\"|\\S+)");

    private final Descriptor descriptor;
    private final Bitmap page;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();

    private SxvipBitmapFont(Descriptor descriptor, Bitmap page) {
        this.descriptor = descriptor;
        this.page = page;
        paint.setFilterBitmap(true);
    }

    static SxvipBitmapFont load(Resources resources, String assetPath) {
        try {
            Descriptor descriptor = parseDescriptor(readAsset(resources, assetPath));
            String pagePath = sibling(assetPath, descriptor.pageFile());
            Bitmap bitmap;
            try (InputStream stream = resources.getAssets().open(pagePath)) {
                bitmap = BitmapFactory.decodeStream(stream);
            }
            return new SxvipBitmapFont(descriptor, bitmap);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load Sxvip BMFont: " + assetPath, exception);
        }
    }

    /** Loads an original BMFont pair packaged as res/raw .fnt + drawable page. */
    static SxvipBitmapFont loadRawResource(Resources resources, int fntResourceId, int pageDrawableId) {
        try {
            Descriptor descriptor;
            try (InputStream stream = resources.openRawResource(fntResourceId)) {
                descriptor = parseDescriptor(readFully(stream));
            }
            Bitmap bitmap = BitmapFactory.decodeResource(resources, pageDrawableId);
            return new SxvipBitmapFont(descriptor, bitmap);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load raw BMFont: " + fntResourceId, exception);
        }
    }

    static Descriptor parseDescriptor(String content) {
        int lineHeight = 0;
        int base = 0;
        int scaleW = 0;
        int scaleH = 0;
        String pageFile = "";
        Map<Integer, Glyph> glyphs = new HashMap<>();
        for (String rawLine : content.split("\\R")) {
            String line = rawLine.trim();
            Map<String, String> attributes = attributes(line);
            if (line.startsWith("common ")) {
                lineHeight = intValue(attributes, "lineHeight");
                base = intValue(attributes, "base");
                scaleW = intValue(attributes, "scaleW");
                scaleH = intValue(attributes, "scaleH");
            } else if (line.startsWith("page ")) {
                pageFile = stringValue(attributes, "file");
            } else if (line.startsWith("char ")) {
                Glyph glyph =
                        new Glyph(
                                intValue(attributes, "id"),
                                intValue(attributes, "x"),
                                intValue(attributes, "y"),
                                intValue(attributes, "width"),
                                intValue(attributes, "height"),
                                intValue(attributes, "xoffset"),
                                intValue(attributes, "yoffset"),
                                intValue(attributes, "xadvance"));
                glyphs.put(glyph.id(), glyph);
            }
        }
        if (pageFile.isBlank() || lineHeight <= 0 || glyphs.isEmpty()) {
            throw new IllegalArgumentException("Invalid Sxvip BMFont descriptor");
        }
        return new Descriptor(pageFile, lineHeight, base, scaleW, scaleH, glyphs);
    }

    void drawCentered(Canvas canvas, String text, float centerX, float centerY) {
        drawCentered(canvas, text, centerX, centerY, 1.0f);
    }

    void drawCentered(Canvas canvas, String text, float centerX, float centerY, int color) {
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        drawCentered(canvas, text, centerX, centerY, 1.0f);
        paint.setColorFilter(null);
    }

    void drawCentered(Canvas canvas, String text, float centerX, float centerY, float scale) {
        float left = centerX - descriptor.measure(text) * scale * 0.5f;
        float top = centerY - descriptor.lineHeight() * scale * 0.5f;
        draw(canvas, text, left, top, scale);
    }

    void drawLeft(Canvas canvas, String text, float left, float centerY) {
        draw(canvas, text, left, centerY - descriptor.lineHeight() * 0.5f, 1.0f);
    }

    void drawLeft(Canvas canvas, String text, float left, float centerY, float scale) {
        draw(canvas, text, left, centerY - descriptor.lineHeight() * scale * 0.5f, scale);
    }

    private void draw(Canvas canvas, String text, float left, float top, float scale) {
        if (page == null || page.isRecycled() || text == null || text.isEmpty()) {
            return;
        }
        float cursor = 0f;
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            Glyph glyph = descriptor.glyph(codePoint);
            if (glyph.width() > 0 && glyph.height() > 0) {
                source.set(
                        glyph.x(),
                        glyph.y(),
                        glyph.x() + glyph.width(),
                        glyph.y() + glyph.height());
                destination.set(
                        left + (cursor + glyph.xOffset()) * scale,
                        top + glyph.yOffset() * scale,
                        left + (cursor + glyph.xOffset() + glyph.width()) * scale,
                        top + (glyph.yOffset() + glyph.height()) * scale);
                canvas.drawBitmap(page, source, destination, paint);
            }
            cursor += glyph.xAdvance();
        }
    }

    private static String readAsset(Resources resources, String assetPath) throws IOException {
        try (InputStream stream = resources.getAssets().open(assetPath)) {
            return readFully(stream);
        }
    }

    private static String readFully(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }

    private static String sibling(String assetPath, String fileName) {
        int slash = assetPath.lastIndexOf('/');
        return slash < 0 ? fileName : assetPath.substring(0, slash + 1) + fileName;
    }

    private static Map<String, String> attributes(String line) {
        Map<String, String> values = new HashMap<>();
        Matcher matcher = ATTRIBUTE.matcher(line);
        while (matcher.find()) {
            values.put(matcher.group(1), unquote(matcher.group(2)));
        }
        return values;
    }

    private static int intValue(Map<String, String> attributes, String name) {
        return Integer.parseInt(attributes.getOrDefault(name, "0"));
    }

    private static String stringValue(Map<String, String> attributes, String name) {
        return attributes.getOrDefault(name, "");
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    record Descriptor(
            String pageFile,
            int lineHeight,
            int base,
            int scaleW,
            int scaleH,
            Map<Integer, Glyph> glyphs) {
        Descriptor {
            glyphs = Collections.unmodifiableMap(new HashMap<>(glyphs));
        }

        int measure(String text) {
            if (text == null || text.isEmpty()) {
                return 0;
            }
            int width = 0;
            for (int offset = 0; offset < text.length(); ) {
                int codePoint = text.codePointAt(offset);
                offset += Character.charCount(codePoint);
                width += glyph(codePoint).xAdvance();
            }
            return width;
        }

        Glyph glyph(char character) {
            return glyph((int) character);
        }

        Glyph glyph(int codePoint) {
            Glyph glyph = glyphs.get(codePoint);
            if (glyph != null) {
                return glyph;
            }
            return glyphs.getOrDefault((int) ' ', Glyph.EMPTY);
        }
    }

    record Glyph(
            int id,
            int x,
            int y,
            int width,
            int height,
            int xOffset,
            int yOffset,
            int xAdvance) {
        private static final Glyph EMPTY = new Glyph(0, 0, 0, 0, 0, 0, 0, 0);
    }
}
