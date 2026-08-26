local Json = {}
Json.null = {}

local escapes = {
    ['"'] = '\\"',
    ['\\'] = '\\\\',
    ['\b'] = '\\b',
    ['\f'] = '\\f',
    ['\n'] = '\\n',
    ['\r'] = '\\r',
    ['\t'] = '\\t'
}

local function encode_string(value)
    return '"' .. value:gsub('[%z\1-\31\\"]', function(character)
        return escapes[character] or string.format("\\u%04x", character:byte())
    end) .. '"'
end

local function is_array(value)
    local count = 0
    local maximum = 0
    for key in pairs(value) do
        if type(key) ~= "number" or key < 1 or key % 1 ~= 0 then
            return false
        end
        count = count + 1
        maximum = math.max(maximum, key)
    end
    return maximum == count and count > 0
end

local function encode_value(value, stack)
    local value_type = type(value)
    if value == Json.null or value_type == "nil" then
        return "null"
    end
    if value_type == "boolean" then
        return value and "true" or "false"
    end
    if value_type == "number" then
        if value ~= value or value == math.huge or value == -math.huge then
            error("cannot encode a non-finite number")
        end
        return tostring(value)
    end
    if value_type == "string" then
        return encode_string(value)
    end
    if value_type ~= "table" then
        error("cannot encode value of type " .. value_type)
    end
    if stack[value] then
        error("cannot encode a circular table")
    end
    stack[value] = true

    local result = {}
    if is_array(value) then
        for index = 1, #value do
            result[index] = encode_value(value[index], stack)
        end
        stack[value] = nil
        return "[" .. table.concat(result, ",") .. "]"
    end

    for key, item in pairs(value) do
        if type(key) ~= "string" then
            error("JSON object keys must be strings")
        end
        result[#result + 1] = encode_string(key) .. ":" .. encode_value(item, stack)
    end
    table.sort(result)
    stack[value] = nil
    return "{" .. table.concat(result, ",") .. "}"
end

function Json.encode(value)
    return encode_value(value, {})
end

local function utf8_character(codepoint)
    if codepoint <= 0x7f then
        return string.char(codepoint)
    end
    if codepoint <= 0x7ff then
        return string.char(0xc0 + math.floor(codepoint / 0x40), 0x80 + codepoint % 0x40)
    end
    if codepoint <= 0xffff then
        return string.char(
            0xe0 + math.floor(codepoint / 0x1000),
            0x80 + math.floor(codepoint / 0x40) % 0x40,
            0x80 + codepoint % 0x40
        )
    end
    return string.char(
        0xf0 + math.floor(codepoint / 0x40000),
        0x80 + math.floor(codepoint / 0x1000) % 0x40,
        0x80 + math.floor(codepoint / 0x40) % 0x40,
        0x80 + codepoint % 0x40
    )
end

local function decoder(source)
    local position = 1
    local length = #source

    local function fail(message)
        error(string.format("invalid JSON at byte %d: %s", position, message), 0)
    end

    local function skip_space()
        local _, finish = source:find("^[ \n\r\t]*", position)
        position = (finish or position - 1) + 1
    end

    local parse_value

    local function parse_string()
        position = position + 1
        local parts = {}
        local start = position
        while position <= length do
            local byte = source:byte(position)
            if byte == 34 then
                parts[#parts + 1] = source:sub(start, position - 1)
                position = position + 1
                return table.concat(parts)
            end
            if byte == 92 then
                parts[#parts + 1] = source:sub(start, position - 1)
                local escaped = source:sub(position + 1, position + 1)
                local simple = {
                    ['"'] = '"', ['\\'] = '\\', ['/'] = '/',
                    b = '\b', f = '\f', n = '\n', r = '\r', t = '\t'
                }
                if simple[escaped] then
                    parts[#parts + 1] = simple[escaped]
                    position = position + 2
                elseif escaped == "u" then
                    local hex = source:sub(position + 2, position + 5)
                    local codepoint = tonumber(hex, 16)
                    if not codepoint or #hex ~= 4 then
                        fail("invalid unicode escape")
                    end
                    position = position + 6
                    if codepoint >= 0xd800 and codepoint <= 0xdbff
                            and source:sub(position, position + 1) == "\\u" then
                        local low = tonumber(source:sub(position + 2, position + 5), 16)
                        if low and low >= 0xdc00 and low <= 0xdfff then
                            codepoint = 0x10000 + (codepoint - 0xd800) * 0x400 + low - 0xdc00
                            position = position + 6
                        end
                    end
                    parts[#parts + 1] = utf8_character(codepoint)
                else
                    fail("invalid string escape")
                end
                start = position
            elseif byte < 32 then
                fail("control character in string")
            else
                position = position + 1
            end
        end
        fail("unterminated string")
    end

    local function parse_number()
        local start = position
        local token = source:match("^-?%d+%.?%d*[eE]?[+-]?%d*", position)
        if not token or token == "" then
            fail("invalid number")
        end
        position = position + #token
        local value = tonumber(source:sub(start, position - 1))
        if not value then
            fail("invalid number")
        end
        return value
    end

    local function parse_array()
        position = position + 1
        skip_space()
        local result = {}
        if source:sub(position, position) == "]" then
            position = position + 1
            return result
        end
        while true do
            result[#result + 1] = parse_value()
            skip_space()
            local character = source:sub(position, position)
            if character == "]" then
                position = position + 1
                return result
            end
            if character ~= "," then
                fail("expected ',' or ']'")
            end
            position = position + 1
            skip_space()
        end
    end

    local function parse_object()
        position = position + 1
        skip_space()
        local result = {}
        if source:sub(position, position) == "}" then
            position = position + 1
            return result
        end
        while true do
            if source:sub(position, position) ~= '"' then
                fail("expected object key")
            end
            local key = parse_string()
            skip_space()
            if source:sub(position, position) ~= ":" then
                fail("expected ':'")
            end
            position = position + 1
            skip_space()
            result[key] = parse_value()
            skip_space()
            local character = source:sub(position, position)
            if character == "}" then
                position = position + 1
                return result
            end
            if character ~= "," then
                fail("expected ',' or '}'")
            end
            position = position + 1
            skip_space()
        end
    end

    function parse_value()
        skip_space()
        local character = source:sub(position, position)
        if character == '"' then
            return parse_string()
        end
        if character == "{" then
            return parse_object()
        end
        if character == "[" then
            return parse_array()
        end
        if character == "-" or character:match("%d") then
            return parse_number()
        end
        if source:sub(position, position + 3) == "true" then
            position = position + 4
            return true
        end
        if source:sub(position, position + 4) == "false" then
            position = position + 5
            return false
        end
        if source:sub(position, position + 3) == "null" then
            position = position + 4
            return Json.null
        end
        fail("unexpected token")
    end

    local result = parse_value()
    skip_space()
    if position <= length then
        fail("trailing content")
    end
    return result
end

function Json.decode(source)
    if type(source) ~= "string" then
        error("JSON source must be a string")
    end
    return decoder(source)
end

return Json
