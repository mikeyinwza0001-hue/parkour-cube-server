set args = WScript.Arguments
if args.Count < 2 then
    WScript.Quit 1
end if

dim cp, max
cp = args(0)
max = args(1)

Set WshShell = CreateObject("WScript.Shell")
WshShell.Run "node ""d:\Coding Project\Games\2\minecraft-server\plugins\Skript\scripts\cp_overlay\send.js"" " & cp & " " & max, 0, False
