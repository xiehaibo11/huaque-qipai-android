local Events = require("auth.Events")
local HttpClient = require("auth.HttpClient")
local SessionStore = require("auth.SessionStore")
local PhoneLogin = require("auth.Modules.PhoneLogin.Module")
local ReqPhoneVerificationCode = require("auth.Req.ReqPhoneVerificationCode")
local ReqPhoneLogin = require("auth.Req.ReqPhoneLogin")
local ReqLogoutSession = require("auth.Req.ReqLogoutSession")
local ReqRefreshSession = require("auth.Req.ReqRefreshSession")
local ReqWechatLogin = require("auth.Req.ReqWechatLogin")
local WechatLogin = require("auth.Modules.WechatLogin.Module")

local Manager = {}
Manager.__index = Manager

function Manager.new()
    local http = HttpClient.new()
    local storage = {
        get = function(_, key)
            return platform_store_get(key)
        end,
        set = function(_, key, value)
            platform_store_set(key, value)
        end
    }
    local session_store = SessionStore.new(storage)
    local manager = setmetatable({
        http = http,
        sessionStore = session_store,
        logoutRequest = ReqLogoutSession.new(http),
        refreshRequest = ReqRefreshSession.new(http),
        restoreInFlight = false
    }, Manager)
    manager.phoneLogin = PhoneLogin.new({
        requestCode = ReqPhoneVerificationCode.new(http),
        requestLogin = ReqPhoneLogin.new(http),
        sessionStore = session_store,
        publish = function(event, payload)
            manager:onEvent(event, payload)
        end,
        emitState = function(state)
            platform_emit(state)
        end,
        now = function()
            return platform_now_seconds()
        end
    })
    manager.wechatLogin = WechatLogin.new({
        requestLogin = ReqWechatLogin.new(http),
        sessionStore = session_store,
        publish = function(event, payload)
            manager:onEvent(event, payload)
        end,
        emitState = function(state)
            platform_emit(state)
        end,
        now = function()
            return platform_now_seconds()
        end
    })
    return manager
end

function Manager:onEvent(event, payload)
    if event == Events.AUTH_SUCCEEDED then
        self.authenticatedSession = payload
    end
end

function Manager:dispatch(action, phone, code, now)
    now = tonumber(now) or 0
    if action == "open" then
        self.phoneLogin:open(now)
    elseif action == "close" then
        self.phoneLogin:close(now)
    elseif action == "requestCode" then
        self.phoneLogin:requestCode(phone, now)
    elseif action == "submitCode" then
        self.phoneLogin:submitCode(phone, code, now)
    elseif action == "tick" then
        self.phoneLogin:tick(now)
    elseif action == "restore" then
        self:restore(now)
    elseif action == "wechat" then
        self.wechatLogin:login(phone, now)
    elseif action == "logout" then
        local saved = self.sessionStore:load()
        if saved and type(saved.refreshToken) == "string" and saved.refreshToken ~= "" then
            self.logoutRequest:execute(saved.refreshToken)
        end
        self.sessionStore:clear()
        self.authenticatedSession = nil
        self.phoneLogin:close(now)
    end
end

local function emit_restore_state(phase, message, authenticated)
    platform_emit({
        visible = false,
        phase = phase,
        phone = "",
        message = message or "",
        remainingSeconds = 0,
        sendEnabled = false,
        loginEnabled = false,
        authenticated = authenticated == true
    })
end

function Manager:restore(now)
    if self.restoreInFlight then
        return
    end
    local saved = self.sessionStore:load()
    if not saved or type(saved.refreshToken) ~= "string" or saved.refreshToken == "" then
        emit_restore_state("RESTORE_REQUIRED", "", false)
        return
    end

    self.restoreInFlight = true
    emit_restore_state("RESTORING", "正在恢复登录状态…", false)
    self.refreshRequest:execute(saved.refreshToken, function(ok, value)
        self.restoreInFlight = false
        if ok then
            local session = {
                accessToken = value.accessToken,
                refreshToken = value.refreshToken,
                tokenType = value.tokenType,
                expiresIn = value.expiresIn,
                issuedAt = platform_now_seconds()
            }
            self.sessionStore:save(session)
            self:onEvent(Events.AUTH_SUCCEEDED, session)
            emit_restore_state("RESTORED", "", true)
            return
        end

        local code = type(value) == "table" and value.code or "SERVICE_UNAVAILABLE"
        if code == "AUTH_INVALID_CREDENTIAL" or code == "AUTH_REFRESH_REUSED" then
            self.sessionStore:clear()
            emit_restore_state("RESTORE_REQUIRED", "登录状态已失效", false)
            return
        end
        emit_restore_state("RESTORE_RETRY", "网络连接失败，正在重试…", false)
    end)
end

function Manager:onHttpResult(request_id, status, body)
    self.http:complete(request_id, status, body)
end

function Manager:accessToken()
    local session = self.sessionStore:current()
    if not session or session.tokenType ~= "Bearer" or type(session.accessToken) ~= "string" then
        return ""
    end
    return session.accessToken
end

function Manager:close(now)
    self.phoneLogin:close(now or 0)
    self.http:close()
end

return Manager
