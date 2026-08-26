package com.nanbeiyule.game.wechat;

import com.tencent.mm.opensdk.modelbase.BaseResp;

public record WechatAuthResponse(Status status, String code, String state) {
    public enum Status {
        SUCCESS,
        CANCELLED,
        DENIED,
        FAILED
    }

    public static WechatAuthResponse from(
            int errorCode, String code, String state) {
        Status status;
        if (errorCode == BaseResp.ErrCode.ERR_OK
                && code != null
                && !code.isBlank()) {
            status = Status.SUCCESS;
        } else if (errorCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
            status = Status.CANCELLED;
        } else if (errorCode == BaseResp.ErrCode.ERR_AUTH_DENIED) {
            status = Status.DENIED;
        } else {
            status = Status.FAILED;
        }
        return new WechatAuthResponse(status, code, state);
    }
}
