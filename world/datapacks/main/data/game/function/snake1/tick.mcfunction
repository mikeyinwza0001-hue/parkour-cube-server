execute unless entity @e[tag=snake1snake] if entity @a[x=-18,y=-52,z=-1,dx=41,dy=27,dz=45,gamemode=!spectator] run function game:snake1/summon
execute unless entity @a[x=-18,y=-52,z=-1,dx=41,dy=27,dz=45,gamemode=!spectator] if entity @e[tag=snake1snake] run function game:snake1/clear

execute if block 8 -42 13 light_weighted_pressure_plate unless block 8 -42 13 light_weighted_pressure_plate[power=0] as @e[tag=snake1begin] unless entity @e[tag=snake1begin,scores={snake1timer=1..}] at @s run scoreboard players set @s snake1timer 1