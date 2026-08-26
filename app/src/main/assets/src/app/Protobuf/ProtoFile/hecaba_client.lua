return [[
syntax = "proto3";

package Zeus.hecaba.v1;

//processid = 116

message CommonResp {
    int32 err_code = 1;
    string err_reason = 2;
    int32 ask_id = 3;
}

//单个奖励道具的配置
message AwardPropConfig {
    int32 prop_id = 1;//道具id
    int32 count = 2;//道具数量
    int32 time_round = 3;//道具领取周期
    string url = 4;//道具图片地址
}

//vip道具的配置
message VIPConfig {
  int32 vip_id = 1;//vip等级id
  string name = 2;//vip等级名称
  int32 condition = 3;//vip等级达成条件
  string condition_desc = 4;//vip条件描述
  bool can_trans_prop = 5;//是否可以转卡
  bool is_open = 6;//是否开启奖励
  bool have_award = 7;//是否可领取奖励
  int32 add_sr = 8;//金币奖励数值
  int32 award_type = 9;//奖励类型
  repeated AwardPropConfig award_prop = 10;//道具信息
  string desc = 11;//其它描述内容
}


message VIPTransCardLog {
  int64 log_index = 1;     // 数据号，每条记录的唯一识别id
    int32 user_id = 2;       // 玩家id
    int32 area_id = 3;       // 地域id
    int32 tenant_id = 4;     // 租户id
    int32 prop_id = 5;       // 道具id
    int32 prop_cnt = 6;      // 道具数量
    int32 to_user_id = 7;    // 转让给的玩家id
    string to_user_name = 8; // 转让给的玩家名字
    int32 oper_type = 9;     // 操作类型,1:赠送,2:收到
    int32 oper_state = 10;   // 转让状态：0：默认状态,1:已撤回,2:已失效
    int64 oper_time = 11;    // 转让时间
}

// 道具信息
message PropInfo {
  int32 prop_id = 1;    //道具id
  int32 prop_cnt = 2;   //道具数量
}

// 奖励信息
message AwardInfo {
  int32 prop_id = 1;    //道具id
  int32 prop_cnt = 2;   //道具数量
  string award_url = 3;  //道具url
}

// 召回状态
enum RECALL_STATUS {
  INIT = 0;         // 待完成
  DOING = 1;        // 完成中
  DONE = 2;         // 已完成/待领奖
  REWARD_DONE = 3;  // 已领奖

  // 召回活动2.0需求
  RECALL_GOT_REWARD = 5;  // 已领奖 - 领奖后
  RECALL_RECALLED_OTHERS =
      6;  // 被他人召回 - 召回仅登录(玩家已经通过其他人的召回链接登录回来)
  RECALL_RECALLED =
      7;  // 已召回 - 召回仅登录(无奖励 AND 玩家已经通过本人的召回链接登录回来)
  RECALL_PLAY_REWARD = 8;  // 对局奖励 - 召回仅登录(有奖励)
  RECALL_LOGIN_PLAY = 9;   // 待领奖 - 已登录且已对局
}

message Rewards {
  int32 id = 1;
  int32 count = 2;
  string icon = 3;
  string name = 4;
}

message Player {
  int32 user_id = 1;                 // 玩家ID
  string nick_name = 2;              // 玩家昵称
  string head_icon = 3;              // 玩家头像图片地址
  int64 last_login_time = 4;         // 上次登录时间(秒)
  int64 last_play_time = 5;          // 上次对局时间(秒)
  int64 quit_time = 6;               // 退出比赛场时间(秒)
  int64 recall_time = 7;             // 召回邀请时间(秒)
  int64 recalled_time = 8;           // 召回时间(秒)
  int32 play_count = 9;              // 当日对局数
  RECALL_STATUS recall_status = 10;  // 召回状态
  int32 tenant_id = 11;
  int32 area_id = 12;
  int32 club_id = 13;
  string task_status_tips = 14;  // 状态提示
  string task_reward_tips = 15;  // 奖励提示
  // 2.0新增
  bool hot = 16;                  // 标记"热"
  int32 successRate = 17;         // 成功率
  repeated Rewards rewards = 18;  // 普通玩家奖励
  int64 expire_at = 19;           // 领奖过期时间, 时间戳(秒)
  int32 away_days = 20;           // 流失天数
  int32 from_user_id = 21;        // 召回发起玩家
  int32 club_owner = 22;          // 领队ID
  Rewards ownerRewards = 23;      // 领队/副领队奖励
  map<int32, int32> got_reward_players =
      24;                      // 已领奖玩家ID列表 map<玩家id, 1|0>
  bool in_charge = 25;         // 领队是否在B端充值中
  int64 first_play_time = 26;  // 召回后首次对局时间
  repeated int32 club_managers = 27;  // 副领队列表
}

//--分页获取玩家召回列表--//
// xyid=1537
message PageRecallPlayerRequest {
  int32 tenant_id = 1;                // 租户ID
  int32 area_id = 2;                  // 区域ID
  int32 club_id = 3;                  // 比赛场ID
  int32 page_no = 4;                  // 分页页码
  int32 page_size = 5;                // 分页大小
  RECALL_USER_TYPE user_type = 6;     // 召回用户类型
  AWAY_USER_TYPE away_user_type = 7;  // 流失用户类型
  int32 away_days = 8;                // 流失天数
  int32 ask_id = 9;
  int32 user_id = 10;           // 玩家ID(搜索功能)
  DeviceType device_type = 11;  // 玩家设备类型
}
// xyid=1538
message PageRecallPlayerReply {
  repeated Player player = 1;  // 玩家信息列表
  int32 curPage = 2;           // 当前页码
  int32 totalPage = 3;         // 总页数
  CommonResp cr = 4;
  int32 inaction_count = 5;  // 不活跃玩家总数
}

// 召回用户类型
enum RECALL_USER_TYPE {
  ACTIVITY = 0;  // 召回活动用户
  AWAY = 1;      // 常驻功能流失用户
  QUIT = 2;      // 常驻功能退圈用户
  AWAY2 = 3;     // 常驻功能-流失用户召回2.0 目前只有 这个值有用
}

// 流失用户维度定义
enum AWAY_USER_TYPE {
  NOT_LOGIN = 0;  // N天未登录
  NOT_PLAY = 1;   // N天未对局
  QUIT_CLUB = 2;  // 退出比赛场天数
}

enum DeviceType {
  AC_DEFAULT = 0;  // 默认没传
  AC_IOS = 1;      // ios
  AC_AZ = 2;       // 安卓
  AC_H5 = 3;       // h5
}


//--领取奖励--//
// xyid=1541
message AcceptRecallRewardsRequest {
  int32 tenant_id = 1;  // 租户ID
  int32 area_id = 2;    // 区域ID
  int32 club_id = 3;    // 比赛场ID
  int32 user_id = 4;    // 玩家ID
  int32 ask_id = 5;
  int32 activity_task_id = 6;    // 大奖任务id
  int32 prop_id = 7;             // 道具ID
  int32 get_reward_user_id = 8;  // 领奖用户ID
  DeviceType device_type = 9;    // 玩家设备类型
}
// xyid=1542
message AcceptRecallRewardsReply {
  CommonResp cr = 1;
}

//--召回是否有奖励--//
// xyid=1545
message HasRecallRewardsRequest {
  int32 tenant_id = 1;  // 租户ID
  int32 area_id = 2;    // 区域ID
  int32 club_id = 3;    // 比赛场ID
  int32 user_id = 4;    // 玩家ID
  int32 ask_id = 5;
}

// xyid=1546
message HasRecallRewardsReply {
  bool has = 1;            // 是否有可领取奖励
  int32 recall_count = 2;  // 可召回玩家数
  CommonResp cr = 3;
  bool is_club_manager =
      4;  // 是否比赛场副领队，用于大厅召回，req clubId为0的时候才有效
}

// xyid=1550
message TokenRequest{
  int32 ask_id = 1;
  int32 tenant_id = 2;          //租户id
  int32 area_id = 3;            //地区id
  int32 user_id = 4;            //用户id
}

// xyid=1551
message TokenReply{
  CommonResp cr = 1;
  int32 area_id = 2;            //地区id
  int32 user_id = 3;            //用户id
  string token = 4;             //jwt token
}

//请求vip信息
//xyid=1601
message GetVIPRequest {
    int32 ask_id = 1;
}

//返回vip信息
//xyid=1602
message GetVIPReply {
    CommonResp resp = 1;
  int32 vip_id = 2;
  int64 start_time = 3;
  int64 end_time = 4;
  int32 vip_award_state = 5;     //0：未领取，其他都是已领取
}

//请求vip配置
//xyid=1603
message VIPConfigRequest {
    int32 ask_id = 1;
}

//返回vip信息
//xyid=1604
message VIPConfigReply {
    CommonResp resp = 1;
    repeated VIPConfig vip_config = 2;
    int64 start_time = 3;
    int64 end_time = 4;
}

//获取vip奖励
//xyid=1605
message VIPGetAwardRequest {
    int32 ask_id = 1;
}

//返回vip奖励
//xyid=1606
message VIPGetAwardReply {
  CommonResp resp = 1;
  repeated AwardInfo awardInfo = 2;  //领奖信息
}

//请求vip转卡
//xyid=1607
message VIPTransCardRequest {
    int32 ask_id = 1;
    int32 to_tenant_id = 2;
    int32 to_area_id = 3;
    int32 to_user_id = 4;
    int32 prop_id = 5;
    int32 count = 6;
    string passwd = 7;
}

//返回vip转卡
//xyid=1608
message VIPTransCardReply {
    CommonResp resp = 1;
}

//请求vip密码状态
//xyid=1609
message VIPGetPasswdStateRequest {
    int32 ask_id = 1;
}

//返回vip密码状态
//xyid=1610
message VIPGetPasswdStateReply {
    CommonResp resp = 1;
    bool need_sure_again=2;
    bool need_passwd = 3;
    int32 reset_count = 4;
    int32 reset_max_count = 5;
}

//请求vip设置是否免密
//xyid=1611
message VIPSetNeedPasswdRequest {
    int32 ask_id = 1;
    string pwd = 2;
    bool need_passwd = 3;
}

//返回vip设置是否免密
//xyid=1612
message VIPSetNeedPasswdReply {
    CommonResp resp = 1;
}

//请求vip设置是否二次确认
//xyid=1613
message VIPSetNeedSureRequest {
    int32 ask_id = 1;
    bool need_sure_again=2;
}

//返回vip设置是否二次确认
//xyid=1614
message VIPSetNeedSureReply {
    CommonResp resp = 1;
}

//请求vip设置修改密码
//xyid=1615
message VIPChangePasswdRequest {
    int32 ask_id = 1;
    string old_pwd = 2;
    string new_pwd = 3;
}

//返回vip设置修改密码
//xyid=1616
message VIPChangePasswdReply {
    CommonResp resp = 1;
}

//请求vip重置密码
//xyid=1617
message VIPResetPasswdRequest {
    int32 ask_id = 1;
}

//返回vip重置密码
//xyid=1618
message VIPResetPasswdReply {
    CommonResp resp = 1;
}

//更新玩家信息
//xyid=1619
message UpdatePlayerPropCountRequest {
  int32 ask_id = 1;
}

//返回更新玩家信息
//xyid=1620
message UpdatePlayerPropCountReply {
  CommonResp resp = 1;
  repeated PropInfo propInfo = 2;  //道具信息
}

//请求赠卡记录
//xyid=1621
message VIPTransCardLogsRequest {
  int32 ask_id = 1;
  int32 oper_type = 2;      // 操作类型,0：全部,1:赠送,2:收到
  int32 cur_page = 3;   // 当前页数
  int32 page_size = 4;      // 每页数量
}

//xyid=1622
message VIPTransCardLogsReply {
    CommonResp resp = 1;
    int32 cur_page = 2;                       // 当前页数
    int32 total_page = 3;                       // 总页数
    int64 log_expire_time = 4;                    // 记录有效时间
    int64 rollback_expire_time = 5;                 // 记录可回撤时间
    repeated VIPTransCardLog logs = 6;    // 日志列表
}

//请求回撤
//xyid=1623
message VIPRollbackTransCardRequest {
  int32 ask_id = 1;
  int64 log_index = 2;                  // 数据号，每条记录的唯一识别id
}

//xyid=1624
message VIPRollbackTransCardReply {
    CommonResp resp = 1;
    int64 log_index = 2;                // 操作的数据号，每条记录的唯一识别id
    int32 oper_state = 3;     // 转让状态：0：默认状态,1:已撤回,2:已失效
}

//钱包类型
enum BurseType {
  BurseTypeNotUse = 0;  //不使用
  User = 1;             //用户道具
  Tea = 2;              //比赛场道具
  AgentUser = 3;        //用户用户道具
  TeaVirtual = 4;       //比赛场虚拟道具
  Agent = 5;            //用户卡
  UserTime = 6;         //时效道具 有个数属性
  UserTimeNoCount = 7;  //时效道具 无个数属性
  TeaTime = 8;          //比赛场时效道具 有个数属性
  TeaTimeNoCount = 9;   //比赛场时效道具 无个数属性
}

//道具
message PropInfo1 {
  int32 prop_id = 1 [(validate.rules).int32.gte = 0];  //道具id
  int32 count = 2;                                     //道具数量
  BurseType burse_type = 3
      [(validate.rules).enum.defined_only = true];       //钱包类型
  int32 time = 4 [(validate.rules).int32.gte = 0];       //道具到期时间
  int64 count_big = 5;                                   //道具数量int64
  int32 zero_time = 6 [(validate.rules).int32.gte = 0];  //耗光时间
  int32 expire = 7 [(validate.rules).int32.gte = 0];     //期限
  int32 freeze_type = 8;                                 //冻结类型
  int32 balance = 9;                                //道具余额
  int32 old_count = 10;                            //道具数量原值
}

//道具数量查询
//xyid=1701
message GetPropsCountRequest {
  int32 ask_id = 1;                     // ask
  int32 tenant_id = 2;                  //租户id
  int32 area_id = 3;                    //地区id
  int32 user_id = 4;                    //用户id
  int32 tea_id = 5;                     //比赛场id
  repeated PropInfo1 props = 6;  //道具id
}

//道具数量查询
//xyid=1702
message GetPropsCountReply {
  CommonResp resp = 1;                  //通用返回
  repeated PropInfo1 props = 2;  //道具信息
}

//xyid=1547
message CutCardsStateRequest{
    int32 tenant_id = 1;          //租户id
    int32 area_id = 2;            //地区id
    int32 user_id = 3;            //用户id
    int32 state = 4;              //请求状态，1为获取，2为写入
}

//xyid=1548
message CutCardsStateReply{
    int32 count = 1;               //次数
}

//xyid=1577
message GoldRoomShareRewardRequest{
    int32 ask_id = 1;
    int32 tenant_id = 2;          //租户id
    int32 area_id = 3;            //地区id
    int32 user_id = 4;            //用户id
    int32 room_id = 5;            //房间id
    int64 create_time = 6;        //
    int32 score = 7;              //对局分数
    GoldRoomShareType reward_type = 8;
    int32 app_id = 9;             //桌子所在的进程id
    int32 table_id = 10;           //桌子ID
}

//xyid=1578
message GoldRoomShareRewardReply{
    CommonResp cr = 1;
    GoldRoomShareResp resp = 2;   //返回码
    int32 num = 3;                //发送金币数量
}

//xyid=1579
message GetGoldRoomShareRewardCountRequest{
    int32 ask_id = 1;
    int32 tenant_id = 2;          //租户id
    int32 area_id = 3;            //地区id
    int32 user_id = 4;            //用户id
    GoldRoomShareType reward_type = 5;
}

//xyid=1580
message GetGoldRoomShareRewardCountReply{
    CommonResp cr = 1;
    int32 count = 2;               //次数
}

enum GoldRoomShareResp {
    SUCCESS = 0;     //成功
    COUNT_LIMIT = 1; //领取次数上限
    GAME_INVALID = 2;//对局校验失败
    ERROR =3;        //其他错误
}

enum GoldRoomShareType {
    DOUBLE_WIN = 0;   //双倍胜利
    AVOID_DEFEAT = 1; //免败
}

// 求财运
message TransferPropsRequestInfo {
    int32 tenant_id = 1;  //租户id
    int32 area_id = 2;                 //地区id
    repeated OwnerInfo costs = 3;  //消耗列表
    repeated OwnerInfo targets = 4;  //转换目标
    string params = 5;              //自定义参数
}
message TransferPropsReplyInfo {
    repeated OwnerInfo owners = 1;  //剩余
}

//道具拥有者
message OwnerInfo {
    int32 owner_id = 1;  //道具拥有者id，例：大厅卡操作，拥有者为uid;比赛场卡则为 teaid
    repeated TransferPropInfo props = 2;  //道具列表
    SubIdInfo sub_ids = 3;                           //附加id
}

//道具
message TransferPropInfo {
    int32 prop_id = 1;  //道具id
    int32 count = 2;                                     //道具数量
    BurseType burse_type = 3;       //钱包类型
    int32 time = 4;       //道具到期时间
    int64 count_big = 5;                                   //道具数量int64
    int32 zero_time = 6;  //耗光时间
    int32 expire = 7;     //期限
    int32 freeze_type = 8;                                 //冻结类型
    int32 balance = 9;                                //道具余额
    int32 old_count = 10;                            //道具数量原值
}

//附加id处理
message SubIdInfo {
    int32 user_id = 1;        //用户id
    int32 tea_id = 2;         //比赛场id
    int32 agent_user_id = 3;  //用户用户id
    int32 agent_area_id = 4;  //用户区id
    int32 game_id = 5;        //游戏id
    int32 channel_id = 6;     //渠道id
}

// 求财运使用道具
//xyid=1749
message TransferPropsRequest {
  int32 ask_id = 1;            // ask
  TransferPropsRequestInfo req = 2;
}

//xyid=1750
message TransferPropsReply {
  CommonResp resp = 1;        //通用返回
  TransferPropsReplyInfo reply = 2;  //道具余额
}

// 获取财运信息
// xyid=1583
message CaiYunInfoRequest{
  int32 ask_id = 1;
  int32 tenant_id = 2;          //租户id
  int32 area_id = 3;            //地区id
  int32 user_id = 4;            //用户id
}
// xyid=1584
message CaiYunInfoReply{
  CommonResp cr = 1;
  int32 area_id = 2;            //地区id
  int32 user_id = 3;            //用户id
  int32 cai_cur = 4; // 财当前值
  int32 cai_max = 5; // 财最大值
  int32 yun_cur = 6;
  int32 yun_max = 7;
  int32 comm_point = 8; // 当前普通积分
  int32 gold_point = 9; // 当前金龙积分
  int32 first_award_point = 10; // 首次奖励需要的积分（金龙）
  int32 comm_award_point = 11; // 普通奖励需要的积分
  int32 gold_award_point = 12; // 金龙奖励需要的积分
  bool is_first_award = 13; // 是否领取首次奖励
  repeated CaiYunConf conf_list = 14;
}

message CaiYunConf {
    int32 prop_id = 1; //道具ID
    int32 cai = 2;  // 给的财气值
    int32 yun = 3;
}

//财运奖励类型
enum AwardType {
    FirstAward = 0;
    NormalAward = 1;
    GoldDragonAward = 2;
}
message GetCaiYunAwardRequest {
    int32 ask_id = 1;
    int32 tenant_id = 2;          //租户id
    int32 area_id = 3;            //地区id
    int32 user_id = 4;            //用户id
    AwardType type = 5;           //奖励类型
}
message GetCaiYunAwardReply{
    CommonResp cr = 1;
    repeated CaiYunPropInfo prop_info = 2;
}

message CaiYunPropInfo {
    int32 id = 1;
    int32 count = 2;
    int64 expire = 3;
}

]]   �F  