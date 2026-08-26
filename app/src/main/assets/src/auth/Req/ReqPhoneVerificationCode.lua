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

function Request:execute(phone, callback)
    self.http:post(
        "/api/v1/auth/otp/request",
        Json.encode({phoneNumber = phone}),
        function(status, body)
            local value = decode(body)
            callback(status == 202, value)
        end
    )
end

return Request
