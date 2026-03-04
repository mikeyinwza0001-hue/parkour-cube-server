execute if block 12 225 22 light_weighted_pressure_plate unless block 12 225 22 light_weighted_pressure_plate[power=0] unless score 15_pillar ingame_timer matches 1.. run scoreboard players set 15_pillar ingame_timer 1
execute if score 15_pillar ingame_timer matches 1.. run scoreboard players add 15_pillar ingame_timer 1

execute if score 15_pillar ingame_timer matches 4 positioned 13 220 25 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 6 positioned 13 221 25 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 8 positioned 13 222 25 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 10 positioned 13 223 25 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 12 positioned 15 220 29 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 14 positioned 15 221 29 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 16 positioned 15 222 29 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 18 positioned 15 223 29 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 20 positioned 15 224 29 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 22 positioned 19 220 26 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 24 positioned 19 221 26 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 26 positioned 19 222 26 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 28 positioned 19 223 26 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 30 positioned 19 224 26 run function game:15/pillar/rise
execute if score 15_pillar ingame_timer matches 32 positioned 19 225 26 run function game:15/pillar/rise

execute if score 15_pillar ingame_timer matches 204 positioned 13 223 25 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 206 positioned 13 222 25 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 208 positioned 13 221 25 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 210 positioned 13 220 25 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 212 positioned 15 224 29 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 214 positioned 15 223 29 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 216 positioned 15 222 29 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 218 positioned 15 221 29 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 220 positioned 15 220 29 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 222 positioned 19 225 26 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 224 positioned 19 224 26 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 226 positioned 19 223 26 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 228 positioned 19 222 26 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 230 positioned 19 221 26 run function game:15/pillar/down
execute if score 15_pillar ingame_timer matches 232 positioned 19 220 26 run function game:15/pillar/down

execute if score 15_pillar ingame_timer matches 250.. run scoreboard players set 15_pillar ingame_timer 0