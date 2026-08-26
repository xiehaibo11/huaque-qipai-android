package com.nanbeiyule.game.mahjong.protocol;

import java.util.Objects;

/**
 * 转发/形象域语义消息（BasicMahjong 层）：msgAvatarUrl、msgSpeak、
 * msgClientForward、msgBaseClientForwardEx。来源
 * {@code $M/BasicMahjong/Protocols/GameProtocol.luac}
 * （$M = artifacts/zhejiang_game_lobby_1.5.4/recovered/mahjong-1.0.0.687-plain/src/game/Mahjong）。
 */
public final class ForwardMessages {
    private ForwardMessages() {}

    /**
     * {@code msgAvatarUrl} 玩家头像。方向 双向（bistream + bostream）。XY_ID = 1042。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1623。
     * 线上 strData 经 urlencode/urldecode（:1638/:1646）。
     *
     * @param seat 座位号 — :1626
     * @param avatarUrl 头像地址 — :1627
     */
    public record MsgAvatarUrl(int seat, String avatarUrl) {
        public MsgAvatarUrl {
            Objects.requireNonNull(avatarUrl, "avatarUrl");
        }
    }

    /**
     * {@code msgSpeak} 语音说话（基础层，台州层有覆盖）。方向 双向。
     * XY_ID = 1028。声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1372。
     *
     * @param id 语音 ID — :1375
     * @param bIsMan 是否男声 — :1376
     * @param speakSeat 说话座位 — :1377
     */
    public record MsgSpeak(int id, boolean bIsMan, int speakSeat) {}

    /**
     * {@code msgClientForward} 客户端转发。方向 双向（bistream + bostream）。
     * XY_ID = 1043。声明 $M/BasicMahjong/Protocols/GameProtocol.luac:1651。
     * CF_ID 常量表 :1654-1665（FastVoice..PlayerHeadTrust）。
     *
     * @param seat 座位号 — :1666
     * @param id 转发 ID（CF_ID） — :1667
     * @param strData 数据（线上 urlencode） — :1668
     */
    public record MsgClientForward(int seat, int id, String strData) {
        public MsgClientForward {
            Objects.requireNonNull(strData, "strData");
        }

        /** {@code CF_ID.FastVoice} — :1655 */ public static final int CF_FAST_VOICE = 1;
        /** {@code CF_ID.GPS_MSG} — :1656 */ public static final int CF_GPS_MSG = 2;
        /** {@code CF_ID.Mobile_Signal} — :1657 */ public static final int CF_MOBILE_SIGNAL = 3;
        /** {@code CF_ID.Speed_Test} — :1658 */ public static final int CF_SPEED_TEST = 4;
        /** {@code CF_ID.WireBreak_Signal} — :1659 */ public static final int CF_WIRE_BREAK_SIGNAL = 5;
        /** {@code CF_ID.Expression} — :1660 */ public static final int CF_EXPRESSION = 6;
        /** {@code CF_ID.FaceAni} — :1661 */ public static final int CF_FACE_ANI = 7;
        /** {@code CF_ID.PlayerHeadEffect} — :1662 */ public static final int CF_PLAYER_HEAD_EFFECT = 8;
        /** {@code CF_ID.PropAni} — :1663 */ public static final int CF_PROP_ANI = 9;
        /** {@code CF_ID.PlayerHeadTrust} — :1664 */ public static final int CF_PLAYER_HEAD_TRUST = 10;
    }

    /**
     * {@code msgBaseClientForwardEx} 客户端转发扩展（基础层，台州层有覆盖）。
     * 方向 双向（bistream + bostream）。XY_ID = 22。
     * 声明 $M/BasicMahjong/Protocols/GameProtocol.luac:32。
     * CF_ID 常量表 :34-51（含 OPERATE_PASS = 160），CT_ID 常量表 :52-55。
     *
     * @param sSeat 源座位 — :56
     * @param toSeat 目标座位（默认 -1 广播） — :57
     * @param sID 转发 ID（CF_ID） — :58
     * @param sType 转发类型（CT_ID） — :59
     * @param strData 数据（线上 urlencode） — :60
     */
    public record MsgBaseClientForwardEx(int sSeat, int toSeat, int sID, int sType,
            String strData) {
        public MsgBaseClientForwardEx {
            Objects.requireNonNull(strData, "strData");
        }

        /** {@code CF_ID.FastVoice} — :35 */ public static final int CF_FAST_VOICE = 1;
        /** {@code CF_ID.GPS_MSG} — :36 */ public static final int CF_GPS_MSG = 2;
        /** {@code CF_ID.Mobile_Signal} — :37 */ public static final int CF_MOBILE_SIGNAL = 3;
        /** {@code CF_ID.Speed_Test} — :38 */ public static final int CF_SPEED_TEST = 4;
        /** {@code CF_ID.WireBreak_Signal} — :39 */ public static final int CF_WIRE_BREAK_SIGNAL = 5;
        /** {@code CF_ID.Expression} — :40 */ public static final int CF_EXPRESSION = 6;
        /** {@code CF_ID.FaceAni} — :41 */ public static final int CF_FACE_ANI = 7;
        /** {@code CF_ID.PlayerHeadEffect} — :42 */ public static final int CF_PLAYER_HEAD_EFFECT = 8;
        /** {@code CF_ID.PropAni} — :43 */ public static final int CF_PROP_ANI = 9;
        /** {@code CF_ID.PlayerHeadTrust} — :44 */ public static final int CF_PLAYER_HEAD_TRUST = 10;
        /** {@code CF_ID.Emoji} — :45 */ public static final int CF_EMOJI = 11;
        /** {@code CF_ID.HeadUrl} — :46 */ public static final int CF_HEAD_URL = 12;
        /** {@code CF_ID.AMap} — :47 */ public static final int CF_A_MAP = 13;
        /** {@code CF_ID.QiaoPiHua} — :48 */ public static final int CF_QIAO_PI_HUA = 14;
        /** {@code CF_ID.TouchPoint} — :49 */ public static final int CF_TOUCH_POINT = 15;
        /** {@code CF_ID.OPERATE_PASS} — :50 */ public static final int CF_OPERATE_PASS = 160;
        /** {@code CT_ID.XY_FORWARD} — :53 */ public static final int CT_XY_FORWARD = 1;
        /** {@code CT_ID.XY_SAVE_FORWARD} — :54 */ public static final int CT_XY_SAVE_FORWARD = 2;
    }
}
