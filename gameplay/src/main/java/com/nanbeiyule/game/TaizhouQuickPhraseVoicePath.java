package com.nanbeiyule.game;

/**
 * 复刻麻将族 {@code BasicMahjong/Manager/SoundManager.luac:90-145} 的俏皮话音频路径解析。
 *
 * <p>常量取自同文件 {@code PATH_SPEAK = "res/audio/Speak/"}、{@code sexPath = {"Man/", "Women/"}}、
 * {@code languageType = {"standard/", "dialect"}}（注意 dialect 项没有尾斜杠，斜杠由 dialectType
 * 之后补上）。麻将族与斗地主、红十等族的方案不同：麻将在方言目录后多一层
 * {@code dialectType}，来自 {@code CF.settingData:getMahDialectVoiceType()}，取值 1 或 2 时该层
 * 为空串，其它取值直接拼数字。
 *
 * <p>回退方向按开关反向：方言开时先找方言、缺失回标准话；方言关时先找标准话、缺失回方言。
 * 文件名来自 {@code GameSpeak_<gameID>} 配置的 {@code M_SpeakFileName}/{@code W_SpeakFileName}，
 * 30109 是不补零的 {@code M_Speak1.mp3} 到 {@code M_Speak9.mp3}。
 */
final class TaizhouQuickPhraseVoicePath {
    /** 原版 {@code PATH_SPEAK}，去掉 Cocos 的 res/ 前缀后就是 Android assets 下的相对路径。 */
    static final String ROOT = "audio/Speak";

    static final String MAN = "Man";
    static final String WOMEN = "Women";
    static final String STANDARD = "standard";
    static final String DIALECT = "dialect";

    /** 原版 dialectType 为 1 或 2 时不追加后缀。 */
    static final int DIALECT_TYPE_DEFAULT = 1;

    private TaizhouQuickPhraseVoicePath() {}

    /**
     * 按原版顺序返回候选路径：第一个是首选，第二个是缺失时的回退。
     *
     * @param gameId 牌局 ID，30109 为台州麻将
     * @param index 俏皮话序号，1 起，与 GameSpeak 配置的 index 一致
     * @param male 说话人是否为男性，决定 {@code M_}/{@code W_} 与 Man/Women 目录
     * @param dialect 设置页「方言」开关
     * @param dialectVoiceType 原版 {@code getMahDialectVoiceType()}，1/2 表示默认方言包
     */
    static String[] candidates(
            int gameId, int index, boolean male, boolean dialect, int dialectVoiceType) {
        String standard = standardPath(gameId, index, male);
        String dialectPath = dialectPath(gameId, index, male, dialectVoiceType);
        return dialect
                ? new String[] {dialectPath, standard}
                : new String[] {standard, dialectPath};
    }

    static String standardPath(int gameId, int index, boolean male) {
        return ROOT + "/" + gameId + "/" + STANDARD + "/" + sex(male) + "/" + fileName(index, male);
    }

    static String dialectPath(int gameId, int index, boolean male, int dialectVoiceType) {
        return ROOT
                + "/"
                + gameId
                + "/"
                + DIALECT
                + dialectSuffix(dialectVoiceType)
                + "/"
                + sex(male)
                + "/"
                + fileName(index, male);
    }

    /** {@code (dialectType == 2 or dialectType == 1) and "" or tostring(dialectType)} */
    static String dialectSuffix(int dialectVoiceType) {
        return dialectVoiceType == 1 || dialectVoiceType == 2
                ? ""
                : String.valueOf(dialectVoiceType);
    }

    private static String sex(boolean male) {
        return male ? MAN : WOMEN;
    }

    /** 30109 的 GameSpeak 配置用不补零的序号。 */
    private static String fileName(int index, boolean male) {
        return (male ? "M_Speak" : "W_Speak") + index + ".mp3";
    }
}
