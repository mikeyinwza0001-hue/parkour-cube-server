execute if score _3_strider ingame_timer matches 1.. run scoreboard players remove _3_strider ingame_timer 1
execute as @e[type=strider,x=14,y=-12.5,z=38,distance=..1] run scoreboard players set _3_strider ingame_timer 50
execute if entity @a[x=14,y=-12.5,z=38,distance=..10] unless score _3_strider ingame_timer matches 1.. run summon strider 14 -12.5 38 {Tags:["3_strider"],Invulnerable:1b,Rotation:[135.0f,30.0f],equipment:{saddle:{id:saddle}}}
execute as @e[type=strider,tag=3_strider,x=14,y=-12.5,z=38,distance=..1] at @s unless entity @a[distance=..2.5,nbt={RootVehicle:{Entity:{id:"minecraft:strider",Tags:["3_strider"]}}}] run data merge entity @s {Pos:[14.5,-12.5,38.5]}
execute as @e[type=strider,tag=3_strider,x=14,y=-12.5,z=38,distance=1..] at @s unless entity @a[distance=..2.5,nbt={RootVehicle:{Entity:{id:"minecraft:strider",Tags:["3_strider"]}}}] run function common:ingame/minecart/remove
execute as @e[type=strider,tag=3_strider] at @s unless entity @a[distance=..12] run function common:ingame/minecart/remove


execute as @a[nbt={RootVehicle:{Entity:{id:"minecraft:strider",Tags:["3_strider"]}}},nbt=!{Inventory:[{Slot:4b,components:{"minecraft:custom_data":{3_wand:true}}}]}] run clear @s warped_fungus_on_a_stick
execute as @a[nbt={RootVehicle:{Entity:{id:"minecraft:strider",Tags:["3_strider"]}}},nbt=!{Inventory:[{Slot:4b,components:{"minecraft:custom_data":{3_wand:true}}}]}] run item replace entity @s hotbar.4 with warped_fungus_on_a_stick[unbreakable={},tooltip_display={hidden_components:["unbreakable","enchantments"]},custom_data={3_wand:true}]
execute as @a[nbt=!{RootVehicle:{Entity:{id:"minecraft:strider",Tags:["3_strider"]}}},nbt={Inventory:[{Slot:4b,components:{"minecraft:custom_data":{3_wand:true}}}]}] run clear @s warped_fungus_on_a_stick


execute if entity @e[tag=3_strider,x=31,y=-23,z=30,dx=10,dy=3,dz=8] unless score _3_gate ingame_timer matches 1.. run scoreboard players set _3_gate ingame_timer 100
execute if entity @e[tag=3_strider,x=31,y=-23,z=30,dx=10,dy=3,dz=8] if score _3_gate ingame_timer matches 40..80 run scoreboard players set _3_gate ingame_timer 80
execute if score _3_gate ingame_timer matches 81.. run scoreboard players remove _3_gate ingame_timer 1
execute if score _3_gate ingame_timer matches 40..80 unless entity @e[tag=3_strider,x=31,y=-23,z=30,dx=10,dy=3,dz=8] run scoreboard players remove _3_gate ingame_timer 1
execute if score _3_gate ingame_timer matches 1..39 run scoreboard players remove _3_gate ingame_timer 1

execute if score _3_gate ingame_timer matches 98 positioned ~ -22 ~ run function game:3/strider/gate_open
execute if score _3_gate ingame_timer matches 90 positioned ~ -21 ~ run function game:3/strider/gate_open
execute if score _3_gate ingame_timer matches 82 positioned ~ -20 ~ run function game:3/strider/gate_open
execute if score _3_gate ingame_timer matches 22 positioned ~ -22 ~ run function game:3/strider/gate_close
execute if score _3_gate ingame_timer matches 30 positioned ~ -21 ~ run function game:3/strider/gate_close
execute if score _3_gate ingame_timer matches 38 positioned ~ -20 ~ run function game:3/strider/gate_close

execute if score _3_gate ingame_timer matches 40..100 unless block 37 -17 40 redstone_block run fill 37 -17 40 38 -17 40 redstone_block
execute unless score _3_gate ingame_timer matches 40..100 if block 37 -17 40 redstone_block run fill 37 -17 40 38 -17 40 netherrack