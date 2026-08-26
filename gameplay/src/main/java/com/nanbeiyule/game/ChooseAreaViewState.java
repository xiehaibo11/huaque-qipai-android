package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Region picker reconstructed from the original ChooseArea Lua behavior.
 *
 * <p>The catalog is supplied exclusively by the real region API. This view owns presentation and
 * hit testing only; it does not contain a fallback city or lobby data set.
 */

abstract class ChooseAreaViewState extends AdaptiveCanvasView {
    public interface OnRegionSelectedListener {
        void onRegionSelected(RegionApiClient.Lobby lobby);
    }

    public interface OnBackRequestedListener {
        void onBackRequested();
    }

    protected static final float PAGE_WIDTH = 1672.0f;
    protected static final float PAGE_HEIGHT = 941.0f;
    protected static final float PANEL_LEFT = 1277.0f;
    protected static final float LIST_LEFT = 1290.0f;
    protected static final float LIST_RIGHT = 1638.0f;
    protected static final float LIST_TOP = 120.0f;
    protected static final float LIST_BOTTOM = 824.0f;
    protected static final float FIRST_ROW_CENTER_Y = 176.0f;
    protected static final float ROW_HEIGHT = 94.0f;
    protected static final int VISIBLE_ROWS = 7;
    protected static final float MARKER_WIDTH = 48.0f;
    protected static final float MARKER_HEIGHT = 100.0f;
    protected static final long TIP_INTERVAL_MS = 5_000L;

    protected final Paint bitmapPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    protected final Paint markerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint lobbyTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint tipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint tipAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint pressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected final Bitmap mainBackground;
    protected final Bitmap selectedMarker;
    protected final Bitmap normalMarker;
    protected final Bitmap multipleMarker;
    protected final Bitmap notOpenMarker;
    protected final Bitmap locationMarker;
    protected final Bitmap noLocationMarker;
    protected final Bitmap chooseAreaTitle;
    protected final Bitmap listButtonNormal;
    protected final Bitmap listButtonPressed;
    protected final Bitmap listButtonDisabled;
    protected final Bitmap rightPanel;
    protected final Bitmap backButton;
    protected final Bitmap secondaryButton;

    protected final RectF backHit = new RectF(12.0f, 8.0f, 150.0f, 105.0f);
    protected final RectF listClip =
            new RectF(LIST_LEFT, LIST_TOP, LIST_RIGHT, LIST_BOTTOM);
    protected final Runnable rotateTip =
            new Runnable() {
                @Override
                public void run() {
                    tipIndex = (tipIndex + 1) % 2;
                    invalidate();
                    postDelayed(this, TIP_INTERVAL_MS);
                }
            };

    protected RegionApiClient.Catalog catalog;
    protected long selectedLobbyId;
    protected RegionApiClient.City secondaryCity;
    protected Bitmap secondaryBackground;
    protected OnRegionSelectedListener regionSelectedListener;
    protected OnBackRequestedListener backRequestedListener;

    protected float listScroll;
    protected float downPageX;
    protected float downPageY;
    protected float lastPageY;
    protected boolean listGesture;
    protected boolean dragged;
    protected int pressedLobbyIndex = -1;
    protected int tipIndex;
    protected Runnable buttonClickSound = () -> {};

    public ChooseAreaViewState(
            Context context,
            RegionApiClient.Catalog catalog,
            long selectedLobbyId) {
        super(context);
        this.catalog = catalog;
        this.selectedLobbyId = selectedLobbyId;

        mainBackground = loadBitmap(R.drawable.choose_area_map_nanbei_v1);
        selectedMarker = loadBitmap(R.drawable.choose_area_check);
        normalMarker = loadBitmap(R.drawable.choose_area_check_no);
        multipleMarker = loadBitmap(R.drawable.choose_area_more_open);
        notOpenMarker = loadBitmap(R.drawable.choose_area_not_open);
        locationMarker = loadBitmap(R.drawable.choose_area_logo);
        noLocationMarker = loadBitmap(R.drawable.choose_area_no_logo);
        chooseAreaTitle = loadBitmap(R.drawable.choose_area_title);
        listButtonNormal = loadBitmap(R.drawable.choose_area_btn1);
        listButtonPressed = loadBitmap(R.drawable.choose_area_btn2);
        listButtonDisabled = loadBitmap(R.drawable.choose_area_btn3);
        rightPanel = loadBitmap(R.drawable.choose_area_name_bg);
        backButton = loadBitmap(R.drawable.com_btn_back);
        secondaryButton = loadBitmap(R.drawable.btn_bg);

        Typeface typeface;
        try {
            typeface =
                    Typeface.createFromAsset(
                            context.getAssets(), "fonts/fangzhengcuyuan.ttf");
        } catch (RuntimeException ignored) {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        }
        markerTextPaint.setTypeface(typeface);
        markerTextPaint.setTextAlign(Paint.Align.CENTER);
        markerTextPaint.setTextSize(29.0f);
        markerTextPaint.setColor(Color.WHITE);
        markerTextPaint.setShadowLayer(2.0f, 0.0f, 2.0f, Color.rgb(62, 35, 22));

        lobbyTextPaint.setTypeface(typeface);
        lobbyTextPaint.setTextAlign(Paint.Align.CENTER);
        lobbyTextPaint.setTextSize(31.0f);
        lobbyTextPaint.setColor(Color.rgb(133, 68, 29));
        lobbyTextPaint.setShadowLayer(1.0f, 0.0f, 1.0f, Color.WHITE);

        tipPaint.setTypeface(typeface);
        tipPaint.setTextSize(25.0f);
        tipPaint.setColor(Color.WHITE);
        tipAccentPaint.setTypeface(typeface);
        tipAccentPaint.setTextSize(25.0f);
        tipAccentPaint.setColor(Color.rgb(255, 224, 38));
        pressedPaint.setColor(Color.argb(55, 84, 51, 22));

        setBackgroundColor(Color.rgb(23, 66, 62));
        setContentDescription(
                getResources().getString(R.string.choose_area_page_title));
        setFocusable(true);
    }

    public void setButtonClickSound(Runnable buttonClickSound) {
        this.buttonClickSound =
                buttonClickSound == null ? () -> {} : buttonClickSound;
    }

    public void setOnRegionSelectedListener(OnRegionSelectedListener listener) {
        regionSelectedListener = listener;
    }

    public void setOnBackRequestedListener(OnBackRequestedListener listener) {
        backRequestedListener = listener;
    }

    protected abstract void drawMainArea(
            Canvas canvas, AdaptiveViewport viewport);

    protected abstract void drawSecondaryArea(
            Canvas canvas, AdaptiveViewport viewport);

    protected abstract void drawBackButton(Canvas canvas);

    protected abstract void drawTitle(Canvas canvas);

    protected abstract void drawRegionMarkers(Canvas canvas);

    protected abstract void drawVerticalCityName(
            Canvas canvas, String cityName, float centerX, float centerY);

    protected abstract void drawLobbyList(
            Canvas canvas, List<RegionApiClient.Lobby> displayedLobbies);

    protected abstract void drawSecondaryButtons(Canvas canvas);

    protected abstract void drawTip(Canvas canvas);

    protected abstract void showSecondaryArea(RegionApiClient.City city);

    protected abstract Bitmap loadSecondaryBackground(String name);

    protected abstract void selectLobby(RegionApiClient.Lobby lobby);

    protected abstract List<RegionApiClient.Lobby> displayedLobbies();

    protected abstract int lobbyIndexAt(
            float pageY, List<RegionApiClient.Lobby> displayedLobbies);

    protected abstract int secondaryButtonIndexAt(float pageX, float pageY);

    protected abstract float maxListScroll(List<RegionApiClient.Lobby> displayedLobbies);

    protected abstract float[] toPagePoint(float viewX, float viewY);

    protected abstract AdaptiveViewport currentViewport();

    protected abstract Bitmap loadBitmap(int resourceId);

    protected static float lobbyListAnchorX() {
        return (LIST_LEFT + LIST_RIGHT) / 2.0f;
    }

    protected static float rightPanelAnchorX() {
        return (PANEL_LEFT + PAGE_WIDTH) / 2.0f;
    }

    protected static int applySafeEdgeX(
            Canvas canvas,
            AdaptiveViewport viewport,
            float anchorX) {
        int saveCount = canvas.save();
        canvas.translate(viewport.safeEdgeOffsetX(anchorX), 0.0f);
        return saveCount;
    }

    protected static RectF centeredRect(
            float centerX, float centerY, float width, float height) {
        return new RectF(
                centerX - width / 2.0f,
                centerY - height / 2.0f,
                centerX + width / 2.0f,
                centerY + height / 2.0f);
    }

    protected static void drawCenteredText(
            Canvas canvas,
            String text,
            float centerX,
            float centerY,
            Paint paint) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline =
                centerY - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(text, centerX, baseline, paint);
    }
}
