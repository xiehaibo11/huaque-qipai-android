package com.nanbeiyule.game.wechat;

import android.content.Context;
import android.graphics.Bitmap;
import com.nanbeiyule.game.BuildConfig;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import java.io.ByteArrayOutputStream;

public final class WechatLoginManager implements WechatSubscriptionLauncher {
    interface SdkGateway {
        boolean isInstalled();

        int supportApi();

        boolean send(BaseReq request);

        boolean sendSubscription(WechatSubscriptionIntent intent);

        void detach();
    }

    public enum StartResult {
        STARTED,
        NOT_CONFIGURED,
        NOT_INSTALLED,
        REJECTED
    }

    private final boolean configured;
    private final SdkGateway sdk;
    private final boolean registered;
    private final WechatSubscriptionStore subscriptionStore;

    public WechatLoginManager(Context context) {
        this(context, new SecureWechatSubscriptionStore(context));
    }

    public WechatLoginManager(
            Context context, WechatSubscriptionStore subscriptionStore) {
        configured = !BuildConfig.WECHAT_APP_ID.isBlank();
        IWXAPI candidate = null;
        boolean registrationSucceeded = false;
        if (configured) {
            try {
                candidate =
                        WXAPIFactory.createWXAPI(
                                context.getApplicationContext(),
                                BuildConfig.WECHAT_APP_ID,
                                true);
                registrationSucceeded =
                        candidate.registerApp(BuildConfig.WECHAT_APP_ID);
            } catch (RuntimeException ignored) {
                candidate = null;
                registrationSucceeded = false;
            }
        }
        sdk = candidate == null ? null : new IwxSdkGateway(candidate);
        registered = registrationSucceeded;
        this.subscriptionStore = subscriptionStore;
    }

    WechatLoginManager(
            boolean configured,
            boolean registered,
            SdkGateway sdk,
            WechatSubscriptionStore subscriptionStore) {
        this.configured = configured;
        this.registered = registered;
        this.sdk = sdk;
        this.subscriptionStore = subscriptionStore;
    }

    public StartResult start(String state) {
        if (!configured) {
            return StartResult.NOT_CONFIGURED;
        }
        if (sdk == null || !registered) {
            return StartResult.REJECTED;
        }
        try {
            if (!sdk.isInstalled()) {
                return StartResult.NOT_INSTALLED;
            }
            SendAuth.Req request = new SendAuth.Req();
            request.scope = "snsapi_userinfo";
            request.state = state;
            return sdk.send(request)
                    ? StartResult.STARTED
                    : StartResult.REJECTED;
        } catch (RuntimeException ignored) {
            return StartResult.REJECTED;
        }
    }

    public StartResult shareWebpage(String title, String description, String url) {
        if (!configured) {
            return StartResult.NOT_CONFIGURED;
        }
        if (sdk == null || !registered || url == null || url.isBlank()) {
            return StartResult.REJECTED;
        }
        try {
            if (!sdk.isInstalled()) {
                return StartResult.NOT_INSTALLED;
            }
            WXWebpageObject webpage = new WXWebpageObject();
            webpage.webpageUrl = url;
            WXMediaMessage message = new WXMediaMessage(webpage);
            message.title = title;
            message.description = description;
            SendMessageToWX.Req request = new SendMessageToWX.Req();
            request.transaction = "webpage" + System.currentTimeMillis();
            request.message = message;
            request.scene = SendMessageToWX.Req.WXSceneSession;
            return sdk.send(request) ? StartResult.STARTED : StartResult.REJECTED;
        } catch (RuntimeException ignored) {
            return StartResult.REJECTED;
        }
    }

    /** 发送真实结算画面；完整图片与缩略图都只在本机内存生成。 */
    public StartResult shareImage(Bitmap bitmap) {
        if (!configured) {
            return StartResult.NOT_CONFIGURED;
        }
        if (sdk == null || !registered || bitmap == null || bitmap.isRecycled()) {
            return StartResult.REJECTED;
        }
        try {
            if (!sdk.isInstalled()) {
                return StartResult.NOT_INSTALLED;
            }
            byte[] imageBytes = compressed(bitmap, 85);
            Bitmap thumb = Bitmap.createScaledBitmap(bitmap, 240, 135, true);
            byte[] thumbBytes = compressed(thumb, 70);
            if (thumb != bitmap) {
                thumb.recycle();
            }
            WXImageObject image = new WXImageObject(imageBytes);
            WXMediaMessage message = new WXMediaMessage(image);
            message.thumbData = thumbBytes;
            SendMessageToWX.Req request = new SendMessageToWX.Req();
            request.transaction = "totalResult" + System.currentTimeMillis();
            request.message = message;
            request.scene = SendMessageToWX.Req.WXSceneSession;
            return sdk.send(request) ? StartResult.STARTED : StartResult.REJECTED;
        } catch (RuntimeException ignored) {
            return StartResult.REJECTED;
        }
    }

    private static byte[] compressed(Bitmap bitmap, int quality) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output);
        return output.toByteArray();
    }

    @Override
    public WechatSubscriptionStartResult startSubscription(
            WechatSubscriptionIntent intent) {
        if (!configured) {
            return WechatSubscriptionStartResult.NOT_CONFIGURED;
        }
        long nowMillis = nowMillis();
        if (sdk == null || !registered || intent == null || intent.isExpired(nowMillis)) {
            return WechatSubscriptionStartResult.REJECTED;
        }
        boolean stored = false;
        try {
            if (!sdk.isInstalled()) {
                return WechatSubscriptionStartResult.NOT_INSTALLED;
            }
            if (sdk.supportApi()
                    < com.tencent.mm.opensdk.constants.Build
                            .SUBSCRIBE_MESSAGE_SUPPORTED_SDK_INT) {
                return WechatSubscriptionStartResult.UNSUPPORTED;
            }
            if (!subscriptionStore.saveNew(intent, nowMillis)) {
                return WechatSubscriptionStartResult.ALREADY_PENDING;
            }
            stored = true;
            if (sdk.sendSubscription(intent)) {
                return WechatSubscriptionStartResult.STARTED;
            }
            subscriptionStore.clearSdkFailure(intent.intentId());
            return WechatSubscriptionStartResult.REJECTED;
        } catch (RuntimeException error) {
            if (stored) {
                subscriptionStore.clearSdkFailure(intent.intentId());
            }
            return WechatSubscriptionStartResult.REJECTED;
        }
    }

    private static long nowMillis() {
        return System.currentTimeMillis();
    }

    public void detach() {
        if (sdk != null) {
            sdk.detach();
        }
    }

    private static final class IwxSdkGateway implements SdkGateway {
        private final IWXAPI api;

        private IwxSdkGateway(IWXAPI api) {
            this.api = api;
        }

        @Override
        public boolean isInstalled() {
            return api.isWXAppInstalled();
        }

        @Override
        public int supportApi() {
            return api.getWXAppSupportAPI();
        }

        @Override
        public boolean send(BaseReq request) {
            return api.sendReq(request);
        }

        @Override
        public boolean sendSubscription(WechatSubscriptionIntent intent) {
            SubscribeMessage.Req request = new SubscribeMessage.Req();
            request.transaction = intent.intentId();
            request.templateID = intent.templateId();
            request.scene = intent.scene();
            request.reserved = intent.reserved();
            return api.sendReq(request);
        }

        @Override
        public void detach() {
            api.detach();
        }
    }
}
