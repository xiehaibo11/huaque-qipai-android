local Json = require("auth.Json")

local Request = {}
Request.__index = Request

function Request.new(http)
    return setmetatable({http = assert(http, "http is required")}, Request)
end

function Request:execute(refresh_token, callback)
    self.http:post(
        "/api/v1/auth/logout",
        Json.encode({refreshToken = refresh_token}),
        function(status)
            if callback then
                callback(status == 204)
            end
        end
    )
end

return Request
