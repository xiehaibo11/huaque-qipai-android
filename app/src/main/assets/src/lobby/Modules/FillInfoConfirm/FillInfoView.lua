local FillInfoView = class("FillInfoView", XH.ViewBase)

local KW_HEAD_URL_LIST = {
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/1.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/2.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/3.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/4.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/5.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/6.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/7.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/8.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/9.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/10.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/11.jpg",
    "https://palmstatic.hzxuanming.com/Download/avatar/zjb/12.jpg",
}

local KW_HEAD_DEFAULT_PLIST = "cocosStudio/hall/Image/fill_info_view.plist"
local KW_HEAD_DEFAULT_PNG = "fill_info_default_head.png"
local KW_HEAD_LIST = {
    "fill_info_head_1.jpg",
    "fill_info_head_2.jpg",
    "fill_info_head_3.jpg",
    "fill_info_head_4.jpg",
    "fill_info_head_5.jpg",
    "fill_info_head_6.jpg",
    "fill_info_head_7.jpg",
    "fill_info_head_8.jpg",
    "fill_info_head_9.jpg",
    "fill_info_head_10.jpg",
    "fill_info_head_11.jpg",
    "fill_info_head_12.jpg",
}

local KW_SEX_TYPE = {
    TYPE_MALE = 0,
    TYPE_FAMALE = 1,
}

function FillInfoView:getCSBPath()
    return "cocosStudio/hall/CSB/window/FillInfoView.csb"
end

function FillInfoView:getBindingInfo()
    return {
        ["_KW_PANEL_HEAD_BG_1"] = { varName = "_panelHeadBG1", type = XH.UI_TYPE.PANEL_TOBUTTON, style = XH.UIButton.CLICK_STYLE.NOEFFECT, onTouchEnded = "onHeadBG1Clicked" },
        ["_KW_PANEL_HEAD_BG_2"] = { varName = "_panelHeadBG2", type = XH.UI_TYPE.PANEL_TOBUTTON, style = XH.UIButton.CLICK_STYLE.NOEFFECT, onTouchEnded = "onHeadBG2Clicked" },
        ["_KW_PANEL_HEAD_BG_3"] = { varName = "_panelHeadBG3", type = XH.UI_TYPE.PANEL_TOBUTTON, style = XH.UIButton.CLICK_STYLE.NOEFFECT, onTouchEnded = "onHeadBG3Clicked" },
        ["_KW_PANEL_HEAD_1"] = { varName = "_panelHead1" },
        ["_KW_PANEL_HEAD_2"] = { varName = "_panelHead2" },
        ["_KW_PANEL_HEAD_3"] = { varName = "_panelHead3" },
        ["_KW_IMG_HEAD_SELECT_1"] = { varName = "_imgHeadSelect1" },
        ["_KW_IMG_HEAD_SELECT_2"] = { varName = "_imgHeadSelect2" },
        ["_KW_IMG_HEAD_SELECT_3"] = { varName = "_imgHeadSelect3" },
        ["_KW_BTN_REFRESH"] = { varName = "_btnRefresh", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onRefreshClicked" },
        ["_KW_TF_NAME"] = { varName = "_tfName" },
        ["_KW_PANEL_MALE"] = { varName = "_panelMale", type = XH.UI_TYPE.LAYOUT, onTouchEnded = "onMaleClicked" },
        ["_KW_PANEL_FEMALE"] = { varName = "_panelFemale", type = XH.UI_TYPE.LAYOUT, onTouchEnded = "onFemaleClicked" },
        ["_KW_BTN_MALE"] = { varName = "_btnMale", type = XH.UI_TYPE.BUTTON,onTouchEnded = "onMaleClicked" },
        ["_KW_BTN_FEMALE"] = { varName = "_btnFemale", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onFemaleClicked" },
        ["_KW_BTN_AUTH"] = { varName = "_btnAuth", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onAuthClicked" },
        ["_KW_BTN_WX_AUTH"] = { varName = "_btnWxAuth", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onWXAuthClicked" },
        ["_KW_BTN_OK"] = { varName = "_btnOK", type = XH.UI_TYPE.BUTTON, onTouchEnded = "onOKClicked" },
    }
end

function FillInfoView:ctor()
    FillInfoView.super.ctor(self)

    self._headIndex1 = 1
    self._headIndex2 = self:getNextHeadIndex(self._headIndex1)
    self._headIndex3 = self:getNextHeadIndex(self._headIndex2)

    self._startIndex = 1
    self._selectIndex = 1

    self._sexType = KW_SEX_TYPE.TYPE_MALE

    self:initUI()
end

function FillInfoView:getProxyEvents()
    return {
        { module = XH.playerData, eventKeyName = "EVENT_PLAYER_INFO_CHANGED", callBack = "onChangePlayerInfo" },
        { module = XH.login:getModule("Login"), eventKeyName = "EVENT_BIND_USER", callBack = "onBindUser" },
    }
end

function FillInfoView:initUI()
    self:refreshHeads()
    self:refreshSex()
    if self._tfName then
        self._tfName:setPlaceholderFontColor(cc.c3b(174, 137, 93))
        self._tfName:setFontColor(cc.c3b(205, 133, 81))
    end
end

function FillInfoView:refreshHeads()
    if not self._localImage1 then
        local size = self._panelHead1:getContentSize()
        self._localImage1 = ccui.ImageView:create()
        :setPosition(size.width / 2, size.height / 2)
        :ignoreContentAdaptWithSize(false)
        :setContentSize(size.width - 5, size.height - 5)
        :addTo(self._panelHead1)
    end
    cc.SpriteFrameCache:getInstance():addSpriteFrames(KW_HEAD_DEFAULT_PLIST)
    self._localImage1:loadTexture(KW_HEAD_LIST[self._headIndex1], ccui.TextureResType.plistType)

    if not self._localImage2 then
        local size = self._panelHead2:getContentSize()
        self._localImage2 = ccui.ImageView:create()
        :setPosition(size.width / 2, size.height / 2)
        :ignoreContentAdaptWithSize(false)
        :setContentSize(size.width - 5, size.height - 5)
        :addTo(self._panelHead2)
    end
    cc.SpriteFrameCache:getInstance():addSpriteFrames(KW_HEAD_DEFAULT_PLIST)
    self._localImage2:loadTexture(KW_HEAD_LIST[self._headIndex2], ccui.TextureResType.plistType)

    if not self._localImage3 then
        local size = self._panelHead3:getContentSize()
        self._localImage3 = ccui.ImageView:create()
        :setPosition(size.width / 2, size.height / 2)
        :ignoreContentAdaptWithSize(false)
        :setContentSize(size.width - 5, size.height - 5)
        :addTo(self._panelHead3)
    end
    cc.SpriteFrameCache:getInstance():addSpriteFrames(KW_HEAD_DEFAULT_PLIST)
    self._localImage3:loadTexture(KW_HEAD_LIST[self._headIndex3], ccui.TextureResType.plistType)

    self._startIndex = self._headIndex1
    self._headIndex1 = self:getNextHeadIndex(self._headIndex3)
    self._headIndex2 = self:getNextHeadIndex(self._headIndex1)
    self._headIndex3 = self:getNextHeadIndex(self._headIndex2)

    self._selectIndex = 1
    self:refreshSelect()
end

function FillInfoView:refreshUrlHeads()
    if not self._remoteImage1 then
        local size = self._panelHead1:getContentSize()
        self._remoteImage1 = XH.RemoteImage.new()
        :setPosition(size.width / 2, size.height / 2)
        :ignoreContentAdaptWithSize(false)
        :setContentSize(size.width - 5, size.height - 5)
        :addTo(self._panelHead1)
    end
    self._remoteImage1._remoteUrl = ""
    cc.SpriteFrameCache:getInstance():addSpriteFrames(KW_HEAD_DEFAULT_PLIST)
    self._remoteImage1:loadTexture(KW_HEAD_DEFAULT_PNG, ccui.TextureResType.plistType)
    self._remoteImage1:setUrl(KW_HEAD_URL_LIST[self._headIndex1])

    if not self._remoteImage2 then
        local size = self._panelHead2:getContentSize()
        self._remoteImage2 = XH.RemoteImage.new()
        :setPosition(size.width / 2, size.height / 2)
        :ignoreContentAdaptWithSize(false)
        :setContentSize(size.width - 5, size.height - 5)
        :addTo(self._panelHead2)
    end
    self._remoteImage2._remoteUrl = ""
    cc.SpriteFrameCache:getInstance():addSpriteFrames(KW_HEAD_DEFAULT_PLIST)
    self._remoteImage2:loadTexture(KW_HEAD_DEFAULT_PNG, ccui.TextureResType.plistType)
    self._remoteImage2:setUrl(KW_HEAD_URL_LIST[self._headIndex2])

    if not self._remoteImage3 then
        local size = self._panelHead3:getContentSize()
        self._remoteImage3 = XH.RemoteImage.new()
        :setPosition(size.width / 2, size.height / 2)
        :ignoreContentAdaptWithSize(false)
        :setContentSize(size.width - 5, size.height - 5)
        :addTo(self._panelHead3)
    end
    self._remoteImage3._remoteUrl = ""
    cc.SpriteFrameCache:getInstance():addSpriteFrames(KW_HEAD_DEFAULT_PLIST)
    self._remoteImage3:loadTexture(KW_HEAD_DEFAULT_PNG, ccui.TextureResType.plistType)
    self._remoteImage3:setUrl(KW_HEAD_URL_LIST[self._headIndex3])

    self._headIndex1 = self:getNextHeadIndex(self._headIndex3)
    self._headIndex2 = self:getNextHeadIndex(self._headIndex1)
    self._headIndex3 = self:getNextHeadIndex(self._headIndex2)

    self._selectIndex = 1
    self:refreshSelect()
end

function FillInfoView:refreshSelect()
    if self._imgHeadSelect1 then
        self._imgHeadSelect1:setVisible(false)
    end
    if self._imgHeadSelect2 then
        self._imgHeadSelect2:setVisible(false)
    end
    if self._imgHeadSelect3 then
        self._imgHeadSelect3:setVisible(false)
    end
    local imgHeadSelect = self["_imgHeadSelect" .. self._selectIndex]
    if imgHeadSelect then
        imgHeadSelect:setVisible(true)
    end
end

function FillInfoView:refreshSex()
    if self._btnMale then
        self._btnMale:setSelected(self._sexType == KW_SEX_TYPE.TYPE_MALE)
    end
    if self._btnFemale then
        self._btnFemale:setSelected(self._sexType == KW_SEX_TYPE.TYPE_FAMALE)
    end
end

function FillInfoView:getNextHeadIndex(index)
    index = index + 1
    if index > #KW_HEAD_URL_LIST then
        index = 1
    end
    return index
end

function FillInfoView:onHeadBG1Clicked(send, eventType)
    self._selectIndex = 1
    self:refreshSelect()
end

function FillInfoView:onHeadBG2Clicked(send, eventType)
    self._selectIndex = 2
    self:refreshSelect()
end

function FillInfoView:onHeadBG3Clicked(send, eventType)
    self._selectIndex = 3
    self:refreshSelect()
end

function FillInfoView:onRefreshClicked(send, eventType)
    self:refreshHeads()
end

function FillInfoView:onMaleClicked(send, eventType)
    self._sexType = KW_SEX_TYPE.TYPE_MALE
    self:refreshSex()
end

function FillInfoView:onFemaleClicked(send, eventType)
    self._sexType = KW_SEX_TYPE.TYPE_FAMALE
    self:refreshSex()
end

function FillInfoView:onAuthClicked(send, eventType)
    XH.login:getModule("Login"):reqWXBind()
end

function FillInfoView:onWXAuthClicked(send, eventType)
    XH.login:getModule("Login"):reqWXBind()
end

function FillInfoView:onOKClicked(send, eventType)
    local name = ""
    if self._tfName then
        name = self._tfName:getText()
    end
    if #name <= 0 then
        XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK,
		}, "请输入昵称！")
        return
    end
    local index = self._startIndex + self._selectIndex - 1
    local url = KW_HEAD_URL_LIST[index]
    if url == nil then
        XH.TipTool.showTip({
			type = XH.TIP_LAYER_TYPE.OK,
		}, "请选择头像！")
        return
    end

    XH.TipTool.showLoading()
    XH.playerData:reqChangePlayerInfo(self._sexType, name, url, true)
end

function FillInfoView:onChangePlayerInfo(event)
    if not event.msg.data.isReq then
        return
    end
    XH.TipTool.hideLoading()
    if event.msg.isSuccess then
        XH.TipTool.showToast("修改成功")
        self:close()
    else
        XH.TipTool.showToast("存在非法值！修改失败")
    end
end

function FillInfoView:onBindUser(event)
    self:close()
end

return FillInfoView  j,  