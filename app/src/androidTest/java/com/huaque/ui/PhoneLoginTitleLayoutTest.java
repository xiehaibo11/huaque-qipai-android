package com.huaque.ui;

import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.cocos2dx.lua.AppActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PhoneLoginTitleLayoutTest {
    @Rule
    public final ActivityScenarioRule<AppActivity> activityRule =
            new ActivityScenarioRule<>(AppActivity.class);

    @Test
    public void testPhoneLoginUsesXianyiReferenceHierarchy() throws Throwable {
        openPhoneLogin();

        activityRule.getScenario().onActivity(activity -> {
            View decor = activity.getWindow().getDecorView();
            TextView verifyTab = findTextView(decor, "验证码登录");
            TextView passwordTab = findTextView(decor, "账号密码登录");
            TextView phoneLabel = findTextView(decor, "手机号：");
            TextView codeLabel = findTextView(decor, "验证码：");
            View sendCode = findByContentDescription(decor, "获取验证码");
            View confirm = findByContentDescription(decor, "确定");
            View close = findByContentDescription(decor, "关闭手机登录");

            assertNotNull("必须存在验证码登录页签", verifyTab);
            assertNotNull("必须存在账号密码登录页签", passwordTab);
            assertNotNull("必须存在手机号字段名", phoneLabel);
            assertNotNull("必须存在验证码字段名", codeLabel);
            assertNotNull("必须存在获取验证码按钮", sendCode);
            assertNotNull("必须存在确定按钮", confirm);
            assertNotNull("必须存在右上角关闭按钮", close);
            assertTrue("账号密码登录页签必须可以点击切换", passwordTab.isEnabled());
        });
    }

    @Test
    public void testPhoneLoginContentFitsWideHighDensityPhone() throws Throwable {
        openPhoneLogin();

        activityRule.getScenario().onActivity(activity -> {
            View decor = activity.getWindow().getDecorView();
            int screenWidth = decor.getWidth();
            int screenHeight = decor.getHeight();
            View panel = findByContentDescription(decor, "手机登录面板");
            TextView verifyTab = findTextView(decor, "验证码登录");
            TextView passwordTab = findTextView(decor, "账号密码登录");
            TextView phoneLabel = findTextView(decor, "手机号：");
            TextView codeLabel = findTextView(decor, "验证码：");
            TextView sendCode = (TextView) findByContentDescription(decor, "获取验证码");
            View confirm = findByContentDescription(decor, "确定");
            View close = findByContentDescription(decor, "关闭手机登录");
            EditText phone = (EditText) findByContentDescription(decor, "手机号输入框");
            EditText code = (EditText) findByContentDescription(decor, "验证码输入框");

            assertNotNull("手机登录弹窗必须存在面板", panel);
            assertNotNull("手机登录弹窗必须存在验证码登录页签", verifyTab);
            assertNotNull("手机登录弹窗必须存在账号密码登录页签", passwordTab);
            assertNotNull("手机登录弹窗必须存在手机号字段名", phoneLabel);
            assertNotNull("手机登录弹窗必须存在验证码字段名", codeLabel);
            assertNotNull("手机登录弹窗必须存在获取验证码按钮", sendCode);
            assertNotNull("手机登录弹窗必须存在确定按钮", confirm);
            assertNotNull("手机登录弹窗必须存在关闭按钮", close);
            assertNotNull("手机登录弹窗必须存在手机号输入框", phone);
            assertNotNull("手机登录弹窗必须存在验证码输入框", code);
            assertTrue("测试设备必须处于横屏", screenWidth > screenHeight);
            assertTrue("参考弹窗宽度不能因超宽屏缩小", panel.getWidth() >= screenWidth * 0.42f);
            assertTrue("参考弹窗不能横向铺得过宽", panel.getWidth() <= screenWidth * 0.52f);
            assertTrue("参考弹窗高度必须覆盖完整表单", panel.getHeight() >= screenHeight * 0.60f);
            assertInsidePanel("验证码登录页签", panel, verifyTab);
            assertInsidePanel("账号密码登录页签", panel, passwordTab);
            assertInsidePanel("手机号字段名", panel, phoneLabel);
            assertInsidePanel("验证码字段名", panel, codeLabel);
            assertInsidePanel("手机号输入框", panel, phone);
            assertInsidePanel("验证码输入框", panel, code);
            assertInsidePanel("获取验证码按钮", panel, sendCode);
            assertInsidePanel("确定按钮", panel, confirm);
            assertTextFits("验证码登录页签", verifyTab, verifyTab.getText());
            assertTextFits("账号密码登录页签", passwordTab, passwordTab.getText());
            assertTextFits("手机号字段名", phoneLabel, phoneLabel.getText());
            assertTextFits("验证码字段名", codeLabel, codeLabel.getText());
            assertTextFits("手机号输入提示", phone, phone.getHint());
            assertTextFits("验证码输入提示", code, code.getHint());
            assertTrue("手机号输入不能进入横屏全屏编辑模式",
                    (phone.getImeOptions() & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0);
            assertTrue("验证码输入不能进入横屏全屏编辑模式",
                    (code.getImeOptions() & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0);
        });
    }

    @Test
    public void testPhoneLoginKeepsXianyiReferenceAlignment() throws Throwable {
        openPhoneLogin();

        activityRule.getScenario().onActivity(activity -> {
            View decor = activity.getWindow().getDecorView();
            View panel = findByContentDescription(decor, "手机登录面板");
            TextView verifyTab = findTextView(decor, "验证码登录");
            TextView passwordTab = findTextView(decor, "账号密码登录");
            TextView phoneLabel = findTextView(decor, "手机号：");
            TextView codeLabel = findTextView(decor, "验证码：");
            View sendCode = findByContentDescription(decor, "获取验证码");
            View confirm = findByContentDescription(decor, "确定");
            View close = findByContentDescription(decor, "关闭手机登录");
            View phone = findByContentDescription(decor, "手机号输入框");
            View code = findByContentDescription(decor, "验证码输入框");

            assertNotNull(panel);
            assertNotNull(verifyTab);
            assertNotNull(passwordTab);
            assertNotNull(phoneLabel);
            assertNotNull(codeLabel);
            assertNotNull(sendCode);
            assertNotNull(confirm);
            assertNotNull(close);
            assertNotNull(phone);
            assertNotNull(code);
            assertEquals("两个登录页签必须等宽", verifyTab.getWidth(), passwordTab.getWidth());
            assertTrue("手机号字段名必须在输入框左侧", phoneLabel.getRight() <= phone.getLeft());
            assertTrue("验证码字段名必须在输入框左侧", codeLabel.getRight() <= code.getLeft());
            assertTrue("验证码按钮必须在验证码输入框右侧", code.getRight() < sendCode.getLeft());
            assertTrue(
                    "确定按钮必须在弹窗中轴居中",
                    Math.abs((confirm.getLeft() + confirm.getRight())
                            - (panel.getLeft() + panel.getRight())) <= 2);
            assertTrue(
                    "确定按钮宽度不能超过弹窗宽度的三成",
                    confirm.getWidth() <= panel.getWidth() * 0.30f);
            assertTrue("关闭按钮必须位于弹窗右上角", close.getLeft() > panel.getRight() - close.getWidth());
            assertTrue("关闭按钮必须与弹窗顶部相交", close.getTop() < panel.getTop() + close.getHeight());
        });
    }

    @Test
    public void testAccountPasswordTabSwitchesToCompletePasswordForm() throws Throwable {
        openPhoneLogin();

        activityRule.getScenario().onActivity(activity -> {
            View decor = activity.getWindow().getDecorView();
            TextView passwordTab = findTextView(decor, "账号密码登录");
            assertNotNull("必须存在账号密码登录页签", passwordTab);
            assertTrue("账号密码登录页签必须可用", passwordTab.isEnabled());

            passwordTab.performClick();

            TextView passwordLabel = findTextView(decor, "密码：");
            EditText password = (EditText) findByContentDescription(decor, "密码输入框");
            TextView register = findTextView(decor, "注册账号");
            TextView forgot = findTextView(decor, "忘记密码");
            View code = findByContentDescription(decor, "验证码输入框");
            View sendCode = findByContentDescription(decor, "获取验证码");

            assertNotNull("密码模式必须显示密码字段名", passwordLabel);
            assertNotNull("密码模式必须显示密码输入框", password);
            assertNotNull("密码模式必须显示注册账号入口", register);
            assertNotNull("密码模式必须显示忘记密码入口", forgot);
            assertEquals("密码输入框必须遮罩明文", InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    password.getInputType() & InputType.TYPE_MASK_VARIATION);
            assertTrue("密码输入不能进入横屏全屏编辑模式",
                    (password.getImeOptions() & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0);
            assertFalse("密码模式不能继续显示验证码输入框", code != null && code.getVisibility() == View.VISIBLE);
            assertFalse("密码模式不能继续显示获取验证码按钮", sendCode != null && sendCode.getVisibility() == View.VISIBLE);

            TextView verifyTab = findTextView(decor, "验证码登录");
            assertNotNull(verifyTab);
            verifyTab.performClick();
            assertEquals("切回验证码登录后必须恢复验证码输入框", View.VISIBLE, code.getVisibility());
            assertEquals("切回验证码登录后必须恢复获取验证码按钮", View.VISIBLE, sendCode.getVisibility());
        });
    }

    @Test
    public void testRegisterAccountOpensXianyiRegistrationForm() throws Throwable {
        openPhoneLogin();

        activityRule.getScenario().onActivity(activity -> {
            View decor = activity.getWindow().getDecorView();
            TextView passwordTab = findTextView(decor, "账号密码登录");
            assertNotNull(passwordTab);
            passwordTab.performClick();
            TextView register = findTextView(decor, "注册账号");
            assertNotNull(register);
            register.performClick();

            assertNotNull("注册页必须使用原版注册账号标题资源",
                    findByContentDescription(decor, "注册账号标题"));
            assertNotNull("注册页必须显示手机号字段名", findTextView(decor, "手机号："));
            assertNotNull("注册页必须显示验证码字段名", findTextView(decor, "验证码："));
            EditText phone = (EditText) findByContentDescription(decor, "注册手机号输入框");
            EditText code = (EditText) findByContentDescription(decor, "注册验证码输入框");
            assertNotNull("注册页必须显示手机号输入框", phone);
            assertNotNull("注册页必须显示验证码输入框", code);
            assertNotNull("注册页必须显示获取验证码按钮",
                    findByContentDescription(decor, "注册获取验证码"));
            assertNotNull("注册页必须显示确定按钮",
                    findByContentDescription(decor, "注册确定"));
            assertNotNull("注册页必须显示关闭按钮",
                    findByContentDescription(decor, "关闭注册账号"));
            assertTrue("注册手机号输入不能进入横屏全屏编辑模式",
                    (phone.getImeOptions() & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0);
            assertTrue("注册验证码输入不能进入横屏全屏编辑模式",
                    (code.getImeOptions() & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0);
        });
    }

    @Test
    public void testRegistrationSuccessUsesXianyiResultDialog() throws Throwable {
        openPhoneLogin();

        activityRule.getScenario().onActivity(activity -> {
            invokeRegistrationResult(activity, "注册成功，请点击确定进入游戏", true);
            View decor = activity.getWindow().getDecorView();
            assertNotNull("注册结果必须使用闲逸斗地主提示框",
                    findByContentDescription(decor, "注册结果提示"));
            assertNotNull("注册成功提示必须显示完整文案",
                    findTextView(decor, "注册成功，请点击确定进入游戏"));
            assertNotNull("注册结果必须显示确定按钮",
                    findByContentDescription(decor, "注册结果确定"));
        });
    }

    private static void assertInsidePanel(String label, View panel, View child) {
        assertTrue(label + "不能超出弹窗左边界", child.getLeft() >= panel.getLeft());
        assertTrue(label + "不能超出弹窗右边界", child.getRight() <= panel.getRight());
        assertTrue(label + "不能超出弹窗上边界", child.getTop() >= panel.getTop());
        assertTrue(label + "不能超出弹窗下边界", child.getBottom() <= panel.getBottom());
    }

    private void openPhoneLogin() {
        activityRule.getScenario().onActivity(activity -> {
            invokeShowLoginPage(activity);
            acceptAgreement(activity);
            View phoneLogin = findByContentDescription(
                    activity.getWindow().getDecorView(), "手机登录");
            assertNotNull("登录页必须存在手机登录入口", phoneLogin);
            phoneLogin.performClick();
        });
    }

    private static void assertTextFits(String label, TextView view, CharSequence value) {
        float requiredWidth = view.getPaint().measureText(value.toString())
                + view.getCompoundPaddingLeft()
                + view.getCompoundPaddingRight();
        assertTrue(
                label + "不能横向截断，需要=" + requiredWidth + "，实际=" + view.getWidth(),
                requiredWidth <= view.getWidth());
        assertTrue(
                label + "不能纵向截断，行高=" + view.getLineHeight() + "，实际=" + view.getHeight(),
                view.getLineHeight() <= view.getHeight());
    }

    private static void invokeShowLoginPage(MainActivity activity) {
        try {
            Method method = MainActivity.class.getDeclaredMethod("showLoginPage");
            method.setAccessible(true);
            method.invoke(activity);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("无法打开登录页", exception);
        }
    }

    private static void acceptAgreement(MainActivity activity) {
        try {
            Field field = MainActivity.class.getDeclaredField("agreementAccepted");
            field.setAccessible(true);
            field.setBoolean(activity, true);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("无法设置协议状态", exception);
        }
    }

    private static void invokeRegistrationResult(
            MainActivity activity, String message, boolean success) {
        try {
            Method method = MainActivity.class.getDeclaredMethod(
                    "showRegistrationResult", String.class, boolean.class);
            method.setAccessible(true);
            method.invoke(activity, message, success);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("无法打开注册结果提示", exception);
        }
    }

    private static View findByContentDescription(View view, String description) {
        CharSequence contentDescription = view.getContentDescription();
        if (contentDescription != null && description.contentEquals(contentDescription)) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View match = findByContentDescription(group.getChildAt(i), description);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private static TextView findTextView(View view, String text) {
        if (view instanceof TextView && text.contentEquals(((TextView) view).getText())) {
            return (TextView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                TextView match = findTextView(group.getChildAt(i), text);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private static EditText findEditText(View view, String hint) {
        if (view instanceof EditText
                && ((EditText) view).getHint() != null
                && hint.contentEquals(((EditText) view).getHint())) {
            return (EditText) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                EditText match = findEditText(group.getChildAt(i), hint);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }
}
