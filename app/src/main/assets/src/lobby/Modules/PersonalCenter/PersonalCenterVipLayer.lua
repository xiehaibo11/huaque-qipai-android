local PersonalCenterVipLayer = class("PersonalCenterVipLayer", XH.ViewBase)
local ShopConfig = require("lobby.Modules.Shop.Config")

local KW_VIP_IMG = "_KW_VIP_IMG"
local KW_VIP_NAME = "_KW_VIP_NAME"
local KW_VIP_INFO = "_KW_VIP_INFO"
local KW_VIP_PAWER = "_KW_VIP_PAWER"
local KW_VIP_POWER_BG = "_KW_VIP_POWER_BG"
local KW_VIP_TIP = "_KW_VIP_TIP"
local KW_BTN_GET_AWARD = "_KW_BTN_GET_AWARD"
local KW_BTN_GIVE_PROP = "_KW_BTN_GIVE_PROP"

local KW_MAX_VIP_LEVEL = 3
local KW_MIN_VIP_LEVEL = 0

function PersonalCenterVipLayer:getCSBPath()
    return "cocosStudio/hall/CSB/PersonalCenter/PerCenterVipLayer.csb"
end

function PersonalCenterVipLayer:getBindingInfo()
    return{
        ["_KW_BTN_LEFT"] = { varName = "_btnLeftArrow", onTouchEnded = "onTouchEventLeftArrow", type = XH.UI_TYPE.IMAGE_TOBUTTON },
        ["_KW_BTN_RIGHT"] = { varName = "_btnRightArrow", onTouchEnded = "onTouchEventRightArrow", type = XH.UI_TYPE.IMAGE_TOBUTTON },
        ["_KW_BTN_GET_AWARD"] = { varName = "_btnGetAwd", onTouchEnded = "onTouchEventGetAward", type = XH.UI_TYPE.IMAGE_TOBUTTON },
        ["_KW_BTN_GIVE_PROP"] = { varName = "_btnGiveProp", onTouchEnded = "onTouchEventGiveProp", type = XH.UI_TYPE.IMAGE_TOBUTTON },
        ["_KW_TEXT_VIP_TIME"] = { varName = "_textVipTime" },
        ["_KW_PAPGVIEW"] = { varName = "_pageView" },
        ["_KW_VIP_INFO_PANEL"] = { varName = "_vipInfoPanel" },
        ["_KW_BTN_RECHARGE"] = { varName = "_btnRecharge", onTouchEnded = "onTouchEventRecharge", type = XH.UI_TYPE.IMAGE_TOBUTTON },
        ["_KW_VIP_TIME_BG"] = { varName = "_bgTimeBg" },
    }
end

function PersonalCenterVipLayer:getProxyEvents()
    return {
        { module = XH.lobby:getModule("PersonalCenter"),eventKeyName = "EVENT_VIP_LAYER_VIP_CONFIG", callBack = "eventVipConfig" },
        { module = XH.lobby:getModule("PersonalCenter"),eventKeyName = "EVENT_VIP_LAYER_VIP_INFO", callBack = "eventVipInfo" },
        { module = XH.lobby:getModule("PersonalCenter"),eventKeyName = "EVENT_VIP_LAYER_GET_AWARD", callBack = "eventGetAward" },
    }
end

function PersonalCenterVipLayer:ctor(param)
    param = param or {}
    PersonalCenterVipLayer.super.ctor(self, param)
    self._showLevel = 1
    local vipConfig = XH.lobby:getModule("PersonalCenter"):getVipConfig(true)
    local vipInfo = XH.lobby:getModule("PersonalCenter"):getVipInfo(true)
    if vipConfig and vipInfo then
        self:initUI(vipConfig, vipInfo)
    end
end

function PersonalCenterVipLayer:initUI(vipConfig, vipInfo)
    self._showLevel = vipInfo.vip_id or self._showLevel
    self._textVipTime:setString(vipConfig.vipTime)
    self._bgTimeBg:setVisible(not (vipConfig.vipTime == ""))
    for i, vip in ipairs(vipConfig.vip_config) do
        local infoNode = self._vipInfoPanel:clone()
        XH.UITool.loadTexture(infoNode, KW_VIP_IMG, "personalcenter_vip_level" .. vip.vip_id .. ".png", ccui.TextureResType.plistType)
        XH.UITool.setText(infoNode, KW_VIP_NAME, vip.name)
        XH.UITool.setTextColor(infoNode, KW_VIP_NAME, vip.nameColor)
        XH.UITool.setText(infoNode, KW_VIP_INFO, vip.condition_desc)
        if vip.desc and vip.desc ~= "" then
            XH.UITool.setText(infoNode, KW_VIP_PAWER, vip.desc)
            XH.UITool.setVisible(infoNode, KW_VIP_POWER_BG, true)
        else
            XH.UITool.setVisible(infoNode, KW_VIP_POWER_BG, false)
        end
        XH.UITool.setVisible(infoNode,KW_BTN_GET_AWARD,vip.award_prop~= nil and #vip.award_prop > 0)
        XH.UITool.setTag(infoNode,KW_BTN_GET_AWARD,vip.vip_id)
        XH.UITool.setTag(infoNode,KW_BTN_GIVE_PROP,vip.vip_id)
        if vip.can_trans_prop then
            XH.UITool.setVisible(infoNode,KW_BTN_GIVE_PROP,true)
            if vip.award_prop~= nil and #vip.award_prop > 0 then
                XH.UITool.setPositionX(infoNode,KW_BTN_GET_AWARD,850*0.3)
            else
                XH.UITool.setPositionX(infoNode,KW_BTN_GIVE_PROP,XH.UITool.getPositionX(infoNode,KW_BTN_GET_AWARD))
            end
        else
            XH.UITool.setVisible(infoNode,KW_BTN_GIVE_PROP,false)
        end
        if vipInfo.vip_id == vip.vip_id then
            XH.UITool.setBright(infoNode,KW_BTN_GET_AWARD,vipInfo.vip_award_state == 0)
            XH.UITool.setBright(infoNode,KW_BTN_GIVE_PROP,true)
        else
            XH.UITool.setBright(infoNode,KW_BTN_GET_AWARD,false)
            XH.UITool.setBright(infoNode,KW_BTN_GIVE_PROP,false)
        end
        
        XH.UITool.setVisible(infoNode, KW_VIP_TIP, vipInfo.vip_id == vip.vip_id)
        self._pageView:insertPage(infoNode, i)
    end
    self._vipInfoPanel:removeFromParent()
    self:fulshArrowState()
end

function PersonalCenterVipLayer:eventVipConfig(event)
    if event and event.data then
        local vipInfo = XH.lobby:getModule("PersonalCenter"):getVipInfo(false)
        if vipInfo then
            self:initUI(event.data, vipInfo)
        end
    end
end

function PersonalCenterVipLayer:eventVipInfo(event)
    if event and event.data then
        local vipConfig = XH.lobby:getModule("PersonalCenter"):getVipConfig(false)
        if vipConfig then
            self:initUI(vipConfig, event.data)
        end
    end
end

function PersonalCenterVipLayer:onTouchEventLeftArrow(send, eventType)
    self._showLevel = self._showLevel - 1
    self:fulshArrowState()
end

function PersonalCenterVipLayer:onTouchEventRightArrow(send, eventType)
    self._showLevel = self._showLevel + 1
    self:fulshArrowState()
end

function PersonalCenterVipLayer:fulshArrowState()
    if self._showLevel < KW_MIN_VIP_LEVEL then
        self._showLevel = KW_MIN_VIP_LEVEL
    end
    if self._showLevel > KW_MAX_VIP_LEVEL then
        self._showLevel = KW_MAX_VIP_LEVEL
    end
    if self._showLevel == KW_MIN_VIP_LEVEL then
        self._btnLeftArrow:setOpacity(255*0.4)
        self._btnLeftArrow:setEnabled(false)
    else
        self._btnLeftArrow:setOpacity(255)
        self._btnLeftArrow:setEnabled(true)
    end
    if self._showLevel == KW_MAX_VIP_LEVEL then
        self._btnRightArrow:setOpacity(255*0.4)
        self._btnRightArrow:setEnabled(false)
    else
        self._btnRightArrow:setOpacity(255)
        self._btnRightArrow:setEnabled(true)    
    end
    self._pageView:scrollToPage(self._showLevel + 1)
end
function PersonalCenterVipLayer:onTouchEventRecharge(send, eventType)
    XH.viewManager:openView("ShopView", nil, { productType = ShopConfig.SpecialTag.DIAMOND })
end

function PersonalCenterVipLayer:onTouchEventGetAward(send, eventType)
    local vipInfo = XH.lobby:getModule("PersonalCenter"):getVipInfo()
    if send:getTag() < vipInfo.vip_id then
        XH.TipTool.showToast( "不属于当前等级哦~", 2)
        return
    end
    if send:getTag() > vipInfo.vip_id then
        XH.TipTool.showToast( "不满足等级条件啦，快去充值升级啦~", 2)
        return
    end
    if self._isGeting then
        XH.TipTool.showToast( "操作频繁", 2)
        return 
    end
    self._isGeting = true
    XH.lobby:getModule("PersonalCenter"):reqVIPGetAward()
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.PERSONAL_CENTET_GETAWD_BTN_CLICK)
    XH.SysTool.performWithDelayGlobal(function ()
            self._isGeting = false
        end, 0.5)
end

function PersonalCenterVipLayer:onTouchEventGiveProp(send, eventType)
    local vipInfo = XH.lobby:getModule("PersonalCenter"):getVipInfo()
    if send:getTag() ~= vipInfo.vip_id then
        XH.TipTool.showToast( "不属于当前等级哦~", 2)
        return
    end
    XH.lobby:getModule("PersonalCenter"):showTabLayer("PersonalCenterGiveLayer")
    XH.lobby:getModule("PersonalCenter"):showGiveLayerTab("GivePropLayer")
    XH.throwDataManager:recordButtonClick(XH.ThrowDataDefine.ButtonEnumTable.PERSONAL_CENTET_GIVEPROP_BTN_CLICK)
end

function PersonalCenterVipLayer:eventGetAward(event)
    if event then
        self._isGeting = false
        local childernNode = self._pageView:getChildren()
        local vipInfo = XH.lobby:getModule("PersonalCenter"):getVipInfo()
        for i,pageNode in ipairs(childernNode) do
            if vipInfo.vip_id + 1 == i then
                --XH.UITool.setEnabled(pageNode,KW_BTN_GET_AWARD, false)
                --XH.UITool.setColor(pageNode,KW_BTN_GET_AWARD, cc.c3b(160,160,160))
                XH.UITool.setBright(pageNode,KW_BTN_GET_AWARD,false)
            end
        end
    end
end

return PersonalCenterVipLayer   �!  