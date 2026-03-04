execute unless entity @e[tag=snake2snake] if entity @a[x=15,y=137,z=-8,dx=53,dy=30,dz=60,gamemode=!spectator] run function game:snake2/summon
execute unless entity @a[x=15,y=137,z=-8,dx=53,dy=30,dz=60,gamemode=!spectator] if entity @e[tag=snake2snake] run function game:snake2/clear

execute if block 36 151 18 light_weighted_pressure_plate unless block 36 151 18 light_weighted_pressure_plate[power=0] as @e[tag=snake2begin] unless entity @e[tag=snake2begin,scores={snake2timer=1..}] at @s run scoreboard players set @s snake2timer 1