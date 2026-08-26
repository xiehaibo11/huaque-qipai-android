package com.huaque.ui.friend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.huaque.ui.MainActivity;
import com.huaque.ui.R;

import java.util.List;

public final class FriendPanelView extends MainActivity.BoxRoot
        implements FriendPanelController.View {
    private static final int PANEL_WIDTH = 695;
    private static final long MOTION_MS = 200L;
    private static final String ORIGINAL_FRIEND_FONT = "fonts/fangzhengcuyuan.ttf";
    private static final String FRIEND_PANEL_PREFERENCES = "huaque_friend_panel";
    private static final String FILTER_GUIDE_SEEN = "filter_guide_seen";

    private final Activity activity;
    private final View outsideDismiss;
    private final ScaledRoot collapsedRail;
    private final ScaledRoot expandedPanel;
    private final ScaledRoot friendPage;
    private final ScaledRoot upcomingPage;
    private final FriendPanelUpcomingState upcomingState;
    private final LinearLayout content;
    private final EditText searchInput;
    private final ProgressBar loading;
    private final ImageView friendsTab;
    private final ImageView upcomingTab;
    private final ImageView sideTabs;
    private ScaledRoot upcomingGuide;
    private ScaledRoot upcomingFilterList;
    private ScaledLabel upcomingSelectedName;
    private ImageView upcomingAllSelected;
    private ImageView upcomingMatchSelected;
    private FriendPanelController controller;
    private boolean open;

    public FriendPanelView(Activity activity) {
        super(activity);
        this.activity = activity;
        this.setClipChildren(false);

        outsideDismiss = new View(activity);
        outsideDismiss.setVisibility(GONE);
        outsideDismiss.setOnClickListener(ignored -> closePanel());
        addStretchedBox(outsideDismiss, 0, 0, 1920, 1080);

        collapsedRail = buildCollapsedRail();
        addStretchedBox(collapsedRail, 0, 225, 246, 630);

        expandedPanel = new ScaledRoot(activity, PANEL_WIDTH, 1080);
        expandedPanel.setClipChildren(false);
        expandedPanel.setVisibility(GONE);
        addStretchedBox(expandedPanel, 0, 0, PANEL_WIDTH, 1080);

        ImageView background = image(R.drawable.friend_panel_bg);
        expandedPanel.addBox(background, 0, 0, PANEL_WIDTH, 1080);

        friendsTab = image(R.drawable.friend_panel_tab_friends_on);
        friendsTab.setOnClickListener(ignored -> selectFriends());
        expandedPanel.addBox(friendsTab, 5, 35, 208, 90);

        upcomingTab = image(R.drawable.friend_panel_tab_upcoming_off);
        upcomingTab.setOnClickListener(ignored -> selectUpcoming());
        expandedPanel.addBox(upcomingTab, 185, 49, 190, 76);

        friendPage = new ScaledRoot(activity, PANEL_WIDTH, 1080);
        expandedPanel.addBox(friendPage, 0, 0, PANEL_WIDTH, 1080);

        searchInput = new EditText(activity);
        searchInput.setSingleLine(true);
        searchInput.setHint("输入玩家序号");
        searchInput.setHintTextColor(0xFF9B8B73);
        searchInput.setTextColor(0xFF6C4A25);
        searchInput.setTextSize(22);
        searchInput.setPadding(18, 0, 78, 0);
        searchInput.setInputType(InputType.TYPE_CLASS_TEXT);
        searchInput.setBackground(roundRect(0xFFF2E3BD, 0xFFDDC99D, 18, 2));
        friendPage.addBox(searchInput, 22, 171, 533, 70);

        ImageView search = image(R.drawable.friend_panel_search);
        search.setPadding(10, 8, 10, 8);
        search.setOnClickListener(ignored -> {
            if (controller != null) controller.search(searchInput.getText().toString());
        });
        friendPage.addBox(search, 480, 177, 70, 55);

        sideTabs = image(R.drawable.friend_panel_my_friends);
        friendPage.addBox(sideTabs, 5, 269, 61, 690);
        View friendSideHit = new View(activity);
        friendSideHit.setOnClickListener(ignored -> selectFriends());
        friendPage.addBox(friendSideHit, 5, 269, 61, 345);
        View groupSideHit = new View(activity);
        groupSideHit.setOnClickListener(ignored -> selectGroups());
        friendPage.addBox(groupSideHit, 5, 614, 61, 345);

        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(8, 8, 8, 8);
        scroll.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        friendPage.addBox(scroll, 72, 268, 485, 682);

        ImageView inviteAll = image(R.drawable.friend_panel_invite_all);
        inviteAll.setOnClickListener(ignored -> {
            if (controller != null) controller.inviteAll();
        });
        friendPage.addBox(inviteAll, 78, 971, 226, 88);

        ImageView addFriend = image(R.drawable.friend_panel_add_friend);
        addFriend.setOnClickListener(ignored -> searchInput.requestFocus());
        friendPage.addBox(addFriend, 329, 976, 201, 77);

        boolean guideSeen = activity.getSharedPreferences(
                FRIEND_PANEL_PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(FILTER_GUIDE_SEEN, false);
        upcomingState = new FriendPanelUpcomingState(guideSeen);
        upcomingPage = buildUpcomingPage();
        upcomingPage.setVisibility(GONE);
        expandedPanel.addBox(upcomingPage, 0, 0, PANEL_WIDTH, 1080);
        friendsTab.bringToFront();
        upcomingTab.bringToFront();

        ImageView close = image(R.drawable.friend_panel_close);
        close.setOnClickListener(ignored -> closePanel());
        expandedPanel.addBox(close, 572, 466, 68, 139);

        loading = new ProgressBar(activity);
        loading.setVisibility(GONE);
        expandedPanel.addBox(loading, 285, 515, 70, 70);
    }

    public void attach(FriendPanelController controller) {
        this.controller = controller;
    }

    public void openPanel() {
        if (open) return;
        open = true;
        collapsedRail.setVisibility(GONE);
        outsideDismiss.setVisibility(VISIBLE);
        expandedPanel.setVisibility(VISIBLE);
        expandedPanel.setAlpha(0f);
        expandedPanel.setTranslationX(-expandedPanel.getWidth() * 0.18f);
        expandedPanel.animate().alpha(1f).translationX(0f).setDuration(MOTION_MS).start();
        if (controller != null) controller.open();
    }

    public void closePanel() {
        if (!open) return;
        open = false;
        outsideDismiss.setVisibility(GONE);
        expandedPanel.animate().alpha(0f).translationX(-expandedPanel.getWidth() * 0.18f)
                .setDuration(MOTION_MS).withEndAction(() -> {
                    expandedPanel.setVisibility(GONE);
                    expandedPanel.setTranslationX(0f);
                    collapsedRail.setAlpha(0f);
                    collapsedRail.setVisibility(VISIBLE);
                    collapsedRail.animate().alpha(1f).setDuration(MOTION_MS).start();
                }).start();
    }

    @Override
    public void setLoading(boolean isLoading) {
        loading.setVisibility(isLoading ? VISIBLE : GONE);
    }

    @Override
    public void showAuthenticationRequired() {
        showCenteredMessage("请使用手机号登录后查看牌友\n微信预览登录不包含牌友身份");
    }

    @Override
    public void showFriends(List<FriendData.Entry> friends) {
        content.removeAllViews();
        if (friends.isEmpty()) {
            showCenteredMessage("您的牌友列表当前为空\n快去添加牌友开始游戏吧");
            return;
        }
        for (FriendData.Entry friend : friends) content.addView(friendRow(friend));
    }

    @Override
    public void showUpcoming(List<FriendData.Application> applications,
            List<FriendData.Notification> notifications) {
        content.removeAllViews();
        if (applications.isEmpty() && notifications.isEmpty()) {
            showCenteredMessage("暂无牌友申请或游戏邀请");
            return;
        }
        if (!applications.isEmpty()) content.addView(sectionTitle("牌友申请"));
        for (FriendData.Application application : applications) {
            content.addView(applicationRow(application));
        }
        if (!notifications.isEmpty()) content.addView(sectionTitle("游戏邀请"));
        for (FriendData.Notification notification : notifications) {
            content.addView(notificationRow(notification));
        }
        if (!notifications.isEmpty()) {
            TextView read = actionButton("全部已读", 0xFF62BBA2);
            read.setOnClickListener(ignored -> {
                if (controller != null) controller.markNotificationsRead();
            });
            content.addView(read, rowParams(LinearLayout.LayoutParams.MATCH_PARENT, 58, 8));
        }
    }

    @Override
    public void showSearchResult(FriendData.SearchResult result) {
        content.removeAllViews();
        LinearLayout row = baseRow();
        row.addView(identity(result.displayName, result.publicPlayerId),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        TextView action = actionButton(searchAction(result.relation), 0xFF59BDA6);
        action.setEnabled("NONE".equals(result.relation) || "REJECTED".equals(result.relation));
        action.setAlpha(action.isEnabled() ? 1f : 0.55f);
        action.setOnClickListener(ignored -> {
            if (controller != null) controller.apply(result.publicPlayerId);
        });
        row.addView(action, new LinearLayout.LayoutParams(125, 62));
        content.addView(row);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    private ScaledRoot buildCollapsedRail() {
        ScaledRoot rail = new ScaledRoot(
                activity,
                FriendPanelCollapsedGeometry.ROOT_WIDTH,
                FriendPanelCollapsedGeometry.ROOT_HEIGHT);
        View paper = new View(activity);
        paper.setBackgroundResource(R.drawable.friend_panel_ready_bg);
        addCollapsedBox(rail, paper, FriendPanelCollapsedGeometry.backgroundBounds());

        ImageView title = image(R.drawable.friend_panel_ready_title);
        addCollapsedBox(rail, title, FriendPanelCollapsedGeometry.titleBounds());

        ScaledLabel empty = new ScaledLabel(
                activity, FriendPanelCollapsedGeometry.emptyLabelTextSize());
        empty.setText("暂无牌友");
        empty.setTextColor(0xFFB38857);
        empty.setGravity(Gravity.CENTER);
        empty.setTypeface(loadOriginalFriendTypeface());
        addCollapsedBox(rail, empty, FriendPanelCollapsedGeometry.emptyLabelBounds());

        ImageView openArrow = image(R.drawable.friend_panel_open);
        openArrow.setOnClickListener(ignored -> openPanel());
        addCollapsedBox(rail, openArrow, FriendPanelCollapsedGeometry.openArrowBounds());
        rail.setOnClickListener(ignored -> openPanel());
        return rail;
    }

    private static void addCollapsedBox(
            ScaledRoot rail, View view, FriendPanelCollapsedGeometry.Rect bounds) {
        rail.addBox(view, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private ScaledRoot buildUpcomingPage() {
        ScaledRoot page = new ScaledRoot(activity, PANEL_WIDTH, 1080);
        page.setClipChildren(false);
        page.setOnClickListener(ignored -> {
            upcomingState.dismissFilter();
            syncUpcomingState();
        });

        View filterBackground = new View(activity);
        filterBackground.setBackgroundResource(R.drawable.friend_panel_upcoming_filter_bg);
        addUpcomingBox(page, filterBackground, FriendPanelUpcomingGeometry.filterHeader());

        addUpcomingBox(page, image(R.drawable.friend_panel_upcoming_filter_icon),
                FriendPanelUpcomingGeometry.filterIcon());

        upcomingSelectedName = originalLabel("所有房间", 43.2f, 0xFFA97556,
                Gravity.LEFT | Gravity.CENTER_VERTICAL);
        addUpcomingBox(page, upcomingSelectedName,
                FriendPanelUpcomingGeometry.selectedName());

        ImageView filterButton = image(R.drawable.friend_panel_upcoming_filter_button);
        filterButton.setOnClickListener(ignored -> openUpcomingFilter());
        addUpcomingBox(page, filterButton, FriendPanelUpcomingGeometry.filterButton());

        ScaledLabel emptyMessage = originalLabel(
                "抱歉，当前没找到正在等\n待开局的牌友，可在5s后\n刷新再次查询",
                40, 0xFFB38857, Gravity.CENTER);
        addUpcomingBox(page, emptyMessage, FriendPanelUpcomingGeometry.emptyMessage());

        ImageView refresh = image(R.drawable.friend_panel_upcoming_refresh);
        addUpcomingBox(page, refresh, FriendPanelUpcomingGeometry.refreshButton());

        upcomingFilterList = buildUpcomingFilterList();
        addUpcomingBox(page, upcomingFilterList, FriendPanelUpcomingGeometry.filterList());

        upcomingGuide = buildUpcomingGuide();
        addUpcomingBox(page, upcomingGuide, FriendPanelUpcomingGeometry.guideBubble());
        return page;
    }

    private ScaledRoot buildUpcomingGuide() {
        ScaledRoot guide = new ScaledRoot(activity, 432, 132);
        guide.addBox(image(R.drawable.friend_panel_upcoming_guide_bg), 0, 0, 432, 132);
        ScaledLabel text = originalLabel(
                "可筛选不同组别、比赛场\n或者大厅包厢哦~",
                36, 0xFFA36F48, Gravity.CENTER);
        guide.addBox(text, 34, 16, 364, 84);
        guide.setOnClickListener(ignored -> openUpcomingFilter());
        return guide;
    }

    private ScaledRoot buildUpcomingFilterList() {
        ScaledRoot list = new ScaledRoot(activity, 526, 150);
        View background = new View(activity);
        background.setBackgroundResource(R.drawable.friend_panel_upcoming_filter_list_bg);
        list.addBox(background, 0, 0, 526, 150);

        ScaledLabel allRooms = originalLabel(
                "所有房间", 46, Color.WHITE, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        list.addBox(allRooms, 14, 11, 440, 54);
        ScaledLabel matchArena = originalLabel(
                "比赛场", 46, Color.WHITE, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        list.addBox(matchArena, 14, 86, 440, 54);
        list.addBox(image(R.drawable.friend_panel_upcoming_filter_line), 10, 74, 500, 2);

        upcomingAllSelected = image(R.drawable.friend_panel_upcoming_filter_selected);
        list.addBox(upcomingAllSelected, 472, 17, 43, 42);
        upcomingMatchSelected = image(R.drawable.friend_panel_upcoming_filter_selected);
        list.addBox(upcomingMatchSelected, 472, 92, 43, 42);

        View allRoomsHit = new View(activity);
        allRoomsHit.setOnClickListener(ignored -> selectUpcomingFilter(
                FriendPanelUpcomingState.Filter.ALL_ROOMS));
        list.addBox(allRoomsHit, 0, 0, 526, 75);
        View matchArenaHit = new View(activity);
        matchArenaHit.setOnClickListener(ignored -> selectUpcomingFilter(
                FriendPanelUpcomingState.Filter.MATCH_ARENA));
        list.addBox(matchArenaHit, 0, 75, 526, 75);
        return list;
    }

    private static void addUpcomingBox(
            ScaledRoot page, View view, FriendPanelUpcomingGeometry.Rect bounds) {
        page.addBox(view, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private ScaledLabel originalLabel(String text, float size, int color, int gravity) {
        ScaledLabel label = new ScaledLabel(activity, size);
        label.setText(text);
        label.setTextColor(color);
        label.setGravity(gravity);
        label.setTypeface(loadOriginalFriendTypeface());
        return label;
    }

    private void openUpcomingFilter() {
        upcomingState.tapFilter();
        activity.getSharedPreferences(FRIEND_PANEL_PREFERENCES, Context.MODE_PRIVATE)
                .edit().putBoolean(FILTER_GUIDE_SEEN, true).apply();
        syncUpcomingState();
    }

    private void selectUpcomingFilter(FriendPanelUpcomingState.Filter filter) {
        upcomingState.select(filter);
        syncUpcomingState();
    }

    private void syncUpcomingState() {
        upcomingGuide.setVisibility(upcomingState.isGuideVisible() ? VISIBLE : GONE);
        upcomingFilterList.setVisibility(
                upcomingState.isFilterListVisible() ? VISIBLE : GONE);
        upcomingSelectedName.setText(upcomingState.selectedName());
        upcomingAllSelected.setVisibility(
                upcomingState.selectedFilter() == FriendPanelUpcomingState.Filter.ALL_ROOMS
                        ? VISIBLE : GONE);
        upcomingMatchSelected.setVisibility(
                upcomingState.selectedFilter() == FriendPanelUpcomingState.Filter.MATCH_ARENA
                        ? VISIBLE : GONE);
    }

    private Typeface loadOriginalFriendTypeface() {
        try {
            return Typeface.createFromAsset(activity.getAssets(), ORIGINAL_FRIEND_FONT);
        } catch (RuntimeException ignored) {
            return Typeface.DEFAULT;
        }
    }

    private void selectFriends() {
        friendsTab.setImageResource(R.drawable.friend_panel_tab_friends_on);
        upcomingTab.setImageResource(R.drawable.friend_panel_tab_upcoming_off);
        friendPage.setVisibility(VISIBLE);
        upcomingPage.setVisibility(GONE);
        upcomingState.dismissFilter();
        sideTabs.setImageResource(R.drawable.friend_panel_my_friends);
        if (controller != null) controller.loadFriends();
    }

    private void selectUpcoming() {
        friendsTab.setImageResource(R.drawable.friend_panel_tab_friends_off);
        upcomingTab.setImageResource(R.drawable.friend_panel_tab_upcoming_on);
        friendPage.setVisibility(GONE);
        upcomingPage.setVisibility(VISIBLE);
        upcomingState.enterUpcoming();
        syncUpcomingState();
    }

    private void selectGroups() {
        friendPage.setVisibility(VISIBLE);
        upcomingPage.setVisibility(GONE);
        upcomingState.dismissFilter();
        sideTabs.setImageResource(R.drawable.friend_panel_my_groups);
        content.removeAllViews();
        showCenteredMessage("暂无分组\n分组功能将在牌友关系建立后显示");
    }

    private LinearLayout friendRow(FriendData.Entry friend) {
        LinearLayout row = baseRow();
        LinearLayout identity = identity(friend.displayName, friend.publicPlayerId);
        TextView state = label(presence(friend.presence), 16, presenceColor(friend.presence), Gravity.LEFT);
        identity.addView(state, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 30));
        row.addView(identity, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        LinearLayout actions = new LinearLayout(activity);
        actions.setOrientation(LinearLayout.VERTICAL);
        TextView invite = actionButton("邀请", 0xFF53BBA3);
        invite.setOnClickListener(ignored -> {
            if (controller != null) controller.invite(friend.publicPlayerId);
        });
        actions.addView(invite, new LinearLayout.LayoutParams(116, 52));
        TextView more = actionButton(friend.shielded ? "取消屏蔽" : "屏蔽", 0xFFCB9A61);
        more.setOnClickListener(ignored -> {
            if (controller != null) controller.shield(friend.publicPlayerId, !friend.shielded);
        });
        actions.addView(more, new LinearLayout.LayoutParams(116, 40));
        TextView remove = actionButton("删除", 0xFFB96E5E);
        remove.setOnClickListener(ignored -> new AlertDialog.Builder(activity)
                .setTitle("删除牌友")
                .setMessage("确定从牌友列表中删除“" + friend.displayName + "”吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    if (controller != null) controller.remove(friend.publicPlayerId);
                })
                .show());
        actions.addView(remove, new LinearLayout.LayoutParams(116, 40));
        row.addView(actions);
        return row;
    }

    private LinearLayout applicationRow(FriendData.Application application) {
        LinearLayout row = baseRow();
        row.addView(identity(application.displayName, application.publicPlayerId),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        LinearLayout actions = new LinearLayout(activity);
        actions.setOrientation(LinearLayout.VERTICAL);
        TextView accept = actionButton("同意", 0xFF59BDA6);
        accept.setOnClickListener(ignored -> {
            if (controller != null) controller.accept(application.id);
        });
        actions.addView(accept, new LinearLayout.LayoutParams(112, 52));
        TextView reject = actionButton("拒绝", 0xFFC38D58);
        reject.setOnClickListener(ignored -> {
            if (controller != null) controller.reject(application.id);
        });
        actions.addView(reject, new LinearLayout.LayoutParams(112, 48));
        row.addView(actions);
        return row;
    }

    private LinearLayout notificationRow(FriendData.Notification notification) {
        LinearLayout row = baseRow();
        row.addView(identity(notification.actorDisplayName, notification.actorPublicPlayerId),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        row.addView(label("INVITE".equals(notification.type) ? "邀请您游戏" : "召回您游戏",
                18, 0xFF9D6740, Gravity.CENTER_VERTICAL),
                new LinearLayout.LayoutParams(130, LinearLayout.LayoutParams.MATCH_PARENT));
        return row;
    }

    private LinearLayout baseRow() {
        LinearLayout row = new LinearLayout(activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(14, 10, 14, 10);
        row.setBackground(roundRect(0xFFF3E5BE, 0xFFE2CDA0, 14, 2));
        row.setLayoutParams(rowParams(LinearLayout.LayoutParams.MATCH_PARENT, 118, 8));
        return row;
    }

    private LinearLayout identity(String name, long playerId) {
        LinearLayout value = new LinearLayout(activity);
        value.setOrientation(LinearLayout.VERTICAL);
        value.setGravity(Gravity.CENTER_VERTICAL);
        TextView nameView = label(name == null || name.isEmpty() ? "玩家" : name,
                22, 0xFF7A4E2B, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        value.addView(nameView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 48));
        value.addView(label("ID: " + playerId, 16, 0xFFA38A6B,
                Gravity.LEFT | Gravity.CENTER_VERTICAL), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 30));
        return value;
    }

    private TextView sectionTitle(String text) {
        TextView value = label(text, 21, 0xFF87572F, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        value.setTypeface(Typeface.DEFAULT_BOLD);
        value.setPadding(12, 0, 0, 0);
        value.setLayoutParams(rowParams(LinearLayout.LayoutParams.MATCH_PARENT, 52, 4));
        return value;
    }

    private void showCenteredMessage(String message) {
        content.removeAllViews();
        TextView value = label(message, 24, 0xFFB28C58, Gravity.CENTER);
        value.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(value, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 500));
    }

    private static String searchAction(String relation) {
        if ("FRIEND".equals(relation)) return "已是牌友";
        if ("PENDING".equals(relation)) return "已申请";
        return "牌友申请";
    }

    private static String presence(FriendData.Presence presence) {
        switch (presence) {
            case ONLINE: return "在线";
            case GAMING: return "游戏中";
            case WAITING: return "等待中";
            default: return "离线";
        }
    }

    private static int presenceColor(FriendData.Presence presence) {
        return presence == FriendData.Presence.OFFLINE ? 0xFF9E927F : 0xFF45A889;
    }

    private ImageView image(int resource) {
        ImageView image = new ImageView(activity);
        image.setImageResource(resource);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        return image;
    }

    private TextView actionButton(String text, int color) {
        TextView value = label(text, 18, Color.WHITE, Gravity.CENTER);
        value.setTypeface(Typeface.DEFAULT_BOLD);
        value.setBackground(roundRect(color, 0x99FFFFFF, 14, 2));
        return value;
    }

    private TextView label(String text, int size, int color, int gravity) {
        TextView value = new TextView(activity);
        value.setText(text);
        value.setTextSize(size);
        value.setTextColor(color);
        value.setGravity(gravity);
        return value;
    }

    private static LinearLayout.LayoutParams rowParams(int width, int height, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.bottomMargin = bottom;
        return params;
    }

    private static GradientDrawable roundRect(int fill, int stroke, int radius, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        drawable.setStroke(strokeWidth, stroke);
        return drawable;
    }

    private static final class ScaledRoot extends FrameLayout {
        private final int baseWidth;
        private final int baseHeight;

        ScaledRoot(Activity activity, int baseWidth, int baseHeight) {
            super(activity);
            this.baseWidth = baseWidth;
            this.baseHeight = baseHeight;
            setClipChildren(false);
        }

        void addBox(View view, int x, int y, int width, int height) {
            addView(view, new BaseParams(x, y, width, height));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            float sx = width / (float) baseWidth;
            float sy = height / (float) baseHeight;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                BaseParams params = (BaseParams) child.getLayoutParams();
                params.width = Math.round(params.baseWidth * sx);
                params.height = Math.round(params.baseHeight * sy);
                params.leftMargin = Math.round(params.baseX * sx);
                params.topMargin = Math.round(params.baseY * sy);
                if (child instanceof ScaledLabel) {
                    ((ScaledLabel) child).applyScale(Math.min(sx, sy));
                }
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private static final class ScaledLabel extends TextView {
        private final float baseTextSize;

        ScaledLabel(Activity activity, float baseTextSize) {
            super(activity);
            this.baseTextSize = baseTextSize;
            setIncludeFontPadding(false);
        }

        void applyScale(float scale) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * scale);
        }
    }

    private static final class BaseParams extends FrameLayout.LayoutParams {
        final int baseX;
        final int baseY;
        final int baseWidth;
        final int baseHeight;

        BaseParams(int x, int y, int width, int height) {
            super(width, height);
            baseX = x;
            baseY = y;
            baseWidth = width;
            baseHeight = height;
        }
    }
}
