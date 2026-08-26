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

local tests = {}

function tests.request_sends_only_the_temporary_code_to_the_backend()
    local Json = require("auth.Json")
    local Request = require("auth.Req.ReqWechatLogin")
    local captured
    local result
    local http = {
        post = function(_, path, body, callback)
            captured = {path = path, body = body}
            callback(200, '{"accessToken":"a","refreshToken":"r","tokenType":"Bearer","expiresIn":900}')
        end
    }

    Request.new(http):execute("temporary-code", function(ok, value)
        result = {ok = ok, value = value}
    end)

    local body = Json.decode(captured.body)
    assert_equal("/api/v1/auth/providers/wechat/login", captured.path, "path")
    assert_equal("temporary-code", body.credential, "credential")
    assert_equal(nil, body.appSecret, "client secret")
    assert_true(result.ok, "request succeeded")
    assert_equal("a", result.value.accessToken, "access token")
end

function tests.success_saves_session_and_publishes_once()
    local WechatLogin = require("auth.Modules.WechatLogin.Module")
    local Events = require("auth.Events")
    local request
    local saved = {}
    local published = {}
    local states = {}
    local module = WechatLogin.new({
        requestLogin = {
            execute = function(_, code, callback)
                request = {code = code, callback = callback}
            end
        },
        sessionStore = {
            save = function(_, session)
                table.insert(saved, session)
            end
        },
        publish = function(event, payload)
            table.insert(published, {event = event, payload = payload})
        end,
        emitState = function(state)
            table.insert(states, state)
        end,
        now = function()
            return 212
        end
    })

    module:login("temporary-code", 200)
    assert_equal("temporary-code", request.code, "authorization code")
    assert_equal("WECHAT_VERIFYING", states[#states].phase, "pending phase")
    request.callback(true, {
        accessToken = "access-token",
        refreshToken = "refresh-token",
        tokenType = "Bearer",
        expiresIn = 900
    })
    request.callback(true, {
        accessToken = "duplicate",
        refreshToken = "duplicate",
        tokenType = "Bearer",
        expiresIn = 900
    })

    assert_equal(1, #saved, "saved sessions")
    assert_equal(212, saved[1].issuedAt, "issue time")
    assert_equal(1, #published, "published events")
    assert_equal(Events.AUTH_SUCCEEDED, published[1].event, "event")
    assert_true(states[#states].authenticated, "authenticated")
end

function tests.failed_exchange_does_not_create_a_session()
    local WechatLogin = require("auth.Modules.WechatLogin.Module")
    local request
    local saved = 0
    local states = {}
    local module = WechatLogin.new({
        requestLogin = {
            execute = function(_, _, callback)
                request = callback
            end
        },
        sessionStore = {
            save = function()
                saved = saved + 1
            end
        },
        publish = function() end,
        emitState = function(state)
            table.insert(states, state)
        end,
        now = function()
            return 100
        end
    })

    module:login("expired-code", 100)
    request(false, {code = "AUTH_INVALID_CREDENTIAL"})

    assert_equal(0, saved, "saved sessions")
    assert_equal("WECHAT_ERROR", states[#states].phase, "error phase")
    assert_equal("微信授权已失效，请重新登录", states[#states].message, "safe error")
end

local names = {}
for name in pairs(tests) do
    table.insert(names, name)
end
table.sort(names)

for _, name in ipairs(names) do
    tests[name]()
end

print(string.format("wechat_login_spec: %d tests passed", #names))
