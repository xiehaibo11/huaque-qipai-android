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

/** Public region-selection facade backed by focused rendering and interaction layers. */
public final class ChooseAreaView extends ChooseAreaInteractionView {
    public ChooseAreaView(
            Context context,
            RegionApiClient.Catalog catalog,
            long selectedLobbyId) {
        super(context, catalog, selectedLobbyId);
    }
}
