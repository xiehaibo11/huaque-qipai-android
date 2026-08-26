local State = {}

function State.new()
    return {
        visible = false,
        phase = "IDLE",
        phone = "",
        message = "",
        cooldownUntil = 0,
        authenticated = false
    }
end

function State.snapshot(state, now)
    local remaining = math.max(0, math.ceil(state.cooldownUntil - now))
    local requesting = state.phase == "REQUESTING_CODE"
    local verifying = state.phase == "VERIFYING"
    local busy = requesting or verifying
    return {
        visible = state.visible,
        phase = state.phase,
        phone = state.phone,
        message = state.message,
        remainingSeconds = remaining,
        sendEnabled = not busy and not state.authenticated and remaining == 0,
        loginEnabled = not busy and not state.authenticated,
        authenticated = state.authenticated
    }
end

return State
