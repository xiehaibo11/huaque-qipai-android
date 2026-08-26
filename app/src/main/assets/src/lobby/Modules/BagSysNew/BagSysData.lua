--新背包系统

local BagSysData = class("BagSysData")

local basePropsConfigFileName = "basePropsConfig.json"
local boxemojiPropsConfigFileName = "boxemojiPropsConfig.json"
local goldemojiPropsConfigFileName = "goldemojiPropsConfig.json"
local DEFAULT_DOWNLOAD_IMAGE_DIR = device.writablePath .. "tmpImageCachDir/"

local KW_ADD_HOUR_STR = "addhour"
local KW_ADD_DAY_STR = "addday"
local KW_PROP_HEAD_FRAME_ID = "head_frame"
local KW_PROP_ALL_ID = "all_prop"
local KW_PROP_JPQ_TAG = "prop_jpq"
local KW_PROP_COUPON_ID = "prop_coupon"     --优惠卡券

local BASIC_RROPS_TYPE = 0 
local BOX_EMOJI_RROPS_TYPE = 1
local GOLD_EMOJI_RROPS_TYPE = 2 
local KW_ZJB_PKG_ID = 2

local KW_PROP_ID_HIDE = "hide"

function BagSysData:ctor()
    cc(self):addComponent("components.behavior.EventProtocol"):exportMethods()

    -- Bag服务的appid列表
    self._svrAppidList = {}

    -- Bag期望指定的appid
    self._proposalAppid = 0

    -- 所有玩家背包数据,numid->bagData
    self._allBagData = {}

    -- 自己背包数据
    self._selfBagData = {}

    -- 配置
    self._propsConfig = nil
    self._boxEmojipropsConfig = nil
    self._goldEmojipropsConfig = nil
    -- 配置分类，按照propsid->propsconfig分类
    self._devideConfigByPropid = nil

    --配置分类，按照props_tag_id->propsconfig分类
    self._devideConfigByProptag = nil

    self:initData()
end

function BagSysData:setSvrAppidList(list)
    self._svrAppidList = list
end

function BagSysData:getSvrAppidList()
    return self._svrAppidList or {}
end

function BagSysData:setProposalAppid(appid)
    self._proposalAppid = appid
end

function BagSysData:getProposalAppid()
    return self._proposalAppid or 0
end

function BagSysData:initData()
    self:devideBagConfigByPropid()
    self:devideBagConfigByPropTag()
    if self._propsConfig and self._propsConfig.extend_props_config then
        XH.propsData:setExtendPropsConfig(self._propsConfig.extend_props_config)
    end

end

--保存背包配置，备注：md5不写到json文件里去，直接保存json文件到本地
--bzip 是否是zlib压缩过的文件
function BagSysData:setPropsConfig(resData)
    local bagMsg = resData.data
    if not bagMsg or bagMsg == "" then
        return
    end
    local bzip = resData.bzip
    if bzip then
        local zip = require("zlib")
        local uncompress = zip.inflate()
        local inflated, eof = uncompress(bagMsg)
        while( eof == false ) do
            inflated, eof = uncompress(bagMsg)
        end
        bagMsg = inflated
    end

    local jsonData = {}
    local ok, msg = pcall(function()
        jsonData = cjson.decode(bagMsg)
    end)

    if not ok  then
        print("bag cjson failed: " .. tostring(msg))
        return
    end

    if resData.propstype ==  BASIC_RROPS_TYPE then
        self._propsConfig = jsonData
    elseif resData.propstype ==  BOX_EMOJI_RROPS_TYPE then 
        self._boxEmojipropsConfig = jsonData
    elseif resData.propstype ==  GOLD_EMOJI_RROPS_TYPE then 
        self._goldEmojipropsConfig = jsonData 
    end
    self:saveDataToLocalFile(bagMsg,resData.propstype)
    self:initData()
end

--获取背包配置
function BagSysData:getPropsConfig()
    if self._propsConfig then
        return self._propsConfig
    end

    local configStr = self:getDataFromLocalFile(BASIC_RROPS_TYPE)
    local config = {}
    if configStr and configStr ~= "" then
        config = cjson.decode(configStr)
        self._propsConfig = config or {}
    end
    return config
end

--获取背包配置md5
function BagSysData:getPropsConfigMd5(propstype)
    local fileStr = self:getLocalFileName(propstype)
    if fileStr then
        local md5 = un.getMd5OfFile(fileStr)
        return md5
    end
    return ""
end

--保存其他玩家背包数据
function BagSysData:setPlayerBagData(data, numid)
    if not numid then return end
    self._allBagData[numid] = data
end

--游戏内
function BagSysData:getPlayerBagData(numid)
   numid = numid or XH.playerData:getNumberID()
   return self._allBagData[numid]
end

--单个玩家所有背包数据，按照propid->data分类
function BagSysData:devidePlayerBagData(bagData)
    local devideData = {}
    if bagData and next(bagData) then
        local countPropInfo = bagData.count_prop_info or {}
        local timePropInfo = bagData.time_prop_info or {}
        devideData = self:devideSingleData(countPropInfo)
        local devideData2 = self:devideSingleData(timePropInfo)
        table.merge(devideData,devideData2)
    end
    return devideData
end

--某种时效道具或个数道具，按照pripid->data 分类
function BagSysData:devideSingleData(propData)
    local devideData = {}
    if not propData or not next(propData) then
        return {}
    end
    for _,v in pairs(propData) do
        if v.propsid then
            devideData[v.propsid] = v
        end
    end
    return devideData
end

-- 背包配置分类,基础配置，web配置，全部组合起来,按照propid分类
--[[
    config = {
        ['propid1'] = prop_config1,
        ['propid2'] = prop_config2,
    }
]]
function BagSysData:devideBagConfigByPropid()
    local propidPropConfig = {}
    local bagDataConfig = self:getPropsConfig()
    if bagDataConfig then
        local extendPropsConfig = bagDataConfig.extend_props_config or {}
        local basePropsConfig = bagDataConfig.base_props_config or {}

        if extendPropsConfig then
            for _ , prop in pairs(extendPropsConfig) do
                if prop.propsid then
                    propidPropConfig[prop.propsid] = prop
                end
            end
        end

        if basePropsConfig then
            for _ , prop in pairs(basePropsConfig) do
                if prop.propsid then
                    propidPropConfig[prop.propsid] = propidPropConfig[prop.propsid] or {}
                    table.merge(propidPropConfig[prop.propsid], prop)
                end
            end
        end
    end
    self._devideConfigByPropid = propidPropConfig
end

function BagSysData:getDevideBagConfigData()
   return self._devideConfigByPropid
end

-- 背包配置分类，按照props_tag_id分类
--[[
    config = {
        ['head_frame'] = {prop1,prop2},
        ['mahback'] = {prop1,prop2},
    }
]]
function BagSysData:devideBagConfigByPropTag()
    if not self._devideConfigByPropid then return end
    local propTagConfig = {}
    for _, propData in pairs(self._devideConfigByPropid) do
        --约定:gift,hide标签的道具客户端不显示
        if propData.props_tag_id and propData.props_tag_id ~= KW_PROP_ID_HIDE then
            propTagConfig[propData.props_tag_id] = propTagConfig[propData.props_tag_id] or {}
            table.insert(propTagConfig[propData.props_tag_id], propData)
        end
    end
    --增加一个全部的页签
    propTagConfig[KW_PROP_ALL_ID] = {}
    propTagConfig[KW_PROP_COUPON_ID] = {}
    self._devideConfigByProptag = propTagConfig
end

function BagSysData:getDevideBagConfigDataTag()
    return self._devideConfigByProptag
end

--将某个人的背包数据按类型分类
-- 分类后的道具
--[[
    bagData = {
        head_frame = {prop1, prop2.},
        mahback = { prop1, prop2.},
    }
]]
function BagSysData:getDevideCategoryData(playerBagData)
    if not playerBagData then return end
    local devideBackConfig = self:getDevideBagConfigData()
    
    if not devideBackConfig  then
        return
    end

    --所有道具
    local allprop = {}
    local categoryDevideData = {}
    local timePropInfo = playerBagData.time_prop_info or {}
    local countPropInfo = playerBagData.count_prop_info or {}
    

    for _, timeProp in pairs(timePropInfo) do
        if timeProp.propsid then
            local prop = devideBackConfig[timeProp.propsid]
            if prop and prop.props_tag_id and prop.props_tag_id ~= "" and prop.props_tag_name and  prop.props_tag_name ~= "" and prop.props_tag_id ~= KW_PROP_ID_HIDE then
                categoryDevideData[prop.props_tag_id] = categoryDevideData[prop.props_tag_id] or {}
                local tmpTimeProp = clone(timeProp)
                tmpTimeProp.is_time_prop = true
                table.merge(tmpTimeProp, prop) --背包数据和基础配置合并
                table.insert(categoryDevideData[prop.props_tag_id],tmpTimeProp)
                table.insert(allprop,tmpTimeProp)
            end
        end
    end

    for _, countProp in pairs(countPropInfo) do
        if countProp.propsid then
            local prop = devideBackConfig[countProp.propsid]
            if prop and prop.props_tag_id and prop.props_tag_id ~= "" and prop.props_tag_name and prop.props_tag_name ~= "" and prop.props_tag_id ~= KW_PROP_ID_HIDE then
                categoryDevideData[prop.props_tag_id] = categoryDevideData[prop.props_tag_id] or {}
               local tmpCountProp = clone(countProp)
                tmpCountProp.is_time_prop = false
                table.merge(tmpCountProp, prop) --背包数据和基础配置合并
                table.insert(categoryDevideData[prop.props_tag_id],tmpCountProp)
                table.insert(allprop,tmpCountProp)
            end
        end
    end

    --此处获取web优惠卡券信息
    self._couponList = XH.lobby:getModule("BagSysNew"):dealCouponData()
    --如果请求到了，则在本地添加一个userdefault用来判断是否显示优惠卡券页签
    if next(self._couponList) then 
        categoryDevideData[KW_PROP_COUPON_ID] = categoryDevideData[KW_PROP_COUPON_ID] or {}
        for _,couponProp in pairs(self._couponList) do
            local tmpTimeProp = clone(couponProp)
            tmpTimeProp.is_time_prop = true
            table.insert(categoryDevideData[KW_PROP_COUPON_ID],tmpTimeProp)
            table.insert(allprop,tmpTimeProp)
        end
    end

    --保存所有道具
    categoryDevideData[KW_PROP_ALL_ID] = allprop
    --按获取时间降序排序
    for _ , divProp in pairs(categoryDevideData) do
        table.sort( divProp, function(p1,p2)
            return p1.addtime > p2.addtime
        end )
    end

    return categoryDevideData
end

function BagSysData:getSelfDevideCategoryData()
    return self:getDevideCategoryData(self._selfBagData)
end


--是否是时效道具
--判断是否需要UseProp: baseconfig里面的道具的script字段里面有addhour或者addday，就是时效道具
function BagSysData:isTimeProp(propData)
    propData = propData or {}
    local script = propData.script or ""
    local addHourCount = self:getLuaStrValue(script,KW_ADD_HOUR_STR)
    local addDayCount = self:getLuaStrValue(script,KW_ADD_DAY_STR)
    if tonumber(addHourCount) or tonumber(addDayCount) then
        return true
    end
    return false
end

--得到道具信息
function BagSysData:getPropInfo(propID)
    propID = tonumber(propID) or -1
    if self._devideConfigByPropid and next(self._devideConfigByPropid) then
        local propData = self._devideConfigByPropid[propID]
        if propData then
            return propData
        end
    end
    return
end

--是否是时效道具，直接用propID判断
function BagSysData:isTimePropByPropID(propID)
    propID = tonumber(propID) or -1
    if self._devideConfigByPropid and next(self._devideConfigByPropid) then
        local propData = self._devideConfigByPropid[propID]
        if propData then
            return self:isTimeProp(propData)
        end
    end
    return false
end

--是否是记牌器道具
function BagSysData:isJPQProp(propID)
    propID = tonumber(propID) or -1
    if self._devideConfigByPropid and next(self._devideConfigByPropid) then
        local propData = self._devideConfigByPropid[propID]
        if propData then
            return KW_PROP_JPQ_TAG == tostring(propData.props_tag_id)
        end
    end
    return false
end

--保存自己背包数据 大厅背包用
function BagSysData:setSelfBagData(data)
    if self._selfBagData then
        XH.lobby:getModule("BagSysNew"):judgeCanUseProp(data)
    end
    self._selfBagData = data
end

--获取自己背包数据
function BagSysData:getSelfBagData()
    return self._selfBagData
end

--获取自己头像框，取最后一张图
function BagSysData:getSelfHeadFrameTexture()
    local headUrl = self:getSelfHeadFrameUrl()
    if not headUrl then
        return nil
    end
    local imageName = XH.StringTool.getImageNameByUrl(headUrl)
    headUrl = DEFAULT_DOWNLOAD_IMAGE_DIR .. imageName
    return headUrl
end

function BagSysData:getSelfHeadFrameUrl()
    local headUrl = nil
    local propid = 0
    local devideBaseConfig = self:getDevideBagConfigData()
    if not devideBaseConfig then return end
    local devidePlayerBagData = self:devidePlayerBagData(self._selfBagData)
    for propsid, playerBagData in pairs(devidePlayerBagData) do
        local configData = devideBaseConfig[propsid]
        if configData and configData.props_tag_name then
            if configData.props_tag_id == KW_PROP_HEAD_FRAME_ID and 
                playerBagData.props_state == XH.BagSysProtocol.PROPS_STATE.PS_IN_BODY and
                (playerBagData.propscnt == 0 or XH.lobby:getModule("BagSysNew"):judgeForeverProp(playerBagData.propsid) )then --已经穿戴,且都转成了时效道具,才显示
                local props_img = configData.props_img or {}
                for _,av in pairs(props_img) do
                    if tonumber(av.package_id) == KW_ZJB_PKG_ID then -- package_id == 2表示浙江包
                        local imgArray = av.img or {}
                        if next(imgArray) then
                            headUrl = imgArray[#imgArray].img_url
                            propid = configData.propsid
                        end
                        break
                    end
                end
            end
        end
    end
    return headUrl, propid
end

----------------------------------------------------
function BagSysData:dispatch(eventName, bShowTip)
    if bShowTip ~= nil then
        self:dispatchEvent( { name = eventName, data = bShowTip })
    else
        self:dispatchEvent( { name = eventName })
    end
end

function BagSysData:getLocalFileName(propstype)
    if propstype == BASIC_RROPS_TYPE then
        return device.writablePath .. basePropsConfigFileName
    elseif propstype == BOX_EMOJI_RROPS_TYPE then
        return device.writablePath .. boxemojiPropsConfigFileName
    elseif propstype == GOLD_EMOJI_RROPS_TYPE then
        return device.writablePath .. goldemojiPropsConfigFileName
    end
end

function BagSysData:saveDataToLocalFile(data_str,propstype)
    local filePath = self:getLocalFileName(propstype)
    if filePath then
        return io.writefile(filePath, data_str)
    end
end

function BagSysData:getDataFromLocalFile(propstype)
    local filePath = self:getLocalFileName(propstype)
    local fullPath =  cc.FileUtils:getInstance():fullPathForFilename(filePath)
    if fullPath then
        local fileStr = cc.FileUtils:getInstance():getStringFromFile(fullPath)
        return fileStr
    end
end

function BagSysData:getLuaStrValue(luaString,  variable)
    local value = ""
    local vs = string.split(luaString, ";") or {}
    for i = 1,#vs do
        local vss = string.split(vs[i], "=") or {}
        if (#vss >= 2 and vss[1] == variable)then
            value = vss[2]
            if (string.len(value) >= 2)then
                if (value[1] == '\'' and value[string.len(value)] == '\'')then
                    value = string.sub(value,1,string.len(value) - 1)
                end
            end
            return value
        end
    end
    return value
end

--获取包厢场互动表情配置
function BagSysData:getboxEmojiPropsConfig()
    if self._boxEmojipropsConfig then
        return self._boxEmojipropsConfig
    end

    local configStr = self:getDataFromLocalFile(BOX_EMOJI_RROPS_TYPE)
    local config = {}
    if configStr and configStr ~= "" then
        config = cjson.decode(configStr)
        self._boxEmojipropsConfig = config or {}
    end
    return config
end

--获取金币场场互动表情配置
function BagSysData:getgoldEmojiPropsConfig()
    if self._goldEmojipropsConfig then
        return self._goldEmojipropsConfig
    end

    local configStr = self:getDataFromLocalFile(GOLD_EMOJI_RROPS_TYPE)
    local config = {}
    if configStr and configStr ~= "" then
        config = cjson.decode(configStr)
        self._goldEmojipropsConfig = config or {}
    end
    return config
end

return BagSysDataDB  