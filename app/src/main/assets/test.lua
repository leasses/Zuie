--test.lua

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
   anlua.print(msg)
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
           end
           time = os.clock()
           return p or 0
       end
   end

   local Test = anlua.import("leasses.zuie.dev.AnluaTest")
   --local TestIns = Test();

   print("---------------------------\nThe value of class leasses.anlua.Test:",
           Test)
    print("---------------------------\nThe value of class leasses.anlua.Test:",
           Test)
   --print(Test[1])
   --timeUsed()
   print("---------------------------\nGet a static string:",
           Test.staticString)
   print("---------------------------\nGet an inner class:",
           Test.LargeClass)
    print("---------------------------\nGet a field:",
           Test.LargeClass.FIELD_0)

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
   print("--------------------------call Test::ohh", Test.ohh(1, 2))
   print("--------------------------call test::toString", test.toString())
   --print("---------------------------this is 11", TestIns())
   --timeUsed()
   timeUsed()
end

--do
--    local ArrayAdapter = anlua.import "android.widget.ArrayAdapter"
--    local LinearLayout = anlua.import "android.widget.LinearLayout"
--    local TextView = anlua.import "android.widget.TextView"
--    local File = anlua.import "java.io.File"
--    local ListView = anlua.import "android.widget.ListView"
--    local AlertDialog = anlua.import "android.app.AlertDialog"
--
--    function ChoiceFile(StartPath, callback)
--        --创建ListView作为文件列表
--        lv = ListView(activity).setFastScrollEnabled(true)
--        --创建路径标签
--        cp = TextView(activity)
--        lay = LinearLayout(activity).setOrientation(1).addView(cp).addView(lv)
--        ChoiceFile_dialog = AlertDialog.Builder(activity)--创建对话框
--                                       .setTitle("选择文件")
--                                       .setView(lay)
--                                       .show()
--        adp = ArrayAdapter(activity, android.R.layout.simple_list_item_1)
--        lv.setAdapter(adp)
--        function SetItem(path)
--            path = tostring(path)
--            adp.clear()--清空适配器
--            cp.Text = tostring(path)--设置当前路径
--            if path ~= "/" then
--                --不是根目录则加上../
--                adp.register("../")
--            end
--            ls = File(path).listFiles()
--            if ls ~= nil then
--                ls = luajava.astable(File(path).listFiles()) --全局文件列表变量
--                table.sort(ls, function(a, b)
--                    return (a.isDirectory() ~= b.isDirectory() and a.isDirectory()) or ((a.isDirectory() == b.isDirectory()) and a.Name < b.Name)
--                end)
--            else
--                ls = {}
--            end
--            for index, c in ipairs(ls) do
--                if c.isDirectory() then
--                    --如果是文件夹则
--                    adp.register(c.Name .. "/")
--                else
--                    --如果是文件则
--                    adp.register(c.Name)
--                end
--            end
--        end
--        lv.onItemClick = function(l, v, p, s)
--            --列表点击事件
--            项目 = tostring(v.Text)
--            if tostring(cp.Text) == "/" then
--                路径 = ls[p + 1]
--            else
--                路径 = ls[p]
--            end
--
--            if 项目 == "../" then
--                SetItem(File(cp.Text).getParentFile())
--            elseif 路径.isDirectory() then
--                SetItem(路径)
--            elseif 路径.isFile() then
--                callback(tostring(路径))
--                ChoiceFile_dialog.hide()
--            end
--
--        end
--
--        SetItem(StartPath)
--    end
--
--    --ChoiceFile(StartPath,callback)
--    --第一个参数为初始化路径,第二个为回调函数
--    --原创
--end

return "haha", 114514.198919