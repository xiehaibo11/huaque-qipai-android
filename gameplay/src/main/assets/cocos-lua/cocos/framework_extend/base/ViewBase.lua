
local AutoBindUITools = require("cocos.framework_extend.tools.AutoBindUITools")

local ViewBase = class("ViewBase", function ()
   return ccui.Layout:create()
end)

--修改为csb
function ViewBase:ctor(...)
    local csbRes = nil
    csbRes = ViewBase.mergeCsbCfg(self,csbRes)

    --local csbRes = self.RAW_RES_BINDING_CSB--rawget(self.class, "RAW_RES_BINDING_CSB")
    if csbRes then
        self._csbRes = csbRes
		self._autoBindUITools = AutoBindUITools.new()
		self._autoBindUITools:setDealVarNameFunc(handler(self, self.getBingNameByTag))
		self._autoBindUITools:setDealFuncNameFunc(handler(self, self.getBingFuncByTag))
        
        self:createResourceNode(csbRes)
    end

    if self.onCreate then self:onCreate(...) end

    self:setNodeEventEnabled(true)
end

function ViewBase.mergeCsbCfg(object,retCsbRes)
    if  object == nil  then
        return retCsbRes
    end

    local bingCSB = object.RAW_RES_BINDING_CSB
    if bingCSB == nil then
        return ViewBase.mergeCsbCfg(object.super,retCsbRes)
        
    end
    if bingCSB.binding == nil then
        return ViewBase.mergeCsbCfg(object.super,retCsbRes)
    end

    retCsbRes = retCsbRes or bingCSB

    for k, v in pairs(object.RAW_RES_BINDING_CSB.binding) do
        if retCsbRes.binding[k] == nil then
            retCsbRes.binding[k] = v
        end
    end
    return ViewBase.mergeCsbCfg(object.super,retCsbRes)
end

function ViewBase:getResourceNode()
    return self.resourceNode_
end

function ViewBase:setNodeEventEnabled(enable)
    self:registerScriptHandler(function(state)
        if state == "enter" then
            self:onEnter()
        elseif state == "exit"then
            self:onExit()
        elseif state == "enterTransitionFinish" then
            self:onEnterTransitionFinish()
        elseif state == "exitTransitionStart" then
            self:onExitTransitionStart()
        elseif state == "cleanup" then
            self:onCleanup()
        end
    end)
end

function ViewBase:onNodeEvent(eventName, callback)
    if "enter" == eventName then
        self.onEnterCallback_ = callback
    elseif "exit" == eventName then
        self.onExitCallback_ = callback
    elseif "enterTransitionFinish" == eventName then
        self.onEnterTransitionFinishCallback_ = callback
    elseif "exitTransitionStart" == eventName then
        self.onExitTransitionStartCallback_ = callback
    elseif "cleanup" == eventName then
        self.onCleanupCallback_ = callback
    end
    self:enableNodeEvents()
end

function ViewBase:createResourceNode(csbRes)
    if csbRes.csb then
        --local csbNode = cc.CSLoader:createNodeWithVisibleSize(csbRes.csb)
		local csbNode = self._autoBindUITools:loadCSB(csbRes.csb, self)

        if csbRes.width and csbRes.height then
            csbNode:setContentSize(csbRes.width,csbRes.height)
            self:setContentSize(csbRes.width,csbRes.height)
        else
            self:setContentSize(csbNode:getContentSize())
        end
        csbNode:setAnchorPoint(0,0)
        self.resourceNode_ = csbNode
        self:addChild(self.resourceNode_)
    end
    if csbRes.binding then
        self:createResourceBinding(self,self.resourceNode_,csbRes.binding)
    end
end

function ViewBase:createResourceBinding(view, csbNode, binding)
	assert(self.resourceNode_, "ViewBase:createResourceBinding() - not load resource node")

	for k, nodeBinding in pairs(binding) do
		local node = self[nodeBinding.name]
		if node then
			--绑定层级关系
			if nodeBinding.ZOrder then
				node:setLocalZOrder(nodeBinding.ZOrder)
			end
		end
	end
end

function ViewBase:getBingNameByTag(tag)
    local csbRes = self._csbRes --rawget(self.class, "RAW_RES_BINDING_CSB")
	if csbRes and csbRes.binding and csbRes.binding[tag] then
		return csbRes.binding[tag].name or nil
	end
end

function ViewBase:getBingFuncByTag(tag)
    local csbRes = self._csbRes --rawget(self.class, "RAW_RES_BINDING_CSB")
	if csbRes and csbRes.binding and csbRes.binding[tag] then
		return csbRes.binding[tag].events or nil
	end
end

--隐藏不显示的csb节点
function ViewBase:hideCsbTags(tags)
    local  csbNode = self:getResourceNode()
    if csbNode and tags then
        for _,v in pairs(tags) do
            local  node  = csbNode:getChildByName(v)
            if node then
                node:hide()
            end
        end
    end
end

function ViewBase:showCsbTags(tags)
    local  csbNode = self:getResourceNode()
    if csbNode and tags then
        for _,v in pairs(tags) do
            local  node  = csbNode:getChildByName(v)
            if node then
                node:show()
            end
        end
    end
end

function ViewBase:onEnter()
    
end 

function ViewBase:onExit()
    
end

function ViewBase:onCleanup()
    
end

function ViewBase:onEnterTransitionFinish()
    
end 

function ViewBase:onExitTransitionStart()
    
end

return ViewBase
