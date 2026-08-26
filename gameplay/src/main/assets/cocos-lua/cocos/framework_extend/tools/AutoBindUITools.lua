
-----------------------------------------------------
-- @name AutoBindUITools
-- @author Stephen Cai
-- @date 2019-07-03
-- @brief 通过本工具加载csb,可以自动绑定变量和点击事件
--        这里以默认的匹配规则举例说明 cocos studio 命名格式 "_text@"  "_"表示是需要生成变量的节点,"@"表示是要生成点击事件绑定
--        "_text@" 生成的变量名为 "_text", 绑定的事件名字 "_textTouchEventCallBack(sender, eventType)"
--
-- @example(使用默认规则)
--          --加载csb
--          local AutoBindUITools = require("src.AutoBindUITools")
--          local autoBindUITools = AutoBindUITools.new()
--          local uiRoot = autoBind:loadCSB(csbPath, self)
--
--          --变量访问,以"_text"为例
--          self.ui._text:setString("AutoBindUITools")
--
--          --函数怎么写,以"_btn@"为例
--          function GameScene:_btnTouchEventCallBack(send, eventType)
--              print("name: " .. send:getName() .. "type: " .. eventType)
--          end
--
-- 就写这么多了,主要目的是简化 getChildByName()和addTouchEventListener()的使用
------------------------------------------------------

local AutoBindUITools = class("AutoBindUITools")

function AutoBindUITools:ctor()
    self._regex = ""
    self._varNameSpace = ""
    self._dealVarNameFunc = nil
    self._dealFuncNameFunc = nil
end

---------------------------------------------------------
-- @brief 默认使用的解析规则
-- 需要绑定的节点以"_"开头,需要绑定点击事件的结尾加个"@",需要的自己扩展呀
-- @return nil
---------------------------------------------------------
function AutoBindUITools:initDefault()
--    local dealFuncNameFunc = function(name)
--        local res = nil
--        local nameList = string.split(name, '@')
--        if #nameList == 2 then
--            res = nameList[1] .. "TouchEventCallBack"
--        end
--        return res
--    end

    self:init("//^_.*")
end

---------------------------------------------------------
-- @brief 加载一个CSB
-- @param string CSB路径
-- @param table 变量和函数绑定的对象
-- @return Node
---------------------------------------------------------
function AutoBindUITools:loadCSB(path, bindobj)
    local uiRoot = nil

    if self._regex == "" then
        self:initDefault()
    end

    if path then
        uiRoot = cc.CSLoader:createNodeWithVisibleSize(path)
        self:autoBind(uiRoot, bindobj)
    end
    return uiRoot
end

---------------------------------------------------------
-- @brief 设置匹配规则
-- @param string 正则表达式
-- @return nil
---------------------------------------------------------
function AutoBindUITools:setRegex(regex)
    if regex then
        self._regex = regex
    end
end

---------------------------------------------------------
-- @brief 生成的变量挂在对象的名字
-- @param 字符串名字
-- @return nil
---------------------------------------------------------
function AutoBindUITools:setVarNameSpace(nameSpace)
    if nameSpace then
        self._varNameSpace = nameSpace
    end
end

---------------------------------------------------------
-- @brief 设置CSB节点名字和将要生成的变量名字的映射关系
-- @param function(string)
-- @return nil
---------------------------------------------------------
function AutoBindUITools:setDealVarNameFunc(func)
    if func and type(func) == "function" then
        self._dealVarNameFunc = func
    end
end

---------------------------------------------------------
-- @brief 设置CSB节点名字和将要生成的函数名字的映射关系
-- @param function(string)
-- @return nil
---------------------------------------------------------
function AutoBindUITools:setDealFuncNameFunc(func)
    if func and type(func) == "function" then
        self._dealFuncNameFunc = func
    end
end

---------------------------------------------------------
-- @brief 初始化方法
-- @param string 正则匹配串
-- @param string 变量挂载的空间名字
-- @param function(string) 变量名处理函数
-- @param function(string) 函数名处理函数
-- @return nil
---------------------------------------------------------
function AutoBindUITools:init(regex, varNameSpace, dealVarNameFunc, dealFuncNameFunc)
    self:setRegex(regex)
    self:setVarNameSpace(varNameSpace)
    self:setDealVarNameFunc(dealVarNameFunc)
    self:setDealFuncNameFunc(dealFuncNameFunc)
end

function AutoBindUITools:_getBindVarName(name)
    return self._dealVarNameFunc and self._dealVarNameFunc(name) or name
end

function AutoBindUITools:_getBindTouchEventFuncName(name)
    return self._dealFuncNameFunc and self._dealFuncNameFunc(name) or name
end

---------------------------------------------------------
-- @brief 绑定逻辑处理
-- @param Node UI的根节点
-- @param table 变量和函数绑定的对象
-- @return nil
---------------------------------------------------------
function AutoBindUITools:autoBind(uiRoot, obj)
    if uiRoot and obj then
        local varSpace = obj
        local varNameSpace = self._varNameSpace
        local regex = self._regex

        if varNameSpace ~= "" then
            obj[varNameSpace] = obj[varNameSpace] or { }
            varSpace = obj[varNameSpace]
        end

        local children = cc.utils:findChildren(uiRoot, regex)

        for _, child in pairs(children) do
            local childName = child:getName()
            -- 绑定变量
            local varName = self:_getBindVarName(childName)
            if varName and varName ~= "" then
                varSpace[varName] = child
            end

            -- 绑定函数
            local funcName = self:_getBindTouchEventFuncName(childName)
            if funcName and funcName ~= "" then
                local callback = obj[funcName]
                if callback and type(callback) == "function" then
                    child:addTouchEventListener(handler(obj, callback))
                end
            end

        end
    end
end


return AutoBindUITools