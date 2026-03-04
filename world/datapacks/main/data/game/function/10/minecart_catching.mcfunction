execute as @e[tag=10_minecart_c] at @s unless entity @s[x=47,y=108,z=46,dx=-13,dy=7,dz=-23] run function common:ingame/minecart/remove

execute as @e[tag=10_minecart_c] at @s if block ~ ~ ~ rail run data merge entity @s {Motion:[0.075,0.0,0.0]}
execute as @e[tag=10_minecart_c] at @s if block ~-1 ~ ~ rail run data merge entity @s {Motion:[0.075,0.0,0.0]}

execute if entity @a[x=52,y=110,z=15,dx=-19,dy=6,dz=31] unless score 10_minecart_c ingame_timer matches 1.. run scoreboard players set 10_minecart_c ingame_timer 1
execute if score 10_minecart_c ingame_timer matches 1.. run scoreboard players add 10_minecart_c ingame_timer 1
execute if score 10_minecart_c ingame_timer matches 30 run summon minecart 38 113 30 {Tags:["10_minecart_c"]}
execute if score 10_minecart_c ingame_timer matches 65 run summon minecart 37 113 36 {Tags:["10_minecart_c"]}
execute if score 10_minecart_c ingame_timer matches 100 run summon minecart 36 113 41 {Tags:["10_minecart_c"]}
execute if score 10_minecart_c ingame_timer matches 120.. run scoreboard players set 10_minecart_c ingame_timer 0
