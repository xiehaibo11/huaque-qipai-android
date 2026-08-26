package com.nanbeiyule.game.wulong;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses named Cocos plist frames, including their packed-atlas rotation flag. */
final class WuLongPlistFrameResolver {
    private static final Pattern FRAME = Pattern.compile(
            "<key>(?!frames</key>)([^<]+)</key><dict><key>frame</key><string>\\{\\{(\\d+),(\\d+)\\},\\{(\\d+),(\\d+)\\}\\}</string>.*?<key>rotated</key><(true|false)/>.*?<key>sourceSize</key><string>\\{(\\d+),(\\d+)\\}</string>",
            Pattern.DOTALL);
    private final Map<String, Frame> frames;

    private WuLongPlistFrameResolver(Map<String, Frame> frames) { this.frames = Map.copyOf(frames); }

    static WuLongPlistFrameResolver load(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        for (int count; (count = input.read(buffer)) >= 0; ) bytes.write(buffer, 0, count);
        Matcher matcher = FRAME.matcher(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
        Map<String, Frame> parsed = new HashMap<>();
        while (matcher.find()) {
            int left = Integer.parseInt(matcher.group(2));
            int top = Integer.parseInt(matcher.group(3));
            int width = Integer.parseInt(matcher.group(4));
            int height = Integer.parseInt(matcher.group(5));
            parsed.put(matcher.group(1), new Frame(left, top, width, height,
                    Boolean.parseBoolean(matcher.group(6)), Integer.parseInt(matcher.group(7)),
                    Integer.parseInt(matcher.group(8))));
        }
        return new WuLongPlistFrameResolver(parsed);
    }

    Frame frame(String name) { return frames.get(name); }

    void draw(Canvas canvas, Bitmap atlas, String name, RectF target, Paint paint) {
        Frame frame = frame(name);
        if (atlas == null || frame == null) return;
        if (!frame.rotated()) {
            canvas.drawBitmap(atlas, frame.sourceRect(), target, paint);
            return;
        }
        // TexturePacker stores this crop quarter-turned; restore the named source orientation.
        canvas.save();
        canvas.rotate(90f, target.centerX(), target.centerY());
        RectF swapped = new RectF(
                target.centerX() - target.height() / 2f,
                target.centerY() - target.width() / 2f,
                target.centerX() + target.height() / 2f,
                target.centerY() + target.width() / 2f);
        canvas.drawBitmap(atlas, frame.sourceRect(), swapped, paint);
        canvas.restore();
    }

    record Frame(int left, int top, int width, int height, boolean rotated, int sourceWidth,
            int sourceHeight) {
        android.graphics.Rect sourceRect() { return new android.graphics.Rect(left, top, left + width, top + height); }
    }
}
