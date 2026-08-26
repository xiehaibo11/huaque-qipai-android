package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

/** Native rendering of the original JuBaoPenDesView.csb activity explanation. */
@SuppressLint("ViewConstructor")
final class TaizhouTreasureDescriptionView extends TaizhouToolView {
    static final String EVIDENCE_CSB = "JuBaoPenDesView.csb";
    static final String ACTIVITY_DESCRIPTION =
            "1. 宝物获取及有效期：抽取成功后获得相应的宝物，每个宝物有效期为3小时，自获得时开始计算。如果在有效期内再次抽到相同的宝物，那么宝物升1级，运势叠加，并且重置有效期至3小时。满级为10级，达到满级时抽中相同宝物，不再升级，有效期将会重置为3小时。\n\n"
                    + "2. 宝物：一共16个宝物，分为4个品质（普通、精品、极品、绝品），品质越高，获得的运势值越高\n\n"
                    + "3.运势加成：每个宝物都带有纯表现性质的运势加成，虽无实际游戏数值影响，但可为您的游戏历程增添别样乐趣与心理助力。\n\n"
                    + "4.抽取概率说明：所有宝物概率均摊，约为6.25%";

    private static final TaizhouTreasurePotLayout.Node PANEL =
            TaizhouTreasurePotLayout.fromCocos(
                    966.93f, 508.55f, 1607.0f, 870.0f, 0.5f, 0.5f);
    private static final TaizhouTreasurePotLayout.Node HEADER =
            TaizhouTreasurePotLayout.fromCocos(
                    939.72f, 892.88f, 1655.0f, 285.0f, 0.5f, 0.5f);
    private static final TaizhouTreasurePotLayout.Node TITLE =
            TaizhouTreasurePotLayout.fromCocos(
                    415.58f, 917.73f, 260.0f, 67.0f, 0.5f, 0.5f);
    private static final TaizhouTreasurePotLayout.Node CLOSE =
            TaizhouTreasurePotLayout.fromCocos(
                    1712.11f, 919.98f, 100.0f, 100.0f, 0.5f, 0.5f);
    private static final TaizhouTreasurePotLayout.Node CLOSE_ART =
            TaizhouTreasurePotLayout.fromCocos(
                    1712.11f, 919.98f, 53.0f, 54.0f, 0.5f, 0.5f);
    private static final TaizhouTreasurePotLayout.Node CONTENT =
            TaizhouTreasurePotLayout.fromCocos(
                    967.0f, 500.71f, 1447.0f, 664.0f, 0.5f, 0.5f);

    private Runnable closeAction = () -> {};
    private final Bitmap panel;
    private final Bitmap header;
    private final Bitmap title;
    private final Bitmap close;
    private final Typeface typeface;
    private boolean released;

    TaizhouTreasureDescriptionView(Context context) {
        super(context);
        panel = bitmap(R.drawable.taizhou_treasure_inventory_detail_bg);
        header = bitmap(R.drawable.taizhou_treasure_inventory_panel);
        title = bitmap(R.drawable.taizhou_treasure_description_title);
        close = bitmap(R.drawable.taizhou_treasure_close);
        typeface = Typeface.createFromAsset(
                getResources().getAssets(), TaizhouTreasureFonts.TEXT_ASSET);
    }

    void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction == null ? () -> {} : closeAction;
    }

    @Override
    protected void drawDesign(Canvas canvas) {
        TaizhouTreasureCanvas.drawNineSlice(
                canvas, panel, rect(PANEL), 159, 74, 164, 77, bitmapPaint);
        drawBitmap(canvas, header, rect(HEADER));
        drawBitmap(canvas, title, rect(TITLE));
        drawBitmap(canvas, close, rect(CLOSE_ART));
        TaizhouTreasureCanvas.wrappedText(
                canvas,
                textPaint,
                typeface,
                ACTIVITY_DESCRIPTION,
                rect(CONTENT),
                40.0f,
                53.0f,
                Color.rgb(105, 78, 71));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP
                && CLOSE.contains(designX(event), designY(event))) {
            performClick();
            closeAction.run();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!released) {
            released = true;
            TaizhouTreasureCanvas.recycle(panel, header, title, close);
        }
    }

    private static RectF rect(TaizhouTreasurePotLayout.Node node) {
        return new RectF(node.left(), node.top(), node.right(), node.bottom());
    }
}
