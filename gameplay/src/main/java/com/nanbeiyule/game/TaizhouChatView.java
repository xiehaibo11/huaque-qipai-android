package com.nanbeiyule.game;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

/** Canvas reconstruction of Common/CSB/GameBase/ChatLayer.csb. */
@SuppressLint("ViewConstructor")
final class TaizhouChatView extends TaizhouToolView {
    interface Actions {
        void onQuickPhrase(int index);

        void onEmoji(int index);

        void onVoiceMessage(String messageId);
    }

    private enum Page { TALK, EMOJI, RECORD }

    /** 55px atlas frame scaled by Chat/View.lua's setScale(1.68). */
    private static final float EMOJI_ICON_SIZE = 92.4f;

    private final TaizhouRoomToolsState state;
    private final int mySeat;
    private final Actions actions;
    private final Bitmap panelBackground;
    private final Bitmap contentBackground;
    private final Bitmap itemBackground;
    private final Bitmap emojiBackground;
    private final Bitmap recordSelfBackground;
    private final Bitmap recordOtherBackground;
    private final Bitmap[] talkTabs = new Bitmap[2];
    private final Bitmap[] emojiTabs = new Bitmap[2];
    private final Bitmap[] recordTabs = new Bitmap[2];
    private final Bitmap[] emojiIcons = new Bitmap[TaizhouChatEmojiResources.count()];
    private final Bitmap voiceFlag;
    private final TaizhouChatRecordScroll recordScroll;
    private Runnable dismissAction = () -> {};
    private Page page = Page.TALK;
    private float panelOffset = 660.0f;
    private float recordDownY;
    private float recordLastY;
    private boolean recordTouch;
    private boolean recordDragging;
    private boolean closing;

    TaizhouChatView(
            Context context,
            TaizhouRoomToolsState state,
            int mySeat,
            Actions actions) {
        super(context);
        this.state = state == null ? TaizhouRoomToolsState.empty("") : state;
        this.mySeat = mySeat;
        this.actions = actions;
        panelBackground = bitmap(TaizhouWaitingToolLayout.CHAT_PANEL_BACKGROUND);
        contentBackground = bitmap(TaizhouWaitingToolLayout.CHAT_CONTENT_BACKGROUND);
        itemBackground = bitmap(R.drawable.taizhou_tool_chat_talk_item_bg);
        emojiBackground = bitmap(R.drawable.taizhou_tool_chat_emoji_bg);
        recordSelfBackground = bitmap(R.drawable.taizhou_tool_chat_record_self_bg);
        recordOtherBackground = bitmap(R.drawable.taizhou_tool_chat_record_other_bg);
        talkTabs[0] = bitmap(R.drawable.taizhou_tool_chat_talk_tab);
        talkTabs[1] = bitmap(R.drawable.taizhou_tool_chat_talk_tab_selected);
        emojiTabs[0] = bitmap(R.drawable.taizhou_tool_chat_emoji_tab);
        emojiTabs[1] = bitmap(R.drawable.taizhou_tool_chat_emoji_tab_selected);
        recordTabs[0] = bitmap(R.drawable.taizhou_tool_chat_record_tab);
        recordTabs[1] = bitmap(R.drawable.taizhou_tool_chat_record_tab_selected);
        for (int index = 0; index < emojiIcons.length; index++) {
            emojiIcons[index] = bitmap(TaizhouChatEmojiResources.drawableAt(index));
        }
        voiceFlag = bitmap(R.drawable.taizhou_tool_chat_speak_flag);
        recordScroll = new TaizhouChatRecordScroll(this.state.messages().size());
        setContentDescription("聊天");
        post(this::animateIn);
    }

    void setDismissAction(Runnable action) {
        dismissAction = action == null ? () -> {} : action;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        fillPaint.setColor(Color.argb(72, 0, 0, 0));
        canvas.drawRect(0.0f, 0.0f, 1920.0f, 1080.0f, fillPaint);
        canvas.save();
        canvas.translate(panelOffset, 0.0f);
        drawBitmap(canvas, panelBackground, rect(TaizhouWaitingToolLayout.CHAT_PANEL));
        drawBitmap(canvas, contentBackground, rect(TaizhouWaitingToolLayout.CHAT_CONTENT));
        drawTab(canvas, talkTabs, page == Page.TALK, 1877.0f, 209.0f);
        drawTab(canvas, emojiTabs, page == Page.EMOJI, 1868.0f, 388.0f);
        drawTab(canvas, recordTabs, page == Page.RECORD, 1869.0f, 563.0f);
        if (page == Page.TALK) {
            drawPhrases(canvas);
        } else if (page == Page.EMOJI) {
            drawEmoji(canvas);
        } else {
            drawRecords(canvas);
        }
        canvas.restore();
    }

    private void drawTab(Canvas canvas, Bitmap[] tabs, boolean selected, float x, float y) {
        drawCentered(canvas, tabs[selected ? 1 : 0], x, y, 81.0f, 175.0f);
    }

    private void drawPhrases(Canvas canvas) {
        List<String> phrases = state.quickPhrases();
        int count = Math.min(9, phrases.size());
        for (int index = 0; index < count; index++) {
            float top =
                    TaizhouWaitingToolLayout.CHAT_CONTENT.top()
                            + index * TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_ROW_HEIGHT;
            drawBitmap(
                    canvas,
                    itemBackground,
                    new RectF(
                            TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_ITEM_LEFT,
                            top,
                            TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_ITEM_LEFT
                                    + TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_ITEM_WIDTH,
                            top + TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_ITEM_HEIGHT));
            drawPhrase(canvas, phrases.get(index), top);
        }
        if (count == 0) {
            drawText(canvas, "正在加载俏皮话...", 1548.0f, 550.0f, 30.0f,
                    Color.rgb(135, 92, 48));
        }
    }

    private void drawPhrase(Canvas canvas, String text, float top) {
        RectF box =
                new RectF(
                        TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_TEXT_LEFT,
                        top - 2.5f,
                        TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_TEXT_LEFT
                                + TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_TEXT_WIDTH,
                        top + 77.5f);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_TEXT_SIZE);
        textPaint.setColor(TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_TEXT_COLOR);
        textPaint.setStyle(Paint.Style.FILL);
        List<String> lines =
                wrappedLines(text, TaizhouWaitingToolLayout.CHAT_QUICK_PHRASE_TEXT_WIDTH);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float lineHeight = metrics.descent - metrics.ascent;
        float baseline =
                box.centerY()
                        - (metrics.ascent + (lines.size() - 1) * lineHeight + metrics.descent)
                                / 2.0f;
        int save = canvas.save();
        canvas.clipRect(box);
        for (String line : lines) {
            canvas.drawText(line, box.left, baseline, textPaint);
            baseline += lineHeight;
        }
        canvas.restoreToCount(save);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawEmoji(Canvas canvas) {
        int count = Math.min(state.emojiCount(), emojiIcons.length);
        for (int index = 0; index < count; index++) {
            int row = index / 4;
            int column = index % 4;
            float centerX = 1348.0f + column * 128.5f;
            float centerY = 195.5f + row * 135.0f;
            drawCentered(canvas, emojiBackground, centerX, centerY, 117.0f, 116.0f);
            // Chat/View.lua loads the 55x55 atlas frame into the placeholder ImageView and then
            // applies setScale(1.68), so the drawn icon is 92.4 square, not the raw frame size.
            drawCentered(canvas, emojiIcons[index], centerX, centerY,
                    EMOJI_ICON_SIZE, EMOJI_ICON_SIZE);
        }
        if (count == 0) {
            drawText(canvas, "正在加载表情...", 1548.0f, 550.0f, 30.0f,
                    Color.rgb(135, 92, 48));
        }
    }

    private void drawRecords(Canvas canvas) {
        List<TaizhouRoomToolsState.Message> messages = state.messages();
        if (messages.isEmpty()) {
            drawText(canvas, "暂无聊天记录", 1548.0f, 550.0f, 32.0f,
                    Color.rgb(135, 92, 48));
            return;
        }
        int save = canvas.save();
        canvas.clipRect(
                TaizhouWaitingToolLayout.CHAT_CONTENT.left(),
                TaizhouWaitingToolLayout.CHAT_CONTENT.top(),
                TaizhouWaitingToolLayout.CHAT_CONTENT.right(),
                TaizhouWaitingToolLayout.CHAT_CONTENT.bottom());
        for (int position = 0; position < messages.size(); position++) {
            TaizhouRoomToolsState.Message message = messages.get(position);
            boolean self = message.senderSeat() == mySeat;
            float centerY =
                    TaizhouChatRecordScroll.ROW_TOP
                            + position * TaizhouChatRecordScroll.ROW_HEIGHT
                            - recordScroll.offset();
            if (centerY < TaizhouWaitingToolLayout.CHAT_CONTENT.top() - 72.5f
                    || centerY > TaizhouWaitingToolLayout.CHAT_CONTENT.bottom() + 72.5f) {
                continue;
            }
            float centerX = self ? 1590.0f : 1500.0f;
            drawCentered(
                    canvas,
                    self ? recordSelfBackground : recordOtherBackground,
                    centerX,
                    centerY,
                    306.0f,
                    100.0f);
            if ("VOICE".equals(message.type())) {
                drawCentered(canvas, voiceFlag, centerX, centerY, 72.0f, 56.0f);
                drawText(canvas, Math.max(1, message.durationMillis() / 1000) + "\"",
                        centerX + 105.0f, centerY + 12.0f, 24.0f, Color.rgb(116, 74, 35));
            } else if ("EMOJI".equals(message.type())) {
                int index = message.contentIndex();
                if (index >= 0 && index < emojiIcons.length) {
                    drawCentered(canvas, emojiIcons[index], centerX, centerY, 55.0f, 55.0f);
                }
            } else {
                drawText(canvas, abbreviate(message.text(), 16), centerX, centerY + 10.0f,
                        23.0f, Color.rgb(99, 67, 40));
            }
        }
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (closing) {
            return true;
        }
        float x = designX(event) - panelOffset;
        float y = designY(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (page == Page.RECORD
                        && TaizhouWaitingToolLayout.CHAT_CONTENT.contains(x, y)) {
                    recordTouch = true;
                    recordDragging = false;
                    recordDownY = y;
                    recordLastY = y;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (recordTouch) {
                    recordScroll.dragBy(y - recordLastY);
                    recordLastY = y;
                    recordDragging = recordDragging || Math.abs(y - recordDownY) > 12.0f;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                recordTouch = false;
                recordDragging = false;
                return true;
            case MotionEvent.ACTION_UP:
                if (recordTouch) {
                    boolean wasDragging = recordDragging;
                    recordTouch = false;
                    recordDragging = false;
                    if (!wasDragging) {
                        voiceAt(x, y);
                        performClick();
                    }
                    return true;
                }
                break;
            default:
                return true;
        }
        if (!TaizhouWaitingToolLayout.CHAT_PANEL.contains(x, y)) {
            performClick();
            animateOut(dismissAction);
            return true;
        }
        if (TaizhouWaitingToolLayout.CHAT_TALK_TAB.contains(x, y)) {
            page = Page.TALK;
        } else if (TaizhouWaitingToolLayout.CHAT_EMOJI_TAB.contains(x, y)) {
            page = Page.EMOJI;
        } else if (TaizhouWaitingToolLayout.CHAT_RECORD_TAB.contains(x, y)) {
            page = Page.RECORD;
        } else if (page == Page.TALK) {
            int index = TaizhouWaitingToolLayout.quickPhraseAt(x, y, state.quickPhrases().size());
            if (index >= 0) {
                actions.onQuickPhrase(index);
                animateOut(dismissAction);
            }
        } else if (page == Page.EMOJI) {
            int index = TaizhouWaitingToolLayout.emojiAt(x, y, state.emojiCount());
            if (index >= 0) {
                actions.onEmoji(index);
                animateOut(dismissAction);
            }
        } else {
            voiceAt(x, y);
        }
        performClick();
        invalidate();
        return true;
    }

    private void voiceAt(float x, float y) {
        if (!TaizhouWaitingToolLayout.CHAT_CONTENT.contains(x, y)) {
            return;
        }
        List<TaizhouRoomToolsState.Message> messages = state.messages();
        int index = recordScroll.messageAt(y);
        if (index >= 0 && index < messages.size()) {
            TaizhouRoomToolsState.Message message = messages.get(index);
            if ("VOICE".equals(message.type())) {
                actions.onVoiceMessage(message.messageId());
            }
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void animateIn() {
        animate(660.0f, 0.0f, null);
    }

    private void animateOut(Runnable completion) {
        closing = true;
        animate(panelOffset, 660.0f, completion);
    }

    private void animate(float from, float to, Runnable completion) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(200L);
        animator.addUpdateListener(
                value -> {
                    panelOffset = (float) value.getAnimatedValue();
                    invalidate();
                });
        if (completion != null) {
            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    completion.run();
                }
            });
        }
        animator.start();
    }

    private static String abbreviate(String value, int limit) {
        if (value == null || value.length() <= limit) {
            return value == null ? "" : value;
        }
        return value.substring(0, limit) + "...";
    }

    private List<String> wrappedLines(String value, float width) {
        String text = value == null ? "" : value;
        if (text.isEmpty()) {
            return List.of("");
        }
        List<String> lines = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int count =
                    Math.max(
                            1,
                            textPaint.breakText(
                                    text, start, text.length(), true, width, null));
            lines.add(text.substring(start, start + count));
            start += count;
        }
        return lines;
    }

}
