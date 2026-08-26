local H5GameWidgetView = class("H5GameWidgetView",XH.ViewBase)

local H5GameNameList = {
    LEGEND = "LEGEND",
    BUYU = "BUYU",
    KNIFELEGEND = "KNIFELEGEND",
    BUYUDAJIANGSAI = "BUYUDAJIANGSAI",
    DATIANSHIZHIJIAN = "DATIANSHIZHIJIAN",
    LINGMENGZHIBO = "LINGMENGZHIBO",
    REXUEFENGSHEN = "REXUEFENGSHEN",
    DOULUODALU = "DOULUODALU",
    JIEJISANGUO = "JIEJISANGUO",
    RENRENBUYU = "RENRENBUYU",
    SHEISHISHOUFU = "SHEISHISHOUFU",
    WEIJINGCHUANQI = "WEIJINGCHUANQI", -- 维京传奇
    ZHUOYUECHUANSHUO = "ZHUOYUECHUANSHUO", -- 卓越传说
    YUANZHENGJIANGSHI = "YUANZHENGJIANGSHI", -- 远征将士
    RONGYAOZHINU = "RONGYAOZHINU", -- 远征将士
    BAIZHANSHACHENG = "BAIZHANSHACHENG", -- 百战沙城
    HUANXIANGMINGJIANGLU = "HUANXIANGMINGJIANGLU"
}
local iconAntList = {
    ["fishing_king"] = {
        File = "animation/Lobby/Base/zjb-by-icon/zjb-by-icon.ExportJson",
        ArmatureName = "zjb-by-icon",
        AniName = "Animation1",
        IconAniScale = 1,
        OffSetY = 0,
        OffSetX = 0,
        name = "捕渔王",
        H5GameName = H5GameNameList.BUYU,
    },
    ["dust_duel"] = {
        File = "animation/Lobby/Base/mr2_comeon_ani/mr2_comeon_ani.ExportJson",
        ArmatureName = "mr2_comeon_ani",
        AniName = "Animation1",
        IconAniScale = 0.5,
        OffSetY = 0,
        OffSetX = 0,
        name = "决战沙邑",
        H5GameName = H5GameNameList.LEGEND,
    },
    ["sword_legend"] = {
        File = "animation/Lobby/Base/mir2_comeon_ani/mir2_comeon_ani.ExportJson",
        ArmatureName = "mir2_comeon_ani",
        AniName = "Animation1",
        IconAniScale = 0.55,
        OffSetY = 0,
        OffSetX = 0,
        name = "一刀传世",
        H5GameName = H5GameNameList.KNIFELEGEND,
     },
    ["fishing_match"] = {
        AniInfo = {
            File = "animation/Lobby/Base/budjs-icon/budjs-icon.ExportJson",
            ArmatureName = "budjs-icon",
            AniName = "Animation1"
        },
        IconAniScale = 0.8,
        OffSetY = 0,
        OffSetX = 0,
        File = "animation/Lobby/Base/budjs-icon/budjs-icon.ExportJson",
        ArmatureName = "budjs-icon",
        AniName = "Animation1",
        name = "捕鱼大奖赛",
        H5GameName = H5GameNameList.BUYUDAJIANGSAI,
    },
    ["bloody_wind_god"] = {
        DargonBonesInfo = {
            path = "animation/Lobby/Base/ly-rxfs-icon/",
            ske = "ly-rxfs-icon_ske.json",
            tex = "ly-rxfs-icon_tex.json",
            armatureName = "armatureName",
            dragonBonesName = "ly-rxfs-icon",
            animationName = "Animation1"
        },
        IconAniScale = 0.6,
        OffSetY = 0,
        OffSetX = 0,
        name = "热血封神",
        H5GameName = H5GameNameList.REXUEFENGSHEN,
    },
    ["lingmeng_live"] = {
        File = "animation/Lobby/Base/tczb_ani/tczb-icon.ExportJson",
        ArmatureName = "tczb-icon",
        AniName = "Animation1",
        AniInfo = {
            File = "animation/Lobby/Base/tczb_ani/tczb-icon.ExportJson",
            ArmatureName = "tczb-icon",
            AniName = "Animation1"
        },
        IconAniScale = 1,
        OffSetY = 0,
        OffSetX = 0,
        name = "羚萌直播",
        H5GameName = H5GameNameList.LINGMENGZHIBO,
    },
    ["all_fishing"] = {
        OffSetY = 10,
        DargonBonesInfo = {
            path = "animation/Lobby/Base/rrby_icon/",
            ske = "rrby_icon_ske.json",
            tex = "rrby_icon_tex.json",
            armatureName = "Armature",
            dragonBonesName = "rrby_icon",
            animationName = "rrby_icon"
        },
        OffSetX = 0,
        IconAniScale = 1,
        name = "人人捕鱼",
        H5GameName = H5GameNameList.RENRENBUYU,
    },
    ["who_richest"] = {
        DargonBonesInfo = {
            path = "animation/Lobby/Base/sssf-icon/",
            ske = "sssf-icon_ske.json",
            tex = "sssf-icon_tex.json",
            armatureName = "armatureName",
            dragonBonesName = "sssf-icon",
            animationName = "Animation1"
        },
        IconAniScale = 0.6,
        OffSetY = 0,
        OffSetX = 0,
        name = "谁是首富",
        H5GameName = H5GameNameList.SHEISHISHOUFU,
    },
}

local scenePos = {
    activity = {posX =1750,posY=85,showIcon="all",direction="y"},
    shop = {posX =1750,posY=85,showIcon="all",direction="y"},
    level_change = {posX =1750,posY=85,showIcon="all",direction="y"},
    game_wait = {posX =160,posY=0,showIcon="all",direction="x"},
    game_begin = {posX =0,posY=467,showIcon="one",direction="x"},
    game_end = {posX =1270,posY=240,showIcon="all",direction="x"},
}

local SHOWSCENE = {
    SHOPVIEW = "商城界面"
}

function H5GameWidgetView:getCSBPath()
    return "cocosStudio/hall/CSB/window/H5GameWidget.csb"
end

function H5GameWidgetView:getBindingInfo()
    return {
        ["_KW_PANEL"] = {varName = "_panelRoot"},
        ["_KW_PANEL_BG"] = {varName="_panelBG",onTouch = "onTouchEventBG"},
        ["_KW_LISTVIEW_ICON"] = {varName="_listViewIcon"},
        ["_KW_ITEM_BTN"] = {varName = "_itemBtn"},
        ["_KW_POS"] = {varName = "_pos"},
        ["_KW_SHOE_MORE_BTN"] = {varName="_btnShowMoer"},
        ["_KW_TEXT_LIKE"] = {varName="_textLike"},

        ["_KW_TIP_PANEL"] = {varName="_tipPanel"},
        ["_KW_TIP_TEXT"] = {varName="_tipText"},
        ["_KW_TIP_CLOSE"] = {varName="_tipClose",onTouchEnded = "onTouchEventCloseTipPanel"},
        ["_KW_TIP_CHECKBOX"] = {varName="_tipCheckBox"},
        ["_KW_TIP_BTN_OK"] = {varName="_tipOK"},
        ["_KW_TIP_BTN_CANEL"] = {varName="_tipCanel",onTouchEnded = "onTouchEventCloseTipPanel"},
        ["_KW_TIP_BTN_LATE_GO"] = {varName="_tipLateGo"},

        ["_KW_GAMESTART_PANEL"] = {varName="_allIconPanel"},
        ["_KW_GAMESTART_CLOSE"] = {varName="_gamestartCanel",onTouchEnded = "onTouchEventCloseGameStartPanel"},
        ["_KW_LISTVIEW_ALL_ICON"] = {varName="_listViewAllIcon"},

        ["_KW_TIP_QIPAO"] = {varName="_tipQiPao"},
        ["_KW_QIPAO_TEXT"] = {varName="_textQiPao"},
    }
end

function H5GameWidgetView:getProxyEvents()
    return {
        { module = XH.lobby:getModule("H5GameWidget"), eventKeyName = XH.lobby:getModule("H5GameWidget").H5GAME_EVENT_CONFIG, callBack = "onFulshConfig" },
        { module = XH.lobby:getModule("H5GameWidget"), eventKeyName = XH.lobby:getModule("H5GameWidget").H5GAME_EVENT_CLOSE, callBack = "onClose" },
    }
end

function H5GameWidgetView:ctor(showType, gameid)
    H5GameWidgetView.super.ctor(self)
    self._showTypeList = {}
    self._showData = nil
    self:init(showType, gameid)
end

function H5GameWidgetView:init(showType, gameid)
    self._gameid = gameid
    table.insert(self._showTypeList,showType)
    self._H5GameConfig = XH.lobby:getModule("H5GameWidget"):getH5GameConfig()
    self:initIcon(showType)
end

function H5GameWidgetView:onTouchEventBG(send, eventType, fun, icon)
    if eventType == ccui.TouchEventType.began then
        self:onBtnPanelBegan(send)
    elseif eventType == ccui.TouchEventType.moved then
        self:onBtnPanelMoved(send)
    elseif eventType == ccui.TouchEventType.ended then
        self:onBtnPanelEnded(send, fun, icon)
    elseif eventType == ccui.TouchEventType.canceled then
        self:onBtnPanelEnded(send)
    end
end

function H5GameWidgetView:onBtnPanelBegan(sender)
    self._BtnPanelStartPosX = self._panelBG:getPositionX()
    self._BtnPanelStartPosY = self._panelBG:getPositionY()
    self._BtnPanelOffPosX = nil
    self._BtnPanelOffPosY = nil
end

function H5GameWidgetView:onBtnPanelMoved(sender)
    local movePos = sender:getTouchMovePosition()
    local tempPos = self:convertToNodeSpace(movePos)
    if self._BtnPanelOffPosX == nil then
        self._BtnPanelOffPosX = self._BtnPanelStartPosX - tempPos.x
        self._BtnPanelOffPosY = self._BtnPanelStartPosY - tempPos.y
    end
    self._panelBG:setPosition(tempPos.x + self._BtnPanelOffPosX, tempPos.y + self._BtnPanelOffPosY)
    self._pos:setString("x:" .. math.floor(tempPos.x + self._BtnPanelOffPosX) .. "  y:" ..  math.floor(tempPos.y + self._BtnPanelOffPosY))
end

function H5GameWidgetView:onBtnPanelEnded(sender, fun, icon)
    local BtnEndPosX = self._panelBG:getPositionX()
    local BtnEndPosY = self._panelBG:getPositionY()

    local viewSize = self._panelRoot:getContentSize()
    if BtnEndPosX < 0 then
        self._panelBG:setPositionX(0)
    end
    if BtnEndPosY < 0 then
        self._panelBG:setPositionY(0)
    end
    if viewSize.height - BtnEndPosY < 150 then
        self._panelBG:setPositionY(viewSize.height - 150)
    end
    if viewSize.width - BtnEndPosX < 150 then
        self._panelBG:setPositionX(viewSize.width - 150)
    end

    if math.abs(BtnEndPosX - self._BtnPanelStartPosX) <= 10 and math.abs(BtnEndPosY - self._BtnPanelStartPosY) <= 10 then
        if fun then
            local userData = {}
            userData.userid = XH.playerData:getNumberID()
            userData.gameid = self._gameid or 0
            userData.showLayer = self._showTypeList[#self._showTypeList] or ""
            userData.iconName = icon or ""
            XH.throwDataManager:throwData(XH.ThrowDataDefine.H5GameIconClick, userData)
            fun()
        elseif self._showTypeList[#self._showTypeList] == "game_begin" and not self._allIconPanel:isVisible() then
            self:showAllIcon()
        end
    end
end

function H5GameWidgetView:initIcon(showType)
    self._textLike:setVisible(false)
    self._btnShowMoer:setVisible(false)
    if self._H5GameConfig and showType then
        local showData = self._H5GameConfig.data
        if showData then
            for scene, iconInfo in pairs(showData) do
                if showType == scene and iconInfo.status == 1 and next(iconInfo.game_sort) ~= nil then
                    self:setPanelBGPos(scenePos[showType].posX, scenePos[showType].posY)
                    self:initIconListView(scene, iconInfo)
                    self._panelRoot:setVisible(true)
                    if showType == "game_wait" then
                        self._textLike:setVisible(true)
                        self._panelBG:setContentSize(self._panelBG:getContentSize().width, self._panelBG:getContentSize().height + 40)
                    elseif showType == "game_begin" then
                        self._btnShowMoer:setVisible(true)
                        self._panelBG:setContentSize(self._panelBG:getContentSize().width + 80, self._panelBG:getContentSize().height)
                    end
                    local gameData = XH.lobby:getModule("H5GameWidget"):getGameData()
                    if gameData then
                        for _,v in ipairs(iconInfo.game_sort) do
                            if iconAntList[v.name] then
                                if gameData.blockLabel == SHOWSCENE.SHOPVIEW then
                                    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.BUYUWANG_SHOP_SHOW,{block_label = gameData.blockLabel,label = iconAntList[v.name].name,block = gameData.block})
                                else
                                    XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.BUYUWANG_SHOP_SHOW,{page = "P1066",block_label = gameData.blockLabel , label = iconAntList[v.name].name , block = gameData.block})
                                end
                            end
                        end     
                    end
                    return
                end
            end
        end
    end
end

function H5GameWidgetView:onFulshConfig()
    self._H5GameConfig = XH.lobby:getModule("H5GameWidget"):getH5GameConfig()
    self:initIcon(self._showTypeList[#self._showTypeList])
end

function H5GameWidgetView:initIconListView(scene, info)
    self._listViewIcon:removeAllChildren()
    if scenePos[scene].direction == "x" then
        self._listViewIcon:setDirection(2)
    elseif scenePos[scene].direction == "y" then
        self._listViewIcon:setDirection(1)
    end
    local iconList = {}
    for _, iconInfo in pairs(info.game_sort) do
        table.insert(iconList,tonumber(iconInfo.sort),iconInfo.name)
    end
    local iconNum = 0
    for _, iconName in pairs(iconList) do
        if iconAntList[iconName] then
            iconNum = iconNum + 1
            local iconBtn = self._itemBtn:clone()
            if iconName == "fishing_king" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenBuYu))
                self:loadIconPic(iconName, iconBtn)
            elseif iconName == "dust_duel" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenLegend))
                self:loadIconPic(iconName, iconBtn)
            elseif iconName == "sword_legend" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenYiDao))
                self:loadIconPic(iconName, iconBtn)
            elseif iconName == "fishing_match" or iconName == "bloody_wind_god" or iconName == "lingmeng_live" or iconName == "who_richest" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenH5Game))
                self:showIconAni(iconName, iconBtn)
                iconBtn.gameName = iconAntList[iconName].name
                iconBtn.H5GameName = iconAntList[iconName].H5GameName
            elseif iconName == "all_fishing" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenRRBuYu))
                self:showIconAni(iconName, iconBtn)
                local pic = iconBtn:getChildByName("_KW_ICON_PIC")
                if pic then
                    pic:setVisible(false)
                end
            end
            self._listViewIcon:addChild(iconBtn)
            if scene == "game_begin" then
                break
            end
        end
    end
    --self._listViewIcon:requestRefreshView()
    self:changeShowType(scenePos[scene].showIcon, scenePos[scene].direction, iconNum)
    --self._showData = info
end

function H5GameWidgetView:showIconAni(iconName,node)
    local file = iconAntList[iconName].File
    local armatureName = iconAntList[iconName].ArmatureName
    local aniName = iconAntList[iconName].AniName
    if file == nil then
        local propAni = display.playDargonBonesAnimByTimes(iconAntList[iconName].DargonBonesInfo, 0)
        if propAni then
            propAni:setPosition(cc.p(node:getContentSize().width / 2 + iconAntList[iconName].OffSetX,node:getContentSize().height / 2 + iconAntList[iconName].OffSetY))
            propAni:setScale(iconAntList[iconName].IconAniScale)
            node:addChild(propAni)
        end
    else
        if not cc.FileUtils:getInstance():isFileExist(file) then
            return
        end
        ccs.ArmatureDataManager:getInstance():addArmatureFileInfo(file)
        local armature = ccs.Armature:create(armatureName)
        node:addChild(armature)
        armature:setAnchorPoint(cc.p(0.5, 0.5))
        armature:setPosition(cc.p(node:getContentSize().width / 2 + iconAntList[iconName].OffSetX,node:getContentSize().height / 2 + iconAntList[iconName].OffSetY))
        armature:setScale(iconAntList[iconName].IconAniScale)
        armature:getAnimation():play(aniName, -1, 1)
    end
end

function H5GameWidgetView:loadIconPic(iconName,node)
    local picNode = node:getChildByName("_KW_ICON_PIC")
    if picNode then
        picNode:loadTexture("h5game_" .. iconName .. ".png", ccui.TextureResType.plistType)
        picNode:ignoreContentAdaptWithSize(true)
        picNode:setPositionX(picNode:getPositionX() + (iconAntList[iconName].OffSetX or 0))
    end
end

function H5GameWidgetView:onTouchOpenBuYu(send, eventType)
    local fun = function()
        -- 点击捕鱼icon
        self:H5GameThrowData(iconAntList["fishing_king"].name)
        if self._gameid then
            self:showTipPanel(function()
                    XH.lobby:getModule("H5GameWidget"):startH5Game(H5GameNameList.BUYU)
                end, "捕渔王")
        else
            if XH.playerData:getWeChatBuYuUserID() ~= "" then
                XH.TipTool.showChoseTip({
                    msg = "请选择登陆方式",
                    btnOneTxt = "公众号登陆",
                    funcChoseOne = function ()
                        XH.lobby:getModule("H5GameWidget"):startH5Game(H5GameNameList.BUYU,true)
                    end,
                    btnTwoTxt = "APP账号登陆",
                    funcChoseTwo = function ()
                        XH.lobby:getModule("H5GameWidget"):startH5Game(H5GameNameList.BUYU)
                    end,
                })
                return
            end
            XH.lobby:getModule("H5GameWidget"):startH5Game(H5GameNameList.BUYU)
        end
    end
    self:onTouchEventBG(send, eventType, fun, "buyu")
end

function H5GameWidgetView:onTouchOpenRRBuYu(send, eventType)
    local fun = function()
        self:H5GameThrowData(iconAntList["all_fishing"].name)
        if self._gameid then
            self:showTipPanel(function()
                    self:onTouchEventUnityRRBUYU()
                end, "人人捕鱼")
        else
            self:onTouchEventUnityRRBUYU()
        end
    end
    self:onTouchEventBG(send, eventType, fun, "rrbuyu")
end

function H5GameWidgetView:onTouchEventUnityRRBUYU()
    if not XH.lobby:getModule("RRBuYu"):isSupportRRBY() then
        return
    end
    if XH.lobby:getModule("RRBuYu"):isDownloading() then 
        return
    end
    self:startUnityGame(H5GameNameList.RENRENBUYU)
end

function H5GameWidgetView:startUnityGame(gameName)
    if gameName == H5GameNameList.RENRENBUYU then
        XH.TipTool.showLoading(5)
        if device.platform == "android" then
            XH.lobby:getModule("RRBuYu"):isDownloadCompleted(true)
            XH.lobby:getModule("Replugin"):postToSepperllitaPlugin(1)
        elseif device.platform == "ios" then
            XH.lobby:getModule("RRBuYu"):joinInRRBuyu()
        end
    end
end

function H5GameWidgetView:onTouchOpenLegend(send, eventType)
    local fun = function()
        self:H5GameThrowData(iconAntList["dust_duel"].name)
        if self._gameid then
            self:showTipPanel(function()
                    XH.lobby:getModule("H5GameWidget"):startH5Game(H5GameNameList.LEGEND)
                end, "决战沙邑")
        else
            XH.lobby:getModule("H5GameWidget"):startH5Game(H5GameNameList.LEGEND)
        end
    end
    self:onTouchEventBG(send, eventType, fun, "legend")
end

function H5GameWidgetView:onTouchOpenYiDao(send, eventType)
    local fun = function()
        if self._gameid then
            self:showTipPanel(function()
                    XH.lobby:getModule("H5GameWidget"):startH5Game(H5GameNameList.KNIFELEGEND)
                end, "一刀传世")
        else
            XH.lobby:getModule("H5GameWidget"):startH5Game(H5GameNameList.KNIFELEGEND)
        end
    end
    self:onTouchEventBG(send, eventType, fun, "yidao")
end

function H5GameWidgetView:onTouchOpenH5Game(send, eventType)
    local fun = function()
        self:H5GameThrowData(send.H5GameName)
        if self._gameid then
            self:showTipPanel(function()
                    XH.lobby:getModule("H5GameWidget"):startH5Game(send.H5GameName)
                end, send.gameName)
        else
            XH.lobby:getModule("H5GameWidget"):startH5Game(send.H5GameName)
        end
    end
    self:onTouchEventBG(send, eventType, fun, send.gameName)
end

function H5GameWidgetView:onClose(event)
    local data = event.data
    if data then
        if data == "all" then
            self:close()
        else
            for key, iconName in ipairs(self._showTypeList) do
                if iconName == data then
                    table.remove(self._showTypeList, key)
                end
            end
            if #self._showTypeList == 0 then
                self:close()
            else
                self:initIcon(self._showTypeList[#self._showTypeList])
            end
        end
    end
end

function H5GameWidgetView:changeShowType(showType, direction, iconCnt)
    if showType == "one" then
        self._panelBG:setContentSize(self._itemBtn:getContentSize().width, self._itemBtn:getContentSize().height)
    elseif showType == "all" then
        if direction == "x" then
            self._panelBG:setContentSize(self._itemBtn:getContentSize().width * iconCnt, self._itemBtn:getContentSize().height)
            self._listViewIcon:setContentSize(self._itemBtn:getContentSize().width * iconCnt, self._itemBtn:getContentSize().height)
            self._listViewIcon:setPosition(self._panelBG:getContentSize().width / 2, self._panelBG:getContentSize().height / 2)
        elseif direction == "y" then
            self._panelBG:setContentSize(self._itemBtn:getContentSize().width, self._itemBtn:getContentSize().height * iconCnt)
            self._listViewIcon:setContentSize(self._itemBtn:getContentSize().width, self._itemBtn:getContentSize().height * iconCnt)
            self._listViewIcon:setPosition(self._panelBG:getContentSize().width / 2, self._panelBG:getContentSize().height / 2)
        end
    end
end

function H5GameWidgetView:onTouchEventShowMore(send, eventType)
    if self._showData.showIcon == "one" then
        self._showData.showIcon = "all"
    else
        self._showData.showIcon = "one"
    end
    self:changeShowType(self._showData.showIcon, self._showData.direction, #self._showData.icon)
end

function H5GameWidgetView:showTipPanel(backFun, tipText)
    local showTip = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_H5GAME_TIP_SHOW, 0)
    local scene = self._showTypeList[#self._showTypeList] ~= "game_end"
    if backFun and tipText then
        if (showTip == 0) and scene then
            self._tipText:setString("即将前往" .. tipText .. "，当前游戏将默认退出。若游戏已开局则会视为逃跑哦！")
            self._tipOK:addTouchEventListener(function()
                self:onTouchEventCloseTipPanel()
                backFun()
            end)
            self._tipLateGo:addTouchEventListener(function()
                self:onTouchEventCloseTipPanel()
                self:showLateGoQiPao("游戏结束后将自动前往" .. tipText)
                XH.lobby:getModule("H5GameWidget"):setLateGoFun(backFun, tipText)
            end)
            self._tipPanel:setVisible(true)
        else
            backFun()
        end
    end
end

function H5GameWidgetView:onTouchEventCloseTipPanel(send, eventType)
    self._tipPanel:setVisible(false)
    if self._tipCheckBox:isSelected() then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_H5GAME_TIP_SHOW, 1)
    end
end

function H5GameWidgetView:showAllIcon()
    self._allIconPanel:setVisible(true)
    XH.lobby:getModule("H5GameWidget"):setGameData(nil,nil,"联运入口","BHF001")
    if #self._listViewAllIcon:getChildren() == 0 then
        self._listViewAllIcon:removeAllChildren()
        local allgameicon = {}
        for scene, iconfo in pairs(self._H5GameConfig.data) do
            if scene == "game_begin" then
                for _, h5game in pairs(iconfo.game_sort) do
                    table.insert(allgameicon, tonumber(h5game.sort), h5game.name)
                end
            end
        end
        local gameData = XH.lobby:getModule("H5GameWidget"):getGameData()
        for _, iconName in ipairs(allgameicon) do
            local iconBtn = self._itemBtn:clone()
            if iconName == "fishing_king" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenBuYu))
                self:loadIconPic(iconName, iconBtn)
            elseif iconName == "dust_duel" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenLegend))
                self:loadIconPic(iconName, iconBtn)
            elseif iconName == "sword_legend" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenYiDao))
                self:loadIconPic(iconName, iconBtn)
            elseif iconName == "fishing_match" or iconName == "bloody_wind_god" or iconName == "lingmeng_live" or iconName == "who_richest" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenH5Game))
                self:showIconAni(iconName, iconBtn)
                iconBtn.gameName = iconAntList[iconName].name
                iconBtn.H5GameName = iconAntList[iconName].H5GameName
            elseif iconName == "all_fishing" then
                iconBtn:addTouchEventListener(handler(self, self.onTouchOpenRRBuYu))
                self:showIconAni(iconName, iconBtn)
                local pic = iconBtn:getChildByName("_KW_ICON_PIC")
                if pic then
                    pic:setVisible(false)
                end
            end
            self._listViewAllIcon:addChild(iconBtn)
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.UNION_MORE_SHOW_PUSH,{page_item_id = gameData.roomId,page_label = gameData.gameName,label = iconAntList[iconName].name})
        end
        self._listViewAllIcon:setContentSize(self._itemBtn:getContentSize().width  * #allgameicon,self._itemBtn:getContentSize().height)
    end
end

function H5GameWidgetView:onTouchEventCloseGameStartPanel()
    self._allIconPanel:setVisible(false)
    XH.lobby:getModule("H5GameWidget"):setGameData(nil,nil,nil,"BMC001")
end

function H5GameWidgetView:setPanelBGPos(posX, posY)
    local screenSize = cc.Director:getInstance():getWinSize()
    self._panelBG:setPositionX(screenSize.width / 1920 * posX)
    self._panelBG:setPositionY(screenSize.height / 1080 * posY)
end

function H5GameWidgetView:showLateGoQiPao(text)
    self._tipQiPao:setVisible(true)
    self._textQiPao:setString(text)
    local seq = cc.Sequence:create( cc.DelayTime:create(5), cc.CallFunc:create(function()
        self._tipQiPao:setVisible(false)
    end))
    self._tipQiPao:runAction(cc.Repeat:create(seq,1))
end

function H5GameWidgetView:H5GameThrowData(gameName)
    local gameData = XH.lobby:getModule("H5GameWidget"):getGameData()
    if gameData then
        if gameData.blockLabel == SHOWSCENE.SHOPVIEW then
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.BUYUWANG_SHOP,{block_label = gameData.blockLabel,block = gameData.block,label = gameName})
        else
            XH.NewThrowDataManager:throwData(XH.NewThrowDataDefine.BUYUWANG_CLICK,{page_item_id = gameData.roomId,page_label = gameData.gameName,block_label = gameData.blockLabel,block =gameData.block,label = gameName})
        end
    end
end

return H5GameWidgetViewj  