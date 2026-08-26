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

local function valid_token_pair(value)
    return type(value.accessToken) == "string"
            and value.accessToken ~= ""
            and type(value.refreshToken) == "string"
            and value.refreshToken ~= ""
            and value.tokenType == "Bearer"
            and type(value.expiresIn) == "number"
            and value.expiresIn > 0
            and value.expiresIn < math.huge
end

function Request:execute(refresh_token, callback)
    self.http:post(
        "/api/v1/auth/refresh",
        Json.encode({refreshToken = refresh_token}),
        function(status, body)
            local value = decode(body)
            if status == 200 and valid_token_pair(value) then
                callback(true, value)
                return
            end
            if status == 200 then
                value = {code = "SERVICE_UNAVAILABLE"}
            end
            callback(false, value)
        end
    )
end

return Request
