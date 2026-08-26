package com.nanbeiyule.game.gameplay;

import com.nanbeiyule.game.mahjong.MahjongTile;
import com.nanbeiyule.game.mahjong.TaizhouMultipleState;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Payload builders of the Wave 2-B action commands. Each builder produces
 * exactly the fields the backend consumes — no more, no less:
 *
 * <ul>
 *   <li>DISCARD: {@code {"tileValue":int,"actionToken":"uuid"}}
 *   <li>CHOW: {@code {"tileValue":int,"candidateIndex":int,"actionToken":"uuid"}}
 *   <li>PUNG: {@code {"tileValue":int,"actionToken":"uuid"}}
 *   <li>KONG: {@code {"tileValue":int,"kongType":"EXPOSED|CONCEALED|FILL","actionToken":"uuid"}}
 *   <li>HU / PASS: {@code {"actionToken":"uuid"}}
 *   <li>MULTIPLE_CHOICE: {@code {"choice":"PASS|DEFAULT|SUPER"}}
 *   <li>TRUST: {@code {"trusted":bool}}
 *   <li>DISMISS_REQUEST: 无载荷
 *   <li>DISMISS_RESPOND: {@code {"agree":bool}}
 *   <li>KICK: {@code {"seat":int}}
 * </ul>
 */
public final class GameplayActionProtocol {
    public static final String DISCARD = "DISCARD";
    public static final String CHOW = "CHOW";
    public static final String PUNG = "PUNG";
    public static final String KONG = "KONG";
    public static final String HU = "HU";
    public static final String PASS = "PASS";
    public static final String MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    /**
     * 原版转发族命令（msgClientForward XY_ID=1043，字段 seat/id/strData，CF_ID 1..10）：
     * 表情、GPS、语音等客户端互动，任何阶段可发，不携带 actionToken，与 backend
     * GameplayCommandType.CLIENT_FORWARD 对应。（XY_ID=22 是 msgBaseClientForwardEx，
     * 它的 CF_ID 扩展到 15+OPERATE_PASS=160，本命令不覆盖那层。）
     */
    public static final String CLIENT_FORWARD = "CLIENT_FORWARD";
    /**
     * 原版托管命令（{@code msgTrust} XY_ID=517，双向，字段 {@code nSeat}/{@code nFlag}）。
     * 上行只带标志位，座位由服务端按会话身份填；下行经 {@code Trust/Module.luac:19-38}
     * 写入 {@code MahjongTrustState}。
     */
    public static final String TRUST = "TRUST";
    /**
     * 原版请求解散（{@code msgRequestDismiss} XY_ID=1039，字段 {@code seat}）。
     * {@code Dismiss/Module.luac:181-189 sendRequestDismiss} 按 {@code getDismissType()}
     * 在 SO 与 GP 两条通道间二选一，现代后端只保留一条。
     */
    public static final String DISMISS_REQUEST = "DISMISS_REQUEST";
    /** 原版回应解散（{@code msgRespondDismiss} XY_ID=1040，字段 {@code seat}/{@code agree}）。 */
    public static final String DISMISS_RESPOND = "DISMISS_RESPOND";
    /**
     * 原版房主免费踢人（{@code PlayerInfo/View.luac:655 sendReqKickUser(numberID, areaID)}）。
     * 与 {@code Kick/Module.luac:38 sendReqVipKickUser} 的会员付费踢人不是同一条：
     * 前者只在未开局的包厢由房主发起，后者要会员且有次数上限。这里迁移的是前者。
     */
    public static final String KICK = "KICK";

    private GameplayActionProtocol() {}

    public static JSONObject discardPayload(int tileValue, String actionToken)
            throws JSONException {
        return tileAndToken(tileValue, actionToken);
    }

    public static JSONObject chowPayload(int tileValue, int candidateIndex, String actionToken)
            throws JSONException {
        if (candidateIndex < 0) {
            throw new IllegalArgumentException("candidateIndex must be non-negative");
        }
        return tileAndToken(tileValue, actionToken).put("candidateIndex", candidateIndex);
    }

    public static JSONObject pungPayload(int tileValue, String actionToken) throws JSONException {
        return tileAndToken(tileValue, actionToken);
    }

    public static JSONObject kongPayload(
            int tileValue, GameplayKongType kongType, String actionToken) throws JSONException {
        if (kongType == null) {
            throw new IllegalArgumentException("kongType is required");
        }
        return tileAndToken(tileValue, actionToken).put("kongType", kongType.name());
    }

    public static JSONObject huPayload(String actionToken) throws JSONException {
        return tokenOnly(actionToken);
    }

    public static JSONObject passPayload(String actionToken) throws JSONException {
        return tokenOnly(actionToken);
    }

    public static JSONObject multipleChoicePayload(TaizhouMultipleState.Choice choice)
            throws JSONException {
        if (choice == null) {
            throw new IllegalArgumentException("choice is required");
        }
        return new JSONObject().put("choice", choice.name());
    }

    /** 原版 msgTrust(517) 的 {@code nFlag}：1 置位托管、0 取消。 */
    public static JSONObject trustPayload(boolean trusted) throws JSONException {
        return new JSONObject().put("trusted", trusted);
    }

    /** 原版 sendReqKickUser 的目标座位。 */
    public static JSONObject kickPayload(int seat) throws JSONException {
        if (seat <= 0) {
            throw new IllegalArgumentException("seat must be positive");
        }
        return new JSONObject().put("seat", seat);
    }

    /** 原版 msgRespondDismiss(1040) 的 {@code agree}。 */
    public static JSONObject dismissRespondPayload(boolean agree) throws JSONException {
        return new JSONObject().put("agree", agree);
    }

    /** 原版 msgClientForward(1043) 的 CF_ID 值域 1..10（FastVoice..PlayerHeadTrust）；data 对应 strData。 */
    public static JSONObject forwardPayload(int cfId, String data) throws JSONException {
        if (cfId < 1 || cfId > 10) {
            throw new IllegalArgumentException("cfId out of original CF_ID range: " + cfId);
        }
        return new JSONObject()
                .put("cfId", cfId)
                .put("data", data == null ? "" : data);
    }

    private static JSONObject tileAndToken(int tileValue, String actionToken)
            throws JSONException {
        if (!MahjongTile.isValid(tileValue)) {
            throw new IllegalArgumentException("undefined mahjong tile " + tileValue);
        }
        return tokenOnly(actionToken).put("tileValue", tileValue);
    }

    private static JSONObject tokenOnly(String actionToken) throws JSONException {
        if (actionToken == null || actionToken.isBlank()) {
            throw new IllegalArgumentException("actionToken must not be blank");
        }
        return new JSONObject().put("actionToken", actionToken);
    }
}
