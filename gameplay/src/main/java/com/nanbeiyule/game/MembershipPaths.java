package com.nanbeiyule.game;

import android.graphics.Path;

/** Small path factory used by the restored membership popup drawing. */
final class MembershipPaths {
    private MembershipPaths() {}

    static Path banner() {
        Path path = new Path();
        path.moveTo(1358.0f, 44.0f);
        path.lineTo(1910.0f, 44.0f);
        path.lineTo(1878.0f, 174.0f);
        path.lineTo(1352.0f, 174.0f);
        path.close();
        return path;
    }

    static Path ctaRibbon() {
        Path path = new Path();
        path.moveTo(1445.0f, 796.0f);
        path.lineTo(1908.0f, 796.0f);
        path.lineTo(1876.0f, 874.0f);
        path.lineTo(1412.0f, 874.0f);
        path.close();
        return path;
    }
}
