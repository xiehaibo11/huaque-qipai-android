package com.nanbeiyule.game;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Recovered 30580 box-room handoff to the real WeChat mini program. */
final class ShiSanZhangMiniProgram {
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private static final String APP_ID = "wx5273ca61ed6c3ede";

    private ShiSanZhangMiniProgram() {}

    static String urlFor(String publicPlayerId) {
        String playerId = publicPlayerId == null ? "" : publicPlayerId.trim();
        String query = "sessionFrom=1&gameId=30580&lcc=zjb_7109_boxroom_" + playerId
                + "&lwccss=zjb_7109_boxroom";
        return "weixin://dl/business/?appid=" + APP_ID + "&path=&query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&env_version=release";
    }

    /** @return a user-facing failure message, or {@code null} after WeChat accepts the handoff. */
    static String open(Context context, String publicPlayerId) {
        if (context == null) {
            return "无法打开微信，请稍后重试";
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlFor(publicPlayerId)));
        intent.setPackage(WECHAT_PACKAGE);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return "未安装微信，无法打开边锋十三水";
        }
        try {
            context.startActivity(intent);
            return null;
        } catch (RuntimeException exception) {
            return "微信无法打开边锋十三水，请稍后重试";
        }
    }
}
