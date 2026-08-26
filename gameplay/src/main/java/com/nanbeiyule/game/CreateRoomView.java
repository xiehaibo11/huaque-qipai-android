package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.MotionEvent;
import java.util.List;

final class CreateRoomView extends AdaptiveCanvasView implements CreateRoomTouchController.Host {
    interface Actions {
        void onGameSelected(CreateRoomGame game);
        void onExternalGameRequested();
        void onSelectionChanged(CreateRoomState state);
        void onCreateRequested(CreateRoomState state);
        void onFeedbackRequested();
    }

    private final Runnable closeAction;
    private final Actions actions;
    private final CreateRoomRenderer renderer;
    private final CreateRoomTouchController touchController;
    private List<CreateRoomGame> games = List.of();
    private int selectedGameIndex = -1;
    private CreateRoomState state;
    private float gameScroll;
    private float ruleScroll;
    private String openTipNode;
    private String openDropdownNode;
    private boolean loading;
    private String error;
    private CreateRoomResult result;
    private Runnable buttonClickSound = () -> {};

    CreateRoomView(Context context, Runnable closeAction, Actions actions) {
        super(context);
        this.closeAction = closeAction == null ? () -> {} : closeAction;
        this.actions = actions == null ? new NoOpActions() : actions;
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), CreateRoomLayout.FONT_ASSET);
        renderer = new CreateRoomRenderer(new CreateRoomDrawableSet(getResources()), typeface);
        touchController = new CreateRoomTouchController(this);
        setFocusable(true);
        setClickable(true);
    }

    void setGames(List<CreateRoomGame> games) {
        setGames(games, 0L);
    }

    void setGames(List<CreateRoomGame> games, long initialGameId) {
        this.games = games == null ? List.of() : List.copyOf(games);
        selectedGameIndex = initialGameIndex(this.games, initialGameId);
        gameScroll = 0;
        invalidate();
    }

    static int initialGameIndex(List<CreateRoomGame> games, long initialGameId) {
        return CreateRoomEntryPolicy.defaultGameIndex(games, initialGameId);
    }

    void setState(CreateRoomState state) {
        this.state = state;
        ruleScroll = 0;
        openTipNode = null;
        openDropdownNode = null;
        loading = false;
        error = null;
        invalidate();
    }

    void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            error = null;
        }
        invalidate();
    }

    void setError(String error) {
        this.loading = false;
        this.error = error;
        invalidate();
    }

    void setResult(CreateRoomResult result) {
        this.loading = false;
        this.result = result;
        invalidate();
    }

    void setButtonClickSound(Runnable sound) {
        buttonClickSound = sound == null ? () -> {} : sound;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.rgb(48, 76, 91));
        CreateRoomLayout.Viewport viewport = viewport();
        canvas.save();
        canvas.translate(viewport.offsetX(), viewport.offsetY());
        canvas.scale(viewport.scale(), viewport.scale());
        renderer.draw(canvas, games, selectedGameIndex, state, gameScroll, ruleScroll,
                openTipNode, openDropdownNode, loading, error, result);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        return touchController.onTouchEvent(
                event, viewport());
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override public List<CreateRoomGame> games() { return games; }
    @Override public int selectedGameIndex() { return selectedGameIndex; }
    @Override public CreateRoomState state() { return state; }
    @Override public float gameScroll() { return gameScroll; }
    @Override public float ruleScroll() { return ruleScroll; }
    @Override public String openTipNode() { return openTipNode; }
    @Override public String openDropdownNode() { return openDropdownNode; }
    @Override public boolean hasResult() { return result != null; }
    @Override public boolean loading() { return loading; }

    @Override
    public void setGameScroll(float value) {
        gameScroll = value;
        invalidate();
    }

    @Override
    public void setRuleScroll(float value) {
        ruleScroll = value;
        invalidate();
    }

    @Override
    public void setOpenTipNode(String value) {
        openTipNode = value;
        invalidate();
    }

    @Override
    public void setOpenDropdownNode(String value) {
        openDropdownNode = value;
        invalidate();
    }

    @Override
    public void selectGame(int index) {
        if (loading || index < 0 || index >= games.size()) {
            return;
        }
        if (index == selectedGameIndex) {
            return;
        }
        CreateRoomGame game = games.get(index);
        buttonClickSound.run();
        if (!CreateRoomEntryPolicy.shouldLoadRuleConfig(game.gameId())) {
            actions.onExternalGameRequested();
            return;
        }
        selectedGameIndex = index;
        state = null;
        ruleScroll = 0;
        loading = true;
        error = null;
        actions.onGameSelected(game);
        invalidate();
    }

    @Override
    public void stateChanged() {
        buttonClickSound.run();
        error = state != null && !CreateRoomEntryPolicy.supportsRoomCreation(state.gameId())
                ? CreateRoomEntryPolicy.ROOM_CREATION_UNAVAILABLE_MESSAGE
                : state != null && !state.isCreateReady()
                        ? "规则配置尚未通过服务器校验，仅供预览"
                        : null;
        actions.onSelectionChanged(state);
        invalidate();
    }

    @Override
    public void createRequested() {
        if (loading || state == null || !state.isCreateReady()) {
            return;
        }
        buttonClickSound.run();
        actions.onCreateRequested(state);
    }

    @Override
    public void feedbackRequested() {
        buttonClickSound.run();
        actions.onFeedbackRequested();
    }

    @Override
    public void closeRequested() {
        buttonClickSound.run();
        closeAction.run();
    }

    private static final class NoOpActions implements Actions {
        @Override public void onGameSelected(CreateRoomGame game) {}
        @Override public void onExternalGameRequested() {}
        @Override public void onSelectionChanged(CreateRoomState state) {}
        @Override public void onCreateRequested(CreateRoomState state) {}
        @Override public void onFeedbackRequested() {}
    }

    private CreateRoomLayout.Viewport viewport() {
        AdaptiveViewport.Insets insets = adaptiveSafeInsets();
        return CreateRoomLayout.safeViewport(
                getWidth(),
                getHeight(),
                Math.round(insets.left()),
                Math.round(insets.top()),
                Math.round(insets.right()),
                Math.round(insets.bottom()));
    }
}
