local SysTool = {}
local cjson = require("cjson")

function SysTool:GetDevid()
    local userType = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_LAST_LOGIN_TYPE_TMP,-1)
    local userNumberid =  XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_LAST_LOGIN_NUMBERID, "")
    local areaid = XH.areaData:getAreaID()
    local targetPlatform = cc.Application:getInstance():getTargetPlatform()                                                                     
    local realnameLogin = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_RN_TOURIST_LOGIN)
    if 0 == targetPlatform then       
        --macAdd = "PF3333379asfafF86"
        -- macAdd = "FD3333379sdfafF09"
        local macAdd = G_MAC_ADD or "FD3333379assssF092010"
        local UDID = G_UDID or "41f14saf900030699000000032010"
        local devid = string.format("%s%s", UDID, macAdd) or ""
        
        local maxDevidLength = 64;
        if (string.len(devid) > maxDevidLength) then
            devid = string.sub(devid, 1, maxDevidLength)
        end
        --devid = "5b74214409cec18db2f92f3b513bbee7de4ea5f4"
        return devid;
    elseif userType == XH.SRSProtocol.PlayerConnect.USERTYPE.IDENTIFY and not realnameLogin then
        local devid = string.format("%s%s", areaid, userNumberid).."5b74214409cec18db2f92f3b513bbee7de4ea5f4"
        local maxDevidLength = 64;
        if (string.len(devid) > maxDevidLength) then
            devid = string.sub(devid, 1, maxDevidLength)
        end
        return devid;
    end
	local TKDevid = SysTool:getTKDevid()
    if TKDevid ~= nil and TKDevid ~= "" then 
        return TKDevid
    else
        return SysTool.GetLocalDevid()
    end
end

function SysTool:getTKDevid()
    local strDevid = un.Device.getHardwareCode()
    local maxDevidLength = 64
    if (string.len(strDevid) > maxDevidLength) then
        strDevid = string.sub(strDevid, 1, maxDevidLength)
    end
    print("getTKDevid:", strDevid)
    return strDevid
end

function SysTool.GetLocalDevid()
    local devid
    if device.platform == "ios" then
        devid = un.Device.getUUID()
    else
        local UDID = un.Device.getUUID() or ""
        local macAdd = un.Device.getMacAddress() or ""
        devid = string.format("%s%s", UDID, macAdd)
    end

    local maxDevidLength = 64;
    if (string.len(devid) > maxDevidLength) then
        devid = string.sub(devid, 1, maxDevidLength)
    end

    return devid;
end

function SysTool:GetOsVersion()
    local strReturn = 10000

    local strSystemVer = un.Device.getSystemVersion()
    if(strSystemVer == "empty") then
        print("Get SysTem Version error")
        strSystemVer = "1"
    end

    local str = ""
    for name in string.gmatch(strSystemVer,"[^.]+") do
        str = str .. name
    end

    local nVersion = tonumber(str)
    if(nVersion == nil) then
        nVersion = 0
    end

    if (nVersion < 100) then
        nVersion = nVersion * 10
    end

    if device.platform == "android" then
        strReturn = 10000 + nVersion
    elseif device.platform == "ios" then
        strReturn = 20000 + nVersion
    end

    return strReturn
end

function SysTool:GetUniqueIdentifier()
    return un.Device.getUUID()
end

function SysTool:MacAddress()
    return un.Device.getMacAddress()
end

function SysTool:GetCurBattery()
    return un.Device.getBatteryState()
end

local strChannelID
function SysTool:getChannelID()
    local pathKey = "channel.json";
	if not cc.FileUtils:getInstance():isFileExist(pathKey) then
        if device.platform == "windows" then
            return 30002
        end
        if device.platform == "android" then
            if not strChannelID then
                local isInit = true
                if cc.Application:getInstance():getVersion() >= "1.5.0" then
                    isInit = XH.ThirdDefine.isInit
                end
                if isInit then
                    strChannelID = xh.XHBridge:xhCallOCFunc("UserInterface", "callFunctionWithResult", json.encode({
                        functionName = "get_channel_id"
                    }))
                end
            end
            print(string.format("strChannelID: %s", strChannelID))
            local channelID = tonumber(strChannelID)
            if channelID ~= nil and channelID ~= 60050 then
                return channelID
            end
            return 30002
        end
        return 30001 
    end
    local channelJson  = cc.FileUtils:getInstance():getStringFromFile(pathKey)
	local jsonData = {}
	if channelJson ~= nil and channelJson ~= "" then	
        local ok, msg = pcall(function()
            jsonData = cjson.decode(channelJson)
        end)

        if not ok then
            print("cjson failed: " .. msg)
            return 30003
        end
	end
    local channelJsonData = 30003--渠道channel.json配置
	for _,data in pairs(jsonData) do
		channelJsonData = data
	end
    return channelJsonData
end

--设置剪切板内容（windows平台不支持）
function SysTool.copyString(string)
    if device.platform == "windows" then
        print("windows not support copy")
        return
    end
    string = XH.StringTool.replaceMatchStr(string)
    if device.platform == "ios" then
        un.Device.copyString(string)
    end

    if device.platform == "android" then
        local luaj = require("cocos.cocos2d.luaj")
        local className = "org/cocos2dx/lua/DeviceUniverseExtend"
        local funcName = "copyString"
        local args = {string}
        local sig = "(Ljava/lang/String;)V"
        luaj.callStaticMethod(className, funcName, args, sig);
    end
end

--获取剪切板内容（windows平台不支持）
function SysTool:getCopyString()
    if device.platform == "windows" then
        print("windows not support getCopyString")
        return
    end
    if device.platform == "android" then
        local luaj = require("cocos.cocos2d.luaj")
        local className = "org/cocos2dx/lua/DeviceUniverseExtend"
        local funcName = "getCopyString"
        local args = {}
        local sig = "()Ljava/lang/String;"
        local _, ret = luaj.callStaticMethod(className, funcName, args, sig);
        return ret
    end
end

function SysTool:md5_encode(str)
	return un.md5Encode(str)
end


local b='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/' -- You will need this for encoding/decoding
-- encoding
function SysTool:base64_encode(data)
    return ((data:gsub('.', function(x) 
        local r,bit='',x:byte()
        for i=8,1,-1 do r=r..(bit%2^i-bit%2^(i-1)>0 and '1' or '0') end
        return r;
    end)..'0000'):gsub('%d%d%d?%d?%d?%d?', function(x)
        if (#x < 6) then return '' end
        local c=0
        for i=1,6 do c=c+(x:sub(i,i)=='1' and 2^(6-i) or 0) end
        return b:sub(c+1,c+1)
    end)..({ '', '==', '=' })[#data%3+1])
end

-- decoding
function SysTool:base64_decode(data)
    data = string.gsub(data, '[^'..b..'=]', '')
    return (data:gsub('.', function(x)
        if (x == '=') then return '' end
        local r,f='',(b:find(x)-1)
        for i=6,1,-1 do r=r..(f%2^i-f%2^(i-1)>0 and '1' or '0') end
        return r;
    end):gsub('%d%d%d?%d?%d?%d?%d?%d?', function(x)
        if (#x ~= 8) then return '' end
        local c=0
        for i=1,8 do c=c+(x:sub(i,i)=='1' and 2^(8-i) or 0) end
            return string.char(c)
    end))
end

--获取Appname
function SysTool:getAppName()
    return XH.ConstString.getStr("APP_NAME")
end

function SysTool:GetBundleVersion()
    local target = cc.Application:getInstance():getTargetPlatform()
    if target == cc.PLATFORM_OS_WINDOWS then  
        return "1.0.1"
    end
    return cc.Application:getInstance():getVersion()
end

function SysTool:CompareToBundleVersion(targetVer)
    if not targetVer then
        return false
    end
    local bundleVer = SysTool:GetBundleVersion()
    local verTable = string.split(bundleVer, ".")
    local targetVerTable = string.split(targetVer, ".")
    for i = 1, #verTable do
        if tonumber(verTable[i]) > tonumber(targetVerTable[i]) then
            return true
        elseif tonumber(verTable[i]) < tonumber(targetVerTable[i]) then
            return false
        end
    end
    return true
end

function SysTool:hexToUUID(hex)
    if not hex then
        return
    end
    local uuid = string.format( "{%02X%02X%02X%02X-%02X%02X-%02x%02x-%02X%02X-%02X%02X%02X%02X%02X%02X}",
    
    string.byte(hex,4,4),
    string.byte(hex,3,3),
    string.byte(hex,2,2),
    string.byte(hex,1,1),

    string.byte(hex,6,6),
    string.byte(hex,5,5),
   
    string.byte(hex,8,8),
    string.byte(hex,7,7),

    string.byte(hex,9,9),
    string.byte(hex,10,10),
    string.byte(hex,11,11),
    string.byte(hex,12,12),
    string.byte(hex,13,13),
    string.byte(hex,14,14),
    string.byte(hex,15,15),
    string.byte(hex,16,16))

    return uuid
end

function SysTool.mergeCsbBinding(target,param)
    if not target then
        return
    end

    param = param or {}
    local preKey = param.preKey
    local startNum = param.startNum
    local endNum = param.endNum
    local preName = param.preName

    if (not preKey) or (not startNum) or (not preName) or (not endNum)  then
        return
    end

    local list = target.binding or {}
    for i=startNum,endNum do
        list[preKey..i] = {tag = preKey..i,name = preName..i,class = param.class,events = param.events}
    end
end

function SysTool.performDelayOnce(callback, delayTime)
    if not callback or not delayTime or delayTime < 0 then
        return
    end
    local scheduleID
    scheduleID = cc.Director:getInstance():getScheduler():scheduleScriptFunc(function()
        cc.Director:getInstance():getScheduler():unscheduleScriptEntry(scheduleID)
        callback()
    end, delayTime, false)
    return scheduleID
end

local char_to_hex = function(c)
    return string.format("%%%02X", string.byte(c))
end

local hex_to_char = function(x)
    return string.char(tonumber(x, 16))
end

function SysTool.url_encode(url)
    if url == nil then
        return
    end
    url = tostring(url)
    url = url:gsub("\n", "\r\n")
    url = url:gsub("([^%w ])", char_to_hex)
    url = url:gsub(" ", "+")
    return url
end

function SysTool.url_decode(url)
    if url == nil then
        return
    end
    url = url:gsub("+", " ")
    url = url:gsub("%%(%x%x)", hex_to_char)
    return url
end

function SysTool.captureScreenshot(callback, fileName)
    local fileFullPath = cc.FileUtils:getInstance():getWritablePath() .. "tmp/" .. fileName
    local directoryFullPath = cc.FileUtils:getInstance():getWritablePath() .. "tmp"
    if cc.FileUtils:getInstance():isDirectoryExist(directoryFullPath) then
        cc.FileUtils:getInstance():removeDirectory(directoryFullPath)
        cc.FileUtils:getInstance():createDirectory(directoryFullPath)
    else
        cc.FileUtils:getInstance():createDirectory(directoryFullPath)
    end


    local scale = 0.5
    local screenSize = cc.Director:getInstance():getWinSize()
    local render = cc.RenderTexture:create(screenSize.width * scale, screenSize.height * scale, _G.kCCTexture2DPixelFormat_RGBA8888, gl.DEPTH24_STENCIL8_OES)
    render:setKeepMatrix(true)
    render:begin()
    cc.Director:getInstance():getRunningScene():visit()
    
    render:endToLua()
    render:saveToFile("tmp/" .. fileName, cc.IMAGE_FORMAT_JPEG, false)
    local retry = 6
    local callbackHanlder
    callbackHanlder = function()
        local exist = cc.FileUtils:getInstance():isFileExist(fileFullPath)
        if exist then
            callback(true, fileFullPath)
        elseif retry >= 0 then
            XH.SysTool.performWithDelayGlobal(callbackHanlder,0.5)
            retry = retry - 1
        else
            callback(false, nil)
        end
    end
    XH.SysTool.performWithDelayGlobal(callbackHanlder,0.5)
end

function SysTool.performWithDelayGlobal(listener, time)
    local runScene = display.getRunningScene()
    return runScene:runAction(cc.Sequence:create(cc.DelayTime:create(time),cc.CallFunc:create(function()
        listener()
    end)))
end

function SysTool.stopPerformWithDelayGlobal(action)
    local runScene = display.getRunningScene()
    return runScene:stopAction(action)
end

function SysTool.downloadNewVersionApp()
    local UrlConf = require("app.Config.UrlConf")
    if device.platform == "ios" then
        cc.Application:getInstance():openURL(UrlConf.DOWNLOAD_ADDRESS_IOS)
    else
        cc.Application:getInstance():openURL(UrlConf.DOWNLOAD_ADDRESS_ANDROID)
    end
end

function SysTool.isToday(timestamp)
    if timestamp <= 0 then
        return false
    end
    local today = os.date("*t")
    local secondOfToday = os.time({day=today.day, month=today.month,year=today.year, hour=0, minute=0, second=0})
    if timestamp >= secondOfToday and timestamp < secondOfToday + 24 * 60 * 60 then
        return true
    end
    return false
end

function SysTool.getTableByStrings(str, semi_char, equ_char, isequ_once)
    local back_table = { }
    local semi_back = string.split(str, semi_char)
    if semi_back == nil or #semi_back < 1 then
        return back_table
    end

    for i = 1, table.nums(semi_back) do
        local equ_back = string.split(semi_back[i], equ_char)
        if equ_back == nil then
            break
        elseif table.nums(equ_back) == 2 then
            back_table[equ_back[1]] = equ_back[2]
        elseif isequ_once then
            for j = 1, table.nums(equ_back) -1 do
                if back_table[equ_back[1]] then
                    back_table[equ_back[1]] = back_table[equ_back[1]] .. equ_char .. equ_back[j + 1]
                else
                    back_table[equ_back[1]] = equ_back[j + 1]
                end
            end
        end
    end
    return back_table
end

-- 获取最恰当适配尺寸
function SysTool.getFitSizeWithDefault(needFitSize, defaultSize)
    local fitSize = 1
    if needFitSize and defaultSize then
        if needFitSize.width and defaultSize.width then
            fitSize = defaultSize.width / needFitSize.width
        end
        if needFitSize.height and defaultSize.height then
            local heightSize = defaultSize.height / needFitSize.height
            if heightSize > fitSize then
                return heightSize
            end
            return fitSize
        end
    end
    return fitSize
end

function SysTool.createShakeAni(node)
    if not node then
        return
    end
    pcall(function()
        node:stopAllActions()
        local action1 = cc.RotateBy:create(0.5, 5)
        local action2 = cc.RotateBy:create(1, -10)
        local action3 = cc.RotateBy:create(1, 10)
        local action4 = cc.DelayTime:create(2)
        local sequenceAction = cc.Sequence:create(action1, action2, action3, action2, action1, action4)
        node:runAction(cc.RepeatForever:create(sequenceAction))
    end)
end

function SysTool.uncompressZlib(data, reTimeMax)
    if not data then
        return
    end
    reTimeMax = reTimeMax or 5
    local zip = require("zlib")
    local uncompress = zip.inflate()
    local inflated, eof = uncompress(data)
    local reTime = 0
    while( eof == false and reTime < reTimeMax ) do
        inflated, eof = uncompress(data)
        reTime = reTime + 1
    end
    if eof then
        return inflated
    end
end

function SysTool.GetResVersion()
    local manifestPath = un.FileSystem.getWritePath() .. un.const.HotFixPath .. "harbor/Lobby/project.manifest"
    if not cc.FileUtils:getInstance():isFileExist(manifestPath) then
        return "0"
    end
    local localManifest = un.hotfix.Manifest.new(manifestPath)
    local curVersion = "0"
    if localManifest:isLoaded() then
        curVersion = localManifest:getVersion()
    end
    return curVersion
end

return SysTool  v?  