package com.nanbeiyule.game;

import android.graphics.Bitmap;
import android.widget.Toast;
import com.nanbeiyule.game.wechat.WechatLoginManager;

/** 大结算截图的微信分享边界。 */
final class TaizhouTotalResultShareFlow {
    private TaizhouTotalResultShareFlow() {}

    static void share(MainActivityGameHomeDisplayFlow owner, TaizhouMahjongTableView tableView) {
        if (tableView == null) {
            return;
        }
        Bitmap bitmap = tableView.totalResultShareBitmap();
        if (bitmap == null) {
            return;
        }
        WechatLoginManager.StartResult result =
                owner.wechatLoginManager == null
                        ? WechatLoginManager.StartResult.REJECTED
                        : owner.wechatLoginManager.shareImage(bitmap);
        bitmap.recycle();
        if (result != WechatLoginManager.StartResult.STARTED) {
            Toast.makeText(owner, "微信分享不可用，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }
}
