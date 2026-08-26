
local EventProxy = class("EventProxy")

function EventProxy:ctor(eventDispatcher, view)
    self.eventDispatcher_ = eventDispatcher
    self.handles_ = {}

   if view and view.registerScriptHandler then
		cc(view):registerScriptHandler(function(state)
            if state == "enter"  then
                if view.onEnter then
                    view:onEnter()
                end
			elseif state == "exit" then
                self:removeAllEventListeners()
                if view.onExit then
                    view:onExit()
                end
            elseif state == "enterTransitionFinish" then
                if view.onEnterTransitionFinish then
                    view:onEnterTransitionFinish()
                end
            elseif state == "exitTransitionStart" then
                if view.onExitTransitionStart then
                    view:onExitTransitionStart()
                end
			elseif state == "cleanup" then
                self:removeAllEventListeners()
                if view.onCleanup then
                    view:onCleanup()
                end
			end
		end)
   end
end

function EventProxy:addEventListener(eventName, listener, data)
    local handle = self.eventDispatcher_:addEventListener(eventName, listener, data)
    self.handles_[#self.handles_ + 1] = {eventName, handle}
    return self, handle
end

function EventProxy:removeEventListener(eventHandle)
    self.eventDispatcher_:removeEventListener(eventHandle)
    for index, handle in pairs(self.handles_) do
        if handle[2] == eventHandle then
            table.remove(self.handles_, index)
            break
        end
    end
    return self
end

function EventProxy:removeAllEventListenersForEvent(eventName)
    for key, handle in pairs(self.handles_) do
        if handle[1] == eventName then
            self.eventDispatcher_:removeEventListenersByEvent(eventName)
            self.handles_[key] = nil
        end
    end
    return self
end

function EventProxy:getEventHandle(eventName)
    for key, handle in pairs(self.handles_) do
        if handle[1] == eventName then
            return handle[2]
        end
    end
end

function EventProxy:removeAllEventListeners()
    for _, handle in pairs(self.handles_) do
        self.eventDispatcher_:removeEventListener(handle[2])
    end
    self.handles_ = {}
    return self
end

return EventProxy
