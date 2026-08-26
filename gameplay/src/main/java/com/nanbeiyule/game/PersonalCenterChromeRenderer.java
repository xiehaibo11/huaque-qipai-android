package com.nanbeiyule.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import java.util.EnumMap;
import java.util.List;

/** Dark-navy, champagne-gold personal center backed by authenticated first-party state. */

abstract class PersonalCenterChromeRenderer extends PersonalCenterSettingsPageRenderer {
    protected PersonalCenterChromeRenderer(
            Context context,
            PersonalCenterState state,
            PersonalCenterSystemSettings systemSettings,
            Bitmap avatarBitmap,
            Listener listener) {
        super(context, state, systemSettings, avatarBitmap, listener);
    }
    protected void drawCreamContent(Canvas canvas) {
        RectF panel = rect(layout.panel());
        fillPaint.setShader(null);
        fillPaint.setColor(CREAM);
        canvas.drawRoundRect(panel, 18.0f, 18.0f, fillPaint);

        RectF content =
                new RectF(
                        layout.sidebar().right(),
                        layout.titleBar().bottom(),
                        layout.panel().right(),
                        layout.panel().bottom());
        fillPaint.setShader(
                new LinearGradient(
                        content.left,
                        content.top,
                        content.right,
                        content.bottom,
                        new int[] {
                            Color.rgb(255, 253, 248),
                            Color.rgb(255, 247, 235),
                            Color.rgb(250, 240, 223)
                        },
                        null,
                        Shader.TileMode.CLAMP));
        canvas.drawRect(content, fillPaint);
        fillPaint.setShader(null);
    }

    protected void drawChampagneTopBar(Canvas canvas) {
        RectF titleBar = rect(layout.titleBar());
        fillPaint.setShader(
                new LinearGradient(
                        titleBar.left,
                        titleBar.top,
                        titleBar.left,
                        titleBar.bottom,
                        new int[] {
                            CHAMPAGNE_LIGHT,
                            Color.rgb(240, 202, 132),
                            CHAMPAGNE
                        },
                        new float[] {0.0f, 0.48f, 1.0f},
                        Shader.TileMode.CLAMP));
        canvas.drawRoundRect(titleBar, 18.0f, 18.0f, fillPaint);
        canvas.drawRect(
                titleBar.left,
                titleBar.centerY(),
                titleBar.right,
                titleBar.bottom,
                fillPaint);
        fillPaint.setShader(null);
        fillPaint.setColor(Color.argb(95, 255, 255, 255));
        canvas.drawRect(
                titleBar.left + 6.0f,
                titleBar.top + 5.0f,
                titleBar.right - 6.0f,
                titleBar.top + 9.0f,
                fillPaint);
        strokePaint.setColor(Color.argb(180, 138, 83, 24));
        strokePaint.setStrokeWidth(1.5f);
        canvas.drawLine(
                titleBar.left,
                titleBar.bottom,
                titleBar.right,
                titleBar.bottom,
                strokePaint);
    }

    protected void drawDarkNavySidebar(Canvas canvas) {
        RectF sidebar = rect(layout.sidebar());
        fillPaint.setAlpha(255);
        fillPaint.setShader(
                new LinearGradient(
                        sidebar.left,
                        sidebar.top,
                        sidebar.right,
                        sidebar.bottom,
                        new int[] {
                            NAVY_TOP,
                            Color.rgb(24, 39, 53),
                            NAVY_BOTTOM
                        },
                        null,
                        Shader.TileMode.CLAMP));
        canvas.drawRoundRect(sidebar, 18.0f, 18.0f, fillPaint);
        canvas.drawRect(
                sidebar.centerX(),
                sidebar.top,
                sidebar.right,
                sidebar.bottom,
                fillPaint);
        fillPaint.setShader(null);

        fillPaint.setColor(Color.argb(38, 255, 255, 255));
        canvas.drawRect(
                sidebar.left,
                sidebar.top,
                sidebar.right,
                sidebar.top + 3.0f,
                fillPaint);
        strokePaint.setStrokeWidth(1.5f);
        strokePaint.setColor(Color.argb(170, 216, 164, 81));
        canvas.drawLine(
                sidebar.right,
                sidebar.top,
                sidebar.right,
                sidebar.bottom,
                strokePaint);
        fillPaint.setAlpha(255);
        drawSidebarLandscape(canvas);
    }

    protected void drawFrameBorder(Canvas canvas) {
        RectF panel = rect(layout.panel());
        strokePaint.setStrokeWidth(2.0f);
        strokePaint.setColor(Color.rgb(186, 127, 50));
        canvas.drawRoundRect(panel, 18.0f, 18.0f, strokePaint);
        RectF inner = new RectF(panel);
        inner.inset(4.0f, 4.0f);
        strokePaint.setStrokeWidth(1.0f);
        strokePaint.setColor(Color.argb(150, 255, 231, 175));
        canvas.drawRoundRect(inner, 14.0f, 14.0f, strokePaint);
    }

    protected void drawTitle(Canvas canvas) {
        float centerY = layout.titleBar().centerY();
        reusablePath.reset();
        reusablePath.moveTo(43.0f, centerY);
        reusablePath.lineTo(52.0f, centerY - 9.0f);
        reusablePath.lineTo(61.0f, centerY);
        reusablePath.lineTo(52.0f, centerY + 9.0f);
        reusablePath.close();
        strokePaint.setStrokeWidth(2.0f);
        strokePaint.setColor(CHAMPAGNE_LIGHT);
        canvas.drawPath(reusablePath, strokePaint);
        fillPaint.setColor(GOLD);
        canvas.drawCircle(52.0f, centerY, 2.8f, fillPaint);

        setText(
                39.0f,
                Color.rgb(255, 237, 197),
                true,
                Paint.Align.LEFT);
        drawBaselineCenteredText(
                canvas, "个人中心", 76.0f, centerY);
        drawClose(canvas);
    }

    protected void drawTabs(Canvas canvas) {
        List<PersonalCenterLayout.Box> tabs = layout.tabHits();
        for (int index = 0; index < tabs.size(); index++) {
            RectF tab = rect(tabs.get(index));
            boolean selected = selectedTab == index;
            if (selected) {
                reusablePath.reset();
                reusablePath.moveTo(tab.left, tab.top + 6.0f);
                reusablePath.lineTo(tab.right - 26.0f, tab.top + 6.0f);
                reusablePath.lineTo(tab.right, tab.centerY());
                reusablePath.lineTo(tab.right - 26.0f, tab.bottom - 6.0f);
                reusablePath.lineTo(tab.left, tab.bottom - 6.0f);
                reusablePath.close();
                fillPaint.setShader(
                        new LinearGradient(
                                tab.left,
                                tab.top,
                                tab.right,
                                tab.bottom,
                                new int[] {
                                    Color.argb(225, 121, 74, 19),
                                    Color.rgb(237, 189, 92),
                                    Color.argb(225, 106, 62, 13)
                                },
                                null,
                                Shader.TileMode.CLAMP));
                canvas.drawPath(reusablePath, fillPaint);
                fillPaint.setShader(null);
                strokePaint.setStrokeWidth(1.5f);
                strokePaint.setColor(Color.rgb(255, 225, 153));
                canvas.drawPath(reusablePath, strokePaint);
            } else {
                strokePaint.setStrokeWidth(1.0f);
                strokePaint.setColor(Color.argb(70, 220, 178, 103));
                canvas.drawLine(
                        tab.left + 20.0f,
                        tab.bottom,
                        tab.right - 20.0f,
                        tab.bottom,
                        strokePaint);
            }
            int color =
                    selected
                            ? Color.rgb(255, 248, 220)
                            : Color.rgb(237, 204, 136);
            drawMenuAssetIcon(
                    canvas,
                    menuIcon(index),
                    tab.left + 55.0f,
                    tab.centerY(),
                    32.0f,
                    color,
                    selected);
            setText(
                    31.0f,
                    color,
                    selected,
                    Paint.Align.LEFT);
            drawBaselineCenteredText(
                    canvas,
                    TAB_TITLES[index],
                    tab.left + 94.0f,
                    tab.centerY());
            if (selected) {
                strokePaint.setStrokeWidth(3.0f);
                strokePaint.setStrokeCap(Paint.Cap.ROUND);
                strokePaint.setColor(Color.rgb(255, 244, 207));
                canvas.drawLine(
                        tab.right - 40.0f,
                        tab.centerY() - 10.0f,
                        tab.right - 29.0f,
                        tab.centerY(),
                        strokePaint);
                canvas.drawLine(
                        tab.right - 29.0f,
                        tab.centerY(),
                        tab.right - 40.0f,
                        tab.centerY() + 10.0f,
                        strokePaint);
                strokePaint.setStrokeCap(Paint.Cap.BUTT);
            }
        }
    }
}
