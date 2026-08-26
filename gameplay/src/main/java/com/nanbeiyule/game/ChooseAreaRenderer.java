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

abstract class ChooseAreaRenderer extends ChooseAreaViewState {
    protected ChooseAreaRenderer(
            Context context,
            RegionApiClient.Catalog catalog,
            long selectedLobbyId) {
        super(context, catalog, selectedLobbyId);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(rotateTip);
        postDelayed(rotateTip, TIP_INTERVAL_MS);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(rotateTip);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        AdaptiveViewport viewport = currentViewport();
        Bitmap currentBackground =
                secondaryCity == null
                        ? mainBackground
                        : secondaryBackground;
        AdaptiveCanvasDrawing.drawFullBleedBitmap(
                canvas,
                currentBackground,
                bitmapPaint,
                viewport,
                PAGE_WIDTH,
                PAGE_HEIGHT);
        int saveCount =
                AdaptiveCanvasDrawing.apply(
                        canvas,
                        viewport.designTransform());
        if (secondaryCity == null) {
            drawMainArea(canvas, viewport);
        } else {
            drawSecondaryArea(canvas, viewport);
        }
        canvas.restoreToCount(saveCount);
    }

    protected void drawMainArea(
            Canvas canvas, AdaptiveViewport viewport) {
        int backSave = applySafeEdgeX(canvas, viewport, backHit.centerX());
        drawBackButton(canvas);
        canvas.restoreToCount(backSave);
        drawRegionMarkers(canvas);
        int listSave =
                applySafeEdgeX(
                        canvas,
                        viewport,
                        lobbyListAnchorX());
        drawLobbyList(canvas, catalog.allLobbies());
        canvas.restoreToCount(listSave);
        drawTip(canvas);
    }

    protected void drawSecondaryArea(
            Canvas canvas, AdaptiveViewport viewport) {
        int panelSave =
                applySafeEdgeX(
                        canvas,
                        viewport,
                        rightPanelAnchorX());
        canvas.drawBitmap(
                rightPanel,
                null,
                new RectF(PANEL_LEFT, 0.0f, PAGE_WIDTH, PAGE_HEIGHT),
                bitmapPaint);
        drawTitle(canvas);
        drawLobbyList(canvas, secondaryCity.lobbies());
        canvas.restoreToCount(panelSave);

        int backSave = applySafeEdgeX(canvas, viewport, backHit.centerX());
        drawBackButton(canvas);
        canvas.restoreToCount(backSave);
        drawSecondaryButtons(canvas);
    }

    protected void drawBackButton(Canvas canvas) {
        canvas.drawBitmap(backButton, null, backHit, bitmapPaint);
    }

    protected void drawTitle(Canvas canvas) {
        canvas.drawBitmap(
                chooseAreaTitle,
                null,
                new RectF(1355.0f, 25.0f, 1601.0f, 95.0f),
                bitmapPaint);
    }

    protected void drawRegionMarkers(Canvas canvas) {
        RegionApiClient.City selectedCity = catalog.findCity(selectedLobbyId);
        for (RegionApiClient.City city : catalog.cities()) {
            boolean selected =
                    selectedCity != null && selectedCity.code().equals(city.code());
            Bitmap plate = selected ? selectedMarker : normalMarker;
            RectF plateRect =
                    centeredRect(
                            city.mapX(),
                            city.mapY(),
                            MARKER_WIDTH,
                            MARKER_HEIGHT);
            canvas.drawBitmap(plate, null, plateRect, bitmapPaint);
            drawVerticalCityName(canvas, city.name(), city.mapX(), city.mapY());

            if (city.lobbies().isEmpty()) {
                canvas.drawBitmap(
                        notOpenMarker,
                        null,
                        centeredRect(
                                city.mapX() + 36.0f,
                                city.mapY() + 18.0f,
                                33.0f,
                                70.0f),
                        bitmapPaint);
                canvas.drawBitmap(
                        noLocationMarker,
                        null,
                        centeredRect(
                                city.mapX(),
                                city.mapY() + 67.0f,
                                25.0f,
                                40.0f),
                        bitmapPaint);
            } else if (city.lobbies().size() > 1) {
                canvas.drawBitmap(
                        multipleMarker,
                        null,
                        centeredRect(
                                city.mapX() + 38.0f,
                                city.mapY() + 14.0f,
                                34.0f,
                                84.0f),
                        bitmapPaint);
            }
            if (selected) {
                canvas.drawBitmap(
                        locationMarker,
                        null,
                        centeredRect(
                                city.mapX(),
                                city.mapY() + 70.0f,
                                27.0f,
                                43.0f),
                        bitmapPaint);
            }
        }
    }

    protected void drawVerticalCityName(
            Canvas canvas, String cityName, float centerX, float centerY) {
        int codePointCount = cityName.codePointCount(0, cityName.length());
        float lineHeight = 31.0f;
        float firstCenter =
                centerY - (codePointCount - 1) * lineHeight / 2.0f;
        int offset = 0;
        for (int index = 0; index < codePointCount; index++) {
            int codePoint = cityName.codePointAt(offset);
            String character = new String(Character.toChars(codePoint));
            drawCenteredText(
                    canvas,
                    character,
                    centerX,
                    firstCenter + index * lineHeight,
                    markerTextPaint);
            offset += Character.charCount(codePoint);
        }
    }

    protected void drawLobbyList(
            Canvas canvas, List<RegionApiClient.Lobby> displayedLobbies) {
        int saveCount = canvas.save();
        canvas.clipRect(listClip);
        for (int index = 0; index < displayedLobbies.size(); index++) {
            float centerY = FIRST_ROW_CENTER_Y + index * ROW_HEIGHT - listScroll;
            if (centerY < LIST_TOP - ROW_HEIGHT
                    || centerY > LIST_BOTTOM + ROW_HEIGHT) {
                continue;
            }
            Bitmap button =
                    index == pressedLobbyIndex
                            ? listButtonPressed
                            : listButtonNormal;
            canvas.drawBitmap(
                    button,
                    null,
                    new RectF(
                            LIST_LEFT,
                            centerY - 43.0f,
                            LIST_RIGHT,
                            centerY + 43.0f),
                    bitmapPaint);
            if (index == pressedLobbyIndex) {
                canvas.drawRoundRect(
                        new RectF(
                                LIST_LEFT,
                                centerY - 43.0f,
                                LIST_RIGHT,
                                centerY + 43.0f),
                        18.0f,
                        18.0f,
                        pressedPaint);
            }
            drawCenteredText(
                    canvas,
                    displayedLobbies.get(index).areaName(),
                    (LIST_LEFT + LIST_RIGHT) / 2.0f,
                    centerY,
                    lobbyTextPaint);
        }
        canvas.restoreToCount(saveCount);
        drawTitle(canvas);
    }

    protected void drawSecondaryButtons(Canvas canvas) {
        List<RegionApiClient.Lobby> lobbies = secondaryCity.lobbies();
        if (lobbies.isEmpty()) {
            return;
        }
        float availableWidth = PANEL_LEFT - 80.0f;
        float gap = lobbies.size() > 2 ? 18.0f : 26.0f;
        float buttonWidth = lobbies.size() > 2 ? 255.0f : 280.0f;
        float totalWidth =
                lobbies.size() * buttonWidth + (lobbies.size() - 1) * gap;
        float left = (availableWidth - totalWidth) / 2.0f + 40.0f;
        for (int index = 0; index < lobbies.size(); index++) {
            RectF rect =
                    new RectF(
                            left + index * (buttonWidth + gap),
                            742.0f,
                            left + index * (buttonWidth + gap) + buttonWidth,
                            870.0f);
            canvas.drawBitmap(secondaryButton, null, rect, bitmapPaint);
            drawCenteredText(
                    canvas,
                    lobbies.get(index).areaName(),
                    rect.centerX(),
                    rect.centerY(),
                    lobbyTextPaint);
        }
    }

    protected void drawTip(Canvas canvas) {
        String accent =
                getResources().getString(R.string.choose_area_tip_prefix);
        String message =
                getResources()
                        .getString(
                                tipIndex == 0
                                        ? R.string.choose_area_tip_map
                                        : R.string.choose_area_tip_list);
        float totalWidth =
                tipAccentPaint.measureText(accent) + tipPaint.measureText(message);
        float startX = (PANEL_LEFT - totalWidth) / 2.0f;
        float centerY = 896.0f;
        Paint.FontMetrics metrics = tipPaint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(accent, startX, baseline, tipAccentPaint);
        canvas.drawText(
                message,
                startX + tipAccentPaint.measureText(accent),
                baseline,
                tipPaint);
    }
}
