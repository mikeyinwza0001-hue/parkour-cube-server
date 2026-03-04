execute if block 14 248 19 light_weighted_pressure_plate unless block 14 248 19 light_weighted_pressure_plate[power=0] unless score 16_powder ingame_timer matches 1.. run scoreboard players set 16_powder ingame_timer 1
execute if score 16_powder ingame_timer matches 1.. run scoreboard players add 16_powder ingame_timer 1

execute if score 16_powder ingame_timer matches 4 positioned 18 248 19 run function game:16/set/red
execute if score 16_powder ingame_timer matches 7 positioned 21 249 18 run function game:16/set/orange
execute if score 16_powder ingame_timer matches 10 positioned 23 250 15 run function game:16/set/yellow
execute if score 16_powder ingame_timer matches 13 positioned 23 251 12 run function game:16/set/lime
execute if score 16_powder ingame_timer matches 16 positioned 22 252 9 run function game:16/set/light_blue
execute if score 16_powder ingame_timer matches 19 positioned 20 253 7 run function game:16/set/cyan
execute if score 16_powder ingame_timer matches 22 positioned 17 254 5 run function game:16/set/blue
execute if score 16_powder ingame_timer matches 25 positioned 14 255 6 run function game:16/set/purple
execute if score 16_powder ingame_timer matches 28 positioned 12 256 8 run function game:16/set/magenta
execute if score 16_powder ingame_timer matches 31 positioned 11 257 11 run function game:16/set/pink

execute if score 16_powder ingame_timer matches 204 positioned 18 248 19 run function game:16/destroy
execute if score 16_powder ingame_timer matches 207 positioned 21 249 18 run function game:16/destroy
execute if score 16_powder ingame_timer matches 210 positioned 23 250 15 run function game:16/destroy
execute if score 16_powder ingame_timer matches 213 positioned 23 251 12 run function game:16/destroy
execute if score 16_powder ingame_timer matches 216 positioned 22 252 9 run function game:16/destroy
execute if score 16_powder ingame_timer matches 219 positioned 20 253 7 run function game:16/destroy
execute if score 16_powder ingame_timer matches 222 positioned 17 254 5 run function game:16/destroy
execute if score 16_powder ingame_timer matches 225 positioned 14 255 6 run function game:16/destroy
execute if score 16_powder ingame_timer matches 228 positioned 12 256 8 run function game:16/destroy
execute if score 16_powder ingame_timer matches 231 positioned 11 257 11 run function game:16/destroy


execute if score 16_powder ingame_timer matches 250.. run scoreboard players set 16_powder ingame_timer 0