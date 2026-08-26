local TeaHouseProtobuf = {}

TeaHouseProtobuf.processid = 116
TeaHouseProtobuf.packageName = "Zeus.hecaba.v1"
TeaHouseProtobuf.protobufFile = "hecaba_client.lua"

TeaHouseProtobuf.RECALL_STATUS = {
  INIT = 0,         -- 待完成
  DOING = 1,        -- 完成中
  DONE = 2,         -- 已完成/待领奖
  REWARD_DONE = 3,  -- 已领奖

  -- 召回活动2.0需求
  RECALL_GOT_REWARD = 5,  -- 已领奖 - 领奖后
  RECALL_RECALLED_OTHERS =
      6,  -- 被他人召回 - 召回仅登录(玩家已经通过其他人的召回链接登录回来)
  RECALL_RECALLED =
      7,  -- 已召回 - 召回仅登录(无奖励 AND 玩家已经通过本人的召回链接登录回来)
  RECALL_PLAY_REWARD = 8,  -- 对局奖励 - 召回仅登录(有奖励)
  RECALL_LOGIN_PLAY = 9,   -- 待领奖 - 已登录且已对局
}


TeaHouseProtobuf.errorCode = {
    [15000] = "密码错误请重试！",
    [15001] = "设置的密码与原密码相同",
    [15002] = "重置密码次数上限",
    [15003] = "密码错误请重试！",
    [15004] = "新老密码一样",
    [15010] = "会员权限已超时",
    [15011] = "没找到会员权限",
    [15012] = "会员权限已关闭",
    [15013] = "当前等级无奖励可领取",
    [15014] = "奖励已领取",
    [15015] = "没有转卡权限",
    [15016] = "奖励发放失败",
    [15017] = "道具不足",
    [15018] = "未绑定手机号",
    [15019] = "操作过于频繁",
    [15020] = "查找记录失败",
    [15021] = "超过时间不能回撤",
    [15022] = "房卡不足，回撤失败",
    [15023] = "获取转卡记录失败",

    [10004] = '库存不足，请联系客服', -- 库存不足
    [10005] = '领取次数上限，请联系客服', -- 领取次数上限
    [10006] = '奖励不存在，请联系客服', -- 奖励不存在
    [10007] = '奖励过期，请联系客服', --奖励过期
}

TeaHouseProtobuf.PageRecallPlayerRequest = {
    reqName = "PageRecallPlayerRequest",
    xyid = 1537,
    resqName = "PageRecallPlayerReply",
    data ={
        tenant_id = 1, -- 租户ID
        area_id = 2,   -- 区域ID
        club_id = 3,   -- 比赛场ID
        page_no = 4,   -- 分页页码
        page_size = 5, -- 分页大小
        user_type = 6, --召回用户类型
        away_user_type = 7, -- 流失用户类型
        away_days = 8, -- 流失天数
        ask_id = 9,
        user_id = 10,       --玩家ID(搜索功能)
        device_type = 11, -- 玩家设备类型
    }
}

TeaHouseProtobuf.AcceptRecallRewardsRequest = {
    reqName = "AcceptRecallRewardsRequest",
    xyid = 1541,
    resqName = "AcceptRecallRewardsReply",
    data ={
        tenant_id = 1, -- 租户ID
        area_id = 2,   -- 区域ID
        club_id = 3,   -- 比赛场ID
        user_id = 4,
        ask_id = 5,
        activity_task_id = 6, --大奖任务id
        prop_id = 7, --道具ID
        get_reward_user_id = 8, --领奖用户ID
        device_type = 9, -- 玩家设备类型
    }
}

TeaHouseProtobuf.HasRecallRewardsRequest = {
    reqName = "HasRecallRewardsRequest",
    xyid = 1545,
    resqName = "HasRecallRewardsReply",
    data ={
        tenant_id = 1,  -- 租户ID
        area_id = 2,    -- 区域ID
        club_id = 3,    -- 比赛场ID
        user_id = 4,    -- 玩家ID
        ask_id = 5,
    }
}

TeaHouseProtobuf.TokenRequest = {
    reqName = "TokenRequest",
    xyid = 1550,
    resqName = "TokenReply",
    data ={
        ask_id = 1,
        tenant_id = 2,  -- 租户id
        area_id = 3,    -- 地区id
        user_id = 4,    -- 用户id
        
    }
}

-- 请求vip信息 xyid=1601 返回vip信息 xyid=1602
TeaHouseProtobuf.GetVIPRequest = {
    reqName = "GetVIPRequest",
    xyid = 1601,
    resqName = "GetVIPReply",
    data ={
        ask_id = 0,
    }
}

-- 请求vip配置 xyid=1603 返回vip信息 xyid=1604
TeaHouseProtobuf.VIPConfigRequest = {
    reqName = "VIPConfigRequest",
    xyid = 1603,
    resqName = "VIPConfigReply",
    data ={
        ask_id = 0,
    }
}

-- 获取vip奖励 xyid=1605 返回vip奖励 xyid=1606
TeaHouseProtobuf.VIPGetAwardRequest = {
    reqName = "VIPGetAwardRequest",
    xyid = 1605,
    resqName = "VIPGetAwardReply",
    data ={
        ask_id = 0,
    }
}

-- 请求vip转卡 xyid=1607 返回vip转卡 xyid=1608
TeaHouseProtobuf.VIPTransCardRequest = {
    reqName = "VIPTransCardRequest",
    xyid = 1607,
    resqName = "VIPTransCardReply",
    data ={
        ask_id = 0,
        to_tenant_id = 0,
        to_area_id = 0,
        to_user_id = 0,
        prop_id = 0,
        count = 0,
        passwd = "",
    }
}

-- 请求vip密码状态 xyid=1609 返回vip密码状态 xyid=1610
TeaHouseProtobuf.VIPGetPasswdStateRequest = {
    reqName = "VIPGetPasswdStateRequest",
    xyid = 1609,
    resqName = "VIPGetPasswdStateReply",
    data ={
        ask_id = 0,
    }
}

-- 请求vip设置是否免密 xyid=1611 返回vip设置是否免密 xyid=1612
TeaHouseProtobuf.VIPSetNeedPasswdRequest = {
    reqName = "VIPSetNeedPasswdRequest",
    xyid = 1611,
    resqName = "VIPSetNeedPasswdReply",
    data ={
        ask_id = 0,
        pwd = "",
        need_passwd = true,
    }
}


-- 请求vip设置是否二次确认 xyid=1613 返回vip设置是否二次确认 xyid=1614
TeaHouseProtobuf.VIPSetNeedSureRequest = {
    reqName = "VIPSetNeedSureRequest",
    xyid = 1613,
    resqName = "VIPSetNeedSureReply",
    data ={
        ask_id = 0,
        need_sure_again = true,
    }
}

-- 请求vip设置修改密码 xyid=1615 返回vip设置修改密码 xyid=1616
TeaHouseProtobuf.VIPChangePasswdRequest = {
    reqName = "VIPChangePasswdRequest",
    xyid = 1615,
    resqName = "VIPChangePasswdReply",
    data ={
        ask_id = 0,
        old_pwd = "",
        new_pwd = "",
    }
}

-- 请求vip重置密码 xyid=1617 返回vip重置密码 xyid=1618
TeaHouseProtobuf.VIPResetPasswdRequest = {
    reqName = "VIPResetPasswdRequest",
    xyid = 1617,
    resqName = "VIPResetPasswdReply",
    data ={
        ask_id = 0,
    }
}

-- 更新玩家信息 xyid=1619 返回vip重置密码 xyid=1620
TeaHouseProtobuf.UpdatePlayerPropCountRequest = {
    reqName = "UpdatePlayerPropCountRequest",
    xyid = 1619,
    resqName = "UpdatePlayerPropCountReply",
    data ={
        ask_id = 0,
    }
}

-- 请求赠卡记录 xyid=1621 返回vip重置密码 xyid=1622
TeaHouseProtobuf.VIPTransCardLogsRequest = {
    reqName = "VIPTransCardLogsRequest",
    xyid = 1621,
    resqName = "VIPTransCardLogsReply",
    data ={
        ask_id = 0,
        oper_type = 0,
        cur_page = 0,
        page_size = 0,
    }
}

-- 请求回撤 xyid=1623 返回vip重置密码 xyid=1624
TeaHouseProtobuf.VIPRollbackTransCardRequest = {
    reqName = "VIPRollbackTransCardRequest",
    xyid = 1623,
    resqName = "VIPRollbackTransCardReply",
    data ={
        ask_id = 0,
        log_index = 0,
    }
}

TeaHouseProtobuf.GetPropsCountRequest = {
    reqName = "GetPropsCountRequest",
    xyid = 1701,
    resqName = "GetPropsCountReply",
    data ={
        ask_id = 1,
        tenant_id = 2,
        area_id = 3,
        user_id = 4,
        tea_id = 5,    
        props = {}
    }
}

-- 请求切牌限免 xyid=1547 返回次数 xyid=1548
TeaHouseProtobuf.CutCardsStateRequest = {
    reqName = "CutCardsStateRequest",
    xyid = 1547,
    resqName = "CutCardsStateReply",
    data ={
        tenant_id = 1, -- 租户ID
        area_id = 2,   -- 区域ID
        user_id = 3,   --玩家ID(搜索功能)
        state = 4,     -- 状态
    }
}

-- 请求奖励 xyid=1577 返回次数 xyid=1578
TeaHouseProtobuf.GoldRoomShareRewardRequest = {
    reqName = "GoldRoomShareRewardRequest",
    xyid = 1577,
    resqName = "GoldRoomShareRewardReply",
    data ={
        ask_id = 1,
        tenant_id = 2, -- 租户ID
        area_id = 3,   -- 区域ID
        user_id = 4,   --玩家ID(搜索功能)
        room_id = 5,     
        create_time = 6,        --
        score = 7,              --对局分数
        reward_type = 8,
        app_id = 9,             --桌子所在的进程id
        table_id = 10,           --桌子ID
    }
}

-- 请求奖励类型 xyid=1579 返回次数 xyid=1580
TeaHouseProtobuf.GetGoldRoomShareRewardCountRequest = {
    reqName = "GetGoldRoomShareRewardCountRequest",
    xyid = 1579,
    resqName = "GetGoldRoomShareRewardCountReply",
    data ={
        ask_id = 1,
        tenant_id = 2, -- 租户ID
        area_id = 3,   -- 区域ID
        user_id = 4,   --玩家ID(搜索功能)
        reward_type = 5,     
    }
}

TeaHouseProtobuf.GoldRoomShareResp = {
    SUCCESS = 0,     --成功
    COUNT_LIMIT = 1, --领取次数上限
    GAME_INVALID = 2,--对局校验失败
    ERROR =3,        --其他错误
}

TeaHouseProtobuf.GoldRoomShareType = {
    DOUBLE_WIN = 0,   --双倍胜利
    AVOID_DEFEAT = 1, --免败
}

-- 求财运道具-财运奖励类型
TeaHouseProtobuf.AwardType = {
    FirstAward = 0,
    NormalAward = 1,
    GoldDragonAward = 2,
}

-- 求财运道具-使用道具
TeaHouseProtobuf.TransferPropsRequest = {
    reqName = "TransferPropsRequest",
    xyid = 1749,
    resqName = "TransferPropsReply",
    data ={
        ask_id = 1,
        req = {
            tenant_id = 2,
            area_id = 3,
            costs = {},
            targets = {},
            params = "",
        },      -- 请求信息
        -- tenant_id = 2, -- 租户ID
        -- area_id = 3,   -- 区域ID
        -- params = "",     -- 扩展字段
        -- targets = {},    -- 金龙用
        -- costs = {},      -- 普通求财运道具
    }
}

-- 求财运道具-获取财运信息
TeaHouseProtobuf.CaiYunInfoRequest = {
    reqName = "CaiYunInfoRequest",
    xyid = 1583,
    resqName = "CaiYunInfoReply",
    data ={
        ask_id = 1,
        tenant_id = 2, -- 租户ID
        area_id = 3,   -- 区域ID
        user_id = 4,   -- 玩家ID
    }
}

-- 求财运道具-领取财运奖励
TeaHouseProtobuf.GetCaiYunAwardRequest = {
    reqName = "GetCaiYunAwardRequest",
    xyid = 1581,
    resqName = "GetCaiYunAwardReply",
    data ={
        ask_id = 1,
        tenant_id = 2, -- 租户ID
        area_id = 3,   -- 区域ID
        user_id = 4,   -- 玩家ID
        type = 5,      -- 奖励类型
    }
}

return TeaHouseProtobuf�)  