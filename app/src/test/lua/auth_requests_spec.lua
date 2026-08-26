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

function tests.verification_code_request_uses_current_backend_contract()
    local Json = require("auth.Json")
    local Req = require("auth.Req.ReqPhoneVerificationCode")
    local captured
    local result
    local http = {
        post = function(_, path, body, callback)
            captured = {path = path, body = body}
            callback(202, '{"challengeId":"challenge-1","expiresIn":300}')
        end
    }

    Req.new(http):execute("13800138000", function(ok, value)
        result = {ok = ok, value = value}
    end)

    assert_equal("/api/v1/auth/otp/request", captured.path, "path")
    assert_equal("13800138000", Json.decode(captured.body).phoneNumber, "phone body")
    assert_true(result.ok, "request succeeded")
    assert_equal("challenge-1", result.value.challengeId, "challenge id")
    assert_equal(300, result.value.expiresIn, "expiry")
end

function tests.phone_login_request_returns_complete_token_pair()
    local Json = require("auth.Json")
    local Req = require("auth.Req.ReqPhoneLogin")
    local captured
    local result
    local http = {
        post = function(_, path, body, callback)
            captured = {path = path, body = body}
            callback(200, '{"accessToken":"a","refreshToken":"r","tokenType":"Bearer","expiresIn":900}')
        end
    }

    Req.new(http):execute("13800138000", "246810", function(ok, value)
        result = {ok = ok, value = value}
    end)

    local body = Json.decode(captured.body)
    assert_equal("/api/v1/auth/otp/verify", captured.path, "path")
    assert_equal("13800138000", body.phoneNumber, "phone body")
    assert_equal("246810", body.code, "code body")
    assert_true(result.ok, "login succeeded")
    assert_equal("a", result.value.accessToken, "access token")
    assert_equal("r", result.value.refreshToken, "refresh token")
end

function tests.phone_registration_uses_independent_remote_contract()
    local Json = require("auth.Json")
    local Req = require("auth.Req.ReqPhoneRegister")
    local captured
    local result
    local http = {
        post = function(_, path, body, callback)
            captured = {path = path, body = body}
            callback(200, '{"accessToken":"a","refreshToken":"r","tokenType":"Bearer","expiresIn":900}')
        end
    }

    Req.new(http):execute("13800138000", "246810", function(ok, value)
        result = {ok = ok, value = value}
    end)

    local body = Json.decode(captured.body)
    assert_equal("/api/v1/auth/otp/register", captured.path, "registration path")
    assert_equal("13800138000", body.phoneNumber, "registration phone")
    assert_equal("246810", body.code, "registration code")
    assert_true(result.ok, "registration succeeded")
end

function tests.problem_details_code_is_preserved_for_business_mapping()
    local Req = require("auth.Req.ReqPhoneLogin")
    local result
    local http = {
        post = function(_, _, _, callback)
            callback(401, '{"code":"AUTH_INVALID_CREDENTIAL","detail":"手机号或验证码错误"}')
        end
    }

    Req.new(http):execute("13800138000", "111111", function(ok, value)
        result = {ok = ok, value = value}
    end)

    assert_true(not result.ok, "login failed")
    assert_equal("AUTH_INVALID_CREDENTIAL", result.value.code, "error code")
end

function tests.malformed_success_payloads_are_rejected()
    local Req = require("auth.Req.ReqPhoneLogin")
    local payloads = {
        '{"accessToken":null,"refreshToken":null,"tokenType":null,"expiresIn":null}',
        '{"accessToken":"","refreshToken":"r","tokenType":"Bearer","expiresIn":900}',
        '{"accessToken":"a","refreshToken":"","tokenType":"Bearer","expiresIn":900}',
        '{"accessToken":"a","refreshToken":"r","tokenType":"Other","expiresIn":900}',
        '{"accessToken":"a","refreshToken":"r","tokenType":"Bearer","expiresIn":0}',
        '{"accessToken":"a","refreshToken":"r","tokenType":"Bearer","expiresIn":"900"}'
    }

    for index, payload in ipairs(payloads) do
        local result
        local http = {
            post = function(_, _, _, callback)
                callback(200, payload)
            end
        }
        Req.new(http):execute("13800138000", "246810", function(ok, value)
            result = {ok = ok, value = value}
        end)
        assert_true(not result.ok, "malformed payload " .. index)
        assert_equal("SERVICE_UNAVAILABLE", result.value.code, "malformed error " .. index)
    end
end

function tests.refresh_session_is_written_once_and_access_token_stays_in_memory()
    local Json = require("auth.Json")
    local SessionStore = require("auth.SessionStore")
    local writes = {}
    local storage = {
        get = function()
            return nil
        end,
        set = function(_, key, value)
            table.insert(writes, {key = key, value = value})
        end
    }
    local session = {
        accessToken = "a",
        refreshToken = "r",
        tokenType = "Bearer",
        expiresIn = 900,
        issuedAt = 100
    }

    local sessionStore = SessionStore.new(storage)
    sessionStore:save(session)

    assert_equal(1, #writes, "write count")
    assert_equal("auth.session", writes[1].key, "storage key")
    local stored = Json.decode(writes[1].value)
    assert_equal(nil, stored.accessToken, "persisted access token")
    assert_equal("r", stored.refreshToken, "stored refresh token")
    assert_equal("a", sessionStore:current().accessToken, "in-memory access token")
end

local names = {}
for name in pairs(tests) do
    table.insert(names, name)
end
table.sort(names)

for _, name in ipairs(names) do
    tests[name]()
end

print(string.format("auth_requests_spec: %d tests passed", #names))
