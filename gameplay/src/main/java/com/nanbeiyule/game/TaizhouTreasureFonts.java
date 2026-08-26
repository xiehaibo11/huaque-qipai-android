package com.nanbeiyule.game;

import android.content.res.Resources;
import android.graphics.Typeface;

/** Original JuBaoPen BMFonts and Fangzheng CuYuan face loaded from packaged evidence. */
final class TaizhouTreasureFonts {
    static final String DRAW_COUNT_ASSET = "taizhou_treasure_fonts/Fnt_chouj-export.fnt";
    static final String DIAMOND_PRICE_ASSET = "taizhou_treasure_fonts/Fnt_zuans-export.fnt";
    static final String FORTUNE_ASSET = "taizhou_treasure_fonts/Fnt_yunshi-export.fnt";
    static final String LEVEL_ASSET = "taizhou_treasure_fonts/Fnt_dengji-export.fnt";
    static final String FORTUNE_VALUE_ASSET = "taizhou_treasure_fonts/Fnt_ysz-export.fnt";
    static final String TEXT_ASSET = "taizhou_treasure_fonts/fangzhengcuyuan.TTF";

    private final SxvipBitmapFont drawCount;
    private final SxvipBitmapFont diamondPrice;
    private final SxvipBitmapFont fortune;
    private final SxvipBitmapFont level;
    private final SxvipBitmapFont fortuneValue;
    private final Typeface text;

    private TaizhouTreasureFonts(
            SxvipBitmapFont drawCount,
            SxvipBitmapFont diamondPrice,
            SxvipBitmapFont fortune,
            SxvipBitmapFont level,
            SxvipBitmapFont fortuneValue,
            Typeface text) {
        this.drawCount = drawCount;
        this.diamondPrice = diamondPrice;
        this.fortune = fortune;
        this.level = level;
        this.fortuneValue = fortuneValue;
        this.text = text;
    }

    static TaizhouTreasureFonts load(Resources resources) {
        return new TaizhouTreasureFonts(
                SxvipBitmapFont.load(resources, DRAW_COUNT_ASSET),
                SxvipBitmapFont.load(resources, DIAMOND_PRICE_ASSET),
                SxvipBitmapFont.load(resources, FORTUNE_ASSET),
                SxvipBitmapFont.load(resources, LEVEL_ASSET),
                SxvipBitmapFont.load(resources, FORTUNE_VALUE_ASSET),
                Typeface.createFromAsset(resources.getAssets(), TEXT_ASSET));
    }

    SxvipBitmapFont drawCount() {
        return drawCount;
    }

    SxvipBitmapFont diamondPrice() {
        return diamondPrice;
    }

    SxvipBitmapFont fortune() {
        return fortune;
    }

    SxvipBitmapFont level() {
        return level;
    }

    SxvipBitmapFont fortuneValue() {
        return fortuneValue;
    }

    Typeface text() {
        return text;
    }
}
