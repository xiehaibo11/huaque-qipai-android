package.path = "app/src/main/assets/src/?.lua;app/src/main/assets/src/?/init.lua;" .. package.path

local requests = {}
local now = 100

function platform_http_post(request_id, path, body)
    table.insert(requests, {id = request_id, path = path, body = body})
end

function platform_store_get(_)
    return nil
end

function platform_store_set(_, _)
end

function platform_emit(_)
end

function platform_now_seconds()
    return now
end

local function assert_equal(expected, actual, label)
    if expected ~= actual then
        error(string.format("%s: expected %s, got %s", label, tostring(expected), tostring(actual)), 2)
    end
end

local Manager = require("auth.Manager")

local fresh = Manager.new()
assert_equal("", fresh:accessToken(), "fresh manager token")

fresh:dispatch("open", "", "", now)
fresh:dispatch("submitCode", "13800138000", "246810", now)
local login_request = requests[#requests]
fresh:onHttpResult(login_request.id, 200,
    '{"accessToken":"access-token","refreshToken":"refresh-token","tokenType":"Bearer","expiresIn":900}')
assert_equal("access-token", fresh:accessToken(), "authenticated token")

local failed = Manager.new()
failed:dispatch("open", "", "", now)
failed:dispatch("submitCode", "13800138000", "111111", now)
local failed_request = requests[#requests]
failed:onHttpResult(failed_request.id, 401, '{"code":"AUTH_INVALID_CREDENTIAL"}')
assert_equal("", failed:accessToken(), "failed login token")

print("manager_access_token_spec: 3 tests passed")
