--

local function dump(t)
    local r = "{"
    for k, v in pairs(t) do
        r = r .. string.format("\n(%s) %s = \t(%s) \t%s",
                type(k), tostring(k), type(k), tostring(v))
    end
    return r .. "\n}"
end

_G.print = function(...)
    local msg = ""
    local len = select("#", ...)
    for i = 1, len do
        v = select(i, ...)
        if type(v) == "table" then
            msg = msg .. "  " .. dump(v)
        else
            msg = msg .. "  " .. tostring(v)
        end
    end
    jua.print(msg)
end

print("Hello", "I am from lua")
print("It will print two nil values:", nil, abc)
print("It will dump two table",
        { k1 = "k1", k2 = 2, },
        { "a", "b", 1, 2 })
do
    local timeUsed
    do
        local time, p
        function timeUsed()
            if time then
                p = os.clock() - time
                --print("used", p)
            end
            time = os.clock()
            return p or 0
        end
    end

    local Test = jua.import("leasses/jua/Test")
    --local TestIns = Test();

    print("---------------------------\nThe value of class leasses.jua.Test:",
            Test)
    --print(Test[1])
    --timeUsed()
    print("---------------------------\nTest for getting an string:",
            Test.pb_f_s_f)
    print("---------------------------\nTest for getting an array:",
            Test.strArray)
    print("---------------------------\nTest for indexing a java array:",
            Test.strArray[1], Test.strArray[2], Test.strArray[3])

    --timeUsed()
    --print("---------------------------this is 5", Test())
    --timeUsed()
    --print("---------------------------this is 6", Test())
    --timeUsed()
    ----print("---------------------------this is 7", TestIns.pb_f_f)
    --timeUsed()
    --print("---------------------------this is 8", Test.pb_s_f)
    timeUsed()
    --print("---------------------------this is 9", Test.public_static_boolean_true)
    timeUsed()
    local test = Test(12, true, nil);
    --print("---------------------------the instance of test is", TestIns)
    print("--------------------------call test.number::", test.number)
    print("--------------------------call Test::ohh", Test.ohh(1,2))
    print("--------------------------call test::toString", test.toString())
    --print("---------------------------this is 11", TestIns())
    --timeUsed()
    timeUsed()
end

return "haha",114514.198919