local Node = cc.Node

function Node:hitTestTouch(point)
    local nsp = self:convertToNodeSpace(point)
    local rect = self:getContentSize()
    rect.x = 0
    rect.y = 0
    if cc.rectContainsPoint(rect, nsp) then
        return true
    end
    return false
end