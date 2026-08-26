package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Draws one SxvipPrivilegeItem using the recovered original CSB child geometry. */
final class MembershipPrivilegeCardRenderer {
    static final int PRIVILEGE_COUNT = 15;
    private static final float CARD_WIDTH = 282.0f;
    private static final float CARD_HEIGHT = 320.0f;
    private static final String ORIGINAL_FONT_ASSET = "fonts/fangzhengcuyuan.ttf";
    private static final float TITLE_TEXT_SIZE = 40.0f;
    private static final float DESCRIPTION_TEXT_SIZE = 30.0f;
    private static final float DESCRIPTION_LINE_SPACING = 35.0f;
    private static final float ACTION_TEXT_SIZE = 38.0f;
    private static final float TITLE_CENTER_Y = CARD_HEIGHT - 292.6080f;
    private static final float DESCRIPTION_CENTER_Y = CARD_HEIGHT - 51.2f;
    private static final float ACTION_CENTER_Y = CARD_HEIGHT - 113.2f;
    private static final int DESCRIPTION_COLOR = Color.rgb(88, 111, 154);
    private static final int ACTION_COLOR = Color.rgb(146, 87, 33);
    private static final int[] ORIGINAL_PRIVILEGE_ICON_RESOURCES = {
        R.drawable.sxvip_privilege_card_watch,
        R.drawable.sxvip_privilege_card_free_props,
        R.drawable.sxvip_privilege_card_statistics,
        R.drawable.sxvip_privilege_card_ad_free,
        R.drawable.sxvip_privilege_card_friend_info,
        R.drawable.sxvip_privilege_card_network,
        R.drawable.sxvip_privilege_card_hide_identity,
        R.drawable.sxvip_privilege_card_daily_gift,
        R.drawable.sxvip_privilege_card_gold_record,
        R.drawable.sxvip_privilege_card_gold_statistics,
        R.drawable.sxvip_privilege_card_gold_kick_protection,
        R.drawable.sxvip_privilege_card_gold_daily_coin,
        R.drawable.sxvip_privilege_card_gold_free_props,
        R.drawable.sxvip_privilege_card_gold_mute_props,
        R.drawable.sxvip_privilege_card_gold_bonus_gift
    };
    private static final String[] PRIVILEGE_TITLES = {
        "牌局观战", "会员道具免费", "牌局数据分析", "订阅免广告",
        "查看牌友数据", "VIP专线网络", "隐藏会员身份", "每日领礼包",
        "金币场战绩", "金币数据统计", "金币场踢人防踢", "每日领1万金币",
        "会员道具免费", "屏蔽语音丢道具", "买会员 送礼包"
    };
    private static final String[] PRIVILEGE_LINES = {
        "牌友对局\n实时观看", "无限免费使用\n会员专属道具", "分析个人数据\n总结胜负经验", "开屏无广告\n[订阅会员专享]",
        "[胜率]解散/离线数\n[胜负分]出牌速度", "会员专线\n快速稳定不卡", "设置他人是否\n可见自己是会员", "每日领取洗牌券\n+多种道具",
        "金币场战绩\n详细查看", "近期胜负情况\n统计与分析", "金币场踢出玩家\n(仅部分玩法生效)", "每日领10000金币\n+多种道具",
        "无限免费使用\n会员专属道具", "不看别人发的\n表情/语音/丢道具", "买会员额外送\n价值136元礼包"
    };
    private static final String[] PRIVILEGE_ACTIONS = {
        "前往", "", "查看", "未激活",
        "查看", "", "已显示", "领取",
        "查看", "查看", "", "领取",
        "", "", ""
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Rect bitmapSource = new Rect();
    private final RectF bitmapDestination = new RectF();
    private final MembershipPrivilegeFallbackRenderer fallbackRenderer =
            new MembershipPrivilegeFallbackRenderer();
    private final Bitmap itemBackground;
    private final Bitmap buttonView;
    private final Bitmap[] originalPrivilegeIcons =
            new Bitmap[ORIGINAL_PRIVILEGE_ICON_RESOURCES.length];
    private final Typeface originalTypeface;

    MembershipPrivilegeCardRenderer(Resources resources) {
        originalTypeface = loadOriginalTypeface(resources);
        textPaint.setTypeface(originalTypeface);
        itemBackground = loadBitmap(resources, R.drawable.sxvip_privilege_item_background);
        buttonView = loadBitmap(resources, R.drawable.sxvip_privilege_button_view);
        for (int index = 0; index < ORIGINAL_PRIVILEGE_ICON_RESOURCES.length; index++) {
            originalPrivilegeIcons[index] =
                    loadBitmap(resources, ORIGINAL_PRIVILEGE_ICON_RESOURCES[index]);
        }
    }

    void draw(Canvas canvas, int index, float x, float y) {
        drawBitmap(canvas, itemBackground, new RectF(x, y, x + CARD_WIDTH, y + CARD_HEIGHT));
        drawTitleText(canvas, PRIVILEGE_TITLES[index], x + 141.0f, y + TITLE_CENTER_Y);
        if (index < originalPrivilegeIcons.length) {
            drawIconFromRecoveredCard(
                    canvas,
                    originalPrivilegeIcons[index],
                    new RectF(x + 46.0f, y + 56.0f, x + 236.0f, y + 232.0f));
        } else {
            fallbackRenderer.drawIcon(canvas, x + 141.0f, y + 146.0f);
        }
        String action = PRIVILEGE_ACTIONS[index];
        if (!action.isEmpty()) {
            drawBitmap(canvas, buttonView, new RectF(x + 76.0f, y + 183.0f, x + 206.0f, y + 233.0f));
            drawButtonText(canvas, action, x + 141.0f, y + ACTION_CENTER_Y);
        }
        drawDescriptionText(canvas, PRIVILEGE_LINES[index], x + 141.0f, y + DESCRIPTION_CENTER_Y);
    }

    private void drawIconFromRecoveredCard(Canvas canvas, Bitmap bitmap, RectF destination) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        bitmapSource.set(
                Math.round(bitmap.getWidth() * 0.13f),
                Math.round(bitmap.getHeight() * 0.18f),
                Math.round(bitmap.getWidth() * 0.87f),
                Math.round(bitmap.getHeight() * 0.71f));
        bitmapDestination.set(destination);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, bitmapSource, bitmapDestination, paint);
    }

    private void drawTitleText(Canvas canvas, String value, float x, float centerY) {
        textPaint.setTypeface(originalTypeface);
        textPaint.setTextSize(TITLE_TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setShadowLayer(3.0f, 1.5f, 2.0f, Color.rgb(48, 91, 183));
        drawTextCentered(canvas, value, x, centerY);
        textPaint.clearShadowLayer();
    }

    private void drawButtonText(Canvas canvas, String value, float x, float centerY) {
        textPaint.setTypeface(originalTypeface);
        textPaint.setTextSize(ACTION_TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(ACTION_COLOR);
        textPaint.setShadowLayer(1.5f, 0.0f, 1.0f, Color.WHITE);
        drawTextCentered(canvas, value, x, centerY);
        textPaint.clearShadowLayer();
    }

    private void drawDescriptionText(Canvas canvas, String value, float x, float centerY) {
        String[] lines = value.split("\\n");
        textPaint.setTypeface(originalTypeface);
        textPaint.setTextSize(DESCRIPTION_TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(DESCRIPTION_COLOR);
        textPaint.setShadowLayer(1.0f, 0.0f, 1.0f, Color.argb(160, 255, 255, 255));
        drawMultilineTextCentered(canvas, lines, x, centerY, DESCRIPTION_LINE_SPACING);
        textPaint.clearShadowLayer();
    }

    private void drawTextCentered(Canvas canvas, String value, float x, float centerY) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float baseline = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2.0f;
        canvas.drawText(value, x, baseline, textPaint);
    }

    private void drawMultilineTextCentered(
            Canvas canvas, String[] lines, float x, float centerY, float lineSpacing) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float firstBaseline =
                centerY
                        - (lines.length - 1) * lineSpacing / 2.0f
                        - (fontMetrics.ascent + fontMetrics.descent) / 2.0f;
        for (int index = 0; index < lines.length; index++) {
            canvas.drawText(lines[index], x, firstBaseline + index * lineSpacing, textPaint);
        }
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap, RectF destination) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        bitmapSource.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        bitmapDestination.set(destination);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, bitmapSource, bitmapDestination, paint);
    }

    private static Bitmap loadBitmap(Resources resources, int resourceId) {
        return BitmapFactory.decodeResource(resources, resourceId);
    }

    private static Typeface loadOriginalTypeface(Resources resources) {
        try {
            return Typeface.createFromAsset(resources.getAssets(), ORIGINAL_FONT_ASSET);
        } catch (RuntimeException exception) {
            return Typeface.DEFAULT_BOLD;
        }
    }
}
