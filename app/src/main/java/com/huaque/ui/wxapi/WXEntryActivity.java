package com.huaque.ui.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.nanbeiyule.game.BuildConfig;
import com.nanbeiyule.game.wechat.WechatCallbackContract;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import org.cocos2dx.lua.AppActivity;

public final class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleWechatIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleWechatIntent(intent);
    }

    @Override
    public void onReq(BaseReq request) {
        finish();
    }

    @Override
    public void onResp(BaseResp response) {
        if (response.getType() == ConstantsAPI.COMMAND_SENDAUTH
                && response instanceof SendAuth.Resp authResponse) {
            routeAuth(authResponse);
            return;
        }
        if (response.getType() == ConstantsAPI.COMMAND_SUBSCRIBE_MESSAGE
                && response instanceof SubscribeMessage.Resp subscriptionResponse) {
            routeSubscription(subscriptionResponse);
            return;
        }
        finish();
    }

    private void routeAuth(SendAuth.Resp authResponse) {
        Intent callback =
                new Intent(this, AppActivity.class)
                        .setAction(WechatCallbackContract.ACTION_AUTH_RESPONSE)
                        .putExtra(
                                WechatCallbackContract.EXTRA_ERROR_CODE,
                                authResponse.errCode)
                        .putExtra(WechatCallbackContract.EXTRA_CODE, authResponse.code)
                        .putExtra(WechatCallbackContract.EXTRA_STATE, authResponse.state)
                        .addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(callback);
        finish();
    }

    private void routeSubscription(SubscribeMessage.Resp response) {
        Intent callback =
                new Intent(this, AppActivity.class)
                        .setAction(WechatCallbackContract.ACTION_SUBSCRIPTION_RESPONSE)
                        .putExtra(WechatCallbackContract.EXTRA_ERROR_CODE, response.errCode)
                        .putExtra(WechatCallbackContract.EXTRA_ACTION, response.action)
                        .putExtra(WechatCallbackContract.EXTRA_TEMPLATE_ID, response.templateID)
                        .putExtra(WechatCallbackContract.EXTRA_SCENE, response.scene)
                        .putExtra(WechatCallbackContract.EXTRA_RESERVED, response.reserved)
                        .putExtra(WechatCallbackContract.EXTRA_TRANSACTION, response.transaction)
                        .putExtra(WechatCallbackContract.EXTRA_OPEN_ID, response.openId)
                        .addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(callback);
        finish();
    }

    private void handleWechatIntent(Intent intent) {
        if (BuildConfig.WECHAT_APP_ID.isBlank()) {
            finish();
            return;
        }
        if (api == null) {
            api = WXAPIFactory.createWXAPI(this, BuildConfig.WECHAT_APP_ID, true);
        }
        if (!api.handleIntent(intent, this)) {
            finish();
        }
    }
}
