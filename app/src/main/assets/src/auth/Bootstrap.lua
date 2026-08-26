local Manager = require("auth.Manager")
local manager = Manager.new()

function auth_dispatch(action, phone, code, now)
    manager:dispatch(action, phone, code, now)
end

function auth_on_http_result(request_id, status, body)
    manager:onHttpResult(request_id, status, body)
end

function auth_close(now)
    manager:close(now)
end

function auth_session_value(key)
    local session = manager.sessionStore:current()
    if not session then
        return nil
    end
    return session[key]
end

function auth_access_token()
    return manager:accessToken()
end
