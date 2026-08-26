package.path = "app/src/main/assets/src/?.lua;app/src/main/assets/src/?/init.lua;" .. package.path

local Json = require("auth.Json")

local requests = {}
local emitted = {}
local stored = {}
local now = 1000

function platform_http_post(request_id, path, body)
    requests[#requests + 1] = {id = request_id, path = path, body = body}
end

function platform_store_get(key)
    return stored[key]
end

function platform_store_set(key, value)
    stored[key] = value
end

function platform_emit(state)
    emitted[#emitted + 1] = state
end

function platform_now_seconds()
    return now
end

local function assert_equal(expected, actual, label)
    if expected ~= actual then
        error(string.format("%s: expected %s, got %s", label, tostring(expected), tostring(actual)), 2)
    end
end

local function reset(refresh_token)
    requests = {}
    emitted = {}
    stored = {}
    package.loaded["auth.Manager"] = nil
    if refresh_token then
        stored["auth.session"] = Json.encode({
            refreshToken = refresh_token,
            tokenType = "Bearer",
            expiresIn = 900,
            issuedAt = 100
        })
    end
end

local function new_manager()
    return require("auth.Manager").new()
end

reset("old-refresh")
local manager = new_manager()
manager:dispatch("restore", "", "", now)
assert_equal(1, #requests, "refresh request count")
assert_equal("/api/v1/auth/refresh", requests[1].path, "refresh path")
assert_equal("old-refresh", Json.decode(requests[1].body).refreshToken, "refresh body")
assert_equal("RESTORING", emitted[#emitted].phase, "restoring phase")

manager:onHttpResult(
        requests[1].id,
        200,
        '{"accessToken":"new-access","refreshToken":"new-refresh","tokenType":"Bearer","expiresIn":900}')
assert_equal("new-access", manager:accessToken(), "restored access token")
assert_equal("RESTORED", emitted[#emitted].phase, "authenticated phase")
assert_equal(true, emitted[#emitted].authenticated, "authenticated state")
assert_equal("new-refresh", Json.decode(stored["auth.session"]).refreshToken, "rotated refresh token")

reset("network-refresh")
manager = new_manager()
manager:dispatch("restore", "", "", now)
manager:onHttpResult(requests[1].id, 0, '{"code":"NETWORK_ERROR"}')
assert_equal("RESTORE_RETRY", emitted[#emitted].phase, "network retry phase")
assert_equal("network-refresh", Json.decode(stored["auth.session"]).refreshToken, "network keeps session")

reset("invalid-refresh")
manager = new_manager()
manager:dispatch("restore", "", "", now)
manager:onHttpResult(requests[1].id, 401, '{"code":"AUTH_REFRESH_REUSED"}')
assert_equal("RESTORE_REQUIRED", emitted[#emitted].phase, "invalid session phase")
assert_equal("", stored["auth.session"], "invalid session cleared")

reset(nil)
manager = new_manager()
manager:dispatch("restore", "", "", now)
assert_equal(0, #requests, "missing session request count")
assert_equal("RESTORE_REQUIRED", emitted[#emitted].phase, "missing session phase")

reset("switch-account-refresh")
manager = new_manager()
manager:dispatch("logout", "", "", now)
assert_equal(1, #requests, "logout request count")
assert_equal("/api/v1/auth/logout", requests[1].path, "logout path")
assert_equal(
        "switch-account-refresh",
        Json.decode(requests[1].body).refreshToken,
        "logout body")
assert_equal("", stored["auth.session"], "switch account clears persisted session")
assert_equal(nil, manager.sessionStore:current(), "switch account clears active session")

print("session_restore_spec: 5 tests passed")
