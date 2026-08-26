local StringTool = {}

-- 截取标记flag之前的字符串
function StringTool.subStringBeforeFlag(str, flag)
    local index = string.find(str, flag)
    if not index then
        return ""
    end
    return string.sub(str, 1, index - 1)
end

-- 截取标记flag之后的字符串
function StringTool.subStringAfterFlag(str, flag)
    local index = string.find(str, flag)
    if not index then
        return ""
    end
    return string.sub(str, index + 1, #str)
end

--获取string的数字后缀
function StringTool.getNumberSuffixByString(string)
    local len = string.len(string);
    local lastNumber = -1
    for i = 1 , len do
        local tempNumber = tonumber(string.sub(string,- i ))

        if tempNumber then
            lastNumber = tempNumber
        else
            return lastNumber
        end
    end
    return lastNumber
end

--获取string的数字前缀
function StringTool.getNumberPrefixByString(string)
    local len = string.len(string);
    local lastNumber = nil
    for i = 1 , len do
        local tempNumber = tonumber(string.sub(string, 1, i ))
        if tempNumber then
            lastNumber = tempNumber
        else
            return lastNumber
        end
    end
    return lastNumber
end

function StringTool.getFrontStringByFlag(str,cFlag)
    local index = string.find(str,cFlag)
    if index ~= nil then
        return string.sub(str,1,index - 1)
    end
    return ""
end

function StringTool.getBackStringByFlag(str,cFlag)
    local index = string.find(str,cFlag)
    if index ~= nil then
        return string.sub(str,index + 1,#str)
    end
    return ""
end

-- 去除字符串头部和尾部的空格 \t等
function StringTool.trim(str)
    local tmp = string.gsub(str, "^%s+", "")
    tmp = string.gsub(tmp, "%s+$", "")
    return tmp
end

-- 判断字符中是否存在空格或者其它特殊字符
function StringTool.isSpecialCharsExist(str)
    --对ASCII码中32-126的特殊字符处理包括字母和数字
    local res = false 
    local k = 1  
    while true do
        if k > #str then break end
        local c = string.byte(str,k)
        if not c then break end
        if c >= 32 and c <= 126 then
            res = true
            break
        end
        k = k + 1
    end
    return res
end

function StringTool.splitWithTrim(str, delim)
    local args = {}
    local pattern = '(.-)' .. delim
    local last_end = 1
    local s, e, cap = string.find(str, pattern , 1)
    while s do
        local tmp = StringTool.trim(cap)
        if tmp ~= '' then
            table.insert(args,tmp)
        end
        last_end = e + 1
        s, e, cap = string.find(str, pattern, last_end)
    end
    if last_end <= #str then
        cap = StringTool.trim(string.sub(str, last_end))
        if cap ~= "" then
            table.insert(args,cap)
        end
    end
    return args
end

-- 获取合法的utf8字节数，同时满足大于等于传入的minimumByte
function StringTool.getMinimumValidBytesUTF8(str, minimumByte)
    local bytes = 0
    local left = string.len(str)
    local arr  = {0, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc}
    while left ~= 0 do
        local tmp = string.byte(str, -left)
        local i   = #arr
        while arr[i] do
            if tmp >= arr[i] then
                left = left - i
                bytes = bytes + i
                if  bytes >= minimumByte then 
                    return  bytes
                end
                break
            end
            i = i - 1
        end
    end
    return bytes
end

--获取昵称[取8位]
function StringTool.cutStringByLength(str, len)
    if str == nil then
        return
    end
    len = len or 8
    if string.len(str) > len then
        local validBytes = StringTool.getMinimumValidBytesUTF8(str, len)
        str = string.sub(str,1,validBytes) .. ".."
    end
    return str
end

--获取昵称[取8位]
function StringTool.cutStringByLengthWithDot(str, len)
    if str == nil then
        return
    end
    len = len or 8
    if string.len(str) > len then
        local validBytes = StringTool.getMinimumValidBytesUTF8(str, len)
        str = string.sub(str,1,validBytes)
    end
    return str
end

function StringTool.compareVersions(ver1, ver2)
    local verList1 = string.split(ver1, ".")
    local verList2 = string.split(ver2, ".")
    for i = 1, math.min(#verList1, #verList2) do
        local verItem1 = tonumber(verList1[i])
        local verItem2 = tonumber(verList2[i])
        if verItem1 ~= verItem2 then
            return verItem1 - verItem2
        end
    end
    return 0
end

function StringTool.urlToHttps(url)
	if not url then
		return 
	end
   
	local find = string.find(url, "https")
	if find then
		return url
	end
	
	local rtnUrl = string.gsub(url, "http", "https")

	return rtnUrl
end

function StringTool.getImageNameByUrl(url)
    local imageName,_ = string.gsub(url, "[:/.]", "_")
    return imageName .. ".png"
end

function StringTool.newLineStringByMaxLen(str, maxLen)
    if not str or type(str) ~= "string" then
        return
    end
    if not maxLen or maxLen <= 0 then
        return str
    end
    local tmpStr = clone(str)
    local targetStr = ""

    while( string.len(tmpStr) > maxLen ) do
        local validBytes = StringTool.getMinimumValidBytesUTF8(tmpStr, maxLen)
        targetStr = targetStr .. string.sub(tmpStr,1,validBytes) .. "\n"
        tmpStr = string.sub(tmpStr,validBytes + 1)
    end
    targetStr = targetStr .. tmpStr
    return targetStr
end

-- 获取字符串最后的数字
function StringTool.getLastCharAndToNum(string)
    local len = string.len(string);
    local lastNumber = 0
    for i = 1 , len do
        local tempNumber = tonumber(string.sub(string,- i ))
        if tempNumber then
            lastNumber = tempNumber
        else
            return lastNumber
        end
    end
    return lastNumber
end

--转化为16进制字符
function StringTool.bin2hex(bytes)
    local allHexStr = ""
    for i =1,string.len(bytes) do 
        local charCode = tonumber(string.byte(bytes,i,i))
        local hexStr = string.format("%02X",charCode)
        
        allHexStr = allHexStr .. hexStr
    end 
    return allHexStr
end

function StringTool.privateGuessUTFLen(b)
    if b < 192 then --110xxxxx 
        return 1
    elseif b < 224 then --1110xxxx 
        return 2
    elseif b < 240 then --11110xxx
        return 3
    elseif b < 248 then --111110xx
        return 4
    elseif b < 252 then --1111110x
        return 5
    end
    return 6
end

function StringTool.simpleGetStrWidth(txt)
    local width = 0
    local i = 1
    while i <= #txt do
        local tmp_c = string.byte(txt, i, i)
        if tmp_c <= 127 then
            i = i + 1
            width = width + 1
        else
            i = i + StringTool.privateGuessUTFLen(tmp_c)
            width = width + 2
        end
    end
    return width
end

function StringTool.simpleSubStrUtilWidth(txt, w)
    if w < 1 then
        return ""
    end
    local i = 1
    local tEnd = #txt
    local width = 0
    while i <= #txt do
        local tmp_c = string.byte(txt, i, i)
        width = width + (tmp_c <= 127 and 1 or 2)
        if width > w then
            tEnd = i - 1
            break
        else
            i = i + StringTool.privateGuessUTFLen(tmp_c)
        end
    end
    return string.sub(txt, 1, tEnd)
end

function StringTool.getTrimName(name, length)
    length = length or 6
    if StringTool.simpleGetStrWidth(name) > length then
        name = StringTool.simpleSubStrUtilWidth(name, length) .. "..."
    end
    return name
end

function StringTool.numberToString(number, byte)
    if number < 10000 then
        return tostring(number)
    elseif number % 10000 == 0 then
        return tostring(number / 10000).. "万"
    else
        local divisor = 10000
        local result = number / divisor
        if byte > 0 then
            result = math.floor(result * 10 ^ byte) / 10 ^ byte
            local str = tostring(result)
            local dot_index = string.find(str, '.')
            if dot_index then
                local decimal_part = string.sub(str, dot_index + byte)
                if tonumber(decimal_part) == 0 then
                    str = string.sub(str, 1, dot_index - 1)
                end
            end
            return str.. "万"
        else
            return tostring(math.floor(number / divisor)).. "万"
        end
    end
end

function StringTool.replaceMatchStr(str)
    if XH.areaData and XH.areaData:isSupportMatch() and str and str ~= "" then
        str = string.gsub(str, "亲友圈", "比赛场")
        str = string.gsub(str, "圈子", "比赛场")
        str = string.gsub(str, "圈主消耗", "比赛场消耗")
        str = string.gsub(str, "圈主支付", "比赛场消耗")
        str = string.gsub(str, "（圈主）消耗", "（比赛场）消耗")
        str = string.gsub(str, "战绩管理", "比赛战绩")
        str = string.gsub(str, "加入黑名单", "禁止参赛")
        str = string.gsub(str, "互斥名单", "禁止同赛")
        str = string.gsub(str, "限制同桌", "禁止同赛")
        str = string.gsub(str, "互斥玩家", "禁止同赛玩家")
        str = string.gsub(str, "无法同桌游戏", "无法同桌比赛")
        str = string.gsub(str, "固定玩法", "比赛玩法")
        str = string.gsub(str, "成员管理", "成员详情")
        str = string.gsub(str, "圈主", "领队")
        str = string.gsub(str, "管理员", "副领队")
        str = string.gsub(str, "冠军支付", "冠军消耗")
        str = string.gsub(str, "平摊支付", "平摊消耗")
        str = string.gsub(str, "战绩查询", "比赛战绩")
        str = string.gsub(str, "房主付", "房主消耗")
        str = string.gsub(str, "房主支付", "房主消耗")
        str = string.gsub(str, "平摊付", "平摊消耗")
        if XH.teaHouseManager:isInTeaHouse() then
            str = string.gsub(str, "解散包厢", "解散比赛")
        end
    end
    return str
end

function StringTool.formatRemainingTime(seconds)
    -- 计算小时、分钟和秒
    local hours = math.floor(seconds / 3600)
    local minutes = math.floor(seconds % 3600 / 60)
    local seconds = seconds % 60

    -- 格式化为 HH:MM:SS，如果任何单位小于10，添加前导零
    return string.format("%02d:%02d:%02d", hours, minutes, seconds)
end


return StringTool   *  