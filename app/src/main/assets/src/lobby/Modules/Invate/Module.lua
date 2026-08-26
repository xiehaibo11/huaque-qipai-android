local InvateModule = class("InvateModule", XH.ModuleBase)

InvateModule.EVENT_CHANGE_BIND_SUCCESS = "EVENT_CHANGE_BIND_SUCCESS"
InvateModule.EVENT_CHANGE_BIND_PURVIEW = "EVENT_CHANGE_BIND_PURVIEW"
InvateModule.EVENT_CHANGE_BIND_REQ = "EVENT_CHANGE_BIND_REQ"
InvateModule.EVENT_CHECK_WEIXININFO = "EVENT_CHECK_WEIXININFO"
InvateModule.EVENT_CHECK_INVATE_CODE = "EVENT_CHECK_INVATE_CODE"
InvateModule.EVENT_CHANGE_BIND_STATE = "EVENT_CHANGE_BIND_STATE"
InvateModule.EVENT_INVATE_UNBIND_SUCCESS = "EVENT_INVATE_UNBIND_SUCCESS"
InvateModule.EVENT_INVATE_INFO_SUCCESS = "EVENT_INVATE_INFO_SUCCESS"
InvateModule.EVENT_MY_INVATE_CODE_SUCCESS = "EVENT_MY_INVATE_CODE_SUCCESS"

local INVATE_TYPE_CHECK = 1

function InvateModule:ctor()
    InvateModule.super.ctor(self)
    self._code = nil
end

function InvateModule:getReqConfig()
    return {
        ReqBind = { reqPath = "lobby.Req.ThreeLevelDistribution.reqBind", callBack = self.onInvateCallback },
        ReqChangeBindState = { reqPath = "lobby.Req.ThreeLevelDistribution.reqChangeBindState", callBack = self.onChangeBindStateCallback },
        ReqCheckInvatWeiXin = { reqPath = "lobby.Req.ThreeLevelDistribution.reqCheckInvateCode", callBack = self.onCheckTWeiXinInfoCallback },
        ReqCheckInvatCode = { reqPath = "lobby.Req.ThreeLevelDistribution.reqCheckInvateCode", callBack = self.onCheckInvateCodeCallback },
        ReqCheckChangeBindPurview = { reqPath = "lobby.Req.ThreeLevelDistribution.reqChangeBindPurview", callBack = self.onChangeBindPurviewCallback },
        ReqChangeBindPurview = { reqPath = "lobby.Req.ThreeLevelDistribution.reqChangeBind", callBack = self.onChangeBindCallback },
        ReqPhoneVerifyCode = { reqPath = "lobby.Req.PhoneVerifyCode.ReqPhoneVerifyCode", callBack = self.onReqCodeCallback },
        ReqChangeBindAgent = { reqPath = "lobby.Req.ThreeLevelDistribution.reqChangeBind", callBack = self.onChangeBindCallback },
        ReqUnBind = { reqPath = "lobby.Req.ThreeLevelDistribution.reqUnBind", callBack = self.onInvateUnBindCallback },
        ReqGetCode = { reqPath = "lobby.Req.ThreeLevelDistribution.reqGetCode", callBack = self.onMyInviteCodeCallback },
    }
end

------InvateView
function InvateModule:setCodeValue(code)
    self._code = code
end

function InvateModule:getCodeValue(code)
    return self._code
end

function InvateModule:createReqChangeBindInfo()
    local tHttpInfo = {}
    tHttpInfo["rid"] = XH.areaData:getApiHubRid()
    tHttpInfo["numid"] = XH.playerData:getNumberID()
    return tHttpInfo
end

function InvateModule:reqBind(code)
    XH.TipTool.showLoading()
    local tHttpInfo = self:createHttpInfo(code)
    tHttpInfo["numid"] = XH.playerData:getNumberID()
    self:startReq("ReqBind", tHttpInfo, 15)
end

function InvateModule:onInvateCallback(cell, type, flag)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if flag == 0 then
            self:dispatchEvent({name = InvateModule.EVENT_INVATE_INFO_SUCCESS, data = flag})
        end
    end
end

function InvateModule:onInvateUnBindCallback(cell, type, flag)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = InvateModule.EVENT_INVATE_UNBIND_SUCCESS, data = flag})
    end
end

function InvateModule:onMyInviteCodeCallback(cell, type, flag)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = InvateModule.EVENT_MY_INVATE_CODE_SUCCESS, data = flag})
    end
end

function InvateModule:reqChangeBindState()
    local tHttpInfo = {}
    tHttpInfo["rid"] = XH.areaData:getApiHubRid()
    tHttpInfo["numid"] = XH.playerData:getNumberID()
    self:startReq("ReqChangeBindState", tHttpInfo, 15)
end

function InvateModule:reqUnBind()
    local tHttpInfo = {}
    tHttpInfo["rid"] = XH.areaData:getApiHubRid()
    tHttpInfo["numid"] = XH.playerData:getNumberID()
    self:startReq("ReqUnBind", tHttpInfo, 15)
end

function InvateModule:reqMyInviteCode(code)
    local tHttpInfo = {}
    tHttpInfo["rid"] = XH.areaData:getApiHubRid()
    tHttpInfo["numid"] = XH.playerData:getNumberID()
    self:startReq("ReqGetCode", tHttpInfo, 15)
end

function InvateModule:onChangeBindStateCallback(req, type, data)
    if type == XH.Req.TYPE.SUCCESS then
        if data.status == 0 then
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
            },"您的申请换绑请求还在处理中，请等待处理结果")
        elseif data.status == 1 or data.status == 3 then
            self:dispatchEvent({name = InvateModule.EVENT_CHANGE_BIND_STATE, data = data})
            XH.userDefault:setValue(XH.userDefault.KEY_ID.NEED_REQ_CHANGE_BIND_RESULT,false)
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
            },"申请换绑成功")
        elseif data.status == 2 then
            XH.userDefault:setValue(XH.userDefault.KEY_ID.NEED_REQ_CHANGE_BIND_RESULT,false)
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
            },"您的申请被拒绝")
        end
    end
end

function InvateModule:createHttpInfo(code)
    local tHttpInfo = {}
    tHttpInfo["rid"] = XH.areaData:getApiHubRid()
    tHttpInfo["code"] = tonumber(code)
    return tHttpInfo
end

function InvateModule:reqCheckInvateCode(code,type)
    XH.TipTool.showLoading()
    local tHttpInfo = self:createHttpInfo(code)
    if type == INVATE_TYPE_CHECK then
        self:startReq("ReqCheckInvatWeiXin", tHttpInfo, 15)
    else
        self:startReq("ReqCheckInvatCode", tHttpInfo, 15)
    end
end

function InvateModule:onCheckTWeiXinInfoCallback(req, type, flag)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = InvateModule.EVENT_CHECK_WEIXININFO, data = flag})
    else
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, req:getMessage())
    end
end

function InvateModule:onCheckInvateCodeCallback(req, type, flag)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({name = InvateModule.EVENT_CHECK_INVATE_CODE, data = flag})
    else
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, req:getMessage())
    end
end

------ChangeView
function InvateModule:reqCheckChangeBindPurview()
    XH.TipTool.showLoading()
    local tHttpInfo = self:createReqChangeBindInfo()
    self:startReq("ReqCheckChangeBindPurview", tHttpInfo, 15)
end

function InvateModule:onChangeBindPurviewCallback(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if data.errorCode == 0 then
            if data.response.is_player_active + data.response.is_agent_active < 2 then
                local getState = true
                self:dispatchEvent({name = InvateModule.EVENT_CHANGE_BIND_PURVIEW, data = getState})
            end
        end
    end
end

function InvateModule:reqChangeBindState(tHttpInfo)
    self:startReq("ReqChangeBindPurview", tHttpInfo, 15)
end

function InvateModule:onChangeBindCallback(cell, type, data)
    XH.TipTool.hideLoading()
    local getInfo = {}
    getInfo.nType = type
    getInfo.nData = data
    self:dispatchEvent({name = InvateModule.EVENT_CHANGE_BIND_REQ, data = getInfo})
end

------ChangeRecordPhoneView
function InvateModule:reqVerifyCode(strPhone)
    if strPhone == nil then
        return
    end
    self:startReq("ReqPhoneVerifyCode", strPhone, XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function InvateModule:onReqCodeCallback(cell, type, data)
    if type == XH.Req.TYPE.FAIL then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, cell:getMessage())
    elseif type == XH.Req.TYPE.TIMEOUT then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, XH.ConstString.getStr("PHONE_CODE_ERROR"))
    end
end

function InvateModule:reqChangeBindAgent(phoneNum, verifyCode)
    local tHttpInfo = {}
    tHttpInfo["rid"] = XH.areaData:getApiHubRid()
    tHttpInfo["numid"] = XH.playerData:getNumberID()
    local getNcode = XH.userDefault:getValue(XH.userDefault.KEY_ID.CHANGE_INVATE_CODE, 0)
    self:setCodeValue(getNcode)
    tHttpInfo["code"] = self._code
    tHttpInfo["reason"] = 1
    tHttpInfo["phone"] = phoneNum
    tHttpInfo["verify_code"] = verifyCode
    tHttpInfo["channel_id"] = 20001
    tHttpInfo["is_register"] = 0

    self:startReq("ReqChangeBindAgent", tHttpInfo,15)
    
    XH.TipTool.showTip({
        type = XH.TIP_LAYER_TYPE.OK,
    }, "您已提交申请，我们会尽快帮您处理！")
    self:close()
end

function InvateModule:onChangeBindCallback(cell, type, data)
    XH.TipTool.hideLoading()
    if type == XH.Req.TYPE.SUCCESS then
        if data.errorCode == 0 then
            if data.response.status == 1 then
                XH.TipTool.showTip({
                    type = XH.TIP_LAYER_TYPE.OK,
                }, "换绑成功！")
                self:dispatchEvent({name = InvateModule.EVENT_CHANGE_BIND_SUCCESS, data = self._code})
            else
                XH.TipTool.showTip({
                    type = XH.TIP_LAYER_TYPE.OK,
                }, "您的申请已提交，请等待处理！")
                XH.userDefault:setValue(XH.userDefault.KEY_ID.NEED_REQ_CHANGE_BIND_RESULT,true)
            end
        elseif data.errorCode == 1075 then
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
            }, "您已经提交过申请，请等待处理结果！")
        elseif data.errorCode == 1059 then
            XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
            }, "输入的邀请码与原先绑定的一致！")
        else
            if data.response and data.response ~= "" then
                XH.TipTool.showTip({
                type = XH.TIP_LAYER_TYPE.OK,
                }, data.response)
            end
        end
    end
end

return InvateModule �'  