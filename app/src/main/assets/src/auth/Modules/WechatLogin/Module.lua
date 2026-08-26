local Events = require("auth.Events")

local Module = {}
Module.__index = Module

local ERROR_MESSAGES = {
    AUTH_INVALID_CREDENTIAL = "微信授权已失效，请重新登录",
    AUTH_PROVIDER_UNAVAILABLE = "微信登录服务尚未启用",
    NETWORK_ERROR = "网络连接失败，请稍后重试",
    SERVICE_UNAVAILABLE = "微信登录服务暂时不可用"
}

local function snapshot(phase, message, authenticated)
    return {
        visible = false,
        phase = phase,
        phone = "",
        message = message or "",
        remainingSeconds = 0,
        sendEnabled = false,
        loginEnabled = false,
        authenticated = authenticated == true
    }
end

function Module.new(dependencies)
    assert(dependencies and dependencies.requestLogin, "requestLogin is required")
    assert(dependencies.sessionStore, "sessionStore is required")
    assert(dependencies.publish, "publish is required")
    assert(dependencies.emitState, "emitState is required")
    assert(dependencies.now, "now is required")
    return setmetatable({
        requestLogin = dependencies.requestLogin,
        sessionStore = dependencies.sessionStore,
        publish = dependencies.publish,
        emitState = dependencies.emitState,
        now = dependencies.now,
        generation = 0,
        phase = "IDLE"
    }, Module)
end

function Module:login(code, now)
    code = tostring(code or "")
    if self.phase == "WECHAT_VERIFYING" or code == "" then
        return
    end
    self.generation = self.generation + 1
    local generation = self.generation
    self.phase = "WECHAT_VERIFYING"
    self.emitState(snapshot(self.phase, "正在登录…", false))
    self.requestLogin:execute(code, function(ok, value)
        if generation ~= self.generation or self.phase ~= "WECHAT_VERIFYING" then
            return
        end
        if not ok then
            self.phase = "WECHAT_ERROR"
            local error_code = type(value) == "table" and value.code or "SERVICE_UNAVAILABLE"
            self.emitState(snapshot(
                self.phase,
                ERROR_MESSAGES[error_code] or ERROR_MESSAGES.SERVICE_UNAVAILABLE,
                false
            ))
            return
        end

        local session = {
            accessToken = value.accessToken,
            refreshToken = value.refreshToken,
            tokenType = value.tokenType,
            expiresIn = value.expiresIn,
            issuedAt = self.now()
        }
        self.phase = "WECHAT_AUTHENTICATED"
        self.sessionStore:save(session)
        self.publish(Events.AUTH_SUCCEEDED, session)
        self.emitState(snapshot(self.phase, "登录成功", true))
    end)
end

return Module
