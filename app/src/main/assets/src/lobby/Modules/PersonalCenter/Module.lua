local PersonalCenterModule = class("PersonalCenterModule", XH.ModuleBase)

PersonalCenterModule.EVENT_PPCMGR_LOCATION_CHANGED = "EVENT_PPCMGR_LOCATION_CHANGED"
PersonalCenterModule.EVENT_FAKE_BIND_CHANGED = "EVENT_FAKE_BIND_CHANGED"

PersonalCenterModule.EVENT_VIP_LAYER_VIP_CONFIG = "EVENT_VIP_LAYER_VIP_CONFIG"
PersonalCenterModule.EVENT_VIP_LAYER_VIP_LOG_INFO = "EVENT_VIP_LAYER_VIP_LOG_INFO"
PersonalCenterModule.EVENT_VIP_LAYER_VIP_INFO = "EVENT_VIP_LAYER_VIP_INFO"
PersonalCenterModule.EVENT_VIP_LAYER_PASSWD_STATE = "EVENT_VIP_LAYER_PASSWD_STATE"
PersonalCenterModule.EVENT_VIP_LAYER_SET_NEED_PASSWD = "EVENT_VIP_LAYER_SET_NEED_PASSWD"
PersonalCenterModule.EVENT_VIP_LAYER_SET_NEED_SURE = "EVENT_VIP_LAYER_SET_NEED_SURE"
PersonalCenterModule.EVENT_VIP_LAYER_TRANS_CARD = "EVENT_VIP_LAYER_TRANS_CARD"
PersonalCenterModule.EVENT_VIP_LAYER_CHECK_USERID = "EVENT_VIP_LAYER_CHECK_USERID"
PersonalCenterModule.EVENT_VIP_LAYER_GET_AWARD = "EVENT_VIP_LAYER_GET_AWARD"
PersonalCenterModule.EVENT_VIP_LAYER_CHANGE_PWD = "EVENT_VIP_LAYER_CHANGE_PWD"
PersonalCenterModule.EVENT_VIP_LAYER_SHOW_TAB_LAYER = "EVENT_VIP_LAYER_SHOW_TAB_LAYER"
PersonalCenterModule.EVENT_GIVE_LAYER_SHOW_TAB_LAYER = "EVENT_GIVE_LAYER_SHOW_TAB_LAYER"
PersonalCenterModule.EVENT_VIP_LAYER_ROLL_BACK_CARD = "EVENT_VIP_LAYER_ROLL_BACK_CARD"

local PersonalCenterCinfig = require("lobby.Modules.PersonalCenter.Config")
local TeaHouseProtobuf = require("app.Protobuf.TeaHouseProtobuf")

function PersonalCenterModule:ctor()
    PersonalCenterModule.super.ctor(self)

    self._locationInfo = nil
    self._isGettingLocation = false
    self._longitude = -1
    self._latitude = -1
end

function PersonalCenterModule:getProxyEvents()
    return {
        { module = XH.sdkManager, eventKeyName = "EVENT_AMAP_CALLBACK", callBack = "onGetLocationCallBack" },
        { module = XH.login:getModule("Login"), eventKeyName = "LOGIN_EVENT_SUCCESS", callBack = "onLoginSuccessCallBack" },
    }
end

function PersonalCenterModule:getReqConfig()
    return {
        ReqUnbindUser = { reqPath = "app.Req.PlayerMessage.ReqUnbindUser" },
        ReqVipLogInfo = { reqPath = "lobby.Req.PersonalCenter.ReqVIPTransCardLogs", callBack = self.resqVipLogInfoCallBack },
        ReqVipConfig = { reqPath = "lobby.Req.PersonalCenter.ReqVIPConfig", callBack = self.resqVipConfigCallBack },
        ReqVipInfo = { reqPath = "lobby.Req.PersonalCenter.ReqGetVIP", callBack = self.resqVipInfoCallBack },
        ReqVIPChangePasswd = { reqPath = "lobby.Req.PersonalCenter.ReqVIPChangePasswd", callBack = self.resqVIPChangePasswdCallBack },
        ReqVIPGetAward = { reqPath = "lobby.Req.PersonalCenter.ReqVIPGetAward", callBack = self.resqVIPGetAwardCallBack },
        ReqVIPGetPasswdState = { reqPath = "lobby.Req.PersonalCenter.ReqVIPGetPasswdState", callBack = self.resqVIPGetPasswdStateCallBack },
        ReqVIPResetPasswd = { reqPath = "lobby.Req.PersonalCenter.ReqVIPResetPasswd", callBack = self.resqVIPResetPasswdCallBack },
        ReqVIPSetNeedPasswd = { reqPath = "lobby.Req.PersonalCenter.ReqVIPSetNeedPasswd", callBack = self.resqVIPSetNeedPasswdCallBack },
        ReqVIPSetNeedSure = { reqPath = "lobby.Req.PersonalCenter.ReqVIPSetNeedSure", callBack = self.resqVIPSetNeedSureCallBack },
        ReqVIPTransCard = { reqPath = "lobby.Req.PersonalCenter.ReqVIPTransCard", callBack = self.resqVIPTransCardCallBack },
        ReqCheckUserId = { reqPath = "teahouse.Req.NetReqOtherUserInfo", callBack = self.resqCheckUserIdCallBack },
        ReqVIPRollbackTransCard = { reqPath = "lobby.Req.PersonalCenter.ReqVIPRollbackTransCard", callBack = self.resqVIPRollbackTransCardCallBack },
        ReqUpdatePlayerPropCount = { reqPath = "lobby.Req.PersonalCenter.ReqUpdatePlayerPropCount", callBack = self.resqUpdatePlayerPropCountCallBack },
    }
end

function PersonalCenterModule:onGetLocationCallBack(event)
    local code = event.data.code
    local msg = event.data.msg
    if code == XH.ThirdDefine.AMAP_CALLBACK_CODE.AMAP_SUCCESS then
        local tData = msg
        if true or device.platform == "ios" then 
            local ok = pcall(function()
                tData = cjson.decode(msg)
            end)

            if not ok then
                return
            end
        end

        local sCountry = tData["country"] or ""
        local sProvince = tData["province"] or ""
        local sCity = tData["city"] or ""
        local sDistrict = tData["district"] or ""
        local sStreet = tData["street"] or ""

        self._longitude = tData["longitude"]
        self._latitude = tData["latitude"]
        self._locationInfo = sCountry .. sProvince .. sCity .. sDistrict .. sStreet
    else
        self._locationInfo = nil
    end

    self._isGettingLocation = false
    self:dispatchEvent({name = PersonalCenterModule.EVENT_PPCMGR_LOCATION_CHANGED, data = self._locationInfo})
end

--各区特有参数需要重置
function PersonalCenterModule:onLoginSuccessCallBack(event)
	self._fakeIdentify = false
    --self:UpdatePlayerPropCount()
    self:getVipSysteamData()
end

function PersonalCenterModule:getLocationInfo()
    return self._locationInfo
end

--是否为伪实名认证，只有在控制器模式下才生效
function PersonalCenterModule:getFakeIdentify()
    return self._fakeIdentify or false
end

function PersonalCenterModule:setFakeIdentify(fakeIdentify)
    self._fakeIdentify = fakeIdentify
    self:dispatchEvent({name = PersonalCenterModule.EVENT_FAKE_BIND_CHANGED})
end

--请求标签信息
function PersonalCenterModule:reqBindID(id)
    local reqBindID = XH.BoxDataProtocol.ReqBindID:new()
    reqBindID.askid = 0
    reqBindID.id = id
    local SRSGroupID = XH.areaData:getSrsGroupID()
    XH.netEngine:addProtocolScriptFuncByObj(self, self.respBindID, XH.BoxDataProtocol.ReqBindID.processid, SRSGroupID)
    XH.netEngine:sendProtocol(reqBindID, XH.BoxDataProtocol.ReqBindID.processid,0,SRSGroupID)
end

function PersonalCenterModule:respBindID(XYID, buff, len, processid)
    if processid ~= XH.BoxDataProtocol.RespBindID.processid then
        return
    end

    if XYID ~=  XH.BoxDataProtocol.RespBindID.XY_ID then
        return
    end
    
    local respBindID =  XH.BoxDataProtocol.RespBindID:new()
    respBindID:bistream(buff, len)
    
    if respBindID.flag == 0 then
        self:setFakeIdentify(true)
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "绑定成功")
    else
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "绑定失败")
    end
end

function PersonalCenterModule:reqUnbindUser()
    self:startReq("ReqUnbindUser", XH.areaData:getSrsGroupID(), XH.KW_CONFIG_LOGIC_TIME_OUT)
end

function PersonalCenterModule:getVipSysteamData()
    self:initAllVipData()
    self:reqVipConfig()
    self:reqVipInfo()
end

function PersonalCenterModule:initAllVipData()
    self._vipLogInfo = nil
    self._vipConfig = nil
    self._vipInfo = nil
end

function PersonalCenterModule:getVipConfig(isNew)
    if not self._vipConfig and isNew then
        self:reqVipConfig()
    else
        return self._vipConfig
    end
    return
end

local KW_VIP_NAME_CONFIG = {
    [0] = {nameColor =  cc.c3b(9,160,112),  name = "普通会员"},
    [1] = {nameColor =  cc.c3b(59,100,193), name = "中级会员"},
    [2] = {nameColor =  cc.c3b(205,68,68),  name = "高级会员"},
    [3] = {nameColor =  cc.c3b(196,93,43),  name = "尊享会员"},
}

function PersonalCenterModule:addVipData()
    if #self._vipConfig.vip_config == 3 then
        table.insert(self._vipConfig.vip_config,{vip_id=#self._vipConfig.vip_config,name="尊贵会员",condition_desc="敬请期待",can_trans_prop=false,is_open=false,have_award=false})
    end
    for _, vip in pairs(self._vipConfig.vip_config) do
        vip.nameColor = KW_VIP_NAME_CONFIG[vip.vip_id].nameColor
    end
    self._vipConfig.vipTime = "会员系统有效时间为：" .. os.date("%Y.%m.%d",self._vipConfig.start_time) .. " ~ " .. os.date("%Y.%m.%d",self._vipConfig.end_time)
    if  tonumber( os.date("%Y",self._vipConfig.end_time)) - tonumber( os.date("%Y",self._vipConfig.start_time)) >= 5 then
        self._vipConfig.vipTime = ""
    end
    return self._vipConfig
end

function PersonalCenterModule:reqVipConfig()
    self:startReq("ReqVipConfig", 10)
end

function PersonalCenterModule:resqVipConfigCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        self._vipConfig = vData.nXYData
        self:addVipData()        
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_VIP_CONFIG, data = self._vipConfig })
    end
end

function PersonalCenterModule:getVipLogInfo(page, page_size, needNew)
    if not self._vipLogInfo or needNew == true then
        self:reqVipLogInfo(page, page_size)
    else
        return self._vipLogInfo
    end
end

function PersonalCenterModule:reqVipLogInfo(page, page_size)
    self:startReq("ReqVipLogInfo", 10, page, page_size)
end

function PersonalCenterModule:resqVipLogInfoCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        self._vipLogInfo = vData.nXYData
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_VIP_LOG_INFO, data = self._vipLogInfo })
    else
        local errorText
        if vData and vData.nXYData.resp and TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code] then
             errorText = TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code]
             XH.TipTool.showToast( errorText, 2)
        end
    end
end


function PersonalCenterModule:getVipInfo(isNew)
    if not self._vipInfo and isNew then
        self:reqVipInfo()
    else
        return self._vipInfo
    end
    return
end

function PersonalCenterModule:reqVipInfo()
    self:startReq("ReqVipInfo", 10)
end

function PersonalCenterModule:resqVipInfoCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        self._vipInfo = vData.nXYData
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_VIP_INFO, data = self._vipInfo })
    end
end

function PersonalCenterModule:reqVIPChangePasswd(oldPwd, newPwd)
    self:startReq("ReqVIPChangePasswd", 10, oldPwd, newPwd)
end

function PersonalCenterModule:resqVIPChangePasswdCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        },"密码修改成功")
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_CHANGE_PWD, data = vData })
    else
        local errorText
        if vData and vData.nXYData.resp and TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code] then
             errorText = TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code]
        end
        XH.TipTool.showToast( errorText or "密码修改失败", 2)
    end
end

function PersonalCenterModule:reqVIPGetAward()
    self:startReq("ReqVIPGetAward", 10)
end

function PersonalCenterModule:resqVIPGetAwardCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        local data = {}
        for _,award in pairs(vData.nXYData.awardInfo) do
            local temp = {}
            local propInfo = XH.lobby:getModule("BagSysNew"):getPropInfo(award.prop_id) or {}
            temp.desc = ( propInfo.propsname or "" ) .. "*" .. award.prop_cnt
            temp.url = award.award_url
            table.insert(data,temp)
        end
        if #data > 0 then
            XH.viewManager:openView("CommonRewardView", nil, data, false)
            self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_GET_AWARD })
        end
    else
        local errorText
        if vData and vData.nXYData.resp and TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code] then
             errorText = TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code]
        end
        XH.TipTool.showToast( errorText or "奖励领取失败", 2)
    end
end

function PersonalCenterModule:reqVIPGetPasswdState()
    self:startReq("ReqVIPGetPasswdState", 10)
end

function PersonalCenterModule:resqVIPGetPasswdStateCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_PASSWD_STATE, data = vData.nXYData })
    end
end

function PersonalCenterModule:reqVIPResetPasswd()
    self:startReq("ReqVIPResetPasswd", 10)
end

function PersonalCenterModule:resqVIPResetPasswdCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        },"短信已发放，请注意查收~")
        self:reqVIPGetPasswdState()
    else
        local errorText
        if vData and vData.nXYData.resp and TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code] then
             errorText = TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code]
        end
        XH.TipTool.showToast( errorText or "密码重置失败", 2)
    end
end

function PersonalCenterModule:reqVIPSetNeedPasswd(pwd, needPwd)
    self:startReq("ReqVIPSetNeedPasswd", 10, pwd, needPwd)
end

function PersonalCenterModule:resqVIPSetNeedPasswdCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_SET_NEED_PASSWD, data = vData.nXYData })
    else
        local errorText
        if vData and vData.nXYData.resp and TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code] then
             errorText = TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code]
        end
        XH.TipTool.showToast( errorText or "密码错误请重试", 2)
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_SET_NEED_PASSWD})
    end
end

function PersonalCenterModule:reqVIPSetNeedSure(needSure)
    self:startReq("ReqVIPSetNeedSure", 10, needSure)
end

function PersonalCenterModule:resqVIPSetNeedSureCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_SET_NEED_SURE, data = vData })
    else
        local errorText
        if vData and vData.nXYData.resp and TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code] then
             errorText = TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code]
        end
        XH.TipTool.showToast(errorText or "设置失败", 2)
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_SET_NEED_SURE })
    end
end

function PersonalCenterModule:reqVIPTransCard(userID, count, passwd)
    self:startReq("ReqVIPTransCard", 10, tonumber(userID), tonumber(count), tostring(passwd))
end

function PersonalCenterModule:resqVIPTransCardCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        XH.TipTool.showToast("赠送成功！", 2)
        XH.playerData:flushPlayerDrop()
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_TRANS_CARD, data = vData })
    else
        local errorText
        if vData and vData.nXYData.resp and TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code] then
             errorText = TeaHouseProtobuf.errorCode[vData.nXYData.resp.err_code]
        end
        XH.TipTool.showToast( errorText or "赠送失败", 2)
    end
end

function PersonalCenterModule:reqCheckUserId(uid)
    self:startReq("ReqCheckUserId", tonumber(uid))
end

function PersonalCenterModule:resqCheckUserIdCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        if vData.nNumid == 0 then
            XH.TipTool.showToast("请输入正确序号！", 2)
        else
            self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_CHECK_USERID, data = vData })
        end
    end
end

function PersonalCenterModule:reqVIPRollbackTransCard(uid)
    self:startReq("ReqVIPRollbackTransCard", 10, tonumber(uid))
end

function PersonalCenterModule:resqVIPRollbackTransCardCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_ROLL_BACK_CARD, data = vData.nXYData })
    else
        local data = vData.nXYData
        if data.resp and data.resp.err_code then
            XH.TipTool.showToast("撤回操作失败:" .. TeaHouseProtobuf.errorCode[data.resp.err_code], 2)
        else
            XH.TipTool.showToast("撤回操作失败", 2)
        end
    end
end

function PersonalCenterModule:showTabLayer(layerName)
    local tabConfigList = PersonalCenterCinfig.KW_VIEW_TAB
    for i = 1, #tabConfigList do
        if tabConfigList[i].viewName == layerName then
            self:dispatchEvent({ name = PersonalCenterModule.EVENT_VIP_LAYER_SHOW_TAB_LAYER, data = i })
            break
        end
    end
end

function PersonalCenterModule:showGiveLayerTab(tabName, data)
    self:dispatchEvent({ name = PersonalCenterModule.EVENT_GIVE_LAYER_SHOW_TAB_LAYER, data = {tabName = tabName, data = data} })
end

function PersonalCenterModule:UpdatePlayerPropCount()
    if self._reqUpdatePlayerPropCount then
        self._reqUpdatePlayerPropCount:fail()
        self._reqUpdatePlayerPropCount = nil
    end
    local reqUpdatePlayerPropCount = require("lobby.Req.PersonalCenter.ReqUpdatePlayerPropCount")
    self._reqUpdatePlayerPropCount = reqUpdatePlayerPropCount.new()
    self._reqUpdatePlayerPropCount:start(0)
end

function PersonalCenterModule:resqUpdatePlayerPropCountCallBack(req, nType, vData)
    if nType == XH.Req.TYPE.SUCCESS then
        XH.playerData:flushPlayerDrop()
    end
end

return PersonalCenterModule�E  