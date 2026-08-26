local LobbyConfig = {}

LobbyConfig.DEVICE_PLATFORM = {
    ALL = 0,
    ANDROID = 1,
    IOS = 2
}

LobbyConfig.ICON_TIP_TEXT_TYPE = {
    BOTTOM = 1,
    TOP = 2,
    RIGHT = 3
}

LobbyConfig.ICON_TIP_BUTTON_STYLE = {
    STYLE_ONE = 1,
}

LobbyConfig.RedPointResName = "menu_red_point.png"
LobbyConfig.TipTextConfig = {
    [LobbyConfig.ICON_TIP_TEXT_TYPE.BOTTOM] = {
        Font = "cocosStudio/Common/Font/fangzhengcuyuan.TTF",
        FontSize = 24,
        TextColor = cc.c3b(175, 112, 60),
        TextAnchorPoint = cc.p(0.5, 0.5),
        TextPositionY = 51,
        BgResName = "qiPao.png",
        AnchorPoint = cc.p(0,0),
        Scale9Enabled = true,
        CapInsets = cc.rect(100,50,17,3),
        OffSetX = 0,
    },
    [LobbyConfig.ICON_TIP_TEXT_TYPE.TOP] = {
        Font = "cocosStudio/Common/Font/fangzhengcuyuan.TTF",
        FontSize = 30,
        TextColor = cc.c3b(201, 90, 41),
        TextAnchorPoint = cc.p(0.5, 0.5),
        TextPositionY = -30,
        BgResName = "lobby_icon_tip_bg.png",
        AnchorPoint = cc.p(0,1),
        Scale9Enabled = true,
        CapInsets = cc.rect(125,125,70,70),
        OffSetX = 35,
    },
    [LobbyConfig.ICON_TIP_TEXT_TYPE.RIGHT] = {
        Font = "cocosStudio/Common/Font/fangzhengcuyuan.TTF",
        FontSize = 30,
        TextColor = cc.c3b(175, 112, 60),
        TextAnchorPoint = cc.p(0.5, 0.5),
        TextPositionY = 31,
        BgResName = "meun_icon_qi_pao.png",
        AnchorPoint = cc.p(1,0.5),
        Scale9Enabled = true,
        CapInsets = cc.rect(39,17,40,20),
        OffSetX = 0,
    },
}

LobbyConfig.TipNewsConfig = {
    ResName = "lobby_mail_tip_news.png",
    AnchorPoint = cc.p(0.5,0),
}

LobbyConfig.TipNewAwardConfig = {
    ResName = "lobby_mail_tip_new_award.png",
    AnchorPoint = cc.p(0.5,0),
}

-- LobbyConfig.TeaHouseAni = {
--     path = "animation/Lobby/Base/tea_join_in/",
--     ske = "ios_zj_cg_ske.json",
--     tex = "ios_zj_cg_tex.json",
--     armatureName = "Armature",
--     dragonBonesName = "ios_zj_cg",
--     animationName = "ios_dt_cg_ani"
-- }

-- LobbyConfig.JoinAni = {
--     path = "animation/Lobby/Base/box_join_in/",
--     ske = "ios_zj_jr_ske.json",
--     tex = "ios_zj_jr_tex.json",
--     armatureName = "Armature",
--     dragonBonesName = "ios_zj_jr",
--     animationName = "ios_dt_jrfj_ani"
-- }

-- LobbyConfig.GoldAni = {
--     path = "animation/Lobby/Base/gold_ani/",
--     ske = "Zjb_dt_xxc_ske.json",
--     tex = "Zjb_dt_xxc_tex.json",
--     armatureName = "Armature",
--     dragonBonesName = "Zjb_dt_xxc",
--     animationName = "newAnimation"
-- }

-- LobbyConfig.BackAni = {
--     path = "animation/Lobby/Base/create_join_in/",
--     ske = "ios_zj_cj_ske.json",
--     tex = "ios_zj_cj_tex.json",
--     armatureName = "Armature",
--     dragonBonesName = "ios_zj_cj",
--     animationName = "ios_dt_cj_ani"
-- }

LobbyConfig.MENUBAR_ID = {
    MAIN_TOP = 1,
    MAIN_BOTTOM_RIGHT = 2,
    MAIN_BOTTOM_LEFT = 3,
    MAIN_RIGHT = 4,
    MAIN_LEFT = 5,
    MAIN_MORE = 6,
    GOLD_TOP = 7,
    GOLD_BOTTOM_RIGHT = 8,
    GOLD_BOTTOM_LEFT = 9,
    GOLD_RIGHT = 10,
    GOLD_LEFT = 11,
    GOLD_MORE = 12,
    UNION_GAME = 13
}

LobbyConfig.ICON_POS_TYPE = {
    LOBBY_LONG = 1,
    LOBBY_SHORT = 2,
    GOLD_LONG = 3,
    GOLD_SHORT = 4,
}

LobbyConfig.ICON_DATA = {
    [1] = "match_long",
    [2] = "match_short",
    [3] = "gold_long",
    [4] = "gold_short",
}

-- 大厅Icon图标ID
LobbyConfig.ICON_ID = {
    SHANG_CHENG = 1001,         -- 商城
    HUO_DONG = 1002,            -- 活动
    GUI_ZE = 1003,              -- 规则
    GONG_GAO = 1004,            -- 公告
    ZHAN_JI = 1005,             -- 战绩
    SHE_ZHI = 1006,             -- 设置
    GENG_DUO = 1007,            -- 更多
    YAO_QING = 1008,            -- 邀请
    JIAN_KANG_XU_ZHI = 1010,    -- 健康须知
    FANG_DB = 1011,
    BANG_DING_SHOU_JI = 1012,   -- 绑定手机
    DUI_HUAN = 1015,            -- 兑奖
    FEN_XAING = 1017,           -- 分享
    YOU_JIAN = 1018,            -- 邮件
    KE_FU = 1019,               -- 客服
    BEI_BAO = 1021,             -- 背包
    ZHE_JIANG_XIN_WEN = 1022,   -- 新闻
    HAO_YOU_XI_TONG = 1023,     -- 好友系统
    TE_HUI_SHANG_CHENG = 1024,  -- 特惠商城
    WX_PUBLIC = 1025,           -- 公众号
    APP_UPDATE_TIP = 1026,      -- 更新提示

    ------------------------------活动开始------------------------------
    GENG_XIN_FU_LI = 2005,      -- 更新福利
    HUI_GUI_LI_BAO = 2006,      -- 回归礼包
    KAN_JIA_HUO_DONG = 2007,    -- 砍价活动
    MEI_RI_CHOU_JIANG = 2008,   -- 每日抽奖
    XIAN_SHI_YOU_HUI = 2010,    -- 限时优惠
    ZHE_KOU_LI_BAO = 2011,      -- 折扣礼包
    NATIONAL_DAY_ACT =  2012,   -- 国庆活动
    SHARE_HMQ_ACT =  2013,      -- 分享活动
    LUCKY_TURN_TABLE = 2014,    -- 幸运转盘活动
    TOPON_PH_FEE =  2016,       -- topon 话费
    CREATE_TEA_HOUSE = 2019,    -- 温茶引导创圈活动
    NEW_PALY_TYPE_C = 2020,     -- 新玩法活动C端

    ------------------------------联运开始------------------------------
    LIAN_YUN_ROOM = 3000,       --联运大厅
    JUE_ZHAN_SHA_YI = 3001,     -- 决战
    QIAN_PAO_BU_YU = 3002,      -- 千炮捕鱼
    YI_DAO_CHUAN_SHI = 3003,    -- 传奇
    NEW_VIP = 3004,             -- vip
    PRAY_ACT = 3009,            --祈福活动
    LAYOR_DAY_GIFT = 3010,      --51礼包
    BUSINESS_ACT = 3011,        --比赛场B端活动
    BU_YU_DA_JIANG_SAI = 3012,      --捕鱼大奖赛 
    FOUR_TO_TWO_TEA_POP = 3013,     --比赛场4转2
    DA_TIAN_SHI_ZHI_JIAN = 3014,      --大天使之剑
    LING_MENG_ZHI_BO = 3015,       --羚萌直播
    RE_XUE_FENG_SHEN = 3017,    --热血封神
    DOU_LUO_DA_LU = 3018,       --斗罗大陆
    JIE_JI_SAN_GUO = 3019,      --街机三国
    TEA_HOUSE_BATCH_INVITE = 3020,
    REN_REN_BU_YU = 3021,       --人人捕鱼
    REN_REN_BU_YU_1 = 30211,       --人人捕鱼
    REN_REN_BU_YU_2 = 30212,       --人人捕鱼
    SHEI_SHI_SHOU_FU = 3022,    --谁是首富
    PROMOTE_ACT = 3023,    --推广活动
    LUCKY_TASK = 3024,     --幸运任务
    SIGN_IN = 3025,     --签到有礼
    GOLDNEW_LZDDZ = 3026, --连炸斗地主
    GOLDNEW_LZDDZ_CENTER = 3027, --连炸斗地主中心ICON
    JINLONGJIANGLIN = 3028,      -- 金龙降临
    WEIJINGCHUANQI = 3029,      -- 维京传奇
    ZHUOYUECHUANSHUO = 3030,           -- 卓越传说
    YUANZHENGJIANGSHI = 3031,           -- 远征将士
    RONGYAOZHINU = 3032,           -- 荣耀之怒
    BAIZHANSHACHENG = 3033,           -- 百战沙城
    GOLD_NEW_BANKRUPT = 3034,           -- 破产礼包
    GOLD_NEW_DEFEAT = 3035,           -- 免赔礼包
    HUANXIANGMINGJIANGLU = 3036,
    AOYUN_ACT = 3037, -- 奥运活动
}

LobbyConfig.RedPointPos = {
    Small = cc.p(40,35),
    Big = cc.p(70,20),
}

-- 大厅Icon图标配置
-- Plist表示图标所在plist，如果图标无png资源，则不需要配置
-- IconResName表示png在plist中的资源名，如果不是plist资源则不需要配置
-- IsNeedRedPoint表示当前Icon是否需要红点
-- IsOpen表示游戏是否开启该Icon，true为开启，只有开启时各区配置该Icon才会显示
-- OffSetX,OffSetY表示图标是的位置偏移，若不需偏移则不需要配置
-- ProgeressInfo表示图标有进度条功能，如无进度条功能则不需要配置
LobbyConfig.MenuIconConfig = {
    [LobbyConfig.ICON_ID.SHANG_CHENG] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/shop-icon/shop-icon.ExportJson",
            ArmatureName = "shop-icon",
            AniName = "Animation1"
        },
        --icon增加扩展动画,在原有的基础上可以添加一个后期可操作显隐性的动画
        AniInfoEx = {
            File = "animation/GameCommon/shouzhi_ani/shouzhi_2.ExportJson",
            ArmatureName = "shouzhi_2",
            AniName = "Animation1",
            AniExScale = 0.5,
            OffSetX = 60,
            OffSetY = -20,
        },
    },
    [LobbyConfig.ICON_ID.GOLDNEW_LZDDZ_CENTER] = {
        IsOpen = true,
        SpineInfo = {
            JsonPath = "animation/Lobby/Base/lzddz_iconpanel/skeleton.json",
            AtlasPath = "animation/Lobby/Base/lzddz_iconpanel/skeleton.atlas",
            ArmatureName = "animation"
        },
        IconAniScale = 1.1,
        OffSetY = 5,
    },
    [LobbyConfig.ICON_ID.GOLDNEW_LZDDZ] = {
        IsOpen = false,
        SpineInfo = {
            JsonPath = "animation/Lobby/Base/lzddz_iconright/skeleton.json",
            AtlasPath = "animation/Lobby/Base/lzddz_iconright/skeleton.atlas",
            ArmatureName = "animation"
        },
        IconAniScale = 1.5,
    },
    [LobbyConfig.ICON_ID.HUO_DONG] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "huodong.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
    },
    [LobbyConfig.ICON_ID.GUI_ZE] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "guize.png",
        RedPointPos = LobbyConfig.RedPointPos.Small,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.GONG_GAO] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        MiniIconResName = "lobby_notice_btn.png",
        IconResName = "lobby_menu_notice.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.ZHAN_JI] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_menu_zhanji.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.SHE_ZHI] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_menu_shezhi.png",
        RedPointPos = LobbyConfig.RedPointPos.Small,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.GENG_DUO] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_menu_more.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.YAO_QING] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_menu_invate.png",
        RedPointPos = LobbyConfig.RedPointPos.Small,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.JIAN_KANG_XU_ZHI] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "jiankangxuzhi.png",
        RedPointPos = LobbyConfig.RedPointPos.Small,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.FANG_DB] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_fangdubo_btn.png",
        RedPointPos = LobbyConfig.RedPointPos.Small,
        IsOpen = false
    },
    [LobbyConfig.ICON_ID.BANG_DING_SHOU_JI] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "bangdingshouji_btn.png",
        RedPointPos = LobbyConfig.RedPointPos.Small,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.DUI_HUAN] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_menu_duihuan.png",
        RedPointPos = LobbyConfig.RedPointPos.Small,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.FEN_XAING] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_sharte_btn.png",
        RedPointPos = cc.p(70,20),
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.YOU_JIAN] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_menu_mail.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.KE_FU] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_serviec_btn.png",
        RedPointPos = LobbyConfig.RedPointPos.Small,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.BEI_BAO] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_bag_btn.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.ZHE_JIANG_XIN_WEN] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "btn_news.png",
        RedPointPos = LobbyConfig.RedPointPos.Small,
        IsOpen = true,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.HAO_YOU_XI_TONG] = {
        Plist = "cocosStudio/hall/Image/friends.plist",
        IconResName = "friend_icon_bell.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.TE_HUI_SHANG_CHENG] = {
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_personalGift.png",
        IsDefaultHide = true,
        IsOpen = true,
        OffSetY = 15
        -- DargonBonesInfo = {
        --     path = "animation/Lobby/Base/ios_zslb_icon/",
        --     ske = "ios_zslb_icon_ske.json",
        --     tex = "ios_zslb_icon_tex.json",
        --     armatureName = "Armature",
        --     dragonBonesName = "ios_zslb_icon",
        --     animationName = "newAnimation"
        -- },
        -- OffSetX = -10,
        -- OffSetY = -25
    },
    [LobbyConfig.ICON_ID.WX_PUBLIC] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_wxpublic_btn.png",
        IsOpen = true,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.APP_UPDATE_TIP] = {
        Plist = "cocosStudio/hall/Image/lobby.plist",
        IconResName = "lobby_app_update_btn.png",
        IsDefaultHide = true,
        IsOpen = true,
    },
    [LobbyConfig.ICON_ID.GENG_XIN_FU_LI] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        Plist = "cocosStudio/hall/Image/transferactivity/transferactivity.plist",
        IconResName = "transfer_mainentrance.png",
        IsDefaultHide = true,
        IsOpen = true,
    },
    [LobbyConfig.ICON_ID.HUI_GUI_LI_BAO] = {
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_backGift.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsDefaultHide = true,
        IsOpen = true,
    },
    [LobbyConfig.ICON_ID.CREATE_TEA_HOUSE] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsDefaultHide = true,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/mflk-icon/mflk-icon.ExportJson",
            ArmatureName = "mflk-icon",
            AniName = "Animation1"
        },
    },
    [LobbyConfig.ICON_ID.MEI_RI_CHOU_JIANG] = {
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_freeDraw.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsDefaultHide = true,
        IsOpen = false,
        OffSetY = 15,
        -- AniInfo = {
        --     File = "animation/Lobby/Base/mfcj-icon/mfcj-icon.ExportJson",
        --     ArmatureName = "mfcj-icon",
        --     AniName = "Animation1"
        -- }
    },
    [LobbyConfig.ICON_ID.XIAN_SHI_YOU_HUI] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsDefaultHide = true,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/xsyh-icon/xsyh-icon.ExportJson",
            ArmatureName = "xsyh-icon",
            AniName = "Animation1"
        }
    },
    [LobbyConfig.ICON_ID.ZHE_KOU_LI_BAO] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsDefaultHide = true,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/3zlb_icon/3zlb_icon.ExportJson",
            ArmatureName = "3zlb_icon",
            AniName = "Animation1"
        }
    },
    [LobbyConfig.ICON_ID.QIAN_PAO_BU_YU] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/zjb-by-icon/zjb-by-icon.ExportJson",
            ArmatureName = "zjb-by-icon",
            AniName = "Animation1"
        },
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.JUE_ZHAN_SHA_YI] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsDefaultHide = true,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/mr2_comeon_ani/mr2_comeon_ani.ExportJson",
            ArmatureName = "mr2_comeon_ani",
            AniName = "Animation1"
        },
        IconAniScale = 0.5,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.YI_DAO_CHUAN_SHI] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        -- AniInfo = {
        --     File = "animation/Lobby/Base/mir2_comeon_ani/mir2_comeon_ani.ExportJson",
        --     ArmatureName = "mir2_comeon_ani",
        --     AniName = "Animation1"
        -- },
        -- IconAniScale = 0.6,
        -- OffSetY = -5,
        Plist = "cocosStudio/hall/Image/GoldIcons.plist",
        IconResName = "lobby_xiuxian_icon_ydcs.png",
        IconScale = 0.9,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.NATIONAL_DAY_ACT] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsDefaultHide = true,
        IsOpen = true,
        DargonBonesInfo = {
            path = "animation/Lobby/Base/national_day_activity/gqhd-icon/",
            ske = "gqhd-icon_ske.json",
            tex = "gqhd-icon_tex.json",
            armatureName = "Armature",
            dragonBonesName = "gqhd-icon",
            animationName = "newAnimation"
        }
    },
    [LobbyConfig.ICON_ID.SHARE_HMQ_ACT] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_ShareForCard.png",
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/mffk-icon/mffk-icon.ExportJson",
            ArmatureName = "mffk-icon",
            AniName = "Animation1"
        },
    },
    [LobbyConfig.ICON_ID.LUCKY_TURN_TABLE] = {
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_turnTable.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.NEW_VIP] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/hyhl_icon/hyhl_icon.ExportJson",
            ArmatureName = "hyhl_icon",
            AniName = "Animation1"
        },
    },
    [LobbyConfig.ICON_ID.BU_YU_DA_JIANG_SAI] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/budjs-icon/budjs-icon.ExportJson",
            ArmatureName = "budjs-icon",
            AniName = "Animation1"
        },
        IconAniScale = 0.8,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    }, 
    [LobbyConfig.ICON_ID.DA_TIAN_SHI_ZHI_JIAN] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        DargonBonesInfo = {
            path = "animation/Lobby/Base/DTSZJ_ske/",
            ske = "DTSZJ_ske.json",
            tex = "DTSZJ_tex.json",
            armatureName = "Armature",
            dragonBonesName = "DTSZJ",
            animationName = "newAnimation"
        },
        IconAniScale = 0.7,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    }, 
    [LobbyConfig.ICON_ID.RE_XUE_FENG_SHEN] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        DargonBonesInfo = {
            path = "animation/Lobby/Base/ly-rxfs-icon/",
            ske = "ly-rxfs-icon_ske.json",
            tex = "ly-rxfs-icon_tex.json",
            armatureName = "armatureName",
            dragonBonesName = "ly-rxfs-icon",
            animationName = "Animation1"
        },
        IconAniScale = 0.6,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.DOU_LUO_DA_LU] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        DargonBonesInfo = {
            path = "animation/Lobby/Base/ly-dldl-icon/",
            ske = "icon_dldl_ske.json",
            tex = "icon_dldl_tex.json",
            armatureName = "Armature",
            dragonBonesName = "icon_dldl",
            animationName = "newAnimation"
        },
        IconAniScale = 0.6,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.JIE_JI_SAN_GUO] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        DargonBonesInfo = {
            path = "animation/Lobby/Base/ly-jjsg-icon/",
            ske = "icon_jjsg_ske.json",
            tex = "icon_jjsg_tex.json",
            armatureName = "Armature",
            dragonBonesName = "icon_jjsg",
            animationName = "newAnimation"
        },
        IconAniScale = 0.6,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.REN_REN_BU_YU] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        --IsDefaultHide = true,
        IsOpen = true,
        DargonBonesInfo = {
            path = "animation/Lobby/Base/rrby_icon/",
            ske = "zjb_zrfkby_icon_ske.json",
            tex = "zjb_zrfkby_icon_tex.json",
            armatureName = "Armature",
            dragonBonesName = "zjb_zrfkby_icon",
            animationName = "newAnimation"
        },
        IconAniScale = 0.62,
        ProgeressInfo = {
            Plist = "cocosStudio/hall/Image/rrbuyu.plist",
            markResName = "btn_rrby_black1.png",
            barResName = "btn_rrby_black2.png",
            markOffset = cc.p(0, 10),
            barOffset = cc.p(0, 10),
            bReverseDirection = true,
            TextFont = "cocosStudio/Common/Font/fangzhengcuyuan.TTF",
            TextFontSize = 26,
            TextColor = cc.c3b(255, 255, 255),
            TextAnchorPoint = cc.p(0.5, 0.5),
            TextOffset = cc.p(0, -40),
        },
        -- Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.REN_REN_BU_YU_1] = {
        Plist = "cocosStudio/hall/Image/LobbyIconRRBY.plist",
        IconResName = "lobby_icon_rrby_1.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        ProgeressInfo = {
            Plist = "cocosStudio/hall/Image/rrbuyu.plist",
            markResName = "btn_rrby_black1.png",
            barResName = "btn_rrby_black2.png",
            markOffset = cc.p(0, 10),
            barOffset = cc.p(0, 10),
            bReverseDirection = true,
            TextFont = "cocosStudio/Common/Font/fangzhengcuyuan.TTF",
            TextFontSize = 26,
            TextColor = cc.c3b(255, 255, 255),
            TextAnchorPoint = cc.p(0.5, 0.5),
            TextOffset = cc.p(0, -40),
        },
        IsOpen = true,
        -- Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.REN_REN_BU_YU_2] = {
        Plist = "cocosStudio/hall/Image/LobbyIconRRBY.plist",
        IconResName = "lobby_icon_rrby_2.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        ProgeressInfo = {
            Plist = "cocosStudio/hall/Image/rrbuyu.plist",
            markResName = "btn_rrby_black1.png",
            barResName = "btn_rrby_black2.png",
            markOffset = cc.p(0, 10),
            barOffset = cc.p(0, 10),
            bReverseDirection = true,
            TextFont = "cocosStudio/Common/Font/fangzhengcuyuan.TTF",
            TextFontSize = 26,
            TextColor = cc.c3b(255, 255, 255),
            TextAnchorPoint = cc.p(0.5, 0.5),
            TextOffset = cc.p(0, -40),
        },
        IsOpen = true,
        -- Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.SHEI_SHI_SHOU_FU] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        DargonBonesInfo = {
            path = "animation/Lobby/Base/sssf-icon/",
            ske = "sssf-icon_ske.json",
            tex = "sssf-icon_tex.json",
            armatureName = "armatureName",
            dragonBonesName = "sssf-icon",
            animationName = "Animation1"
        },
        IconAniScale = 0.6,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.PROMOTE_ACT] = {
      IsOpen = true,
      IsDefaultHide = true,
      IconAniScale = 1,
      DargonBonesInfo = {
          path = "cocosStudio/hall/Image/Activity/Promote/qxwf_icon/",
          ske = "qxwf_icon_ske.json",
          tex = "qxwf_icon_tex.json",
          armatureName = "Armature",
          dragonBonesName = "qxwf_icon",
          animationName = "newAnimation"
      },
    },
    [LobbyConfig.ICON_ID.LUCKY_TASK] = {
      IsOpen = true,
      IsDefaultHide = true,
      Plist = "cocosStudio/hall/Image/Activity/LuckTask/lucky_task.plist",
      IconResName = "lucky_task_icon_1.png",
    },
    [LobbyConfig.ICON_ID.SIGN_IN] = {
        IsOpen = false,
        IsDefaultHide = true,
        Plist = "cocosStudio/hall/Image/Activity/Signin/lobby_sign_in.plist",
        IconResName = "sign_in_act_icon.png",
        RedPointPos = cc.p(60,40),
    },
    [LobbyConfig.ICON_ID.TOPON_PH_FEE] = {
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_pheeAct.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsDefaultHide = true,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/lhf-icon-ani/lhf-icon-ani.ExportJson",
            ArmatureName = "lhf-icon-ani",
            AniName = "Animation1"
        }
    },
    [LobbyConfig.ICON_ID.PRAY_ACT] = {
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_prayAct.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsDefaultHide = false,
        IsOpen = true,
        OffSetY = 15
    },
    [LobbyConfig.ICON_ID.LAYOR_DAY_GIFT] = {
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_LaborGift.png",
        IsDefaultHide = true,
        IsOpen = true,
        IconAniScale = 1,
        DargonBonesInfo = {
            path = "animation/Lobby/Base/LaborDay/",
            ske = "icon_qinglong_ske.json",
            tex = "icon_qinglong_tex.json",
            armatureName = "Armature",
            dragonBonesName = "icon_qinglong",
            animationName = "newAnimation"
        }
    },
    [LobbyConfig.ICON_ID.BUSINESS_ACT] = {
        Plist = "cocosStudio/TeaHouse/Image/teahouse_remodel_act_icon.plist",
        IconResName = "RecallAct_icon.png",
        RedPointPos = cc.p(50,30),
        IsOpen = true,
    },
    [LobbyConfig.ICON_ID.FOUR_TO_TWO_TEA_POP] = {
        Plist = "cocosStudio/TeaHouse/Image/teahouse_remodel_act_icon.plist",
        IconResName = "four_to_two_pop_icon.png",
        RedPointPos = cc.p(50,30),
        IsOpen = true,
        iconSize = 1,
    },
    [LobbyConfig.ICON_ID.LING_MENG_ZHI_BO] = {
        IsDefaultHide = true,
        IsOpen = true,
        AniInfo = {
            File = "animation/Lobby/Base/tczb_ani/tczb-icon.ExportJson",
            ArmatureName = "tczb-icon",
            AniName = "Animation1"
        },
        IconAniScale = 1,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.TEA_HOUSE_BATCH_INVITE] = {
        Plist = "cocosStudio/hall/Image/TeaBatchInvite/lobby_tea_batch_invite.plist",
        IconResName = "teahouse_batch_invite_icon.png",
        IsOpen = true,
        iconSize = 1,
    },
    [LobbyConfig.ICON_ID.NEW_PALY_TYPE_C] = {
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_new_play_type_c.png",
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.JINLONGJIANGLIN] = {
        Plist = "cocosStudio/hall/Image/Activity/activity_icons.plist",
        IconResName = "activityIcon_jinlong.png",
        IsOpen = true
    },
    [LobbyConfig.ICON_ID.WEIJINGCHUANQI] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        Plist = "cocosStudio/hall/Image/GoldIcons.plist",
        IconResName = "lobby_xiuxian_icon_wjcq.png",
        IconScale = 0.78,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },

    [LobbyConfig.ICON_ID.ZHUOYUECHUANSHUO] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        Plist = "cocosStudio/hall/Image/GoldIcons.plist",
        IconResName = "lobby_game_ly_810019.png",
        IconScale = 0.78,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.YUANZHENGJIANGSHI] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        Plist = "cocosStudio/hall/Image/GoldIcons.plist",
        IconResName = "lobby_game_ly_810020.png",
        IconScale = 0.78,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.RONGYAOZHINU] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        Plist = "cocosStudio/hall/Image/GoldIcons.plist",
        IconResName = "lobby_game_ly_810023.png",
        IconScale = 0.78,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.BAIZHANSHACHENG] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        IconScale = 0.78,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID,
        SpineInfo = {
            JsonPath = "animation/Lobby/Base/bzsc/jiezhi.json",
            AtlasPath = "animation/Lobby/Base/bzsc/jiezhi.atlas",
            ArmatureName = "animation"
        },
    },
    [LobbyConfig.ICON_ID.GOLD_NEW_BANKRUPT] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        IconScale = 1,
        Plist = "cocosStudio/hall/Image/Ygold/Ygold.plist",
        IconResName = "jfyl_1.png",
    },
    [LobbyConfig.ICON_ID.GOLD_NEW_DEFEAT] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        IconScale = 1,
        Plist = "cocosStudio/hall/Image/Ygold/Ygold.plist",
        IconResName = "mplb_1.png",
    },
    [LobbyConfig.ICON_ID.HUANXIANGMINGJIANGLU] = {
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        Plist = "cocosStudio/hall/Image/GoldIcons.plist",
        IconResName = "lobby_game_ly_810027.png",
        IconScale = 0.78,
        Platform = LobbyConfig.DEVICE_PLATFORM.ANDROID
    },
    [LobbyConfig.ICON_ID.AOYUN_ACT] = {
        IsDefaultHide = true,
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
        Plist = "cocosStudio/hall/Image/aoyun/aoyunact.plist",
        IconResName = "aoyun_Img_iocn.png",
    },
}

if device.platform == "ios" then
    LobbyConfig.MenuIconConfig[LobbyConfig.ICON_ID.REN_REN_BU_YU] = {
        Plist = "cocosStudio/hall/Image/GoldIcons.plist",
        IconResName = "lobby_xiuxian_renrenbuyu.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
    }
    LobbyConfig.MenuIconConfig[LobbyConfig.ICON_ID.REN_REN_BU_YU_1] = {
        Plist = "cocosStudio/hall/Image/GoldIcons.plist",
        IconResName = "lobby_xiuxian_renrenbuyu.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
    }
    LobbyConfig.MenuIconConfig[LobbyConfig.ICON_ID.REN_REN_BU_YU_2] = {
        Plist = "cocosStudio/hall/Image/GoldIcons.plist",
        IconResName = "lobby_xiuxian_renrenbuyu.png",
        RedPointPos = LobbyConfig.RedPointPos.Big,
        IsOpen = true,
    }
end

LobbyConfig.TipButtonConfig = {
    [LobbyConfig.ICON_TIP_BUTTON_STYLE.STYLE_ONE] = {
        Font = "cocosStudio/Common/Font/fangzhengcuyuan.TTF",
        FontSize = 30,
        TextColor = cc.c3b(201, 90, 41),
        TextAnchorPoint = cc.p(0.5, 0.5),
        TextPositionY = 38,
        NormalResName = "_11111_lobby_icon_know_btn.png",
        PressedResName = "_11111_lobby_icon_know_btn.png",
        DisabledResName = "_11111_lobby_icon_know_btn.png",
        AnchorPoint = cc.p(0.5, 1),
        Scale9Enabled = true,
        BtnScale = 1.2,
        BtnPosY = 10,
        OffSetX = 0,
    },
}

LobbyConfig.MenuIconTipImageStyle = {
    STYPE_ONE = 1,
}

LobbyConfig.MenuIconTipImage = {
    [LobbyConfig.MenuIconTipImageStyle.STYPE_ONE] = {
        resName = "lobby_icon_bird.png",
        AnchorPoint = cc.p(0, 0.5),
        PositionX = -90,
        Scale = 1.2,
        OffSetX = 35,
    }
}

LobbyConfig.GoldIcons = {
    [30116] = {
        {
            Text = "双扣",
            Name = "lobby_xiuxian_shuangkou.png",
            Bg = "lobby_xiuxian_bg_sk.png"
        }
    },
    [30310] = {
        {
            Text = "广式麻将",
            Name = "lobby_xiuxian_guangshimajiang.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        }
    },
    [30329] = {
        {
            Text = "龙港麻将",
            Name = "lobby_xiuxian_longgangmajiang.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        }
    },
    [30401] = {
        {
            Text = "三门玩法",
            Name = "lobby_xiuxian_sanmenmajiang.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        }
    },
    [30312] = {
        {
            Text = "龙泉麻将",
            Name = "lobby_xiuxian_longquanmajiang.png",
            Bg = "lobby_xiuxian_bg_mj_2.png"
        }
    },
    [30386] = {
        {
            Text = "跑得快",
            Name = "lobby_xiuxian_new_cygp.png",
            Bg = "lobby_xiuxian_bg_pdk.png"
        }
    },
    [30421] = {
        {
            Text = "斗地主玩法",
            Name = "lobby_xiuxian_doudizhu.png",
            Bg = "lobby_xiuxian_bg_ddz.png" 
        }
    },
    [30379] = {
        {
            Text = "二人广式",
            Name = "lobby_xiuxian_errenguangshi.png",
            Bg = "lobby_xiuxian_bg_mj_3.png"
        }
    },
    [30117] = {
        {
            Text = "二人温麻",
            Name = "lobby_xiuxian_errenwenma.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        },
        {
            Text = "温州麻将",
            Name = "lobby_xiuxian_wenzhoumajiang.png",
            Bg = "lobby_xiuxian_bg_mj_2.png"
        }
    },
    [30332] = {
        {
            Text = "台炮玩法",
            Name = "lobby_xiuxian_taipaomajiang.png",
            Bg = "lobby_xiuxian_bg_mj_3.png"
        }
    },
    [30159] = {
        {
            Text = "杭州双扣",
            Name = "lobby_xiuxian_new_cysk.png",
            Bg = "lobby_btn_game_sk_2.png"
        }
    },
    [30134] = {
        {
            Text = "杭州玩法",
            Name = "lobby_xiuxian_hangzhoumajiang.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        },
        {
            Text = "二人麻将",
            Name = "lobby_xiuxian_errenmajiang.png",
            Bg = "lobby_xiuxian_bg_mj_2.png"
        }
    },
    [30205] = {
        {
            Text = "温岭玩法",
            Name = "lobby_xiuxian_wenlingmajiang.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        }
    },
    [30206] = {
        {
            Text = "玉环玩法",
            Name = "lobby_xiuxian_yuhuanmajiang.png",
            Bg = "lobby_xiuxian_bg_mj_2.png"
        }
    },
    [30400] = {
        {
            Text = "台州麻将",
            Name = "lobby_xiuxian_taizhoumajiang.png",
            Bg = "lobby_xiuxian_bg_mj_3.png"
        }
    },
    [30450] = {
        {
            Text = "定海玩法",
            Name = "lobby_xiuxian_dinghaimajiang.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        }
    },
    [30451] = {
        {
            Text = "沈家门玩法",
            Name = "lobby_xiuxian_shenjiamenmajiang.png",
            Bg = "lobby_xiuxian_bg_mj_2.png"
        }
    },
    [30123] = {
        {
            Text = "湖州麻将",
            Name = "lobby_xiuxian_huzhoumajiang.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        }
    },
    [30124] = {
        {
            Text = "长兴玩法",
            Name = "lobby_xiuxian_changxingmajiang.png",
            Bg = "lobby_xiuxian_bg_mj_2.png"
        }
    },
    [30185] = {
        {
            Text = "安吉麻将",
            Name = "lobby_xiuxian_anjimajiang.png",
            Bg = "lobby_xiuxian_bg_mj_3.png"
        }
    },
    [30275] = {
        {
            Text = "瑞安麻将",
            Name = "lobby_xiuxian_ruianmajiang.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        }
    },
    [30276] = {
        {
            Text = "点炮麻将",
            Name = "lobby_xiuxian_dianpaomajiang.png",
            Bg = "lobby_xiuxian_bg_mj_2.png"
        }
    },
    [30277] = {
        {
            Text = "洞头麻将",
            Name = "lobby_xiuxian_dongtoumajiang.png",
            Bg = "lobby_xiuxian_bg_mj_3.png"
        }
    },
    [30224] = {
        {
            Text = "乐清麻将",
            Name = "lobby_xiuxian_yueqingmajiang.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        }
    },
    [30179] = {
        {
            Text = "二人麻将",
            Name = "lobby_xiuxian_errenmajiang1.png",
            Bg = "lobby_xiuxian_bg_mj_3.png"
        },
        {
            Text = "瞎子麻将",
            Name = "lobby_xiuxian_new_cjmj.png",
            Bg = "lobby_xiuxian_bg_mj_2.png"
        },
        {
            Text = "亮子麻将",
            Name = "lobby_xiuxian_new_cjmj.png",
            Bg = "lobby_xiuxian_bg_mj.png"
        }
    },
}

LobbyConfig.TempGuanDanGameID = 999999
LobbyConfig.GoldNewGameID = 42210 -- 新金币场

LobbyConfig.LBGameID = 30227 -- 两帮，特殊gameid，用的老包的代码

return LobbyConfig  �  