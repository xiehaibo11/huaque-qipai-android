package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** Original TeaHouseListView.csb rendered in 1920x1080 design coordinates. */
final class MatchArenaListView extends View {
    interface Actions {
        void onBackRequested();
        void onCreateRequested();
    }

    private final Actions actions;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Typeface typeface;
    private final List<MatchArenaSummary> items = new ArrayList<>();
    private final Map<String, Bitmap> avatars = new HashMap<>();
    private boolean loading = true;
    private boolean loaded;
    private float scrollX;
    private float downX;
    private float downY;
    private float lastX;
    private boolean dragging;
    private boolean scrollGesture;
    private Runnable buttonClickSound = () -> {};

    private final Bitmap background;
    private final Bitmap top;
    private final Bitmap titleBackground;
    private final Bitmap title;
    private final Bitmap decoration;
    private final Bitmap back;
    private final Bitmap create;
    private final Bitmap createGuideBubble;
    private final Bitmap hanger;
    private final Bitmap outer;
    private final Bitmap inner;
    private final Bitmap levelBackground;
    private final Bitmap head;
    private final Bitmap headFrame;
    private final Bitmap nameBackground;
    private final Bitmap numberBackground;
    private final Bitmap newButton;
    private final Bitmap shareButton;
    private final Bitmap settingsButton;
    private final Bitmap enterButton;
    private final Bitmap relaunchButton;
    private final Bitmap quitButton;
    private final Bitmap numberFont;
    private boolean showCreateGuide = true;

    MatchArenaListView(Context context, Actions actions) {
        super(context);
        this.actions = actions;
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        background = bitmap(R.drawable.tea_house_bg);
        top = bitmap(R.drawable.tea_house_list_top_bg);
        titleBackground = bitmap(R.drawable.tea_house_list_title_bg);
        title = bitmap(R.drawable.tea_house_list_title);
        decoration = bitmap(R.drawable.tea_house_list_decorate_4);
        back = bitmap(R.drawable.tea_house_list_close_btn);
        create = bitmap(R.drawable.tea_house_list_create_btn);
        createGuideBubble = bitmap(R.drawable.tea_house_createroom_qi_pao2);
        hanger = bitmap(R.drawable.tea_house_list_decorate_2);
        outer = bitmap(R.drawable.tea_house_list_decorate_1);
        inner = bitmap(R.drawable.tea_house_list_bg_2);
        levelBackground = bitmap(R.drawable.item_bg_level_1);
        head = bitmap(R.drawable.tea_house_list_head);
        headFrame = bitmap(R.drawable.tea_house_list_frame);
        nameBackground = bitmap(R.drawable.tea_house_list_name_bg);
        numberBackground = bitmap(R.drawable.tea_house_list_num_bg);
        newButton = bitmap(R.drawable.tea_house_list_new_btn);
        shareButton = bitmap(R.drawable.tea_house_list_share_btn);
        settingsButton = bitmap(R.drawable.tea_house_list_setting_btn);
        enterButton = bitmap(R.drawable.tea_house_list_join_btn);
        relaunchButton = bitmap(R.drawable.tea_house_list_relaunch_btn);
        quitButton = bitmap(R.drawable.tea_house_quit);
        numberFont = bitmap(R.drawable.fangzhengcuyuan_latin_64_0);
        setFocusable(true);
    }

    void setItems(List<MatchArenaSummary> values) {
        items.clear();
        if (values != null) {
            items.addAll(values);
        }
        loading = false;
        loaded = true;
        scrollX = 0;
        invalidate();
    }

    void prepend(MatchArenaSummary value) {
        if (value == null) {
            return;
        }
        items.removeIf(item -> item.id().equals(value.id()));
        items.add(0, value);
        loading = false;
        loaded = true;
        invalidate();
    }

    void setLoading(boolean value) {
        loading = value;
        if (value && !loaded) {
            items.clear();
        }
        invalidate();
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    void setAvatarBitmap(String avatarKey, Bitmap bitmap) {
        if (avatarKey != null && !avatarKey.isBlank() && bitmap != null) {
            avatars.put(avatarKey, bitmap);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        float scale = viewportScale();
        canvas.save();
        canvas.translate((getWidth() - 1920 * scale) / 2f, (getHeight() - 1080 * scale) / 2f);
        canvas.scale(scale, scale);
        drawBitmap(canvas, background, new RectF(0, 0, 1920, 1080));
        if (loaded && !loading) {
            canvas.save();
            canvas.clipRect(0, 80, 1920, 1080);
            for (int index = 0; index < items.size(); index++) {
                drawInfoItem(
                        canvas,
                        MatchArenaListLayout.ITEM_START_X
                                + index * MatchArenaListLayout.ITEM_STRIDE
                                - scrollX,
                        items.get(index));
            }
            drawJoinItem(
                    canvas,
                    MatchArenaListLayout.ITEM_START_X
                            + items.size() * MatchArenaListLayout.ITEM_STRIDE
                            - scrollX);
            canvas.restore();
        }
        drawChrome(canvas);
        canvas.restore();
    }

    private void drawChrome(Canvas canvas) {
        drawBitmap(canvas, top, new RectF(-0.5f, 0, 1920.5f, 264));
        drawBitmap(canvas, titleBackground, new RectF(658.04f, -3, 1265.04f, 119));
        drawBitmap(canvas, title, new RectF(787.04f, 1, 1149.04f, 85));
        drawBitmap(canvas, decoration, new RectF(-0.5f, 23, 339.5f, 157));
        drawBitmap(canvas, back, new RectF(6.5f, 14, 160.5f, 132));
        drawBitmap(canvas, create, new RectF(1661, 820.5f, 1933, 1079.5f));
        if (showCreateGuide) {
            drawBitmap(canvas, createGuideBubble, cocosRect(1810, 270, 173, 101));
            text(canvas, "试试!", 1810, 1080 - 253, 49, 0xff855a3e, Paint.Align.CENTER);
        }
    }

    private void drawJoinItem(Canvas canvas, float left) {
        drawHangersAndOuter(canvas, left);
        drawNine(canvas, inner, new RectF(left + 51, 189.5f, left + 465, 982.5f), 140, 93, 134, 127);
        drawBitmap(canvas, newButton, cocosRect(left + 258, 620.5f, 212, 212));
        text(canvas, "点击上方", left + 258, 1080 - 452, 38, 0xffb87234, Paint.Align.CENTER);
        text(canvas, "再加入一个比赛场", left + 258, 1080 - 410, 38, 0xffb87234, Paint.Align.CENTER);
    }

    private void drawInfoItem(Canvas canvas, float left, MatchArenaSummary item) {
        drawHangersAndOuter(canvas, left);
        drawNine(
                canvas,
                levelBackground,
                new RectF(left + 50, 189.5f, left + 466, 991.5f),
                140,
                93,
                136,
                608);
        drawAvatar(canvas, left, item);
        drawBitmap(canvas, headFrame, cocosRect(left + 256, 743.5f, 164, 164));
        drawBitmap(canvas, nameBackground, cocosRect(left + 257, 615.5f, 336, 64));
        String name = item.ownerNickname() + "的比赛场";
        if (item.remark().matches("\\d{1,4}")) {
            name += "(" + item.remark() + ")";
        }
        textFit(canvas, name, left + 257, 1080 - 604, 36, 26, 310, 0xffb97345);
        drawBitmap(canvas, numberBackground, cocosRect(left + 257, 513.5f, 308, 78));
        drawArenaNumber(canvas, left, paddedNumber(item.arenaNumber()));
        drawBitmap(canvas, shareButton, cocosRect(left + 257, 406.5f, 213, 93));
        boolean owner = "OWNER".equals(item.role());
        boolean open = "OPEN".equals(item.status());
        if (owner) {
            drawBitmap(canvas, settingsButton, cocosRect(left + 257, 297.5f, 213, 93));
            drawBitmap(
                    canvas,
                    open ? enterButton : relaunchButton,
                    cocosRect(left + 261, 173.5f, 283, 117));
        } else if (open) {
            drawBitmap(canvas, enterButton, cocosRect(left + 257, 209.5f, 283, 117));
            centeredText(
                    canvas,
                    "人数：" + item.onlineCount() + "/" + item.memberCount(),
                    left + 252,
                    1080 - 133.5f,
                    30,
                    0xffc66c44);
        } else {
            centeredText(canvas, "已闭赛", left + 260, 1080 - 198.5f, 50, 0xffc66c44);
            centeredText(canvas, "请联系领队", left + 261, 1080 - 145.5f, 34, 0xffbd8b4c);
        }
        if (!owner) {
            drawBitmap(canvas, quitButton, new RectF(left + 345, 154.5f, left + 496, 360.5f));
        }
    }

    private void drawAvatar(Canvas canvas, float left, MatchArenaSummary item) {
        RectF bounds = cocosRect(left + 256, 747.5f, 148, 148);
        Bitmap avatar = avatars.get(item.ownerAvatarKey());
        if (avatar == null) {
            drawBitmap(canvas, head, bounds);
            return;
        }
        drawBitmap(canvas, avatar, bounds);
    }

    private void drawHangersAndOuter(Canvas canvas, float left) {
        drawBitmap(canvas, hanger, new RectF(left + 131, 80, left + 139, 205));
        drawBitmap(canvas, hanger, new RectF(left + 375, 80, left + 383, 205));
        drawNine(canvas, outer, new RectF(left + 55.5f, 198.5f, left + 460.5f, 977.5f), 36, 257, 40, 265);
    }

    private void drawArenaNumber(Canvas canvas, float itemLeft, String number) {
        float[] centers = {165f, 199.4482f, 235.6205f, 272.4202f, 307.8021f, 344.4385f};
        int[][] glyphs = {
            {546, 332, 29, 40, 1, 13, 31}, {598, 413, 16, 39, 1, 13, 20},
            {522, 413, 27, 39, 2, 13, 31}, {90, 385, 28, 40, 1, 13, 31},
            {310, 419, 31, 39, 0, 13, 31}, {119, 384, 28, 40, 1, 13, 31},
            {60, 385, 29, 40, 1, 13, 31}, {374, 416, 29, 39, 0, 13, 29},
            {576, 332, 29, 40, 1, 13, 31}, {0, 385, 29, 40, 1, 13, 31}
        };
        float scale = 0.85f;
        float centerY = 1080 - 513.5f;
        paint.setColorFilter(new PorterDuffColorFilter(0xfffffdea, PorterDuff.Mode.SRC_IN));
        for (int index = 0; index < centers.length; index++) {
            int[] glyph = glyphs[number.charAt(index) - '0'];
            float left = itemLeft + centers[index] - glyph[6] * scale / 2 + glyph[4] * scale;
            float top = centerY - 64 * scale / 2 + glyph[5] * scale;
            canvas.drawBitmap(
                    numberFont,
                    new Rect(glyph[0], glyph[1], glyph[0] + glyph[2], glyph[1] + glyph[3]),
                    new RectF(left, top, left + glyph[2] * scale, top + glyph[3] * scale),
                    paint);
        }
        paint.setColorFilter(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float scale = viewportScale();
        float designX = (event.getX() - (getWidth() - 1920 * scale) / 2f) / scale;
        float designY = (event.getY() - (getHeight() - 1080 * scale) / 2f) / scale;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                downX = designX;
                downY = designY;
                lastX = designX;
                dragging = false;
                scrollGesture =
                        designY >= 80
                                && !new RectF(6.5f, 14, 160.5f, 132).contains(designX, designY)
                                && !new RectF(1661, 820.5f, 1933, 1080).contains(designX, designY);
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                float delta = designX - lastX;
                if (scrollGesture && Math.abs(designX - downX) > 12) {
                    dragging = true;
                }
                if (scrollGesture) scrollX = clampScroll(scrollX - delta);
                lastX = designX;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                if (!dragging && distance(designX, designY, downX, downY) < 24) {
                    return handleTap(designX, designY);
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                dragging = false;
                return true;
            }
            default -> { return true; }
        }
    }

    private boolean handleTap(float x, float y) {
        if (new RectF(6.5f, 14, 160.5f, 132).contains(x, y)) {
            buttonClickSound.run();
            actions.onBackRequested();
            return true;
        }
        if (new RectF(1661, 820.5f, 1933, 1080).contains(x, y)) {
            buttonClickSound.run();
            showCreateGuide = false;
            actions.onCreateRequested();
            return true;
        }
        return true;
    }

    void requestBack() {
        buttonClickSound.run();
        actions.onBackRequested();
    }

    private float clampScroll(float value) {
        int count = loaded ? items.size() + 1 : 0;
        return Math.max(0, Math.min(value, MatchArenaListLayout.innerWidth(count) - 1920));
    }

    private Bitmap bitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        canvas.drawBitmap(bitmap, null, destination, paint);
    }

    private static RectF cocosRect(float centerX, float centerY, float width, float height) {
        return new RectF(
                centerX - width / 2,
                1080 - centerY - height / 2,
                centerX + width / 2,
                1080 - centerY + height / 2);
    }

    private void text(Canvas canvas, String value, float x, float baseline, float size, int color, Paint.Align align) {
        paint.setTypeface(typeface);
        paint.setTextSize(size);
        paint.setColor(color);
        paint.setTextAlign(align);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(value, x, baseline, paint);
    }

    private void centeredText(Canvas canvas, String value, float x, float y, float size, int color) {
        paint.setTypeface(typeface);
        paint.setTextSize(size);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        text(canvas, value, x, y - (metrics.ascent + metrics.descent) / 2f, size, color, Paint.Align.CENTER);
    }

    private void textFit(Canvas canvas, String value, float x, float baseline, float size, float minimum, float width, int color) {
        float selected = size;
        paint.setTypeface(typeface);
        while (selected > minimum) {
            paint.setTextSize(selected);
            if (paint.measureText(value) <= width) {
                break;
            }
            selected -= 1;
        }
        text(canvas, value, x, baseline, selected, color, Paint.Align.CENTER);
    }

    private void drawNine(
            Canvas canvas,
            Bitmap image,
            RectF target,
            int capX,
            int capY,
            int capWidth,
            int capHeight) {
        int sourceWidth = image.getWidth();
        int sourceHeight = image.getHeight();
        int left = capX;
        int right = sourceWidth - capX - capWidth;
        int bottom = capY;
        int top = sourceHeight - capY - capHeight;
        int sourceTop = top;
        int sourceBottom = sourceHeight - bottom;
        int sourceRight = sourceWidth - right;
        float targetRight = target.right - right;
        float targetBottom = target.bottom - bottom;
        drawSlice(canvas, image, 0, 0, left, sourceTop, target.left, target.top, target.left + left, target.top + top);
        drawSlice(canvas, image, left, 0, sourceRight, sourceTop, target.left + left, target.top, targetRight, target.top + top);
        drawSlice(canvas, image, sourceRight, 0, sourceWidth, sourceTop, targetRight, target.top, target.right, target.top + top);
        drawSlice(canvas, image, 0, sourceTop, left, sourceBottom, target.left, target.top + top, target.left + left, targetBottom);
        drawSlice(canvas, image, left, sourceTop, sourceRight, sourceBottom, target.left + left, target.top + top, targetRight, targetBottom);
        drawSlice(canvas, image, sourceRight, sourceTop, sourceWidth, sourceBottom, targetRight, target.top + top, target.right, targetBottom);
        drawSlice(canvas, image, 0, sourceBottom, left, sourceHeight, target.left, targetBottom, target.left + left, target.bottom);
        drawSlice(canvas, image, left, sourceBottom, sourceRight, sourceHeight, target.left + left, targetBottom, targetRight, target.bottom);
        drawSlice(canvas, image, sourceRight, sourceBottom, sourceWidth, sourceHeight, targetRight, targetBottom, target.right, target.bottom);
    }

    private void drawSlice(Canvas canvas, Bitmap image, int l, int t, int r, int b, float dl, float dt, float dr, float db) {
        canvas.drawBitmap(image, new Rect(l, t, r, b), new RectF(dl, dt, dr, db), paint);
    }

    private static String paddedNumber(String value) {
        try {
            return String.format(java.util.Locale.ROOT, "%06d", Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return "000000";
        }
    }

    private static float distance(float x, float y, float otherX, float otherY) {
        return (float) Math.hypot(x - otherX, y - otherY);
    }

    private float viewportScale() {
        return Math.max(0.0001f, Math.min(getWidth() / 1920f, getHeight() / 1080f));
    }
}
