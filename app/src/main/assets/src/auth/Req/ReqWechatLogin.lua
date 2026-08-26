local Json = require("auth.Json")

local Request = {}
Request.__index = Request

function Request.new(http)
    return setmetatable({http = assert(http, "http is required")}, Request)
end

local function decode(body)
    local ok, value = pcall(Json.decode, body or "")
    if ok and type(value) == "table" then
        return value
    end
    return {code = "SERVICE_UNAVAILABLE"}
end

local function non_empty_string(value)
    return type(value) == "string" and value ~= ""
end

local function valid_token_pair(value)
    return non_empty_string(value.accessToken)
            and non_empty_string(value.refreshToken)
            and value.tokenType == "Bearer"
            and type(value.expiresIn) == "number"
            and value.expiresIn > 0
            and value.expiresIn < math.huge
end

function Request:execute(code, callback)
    self.http:post(
        "/api/v1/auth/providers/wechat/login",
        Json.encode({credential = code}),
        function(status, body)
            local value = decode(body)
            if status == 200 and valid_token_pair(value) then
                callback(true, value)
            elseif status == 200 then
                callback(false, {code = "SERVICE_UNAVAILABLE"})
            else
                callback(false, value)
            end
        end
    )
end

return Request
