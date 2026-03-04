execute if block 38 -21 28 light_weighted_pressure_plate unless block 38 -21 28 light_weighted_pressure_plate[power=0] unless score _3_wart ingame_timer matches 1.. run scoreboard players set _3_wart ingame_timer 160
execute if score _3_wart ingame_timer matches 1.. run scoreboard players remove _3_wart ingame_timer 1

execute if score _3_wart ingame_timer matches 155 positioned 34 -23 29 run function game:3/wart/summon
execute if score _3_wart ingame_timer matches 145 positioned 34 -22 29 run function game:3/wart/summon
execute if score _3_wart ingame_timer matches 135 positioned 34 -21 29 run function game:3/wart/summon

execute if score _3_wart ingame_timer matches 5 run setblock 34 -23 29 lava destroy
execute if score _3_wart ingame_timer matches 10 run setblock 34 -22 29 air destroy
execute if score _3_wart ingame_timer matches 15 run setblock 34 -21 29 air destroy 