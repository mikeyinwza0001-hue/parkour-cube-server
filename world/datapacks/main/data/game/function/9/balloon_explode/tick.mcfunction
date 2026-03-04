execute if block -6 104 40 minecraft:oak_pressure_plate[powered=true] unless score _9_balloon ingame_timer matches 1.. run scoreboard players set _9_balloon ingame_timer 1
execute if score _9_balloon ingame_timer matches 1 run setblock -6 104 40 air destroy

execute if score _9_balloon ingame_timer matches 1.. run scoreboard players add _9_balloon ingame_timer 1

execute if score _9_balloon ingame_timer matches 5 run function game:9/balloon_explode/explode
execute if score _9_balloon ingame_timer matches 180 run function game:9/balloon_explode/recover

execute if score _9_balloon ingame_timer matches 220 run setblock -6 104 40 oak_pressure_plate
execute if score _9_balloon ingame_timer matches 260 run scoreboard players set _9_balloon ingame_timer 0