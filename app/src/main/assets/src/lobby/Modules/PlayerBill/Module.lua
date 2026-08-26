local PlayerBillModule = class("PlayerBillModule", XH.ModuleBase)

PlayerBillModule.SHOW_BILL_DAY = 7
PlayerBillModule.EVENT_HALL_BILL_UPDATE_TOTALLIST = "EVENT_HALL_BILL_UPDATE_TOTALLIST"
PlayerBillModule.EVENT_HALL_BILL_UPDATE_BILLDETAIL = "EVENT_HALL_BILL_UPDATE_BILLDETAIL"
PlayerBillModule.EVENT_HALL_BILL_UPDATE_CURTIME = "EVENT_HALL_BILL_UPDATE_CURTIME"
PlayerBillModule.EVENT_HALL_BILL_CLOSE = "EVENT_HALL_BILL_CLOSE"
local KW_DATA_TEAHOUSE_LEDGER = "hallLedger.lua"
local REQ_PLAYER_BILL_FIRST_CNT = 100
local REQ_PLAYER_BILL_NORAML_CNT = 30
local KW_DATA_TODAY_TIME = 24 * 60 * 60

local lastSRSGroupID

function PlayerBillModule:ctor()
    PlayerBillModule.super.ctor(self)
    self:initData()
    self:init()
end

function PlayerBillModule:getReqConfig()
    return {
        ReqShareGame = {reqPath = "lobby.Req.PlayerBill.ReqShareGame", callBack = self.onReqShareGameCallBack},
        ReqCurTime = {reqPath = "lobby.Req.PlayerBill.ReqCurTime", callBack = self.onReqCurTimeCallBack},
        ReqPlayerBill = {reqPath = "lobby.Req.PlayerBill.ReqPlayerBill", callBack = self.onReqPlayerBill},
        ReqPlayerBillCount = {reqPath = "lobby.Req.PlayerBill.ReqPlayerBill", callBack = self.onReqPlayerBillCount},
        ReqLedgerDetail = {reqPath = "lobby.Req.PlayerBill.ReqLedgerDetail", callBack = self.onReqBillInfoDetail},
    }
end

function PlayerBillModule:initData()
    self._totalBillInfo = {}			--战绩数据
    self._detailBillInfo = {}		--战绩详情数据

    self._curShowRoomInfo = nil

    self._curTime = 0				--当前获取到的时间戳
    self._curBilldate = ""			--当前日期的字符串
    self._teaNumber = 0					--当前比赛场ID
    self._bReqPlayerBillFirst = false
    self._loopRequest = false
    self._totalNeedReqBill = {}
end

function PlayerBillModule:init()
    
end

function PlayerBillModule:getProxyEvents()
    return {
        { module = XH.login:getModule("Login"), eventKeyName = "LOGIN_EVENT_SUCCESS", callBack = "onLoginSuccess" },
        { module = XH.roomManager, eventKeyName = "EVENT_GAME_SCENE_EXIT", callBack = "pullResultData" },
    }
end

function PlayerBillModule:cleanup()
    --取消协议监听
	XH.netEngine:removeProtocolScriptFuncByObj(self, self.onReciveBillInfoMsg, XH.areaData:getSrsGroupID())
end

function PlayerBillModule:pullResultData()
    self:reqCurTime()
end

--登陆成功后请求数据
function PlayerBillModule:onLoginSuccess()
    self:initData()
    local srsGroupIDNow = XH.areaData:getAreaGameNameList().srsGroupID
    if lastSRSGroupID and lastSRSGroupID ~= srsGroupIDNow then
        XH.netEngine:removeProtocolScriptFuncByObj(self, self.onReciveBillInfoMsg, lastSRSGroupID)
    end
    lastSRSGroupID = srsGroupIDNow
    
	XH.netEngine:addProtocolScriptFuncByObj(self, self.onReciveBillInfoMsg, XH.ActiveProtocol.processid, lastSRSGroupID)
    --获取服务器时间
    self:reqCurTime()
end

function PlayerBillModule:onReciveBillInfoMsg(nXYID, oBuf, nLen, nProcessID)
    if nProcessID ~= XH.ActiveProtocol.processid then
        return
    end
    if nXYID == XH.ActiveProtocol.RespDetail.XY_ID then
        self:onBillInfoRespDetail(nXYID, oBuf, nLen)
    end
end

--请求回放
function PlayerBillModule:startPlayBack(shareID)
	if not shareID or not tonumber(shareID) then
		print("PlayerBillModule:startPlayBack invalid shareID:", tostring(shareID))
		XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, "RESULT_REPLAY_INVALID_SHARE_CODE")
		return
	end
    self:startReq("ReqShareGame", "@"..shareID ,10)
	XH.TipTool.showLoading()
end

function PlayerBillModule:onReqShareGameCallBack(cell, type, data)
    XH.TipTool.hideLoading()
	if type == XH.Req.TYPE.SUCCESS then
        local param = {}
		param.appID = 0
		param.gameID = cell._nGameid
		param.roomMode = XH.ROOM_MODE.BOXROOM
        if cell._roomid < 0 then
            param.roomID = string.format("%06d",-cell._roomid)
        else
		    param.roomID = string.format("%06d",cell._roomid)
        end
        param.strRecordPath = cell.recordPath
		XH.roomManager:gameStart(param)
        self:dispatchEvent({ name = PlayerBillModule.EVENT_HALL_BILL_CLOSE })

        -- 保存进入回放的初始时间
        cc.UserDefault:getInstance():setIntegerForKey("KW_PLAYBACK_STARTTIME", os.time())
	end
end

function PlayerBillModule:refreshAllData()
    self:clearLocalBillData()
    self:initData()
    self:reqCurTime()
end

--获取时间
function PlayerBillModule:reqCurTime()
    self:startReq("ReqCurTime", XH.KW_CONFIG_LOGIC_TIME_OUT)
end

--获取当前时间回调
function PlayerBillModule:onReqCurTimeCallBack(req, type, data)
    if type == XH.Req.TYPE.TIMEOUT then
        print("reqCurTime FAILED")
        XH.SysTool.performDelayOnce(function()
            self:reqCurTime()
        end, 3)
    else
        self._curTime = req._curTime
        self._curBilldate = os.date("%Y-%m-%d", self._curTime)
        self:dispatchEvent({ name = PlayerBillModule.EVENT_HALL_BILL_UPDATE_CURTIME })
        self:readPlayerBillInfo(PlayerBillModule.SHOW_BILL_DAY)
    end
end

function PlayerBillModule:getCurTime()
    return self._curTime
end

--设置当前选中的日期字符串
function PlayerBillModule:setCurBillDateStr(sCurBillDate)
    self._curBilldate = sCurBillDate
end

function PlayerBillModule:getCurBillDateStr()
    return self._curBilldate
end

--区分是否第一次拉取， 第一次拉取100条，后续2条
function PlayerBillModule:readPlayerBillInfo(curBillTimeIndex)
    local sReqPlayerBillTime = XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_REQ_PLAYER_BILL_TIME_HALL, "")
    if sReqPlayerBillTime == "" then
        XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_DATA_REQ_PLAYER_BILL_TIME_HALL, self._curBilldate)
        self._bReqPlayerBillFirst = true
    else
        if sReqPlayerBillTime < self._curBilldate then
            self._bReqPlayerBillFirst = false
        else
            self._bReqPlayerBillFirst = not XH.userDefault:getValue(XH.userDefault.KEY_ID.KW_DATA_REQ_PLAYER_BILL_HALL, false, { date = self._curBilldate })
        end
    end
    local sReqCnt = self._bReqPlayerBillFirst and REQ_PLAYER_BILL_FIRST_CNT or REQ_PLAYER_BILL_NORAML_CNT
    local nReqTime = self._curTime -(PlayerBillModule.SHOW_BILL_DAY - curBillTimeIndex) * KW_DATA_TODAY_TIME
    self:reqPlayerBill(sReqCnt, nReqTime, XH.ActiveProtocol.ReqBoxLedger.REQFLAG.RPFG_DAYCNT)
end

--获取用户战绩
function PlayerBillModule:reqPlayerBill(reqBillCnt, nReqTime, flag, startIndex)
    local param = {}
    param.nDay = nReqTime
    param.nCnt = reqBillCnt or 0
    param.areaid = XH.areaData:getAreaID()
    param.numberid = XH.playerData:getNumberID()
    param.timeoutTime = XH.KW_CONFIG_LOGIC_TIME_OUT
    param.startIndex = startIndex or 0
    param.flag = flag or 0
    local reqType = "ReqPlayerBill"
    if flag == XH.ActiveProtocol.ReqBoxLedger.REQFLAG.RPFG_DAYCNT then
        reqType = "ReqPlayerBillCount"
    end
    XH.TipTool.showLoading()
    self:startReq(reqType, param)
end

function PlayerBillModule:onReqPlayerBillCount(req, type, data)
    XH.SysTool.performDelayOnce(function ()
        XH.TipTool.hideLoading() 
    end,0.3)
    if type == XH.Req.TYPE.SUCCESS then
        self._totalBillInfoCount = self._totalBillInfoCount or {}
        self._totalBillInfoCount[self._curBilldate] = self._totalBillInfoCount[self._curBilldate] or {}
        self._totalBillCount = data.totalCnt or 0

        if self._bReqPlayerBillFirst then
            self:reqPlayerBill(data.totalCnt, req._time)
            XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_DATA_REQ_PLAYER_BILL_HALL, true, { date = self._curBilldate})
        else
            local tableNotExistBill = {}
            for i= 1,self._totalBillCount do
                tableNotExistBill[i] = self._totalBillCount - i
            end
    
            self._totalBillInfo[self._curBilldate] = self._totalBillInfo[self._curBilldate] or {}
            self:readLocalBillData()

            local historyMaxIndex = 0  --最近一条重复请求，防止半条战绩的缓存，导致请求不到完整的账单
            for _,v in pairs(self._totalBillInfo[self._curBilldate] ) do
                if v.ordinalIndex then
                    tableNotExistBill[ v.ordinalIndex ] = nil
                    if v.ordinalIndex > historyMaxIndex then
                        historyMaxIndex = v.ordinalIndex
                    end
                end
            end

            if historyMaxIndex > self._totalBillCount then
                historyMaxIndex = self._totalBillCount
            end

            if self._totalBillCount > 0 then
                tableNotExistBill[historyMaxIndex] = self._totalBillCount - historyMaxIndex
            end
    
            self._totalNeedReqBill = self._totalNeedReqBill or {}   --需要再请求的账单
            self._totalNeedReqBill[self._curBilldate] = self._totalNeedReqBill[self._curBilldate] or {}
            self._totalNeedReqBill[self._curBilldate] = tableNotExistBill
    
            if #self._totalNeedReqBill[self._curBilldate] == 0  then
                self:reqPlayerBill(req._nCnt, req._time)
            else
                self:reqNextBill(REQ_PLAYER_BILL_NORAML_CNT, req._time)
            end
        end
    end
end

--请求下一条账单
function PlayerBillModule:reqNextBill(nCnt, nDay)
    XH.SysTool.performDelayOnce(function ()
        self._totalNeedReqBill[self._curBilldate] = self._totalNeedReqBill[self._curBilldate] or {}
        if self._totalBillCount <= 0 then
            return
        end
        for i = self._totalBillCount, 1, -1 do
            local index = self._totalNeedReqBill[self._curBilldate][i]
            if index then
                self:reqPlayerBill(nCnt, nDay, 0, index)
                return
            end
        end
    end,0.001)
end

function PlayerBillModule:onReqPlayerBill(req, type, data)
    XH.SysTool.performDelayOnce(function ()
        XH.TipTool.hideLoading()
    end,0.3)
    if type == XH.Req.TYPE.SUCCESS then
        self._totalBillInfo[self._curBilldate] = self._totalBillInfo[self._curBilldate] or {}
        for _,v in pairs(req._playerData) do
            self._totalNeedReqBill[self._curBilldate] = self._totalNeedReqBill[self._curBilldate] or {}
            self._totalBillInfo[self._curBilldate][#self._totalBillInfo[self._curBilldate]+1] = v
            if v.ordinalIndex then
                self._totalNeedReqBill[self._curBilldate][v.ordinalIndex] = nil
            end
        end

        self._totalBillInfo[self._curBilldate] = XH.TableTool.unique(self._totalBillInfo[self._curBilldate], true, "starttime", true)
        --如果是新战绩按时间排序
        local sortFun = function(a, b) return b.starttime < a.starttime end
        table.sort(self._totalBillInfo[self._curBilldate], sortFun)

        local ReqPlayerBill = require("lobby.Req.PlayerBill.ReqPlayerBill")
        local sFileName = ReqPlayerBill.getFileKeyByLobbyIdAndNumberId(XH.areaData:getLobbyID(), XH.playerData:getNumberID())
        self:writeBillInfo(self._totalBillInfo[self._curBilldate], self._curBilldate, sFileName) --重新写入
        self:reqNextBill(REQ_PLAYER_BILL_NORAML_CNT, req._time)
        self:dispatchEvent({ name = PlayerBillModule.EVENT_HALL_BILL_UPDATE_TOTALLIST })
    elseif type == XH.Req.TYPE.FAIL then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        }, req:getMessage())
    end
end

--获取当前选中的日期的战绩信息,如果传入GameId，则只返回对应游戏的战绩信息
function PlayerBillModule:getCurBillInfo(nGameId)
    local tResult
    if nGameId and nGameId > 0 then
        tResult = self:getCurBillInfoByGameId(nGameId)
    else
        tResult = self._totalBillInfo[self._curBilldate] or {}
    end
    
    return tResult
end

-- 获得选中的日期指定游戏的战绩数据
function PlayerBillModule:getCurBillInfoByGameId(nGameId)
    local tResult = {}
    local tCurBillInfo = self:getCurBillInfo()
    for _, v in pairs(tCurBillInfo) do
        if v.gameID == nGameId then
            tResult[#tResult + 1] = v
        end
    end
    return tResult
end

function PlayerBillModule:writeBillInfo(tBillInfo, nTime, sFileName)
    if not cc.FileUtils:getInstance():isDirectoryExist(cc.FileUtils:getInstance():getWritablePath() .. nTime) then
        cc.FileUtils:getInstance():createDirectory(cc.FileUtils:getInstance():getWritablePath() .. nTime)
    end
    local uFile = io.open(cc.FileUtils:getInstance():getWritablePath() .. nTime .. "/" .. sFileName, "wb")
    for _, var in pairs(tBillInfo) do
        local sWriteJSON = json.encode(var) --转换成json格式
        if uFile and type(var) == "table" then
            uFile:write(sWriteJSON .. "\n")
            uFile:flush()
        end
    end
    if uFile then
        uFile:close()
    end
end

--删除当前日期一天的数据
function PlayerBillModule:clearLocalBillData()
    local sFileName = self:getLocalBillDataFileName()
    local uFile = io.open(sFileName, "wb")
    if uFile then
        uFile:close()
    end

    XH.userDefault:setValue(XH.userDefault.KEY_ID.KW_DATA_REQ_PLAYER_BILL_HALL, false, { date = self._curBilldate })
end

function PlayerBillModule:getLocalBillDataFileName()
    local displayUserId = XH.playerData:getNumberID()
    local sCurUserKey = XH.areaData:getLobbyID() .. "_" .. displayUserId .. "_"
    local sFileName = cc.FileUtils:getInstance():getWritablePath() .. self._curBilldate .. "/" .. sCurUserKey .. KW_DATA_TEAHOUSE_LEDGER
    return sFileName
end

--读取本地某一天的数据
function PlayerBillModule:readLocalBillData()
    local tTmpPlayerData = {}
    local sFileName = self:getLocalBillDataFileName()
    self:fileReadPlayerBillInfo(sFileName, tTmpPlayerData)

    local curBillInfo = self:getCurBillInfo()
    if next(tTmpPlayerData) ~= nil then
        for _, v in pairs(tTmpPlayerData) do
            table.insert(curBillInfo, v)
        end
    end
end

--读取(时间为xxxx-xx-xx)
function PlayerBillModule:fileReadPlayerBillInfo(sFileName, tOutInfo)
    local uFile = io.open(sFileName, "rb")
    if uFile then
        local tCheckItem = {}
        tOutInfo = tOutInfo or {}
        for _, var in ipairs(tOutInfo) do
            tCheckItem[var.starttime .. "_" .. var.config] = _
        end
        for var in uFile:lines() do
            local tTemp = json.decode(var)
            if not tTemp then
                uFile:close()
                return
            end
            if tCheckItem[tTemp.starttime .. "_" .. tTemp.config] == nil then
                tOutInfo[#tOutInfo + 1] = tTemp
                tCheckItem[tTemp.starttime .. "_" .. tTemp.config] = #tOutInfo
            else
                local existKey = tCheckItem[tTemp.starttime .. "_" .. tTemp.config]
                tOutInfo[existKey] = tTemp
            end
        end
        uFile:close()
    end
end

--获取详情数据
function PlayerBillModule:fetchDetailDataByIdx(index, withDays, nGameId)
    local curBillInfo = self:getCurBillInfo()
    if nGameId then
        curBillInfo = self:getCurBillInfoByGameId(nGameId)
    end
    if withDays and withDays ~= 0 then
        curBillInfo = self:getCurBillInfoWithDays(withDays)
    end
    local tCurRoomInfo = curBillInfo[tonumber(index)]

    if tCurRoomInfo ~= nil then
        local sKey = self:convertToDetailBillInfoKey(tCurRoomInfo.gameID, tCurRoomInfo.appid, tCurRoomInfo.tableid, tCurRoomInfo.starttime)
        local curDetailBillInfo = self:getCurDetailBillInfoByKey(sKey)
        if curDetailBillInfo == nil then
            XH.TipTool.showLoading()
            self._curShowRoomInfo = tCurRoomInfo

            --开始网络请求
            local nTableID = tCurRoomInfo.tableid
            local nAppID = tCurRoomInfo.appid
            local nStartTime = tCurRoomInfo.starttime
            local nGameID = tCurRoomInfo.gameID
            self:startReq("ReqLedgerDetail", 10, nGameID, nAppID, nTableID, nStartTime)
        else
            return tCurRoomInfo, curDetailBillInfo
        end
    end
end

--获取详情数据回调
function PlayerBillModule:onReqBillInfoDetail(req, nType, vData)
    XH.TipTool.hideLoading()
    if nType == XH.Req.TYPE.SUCCESS then
        self:showBillDetail(vData)
    elseif nType == XH.Req.TYPE.TIMEOUT then
        XH.TipTool.showTip({
            type = XH.TIP_LAYER_TYPE.OK,
        },"REQUESET LedgerDetail TIMEOUT")
    end
end

--战绩详情返回
function PlayerBillModule:onBillInfoRespDetail(nXYID, oBuf, nLen)
    if nXYID ~= XH.ActiveProtocol.RespDetail.XY_ID then
        return
    end

    local tBillDetail = XH.ActiveProtocol.RespDetail:new()
    tBillDetail:bistream(oBuf, nLen)

    self:showBillDetail(tBillDetail)
end

function PlayerBillModule:showBillDetail(tBillDetail)
    local tCurRoomInfo = self._curShowRoomInfo

    --接收战绩详情的时候有可能为空，为空则表示不需要更新
    if tCurRoomInfo then
        local sKey = self:convertToDetailBillInfoKey(tCurRoomInfo.gameID, tCurRoomInfo.appid, tCurRoomInfo.tableid, tCurRoomInfo.starttime)
        self._detailBillInfo[self._curBilldate] = self._detailBillInfo[self._curBilldate] or {}
        self._detailBillInfo[self._curBilldate][sKey] = tBillDetail

        self:dispatchEvent({ name = PlayerBillModule.EVENT_HALL_BILL_UPDATE_BILLDETAIL, msg = { tCurRoomInfo = tCurRoomInfo, tBillDetail = tBillDetail } })
    end
end

--组合出DetailBill的key
function PlayerBillModule:convertToDetailBillInfoKey(sGameID, sAppID, sTableID, sStartTime)
    return string.format("%s_%s_%s_%s", sGameID, sAppID, sTableID, sStartTime)
end

--获取当前选中的日期的战绩详情，skey指定实际哪个
function PlayerBillModule:getCurDetailBillInfoByKey(sKey)
    self._detailBillInfo[self._curBilldate] = self._detailBillInfo[self._curBilldate] or {}
    return self._detailBillInfo[self._curBilldate][sKey]
end

--获取多日战绩信息,如果传入GameId，则只返回对应游戏的战绩信息
function PlayerBillModule:getCurBillInfoWithDays(nDays)
    local tResult = { }
    for i = 1, nDays do
        local nTime = self._curTime -(i - 1) * KW_DATA_TODAY_TIME
        local nCurDate = os.date("%Y-%m-%d", nTime)
        local tLinshiResult = self._totalBillInfo[nCurDate] or { }
        for j = 1, #tLinshiResult do
            tResult[#tResult + 1] = tLinshiResult[j] or { }
        end
    end
    return tResult
end

-- 获得多日指定游戏的战绩数据
function PlayerBillModule:getCurBillInfoWithDaysByGameId(nDays, nGameId)
    local tResult = {}
    local tCurBillInfo = self:getCurBillInfoWithDays(nDays)
    for _, v in pairs(tCurBillInfo) do
        if v.gameID == nGameId then
            tResult[#tResult + 1] = v
        end
    end
    return tResult
end


function PlayerBillModule:getBoxRoomGameListFinal()
    local boxRoomGameList = XH.areaData:getBoxRoomGameList()
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.HANGMAQUAN then
        --杭麻圈大厅的游戏比比赛场的少一个,不加上会导致杭麻圈查询比赛场的杭州麻将失败
        local isAdd = false
        for _, v in pairs(boxRoomGameList) do
            if v == 30145 then
                isAdd = true
            end
        end
        if not isAdd then
            table.insert( boxRoomGameList, 30145 )
        end
    end
    return  boxRoomGameList 
end

function PlayerBillModule:getAreaGameNameListFinal()
    local areaGameNameList = XH.areaData:getAreaGameNameList()
    local result = clone(areaGameNameList)
    if XH.areaData:getLobbyID() == XH.LOBBY_ID.HANGMAQUAN then
        for k, _ in pairs(areaGameNameList) do
            if k == 30145 then
                result[k] = XH.StringTool.replaceMatchStr("杭州麻将(亲友圈)")
            end
        end
    end
    return  result 
end

return PlayerBillModule
  �O  