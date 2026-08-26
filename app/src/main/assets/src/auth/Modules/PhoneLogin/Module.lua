local Events = require("auth.Events")
local PhoneLoginState = require("auth.Models.PhoneLoginState")

local Module = {}
Module.__index = Module

local ERROR_MESSAGES = {
    AUTH_INVALID_CREDENTIAL = "手机号或验证码错误",
    AUTH_OTP_EXPIRED = "验证码已过期，请重新获取",
    AUTH_OTP_ATTEMPTS_EXCEEDED = "验证码尝试次数过多，请重新获取",
    AUTH_OTP_RATE_LIMITED = "验证码发送过于频繁，请稍后再试",
    VALIDATION_FAILED = "手机号或验证码格式不正确",
    NETWORK_ERROR = "网络连接失败，请稍后重试",
    SERVICE_UNAVAILABLE = "登录服务暂时不可用"
}

local function normalize_phone(raw_phone)
    local phone = tostring(raw_phone or ""):gsub("[%s%-]", "")
    phone = phone:gsub("^%+86", "")
    phone = phone:gsub("^86(%d%d%d%d%d%d%d%d%d%d%d)$", "%1")
    if #phone ~= 11 or not phone:match("^1[3-9]%d+$") then
        return nil
    end
    return phone
end

local function error_message(problem)
    if type(problem) ~= "table" then
        return ERROR_MESSAGES.NETWORK_ERROR
    end
    if problem.code == "AUTH_OTP_RATE_LIMITED"
            and type(problem.detail) == "string"
            and problem.detail ~= "" then
        return problem.detail
    end
    return ERROR_MESSAGES[problem.code] or ERROR_MESSAGES.SERVICE_UNAVAILABLE
end

function Module.new(dependencies)
    assert(dependencies and dependencies.requestCode, "requestCode is required")
    assert(dependencies.requestLogin, "requestLogin is required")
    assert(dependencies.sessionStore, "sessionStore is required")
    assert(dependencies.publish, "publish is required")
    assert(dependencies.emitState, "emitState is required")
    assert(dependencies.now, "now is required")
    return setmetatable({
        codeRequest = dependencies.requestCode,
        loginRequest = dependencies.requestLogin,
        sessionStore = dependencies.sessionStore,
        publish = dependencies.publish,
        emitStateCallback = dependencies.emitState,
        now = dependencies.now,
        state = PhoneLoginState.new(),
        generation = 0,
        lastNow = 0
    }, Module)
end

function Module:emit(now)
    self.lastNow = now or self.lastNow
    self.emitStateCallback(PhoneLoginState.snapshot(self.state, self.lastNow))
end

function Module:open(now)
    self.generation = self.generation + 1
    self.state.visible = true
    self.state.phase = "IDLE"
    self.state.message = ""
    self.state.authenticated = false
    self:emit(now)
end

function Module:close(now)
    self.generation = self.generation + 1
    self.state.visible = false
    self.state.phase = "IDLE"
    self.state.message = ""
    self:emit(now)
end

function Module:requestCode(raw_phone, now)
    if self.state.phase == "REQUESTING_CODE" or self.state.phase == "VERIFYING" then
        return
    end
    if PhoneLoginState.snapshot(self.state, now).remainingSeconds > 0 then
        return
    end
    local phone = normalize_phone(raw_phone)
    if not phone then
        self.state.phase = "ERROR"
        self.state.message = "请输入正确的11位手机号"
        self:emit(now)
        return
    end

    self.state.phone = phone
    self.state.phase = "REQUESTING_CODE"
    self.state.message = "正在发送验证码…"
    self:emit(now)
    local generation = self.generation
    self.codeRequest:execute(phone, function(ok, value)
        if generation ~= self.generation or self.state.phase ~= "REQUESTING_CODE" then
            return
        end
        local completed_at = self.now()
        if ok then
            self.state.phase = "CODE_SENT"
            self.state.message = "验证码已发送"
            self.state.cooldownUntil = completed_at + 60
            self.publish(Events.PHONE_CODE_SENT, {phone = phone})
        else
            self.state.phase = "ERROR"
            self.state.message = error_message(value)
            self.state.cooldownUntil = type(value) == "table"
                    and value.code == "AUTH_OTP_RATE_LIMITED"
                    and completed_at + 60 or 0
        end
        self:emit(completed_at)
    end)
end

function Module:submitCode(raw_phone, code, now)
    if self.state.phase == "VERIFYING"
            or self.state.phase == "REQUESTING_CODE"
            or self.state.authenticated then
        return
    end
    local phone = normalize_phone(raw_phone)
    if not phone then
        self.state.phase = "ERROR"
        self.state.message = "请输入正确的11位手机号"
        self:emit(now)
        return
    end
    code = tostring(code or "")
    if not code:match("^%d%d%d%d%d%d$") then
        self.state.phase = "ERROR"
        self.state.message = "请输入6位数字验证码"
        self.state.phone = phone
        self:emit(now)
        return
    end

    self.state.phone = phone
    self.state.phase = "VERIFYING"
    self.state.message = "正在登录…"
    self:emit(now)
    local generation = self.generation
    self.loginRequest:execute(phone, code, function(ok, value)
        if generation ~= self.generation or self.state.phase ~= "VERIFYING" then
            return
        end
        local completed_at = self.now()
        if not ok then
            self.state.phase = "ERROR"
            self.state.message = error_message(value)
            self.publish(Events.PHONE_LOGIN_FAILED, value)
            self:emit(completed_at)
            return
        end

        local session = {
            accessToken = value.accessToken,
            refreshToken = value.refreshToken,
            tokenType = value.tokenType,
            expiresIn = value.expiresIn,
            issuedAt = completed_at
        }
        self.sessionStore:save(session)
        self.state.phase = "AUTHENTICATED"
        self.state.message = "登录成功"
        self.state.authenticated = true
        self.publish(Events.AUTH_SUCCEEDED, session)
        self:emit(completed_at)
    end)
end

function Module:tick(now)
    local before = PhoneLoginState.snapshot(self.state, self.lastNow).remainingSeconds
    local after = PhoneLoginState.snapshot(self.state, now).remainingSeconds
    if before ~= after then
        self:emit(now)
    else
        self.lastNow = now
    end
end

return Module
