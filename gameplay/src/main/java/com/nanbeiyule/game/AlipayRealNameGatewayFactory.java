package com.nanbeiyule.game;

import android.content.Context;
import java.lang.reflect.Constructor;

final class AlipayRealNameGatewayFactory {
    private static final String ALIPAY_GATEWAY_CLASS =
            "com.nanbeiyule.game.AlipayRealNameSdkGateway";

    private AlipayRealNameGatewayFactory() {}

    static AlipayRealNameGateway create(Context context) {
        if (context == null) {
            return new UnavailableAlipayRealNameGateway();
        }
        try {
            Class<?> gatewayClass =
                    Class.forName(ALIPAY_GATEWAY_CLASS);
            if (!AlipayRealNameGateway.class.isAssignableFrom(
                    gatewayClass)) {
                return new UnavailableAlipayRealNameGateway();
            }
            Constructor<?> constructor =
                    gatewayClass.getConstructor(Context.class);
            return (AlipayRealNameGateway)
                    constructor.newInstance(
                            context.getApplicationContext());
        } catch (ReflectiveOperationException
                | LinkageError
                | SecurityException unavailable) {
            return new UnavailableAlipayRealNameGateway();
        }
    }
}
