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

abstract class ChooseAreaInteractionView extends ChooseAreaRenderer {
    protected ChooseAreaInteractionView(
            Context context,
            RegionApiClient.Catalog catalog,
            long selectedLobbyId) {
        super(context, catalog, selectedLobbyId);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        float[] pagePoint = toPagePoint(event.getX(), event.getY());
        float pageX = pagePoint[0];
        float pageY = pagePoint[1];
        AdaptiveViewport viewport = currentViewport();
        float backPageX =
                pageX
                        - viewport.safeEdgeOffsetX(
                                backHit.centerX());
        float listPageX =
                pageX
                        - viewport.safeEdgeOffsetX(
                                secondaryCity == null
                                        ? lobbyListAnchorX()
                                        : rightPanelAnchorX());
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            downPageX = pageX;
            downPageY = pageY;
            lastPageY = pageY;
            dragged = false;
            listGesture = listClip.contains(listPageX, pageY);
            if (listGesture) {
                pressedLobbyIndex = lobbyIndexAt(pageY, displayedLobbies());
                invalidate();
            }
            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (listGesture) {
                float deltaY = pageY - lastPageY;
                if (Math.abs(pageY - downPageY) > 8.0f) {
                    dragged = true;
                    pressedLobbyIndex = -1;
                }
                listScroll =
                        clamp(
                                listScroll - deltaY,
                                0.0f,
                                maxListScroll(displayedLobbies()));
                lastPageY = pageY;
                invalidate();
            }
            return true;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return super.onTouchEvent(event);
        }

        if (listGesture) {
            int index = lobbyIndexAt(pageY, displayedLobbies());
            pressedLobbyIndex = -1;
            invalidate();
            if (!dragged && index >= 0) {
                selectLobby(displayedLobbies().get(index));
            }
            performClick();
            return true;
        }
        if (backHit.contains(backPageX, pageY)) {
            if (secondaryCity != null) {
                secondaryCity = null;
                secondaryBackground = null;
                listScroll = 0.0f;
                invalidate();
            } else if (backRequestedListener != null) {
                backRequestedListener.onBackRequested();
            }
            performClick();
            return true;
        }
        if (secondaryCity != null) {
            int buttonIndex = secondaryButtonIndexAt(pageX, pageY);
            if (buttonIndex >= 0) {
                selectLobby(secondaryCity.lobbies().get(buttonIndex));
                performClick();
                return true;
            }
            return false;
        }
        for (RegionApiClient.City city : catalog.cities()) {
            RectF hit =
                    centeredRect(city.mapX(), city.mapY(), 82.0f, 132.0f);
            if (!hit.contains(pageX, pageY)) {
                continue;
            }
            if (city.lobbies().isEmpty()) {
                Toast.makeText(
                                getContext(),
                                R.string.choose_area_not_open,
                                Toast.LENGTH_SHORT)
                        .show();
            } else if (city.lobbies().size() == 1) {
                selectLobby(city.lobbies().get(0));
            } else {
                showSecondaryArea(city);
            }
            performClick();
            return true;
        }
        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        buttonClickSound.run();
        return true;
    }

    protected void showSecondaryArea(RegionApiClient.City city) {
        Bitmap background = loadSecondaryBackground(city.secondaryMap());
        if (background == null) {
            Toast.makeText(
                            getContext(),
                            R.string.choose_area_secondary_unavailable,
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        secondaryCity = city;
        secondaryBackground = background;
        listScroll = 0.0f;
        invalidate();
    }

    protected Bitmap loadSecondaryBackground(String name) {
        int resourceId =
                switch (name) {
                    case "second_area_hangzhou" ->
                            R.drawable.second_area_hangzhou;
                    case "second_area_lishui" -> R.drawable.second_area_lishui;
                    case "second_area_ningbo" -> R.drawable.second_area_ningbo;
                    case "second_area_shaoxing" ->
                            R.drawable.second_area_shaoxing;
                    case "second_area_wenzhou" -> R.drawable.second_area_wenzhou;
                    default -> 0;
                };
        return resourceId == 0 ? null : loadBitmap(resourceId);
    }

    protected void selectLobby(RegionApiClient.Lobby lobby) {
        selectedLobbyId = lobby.lobbyId();
        if (regionSelectedListener != null) {
            regionSelectedListener.onRegionSelected(lobby);
        }
    }

    protected List<RegionApiClient.Lobby> displayedLobbies() {
        return secondaryCity == null
                ? catalog.allLobbies()
                : secondaryCity.lobbies();
    }

    protected int lobbyIndexAt(
            float pageY, List<RegionApiClient.Lobby> displayedLobbies) {
        float firstTop = FIRST_ROW_CENTER_Y - ROW_HEIGHT / 2.0f;
        int index = (int) Math.floor((pageY + listScroll - firstTop) / ROW_HEIGHT);
        return index >= 0 && index < displayedLobbies.size() ? index : -1;
    }

    protected int secondaryButtonIndexAt(float pageX, float pageY) {
        if (pageY < 742.0f || pageY > 870.0f) {
            return -1;
        }
        List<RegionApiClient.Lobby> lobbies = secondaryCity.lobbies();
        float availableWidth = PANEL_LEFT - 80.0f;
        float gap = lobbies.size() > 2 ? 18.0f : 26.0f;
        float buttonWidth = lobbies.size() > 2 ? 255.0f : 280.0f;
        float totalWidth =
                lobbies.size() * buttonWidth + (lobbies.size() - 1) * gap;
        float left = (availableWidth - totalWidth) / 2.0f + 40.0f;
        for (int index = 0; index < lobbies.size(); index++) {
            float buttonLeft = left + index * (buttonWidth + gap);
            if (pageX >= buttonLeft && pageX <= buttonLeft + buttonWidth) {
                return index;
            }
        }
        return -1;
    }

    protected float maxListScroll(List<RegionApiClient.Lobby> displayedLobbies) {
        return Math.max(0.0f, (displayedLobbies.size() - VISIBLE_ROWS) * ROW_HEIGHT);
    }

    protected float[] toPagePoint(float viewX, float viewY) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return new float[] {-1.0f, -1.0f};
        }
        AdaptiveViewport.Transform transform =
                currentViewport().designTransform();
        return new float[] {
            transform.unmapX(viewX), transform.unmapY(viewY)
        };
    }

    protected AdaptiveViewport currentViewport() {
        return adaptiveViewport(PAGE_WIDTH, PAGE_HEIGHT);
    }

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
        canvas.translate(
                viewport.safeEdgeOffsetX(anchorX),
                0.0f);
        return saveCount;
    }

    protected Bitmap loadBitmap(int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        if (bitmap == null) {
            throw new IllegalStateException(
                    "Unable to decode drawable " + resourceId);
        }
        return bitmap;
    }

    protected static RectF centeredRect(
            float centerX, float centerY, float width, float height) {
        return new RectF(
                centerX - width / 2.0f,
                centerY - height / 2.0f,
                centerX + width / 2.0f,
                centerY + height / 2.0f);
    }

    protected static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    protected static void drawCenteredText(
            Canvas canvas, String text, float centerX, float centerY, Paint paint) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2.0f;
        canvas.drawText(text, centerX, baseline, paint);
    }
}
