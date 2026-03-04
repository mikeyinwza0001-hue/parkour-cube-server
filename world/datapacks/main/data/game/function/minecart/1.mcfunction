execute unless entity @a[x=9,y=87,z=22,distance=..2,nbt={RootVehicle:{Entity:{id:"minecraft:minecart"}}}] run setblock 9 85 22 stone
execute if entity @a[x=9,y=87,z=22,distance=..2,nbt={RootVehicle:{Entity:{id:"minecraft:minecart"}}}] run setblock 9 85 22 redstone_torch

execute if score minecart1 ingame_timer matches 1.. run scoreboard players remove minecart1 ingame_timer 1
execute as @e[type=minecart,x=9,y=87,z=22,distance=..1] run scoreboard players set minecart1 ingame_timer 50
execute if entity @a[x=9,y=87,z=22,distance=..10] unless score minecart1 ingame_timer matches 1.. run summon minecart 9 87 22 {Tags:["minecart1"]}

execute as @e[type=minecart,tag=minecart1,x=9,y=87,z=22,distance=1..] at @s unless entity @a[distance=..1.5,nbt={RootVehicle:{Entity:{id:"minecraft:minecart",Tags:["minecart1"]}}}] run function common:ingame/minecart/remove
execute as @e[type=minecart,tag=minecart1] at @s unless entity @a[distance=..10] run kill @s