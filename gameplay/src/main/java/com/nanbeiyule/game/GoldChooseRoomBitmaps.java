package com.nanbeiyule.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import com.nanbeiyule.game.goldroom.GoldChooseRoomAtlas;
import java.util.HashMap;
import java.util.Map;

/**
 * Lazily slices upright frames out of the two original gold-hall atlases.
 *
 * <p>Both sheets are byte-level copies of the 1.5.4 originals, so frames are cut with the
 * rectangles emitted by android/tools/extract_original_gold_choose_room_assets.py.
 */
final class GoldChooseRoomBitmaps {
    /** Frame prefixes of the two packed sheets. */
    private static final String CHOOSE_ROOM_PREFIX = "hall/Image/NewGoldHall/ChooseRoom/";

    private static final String MAIN_PREFIX = "hall/Image/NewGoldHall/Main/";
    private static final String ACT_BUTTON_PREFIX = "hall/Image/NewGoldHall/ActBtns/";
    private static final String POP_PREFIX = "hall/Image/NewGoldHall/Pop/";
    private static final int SHEET_CHOOSE_ROOM = 0;
    private static final int SHEET_MAIN = 1;
    private static final int SHEET_ACT_BUTTON = 2;
    private static final int SHEET_POP = 3;

    private final Context context;
    private final Map<String, Bitmap> cache = new HashMap<>();
    private Bitmap chooseRoomSheet;
    private Bitmap mainSheet;
    private Bitmap actButtonSheet;
    private Bitmap popSheet;

    GoldChooseRoomBitmaps(Context context) {
        this.context = context.getApplicationContext();
    }

    /** A frame of {@code ChooseRoom/_Plist.png}, e.g. {@code Img_xc_1.png}. */
    Bitmap chooseRoom(String fileName) {
        return frame(
                CHOOSE_ROOM_PREFIX + fileName,
                GoldChooseRoomAtlas.CHOOSE_ROOM_NAMES,
                GoldChooseRoomAtlas.CHOOSE_ROOM_FRAMES,
                GoldChooseRoomAtlas.CHOOSE_ROOM_RESOURCE,
                SHEET_CHOOSE_ROOM);
    }

    /** A frame of {@code Main/_Plist.png}, e.g. {@code Btn_fanhui.png}. */
    Bitmap main(String fileName) {
        return frame(
                MAIN_PREFIX + fileName,
                GoldChooseRoomAtlas.MAIN_NAMES,
                GoldChooseRoomAtlas.MAIN_FRAMES,
                GoldChooseRoomAtlas.MAIN_RESOURCE,
                SHEET_MAIN);
    }

    /** A frame of {@code ActBtns/_Plist.png}, e.g. {@code Btn_xslb.png}. */
    Bitmap actButton(String fileName) {
        return frame(
                ACT_BUTTON_PREFIX + fileName,
                GoldChooseRoomAtlas.ACT_BUTTON_NAMES,
                GoldChooseRoomAtlas.ACT_BUTTON_FRAMES,
                GoldChooseRoomAtlas.ACT_BUTTON_RESOURCE,
                SHEET_ACT_BUTTON);
    }

    /** A frame of {@code Pop/_Plist.png}, e.g. {@code Img_tc_title4.png} (规则弹层图素). */
    Bitmap pop(String fileName) {
        return frame(
                POP_PREFIX + fileName,
                GoldChooseRoomAtlas.POP_NAMES,
                GoldChooseRoomAtlas.POP_FRAMES,
                GoldChooseRoomAtlas.POP_RESOURCE,
                SHEET_POP);
    }

    /** A loose drawable copied verbatim from the original, e.g. {@code gold_hall_background}. */
    Bitmap standalone(String resourceName) {
        Bitmap cached = cache.get(resourceName);
        if (cached != null) {
            return cached;
        }
        int resourceId =
                context.getResources()
                        .getIdentifier(resourceName, "drawable", context.getPackageName());
        if (resourceId == 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap loaded = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (loaded != null) {
            cache.put(resourceName, loaded);
        }
        return loaded;
    }

    void recycle() {
        for (Bitmap bitmap : cache.values()) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        cache.clear();
        chooseRoomSheet = releaseSheet(chooseRoomSheet);
        mainSheet = releaseSheet(mainSheet);
        actButtonSheet = releaseSheet(actButtonSheet);
        popSheet = releaseSheet(popSheet);
    }

    private static Bitmap releaseSheet(Bitmap sheet) {
        if (sheet != null && !sheet.isRecycled()) {
            sheet.recycle();
        }
        return null;
    }

    private Bitmap frame(
            String frameName,
            String[] names,
            int[][] frames,
            String resourceName,
            int sheetKind) {
        Bitmap cached = cache.get(frameName);
        if (cached != null) {
            return cached;
        }
        int index = indexOf(names, frameName);
        if (index < 0) {
            return null;
        }
        Bitmap sheet = sheet(resourceName, sheetKind);
        if (sheet == null) {
            return null;
        }
        int[] rect = frames[index];
        Bitmap sliced = slice(sheet, rect);
        cache.put(frameName, sliced);
        return sliced;
    }

    private Bitmap sheet(String resourceName, int sheetKind) {
        Bitmap existing = existingSheet(sheetKind);
        if (existing != null && !existing.isRecycled()) {
            return existing;
        }
        int resourceId =
                context.getResources()
                        .getIdentifier(resourceName, "drawable", context.getPackageName());
        if (resourceId == 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap loaded = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        setSheet(sheetKind, loaded);
        return loaded;
    }

    private Bitmap existingSheet(int sheetKind) {
        if (sheetKind == SHEET_CHOOSE_ROOM) {
            return chooseRoomSheet;
        }
        if (sheetKind == SHEET_ACT_BUTTON) {
            return actButtonSheet;
        }
        if (sheetKind == SHEET_POP) {
            return popSheet;
        }
        return mainSheet;
    }

    private void setSheet(int sheetKind, Bitmap loaded) {
        if (sheetKind == SHEET_CHOOSE_ROOM) {
            chooseRoomSheet = loaded;
        } else if (sheetKind == SHEET_ACT_BUTTON) {
            actButtonSheet = loaded;
        } else if (sheetKind == SHEET_POP) {
            popSheet = loaded;
        } else {
            mainSheet = loaded;
        }
    }

    private static Bitmap slice(Bitmap sheet, int[] rect) {
        int x = rect[0];
        int y = rect[1];
        int width = rect[2];
        int height = rect[3];
        boolean rotated = rect[4] != 0;
        // A rotated frame is stored as height x width and must be turned back anti-clockwise.
        int storedWidth = rotated ? height : width;
        int storedHeight = rotated ? width : height;
        if (x < 0
                || y < 0
                || x + storedWidth > sheet.getWidth()
                || y + storedHeight > sheet.getHeight()) {
            return null;
        }
        Bitmap stored = Bitmap.createBitmap(sheet, x, y, storedWidth, storedHeight);
        if (!rotated) {
            return stored;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(-90.0f);
        Bitmap upright =
                Bitmap.createBitmap(stored, 0, 0, storedWidth, storedHeight, matrix, true);
        if (upright != stored) {
            stored.recycle();
        }
        return upright;
    }

    private static int indexOf(String[] names, String frameName) {
        for (int index = 0; index < names.length; index++) {
            if (names[index].equals(frameName)) {
                return index;
            }
        }
        return -1;
    }
}
