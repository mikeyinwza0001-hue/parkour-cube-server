execute if score _3_slime ingame_timer matches 1.. run scoreboard players remove _3_slime ingame_timer 1

execute if score _3_slime ingame_timer matches 140 positioned 35 -16 45 run function game:3/slime/set_slime
execute if score _3_slime ingame_timer matches 137 positioned 34 -16 45 run function game:3/slime/set_slime
execute if score _3_slime ingame_timer matches 134 positioned 34 -16 46 run function game:3/slime/set_slime
execute if score _3_slime ingame_timer matches 131 positioned 35 -16 46 run function game:3/slime/set_slime

execute if score _3_slime ingame_timer matches 5 run setblock 35 -16 45 nether_wart_block destroy
execute if score _3_slime ingame_timer matches 8 run setblock 34 -16 45 nether_wart_block destroy
execute if score _3_slime ingame_timer matches 11 run setblock 34 -16 46 nether_wart_block destroy 
execute if score _3_slime ingame_timer matches 14 run setblock 35 -16 46 nether_wart_block destroy 