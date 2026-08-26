package com.nanbeiyule.game;

/**
 * 1920x1080 geometry recovered from MailMainLayer.csb（Y 轴自下而上已换算为自上而下）。
 * 坐标来自 MailMainLayer.csd 和 MailDetailLayer.csd。
 */
final class MailLayout {
    static final float DESIGN_WIDTH = 1920f;
    static final float DESIGN_HEIGHT = 1080f;

    // _KW_BTN_CLOSE：196x96，中心 x≈107.9，顶边 cocos y=1065。
    static final Box CLOSE = new Box(9.9f, 15f, 205.9f, 111f);
    // Btn_fanhui 91x97，中心相对返回区 (45.5,48) → 全局 (55.4,63)。
    static final Box CLOSE_ARROW = new Box(9.9f, 14.5f, 100.9f, 111.5f);
    // Img_mail_title 131x74，中心相对返回区 (160,47) → 全局 (169.9,62)。
    static final Box CLOSE_TITLE = new Box(104.4f, 25f, 235.4f, 99f);

    // 左侧邮筒装饰组：img_mail_youxiang.png（1116x1546）为 spine zzb_jbdt_youxiang
    // loop t=0 静态合成，设计 558x772.15；画布原点相对 _KW_ANI_NODE(960,540) 为
    // cocos (-988,-432) → 全局 cocos (-28,108)，top-down 即 left=-28、top=199.85。
    static final Box MAILBOX = new Box(-28f, 199.85f, 530f, 972f);
    // _KW_IMG_LEFT_TITLE Img_mail_tips 241x145 气泡底，锚点 (0.5,0)，pos cocos (301,813.5)。
    static final Box TIPS = new Box(180.5f, 121.5f, 421.5f, 266.5f);
    // 气泡文字标为气泡子节点，中心相对气泡原点 (94,107) → 全局 top-down 中心 (274.5,159.5)；
    // KW_IMG_HAVE Img_mail_zi1 167x43，KW_IMG_EMPTY Img_mail_zi2 165x43。
    static final Box ZI_HAVE = new Box(191f, 138f, 358f, 181f);
    static final Box ZI_EMPTY = new Box(192f, 138f, 357f, 181f);

    // Img_mail_xf_di 1434x869 居中于右侧面板（原版位图，条纹边与回形针内嵌）。
    static final Box PAPER = new Box(487f, 105.5f, 1921f, 974.5f);

    // _KW_NO_MAIL Img_mail_kzt 618x333，中心全局 cocos (1204.03,650.97)。
    static final Box KZT = new Box(895.03f, 262.53f, 1513.03f, 595.53f);
    // Text_14 "您暂无邮件哦~" 292x52，中心相对 _KW_NO_MAIL 原点 cocos (315,-49)
    // → 全局 cocos (1210.03,435.47)。
    static final Box EMPTY_TEXT = new Box(1064.03f, 618.53f, 1356.03f, 670.53f);

    // _KW_PANEL_MAIL_LIST 1300x720，左上角相对右面板原点 cocos (100,941)。
    static final Box LIST = new Box(534f, 139f, 1834f, 859f);

    // 底部三按钮 306x105，中心 cocos y=77，x 相对右面板原点 208/923/1271。
    static final Box BTN_DELETE_ALL = new Box(489f, 950.5f, 795f, 1055.5f);
    static final Box BTN_READ_ALL = new Box(1204f, 950.5f, 1510f, 1055.5f);
    static final Box BTN_CLAIM_ALL = new Box(1552f, 950.5f, 1858f, 1055.5f);

    // _KW_MAIL_DETAIL_ITEM 1294x165；CompTableView 直接使用 item content size。
    static final float ROW_LEFT = 537f;
    static final float ROW_WIDTH = 1294f;
    static final float ROW_HEIGHT = 160f;
    static final float ROW_TEMPLATE_HEIGHT = 165f;
    static final float ROW_STEP = 165f;

    // MailDetailLayer.csd: 邮件详情层的原版 1920x1080 几何。
    static final Box DETAIL_PANEL = new Box(0f, 0f, 1920f, 1080f);
    static final Box DETAIL_CLOSE = new Box(1676.72f, 82.29f, 1776.72f, 182.29f);
    static final Box DETAIL_TITLE = new Box(427f, 204f, 1627f, 256f);
    static final Box DETAIL_CONTENT = new Box(421f, 328f, 1661f, 778f);
    static final Box DETAIL_AWARD_LIST = new Box(635f, 695f, 1435f, 895f);
    static final Box DETAIL_DELETE_ONLY = new Box(897.5f, 906.5f, 1162.5f, 993.5f);
    static final Box DETAIL_DELETE = new Box(747.5f, 906.5f, 1012.5f, 993.5f);
    static final Box DETAIL_CLAIM = new Box(1050.5f, 906.5f, 1315.5f, 993.5f);
    static final float DETAIL_ATTACHMENT_TOP = DETAIL_AWARD_LIST.top;
    static final float DETAIL_ATTACHMENT_STEP = 190f;

    // 行内子节点均为 KW_MAIL_BG（1294x160）的子节点，行局部坐标按 cocos 中心点
    // 换算到 160 行高（top-down）：
    // 红点 33x33，cocos 中心 (11,144)。
    static final Box RED_POINT_LOCAL = new Box(-5.5f, -0.5f, 27.5f, 32.5f);
    // 邮件图标 98x98，cocos 中心 (125,77)。
    static final Box ICON_LOCAL = new Box(76f, 34f, 174f, 132f);
    // 勾选框 41x41，cocos 中心 (43,80)。
    static final Box CHECKBOX_LOCAL = new Box(22.5f, 59.5f, 63.5f, 100.5f);
    // 附件角标 44x44，为邮件图标子节点，中心相对图标原点 cocos (87,11) → 图标右下角。
    static final Box AWARD_BADGE_LOCAL = new Box(141f, 99f, 185f, 143f);
    // 标题 440x52 左对齐 x=195，cocos y=109；描述 x=195，cocos y=47。
    static final float TITLE_LOCAL_LEFT = 195f;
    static final float TITLE_LOCAL_CENTER_Y = 51f;
    static final float DESC_LOCAL_LEFT = 195f;
    static final float DESC_LOCAL_CENTER_Y = 113f;
    // 时间组：图标 Img_mail_time 32x32 cocos 中心 (1015,105)，
    // 文本 225x42 左对齐 x=1036，cocos y=106；Lua 文案为剩余N天/小时。
    static final Box TIME_ICON_LOCAL = new Box(999f, 39f, 1031f, 71f);
    static final float TIME_TEXT_LOCAL_LEFT = 1036f;
    static final float TIME_LOCAL_CENTER_Y = 54f;
    // 已领取印章 Img_ylq 128x101（原版用于详情层奖励项；行内已领取标记为南北娱乐
    // 现代补充，无原版行内坐标证据，推断置于行右端垂直居中）。
    static final Box CLAIMED_STAMP_LOCAL = new Box(1106f, 29.5f, 1234f, 130.5f);

    private MailLayout() {}

    /** Lua changeMailState：无邮件时三个底部按钮与列表面板全部隐藏。 */
    static boolean bottomButtonsVisible(int mailCount) {
        return mailCount > 0;
    }

    static Box rowRect(int index, int mailCount, float scroll) {
        float top = LIST.top + index * ROW_STEP - clampScroll(scroll, mailCount);
        return new Box(ROW_LEFT, top, ROW_LEFT + ROW_WIDTH, top + ROW_HEIGHT);
    }

    static Box rowChild(Box row, Box local) {
        return new Box(
                row.left + local.left,
                row.top + local.top,
                row.left + local.right,
                row.top + local.bottom);
    }

    static float maxScroll(int mailCount) {
        return Math.max(0f, mailCount * ROW_STEP - LIST.height());
    }

    static float clampScroll(float scroll, int mailCount) {
        return Math.max(0f, Math.min(maxScroll(mailCount), scroll));
    }

    record Box(float left, float top, float right, float bottom) {
        float width() { return right - left; }
        float height() { return bottom - top; }
        float centerX() { return (left + right) * 0.5f; }
        float centerY() { return (top + bottom) * 0.5f; }
        boolean contains(float x, float y) {
            return x >= left && x < right && y >= top && y < bottom;
        }
    }

    record Transform(float scale, float offsetX, float offsetY) {
        static Transform contain(int width, int height) {
            float scale = Math.min(width / DESIGN_WIDTH, height / DESIGN_HEIGHT);
            return new Transform(
                    scale,
                    (width - DESIGN_WIDTH * scale) * 0.5f,
                    (height - DESIGN_HEIGHT * scale) * 0.5f);
        }

        float designX(float x) { return (x - offsetX) / scale; }
        float designY(float y) { return (y - offsetY) / scale; }
    }
}
