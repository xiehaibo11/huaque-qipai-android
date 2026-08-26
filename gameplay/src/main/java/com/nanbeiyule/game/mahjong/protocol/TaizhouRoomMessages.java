package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 台州层房间/转发域语义消息（BasicTaiZhouMahjong 覆盖/新增）：
 * msgRequestDismiss、msgShuffleSeatsTZ、msgDynamicTableChangeSeat、
 * msgBaseClientForwardEx 的台州版。来源
 * {@code $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class TaizhouRoomMessages {
    private TaizhouRoomMessages() {}

    /**
     * {@code msgRequestDismiss} 请求解散（台州覆盖版）。方向 双向
     * （bistream + bostream）。XY_ID = 1039。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:124；
     * 同文件 :244 有内容完全一致的重复定义，编目只登记一次。
     *
     * @param seat 座位（默认 4） — :127
     * @param nRemainingTime 剩余时间（bostream 写 UInt32、bistream 读 Int32，
     *        :140/:147，原版类型不一致如实记录） — :128
     * @param bAgree 四家同意标记（0..3）；可选尾字段（:148-152） — :129
     */
    public record MsgRequestDismissTZ(int seat, int nRemainingTime, boolean[] bAgree) {
        public MsgRequestDismissTZ {
            Objects.requireNonNull(bAgree, "bAgree");
        }
    }

    /**
     * {@code msgShuffleSeatsTZ} 台州换座（台州新增）。方向 双向
     * （bistream + bostream）。XY_ID = 1052。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:296。
     * 线上固定读写 4 个座位（下标 0..3，:309-311/:319-321）。
     *
     * @param sSeat 座位（4 个） — :299
     */
    public record MsgShuffleSeatsTZ(int[] sSeat) {
        public MsgShuffleSeatsTZ {
            Objects.requireNonNull(sSeat, "sSeat");
        }
    }

    /**
     * {@code msgDynamicTableChangeSeat} 动态桌换座（台州新增）。方向 S→C
     * （仅 bistream）。XY_ID = 1204。
     * 声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:496。
     *
     * @param sPlayerSeat 玩家座位 — :499
     * @param sEmptySeat 空座位 — :500
     * @param sChairs 椅子数 — :501
     */
    public record MsgDynamicTableChangeSeat(int sPlayerSeat, int sEmptySeat,
            int sChairs) {}

    /**
     * {@code msgBaseClientForwardEx} 客户端转发扩展（台州覆盖版）。方向 双向。
     * XY_ID = 22。声明 $M/TaiZhou/BasicTaiZhouMahjong/Protocols/GameProtocol.luac:567。
     * 字段与基础层一致；CF_ID 新增 {@code OperatePass = 16}（:585）。
     *
     * @param sSeat 源座位 — :592
     * @param toSeat 目标座位（默认 -1 广播） — :593
     * @param sID 转发 ID（CF_ID） — :594
     * @param sType 转发类型（CT_ID） — :595
     * @param strData 数据（线上 urlencode） — :596
     */
    public record MsgBaseClientForwardExTZ(int sSeat, int toSeat, int sID, int sType,
            String strData) {
        public MsgBaseClientForwardExTZ {
            Objects.requireNonNull(strData, "strData");
        }

        /** {@code CF_ID.FastVoice} — :570 */ public static final int CF_FAST_VOICE = 1;
        /** {@code CF_ID.GPS_MSG} — :571 */ public static final int CF_GPS_MSG = 2;
        /** {@code CF_ID.Mobile_Signal} — :572 */ public static final int CF_MOBILE_SIGNAL = 3;
        /** {@code CF_ID.Speed_Test} — :573 */ public static final int CF_SPEED_TEST = 4;
        /** {@code CF_ID.WireBreak_Signal} — :574 */ public static final int CF_WIRE_BREAK_SIGNAL = 5;
        /** {@code CF_ID.Expression} — :575 */ public static final int CF_EXPRESSION = 6;
        /** {@code CF_ID.FaceAni} — :576 */ public static final int CF_FACE_ANI = 7;
        /** {@code CF_ID.PlayerHeadEffect} — :577 */ public static final int CF_PLAYER_HEAD_EFFECT = 8;
        /** {@code CF_ID.PropAni} — :578 */ public static final int CF_PROP_ANI = 9;
        /** {@code CF_ID.PlayerHeadTrust} — :579 */ public static final int CF_PLAYER_HEAD_TRUST = 10;
        /** {@code CF_ID.Emoji} — :580 */ public static final int CF_EMOJI = 11;
        /** {@code CF_ID.HeadUrl} — :581 */ public static final int CF_HEAD_URL = 12;
        /** {@code CF_ID.AMap} — :582 */ public static final int CF_A_MAP = 13;
        /** {@code CF_ID.QiaoPiHua} — :583 */ public static final int CF_QIAO_PI_HUA = 14;
        /** {@code CF_ID.TouchPoint} — :584 */ public static final int CF_TOUCH_POINT = 15;
        /** {@code CF_ID.OperatePass}（转发过操作，台州层新增） — :585 */
        public static final int CF_OPERATE_PASS_FLAG = 16;
        /** {@code CF_ID.OPERATE_PASS} — :586 */ public static final int CF_OPERATE_PASS = 160;
        /** {@code CT_ID.XY_FORWARD} — :589 */ public static final int CT_XY_FORWARD = 1;
        /** {@code CT_ID.XY_SAVE_FORWARD} — :590 */ public static final int CT_XY_SAVE_FORWARD = 2;
    }
}
