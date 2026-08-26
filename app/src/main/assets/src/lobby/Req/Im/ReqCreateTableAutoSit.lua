---@class ReqCreateTableAutoSit : NetReq
local ReqCreateTableAutoSit = class("ReqCreateTableAutoSit", XH.ReqProtocol)

--领队/用户，玩家共用
function ReqCreateTableAutoSit:ctor()
    ReqCreateTableAutoSit.super.ctor(self)

    self._tableCnt = 0
    self._respPacketCnt = 0
    self._respTableCache = {}
end

function ReqCreateTableAutoSit:start(info, timeoutTime)
    if ReqCreateTableAutoSit.super.start(self, timeoutTime) == false then
        return
    end
    self._nTeaHousePwd = info.nTeaHousePwd
    self._teaHouseTitle = info.teaHouseTitle
    self._tableCnt = info.tableCnt
    --比赛场这边用的默认的srsgroupid，因为不考虑互通跨srs，考虑的话可以在通过类似于树列表的方式读取出来对应的srsgroupid，
    --然后在resp中设置对应的srsgroupid，对于已经在桌子上的玩家，这里可以设置一个全局变量GT._backBoxRoomSrsGroupId，供playerJoinRoom时使用
    --避免出现在比赛场中游戏，退到比赛场大厅时，在点击此比赛场的任意桌子或者创建时，没有任何请求导致不知道srsgroupid重连回去不对的情况
    --因为在其他比赛场创建会先返回大厅，再模拟进入比赛场，大厅的请求位置信息会记录相应的srsgroupid。
    --加入桌子和固定玩法模式同理
    local srsGroupID = XH.areaData:getSrsGroupID()
    local reqCreateTableAutoSit = XH.TeaHouseProtocol.ReqCreateTableAutoSit:new()
    reqCreateTableAutoSit.nGameId = info.gameid
    reqCreateTableAutoSit.nBase = info.base
    reqCreateTableAutoSit.nCount = info.cont
    reqCreateTableAutoSit.nType = info.type
    reqCreateTableAutoSit.sSize = info.size
    reqCreateTableAutoSit.acName = info.name
    reqCreateTableAutoSit.acRuleData = info.data
    reqCreateTableAutoSit.ucCreateType = info.ucCreateType
    reqCreateTableAutoSit.bNotAutoSit = info.bNotAutoSit
    reqCreateTableAutoSit.nPropID = XH.areaData:getPropRoomCardID()
    reqCreateTableAutoSit.acIdentify = XH.SysTool:GetDevid()
    reqCreateTableAutoSit.nOsVer = XH.SysTool.GetOsVersion()
    reqCreateTableAutoSit.nChannelId = XH.SysTool.getChannelID()
    reqCreateTableAutoSit.acHeadUrl = XH.playerData:getWeChatURL()
    reqCreateTableAutoSit.nClientType = XH.TeaHouseProtocol.ReqCreateTableAutoSit.CLIENTTYPE.MOBILE
    reqCreateTableAutoSit.nRoomVer = 0
    reqCreateTableAutoSit.nHardWareFlag = XH.TeaHouseProtocol.ReqCreateTableAutoSit.HARDWAREFLAG.HF_NONE
    reqCreateTableAutoSit.nModelIndex = info.nModelIndex
    reqCreateTableAutoSit.nTeaid =  info.nTeaid
    if info.nPayType ~= nil then
        reqCreateTableAutoSit.nPayType = info.nPayType
    end

    XH.netEngine:addProtocolScriptFuncByObj(self, self.onRespCreateTableAutoSit, reqCreateTableAutoSit.processid, srsGroupID)
    XH.netEngine:sendProtocol(reqCreateTableAutoSit, reqCreateTableAutoSit.processid, XH.areaData:getTeaHouseAppID(), srsGroupID)
end

function ReqCreateTableAutoSit:onRespCreateTableAutoSit(XYID, buff, len, processid)
    if XYID ~= XH.TeaHouseProtocol.RespCreateTableAutoSit.XY_ID then
        return
    end
    if processid ~= XH.TeaHouseProtocol.processid then
        return
    end
	local msgData = XH.TeaHouseProtocol.RespCreateTableAutoSit:new()
    msgData:bistream(buff, len)

    self._respPacketCnt = self._respPacketCnt + 1
    if msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.SUCCESS then
        self._respTableCache[#self._respTableCache + 1] = msgData
    else
        if msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.NOT_RIGHT then
            self:setMessage("创建失败,没有权限")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.MAXTABLE then
            self:setMessage("创建失败,已经达到创建上限")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.NOT_APPID then
            self:setMessage("没有appid")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.NOT_SETUP then
            self:setMessage("创建失败,没有对应局数配置")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.OTHER then
            self:setMessage("创建失败，其他的错误")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.NOT_PROP or
        msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.NOT_PROP_BY_GP then
            self:setMessage("房卡不足")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.NOT_TABLE then
            self:setMessage("没有桌子")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.NOT_ENOUGHPROP then
            self:setMessage("沒有足够的道具")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.MAX_CREAT_TABLE then
            self:setMessage("您创建的桌子数已经到达上限")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.NOENOUGH_PROP then
            self:setMessage("没有足够的普通道具")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.BAN then
            self:setMessage("您已被封禁对局，无法进入房间，如有疑问请联系客服。")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_DATA then
            self:setMessage("数据不正确")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.NO_TYPEID then
            self:setMessage("没有扣除道具类型id")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_INTABLE then
            self:setMessage("已经在桌子上")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_USEPROP then
            self:setMessage("使用道具失败")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_TABLEID then
            self:setMessage("错误的房间号")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_EMPTY_SEAT then
            self:setMessage("无法获取空位置")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_NOT_CREATE_TABLE then
            self:setMessage("未创建桌子")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_ALREADY_GAME then
            self:setMessage("已经开始游戏了")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_ALREADY_CREATE then
            self:setMessage("已经在创建桌子了")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_ALREADY_JOIN then
            self:setMessage("已经在加入桌子了")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_TABLE_FULL then
            self:setMessage("桌子已经满了")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_GPS_OPEN then
            self:setMessage("没开启定位,无法加入桌子")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.ERR_HAS_ROOM then
            self:setMessage("已经在房间里了,请先返回房间")
        elseif msgData.ucFlag == XH.TeaHouseProtocol.RespCreateTableAutoSit.FLAG.FAILED then
            self:setMessage("创建失败,其他错误")
        else
            self:setMessage("创建失败，错误码："..msgData.ucFlag)
        end
        self:fail(msgData)
        XH.NewThrowDataManager:reportCommonMonitor({
            Monitoring_source = "APP",
            Monitoring_type = "客户端监控比赛场创建房间失败",
            Monitoring_content = {
                info_des = self:getMessage() or "",
                code = msgData.ucFlag or -1,
                info = "创建失败"
            }
        })
        return
    end
    if self._respPacketCnt == self._tableCnt then
        self:success(self._respTableCache)
    end
end

return ReqCreateTableAutoSit   A   