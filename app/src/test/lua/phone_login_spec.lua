package.path = "app/src/main/assets/src/?.lua;app/src/main/assets/src/?/init.lua;" .. package.path

local function assert_equal(expected, actual, label)
    if expected ~= actual then
        error(string.format("%s: expected %s, got %s", label, tostring(expected), tostring(actual)), 2)
    end
end

local function assert_true(value, label)
    if not value then
        error(label .. ": expected true", 2)
    end
end

local function new_fixture()
    local PhoneLogin = require("auth.Modules.PhoneLogin.Module")
    local Events = require("auth.Events")
    local fixture = {
        now = 100,
        codeCalls = {},
        loginCalls = {},
        registerCalls = {},
        saved = {},
        events = {},
        states = {}
    }

    local module = PhoneLogin.new({
        requestCode = {
            execute = function(_, phone, callback)
                table.insert(fixture.codeCalls, {phone = phone, callback = callback})
            end
        },
        requestLogin = {
            execute = function(_, phone, code, callback)
                table.insert(fixture.loginCalls, {phone = phone, code = code, callback = callback})
            end
        },
        requestRegister = {
            execute = function(_, phone, code, callback)
                table.insert(fixture.registerCalls, {phone = phone, code = code, callback = callback})
            end
        },
        sessionStore = {
            save = function(_, session)
                table.insert(fixture.saved, session)
            end
        },
        publish = function(event, payload)
            table.insert(fixture.events, {event = event, payload = payload})
        end,
        emitState = function(state)
            table.insert(fixture.states, state)
        end,
        now = function()
            return fixture.now
        end
    })
    fixture.module = module
    fixture.eventsContract = Events
    return fixture
end

local function latest_state(fixture)
    return fixture.states[#fixture.states]
end

local tests = {}

function tests.invalid_phone_never_sends_a_request()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:requestCode("12345", 100)

    assert_equal(0, #fixture.codeCalls, "request count")
    assert_equal("ERROR", latest_state(fixture).phase, "phase")
    assert_equal("请输入正确的11位手机号", latest_state(fixture).message, "message")
end

function tests.normalizes_mainland_phone_and_blocks_duplicate_requests()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:requestCode("+86 138-0013-8000", 100)
    fixture.module:requestCode("13800138000", 100)

    assert_equal(1, #fixture.codeCalls, "request count")
    assert_equal("13800138000", fixture.codeCalls[1].phone, "normalized phone")
    assert_equal("REQUESTING_CODE", latest_state(fixture).phase, "phase")
    assert_true(not latest_state(fixture).sendEnabled, "send disabled")
end

function tests.code_request_and_login_cannot_interrupt_each_other()
    local sending = new_fixture()
    sending.module:open(100)
    sending.module:requestCode("13800138000", 100)
    sending.module:submitCode("13800138000", "246810", 100)
    assert_equal(0, #sending.loginCalls, "login while sending")
    assert_equal("REQUESTING_CODE", latest_state(sending).phase, "sending phase")
    assert_true(not latest_state(sending).loginEnabled, "login disabled while sending")

    local verifying = new_fixture()
    verifying.module:open(100)
    verifying.module:submitCode("13800138000", "246810", 100)
    verifying.module:requestCode("13800138000", 100)
    assert_equal(0, #verifying.codeCalls, "send while verifying")
    assert_equal("VERIFYING", latest_state(verifying).phase, "verifying phase")
    assert_true(not latest_state(verifying).sendEnabled, "send disabled while verifying")
end

function tests.starts_sixty_second_cooldown_only_after_success()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:requestCode("13800138000", 100)
    fixture.codeCalls[1].callback(true, {challengeId = "challenge-1", expiresIn = 300})

    assert_equal("CODE_SENT", latest_state(fixture).phase, "phase")
    assert_equal(60, latest_state(fixture).remainingSeconds, "initial cooldown")

    fixture.module:tick(131)
    assert_equal(29, latest_state(fixture).remainingSeconds, "updated cooldown")

    fixture.module:tick(160)
    assert_equal(0, latest_state(fixture).remainingSeconds, "finished cooldown")
    assert_true(latest_state(fixture).sendEnabled, "send enabled")
end

function tests.cooldown_and_session_use_async_completion_time()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:requestCode("13800138000", 100)
    fixture.now = 115
    fixture.codeCalls[1].callback(true, {challengeId = "challenge-1", expiresIn = 300})
    assert_equal(60, latest_state(fixture).remainingSeconds, "cooldown at completion")
    fixture.module:tick(130)
    assert_equal(45, latest_state(fixture).remainingSeconds, "cooldown after delayed response")

    local login = new_fixture()
    login.module:open(200)
    login.module:submitCode("13800138000", "246810", 200)
    login.now = 212
    login.loginCalls[1].callback(true, {
        accessToken = "access-token",
        refreshToken = "refresh-token",
        tokenType = "Bearer",
        expiresIn = 900
    })
    assert_equal(212, login.saved[1].issuedAt, "session issue time")
end

function tests.rate_limited_code_request_shows_safe_backend_detail_and_starts_cooldown()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:requestCode("13800138000", 100)
    fixture.codeCalls[1].callback(false, {
        code = "AUTH_OTP_RATE_LIMITED",
        detail = "验证码发送过于频繁，请一分钟后再试"
    })

    assert_equal("ERROR", latest_state(fixture).phase, "phase")
    assert_equal(60, latest_state(fixture).remainingSeconds, "cooldown")
    assert_equal("验证码发送过于频繁，请一分钟后再试", latest_state(fixture).message, "message")
    assert_true(not latest_state(fixture).sendEnabled, "send disabled")
end

function tests.code_request_network_failure_without_problem_allows_retry()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:requestCode("13800138000", 100)
    fixture.codeCalls[1].callback(false, nil)

    assert_equal("ERROR", latest_state(fixture).phase, "phase")
    assert_equal(0, latest_state(fixture).remainingSeconds, "cooldown")
    assert_equal("网络连接失败，请稍后重试", latest_state(fixture).message, "message")
    assert_true(latest_state(fixture).sendEnabled, "send enabled")
end

function tests.invalid_code_never_sends_login_request()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:submitCode("13800138000", "12x", 100)

    assert_equal(0, #fixture.loginCalls, "login request count")
    assert_equal("请输入6位数字验证码", latest_state(fixture).message, "message")
end

function tests.registration_uses_separate_request_and_reports_existing_account()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:submitRegister("13800138000", "246810", 100)

    assert_equal(1, #fixture.registerCalls, "register request count")
    assert_equal(0, #fixture.loginCalls, "login request count")
    assert_equal("REGISTERING", latest_state(fixture).phase, "register phase")

    fixture.registerCalls[1].callback(false, {code = "AUTH_ACCOUNT_EXISTS"})
    assert_equal("ERROR", latest_state(fixture).phase, "existing account phase")
    assert_equal("该手机号已经注册", latest_state(fixture).message, "existing account message")
end

function tests.failed_login_keeps_phone_and_allows_retry()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:submitCode("13800138000", "111111", 100)
    fixture.loginCalls[1].callback(false, {code = "AUTH_INVALID_CREDENTIAL"})

    assert_equal("ERROR", latest_state(fixture).phase, "phase")
    assert_equal("13800138000", latest_state(fixture).phone, "phone")
    assert_equal("手机号或验证码错误", latest_state(fixture).message, "message")
    assert_true(latest_state(fixture).loginEnabled, "login enabled")
end

function tests.unknown_backend_detail_is_never_shown()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:submitCode("13800138000", "111111", 100)
    fixture.loginCalls[1].callback(false, {
        code = "INTERNAL_DATABASE_FAILURE",
        detail = "relation auth_secret does not exist"
    })

    assert_equal("登录服务暂时不可用", latest_state(fixture).message, "safe message")
end

function tests.late_callback_from_closed_view_is_ignored()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:requestCode("13800138000", 100)
    local callback = fixture.codeCalls[1].callback
    fixture.module:close(101)
    fixture.module:open(102)
    callback(true, {challengeId = "late", expiresIn = 300})

    assert_equal("IDLE", latest_state(fixture).phase, "current phase")
    assert_equal(0, latest_state(fixture).remainingSeconds, "current cooldown")
end

function tests.success_saves_session_and_publishes_once()
    local fixture = new_fixture()
    fixture.module:open(100)
    fixture.module:submitCode("13800138000", "246810", 100)
    local callback = fixture.loginCalls[1].callback
    local tokens = {
        accessToken = "access-token",
        refreshToken = "refresh-token",
        tokenType = "Bearer",
        expiresIn = 900
    }
    callback(true, tokens)
    callback(true, tokens)

    assert_equal(1, #fixture.saved, "saved sessions")
    assert_equal("refresh-token", fixture.saved[1].refreshToken, "saved refresh token")
    assert_equal(1, #fixture.events, "published events")
    assert_equal(fixture.eventsContract.AUTH_SUCCEEDED, fixture.events[1].event, "event")
    assert_true(latest_state(fixture).authenticated, "authenticated")
end

local names = {}
for name in pairs(tests) do
    table.insert(names, name)
end
table.sort(names)

for _, name in ipairs(names) do
    tests[name]()
end

print(string.format("phone_login_spec: %d tests passed", #names))
