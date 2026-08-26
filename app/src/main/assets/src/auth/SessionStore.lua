local Json = require("auth.Json")

local SessionStore = {}
SessionStore.__index = SessionStore

local STORAGE_KEY = "auth.session"

function SessionStore.new(storage)
    return setmetatable({storage = assert(storage, "storage is required"), activeSession = nil}, SessionStore)
end

function SessionStore:save(session)
    assert(session.accessToken and session.refreshToken, "complete token pair is required")
    self.activeSession = session
    self.storage:set(STORAGE_KEY, Json.encode({
        refreshToken = session.refreshToken,
        tokenType = session.tokenType,
        expiresIn = session.expiresIn,
        issuedAt = session.issuedAt
    }))
end

function SessionStore:current()
    return self.activeSession
end

function SessionStore:load()
    local value = self.storage:get(STORAGE_KEY)
    if not value or value == "" then
        return nil
    end
    local ok, session = pcall(Json.decode, value)
    if not ok or type(session) ~= "table" or not session.refreshToken then
        self:clear()
        return nil
    end
    self.activeSession = session
    return session
end

function SessionStore:clear()
    self.activeSession = nil
    self.storage:set(STORAGE_KEY, "")
end

return SessionStore
