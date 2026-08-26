package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.view.MotionEvent;
import com.nanbeiyule.game.mahjong.TaizhouPlayerInfoLayout;
import java.util.Locale;

/**
 * {@code Common/CSB/GameBase/PlayerInfoLayer.csb} 玩家信息面板。
 *
 * <p>右侧统计区按 {@code PlayerInfoView.luac:700-737} 的会员判定分两态：会员有效走
 * {@code KW_NORMAL}、过期走 {@code KW_BLUR} 马赛克 + {@code _KW_BTN_BUY_VIP}「开通查看」。
 * 南北娱乐当前没有对局统计接口，因此会员有效时数值仍按 CSB 默认的 {@code --} 占位，
 * 不编造战绩。
 *
 * <p>{@code _KW_PLAYER_INFO}（钻石/房卡）只在自己座位显示（:16），{@code _KW_BLOCK}
 * 「屏蔽TA」只在他人座位显示（:18）。
 */
@SuppressLint("ViewConstructor")
final class TaizhouPlayerInfoView extends TaizhouToolView {
    interface Actions {
        void onKick();

        void onBuyMembership();

        void onBlockChanged(TaizhouPlayerBlockStore.Type type, boolean blocked);
    }

    private static final String PLACEHOLDER = "--";

    private final Actions actions;
    private final Bitmap panel;
    private final Bitmap ornamentLeft;
    private final Bitmap ornamentRight;
    private final Bitmap dividerThick;
    private final Bitmap dividerThin;
    private final Bitmap vipPanel;
    private final Bitmap sectionTitle;
    private final Bitmap kick;
    private final Bitmap close;
    private final Bitmap buyVip;
    private final Bitmap blurSmall;
    private final Bitmap blurWide;
    private final Bitmap blurFlat;
    private final Bitmap checkboxOn;
    private final Bitmap checkboxOff;
    private final Bitmap diamond;
    private final Bitmap roomCard;
    private final Bitmap avatar;
    private TaizhouPlayerInfoState state;
    private Runnable dismissAction = () -> {};

    TaizhouPlayerInfoView(
            Context context, TaizhouPlayerInfoState state, Bitmap avatar, Actions actions) {
        super(context);
        this.state = state;
        this.avatar = avatar;
        this.actions = actions;
        panel = bitmap(R.drawable.taizhou_player_info_panel);
        ornamentLeft = bitmap(R.drawable.taizhou_player_info_ornament_left);
        ornamentRight = bitmap(R.drawable.taizhou_player_info_ornament_right);
        dividerThick = bitmap(R.drawable.taizhou_player_info_divider_thick);
        dividerThin = bitmap(R.drawable.taizhou_player_info_divider_thin);
        vipPanel = bitmap(R.drawable.taizhou_player_info_vip_panel);
        sectionTitle = bitmap(R.drawable.taizhou_player_info_section_title);
        kick = bitmap(R.drawable.taizhou_player_info_kick);
        close = bitmap(R.drawable.taizhou_player_info_close);
        buyVip = bitmap(R.drawable.taizhou_player_info_buy_vip);
        blurSmall = bitmap(R.drawable.taizhou_player_info_blur_small);
        blurWide = bitmap(R.drawable.taizhou_player_info_blur_wide);
        blurFlat = bitmap(R.drawable.taizhou_player_info_blur_flat);
        checkboxOn = bitmap(R.drawable.taizhou_player_info_checkbox_on);
        checkboxOff = bitmap(R.drawable.taizhou_player_info_checkbox_off);
        diamond = bitmap(R.drawable.taizhou_player_info_diamond);
        roomCard = bitmap(R.drawable.taizhou_player_info_room_card);
        setContentDescription("玩家信息");
    }

    void setDismissAction(Runnable action) {
        dismissAction = action == null ? () -> {} : action;
    }

    void update(TaizhouPlayerInfoState next) {
        state = next;
        invalidate();
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        // 推断: _KW_PANAEL_BG 的遮罩色解析工具读不到，沿用同族弹层已验证的压暗值。
        fillPaint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(
                0.0f, 0.0f, TaizhouPlayerInfoLayout.DESIGN_WIDTH,
                TaizhouPlayerInfoLayout.DESIGN_HEIGHT, fillPaint);
        TaizhouTreasureCanvas.drawNineSlice(
                canvas,
                panel,
                new RectF(
                        TaizhouPlayerInfoLayout.BACKGROUND_LEFT,
                        TaizhouPlayerInfoLayout.BACKGROUND_TOP,
                        TaizhouPlayerInfoLayout.BACKGROUND_LEFT
                                + TaizhouPlayerInfoLayout.BACKGROUND_WIDTH,
                        TaizhouPlayerInfoLayout.BACKGROUND_TOP
                                + TaizhouPlayerInfoLayout.BACKGROUND_HEIGHT),
                TaizhouPlayerInfoLayout.BACKGROUND_CAP_X,
                TaizhouPlayerInfoLayout.BACKGROUND_CAP_Y,
                TaizhouPlayerInfoLayout.BACKGROUND_CAP_WIDTH,
                TaizhouPlayerInfoLayout.BACKGROUND_CAP_HEIGHT,
                bitmapPaint);
        drawNode(canvas, ornamentLeft, TaizhouPlayerInfoLayout.ORNAMENT_LEFT);
        drawNode(canvas, ornamentRight, TaizhouPlayerInfoLayout.ORNAMENT_RIGHT);
        drawNode(canvas, dividerThick, TaizhouPlayerInfoLayout.DIVIDER_THICK);
        drawNode(canvas, dividerThin, TaizhouPlayerInfoLayout.DIVIDER_THIN);
        drawIdentity(canvas);
        drawVipSection(canvas);
        if (state.self()) {
            drawWallet(canvas);
        } else {
            drawBlockRow(canvas);
        }
        if (state.kickVisible()) {
            drawNode(canvas, kick, TaizhouPlayerInfoLayout.BUTTON_KICK);
            drawText(
                    canvas,
                    "请出房间",
                    TaizhouPlayerInfoLayout.BUTTON_KICK.centerX(),
                    TaizhouPlayerInfoLayout.BUTTON_KICK.centerY() + 13.0f,
                    TaizhouPlayerInfoLayout.KICK_FONT_SIZE,
                    TaizhouPlayerInfoLayout.TEXT_COLOR);
        }
        drawNode(canvas, close, TaizhouPlayerInfoLayout.BUTTON_CLOSE);
    }

    private void drawIdentity(Canvas canvas) {
        if (avatar != null) {
            drawNode(canvas, avatar, TaizhouPlayerInfoLayout.HEAD);
        }
        leftText(
                canvas,
                state.nickname(),
                TaizhouPlayerInfoLayout.NICKNAME_LEFT,
                TaizhouPlayerInfoLayout.NICKNAME_CENTER_Y,
                TaizhouPlayerInfoLayout.NICKNAME_FONT_SIZE);
        leftText(
                canvas,
                "ID:" + state.playerId(),
                TaizhouPlayerInfoLayout.ID_LEFT,
                TaizhouPlayerInfoLayout.ID_CENTER_Y,
                TaizhouPlayerInfoLayout.ID_FONT_SIZE);
        // 原版 _KW_TEXT_IP / _KW_TEXT_GPS / _KW_BTN_GPS 由 msgPlayerGps 与定位服务填充，
        // 南北娱乐没有该链路，按 PlayerInfoView.luac:228-230 金币场分支保持隐藏。
    }

    private void drawVipSection(Canvas canvas) {
        bitmapPaint.setAlpha(TaizhouPlayerInfoLayout.VIP_ALPHA);
        drawBitmap(
                canvas,
                vipPanel,
                new RectF(
                        TaizhouPlayerInfoLayout.VIP_LEFT,
                        TaizhouPlayerInfoLayout.VIP_TOP,
                        TaizhouPlayerInfoLayout.VIP_LEFT + TaizhouPlayerInfoLayout.VIP_WIDTH,
                        TaizhouPlayerInfoLayout.VIP_TOP + TaizhouPlayerInfoLayout.VIP_HEIGHT));
        bitmapPaint.setAlpha(255);
        drawNode(canvas, sectionTitle, TaizhouPlayerInfoLayout.VIP_TITLE_BACKGROUND);
        drawText(
                canvas,
                state.self() ? "我的信息" : "TA的信息",
                TaizhouPlayerInfoLayout.VIP_TITLE_TEXT.centerX(),
                TaizhouPlayerInfoLayout.VIP_TITLE_TEXT.centerY() + 13.0f,
                TaizhouPlayerInfoLayout.VIP_LABEL_FONT_SIZE,
                TaizhouPlayerInfoLayout.TEXT_COLOR);
        if (state.self()) {
            label(canvas, "我的总胜率", TaizhouPlayerInfoLayout.VIP_SELF_LABEL_TOTAL_WIN_RATE);
            label(canvas, "出牌速度", TaizhouPlayerInfoLayout.VIP_SELF_LABEL_SPEED);
            label(canvas, "离线次数", TaizhouPlayerInfoLayout.VIP_SELF_LABEL_OFFLINE);
            label(canvas, "解散次数", TaizhouPlayerInfoLayout.VIP_SELF_LABEL_DISMISS);
        } else {
            label(canvas, "与我对局数", TaizhouPlayerInfoLayout.VIP_OTHER_LABEL_ROUNDS);
            label(canvas, "对我胜率", TaizhouPlayerInfoLayout.VIP_OTHER_LABEL_WIN_RATE);
            label(canvas, "牌友总胜率", TaizhouPlayerInfoLayout.VIP_OTHER_LABEL_TOTAL_WIN_RATE);
            label(canvas, "出牌速度", TaizhouPlayerInfoLayout.VIP_OTHER_LABEL_SPEED);
            label(canvas, "离线次数", TaizhouPlayerInfoLayout.VIP_OTHER_LABEL_OFFLINE);
            label(canvas, "解散次数", TaizhouPlayerInfoLayout.VIP_OTHER_LABEL_DISMISS);
            label(canvas, "我胜对方分", TaizhouPlayerInfoLayout.VIP_OTHER_LABEL_SCORE);
        }
        if (state.membershipActive()) {
            drawStatPlaceholders(canvas);
        } else {
            drawBlur(canvas);
            drawNode(canvas, buyVip, TaizhouPlayerInfoLayout.BUTTON_BUY_VIP);
            drawText(
                    canvas,
                    "开通查看",
                    TaizhouPlayerInfoLayout.BUTTON_BUY_VIP.centerX(),
                    TaizhouPlayerInfoLayout.BUTTON_BUY_VIP.centerY() + 16.0f,
                    TaizhouPlayerInfoLayout.BUY_VIP_FONT_SIZE,
                    TaizhouPlayerInfoLayout.TEXT_COLOR);
        }
    }

    /** 会员有效但没有战绩接口时，保持 CSB 默认的 {@code --} 占位，不编造数值。 */
    private void drawStatPlaceholders(Canvas canvas) {
        for (TaizhouPlayerInfoLayout.Node node : blurNodes()) {
            drawText(
                    canvas,
                    PLACEHOLDER,
                    node.centerX(),
                    node.centerY() + 16.0f,
                    42.0f,
                    TaizhouPlayerInfoLayout.TEXT_COLOR);
        }
    }

    private void drawBlur(Canvas canvas) {
        for (TaizhouPlayerInfoLayout.Node node : blurNodes()) {
            Bitmap mosaic = node.width() >= 126.0f
                    ? blurFlat
                    : node.width() >= 122.0f ? blurWide : blurSmall;
            drawNode(canvas, mosaic, node);
        }
    }

    private TaizhouPlayerInfoLayout.Node[] blurNodes() {
        return state.self()
                ? TaizhouPlayerInfoLayout.VIP_SELF_BLUR
                : TaizhouPlayerInfoLayout.VIP_OTHER_BLUR;
    }

    private void drawWallet(Canvas canvas) {
        drawNode(canvas, diamond, TaizhouPlayerInfoLayout.WALLET_DIAMOND_ICON);
        drawNode(canvas, roomCard, TaizhouPlayerInfoLayout.WALLET_ROOM_CARD_ICON);
        leftText(
                canvas,
                String.valueOf(state.diamondBalance()),
                TaizhouPlayerInfoLayout.WALLET_DIAMOND_TEXT_LEFT,
                TaizhouPlayerInfoLayout.WALLET_TEXT_CENTER_Y,
                TaizhouPlayerInfoLayout.WALLET_FONT_SIZE);
        leftText(
                canvas,
                String.valueOf(state.roomCardBalance()),
                TaizhouPlayerInfoLayout.WALLET_ROOM_CARD_TEXT_LEFT,
                TaizhouPlayerInfoLayout.WALLET_TEXT_CENTER_Y,
                TaizhouPlayerInfoLayout.WALLET_FONT_SIZE);
    }

    private void drawBlockRow(Canvas canvas) {
        drawText(
                canvas,
                "屏蔽TA：",
                TaizhouPlayerInfoLayout.BLOCK_LABEL.centerX(),
                TaizhouPlayerInfoLayout.BLOCK_LABEL.centerY() + 13.0f,
                TaizhouPlayerInfoLayout.BLOCK_FONT_SIZE,
                TaizhouPlayerInfoLayout.TEXT_COLOR);
        checkbox(canvas, TaizhouPlayerInfoLayout.BLOCK_VOICE, state.blockedVoice());
        checkbox(canvas, TaizhouPlayerInfoLayout.BLOCK_CHAT, state.blockedChat());
        checkbox(canvas, TaizhouPlayerInfoLayout.BLOCK_EMOJIS, state.blockedEmojis());
        leftText(canvas, "语音", TaizhouPlayerInfoLayout.BLOCK_VOICE_TEXT_LEFT,
                TaizhouPlayerInfoLayout.BLOCK_TEXT_CENTER_Y,
                TaizhouPlayerInfoLayout.BLOCK_FONT_SIZE);
        leftText(canvas, "俏皮话", TaizhouPlayerInfoLayout.BLOCK_CHAT_TEXT_LEFT,
                TaizhouPlayerInfoLayout.BLOCK_TEXT_CENTER_Y,
                TaizhouPlayerInfoLayout.BLOCK_FONT_SIZE);
        leftText(canvas, "丢道具", TaizhouPlayerInfoLayout.BLOCK_EMOJIS_TEXT_LEFT,
                TaizhouPlayerInfoLayout.BLOCK_TEXT_CENTER_Y,
                TaizhouPlayerInfoLayout.BLOCK_FONT_SIZE);
    }

    private void checkbox(Canvas canvas, TaizhouPlayerInfoLayout.Node node, boolean selected) {
        drawNode(canvas, selected ? checkboxOn : checkboxOff, node);
    }

    private void label(Canvas canvas, String value, TaizhouPlayerInfoLayout.Node node) {
        drawText(
                canvas,
                value,
                node.centerX(),
                node.centerY() + 13.0f,
                TaizhouPlayerInfoLayout.VIP_LABEL_FONT_SIZE,
                TaizhouPlayerInfoLayout.TEXT_COLOR);
    }

    private void leftText(Canvas canvas, String value, float left, float centerY, float size) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(size);
        textPaint.setColor(TaizhouPlayerInfoLayout.TEXT_COLOR);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(
                value == null ? "" : value,
                left,
                centerY - (metrics.ascent + metrics.descent) * 0.5f,
                textPaint);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        float x = designX(event);
        float y = designY(event);
        if (TaizhouPlayerInfoLayout.BUTTON_CLOSE.contains(x, y)) {
            performClick();
            dismissAction.run();
            return true;
        }
        if (state.kickVisible() && TaizhouPlayerInfoLayout.BUTTON_KICK.contains(x, y)) {
            performClick();
            actions.onKick();
            return true;
        }
        if (!state.membershipActive()
                && TaizhouPlayerInfoLayout.BUTTON_BUY_VIP.contains(x, y)) {
            performClick();
            actions.onBuyMembership();
            return true;
        }
        if (!state.self()) {
            if (TaizhouPlayerInfoLayout.BLOCK_VOICE.contains(x, y)) {
                toggle(TaizhouPlayerBlockStore.Type.VOICE, state.blockedVoice());
            } else if (TaizhouPlayerInfoLayout.BLOCK_CHAT.contains(x, y)) {
                toggle(TaizhouPlayerBlockStore.Type.CHAT, state.blockedChat());
            } else if (TaizhouPlayerInfoLayout.BLOCK_EMOJIS.contains(x, y)) {
                toggle(TaizhouPlayerBlockStore.Type.EMOJIS, state.blockedEmojis());
            }
        }
        return true;
    }

    /** {@code onTouchEventCheckVoice}(:824-827)：会员过期时回弹并弹开通引导。 */
    private void toggle(TaizhouPlayerBlockStore.Type type, boolean current) {
        performClick();
        if (!state.membershipActive()) {
            actions.onBuyMembership();
            return;
        }
        actions.onBlockChanged(type, !current);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void drawNode(Canvas canvas, Bitmap bitmap, TaizhouPlayerInfoLayout.Node node) {
        drawBitmap(canvas, bitmap, new RectF(node.left(), node.top(), node.right(), node.bottom()));
    }
}
