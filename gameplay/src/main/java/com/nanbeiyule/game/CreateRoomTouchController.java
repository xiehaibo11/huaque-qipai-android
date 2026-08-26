package com.nanbeiyule.game;

import android.view.MotionEvent;
import java.util.List;

/** Hit testing and gestures kept outside the Canvas renderer. */
final class CreateRoomTouchController {
    interface Host {
        List<CreateRoomGame> games();
        int selectedGameIndex();
        CreateRoomState state();
        float gameScroll();
        float ruleScroll();
        void setGameScroll(float value);
        void setRuleScroll(float value);
        String openTipNode();
        String openDropdownNode();
        void setOpenTipNode(String value);
        void setOpenDropdownNode(String value);
        void selectGame(int index);
        void stateChanged();
        void createRequested();
        void feedbackRequested();
        void closeRequested();
        boolean hasResult();
        boolean loading();
    }

    private static final float TAP_SLOP = 18.0f;
    private final Host host;
    private float downX;
    private float downY;
    private float lastX;
    private float lastY;
    private boolean moved;
    private boolean gameGesture;
    private boolean ruleGesture;

    CreateRoomTouchController(Host host) {
        this.host = host;
    }

    boolean onTouchEvent(MotionEvent event, CreateRoomLayout.Viewport viewport) {
        float x = viewport.designX(event.getX());
        float y = viewport.designY(event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN -> {
                downX = lastX = x;
                downY = lastY = y;
                moved = false;
                gameGesture = inGameList(x, y);
                ruleGesture = inRuleList(x, y);
                return true;
            }
            case MotionEvent.ACTION_MOVE -> {
                moved |= distanceSquared(x, y, downX, downY) > TAP_SLOP * TAP_SLOP;
                if (gameGesture) {
                    host.setGameScroll(clamp(
                            host.gameScroll() + lastY - y,
                            0,
                            maxGameScroll(host.games().size())));
                } else if (ruleGesture) {
                    host.setRuleScroll(clamp(
                            host.ruleScroll() + lastY - y,
                            0,
                            CreateRoomRows.maxScroll(host.state())));
                }
                lastX = x;
                lastY = y;
                return true;
            }
            case MotionEvent.ACTION_UP -> {
                if (!moved) {
                    handleTap(x, y);
                }
                reset();
                return true;
            }
            case MotionEvent.ACTION_CANCEL -> {
                reset();
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    private void handleTap(float x, float y) {
        if (host.hasResult()) {
            host.closeRequested();
            return;
        }
        if (contains(x, y, CreateRoomLayout.BACK_LEFT, CreateRoomLayout.BACK_TOP,
                CreateRoomLayout.BACK_RIGHT, CreateRoomLayout.BACK_BOTTOM)) {
            host.closeRequested();
            return;
        }
        if (host.loading()) {
            return;
        }
        String openDropdown = host.openDropdownNode();
        int dropdownItem = openDropdownItemAt(
                host.state(), openDropdown, host.ruleScroll(), x, y);
        if (dropdownItem >= 0) {
            CreateRoomRuleConfig.Option owner = optionNamed(host.state(), openDropdown);
            if (owner != null) {
                host.state().selectDropdown(
                        owner.nodeName(), owner.dropdown().get(dropdownItem).nodeName());
                host.setOpenDropdownNode(null);
                host.stateChanged();
            }
            return;
        }
        if (contains(
                x,
                y,
                CreateRoomLayout.FEEDBACK_LEFT,
                CreateRoomLayout.FEEDBACK_TOP,
                CreateRoomLayout.FEEDBACK_RIGHT,
                CreateRoomLayout.FEEDBACK_BOTTOM)) {
            host.feedbackRequested();
            return;
        }
        if (contains(x, y,
                CreateRoomLayout.CREATE_BUTTON_CENTER_X - CreateRoomLayout.CREATE_BUTTON_WIDTH * 0.5f,
                CreateRoomLayout.CREATE_BUTTON_CENTER_Y - CreateRoomLayout.CREATE_BUTTON_HEIGHT * 0.5f,
                CreateRoomLayout.CREATE_BUTTON_CENTER_X + CreateRoomLayout.CREATE_BUTTON_WIDTH * 0.5f,
                CreateRoomLayout.CREATE_BUTTON_CENTER_Y + CreateRoomLayout.CREATE_BUTTON_HEIGHT * 0.5f)) {
            host.createRequested();
            return;
        }
        int gameIndex = gameIndexAt(x, y);
        if (gameIndex >= 0) {
            host.selectGame(gameIndex);
            return;
        }
        if (!inRuleList(x, y) || host.state() == null) {
            dismissPopups();
            return;
        }
        OptionHit hit = optionAt(x, y);
        if (hit == null) {
            dismissPopups();
            return;
        }
        CreateRoomRuleConfig.Option option = hit.option();
        if (hit.tip()) {
            host.setOpenDropdownNode(null);
            host.setOpenTipNode(
                    option.nodeName().equals(host.openTipNode()) ? null : option.nodeName());
            return;
        }
        if (openDropdown != null && openDropdown.equals(option.nodeName())) {
            host.setOpenDropdownNode(null);
            return;
        }
        if (!host.state().isEnabled(option.nodeName())) {
            return;
        }
        if (!option.dropdown().isEmpty()) {
            host.setOpenTipNode(null);
            host.setOpenDropdownNode(
                    option.nodeName().equals(openDropdown) ? null : option.nodeName());
        } else if (hit.group().type() == CreateRoomRuleConfig.Type.CHECKBOX) {
            host.state().toggle(option.nodeName());
            host.stateChanged();
        } else {
            host.state().select(option.nodeName());
            host.stateChanged();
        }
    }

    private OptionHit optionAt(float x, float y) {
        List<CreateRoomRows.Row> rows = CreateRoomRows.flatten(host.state());
        float contentY = y + host.ruleScroll() - CreateRoomLayout.RULE_LIST_TOP;
        int rowIndex = (int) Math.floor(contentY / CreateRoomLayout.RULE_ROW_STRIDE);
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return null;
        }
        float within = contentY - rowIndex * CreateRoomLayout.RULE_ROW_STRIDE;
        if (within < 0 || within > CreateRoomLayout.RULE_ROW_HEIGHT) {
            return null;
        }
        CreateRoomRows.Row row = rows.get(rowIndex);
        float[] positions = CreateRoomLayout.optionXs(row.options().size());
        float centerY = CreateRoomLayout.RULE_LIST_TOP
                + rowIndex * CreateRoomLayout.RULE_ROW_STRIDE
                + 50.0f - host.ruleScroll();
        for (int index = 0; index < row.options().size(); index++) {
            CreateRoomRuleConfig.Option option = row.options().get(index);
            if (!host.state().isVisible(option.nodeName())) {
                continue;
            }
            float centerX = CreateRoomLayout.RULE_CONTENT_LEFT
                    + positions[index] + option.diffNodeX();
            boolean radio = row.group().type() == CreateRoomRuleConfig.Type.RADIO;
            float textLeft = centerX
                    + (radio
                            ? CreateRoomLayout.RADIO_TEXT_OFFSET_X
                            : CreateRoomLayout.CHECKBOX_TEXT_OFFSET_X);
            float textWidth =
                    Math.max(120, option.text().length() * CreateRoomLayout.OPTION_TEXT_SIZE);
            float optionRight = textLeft + textWidth
                    + CreateRoomLayout.TIP_ICON_GAP + CreateRoomLayout.TIP_ICON_SIZE;
            if (contains(x, y, centerX - 45, centerY - 45, optionRight, centerY + 45)) {
                boolean tip = !option.tip().isBlank() && x > textLeft + textWidth;
                return new OptionHit(row.group(), option, centerX, centerY, tip);
            }
        }
        return null;
    }

    static int openDropdownItemAt(
            CreateRoomState state,
            String ownerNodeName,
            float ruleScroll,
            float x,
            float y) {
        if (state == null || ownerNodeName == null || ownerNodeName.isBlank()) {
            return -1;
        }
        List<CreateRoomRows.Row> rows = CreateRoomRows.flatten(state);
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            CreateRoomRows.Row row = rows.get(rowIndex);
            float[] positions = CreateRoomLayout.optionXs(row.options().size());
            for (int optionIndex = 0; optionIndex < row.options().size(); optionIndex++) {
                CreateRoomRuleConfig.Option option = row.options().get(optionIndex);
                if (!ownerNodeName.equals(option.nodeName())
                        || option.dropdown().isEmpty()
                        || !state.isVisible(option.nodeName())) {
                    continue;
                }
                float centerX = CreateRoomLayout.RULE_CONTENT_LEFT
                        + positions[optionIndex] + option.diffNodeX();
                float centerY = CreateRoomLayout.RULE_LIST_TOP
                        + rowIndex * CreateRoomLayout.RULE_ROW_STRIDE
                        + 50.0f - ruleScroll;
                float left = centerX + 60.0f;
                float top = centerY + 42.0f;
                int item = (int) Math.floor((y - top) / 66.0f);
                if (item >= 0
                        && item < option.dropdown().size()
                        && contains(x, y, left, top, left + 260.0f,
                                top + option.dropdown().size() * 66.0f)) {
                    return item;
                }
                return -1;
            }
        }
        return -1;
    }

    private static CreateRoomRuleConfig.Option optionNamed(
            CreateRoomState state, String nodeName) {
        if (state == null || nodeName == null) {
            return null;
        }
        for (CreateRoomRows.Row row : CreateRoomRows.flatten(state)) {
            for (CreateRoomRuleConfig.Option option : row.options()) {
                if (nodeName.equals(option.nodeName())) {
                    return option;
                }
            }
        }
        return null;
    }

    private int gameIndexAt(float x, float y) {
        if (!inGameList(x, y)) {
            return -1;
        }
        float contentTop = CreateRoomLayout.gameContentTop(host.games().size());
        int index = Math.round(
                (y + host.gameScroll() - contentTop - 68.0f)
                        / CreateRoomLayout.GAME_TAB_STRIDE);
        if (index < 0 || index >= host.games().size()) {
            return -1;
        }
        float center = contentTop + 68.0f
                + index * CreateRoomLayout.GAME_TAB_STRIDE - host.gameScroll();
        return Math.abs(y - center) <= CreateRoomLayout.GAME_TAB_HEIGHT * 0.5f ? index : -1;
    }

    private void dismissPopups() {
        host.setOpenTipNode(null);
        host.setOpenDropdownNode(null);
    }

    private void reset() {
        gameGesture = false;
        ruleGesture = false;
        moved = false;
    }

    private static boolean inGameList(float x, float y) {
        return contains(x, y, CreateRoomLayout.GAME_LIST_LEFT, CreateRoomLayout.GAME_LIST_TOP,
                CreateRoomLayout.GAME_LIST_RIGHT, CreateRoomLayout.GAME_LIST_BOTTOM);
    }

    private static boolean inRuleList(float x, float y) {
        return contains(x, y, CreateRoomLayout.RULE_LIST_LEFT, CreateRoomLayout.RULE_LIST_TOP,
                CreateRoomLayout.RULE_LIST_RIGHT, CreateRoomLayout.RULE_LIST_BOTTOM);
    }

    private static float maxGameScroll(int count) {
        return Math.max(0, count * CreateRoomLayout.GAME_TAB_STRIDE
                - (CreateRoomLayout.GAME_LIST_BOTTOM - CreateRoomLayout.GAME_LIST_TOP));
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static boolean contains(
            float x, float y, float left, float top, float right, float bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    private static float distanceSquared(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return dx * dx + dy * dy;
    }

    private record OptionHit(
            CreateRoomRuleConfig.Group group,
            CreateRoomRuleConfig.Option option,
            float centerX,
            float centerY,
            boolean tip) {}
}
