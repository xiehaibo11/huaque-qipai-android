package com.nanbeiyule.game.mahjong.protocol;

/**
 * All {@code XY_ID} message id constants of the original Zhejiang lobby 1.5.4
 * Taizhou mahjong (gameid 30109) protocol chain
 * BasicMahjong → BasicTaiZhouMahjong → TaiZhouMahjong.
 *
 * <p>{@code $M} below abbreviates
 * {@code artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong}.
 * Line numbers point at the {@code XY_ID = ...} statement of each recovered Lua
 * message table.
 *
 * <p>The original reuses three ids for two different messages across the
 * inheritance chain; both names are kept and the collision is documented, never
 * "fixed": 1045 (msgGameMaxFan/msgTaiName), 1049 (msgBetResult/msgShengPaiCnt),
 * 1053 (msgBaseScore/msgTestSingleTingMah).
 */
public final class MahjongMessageId {
    // ---- BasicMahjong layer ($M/BasicMahjong/Protocols/GameProtocol.luac) ----

    /** {@code msgPlayerTimer} 玩家定时器（品牌/数字/秒/权限/位置）。$M/BasicMahjong/Protocols/GameProtocol.luac:5 */
    public static final int MSG_PLAYER_TIMER = 9;
    /** {@code msgBaseClientForwardEx} 客户端转发扩展（语音/GPS/表情等）。$M/BasicMahjong/Protocols/GameProtocol.luac:33 */
    public static final int MSG_BASE_CLIENT_FORWARD_EX = 22;
    /** {@code msgStartGame} 开局。$M/BasicMahjong/Protocols/GameProtocol.luac:128 */
    public static final int MSG_START_GAME = 513;
    /** {@code msgEndGame} 结束。$M/BasicMahjong/Protocols/GameProtocol.luac:146 */
    public static final int MSG_END_GAME = 514;
    /** {@code msgRelinkEnter} 断线重连进入。$M/BasicMahjong/Protocols/GameProtocol.luac:164 */
    public static final int MSG_RELINK_ENTER = 515;
    /** {@code msgLookerEnter} 旁观者进入。$M/BasicMahjong/Protocols/GameProtocol.luac:182 */
    public static final int MSG_LOOKER_ENTER = 516;
    /** {@code msgTrust} 托管标志。$M/BasicMahjong/Protocols/GameProtocol.luac:200 */
    public static final int MSG_TRUST = 517;
    /** {@code msgPower} 操作权限。$M/BasicMahjong/Protocols/GameProtocol.luac:227 */
    public static final int MSG_POWER = 518;
    /** {@code msgGameStep} 游戏阶段。$M/BasicMahjong/Protocols/GameProtocol.luac:251 */
    public static final int MSG_GAME_STEP = 519;
    /** {@code msgClock} 座位时钟。$M/BasicMahjong/Protocols/GameProtocol.luac:269 */
    public static final int MSG_CLOCK = 520;
    /** {@code msgEndWait} 结束等待。$M/BasicMahjong/Protocols/GameProtocol.luac:296 */
    public static final int MSG_END_WAIT = 521;
    /** {@code msgSpecfReq} 旁观请求。$M/BasicMahjong/Protocols/GameProtocol.luac:323 */
    public static final int MSG_SPECF_REQ = 522;
    /** {@code msgSpecfData} 旁观数据。$M/BasicMahjong/Protocols/GameProtocol.luac:348 */
    public static final int MSG_SPECF_DATA = 523;
    /** {@code msgSpecfPower} 旁观权限。$M/BasicMahjong/Protocols/GameProtocol.luac:371 */
    public static final int MSG_SPECF_POWER = 524;
    /** {@code msgSpecfHand} 旁观手牌。$M/BasicMahjong/Protocols/GameProtocol.luac:391 */
    public static final int MSG_SPECF_HAND = 525;
    /** {@code msgSpecfDanFang} 旁观单放。$M/BasicMahjong/Protocols/GameProtocol.luac:426 */
    public static final int MSG_SPECF_DAN_FANG = 526;
    /** {@code msgSpecfWall} 旁观牌墙。$M/BasicMahjong/Protocols/GameProtocol.luac:446 */
    public static final int MSG_SPECF_WALL = 527;
    /** {@code msgSpecfEnd} 旁观结束。$M/BasicMahjong/Protocols/GameProtocol.luac:468 */
    public static final int MSG_SPECF_END = 528;
    /** {@code msgWallMah} 牌墙状态。$M/BasicMahjong/Protocols/GameProtocol.luac:492 */
    public static final int MSG_WALL_MAH = 529;
    /** {@code msgOpenWall} 开墙翻牌。$M/BasicMahjong/Protocols/GameProtocol.luac:520 */
    public static final int MSG_OPEN_WALL = 530;
    /** {@code msgThrowChip} 掷骰。$M/BasicMahjong/Protocols/GameProtocol.luac:541 */
    public static final int MSG_THROW_CHIP = 532;
    /** {@code msgTakeFirst} 首次抓牌标志。$M/BasicMahjong/Protocols/GameProtocol.luac:566 */
    public static final int MSG_TAKE_FIRST = 533;
    /** {@code msgPlayerMah} 玩家牌面。$M/BasicMahjong/Protocols/GameProtocol.luac:584 */
    public static final int MSG_PLAYER_MAH = 534;
    /** {@code msgPlayerBack} 玩家牌背。$M/BasicMahjong/Protocols/GameProtocol.luac:651 */
    public static final int MSG_PLAYER_BACK = 535;
    /** {@code msgJoker} 财神及替代牌。$M/BasicMahjong/Protocols/GameProtocol.luac:744 */
    public static final int MSG_JOKER = 536;
    /** {@code msgReplace} 补花/补牌。$M/BasicMahjong/Protocols/GameProtocol.luac:776 */
    public static final int MSG_REPLACE = 537;
    /** {@code msgTake} 抓牌。$M/BasicMahjong/Protocols/GameProtocol.luac:803 */
    public static final int MSG_TAKE = 538;
    /** {@code msgPlay} 出牌。$M/BasicMahjong/Protocols/GameProtocol.luac:841 */
    public static final int MSG_PLAY = 539;
    /** {@code msgCancel} 取消操作。$M/BasicMahjong/Protocols/GameProtocol.luac:869 */
    public static final int MSG_CANCEL = 540;
    /** {@code msgHu} 胡牌。$M/BasicMahjong/Protocols/GameProtocol.luac:889 */
    public static final int MSG_HU = 541;
    /** {@code msgHuEx} 一炮多响。$M/BasicMahjong/Protocols/GameProtocol.luac:914 */
    public static final int MSG_HU_EX = 542;
    /** {@code msgAction} 吃碰杠动作。$M/BasicMahjong/Protocols/GameProtocol.luac:942 */
    public static final int MSG_ACTION = 543;
    /** {@code msgPanData} 盘/圈/局数据。$M/BasicMahjong/Protocols/GameProtocol.luac:1000 */
    public static final int MSG_PAN_DATA = 544;
    /** {@code msgTurnData} 回合数据。$M/BasicMahjong/Protocols/GameProtocol.luac:1032 */
    public static final int MSG_TURN_DATA = 545;
    /** {@code msgFlower} 花牌。$M/BasicMahjong/Protocols/GameProtocol.luac:1069 */
    public static final int MSG_FLOWER = 546;
    /** {@code msgOutMah} 已出牌列表。$M/BasicMahjong/Protocols/GameProtocol.luac:1094 */
    public static final int MSG_OUT_MAH = 547;
    /** {@code msgFanCnt} 番种统计。$M/BasicMahjong/Protocols/GameProtocol.luac:1121 */
    public static final int MSG_FAN_CNT = 548;
    /** {@code msgEndResult} 结算字符串。$M/BasicMahjong/Protocols/GameProtocol.luac:1148 */
    public static final int MSG_END_RESULT = 549;
    /** {@code msgTWait} 等待。$M/BasicMahjong/Protocols/GameProtocol.luac:1166 */
    public static final int MSG_T_WAIT = 550;
    /** {@code msgJustWaiting} 正在等待。$M/BasicMahjong/Protocols/GameProtocol.luac:1190 */
    public static final int MSG_JUST_WAITING = 551;
    /** {@code msgWaiting} 等待。$M/BasicMahjong/Protocols/GameProtocol.luac:1208 */
    public static final int MSG_WAITING = 552;
    /** {@code msgPlayLmts} 出牌限制。$M/BasicMahjong/Protocols/GameProtocol.luac:1226 */
    public static final int MSG_PLAY_LMTS = 553;
    /** {@code msgObviousMahsData} 明牌数据。$M/BasicMahjong/Protocols/GameProtocol.luac:1251 */
    public static final int MSG_OBVIOUS_MAHS_DATA = 555;
    /** {@code msgOutMahRefresh} 出牌刷新。$M/BasicMahjong/Protocols/GameProtocol.luac:1285 */
    public static final int MSG_OUT_MAH_REFRESH = 556;
    /** {@code msgAllOutMahRefresh} 全部出牌刷新。$M/BasicMahjong/Protocols/GameProtocol.luac:1310 */
    public static final int MSG_ALL_OUT_MAH_REFRESH = 557;
    /** {@code msgReqShuffle} 请求换座。$M/BasicMahjong/Protocols/GameProtocol.luac:1803 */
    public static final int MSG_REQ_SHUFFLE = 559;
    /** {@code msgShuffleSeats} 换座。$M/BasicMahjong/Protocols/GameProtocol.luac:1827 */
    public static final int MSG_SHUFFLE_SEATS = 560;
    /** {@code msgShuffleFinish} 换座完成。$M/BasicMahjong/Protocols/GameProtocol.luac:1884 */
    public static final int MSG_SHUFFLE_FINISH = 561;
    /** {@code msgTingMahInfo} 听牌信息。$M/BasicMahjong/Protocols/GameProtocol.luac:1908 */
    public static final int MSG_TING_MAH_INFO = 562;
    /** {@code msgToTalShuffle} 全部换座结果。$M/BasicMahjong/Protocols/GameProtocol.luac:1851 */
    public static final int MSG_TO_TAL_SHUFFLE = 563;
    /** {@code msgAllThrowChip} 全部掷骰结果。$M/BasicMahjong/Protocols/GameProtocol.luac:1947 */
    public static final int MSG_ALL_THROW_CHIP = 564;
    /** {@code msgTest} 测试。$M/BasicMahjong/Protocols/GameProtocol.luac:1334 */
    public static final int MSG_TEST = 1025;
    /** {@code msgResult} 单局结算。$M/BasicMahjong/Protocols/GameProtocol.luac:1352 */
    public static final int MSG_RESULT = 1026;
    /** {@code msgSpeak} 语音说话。$M/BasicMahjong/Protocols/GameProtocol.luac:1373 */
    public static final int MSG_SPEAK = 1028;
    /** {@code msgServicePay} 服务费。$M/BasicMahjong/Protocols/GameProtocol.luac:1405 */
    public static final int MSG_SERVICE_PAY = 1033;
    /** {@code msgEndType} 结束类型。$M/BasicMahjong/Protocols/GameProtocol.luac:1423 */
    public static final int MSG_END_TYPE = 1034;
    /** {@code msgRoomHostSeat} 房主座位。$M/BasicMahjong/Protocols/GameProtocol.luac:1443 */
    public static final int MSG_ROOM_HOST_SEAT = 1035;
    /** {@code msgPlayCount} 已玩局数。$M/BasicMahjong/Protocols/GameProtocol.luac:1467 */
    public static final int MSG_PLAY_COUNT = 1036;
    /** {@code msgGameRule} 游戏规则串。$M/BasicMahjong/Protocols/GameProtocol.luac:1487 */
    public static final int MSG_GAME_RULE = 1037;
    /** {@code msgTotalResult} 总结算。$M/BasicMahjong/Protocols/GameProtocol.luac:1511 */
    public static final int MSG_TOTAL_RESULT = 1038;
    /** {@code msgRequestDismiss} 请求解散。$M/BasicMahjong/Protocols/GameProtocol.luac:1550 */
    public static final int MSG_REQUEST_DISMISS = 1039;
    /** {@code msgRespondDismiss} 回应解散。$M/BasicMahjong/Protocols/GameProtocol.luac:1574 */
    public static final int MSG_RESPOND_DISMISS = 1040;
    /** {@code msgDismissFlag} 解散标志。$M/BasicMahjong/Protocols/GameProtocol.luac:1600 */
    public static final int MSG_DISMISS_FLAG = 1041;
    /** {@code msgAvatarUrl} 玩家头像。$M/BasicMahjong/Protocols/GameProtocol.luac:1625 */
    public static final int MSG_AVATAR_URL = 1042;
    /** {@code msgClientForward} 客户端转发。$M/BasicMahjong/Protocols/GameProtocol.luac:1653 */
    public static final int MSG_CLIENT_FORWARD = 1043;
    /** {@code msgGameMaxFan} 最大番。$M/BasicMahjong/Protocols/GameProtocol.luac:1696 */
    public static final int MSG_GAME_MAX_FAN = 1045;
    /** {@code msgResultExtInfo} 结算扩展信息。$M/BasicMahjong/Protocols/GameProtocol.luac:1716 */
    public static final int MSG_RESULT_EXT_INFO = 1046;
    /** {@code msgFollowMah} 跟牌。$M/BasicMahjong/Protocols/GameProtocol.luac:1736 */
    public static final int MSG_FOLLOW_MAH = 1047;
    /** {@code msgBetResult} 下注结果。$M/BasicMahjong/Protocols/GameProtocol.luac:1763 */
    public static final int MSG_BET_RESULT = 1049;
    /** {@code msgBaseScore} 底分与倍率。$M/BasicMahjong/Protocols/GameProtocol.luac:89 */
    public static final int MSG_BASE_SCORE = 1053;
    /** {@code msgGameRuleUser} 用户规则串。$M/BasicMahjong/Protocols/GameProtocol.luac:1974 */
    public static final int MSG_GAME_RULE_USER = 1100;

    // ---- BasicTaiZhouMahjong layer ($M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac) ----

    /** {@code msgTaiName} 台数名称；与 msgGameMaxFan 共用 1045。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:158 */
    public static final int MSG_TAI_NAME = 1045;
    /** {@code msgWallCnt} 牌墙剩余数。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:182 */
    public static final int MSG_WALL_CNT = 1048;
    /** {@code msgShengPaiCnt} 剩牌数；与 msgBetResult 共用 1049。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:199 */
    public static final int MSG_SHENG_PAI_CNT = 1049;
    /** {@code msgLeftBanker} 剩余庄家数。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:278 */
    public static final int MSG_LEFT_BANKER = 1050;
    /** {@code msgShuffleSeatsTZ} 台州换座。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:297 */
    public static final int MSG_SHUFFLE_SEATS_TZ = 1052;
    /** {@code msgTestSingleTingMah} 单吊听牌测试；与 msgBaseScore 共用 1053。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:371 */
    public static final int MSG_TEST_SINGLE_TING_MAH = 1053;
    /** {@code msgAdvanceStart} 提前开局（2/3 人）。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:401 */
    public static final int MSG_ADVANCE_START = 1200;
    /** {@code msgReqAdvanceStart} 请求提前开局。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:422 */
    public static final int MSG_REQ_ADVANCE_START = 1201;
    /** {@code msgAdvanceStartFlag} 提前开局标志。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:449 */
    public static final int MSG_ADVANCE_START_FLAG = 1202;
    /** {@code msgReqAdPlayerAgree} 提前开局玩家同意。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:468 */
    public static final int MSG_REQ_AD_PLAYER_AGREE = 1203;
    /** {@code msgDynamicTableChangeSeat} 动态桌换座。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:497 */
    public static final int MSG_DYNAMIC_TABLE_CHANGE_SEAT = 1204;
    /** {@code msgAllWaitInfo} 全部听牌信息。$M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:521 */
    public static final int MSG_ALL_WAIT_INFO = 1500;

    // ---- TaiZhouMahjong layer ($M/TaiZhou/TaiZhouMahjong/Protocols/GameProtocol.luac) ----

    /** {@code msgChengBaoFlag} 承包标志。$M/TaiZhou/TaiZhouMahjong/Protocols/GameProtocol.luac:4 */
    public static final int MSG_CHENG_BAO_FLAG = 1055;
    /** {@code msgPreBaoPaiMah} 预报包牌。$M/TaiZhou/TaiZhouMahjong/Protocols/GameProtocol.luac:24 */
    public static final int MSG_PRE_BAO_PAI_MAH = 1501;

    private MahjongMessageId() {}
}
