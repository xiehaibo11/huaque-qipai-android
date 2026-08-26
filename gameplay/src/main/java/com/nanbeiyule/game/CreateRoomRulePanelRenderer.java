package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.List;

/**
 * 右侧动态规则面板 {@code _KW_PANEL_GAME_RULE_DYNAMIC}。
 *
 * <p>行容器 {@code _KW_PANEL_OPTIONS_ITEM}、行标题 {@code KW_ITEM_TEXT}、分割线 {@code Image_8}、
 * 单选 {@code _KW_RADIO_ITEM}、复选 {@code _KW_CHECK_BOX_ITEM} 与提示 {@code KW_PANEL_TIPS}
 * 的几何、字号和颜色全部取自 {@code CreateBoxRoomDynamic.csb}；提示气泡的动态尺寸来自
 * {@code lobby/Modules/CreateBoxRoom/View.lua:828-839}。
 */
final class CreateRoomRulePanelRenderer {
    private static final int BROWN = Color.rgb(163, 111, 72);
    private static final int ROW_TITLE = Color.rgb(194, 108, 70);
    private static final int DISABLED = Color.rgb(160, 160, 160);

    private final CreateRoomDrawableSet drawables;
    private final Paint bitmapPaint;
    private final Paint textPaint;
    private final RectF destination = new RectF();

    CreateRoomRulePanelRenderer(
            CreateRoomDrawableSet drawables, Paint bitmapPaint, Paint textPaint) {
        this.drawables = drawables;
        this.bitmapPaint = bitmapPaint;
        this.textPaint = textPaint;
    }

    void draw(
            Canvas canvas,
            CreateRoomState state,
            float scroll,
            String openTipNode,
            String openDropdownNode) {
        if (state == null) {
            return;
        }
        List<CreateRoomRows.Row> rows = CreateRoomRows.flatten(state);
        canvas.save();
        canvas.clipRect(
                CreateRoomLayout.RULE_LIST_LEFT,
                CreateRoomLayout.RULE_LIST_TOP,
                CreateRoomLayout.RULE_LIST_RIGHT,
                CreateRoomLayout.RULE_LIST_BOTTOM);
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            CreateRoomRows.Row row = rows.get(rowIndex);
            float top = CreateRoomLayout.RULE_LIST_TOP
                    + rowIndex * CreateRoomLayout.RULE_ROW_STRIDE - scroll;
            if (top > CreateRoomLayout.RULE_LIST_BOTTOM || top + 100 < CreateRoomLayout.RULE_LIST_TOP) {
                continue;
            }
            // Image_8 是 KW_ITEM_TEXT 的子节点，标题隐藏时分割线一起隐藏
            // （View.lua:738-743 只在 text 非空且 line==1 时可见）。
            if (row.lineIndex() == 0 && !row.group().title().isBlank()) {
                drawTextAtCenterY(
                        canvas,
                        row.group().title(),
                        CreateRoomLayout.RULE_TITLE_LEFT,
                        top + CreateRoomLayout.RULE_TITLE_CENTER_Y,
                        CreateRoomLayout.RULE_TITLE_TEXT_SIZE,
                        ROW_TITLE);
                drawBitmap(
                        canvas,
                        drawables.rowLine,
                        CreateRoomLayout.RULE_LINE_LEFT,
                        top + CreateRoomLayout.RULE_LINE_TOP,
                        CreateRoomLayout.RULE_LINE_RIGHT,
                        top + CreateRoomLayout.RULE_LINE_BOTTOM);
            }
            drawOptions(canvas, state, row, top, openTipNode, openDropdownNode);
        }
        canvas.restore();
    }

    private void drawOptions(
            Canvas canvas,
            CreateRoomState state,
            CreateRoomRows.Row row,
            float top,
            String openTipNode,
            String openDropdownNode) {
        float[] positions = CreateRoomLayout.optionXs(row.options().size());
        for (int index = 0; index < row.options().size(); index++) {
            CreateRoomRuleConfig.Option option = row.options().get(index);
            if (!state.isVisible(option.nodeName())) {
                continue;
            }
            float centerX = CreateRoomLayout.RULE_CONTENT_LEFT
                    + positions[index] + option.diffNodeX();
            float centerY = top + 50.0f;
            boolean enabled = state.isEnabled(option.nodeName());
            boolean checked = state.isSelected(option.nodeName());
            boolean radio = row.group().type() == CreateRoomRuleConfig.Type.RADIO;
            Bitmap background = radio ? drawables.radioBackground : drawables.checkboxBackground;
            Bitmap mark = radio ? drawables.radioChecked : drawables.checkboxChecked;
            // CheckBox 未开启 ignoreContentAdaptWithSize，底图与勾选图都被拉伸到节点尺寸：
            // _KW_RADIO_ITEM 82x82、_KW_CHECK_BOX_ITEM 76x80。
            float boxWidth = radio
                    ? CreateRoomLayout.RADIO_WIDTH
                    : CreateRoomLayout.CHECKBOX_WIDTH;
            float boxHeight = radio
                    ? CreateRoomLayout.RADIO_HEIGHT
                    : CreateRoomLayout.CHECKBOX_HEIGHT;
            bitmapPaint.setAlpha(enabled ? 255 : 120);
            drawBitmap(canvas, background, centerX - boxWidth * 0.5f,
                    centerY - boxHeight * 0.5f, centerX + boxWidth * 0.5f,
                    centerY + boxHeight * 0.5f);
            if (checked) {
                drawBitmap(canvas, mark, centerX - boxWidth * 0.5f,
                        centerY - boxHeight * 0.5f, centerX + boxWidth * 0.5f,
                        centerY + boxHeight * 0.5f);
            }
            bitmapPaint.setAlpha(255);
            String optionText = dropdownText(state, option);
            float textLeft = centerX
                    + (radio
                            ? CreateRoomLayout.RADIO_TEXT_OFFSET_X
                            : CreateRoomLayout.CHECKBOX_TEXT_OFFSET_X);
            float textCenterY = centerY
                    + (radio
                            ? CreateRoomLayout.RADIO_TEXT_OFFSET_Y
                            : CreateRoomLayout.CHECKBOX_TEXT_OFFSET_Y);
            float textSize = CreateRoomLayout.OPTION_TEXT_SIZE;
            drawTextAtCenterY(
                    canvas, optionText, textLeft, textCenterY, textSize,
                    enabled ? BROWN : DISABLED);
            float optionTextWidth = textWidth(optionText, textSize);
            if (!option.dropdown().isEmpty()) {
                drawTextAtCenterY(canvas, "▼", textLeft + optionTextWidth + 12,
                        textCenterY, 27, enabled ? BROWN : DISABLED);
            }
            if (!option.tip().isBlank()) {
                // KW_PANEL_TIPS 的 x 由 View.lua:835 设为「选项文字宽度 + 10」。
                float tipX = textLeft + optionTextWidth + CreateRoomLayout.TIP_ICON_GAP;
                float tipCenterY = textCenterY + CreateRoomLayout.TIP_ICON_OFFSET_Y;
                drawBitmap(canvas, drawables.tip, tipX,
                        tipCenterY - CreateRoomLayout.TIP_ICON_SIZE * 0.5f,
                        tipX + CreateRoomLayout.TIP_ICON_SIZE,
                        tipCenterY + CreateRoomLayout.TIP_ICON_SIZE * 0.5f);
                if (option.nodeName().equals(openTipNode)) {
                    drawTip(canvas, option.tip(),
                            tipX + CreateRoomLayout.TIP_BUBBLE_OFFSET_X, tipCenterY);
                }
            }
            if (option.nodeName().equals(openDropdownNode)) {
                drawDropdown(canvas, option, centerX + 60, centerY + 42);
            }
        }
    }

    private void drawTip(Canvas canvas, String tip, float left, float centerY) {
        String[] lines = tip.split("\\n", -1);
        float size = CreateRoomLayout.TIP_TEXT_SIZE;
        float widest = 0.0f;
        for (String line : lines) {
            widest = Math.max(widest, textWidth(line, size));
        }
        float bubbleWidth = widest + 40.0f;
        float bubbleHeight = lines.length * size + 40.0f;
        float top = centerY - bubbleHeight * 0.5f;
        drawBitmap(canvas, drawables.tipBubble, left, top, left + bubbleWidth, top + bubbleHeight);
        float firstCenterY = centerY - (lines.length - 1) * size * 0.5f;
        for (int index = 0; index < lines.length; index++) {
            drawTextAtCenterY(
                    canvas, lines[index], left + 20.0f, firstCenterY + index * size, size, BROWN);
        }
    }

    private void drawDropdown(
            Canvas canvas, CreateRoomRuleConfig.Option option, float left, float top) {
        for (int index = 0; index < option.dropdown().size(); index++) {
            float itemTop = top + index * 66;
            drawBitmap(canvas, drawables.tipBubble, left, itemTop, left + 260, itemTop + 64);
            drawCentered(canvas, option.dropdown().get(index).text(), left + 135,
                    itemTop + 32, 28, BROWN);
        }
    }


    private String dropdownText(CreateRoomState state, CreateRoomRuleConfig.Option option) {
        String effective = state.effectiveNodeName(option.nodeName());
        for (CreateRoomRuleConfig.Dropdown item : option.dropdown()) {
            if (item.nodeName().equals(effective)) {
                return item.text();
            }
        }
        return option.text();
    }

    private float textWidth(String text, float size) {
        textPaint.setTextSize(size);
        return textPaint.measureText(text == null ? "" : text);
    }

    private void drawCentered(
            Canvas canvas, String value, float centerX, float centerY, float size, int color) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(value, centerX - textPaint.measureText(value) * 0.5f,
                centerY - (metrics.ascent + metrics.descent) * 0.5f, textPaint);
    }

    private void drawTextAtCenterY(
            Canvas canvas, String value, float left, float centerY, float size, int color) {
        textPaint.setTextSize(size);
        textPaint.setColor(color);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText(
                value == null ? "" : value,
                left,
                centerY - (metrics.ascent + metrics.descent) * 0.5f,
                textPaint);
    }

    private void drawBitmap(
            Canvas canvas, Bitmap bitmap, float left, float top, float right, float bottom) {
        destination.set(left, top, right, bottom);
        canvas.drawBitmap(bitmap, null, destination, bitmapPaint);
    }
}
