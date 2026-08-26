package com.nanbeiyule.game;

import android.graphics.RectF;

/**
 * Page-space geometry for the friend drawer. The original
 * {@code IMListLayer.csb} (1920-wide CSB design, dumped with
 * {@code tools/dump_csb.py}) mounts <em>two</em> bodies that the Lua view
 * swaps between, and the drawer reproduces both:
 *
 * <ul>
 *   <li>Collapsed — {@code _KW_READY_PANEL}, {@code 230x750} at scale
 *       {@code 0.84} anchored to its right edge at x={@code 193.2}, so it
 *       occupies the leftmost {@code 193.2/1920} of the screen. It carries
 *       {@code friend_ready_bg}, the {@code friend_ready_title} plate and
 *       the "暂无牌友" empty label. This is the resting state
 *       ({@code _bReadyState = true}, {@code Im/View.lua:132}).</li>
 *   <li>Expanded — {@code _KWA_LIST_PANEL}, a fixed 580-wide visible body
 *       (the 695-wide {@code friend_list_bg} bitmap carries a transparent
 *       drop-shadow margin past that column). The Lua open animation
 *       drives it to {@code listPanel.width - KW_OPEN_BTN_WHITE_WIDTH}
 *       ({@code View.lua:585}), confirming the 580-unit visible width.</li>
 * </ul>
 *
 * The collapsed body keeps the CSB distinction between the parent
 * {@code _KW_READY_PANEL} ({@code 230x750}) and its stretched child
 * {@code _KW_READY_LIST_BG} ({@code 260x750}); the background intentionally
 * extends past the panel's right edge in the original file.
 */
final class FriendDrawerLayout {
    /** 1920-wide CSB design unit to this 3200-wide page. */
    private static final float CSB_TO_PAGE = 3200.0f / 1920.0f;
    /** {@code _KW_PLAYER_ITEM} height in IMListLayer.csb. */
    static final float ITEM_HEIGHT = 150.0f * CSB_TO_PAGE;
    /** {@code _KWA_LIST_PANEL} width in IMListLayer.csb. */
    private static final float PANEL_WIDTH = 580.0f * CSB_TO_PAGE;
    /** Expanded body: {@code _KWA_FRIEND_PANEL} is anchored to its right
     * edge and the open animation drives it to
     * {@code listPanel.width - KW_OPEN_BTN_WHITE_WIDTH} = 565
     * ({@code View.lua:585}), so the 580-wide body starts 15 units off
     * the left screen edge. It declares the full 1080-unit design height. */
    private static final float PANEL_LEFT = -15.0f * CSB_TO_PAGE;
    private static final float PANEL_BOTTOM =
            GameHomeViewportLayout.PAGE_HEIGHT;
    /** 1080-tall CSB design unit to this page's height. */
    private static final float VERTICAL_SCALE = PANEL_BOTTOM / 1080.0f;
    private static final float TITLE_RATIO = 120.0f / 1080.0f;
    private static final float BOTTOM_RATIO = 92.0f / 1080.0f;
    /** {@code _KW_READY_PANEL} scale; its children inherit it. */
    private static final float READY_SCALE = 0.84f;
    private static final float READY_PANEL_WIDTH =
            230.0f * READY_SCALE * CSB_TO_PAGE;
    private static final float READY_PANEL_HEIGHT =
            750.0f * READY_SCALE * CSB_TO_PAGE;
    /** {@code _KW_READY_LIST_BG} is wider than its parent
     * {@code _KW_READY_PANEL}: size {@code 260x750}, pos {@code (130,375)}
     * and anchor {@code (0.5,0.5)} in the CSB, so its left/top align with
     * the ready panel and its right edge protrudes by 30 design units. */
    private static final float READY_BACKGROUND_WIDTH =
            260.0f * READY_SCALE * CSB_TO_PAGE;
    private static final float READY_BACKGROUND_HEIGHT =
            750.0f * READY_SCALE * CSB_TO_PAGE;
    /** {@code _KW_READY_LIST_TITLE} is 231x92 at scale 0.97, nested in the
     * 0.84-scaled ready panel, so it covers ~97% of the panel width. */
    private static final float READY_TITLE_WIDTH =
            231.0f * 0.97f * READY_SCALE * CSB_TO_PAGE;
    private static final float READY_TITLE_HEIGHT =
            92.0f * 0.97f * READY_SCALE * CSB_TO_PAGE;
    /** Title centre sits 67 units below the {@code _KW_READY_LIST_BG}
     * top (pos y=683 of the 750-tall body, CSB origin bottom-left). */
    private static final float READY_TITLE_TOP =
            (67.0f - 92.0f * 0.97f / 2.0f) * READY_SCALE * CSB_TO_PAGE;
    /** {@code _KW_READY_ITEM} is 200x150 in the CSB. */
    static final float READY_ITEM_HEIGHT = 150.0f * CSB_TO_PAGE;
    private static final float ARROW_HEIGHT = 139.0f * CSB_TO_PAGE;
    private static final float ARROW_ASPECT = 68.0f / 139.0f;
    /** Original {@code KW_OPEN_BTN_WHITE_WIDTH} (15 units in the
     * 1920-wide CSB design) scaled to this 3200-wide page: the handle
     * arrow overlaps the collapsed tab and the expanded panel edge. */
    private static final float ARROW_OVERLAP = 15.0f * CSB_TO_PAGE;
    /** {@code KW_HEAD_NODE} scale (0.6) applied to {@code _KW_IMG_HEAD}
     * (145 units) in {@code _KW_PLAYER_ITEM}. */
    private static final float AVATAR_RADIUS = 145.0f * 0.6f / 2.0f * CSB_TO_PAGE;
    private static final float AVATAR_CENTER_X_OFFSET = 70.0f * CSB_TO_PAGE;
    /** Proportional growth from the panel's previous (bitmap-aspect
     * derived) width to {@link #PANEL_WIDTH}, applied to the item
     * sub-element offsets below that have no direct one-to-one CSB
     * node (the original list item interleaves a bottom name/time
     * caption, state icon and extra watch/data buttons across the row;
     * reproducing that exact arrangement is a larger follow-up). */
    static final float ITEM_CONTENT_SCALE = 1.7494f;
    /** {@code _KW_LABLE_LIST/FOLLOW/RECALL} left offsets, widths and
     * heights in IMListLayer.csb; the three tabs share a bottom edge
     * and the list tab stands taller than the other two. */
    private static final float[] TAB_LEFT = {5.0f, 185.0f, 365.0f};
    private static final float[] TAB_WIDTH = {208.0f, 190.0f, 190.0f};
    private static final float[] TAB_HEIGHT_UNITS = {90.0f, 76.0f, 76.0f};

    private final GameHomeV3Layout.Box panel;

    FriendDrawerLayout() {
        this(new GameHomeV3Layout().friendPanel());
    }

    FriendDrawerLayout(GameHomeV3Layout.Box panel) {
        this.panel = panel;
    }

    GameHomeV3Layout.Box panel() {
        return panel;
    }

    float panelWidth() {
        return PANEL_WIDTH;
    }

    float panelRight() {
        return PANEL_LEFT + panelWidth();
    }

    RectF panelRect() {
        return new RectF(PANEL_LEFT, 0.0f, panelRight(), PANEL_BOTTOM);
    }

    /** Collapsed "ready" body ({@code _KW_READY_PANEL}), hugging the
     * left screen edge. */
    RectF readyPanelRect() {
        float centerY = (panel.top() + panel.bottom()) / 2.0f;
        return new RectF(
                0.0f,
                centerY - READY_PANEL_HEIGHT / 2.0f,
                READY_PANEL_WIDTH,
                centerY + READY_PANEL_HEIGHT / 2.0f);
    }

    /** Nine-sliced {@code _KW_READY_LIST_BG} artwork destination. */
    RectF readyBackgroundRect() {
        RectF ready = readyPanelRect();
        return new RectF(
                ready.left,
                ready.top,
                ready.left + READY_BACKGROUND_WIDTH,
                ready.top + READY_BACKGROUND_HEIGHT);
    }

    /** Gold {@code friend_ready_title} plate centred on the ready body. */
    RectF readyTitleRect() {
        RectF ready = readyPanelRect();
        float centerX = (ready.left + ready.right) / 2.0f;
        float top = ready.top + READY_TITLE_TOP;
        return new RectF(
                centerX - READY_TITLE_WIDTH / 2.0f,
                top,
                centerX + READY_TITLE_WIDTH / 2.0f,
                top + READY_TITLE_HEIGHT);
    }

    /** Scrollable body of the collapsed ready panel. {@code
     * _KW_READY_LIST_VIEW} is 600 units tall centred at y=340 of the
     * 750-tall body, i.e. it spans 110..710 measured from the top. */
    RectF readyListRect() {
        RectF ready = readyPanelRect();
        float height = ready.bottom - ready.top;
        return new RectF(
                ready.left,
                ready.top + height * 110.0f / 750.0f,
                ready.right,
                ready.top + height * 710.0f / 750.0f);
    }

    /** Unscrolled ready-item bounds inside the collapsed list. */
    RectF readyItemRect(int index) {
        float top = readyListRect().top + index * READY_ITEM_HEIGHT;
        RectF ready = readyPanelRect();
        return new RectF(
                ready.left, top, ready.right, top + READY_ITEM_HEIGHT);
    }

    float readyBackgroundInsetX() {
        return 48.0f * READY_SCALE * CSB_TO_PAGE;
    }

    float readyBackgroundInsetY() {
        return 52.0f * READY_SCALE * CSB_TO_PAGE;
    }

    private float arrowWidth() {
        return ARROW_HEIGHT * ARROW_ASPECT;
    }

    /** Small open arrow overlapping the collapsed body's right edge. */
    RectF collapsedArrowRect() {
        RectF ready = readyPanelRect();
        float left = ready.right - ARROW_OVERLAP;
        float top = (ready.top + ready.bottom - ARROW_HEIGHT) / 2.0f;
        return new RectF(
                left, top, left + arrowWidth(), top + ARROW_HEIGHT);
    }

    /** Close arrow overlapping the expanded panel's right edge; it
     * slides between the collapsed and expanded anchors while the
     * panel moves. */
    RectF arrowRect(float expandProgress) {
        float collapsedLeft = readyPanelRect().right - ARROW_OVERLAP;
        float expandedLeft = panelRight() - ARROW_OVERLAP;
        float left =
                collapsedLeft
                        + expandProgress * (expandedLeft - collapsedLeft);
        RectF ready = readyPanelRect();
        float top = (ready.top + ready.bottom - ARROW_HEIGHT) / 2.0f;
        return new RectF(
                left, top, left + arrowWidth(), top + ARROW_HEIGHT);
    }

    RectF titleRect() {
        return new RectF(
                PANEL_LEFT,
                0.0f,
                panelRight(),
                PANEL_BOTTOM * TITLE_RATIO);
    }

    /** Tab strip across the panel's title area, sized and positioned
     * from the CSB's per-tab geometry; the list, follow and recall
     * tabs share a bottom edge and the list tab stands taller. */
    RectF tabRect(int index) {
        float left = PANEL_LEFT + TAB_LEFT[index] * CSB_TO_PAGE;
        float width = TAB_WIDTH[index] * CSB_TO_PAGE;
        float height = TAB_HEIGHT_UNITS[index] * CSB_TO_PAGE;
        float bottom = titleRect().bottom;
        return new RectF(left, bottom - height, left + width, bottom);
    }

    /** Bottom "一键邀请" shortcut shown on the friend-list tab; {@code
     * _KW_UI_ACTION} in {@code _KW_BOTTOM_PANEL} is 226x88 in the CSB. */
    RectF inviteAllRect() {
        float height = 88.0f * CSB_TO_PAGE;
        float width = 226.0f * CSB_TO_PAGE;
        float centerX = PANEL_LEFT + panelWidth() / 2.0f;
        // _KW_UI_ACTION sits at y=55 (CSB origin bottom-left) and is 88
        // tall, so its lower edge is 11 units above the panel bottom.
        float bottom = PANEL_BOTTOM - 11.0f * VERTICAL_SCALE;
        return new RectF(
                centerX - width / 2.0f,
                bottom - height,
                centerX + width / 2.0f,
                bottom);
    }

    /** Bottom "刷新列表" button shown on the follow tab, mirroring the
     * original {@code _btnAction} slot with the
     * {@code friend_btn_refresh_list} artwork; matches
     * {@link #inviteAllRect()}'s height with the refresh art's own
     * aspect ratio. */
    RectF refreshListRect() {
        float height = 88.0f * CSB_TO_PAGE;
        float width = height * (201.0f / 77.0f);
        float centerX = PANEL_LEFT + panelWidth() / 2.0f;
        // _KW_UI_ACTION sits at y=55 (CSB origin bottom-left) and is 88
        // tall, so its lower edge is 11 units above the panel bottom.
        float bottom = PANEL_BOTTOM - 11.0f * VERTICAL_SCALE;
        return new RectF(
                centerX - width / 2.0f,
                bottom - height,
                centerX + width / 2.0f,
                bottom);
    }

    /** Expanded list body. The original panel has no application or
     * add-friend entry rows, so the list starts directly under the tab
     * strip and runs to the bottom action slot. */
    RectF listRect() {
        return new RectF(
                PANEL_LEFT,
                titleRect().bottom + 8.0f,
                panelRight(),
                inviteAllRect().top - 8.0f);
    }

    /** Unscrolled item bounds inside the list content. */
    RectF itemRect(int index) {
        float top = listRect().top + index * ITEM_HEIGHT;
        return new RectF(
                PANEL_LEFT, top, panelRight(), top + ITEM_HEIGHT);
    }

    float avatarCenterX() {
        return PANEL_LEFT + AVATAR_CENTER_X_OFFSET;
    }

    float avatarRadius() {
        return AVATAR_RADIUS;
    }

    /** Golden frame drawn around the circular avatar; {@code
     * _KW_IMG_FRAME} (164 units) is 19 units wider than {@code
     * _KW_IMG_HEAD} (145 units) under the shared 0.6 head-node scale. */
    RectF avatarFrameRect(RectF item) {
        float size = AVATAR_RADIUS * 2.0f + 19.0f * CSB_TO_PAGE * 0.6f;
        float cx = avatarCenterX();
        float cy = item.centerY();
        return new RectF(
                cx - size / 2.0f,
                cy - size / 2.0f,
                cx + size / 2.0f,
                cy + size / 2.0f);
    }

    /** Red shield ribbon over the avatar's top-left corner. */
    RectF shieldRect(RectF item) {
        float inset = 6.0f * ITEM_CONTENT_SCALE;
        float left = avatarCenterX() - AVATAR_RADIUS - inset;
        float top = item.centerY() - AVATAR_RADIUS - inset;
        return new RectF(
                left, top,
                left + 64.0f * ITEM_CONTENT_SCALE,
                top + 51.0f * ITEM_CONTENT_SCALE);
    }

    float textLeft() {
        return PANEL_LEFT + 156.0f * ITEM_CONTENT_SCALE;
    }

    /** Vertical online/offline state badge next to the name. */
    RectF stateBadgeRect(RectF item) {
        float left = textLeft();
        float top = item.top + 82.0f * ITEM_CONTENT_SCALE;
        return new RectF(
                left, top,
                left + 34.0f * ITEM_CONTENT_SCALE,
                top + 67.0f * ITEM_CONTENT_SCALE);
    }

    RectF inviteButtonRect(RectF item) {
        float halfHeight = 57.0f * ITEM_CONTENT_SCALE;
        return new RectF(
                item.right - 138.0f * ITEM_CONTENT_SCALE,
                item.centerY() - halfHeight,
                item.right - 24.0f * ITEM_CONTENT_SCALE,
                item.centerY() + halfHeight);
    }

    int itemIndexAt(float contentY) {
        float offset = contentY - listRect().top;
        if (offset < 0.0f) {
            return -1;
        }
        return (int) (offset / ITEM_HEIGHT);
    }
}
