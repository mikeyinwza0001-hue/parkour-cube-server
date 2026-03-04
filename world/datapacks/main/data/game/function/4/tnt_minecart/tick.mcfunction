execute if entity @a[x=-40,y=-62,z=-30,dx=116,dy=162,dz=141] unless entity @e[tag=tm1_node] run function game:4/tnt_minecart/node/summon
execute unless entity @a[x=-40,y=-62,z=-30,dx=116,dy=162,dz=141] if entity @e[tag=tm1_node] run function game:4/tnt_minecart/node/clear

execute unless entity @a[x=-40,y=-62,z=-30,dx=116,dy=162,dz=141] if entity @e[tag=tntminecart1] as @e[tag=tntminecart1] at @s run function common:ingame/minecart/remove
execute as @e[tag=tntminecart1] at @s run function game:4/tnt_minecart/minecart/tick