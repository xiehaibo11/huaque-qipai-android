local HttpClient = {}
HttpClient.__index = HttpClient

function HttpClient.new()
    return setmetatable({nextRequestId = 1, pending = {}}, HttpClient)
end

function HttpClient:post(path, body, callback)
    local request_id = tostring(self.nextRequestId)
    self.nextRequestId = self.nextRequestId + 1
    self.pending[request_id] = callback
    platform_http_post(request_id, path, body)
end

function HttpClient:complete(request_id, status, body)
    local callback = self.pending[tostring(request_id)]
    if not callback then
        return
    end
    self.pending[tostring(request_id)] = nil
    callback(tonumber(status) or 0, body or "")
end

function HttpClient:close()
    self.pending = {}
end

return HttpClient
