package com.nanbeiyule.game;

import android.content.Context;
import java.lang.reflect.Constructor;

final class OneTapGatewayFactory {
    private static final String ALIYUN_GATEWAY_CLASS =
            "com.nanbeiyule.game.AliyunOneTapLoginGateway";

    private OneTapGatewayFactory() {}

    static OneTapLoginGateway create(Context context) {
        if (context == null) {
            return new UnavailableOneTapLoginGateway();
        }
        try {
            Class<?> gatewayClass =
                    Class.forName(ALIYUN_GATEWAY_CLASS);
            if (!OneTapLoginGateway.class.isAssignableFrom(
                    gatewayClass)) {
                return new UnavailableOneTapLoginGateway();
            }
            Constructor<?> constructor =
                    gatewayClass.getConstructor(Context.class);
            return (OneTapLoginGateway)
                    constructor.newInstance(
                            context.getApplicationContext());
        } catch (ReflectiveOperationException
                | LinkageError
                | SecurityException unavailable) {
            return new UnavailableOneTapLoginGateway();
        }
    }
}
