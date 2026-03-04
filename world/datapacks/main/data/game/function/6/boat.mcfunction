execute if score boat1 ingame_timer matches 1.. run scoreboard players remove boat1 ingame_timer 1
execute as @e[type=birch_boat,x=1,y=58,z=42,distance=..1] run scoreboard players set boat1 ingame_timer 50
execute if entity @a[x=1,y=58,z=42,distance=..10] unless score boat1 ingame_timer matches 1.. run summon birch_boat 1 58 42 {Tags:["boat1"]}

execute as @e[type=birch_boat,tag=boat1,x=1,y=58,z=42,distance=3..] run tag @s add boat1_remove
execute as @e[type=birch_boat,tag=boat1,x=1,y=58,z=42,distance=3..] at @s on passengers on vehicle run tag @s remove boat1_remove
execute as @e[tag=boat1,tag=boat1_remove] at @s run function common:ingame/minecart/remove
execute as @e[type=birch_boat,tag=boat1] at @s unless entity @a[distance=..10] run kill @s